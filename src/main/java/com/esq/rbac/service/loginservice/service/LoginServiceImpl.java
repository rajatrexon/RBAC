package com.esq.rbac.service.loginservice.service;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import com.esq.rbac.service.application.applicationmaintenance.util.ApplicationDownInfo;
import com.esq.rbac.service.application.childapplication.appurldata.AppUrlData;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.group.service.GroupDal;
import com.esq.rbac.service.ldapuserservice.service.LdapUserService;
import com.esq.rbac.service.loginlog.domain.LoginLog;
import com.esq.rbac.service.loginlog.domain.LoginType;
import com.esq.rbac.service.loginlog.service.LoginLogService;
import com.esq.rbac.service.loginservice.email.EmailDal;
import com.esq.rbac.service.loginservice.embedded.*;
import com.esq.rbac.service.lookup.ApplicationMaintenanceCache;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.restriction.domain.Restriction;
import com.esq.rbac.service.restriction.iprange.RestrictionIpRange;
import com.esq.rbac.service.restriction.util.RestrictionUtil;
import com.esq.rbac.service.sessiondata.service.SessionService;
import com.esq.rbac.service.sessionregistry.registry.NativeApplicationSession;
import com.esq.rbac.service.sessionregistry.registry.SessionRegistry;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.embedded.UserIdentity;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.RBACUtil;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    private static final int DEFAULT_consecutiveLoginFailures = 6;
    private static final int DEFAULT_noPasswordChangeDays = 90;
    private static final int DEFAULT_noLoginDays = 50;
    private static final int DEFAULT_noOfTimesPwdChange = 0;
    private static final int DEFAULT_ageOfPasswordInHour = 0;
    private static final String SERVICE_TICKET_PREFIX = "ST-";
    private static final int SERVICE_TICKET_LENGTH = 40;
    public static final String CONF_CONSECUTIVE_LOGIN_FAILURES = "rbac.loginPolicy.consecutiveLoginFailures";
    public static final String CONF_NO_PASSWORD_CHANGE_DAYS = "rbac.loginPolicy.noPasswordChangeDays";
    public static final String CONF_NO_PASSWORD_CHANGE_ACTION = "rbac.loginPolicy.noPasswordChangeAction";
    public static final String CONF_NO_LOGIN_DAYS = "rbac.loginPolicy.noLoginDays";
    public static final String CONF_DOMAIN_SERVER_URL = "rbac.loginWindows.domainServerUrl";
    public static final String CONF_DOMAIN_NAME = "rbac.loginWindows.domainName";
    public static final String CONF_WINDOWS_LOGIN_ENABLED = "rbac.windowsLoginType.enabled"; // LDAP Login
    public static final String CONF_SITEMINDER_AUTO_LOGIN_ENABLED = "rbac.siteMinderLoginType.enabled";
    public static final String CONF_GENERIC_HEADER_AUTH_ENABLED = "rbac.genericHeaderAuthLoginType.enabled";
    public static final String CONF_FORGEROCK_AUTO_LOGIN_ENABLED = "rbac.forgeRockLoginType.enabled";
    public static final String CONF_WINDOWS_AUTO_LOGIN_ENABLED = "rbac.integratedWindowsLoginType.enabled";
    public static final String CONF_WINDOWS_LOGIN_CHANGE_PASS_ENABLED = "rbac.windowsLogin.changePass.enabled";
    public static final String CONF_RBAC_LOGIN_ENABLED = "rbac.rbacLoginType.enabled";
    public static final String CONF_NO_PASSWORD_CHANGE_TIMES = "rbac.passwordPolicy.nmbrOfPasswordChange";
    public static final String CONF_AGE_OF_PASSWORD_IN_HOUR = "rbac.passwordPolicy.ageOfPasswordInHour";
    public static final String CONF_AZURE_ACTIVE_DIRECTORY_LOGIN_ENABLED = "rbac.AzureActiveDirectoryLoginType.enabled";
    public static final String CONF_AZURE_CHANGE_PASSWORD = "rbac.AzureActiveDirectory.changePassword.enabled";
    public static final String CONF_AZURE_EMAIL_ASUSERNAME = "rbac.AzureActiveDirectory.changePassword.enabled";

    public static final int DEFAULT_DONT_EXPIRE_PASSWORD_VALUE = 0;
    public static final int DEFAULT_NO_ACTIVITY_DONT_LOCK_VALUE = 0;
    private final ConcurrentHashMap<String, String> ticketUsername = new ConcurrentHashMap<String, String>();
    private final ConcurrentHashMap<String, String> ticketUsernameNative = new ConcurrentHashMap<String, String>();


    private EntityManager em;

    @Autowired
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }


    private UserDal userDal;
    @Autowired
    public void setUserDal(UserDal userDal) {
        this.userDal = userDal;
    }




    private GroupDal groupDal;

    @Autowired
    public void setGroupDal(GroupDal groupDal) {
        this.groupDal = groupDal;
    }


    private LoginLogService loginLogDal;

    @Autowired
    public void setLoginLogDal(LoginLogService loginLogDal) {
        this.loginLogDal = loginLogDal;
    }

    private Configuration configuration;

    @Autowired
    public void setConfiguration(@Qualifier("DatabaseConfigurationWithCache") Configuration configuration) {
        log.trace("setConfiguration; {}", configuration);
        this.configuration = configuration;
    }

    private int consecutiveLoginFailures = DEFAULT_consecutiveLoginFailures;
    private int noPasswordChangeDays = DEFAULT_noPasswordChangeDays;
    private int noLoginDays = DEFAULT_noLoginDays;
    private int noOfTimesPwdChange = DEFAULT_noOfTimesPwdChange;
    private int ageOfPasswordInHour = DEFAULT_ageOfPasswordInHour;
    private String noPasswordChangeAction = LoginResponse.CHANGE_PASSWORD_REQUIRED;
    private String domainServerUrl = "";
    private String domainName = "";
    private boolean windowsLoginTypeEnabled;
    private boolean integratedWindowsLoginTypeEnabled;
    private boolean azureActiveDirectoryLoginTypeEnabled;
    private boolean siteMinderLoginTypeEnabled;
    private boolean genericHeaderLoginTypeEnabled;
    private boolean forgeRockLoginTypeEnabled;
    private boolean rbacLoginTypeEnabled = true;
    private boolean windowsLoginChangePasswordEnabled;
    private boolean azureLoginChangePasswordEabled;
    private boolean azureEmailAsUsername;


    private DeploymentUtil deploymentUtil;
    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        this.deploymentUtil = deploymentUtil;
    }


    private SessionRegistry sessionRegistry;

    @Autowired
    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }


    private SessionService sessionDal;

    @Autowired
    public void SessionService(SessionService sessionDal) {
        this.sessionDal = sessionDal;
    }


    private LdapUserService ldapUserService;

    @Autowired
    public void setLdapUserService(LdapUserService ldapUserService) {
        this.ldapUserService = ldapUserService;
    }
    //RBAC-1562 Starts
    private static final int DEFAULT_otpAttempts = 3;
    private static final int DEFAULT_otpTimeout = 2;
    private static final int DEFAULT_otpSessionTimeout = 10;
    public static final String CONF_OTP_ATTEMPTS = "rbac.twoFactorAuthPolicy.OTPAttempts";
    public static final String CONF_OTP_TIMEOUT = "rbac.twoFactorAuthPolicy.OTPTimeout";
    public static final String CONF_OTP_SESSION_TIMEOUT = "rbac.twoFactorAuthPolicy.OTPSessionTimeout";
    private int otpAttempts = DEFAULT_otpAttempts;
    private int otpTimeout = DEFAULT_otpTimeout;
    private int otpSessionTimeout = DEFAULT_otpSessionTimeout;
    //RBAC-1562 Ends


    private EmailDal emailDal;

    @Autowired
    public void setEmailDal(EmailDal emailDal) {
        this.emailDal = emailDal;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AppInfoResponse getAppInfo(AppInfoRequest request) {
        ChildApplication childApplicationForAppInfo = getChildApplicationByServiceUrlOrAppKey(request.getService(), request.getAppKey());
        boolean isNonSSO = ChildApplication.isNonSSO(childApplicationForAppInfo.getAppType());
        String sessionUsername = null;
        UserSessionData userSessionData = sessionRegistry.getUserSessionData(request.getSessionHash(), request.getService(), request.getAppKey());
        if(isNonSSO){
            // do nothing, don't look for userName
        }
        else{
            if(userSessionData!=null){
                sessionUsername = userSessionData.getUserName();
            }
        }
        validateAppInfoRequest(request, sessionUsername);
        AppInfoResponse response = new AppInfoResponse();
        response.setService(request.getService());
        // @TODO: lookup application configuration and return renew flag
        response.setRenew(request.isRenew());
        String childApplicationName = null;
        if(request.getAppKey()!=null && !request.getAppKey().isEmpty()){
            ChildApplication childApp = Lookup.getChildApplicationByAppKeyNew(request.getAppKey());
            childApplicationName = childApp!=null? childApp.getChildApplicationName() : null;
        }
        else{
            ChildApplication childApp = Lookup.getChildApplicationByServiceUrlNew(request.getService());
            childApplicationName = childApp!=null? childApp.getChildApplicationName() : null;
        }
        response.setApplicationName(childApplicationName);
        // @TODO: lookup application configuration and return login form URL
        //removed to fetch this in Casservlet using property file
      /*  StringBuilder sb = new StringBuilder();
        sb.append(request.getSsoContextPath() != null ? request.getSsoContextPath() : "/sso");
        sb.append("/a/login.html");
        response.setLoginFormUrl(sb.toString());*/
        if(childApplicationName!=null && !childApplicationName.isEmpty()){
            ApplicationDownInfo appDownInfo = ApplicationMaintenanceCache.isApplicationDown(childApplicationName);
            if(appDownInfo!=null){
                log.info("getAppInfo; {} is under maintenance from {} till {}", appDownInfo.getChildApplicationName(), appDownInfo.getFromDate(), appDownInfo.getToDate());
                ErrorInfoException errorInfoException = new ErrorInfoException(LoginResponse.APP_UNDER_MAINTENANCE);
                errorInfoException.getParameters().put("applicationName", appDownInfo.getChildApplicationName());
                //UriComponentsBuilder uriBuilderForMaintenance = UriComponentsBuilder.fromUriString(deploymentUtil.getMaintenancePage());
                String param = /*EsqSymetricCipher.encryptPassword(appDownInfo.getChildApplicationName());*/appDownInfo.getChildApplicationName();
                //uriBuilderForMaintenance.queryParam("a", param);
                errorInfoException.getParameters().put("a", param);
                //uriBuilderForMaintenance.queryParam("homeUrl", Lookup.getHomeUrlByServiceUrlNew(request.getService()));
                errorInfoException.getParameters().put("homeUrl", Lookup.getHomeUrlByServiceUrlNew(request.getService()));
                //errorInfoException.getParameters().put("redirectUrl", uriBuilderForMaintenance.build().toString());
                throw errorInfoException;
            }
        }

        if(sessionUsername != null && !isUserAuthorizedForApp(sessionUsername, request.getService(), request.getAppKey())){
            log.info("getAppInfo; {} not Authorized for service={}", sessionUsername, request.getService());
            ErrorInfoException errorInfoException = new ErrorInfoException(LoginResponse.NOT_AUTHORIZED_APP);
            String applicationName = childApplicationName;
            errorInfoException.getParameters().put("applicationName", applicationName);
            errorInfoException.getParameters().put("homeUrl", Lookup.getHomeUrlByServiceUrlNew(request.getService()));
            loginLogDal.create(LoginLog.createLoginLog(sessionUsername,
                    LoginLog.LOGIN_LOG_TABLE_SWITCH_PREFIX
                            + LoginLog.LOGIN_LOG_LOGOUT_MAP.get(userSessionData.getLoginType()),
                    false, request.getClientIP(), request.getService(), LoginResponse.NOT_AUTHORIZED_APP,
                    request.getSessionHash(), request.getAppKey()));

            throw errorInfoException;
        }
        if (!request.isRenew() && sessionUsername != null) {
            String serviceTicket = generateServiceTicket(sessionUsername, request.getService());
            log.debug("getAppInfo; GeneratedNewServiceTicket {}",serviceTicket);
            response.setServiceTicket(serviceTicket);
            response.setRedirectUrl(appendTicket(request.getService(), serviceTicket));
            response.setUserName(sessionUsername);
            response.setSsoLogoutDataList(sessionRegistry.login(
                    sessionUsername,
                    request.getService(),
                    serviceTicket,
                    request.getSessionHash(),
                    request.getClientIP(),
                    childApplicationForAppInfo, request.getHeaderMap(), request.getDeviceType(), userSessionData!=null?userSessionData.getLoginType():null, null
            ));
            loginLogDal.create(LoginLog.createLoginLog(sessionUsername,
                    LoginLog.LOGIN_LOG_TABLE_SWITCH_PREFIX
                            + LoginLog.LOGIN_LOG_LOGOUT_MAP.get(userSessionData.getLoginType()),
                    true, request.getClientIP(), request.getService(), LoginResponse.APP_SWITCHED,
                    request.getSessionHash(), request.getAppKey()));
        }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse login(LoginRequest request) {
        log.info("login; loginRequest = {};", request);
        validateLoginRequest(request, LoginLog.LOG_TYPE_LOGIN_RBAC);
        return checkLogin(request, LoginType.LOGIN_RBAC, LoginLog.LOG_TYPE_LOGIN_RBAC);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private LoginResponse checkLogin(LoginRequest request, String loginType, String loginLogType){
        readConfiguration();
        if (request == null || request.getUserName() == null) {
            log.info("login; request==null || request.userName==null");
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), loginLogType, false, request.getClientIP(), request.getService(), LoginResponse.USERNAME_NULL, request.getSessionHash(), request.getAppKey() ));
            return new LoginResponse(LoginResponse.INVALID_CREDENTIALS, request.getService());
        }

        User user = userDal.getByUserName(request.getUserName());
        if (user == null) {
            log.info("login; user not found; userName={}", request.getUserName());
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), loginLogType, false, request.getClientIP(), request.getService(), LoginResponse.USERNAME_NOT_FOUND, request.getSessionHash(), request.getAppKey() ));
            return new LoginResponse(LoginResponse.INVALID_CREDENTIALS, request.getService());
        }

        // after this point we know userName references existing user
        // must update loginTime, failedLoginTime, etc.

        if (!user.checkPassword(request.getPassword())) {
            log.info("login; invalid password; userName={}", request.getUserName());
            // @TODO: audit log

            // added to support existing SHA1 passwords
            if (user.checkSHA1Password(request.getPassword())) {
                log.info("login; checkSHA1Password; success; userName={}", request.getUserName());
                userDal.overrideSHA1Password(user, request.getPassword());
                log.info("login; overrideSHA1Password; userName={}", request.getUserName());
                LoginResponse loginResponse = checkUserAccount(request, user, loginType, null);
                //RBAC-1562 Starts
                if(userDal.checkTwoFactorActiveForUserAndTenant(Lookup.getTenantIdByOrganizationId(user.getOrganizationId())) && checkTwoFactEnabledForLoginType(loginType))
                    loginResponse.setTwoFactorEnabled(true);
                //RBAC-1562 Ends
                if(loginType.equals(LoginType.LOGIN_NATIVE)){
                    loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), loginLogType, LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse.getResultCode()), request.getClientIP(), request.getService(), loginResponse.getResultCode() + (user.getAccountLockedReason()!=null?"-"+user.getAccountLockedReason():""),  NativeApplicationSession.generateSessionHash(loginResponse.getServiceTicket(), request.getAppKey()), request.getAppKey()));
                }
                else{
                    loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), loginLogType, LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse.getResultCode()), request.getClientIP(), request.getService(), loginResponse.getResultCode() + (user.getAccountLockedReason()!=null?"-"+user.getAccountLockedReason():""), request.getSessionHash(), request.getAppKey() ));
                }
                return loginResponse;
            } else {
                log.info("login; invalid SHA1 password; userName={}", request.getUserName());
            }
            LoginResponse returnResponse = failedLogin(user, request, loginLogType);
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), loginLogType, false, request.getClientIP(), request.getService(), LoginResponse.AUTHENTICATION_FAILED + (user.getAccountLockedReason()!=null?"-"+user.getAccountLockedReason():""), request.getSessionHash(), request.getAppKey() ));
            return returnResponse;
        }

        LoginResponse loginResponse = checkUserAccount(request, user, loginType, null);
        //RBAC-1562 Starts
        if(checkTwoFactEnabledForLoginType(loginType) && userDal.checkTwoFactorActiveForUserAndTenant(Lookup.getTenantIdByOrganizationId(user.getOrganizationId())))
            loginResponse.setTwoFactorEnabled(true);
        //RBAC-1562 Ends
        if(loginType.equals(LoginType.LOGIN_NATIVE)){
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), loginLogType, LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse.getResultCode()), request.getClientIP(), request.getService(), loginResponse.getResultCode() + (user.getAccountLockedReason()!=null?"-"+user.getAccountLockedReason():""), NativeApplicationSession.generateSessionHash(loginResponse.getServiceTicket(), request.getAppKey() ), request.getAppKey()));
        }
        else{
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), loginLogType, LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse.getResultCode()), request.getClientIP(), request.getService(), loginResponse.getResultCode() + (user.getAccountLockedReason()!=null?"-"+user.getAccountLockedReason():""), request.getSessionHash(), request.getAppKey() ));
        }if(LoginResponse.PASSWORD_EXPIRED_CHANGE_PASSWORD.equalsIgnoreCase(loginResponse.getResultCode())){
            Map<String, String> additionalAttributes = new HashMap<String, String>();
            additionalAttributes.put("lastPasswordSetTime", Long.toString(user.getPasswordSetTime().getTime()));
            additionalAttributes.put("noPasswordChangeDays", Integer.toString(noPasswordChangeDays));
            if(loginResponse.getUserSessionData()==null){
                loginResponse.setUserSessionData(new UserSessionData(user.getUserName(), loginType, new Date()));
            }
            loginResponse.getUserSessionData().setAdditionalAttributes(additionalAttributes);
        }
        return loginResponse;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse loginWindows(LoginRequest request) {

        validateLoginRequest(request, LoginLog.LOG_TYPE_LOGIN_LDAP);
        return checkLoginLDAP(request, LoginType.LOGIN_WINDOWS_AD, LoginLog.LOG_TYPE_LOGIN_LDAP);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    private LoginResponse checkLoginLDAP(LoginRequest request, String loginType, String loginLogType){

        validateLoginRequest(request, LoginLog.LOG_TYPE_LOGIN_LDAP);

        if (request == null || request.getUserName() == null) {
            log.info("loginWindows; request==null || request.userName==null");
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_LOGIN_LDAP, false, request.getClientIP(), request.getService(), LoginResponse.USERNAME_NULL, request.getSessionHash(), request.getAppKey() ));
            return new LoginResponse(LoginResponse.INVALID_CREDENTIALS, request.getService());
        }

        String ldapUsername = request.getUserName().toLowerCase();
        log.trace("loginLDAP; ldapUsername={}; domainName={}", ldapUsername, domainName);

        User user = userDal.getByIdentity(UserIdentity.LDAP_ACCOUNT, ldapUsername);
        if (user == null) {
            log.info("loginLDAP; user not found; ldapUsername={}", ldapUsername);
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_LOGIN_LDAP, false, request.getClientIP(), request.getService(), LoginResponse.USER_IDENTITY_MAPPING_NOT_FOUND, request.getSessionHash(), request.getAppKey() ));
            return new LoginResponse(LoginResponse.INVALID_CREDENTIALS, request.getService());
        }

        if(ldapUserService.checkUser(ldapUsername, request.getPassword())) {
            log.info("login; checkLdapUser; sucess; userName={}", ldapUsername);
            LoginResponse loginResponse = checkUserAccount(request, user, LoginType.LOGIN_WINDOWS_AD, ldapUsername);
            loginLogDal.create(LoginLog.createLoginLog(ldapUsername, loginLogType, LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse.getResultCode()), request.getClientIP(), request.getService(), loginResponse.getResultCode() + (user.getAccountLockedReason()!=null?"-"+user.getAccountLockedReason():""), request.getSessionHash(), request.getAppKey() ));
            if(loginResponse.getUserSessionData()==null){
                loginResponse.setUserSessionData(new UserSessionData(user.getUserName(), loginType, new Date()));
            }

            return loginResponse;
        }else {
            log.warn("loginLDAP; ldapUsername={};", ldapUsername);
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_LOGIN_LDAP, false, request.getClientIP(), request.getService(), LoginResponse.AUTHENTICATION_FAILED, request.getSessionHash(), request.getAppKey() ));
            return new LoginResponse(LoginResponse.LOGIN_FAILED, request.getService());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private LoginResponse checkLoginAzure(LoginRequest request, String loginType, String loginLogType) {

        validateLoginRequest(request, LoginLog.LOG_TYPE_LOGIN_AZURE_AD);

        if (request == null || request.getUserName() == null) {
            log.info("loginAzureAD; request==null || request.userName==null");
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_LOGIN_AZURE_AD, false,
                    request.getClientIP(), request.getService(), LoginResponse.USERNAME_NULL, request.getSessionHash(),
                    request.getAppKey()));
            return new LoginResponse(LoginResponse.INVALID_CREDENTIALS, request.getService());
        }

        String userName = request.getUserName().toLowerCase();
        log.trace("loginAzureAD; ldapUsername={}; domainName={}", userName, domainName);
        String userEmailAddress = "";
        User user = userDal.getByUserName(request.getUserName());
        if (user == null) {
            user = userDal.getByEmailAddress(request.getUserName());
            if (user == null) {
                log.info("loginAzureAD; user not found; Username={}", userName);
                loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_LOGIN_AZURE_AD,
                        false, request.getClientIP(), request.getService(),
                        LoginResponse.USERNAME_NOT_FOUND, request.getSessionHash(), request.getAppKey()));
                return new LoginResponse(LoginResponse.INVALID_CREDENTIALS, request.getService());
            }

        }

        if (!Pattern.matches(DeploymentUtil.EMAIL_PATTERN, user.getUserName())) {
            if (user.getEmailAddress() == null || user.getEmailAddress().trim().isEmpty() ||
                    !Pattern.matches(DeploymentUtil.EMAIL_PATTERN, user.getEmailAddress())) {
                loginLogDal.create(LoginLog.createLoginLog(userEmailAddress, LoginLog.LOG_TYPE_LOGIN_AZURE_AD, false,
                        request.getClientIP(), request.getService(), LoginResponse.USERNAME_NOT_FOUND,
                        request.getSessionHash(), request.getAppKey()));
                return new LoginResponse(LoginResponse.USERNAME_NOT_FOUND, null);
            }
            else
                userEmailAddress = user.getEmailAddress();
        }
        else
            userEmailAddress = user.getUserName();


        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUserName(userEmailAddress);
        loginResponse.setResultCode(LoginResponse.USERNAME_FOUND);

        log.info("login; checkUser; sucess; userName={}", userName);
        return loginResponse;

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse loginAuto(LoginRequest request) {

        log.trace("loginAuto; {}", request);
        validateLoginRequest(request, LoginLog.LOG_TYPE_LOGIN_INTEGRATED_WINDOWS);
        String windowsUsername = request.getUserName().toLowerCase();

        User user = userDal.getByIdentity(UserIdentity.WINDOWS_ACCOUNT, windowsUsername);
        if (user == null) {
            log.info("loginAuto; user not found; windowsUsername={}", windowsUsername);
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_LOGIN_INTEGRATED_WINDOWS, false, request.getClientIP(), request.getService(), LoginResponse.USER_IDENTITY_MAPPING_NOT_FOUND, request.getSessionHash(), request.getAppKey() ));
            return new LoginResponse(LoginResponse.INVALID_CREDENTIALS, request.getService());
        }

        LoginResponse loginResponse = checkUserAccount(request, user, LoginType.LOGIN_INTEGRATED_WINDOWS, windowsUsername);
        loginResponse.setUserName(user.getUserName());
        LoginLog loginLog = LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_LOGIN_INTEGRATED_WINDOWS,
                ( LoginResponse.LOGIN_SUCCESSFULL.equals( loginResponse.getResultCode() ) ), request.getClientIP(), request.getService(), loginResponse.getResultCode(), request.getSessionHash(), request.getAppKey());
        loginLog.setUserId(user.getUserId());
        loginLogDal.create(loginLog);
        return loginResponse;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse loginForgeRock(LoginRequest request) {

        log.trace("loginForgeRock; {}", request);
        validateLoginRequest(request, LoginLog.LOG_TYPE_LOGIN_FORGEROCK);
        String forgeRockUsername = request.getUserName().toLowerCase();

        User user = userDal.getByIdentity(UserIdentity.FORGEROCK_ACCOUNT,
                forgeRockUsername);
        if (user == null) {
            log.info("loginForgeRock; user not found; forgeRockUsername={}",
                    forgeRockUsername);
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(),
                    LoginLog.LOG_TYPE_LOGIN_FORGEROCK, false,
                    request.getClientIP(), request.getService(),
                    LoginResponse.USER_IDENTITY_MAPPING_NOT_FOUND,
                    request.getSessionHash(), request.getAppKey()));
            return new LoginResponse(LoginResponse.INVALID_CREDENTIALS,
                    request.getService());
        }

        LoginResponse loginResponse = checkUserAccount(request, user,
                LoginType.LOGIN_FORGEROCK, forgeRockUsername);
        LoginLog loginLog = LoginLog.createLoginLog(request.getUserName(),
                LoginLog.LOG_TYPE_LOGIN_FORGEROCK,
                (LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse
                        .getResultCode())), request.getClientIP(), request
                        .getService(), loginResponse.getResultCode(), request
                        .getSessionHash(), request.getAppKey());
        loginLog.setUserId(user.getUserId());
        loginLogDal.create(loginLog);
        return loginResponse;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse genericHeaderLoginService(LoginRequest request) {

//        String	userIdentityType=request.getLoginType().replace("login", "");
//		userIdentityType=userIdentityType.substring(0, 1).toLowerCase()+userIdentityType.substring(1)+"User";

        User user = userDal.getByIdentity(UserIdentity.GENERIC_USER_ACCOUNT,request.getUserName());
        //userDal.getByIdentity(request.getLoginType().replace("login", "")+"GenericUser",request.getUserName());
        if (user == null) {
            log.info("loginType; user not found; userName={}",request.getLoginType(),request.getUserName());
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(),
                    request.getLoginLogType(), false,
                    request.getClientIP(), request.getService(),
                    LoginResponse.USER_IDENTITY_MAPPING_NOT_FOUND,
                    request.getSessionHash(), request.getAppKey()));
            return new LoginResponse(LoginResponse.INVALID_CREDENTIALS,
                    request.getService());
        }

        LoginResponse loginResponse = checkUserAccount(request, user,request.getLoginType(), request.getUserName());
        LoginLog loginLog = LoginLog.createLoginLog(request.getUserName(),
                request.getLoginLogType(),
                (LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse
                        .getResultCode())), request.getClientIP(), request
                        .getService(), loginResponse.getResultCode(), request
                        .getSessionHash(), request.getAppKey());
        loginLog.setUserId(user.getUserId());
        loginLogDal.create(loginLog);
        return loginResponse;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse loginSiteMinder(LoginRequest request) {

        log.trace("loginSiteMinder; {}", request);
        validateLoginRequest(request, LoginLog.LOG_TYPE_LOGIN_SITEMINDER);
        String siteMinderUsername = request.getUserName().toLowerCase();

        User user = userDal.getByIdentity(UserIdentity.SITEMINDER_ACCOUNT,
                siteMinderUsername);
        if (user == null) {
            log.info("loginSiteMinder; user not found; siteMinderUsername={}",
                    siteMinderUsername);
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(),
                    LoginLog.LOG_TYPE_LOGIN_SITEMINDER, false,
                    request.getClientIP(), request.getService(),
                    LoginResponse.USER_IDENTITY_MAPPING_NOT_FOUND,
                    request.getSessionHash(), request.getAppKey()));
            return new LoginResponse(LoginResponse.INVALID_CREDENTIALS,
                    request.getService());
        }

        LoginResponse loginResponse = checkUserAccount(request, user,
                LoginType.LOGIN_SITEMINDER, siteMinderUsername);
        LoginLog loginLog = LoginLog.createLoginLog(request.getUserName(),
                LoginLog.LOG_TYPE_LOGIN_SITEMINDER,
                (LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse
                        .getResultCode())), request.getClientIP(), request
                        .getService(), loginResponse.getResultCode(), request
                        .getSessionHash(), request.getAppKey());
        loginLog.setUserId(user.getUserId());
        loginLogDal.create(loginLog);
        return loginResponse;
    }

    @Override
    public ServiceValidateResponse serviceValidate(ServiceValidateRequest request) {

        if (request == null) {
            ServiceValidateResponse response = new ServiceValidateResponse();
            response.setFailureCode("INVALID_REQUEST");
            response.setFailureMessage("");
            return response;
        }

        String service = request.getService();
        String serviceTicket = request.getServiceTicket();

        if (serviceTicket == null || request.getService() == null || serviceTicket.isEmpty() || service.isEmpty()) {
            ServiceValidateResponse response = new ServiceValidateResponse();
            response.setFailureCode("INVALID_REQUEST");
            response.setFailureMessage("Ticket or service not specified");
            return response;
        }

        String key = getServiceTicketKey(serviceTicket, service);

        String username = ticketUsername.get(key);

        if (username == null || username.isEmpty()) {
            ServiceValidateResponse response = new ServiceValidateResponse();
            response.setFailureCode("INVALID_TICKET");
            response.setFailureMessage("Ticket " + serviceTicket + " not recognized");
            return response;
        }

        //added for application specific user login denial if no roles are found for that particular app
        /*UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(service);
        UriComponents uriComponents =uriComponentsBuilder.build();
        if(uriComponents.getQueryParams()!=null && !(uriComponents.getQueryParams().isEmpty()) && uriComponents.getQueryParams().containsKey("applicationName")){
        	List<String> appNameParams = uriComponents.getQueryParams().get("applicationName");
		    if(appNameParams!=null && !appNameParams.isEmpty() && !userDal.isUserAuthorizedForApp(username, appNameParams.get(0))){
		        ServiceValidateResponse response = new ServiceValidateResponse();
		        response.setFailureCode("NOT_AUTHORIZED");
		        response.setFailureMessage("Not authorized for this app");
	            loginLogDal.create(LoginLog.createLoginLog(username, "serviceValidate", false, request.getIpAddress(), request.getService(), LoginResponse.NOT_AUTHORIZED_APP ));
		        return response;
		    }
        }*/

        //invalidate ticket after first validation to avoid re-verification using the same ticket
        ticketUsername.remove(key);

        ServiceValidateResponse response = new ServiceValidateResponse();
        response.setUsername(username);
        return response;
    }

    private boolean isUserAuthorizedForApp(String username, String serviceUrl, String appKey){
        log.debug("inside isUserAuthorizedForApp check");
        ChildApplication childApplication = Lookup.getChildApplicationByServiceUrlNew(serviceUrl);
        String applicationName = childApplication!=null?childApplication.getApplication().getName():null;
        if(userDal.isRevokedApplicationsForUserName(username,childApplication))
            return false;
        //should return false but kept true to avoid login denial if service url is not configured
        if(applicationName==null || applicationName.isEmpty()){
            return true;
        }
        if(appKey==null || appKey.isEmpty()){
            ChildApplication childApp = getChildApplicationByServiceUrlOrAppKey(serviceUrl, null);
            if(childApp!=null && childApp.getAppKey()!=null && !childApp.getAppKey().isEmpty()){
                appKey = childApp.getAppKey();
            }
        }

        return userDal.isUserAuthorizedForApp(username, applicationName, appKey);
    }

    public boolean isServiceUrlValid(String serviceUrl){
        if(deploymentUtil.getValidateServiceUrl()!=null && deploymentUtil.getValidateServiceUrl().equals(Boolean.TRUE)){
            ChildApplication childApplication = Lookup.getChildApplicationByServiceUrlNew(serviceUrl,
                    deploymentUtil.isIgnoreAppKeyInRedirectValidation());
            if(childApplication==null){
                log.error("isServiceUrlValid; No App Found; "+LoginResponse.UNKNOWN_SERVICE_URL+"; serviceUrl={};", serviceUrl);
                return false;
            }
        }
        return true;
    }

    private void validateLoginRequest(LoginRequest loginRequest, String logType){
        if(deploymentUtil.isValidateNullServiceUrl()){
            if(LoginLog.LOG_TYPE_LOGIN_RBAC.equalsIgnoreCase(logType)
                    || LoginLog.LOG_TYPE_LOGIN_INTEGRATED_WINDOWS.equalsIgnoreCase(logType)
                    || LoginLog.LOG_TYPE_LOGIN_SITEMINDER.equalsIgnoreCase(logType)
                    || LoginLog.LOG_TYPE_LOGIN_AZURE_AD.equalsIgnoreCase(logType)
                    || LoginLog.LOG_TYPE_LOGIN_LDAP.equalsIgnoreCase(logType)
                    || LoginLog.LOG_TYPE_LOGIN_FORGEROCK.equalsIgnoreCase(logType)){
                if(loginRequest.getService()==null || loginRequest.getService().isEmpty()){
                    log.error("validateLoginRequest; "+LoginResponse.NO_SERVICE_URL+"; clientIp={}; userName={};",
                            loginRequest.getClientIP(), loginRequest.getUserName());
                    loginLogDal.create(LoginLog.createLoginLog(loginRequest.getUserName(), logType, false,
                            loginRequest.getClientIP(), loginRequest.getService(), LoginResponse.NO_SERVICE_URL,
                            loginRequest.getSessionHash(), loginRequest.getAppKey()));
                    throw new ErrorInfoException(LoginResponse.NO_SERVICE_URL);
                }
            }
        }
        if(!isServiceUrlValid(loginRequest.getService()))
        {
            log.error("validateLoginRequest; "+LoginResponse.UNKNOWN_SERVICE_URL+"; serviceUrl={}; clientIp={}; userName={};", loginRequest.getService(), loginRequest.getClientIP(), loginRequest.getUserName());
            loginLogDal.create(LoginLog.createLoginLog(loginRequest.getUserName(), logType, false, loginRequest.getClientIP(), loginRequest.getService(), LoginResponse.UNKNOWN_SERVICE_URL, loginRequest.getSessionHash(), loginRequest.getAppKey()));
            throw new ErrorInfoException(LoginResponse.UNKNOWN_SERVICE_URL);
        }
    }

    @Override
    public Boolean validateLogoutRequest(LogoutRequest request){
        if(!isServiceUrlValid(request.getService()))
        {
            log.error("validateLogoutRequest; "+LoginResponse.UNKNOWN_SERVICE_URL+"; serviceUrl={}; clientIp={}; userName={};", request.getService(), request.getClientIP(), request.getUserName());
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_LOGOUT, false, request.getClientIP(), request.getService(), LoginResponse.UNKNOWN_SERVICE_URL, request.getSessionHash(), request.getAppKey()));
            throw new ErrorInfoException(LoginResponse.UNKNOWN_SERVICE_URL);
        }
        return true;
    }

    private void validateAppInfoRequest(AppInfoRequest appInfoRequest, String sessionUserName){
        if(!isServiceUrlValid(appInfoRequest.getService()))
        {
            log.error("validateAppInfoRequest; "+LoginResponse.UNKNOWN_SERVICE_URL+"; serviceUrl={}; clientIp={}; userName={};", appInfoRequest.getService(), appInfoRequest.getClientIP(), sessionUserName);
            loginLogDal.create(LoginLog.createLoginLog(sessionUserName, "appInfoRequest", false, appInfoRequest.getClientIP(), appInfoRequest.getService(), LoginResponse.UNKNOWN_SERVICE_URL, null, appInfoRequest.getAppKey()));
            throw new ErrorInfoException(LoginResponse.UNKNOWN_SERVICE_URL);
        }
    }

    private boolean isTimeRestricted(Restriction restriction) {
        if (restriction == null) {
            return false;
        }
        DateTime now = DateTime.now();
        return RestrictionUtil.isTimeRestricted(now.toDate(), restriction);
    }

    private boolean isIpRestricted(String clientIp, Restriction restriction) throws Exception {
        if (restriction == null || Strings.isNullOrEmpty(clientIp)) {
            return false;
        }
        InetAddress address = RestrictionUtil.parseAddress(clientIp);

        // if deny list is defined, client ip is restricted if it matches
        // any range in the deny list
        List<String> denyList = restriction.getDisallowedIPs();
        if (denyList != null) {
            for (String rangeText : denyList) {
                RestrictionIpRange range = RestrictionUtil.parseRange(rangeText);
                if (RestrictionUtil.inRange(address, range)) {
                    return true;
                }
            }
        }

        // if allow list is defined, client ip is restricted if it doesn't
        // match any range in the allow list
        List<String> allowList = restriction.getAllowedIPs();
        if (allowList != null && allowList.size() > 0) {
            for (String rangeText : allowList) {
                RestrictionIpRange range = RestrictionUtil.parseRange(rangeText);
                if (RestrictionUtil.inRange(address, range)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private Restriction combineUserGroupRestrictions(User user) {

        Restriction result = new Restriction();

        if (user.getGroupId() != null) {
            Group group = groupDal.getById(user.getGroupId());
            if (group != null && group.getRestrictions() != null) {

                overrideRestrictions(group.getRestrictions(), result);
            }
        }

        if (user.getRestrictions() != null) {
            overrideRestrictions(user.getRestrictions(), result);
        }

        return result;
    }

    private void overrideRestrictions(Restriction in, Restriction out) {
        if (!Strings.isNullOrEmpty(in.getTimeZone())
                || !Strings.isNullOrEmpty(in.getFromDate())
                || !Strings.isNullOrEmpty(in.getToDate())
                || !Strings.isNullOrEmpty(in.getDayOfWeek())
                || !Strings.isNullOrEmpty(in.getHours())) {
            out.setTimeZone(in.getTimeZone());
            out.setFromDate(in.getFromDate());
            out.setToDate(in.getToDate());
            out.setDayOfWeek(in.getDayOfWeek());
            out.setHours(in.getHours());
        }
        if ((in.getAllowedIPs() != null && in.getAllowedIPs().size() > 0)
                || (in.getDisallowedIPs() != null && in.getDisallowedIPs().size() > 0)) {
            out.setAllowedIPs(in.getAllowedIPs());
            out.setDisallowedIPs(in.getDisallowedIPs());
        }
    }

    private LoginResponse checkUserAccount(LoginRequest request, User user, String loginType, String identityId) {
        if (user.getIsEnabled() == null || !user.getIsEnabled()) {
            log.info("checkUserAccount; user is disabled; userName={}", request.getUserName());
            // @TODO: audit log
            return failedLogin(user, LoginResponse.ACCOUNT_DISSABLED, request, loginType);
        }
        if(user.getIsLocked()){
            log.info("checkUserAccount; user is locked; userName={}", request.getUserName());
            return failedLogin(user, LoginResponse.ACCOUNT_LOCKED, request, loginType);
        }

        Restriction restriction = combineUserGroupRestrictions(user);

        if (isTimeRestricted(restriction)) {
            log.info("checkUserAccount; login time restricted; RBAC userName={};",
                    user.getUserName());
            if(deploymentUtil.getTimeRestrictionIncCounter().equals(Boolean.TRUE)){
                return failedLogin(user, LoginResponse.TIME_RESTRICTION, request, loginType);
            }
            else{
                return failedLoginNoCounter(user, LoginResponse.TIME_RESTRICTION, request, loginType);
            }
        }
        boolean ipRestrictionResult = false;
        try{
            ipRestrictionResult = isIpRestricted(request.getClientIP(), restriction);
        }
        catch(UnknownHostException uhe){
            log.warn("checkUserAccount; ignoring IP restrictions; userName={}; clientIP={}; sessionHash={}; error={};",
                    request.getUserName(), request.getClientIP(), request.getSessionHash(), uhe.getMessage());
        }
        catch(Exception e){
            log.error("checkUserAccount; ignoring IP restrictions; userName={}; clientIP={}; sessionHash={}; Exception={}",
                    request.getUserName(), request.getClientIP(), request.getSessionHash(), e);
        }
        if (ipRestrictionResult) {
            log.info("checkUserAccount; client IP restricted; RBAC userName={};",
                    user.getUserName());
            if(deploymentUtil.getIpRestrictionIncCounter().equals(Boolean.TRUE)){
                return failedLogin(user, LoginResponse.IP_RESTRICTION, request, loginType);
            }
            else{
                return failedLoginNoCounter(user, LoginResponse.IP_RESTRICTION, request, loginType);
            }
        }

        if (user.getPasswordSetTime() != null) {
            DateTime passwordSetTime = new DateTime(user.getPasswordSetTime());
            DateTime now = DateTime.now();
            int days = Days.daysBetween(passwordSetTime, now).getDays();
            log.trace("checkUserAccount; passSetTime={}; now={}; days={}; passSetDays={}; action={}",
                    passwordSetTime, now, days, noPasswordChangeDays, noPasswordChangeAction);
            if (noPasswordChangeDays!=DEFAULT_DONT_EXPIRE_PASSWORD_VALUE && days > noPasswordChangeDays) {
                if (LoginResponse.ACCOUNT_LOCKED.equals(noPasswordChangeAction)) {
                    log.info("checkUserAccount; noPasswordChangeAction; lockingUser; RBAC userName={};",
                            user.getUserName());
                    if(user.getIsLocked()!=null && user.getIsLocked().equals(Boolean.FALSE)){
                        user.setAccountLockedReason("lockingUser-passwordExpired");
                    }
                    user.setIsLocked(true);
                } else {
                    log.info("checkUserAccount;noPasswordChangeAction; changePassword; RBAC userName={};",
                            user.getUserName());
                    user.setChangePasswordFlag(true);
                    user.setIsPasswordExpired(true);
                }
            }
        }
        // added getUpdatedOn to handle for no login activity account locked re-occurrence
        Date lastLoginDate = user.getLoginTime() == null ? (user.getUpdatedOn()==null?user.getCreatedOn():user.getUpdatedOn()):user.getLoginTime();
        long lastLoginDays = dateDifference(lastLoginDate);
        if(noLoginDays!=DEFAULT_NO_ACTIVITY_DONT_LOCK_VALUE && noLoginDays < lastLoginDays){
            if(user.getIsLocked()!=null && user.getIsLocked().equals(Boolean.FALSE)){
                user.setAccountLockedReason("lockingUser-noActivity");
            }
            user.setIsLocked(true);
            log.info("checkUserAccount; loginInactivity; lockingUser; RBAC userName={};",
                    user.getUserName());
            return failedLogin(user, LoginResponse.ACCOUNT_LOCKED_INACTIVITY, request, loginType);
        }

        if (user.getIsLocked() != null && user.getIsLocked()) {
            log.info("checkUserAccount; user is locked; userName={}", request.getUserName());
            // @TODO: audit log
            return failedLogin(user, LoginResponse.ACCOUNT_LOCKED, request, loginType);
        }
        //RBAC-1562 Starts
        if (user.getConsecutiveLoginFailures() >= otpAttempts) {
            consecutiveLoginFailures = otpAttempts;
            return failedLogin(user, LoginResponse.ACCOUNT_LOCKED, request, loginType);
        }
        //RBAC-1562 Ends

        if (user.getChangePasswordFlag() != null && user.getChangePasswordFlag() && loginType.equals(LoginType.LOGIN_RBAC)) {
            if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
                userDal.changePassword(request.getUserName(), request.getPassword(), request.getNewPassword());
                log.info("checkUserAccount; passwordChanged; userName={}", request.getUserName());
                // @TODO: audit log

            } else {
                // @TODO: audit log
                LoginResponse loginResponseChangePass = null;
                if(user.getIsPasswordExpired()!=null && user.getIsPasswordExpired().equals(Boolean.TRUE)){
                    loginResponseChangePass = new LoginResponse(LoginResponse.PASSWORD_EXPIRED_CHANGE_PASSWORD, request.getService());
                    log.info("checkUserAccount; change password flag set; changePasswordRequiredForExpiry; userName={}", request.getUserName());
                }
                else{
                    loginResponseChangePass = new LoginResponse(LoginResponse.CHANGE_PASSWORD_REQUIRED, request.getService());
                    log.info("checkUserAccount; change password flag set; changePasswordRequired; userName={}", request.getUserName());
                }
                loginResponseChangePass.setUserName(user.getUserName());
                return loginResponseChangePass;
            }
        }
        if(!isUserAuthorizedForApp(user.getUserName(), request.getService(), request.getAppKey())){
            log.info("checkUserAccount; {} not Authorized for service={}", request.getUserName(), request.getService());
            return new LoginResponse(LoginResponse.NOT_AUTHORIZED_APP, request.getService());
        }
        //RBAC-1562 Starts
        if(checkTwoFactEnabledForLoginType(loginType) && userDal.checkTwoFactorActiveForUserAndTenant(Lookup.getTenantIdByOrganizationId(user.getOrganizationId()))) {
            LoginResponse loginResponse = new LoginResponse(LoginResponse.LOGIN_SUCCESSFULL, request.getService());
            loginResponse.setTwoFactorEnabled(true);
            return loginResponse;
        }
        else //RBAC-1562 Ends
            return successfullLogin(request, user, loginType, identityId);
    }

    private LoginResponse failedLogin(User user, LoginRequest request, String loginType) {
        sendFailedLoginAlert(user,request);
        if(!user.getIsLocked()){
            return failedLogin(user, LoginResponse.INVALID_CREDENTIALS, request, loginType);
        }else {
            return failedLogin(user, LoginResponse.ACCOUNT_LOCKED, request, loginType);
        }

    }

    private LoginResponse failedLoginNoCounter(User user, String failureCode, LoginRequest request, String loginType) {
        user.setFailedLoginTime(new Date());
        LoginResponse response = new LoginResponse(failureCode, request.getService());
//         UserSessionData sessionData = new UserSessionData(user.getUserName(), loginType, new Date());
//         if(loginType.equals(LoginType.LOGIN_WINDOWS_AD) || loginType.equals(LoginType.LOGIN_INTEGRATED_WINDOWS) || loginType.equals(LoginType.LOGIN_SITEMINDER)){
        //sessionData = sessionData.identityId(identityId);
//         }
//         response.setUserSessionData(sessionData); commented for RBAC-1858
        return response;
    }
    private LoginResponse failedLogin(User user, String failureCode, LoginRequest request, String loginType) {
        user.setFailedLoginTime(new Date());
        int failureCount = user.getConsecutiveLoginFailures() != null
                ? user.getConsecutiveLoginFailures() + 1
                : 1;

        user.setConsecutiveLoginFailures(failureCount);
        if (failureCount >= consecutiveLoginFailures) {
            log.debug("failedLogin; userName={}, failureCode={}; failureCount={}; consecutiveFailures={}",
                    user.getUserName(), failureCode, failureCount, consecutiveLoginFailures);
            log.info("login; consecutiveLoginFailures; lockingUser; userName={}", user.getUserName());
            if(user.getIsLocked()!=null && user.getIsLocked().equals(Boolean.FALSE)){
                user.setAccountLockedReason("lockingUser-invalidLoginAttempts");
            }
            user.setIsLocked(true);
        }
        /* ChildApplication childApplication =  getChildApplicationByServiceUrlOrAppKey(request.getService(), request.getAppKey());
       		if(ChildApplication.isSSO(childApplication.getAppType()) || ChildApplication.isNonSSO(childApplication.getAppType())){
        	sessionRegistry.removeSessionData(request.getSessionHash(), request.getService(), childApplication.getAppKey());
        }*/
        LoginResponse response = new LoginResponse(failureCode, request.getService());
//        UserSessionData sessionData = new UserSessionData(user.getUserName(), loginType, new Date());
//        if(loginType.equals(LoginType.LOGIN_WINDOWS_AD) || loginType.equals(LoginType.LOGIN_INTEGRATED_WINDOWS) || loginType.equals(LoginType.LOGIN_SITEMINDER)){
        //sessionData = sessionData.identityId(identityId);
//        }
//        response.setUserSessionData(sessionData); commented for RBAC-1858
        return response;
    }

    private LoginResponse successfullLogin(LoginRequest request, User user, String loginType, String identityId) {
        // updated to handle for setting login time null to handle account locked and login inactivity scenario
        user.setLastSuccessfulLoginTime(user.getLoginTime()!=null?user.getLoginTime():user.getLastSuccessfulLoginTime());
        user.setLoginTime(new Date());
        user.setConsecutiveLoginFailures(0);
        String service = request.getService();
        String serviceTicket = null;
        ChildApplication childApplication = getChildApplicationByServiceUrlOrAppKey(request.getService(), request.getAppKey());
        if(ChildApplication.isNative(childApplication.getAppType())){
            serviceTicket = generateNativeServiceTicket(user.getUserName(), request.getAppKey());
        }
        else{
            serviceTicket = generateServiceTicket(user.getUserName(), service);
        }
        LoginResponse loginResponse = new LoginResponse(LoginResponse.LOGIN_SUCCESSFULL, request.getService());
        loginResponse.setServiceTicket(serviceTicket);
        //loginResponse.setService(service);
        loginResponse.setRedirectUrl(appendTicket(service, serviceTicket));
        loginResponse.setUserName(user.getUserName());
        UserSessionData sessionData = new UserSessionData(user.getUserName(), loginType, new Date());
        if(loginType.equals(LoginType.LOGIN_WINDOWS_AD) || loginType.equals(LoginType.LOGIN_INTEGRATED_WINDOWS) ||
                loginType.equals(LoginType.LOGIN_SITEMINDER) || loginType.equals(LoginType.LOGIN_GENERIC_HEADER_AUTH) ||
                loginType.equals(LoginType.LOGIN_FORGEROCK)){
            sessionData = sessionData.identityId(identityId);
        }
        loginResponse.setUserSessionData(sessionData);
        if(ChildApplication.isSSO(childApplication.getAppType()) || ChildApplication.isNonSSO(childApplication.getAppType())){
            /*sessionRegistry.addUserSessionData(request.getSessionHash(), request.getService(), childApplication.getAppKey(), sessionData);*/
            loginResponse.setSsoLogoutDataList(sessionRegistry.login(
                    loginResponse.getUserName(),
                    loginResponse.getService(),
                    loginResponse.getServiceTicket(),
                    request.getSessionHash(), request.getClientIP(),
                    childApplication, request.getHeaderMap(), request.getDeviceType(), loginType, null
            ));
        }
        else{
            NativeApplicationSession nativeSession = new NativeApplicationSession(loginResponse.getServiceTicket(), childApplication.getAppKey(), childApplication.getChildApplicationName());
            nativeSession.setServiceUrl(request.getService());
            sessionRegistry.loginNative(loginResponse.getUserName(), request.getClientIP(), nativeSession, childApplication, request.getHeaderMap(), request.getDeviceType(), null, loginType);
        }
        return loginResponse;
    }

    private void readConfiguration() {
        if (configuration == null) {
            return;
        }

        consecutiveLoginFailures = configuration.getInt(
                CONF_CONSECUTIVE_LOGIN_FAILURES,
                DEFAULT_consecutiveLoginFailures);

        noPasswordChangeDays = configuration.getInt(
                CONF_NO_PASSWORD_CHANGE_DAYS,
                DEFAULT_noPasswordChangeDays);

        noPasswordChangeAction = configuration.getString(
                CONF_NO_PASSWORD_CHANGE_ACTION,
                LoginResponse.CHANGE_PASSWORD_REQUIRED);

        noLoginDays = configuration.getInt(
                CONF_NO_LOGIN_DAYS,
                DEFAULT_noLoginDays);

        domainServerUrl = configuration.getString(
                CONF_DOMAIN_SERVER_URL,
                "");

        domainName = configuration.getString(
                CONF_DOMAIN_NAME,
                "");

        windowsLoginTypeEnabled = configuration.getBoolean(
                CONF_WINDOWS_LOGIN_ENABLED,
                false);

        integratedWindowsLoginTypeEnabled = configuration.getBoolean(
                CONF_WINDOWS_AUTO_LOGIN_ENABLED,
                false);

        siteMinderLoginTypeEnabled = configuration.getBoolean(
                CONF_SITEMINDER_AUTO_LOGIN_ENABLED,
                false);

        forgeRockLoginTypeEnabled = configuration.getBoolean(
                CONF_FORGEROCK_AUTO_LOGIN_ENABLED,
                false);

        windowsLoginChangePasswordEnabled = configuration.getBoolean(
                CONF_WINDOWS_LOGIN_CHANGE_PASS_ENABLED,
                false);
        azureLoginChangePasswordEabled = configuration.getBoolean(CONF_AZURE_CHANGE_PASSWORD,false);
        rbacLoginTypeEnabled = configuration.getBoolean(
                CONF_RBAC_LOGIN_ENABLED,
                true);
        noOfTimesPwdChange = configuration.getInt(CONF_NO_PASSWORD_CHANGE_TIMES, DEFAULT_noOfTimesPwdChange);
        ageOfPasswordInHour = configuration.getInt(CONF_AGE_OF_PASSWORD_IN_HOUR, DEFAULT_ageOfPasswordInHour);

        //RBAC-1562 Starts
        otpAttempts = configuration.getInt(
                CONF_OTP_ATTEMPTS,
                DEFAULT_otpAttempts);

        otpTimeout = configuration.getInt(
                CONF_OTP_TIMEOUT,
                DEFAULT_otpTimeout);

        otpSessionTimeout = configuration.getInt(
                CONF_OTP_SESSION_TIMEOUT,
                DEFAULT_otpSessionTimeout);
        //RBAC-1562 Ends


        azureActiveDirectoryLoginTypeEnabled = configuration.getBoolean(CONF_AZURE_ACTIVE_DIRECTORY_LOGIN_ENABLED, false);
        genericHeaderLoginTypeEnabled = configuration.getBoolean(
                CONF_GENERIC_HEADER_AUTH_ENABLED,
                false);
    }

    private String generateServiceTicket(String userName, String service) {
        String serviceTicket = generateServiceTicket();
        String key = getServiceTicketKey(serviceTicket, service);
        ticketUsername.put(key, userName);
        return serviceTicket;
    }

    private String getServiceTicketKey(String serviceTicket, String service){
        if(deploymentUtil!=null && deploymentUtil.getUseOnlyTicketInServiceValidate().equals(Boolean.TRUE)){
            if(serviceTicket==null){
                // to avoid null pointers
                return "";
            }
            return serviceTicket;
        }
        String key = serviceTicket + service;
        ChildApplication childApp = Lookup.getChildApplicationByServiceUrlNew(service);
        if(childApp!=null){
            key = serviceTicket + childApp.getAppKey();
        }
        return key;
    }

    private String generateNativeServiceTicket(String userName, String appKey) {
        String serviceTicket = generateServiceTicket();
        String key = NativeApplicationSession.generateSessionHash(serviceTicket, appKey);
        ticketUsernameNative.put(key, userName);
        return serviceTicket;
    }

    private static String generateServiceTicket() {
        return generateTicket(SERVICE_TICKET_PREFIX, SERVICE_TICKET_LENGTH);
    }

    public static String generateTicket(String prefix, int length) {
        final char[] AVAILABLE_CHARS = "0123456789abcdefghijklmnoqrstyvwxyzABCDEFGHIJKLMNOQRSTYVWXYZ".toCharArray();

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        for (int i = 0; i < length; i++) {
            sb.append(AVAILABLE_CHARS[random.nextInt(AVAILABLE_CHARS.length)]);
        }
        return sb.toString();
    }

    public static String appendTicket(String service, String ticket) {
        String result = null;
        if(service!=null && !service.isEmpty()){
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(service);
            uriComponentsBuilder.queryParam("ticket", ticket);
            result = uriComponentsBuilder.build().toString();
            log.info("appendTicket; result={}",result);
        }
        return result;
    }

    @Override
    public LoginType[] getLoginTypes() {
        readConfiguration();
        List<LoginType> loginTypesList = new LinkedList<LoginType>();
        if(rbacLoginTypeEnabled){
            loginTypesList.add(new LoginType("rbacLoginType"));
        }
        if(siteMinderLoginTypeEnabled){
            loginTypesList.add(new LoginType("siteMinderLoginType"));
        }
        if(forgeRockLoginTypeEnabled && deploymentUtil.isEnableForgeRockIntegration()){
            loginTypesList.add(new LoginType("forgeRockLoginType"));
        }
        if(integratedWindowsLoginTypeEnabled){
            loginTypesList.add(new LoginType("integratedWindowsLoginType"));
        }
        if(windowsLoginTypeEnabled){
            loginTypesList.add(new LoginType("windowsLoginType"));
        }
        if(azureActiveDirectoryLoginTypeEnabled) {
            loginTypesList.add(new LoginType("azureActiveDirectoryLoginType"));
        }
        if (genericHeaderLoginTypeEnabled && deploymentUtil.getClientNameForGenericLogin()!=null && !deploymentUtil.getClientNameForGenericLogin().isEmpty()) {
            loginTypesList.add(new LoginType(deploymentUtil.getClientNameForGenericLogin()+LoginType.LOGIN_GENERIC_HEADER_AUTH));
        }
        return loginTypesList.toArray(new LoginType[loginTypesList.size()]);
    }

    @Override
    public String[] getChangePasswordsAllowed(){
        readConfiguration();
        List<String> changeAllowedList = new ArrayList<String>();
        if(windowsLoginChangePasswordEnabled){
            changeAllowedList.add("windowsLoginType");
        }
        if(azureLoginChangePasswordEabled) {
            changeAllowedList.add("azureActiveDirectoryLoginType");
        }
        return changeAllowedList.toArray(new String[changeAllowedList.size()]);
    }



    @Override
    public void logout(LogoutRequest request){
        String logTypeFromMap = LoginLog.LOGIN_LOG_LOGOUT_MAP.get(request.getLoginType());
        String logTypeEntry = (logTypeFromMap!=null && !logTypeFromMap.isEmpty())?logTypeFromMap:request.getLoginType();
        if(logTypeEntry.equalsIgnoreCase(LoginType.LOGIN_GENERIC_HEADER_AUTH))
            logTypeEntry = deploymentUtil.getClientNameForGenericLogin();
        loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_LOGOUT+" ("+logTypeEntry+") ", true, request.getClientIP(), request.getService(), request.getLogoutType(), request.getSessionHash(), request.getAppKey() ));
        String key = getServiceTicketKey(request.getServiceTicket(), request.getService());
        ticketUsername.remove(key);
    }

    @Override
    public void changePasswordWindows(ChangePasswordRequest request) {
        readConfiguration();

        if (request == null || request.getUserName() == null) {
            log.info("changePasswordWindows; request==null || request.userName==null");
            ErrorInfoException errorInfo = new ErrorInfoException(
                    "noUserNameSpecified");
            throw errorInfo;
        }

        if(request.getOldPassword()==null || request.getNewPassword() ==null){
            log.info("changePasswordWindows; oldPassword==null || newPassword==null");
            ErrorInfoException errorInfo = new ErrorInfoException(
                    "nullPassword");
            throw errorInfo;
        }

        String windowsUsername = request.getUserName().toLowerCase();
        log.trace("changePasswordWindows; windowsUsername={}; domainName={}",
                windowsUsername, domainName);

        User user = userDal.getByIdentity(UserIdentity.WINDOWS_ACCOUNT,
                windowsUsername);
        if (user == null) {
            log.info(
                    "changePasswordWindows; user not found; windowsUsername={}",
                    windowsUsername);
            StringBuilder sb = new StringBuilder();
            sb.append("changePasswordWindows; cannot find user with username: ")
                    .append(request.getUserName());
            ErrorInfoException errorInfo = new ErrorInfoException(
                    "invalidUserName", sb.toString());
            errorInfo.getParameters().put("userName", request.getUserName());
            throw errorInfo;
        }

        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(
                domainName, domainServerUrl);

        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    windowsUsername, request.getOldPassword());

            Authentication result = provider.authenticate(token);
            log.trace("changePasswordWindows; userName={}; authenticated={}",
                    windowsUsername, result.isAuthenticated());
            log.trace("changePasswordWindows; result.name={}", result.getName());

            Object principal = result.getPrincipal();
            log.trace("changePasswordWindows; result.principal={}", principal);

            LdapUserDetails details = null;
            if (principal instanceof LdapUserDetails) {
                details = (LdapUserDetails) principal;
            }
            String dn = details.getDn();
            char unicodePwd[] = new String("\"" + request.getNewPassword()
                    + "\"").toCharArray();
            byte pwdArray[] = new byte[unicodePwd.length * 2];
            for (int i = 0; i < unicodePwd.length; i++) {
                pwdArray[i * 2 + 1] = (byte) (unicodePwd[i] >>> 8);
                pwdArray[i * 2 + 0] = (byte) (unicodePwd[i] & 0xff);
            }
            Attribute newattr = new BasicAttribute("unicodePwd", pwdArray);
            ModificationItem repitem = new ModificationItem(
                    DirContext.REPLACE_ATTRIBUTE, newattr);
            LdapContextSource contextSource = new LdapContextSource();
            contextSource.setUrl(domainServerUrl);
            contextSource.setUserDn(dn);
            contextSource.setPassword(request.getOldPassword());
            contextSource.afterPropertiesSet();
            new LdapTemplate(contextSource).modifyAttributes(dn,
                    new ModificationItem[] { repitem });

        } catch (BadCredentialsException e) {
            log.info(
                    "changePasswordWindows; windowsUserName={}; bad credentials",
                    windowsUsername);
            final String message = "Change password; invalid old password";
            ErrorInfoException errorInfo = new ErrorInfoException(
                    "invalidPassword", message);
            throw errorInfo;

        } catch (Exception e) {
            log.info("changePasswordWindows; windowsUserName={}; exception={}",
                    windowsUsername, e);
            final String message = "unknownError";
            ErrorInfoException errorInfo = new ErrorInfoException(
                    "unknownError", message);
            throw errorInfo;
        }

    }

    private long dateDifference(Date lastLoginTime){
        Date currentDateTime = new Date();
        if(lastLoginTime != null){
            long diff = currentDateTime.getTime() - lastLoginTime.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000);
            return diffDays;
        }
        return 0;


    }

    @Override
    public String getHomeUrlByServiceUrl(String serviceUrl) {
        return Lookup.getHomeUrlByServiceUrlNew(serviceUrl);
    }

    @Override
    public ApplicationDownInfo isApplicationDown(String childApplicationName) {
        return ApplicationMaintenanceCache
                .isApplicationDown(childApplicationName);
    }

    @Override
    public String getLogoutServiceUrl(String serviceUrl) {
        return Lookup.getLogoutUrlByServiceUrlNew(serviceUrl);
    }

    @Override
    public ChildApplication getChildApplicationByServiceUrlOrAppKey(String serviceUrl, String appKey) {
        ChildApplication childApplication = null;
        if(appKey!=null && !appKey.isEmpty() && !appKey.equals("")){
            ChildApplication childApp = Lookup.getChildApplicationByAppKeyNew(appKey);
            if(childApp!=null){
                return childApp;
            }
        }
        if(serviceUrl!=null && !serviceUrl.isEmpty()){
            ChildApplication childApp = Lookup.getChildApplicationByServiceUrlNew(serviceUrl);
            if(childApp!=null){
                return childApp;
            }
        }
        if(childApplication==null){
            log.info("getChildApplicationByServiceUrlOrAppKey; No child app found for service url={}; Check configuration; Assuming it to be {}", serviceUrl, ChildApplication.APP_TYPE.SSO);
            childApplication = new ChildApplication();
            childApplication.setChildApplicationName("notSpecified-"+serviceUrl);
            childApplication.setAppType(ChildApplication.APP_TYPE.SSO.getCode());
            childApplication.setAllowMultipleLogins(false);
            childApplication.setChildApplicationId(-1);
        }
        return childApplication;
    }

    @Override
    public ChildApplication getChildApplicationByServiceUrlAndAppKey(String serviceUrl, String appKey) {
        ChildApplication childApplication = null;
        Boolean isAppKeyValid = Boolean.FALSE;
        if(appKey!=null && !appKey.isEmpty() && !appKey.equals("")){
            ChildApplication childApp = Lookup.getChildApplicationByAppKeyNew(appKey);
            if(childApp!=null){
                isAppKeyValid = Boolean.TRUE;
            }
        }
        if( isAppKeyValid && serviceUrl!=null && !serviceUrl.isEmpty()){
            ChildApplication childApp = Lookup.getChildApplicationByServiceUrlNew(serviceUrl);
            if(childApp!=null){
                return childApp;
            }
        }
        if(childApplication==null){
            log.error("getChildApplicationByServiceUrlAndAppKey; No child app found for service url={}; Check configuration; Assuming it to be {}", serviceUrl, ChildApplication.APP_TYPE.SSO);
            childApplication = new ChildApplication();
            childApplication.setChildApplicationName("notSpecified-"+serviceUrl);
            childApplication.setAppType(ChildApplication.APP_TYPE.SSO.getCode());
            childApplication.setAllowMultipleLogins(false);
            childApplication.setChildApplicationId(-1);
        }
        return childApplication;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse loginNative(LoginRequest nativeLoginRequest) {
        log.trace("loginNative; {}", nativeLoginRequest);
        LoginResponse loginResponse = null;
        ChildApplication childApplication = Lookup.getChildApplicationByAppKeyNew(nativeLoginRequest.getAppKey());
        if(childApplication==null || childApplication.getAppKey()==null){
            loginLogDal.create(LoginLog.createLoginLog(nativeLoginRequest.getUserName(), LoginLog.LOG_TYPE_LOGIN_NATIVE, false, nativeLoginRequest.getClientIP(), nativeLoginRequest.getService(), LoginResponse.UNRECOGNISED_APP, null, nativeLoginRequest.getAppKey()));
            loginResponse = new LoginResponse(LoginResponse.UNRECOGNISED_APP, null);
            loginResponse.setIsSuccess(false);
            return loginResponse;
        }

        String applicationName = childApplication!=null?childApplication.getApplication().getName():null;
        if (userDal.isRevokedApplicationsForUserName(nativeLoginRequest.getUserName(), childApplication)) {
            log.info("loginNative; {} not Authorized for application={}", nativeLoginRequest.getUserName(),applicationName);
            loginLogDal.create(LoginLog.createLoginLog(nativeLoginRequest.getUserName(), LoginLog.LOG_TYPE_LOGIN_NATIVE,false, nativeLoginRequest.getClientIP(), nativeLoginRequest.getService(),
                    LoginResponse.NOT_AUTHORIZED_APP, null, nativeLoginRequest.getAppKey()));
            loginResponse = new LoginResponse(LoginResponse.NOT_AUTHORIZED_APP, null);
            loginResponse.setIsSuccess(false);
            return loginResponse;

        }

        if(!ChildApplication.isNative(childApplication.getAppType()))
        {
            loginLogDal.create(LoginLog.createLoginLog(nativeLoginRequest.getUserName(), LoginLog.LOG_TYPE_LOGIN_NATIVE,
                    false, nativeLoginRequest.getClientIP(), nativeLoginRequest.getService(), LoginResponse.APP_NOT_CONFIGURED_NATIVE,
                    null, nativeLoginRequest.getAppKey()));
            loginResponse = new LoginResponse(LoginResponse.APP_NOT_CONFIGURED_NATIVE, null);
            loginResponse.setIsSuccess(false);
            return loginResponse;
        }
        nativeLoginRequest.setService(null);
//      validateLoginRequest(nativeLoginRequest, LoginLog.LOG_TYPE_LOGIN_NATIVE);
        loginResponse = checkLogin(nativeLoginRequest, LoginType.LOGIN_NATIVE, LoginLog.LOG_TYPE_LOGIN_NATIVE);
        if(loginResponse.getResultCode().equals(LoginResponse.LOGIN_SUCCESSFULL)){
            loginResponse.setIsSuccess(true);
        }
        else{
            loginResponse.setIsSuccess(false);
        }
        loginResponse.setRedirectUrl(null);
        loginResponse.setUserSessionData(null);
        return loginResponse;
    }

    @Override
    public ServiceValidateResponse validateNativeTicket(
            ServiceValidateRequest serviceValidateRequest) {
        if (serviceValidateRequest == null) {
            ServiceValidateResponse response = new ServiceValidateResponse();
            response.setFailureCode("requestNotWellFormed");
            response.setFailureMessage("");
            response.setIsSuccess(false);
            return response;
        }

        String ticket = serviceValidateRequest.getServiceTicket();
        String appKey = serviceValidateRequest.getAppKey();

        if (ticket == null || appKey == null || ticket.isEmpty() || appKey.isEmpty()) {
            ServiceValidateResponse response = new ServiceValidateResponse();
            response.setFailureCode("ticketAppKeyMissing");
            response.setFailureMessage("Ticket or appKey not specified");
            response.setIsSuccess(false);
            return response;
        }
        String key = NativeApplicationSession.generateSessionHash(ticket, appKey);
        String username = ticketUsernameNative.get(key);

        if (username == null || username.isEmpty()) {
            ServiceValidateResponse response = new ServiceValidateResponse();
            response.setFailureCode("ticketNotRecognized");
            response.setFailureMessage("Ticket " + ticket + " not recognized");
            response.setIsSuccess(false);
            return response;
        }

        ServiceValidateResponse response = new ServiceValidateResponse();
        response.setUsername(username);
        response.setIsSuccess(true);
        return response;
    }

    @Override
    public LogoutResponse logoutNative(LogoutRequest request) {
        ChildApplication childApplication = Lookup.getChildApplicationByAppKeyNew(request.getAppKey());
        if(childApplication==null){
            return new LogoutResponse(NativeApplicationSession.generateSessionHash(request.getServiceTicket(), request.getAppKey()), LogoutResponse.INVALID_APP_KEY, false);
        }
        return destroyNativeSession(NativeApplicationSession.generateSessionHash(request.getServiceTicket(), request.getAppKey()), childApplication, request.getClientIP(), LogoutRequest.LOGOUT_TYPE_NATIVE_APP);
    }

    @Override
    public void removeNativeTicket(String nativeSessionHash){
        ticketUsernameNative.remove(nativeSessionHash);
    }
    @Override
    public LogoutResponse destroyNativeSession(String nativeSessionHash, ChildApplication childApplication, String clientIp, String logoutType){
        String userName = ticketUsernameNative.remove(nativeSessionHash);
        if(userName!=null){
            String logTypeFromMap = LoginLog.LOGIN_LOG_LOGOUT_MAP.get(LoginType.LOGIN_NATIVE);
            loginLogDal.create(LoginLog.createLoginLog(userName, LoginLog.LOG_TYPE_LOGOUT+" ("+logTypeFromMap+") ", true, clientIp, null, logoutType, nativeSessionHash, childApplication.getAppKey() ));
            sessionRegistry.logout(userName, nativeSessionHash, null, clientIp, logoutType,childApplication,RBACUtil.LOGOUT_ACTION.LOGOUT_NATIVE , RBACUtil.generateLogoutRequestId(), null, null, null, null, null);
            return new LogoutResponse(nativeSessionHash, LogoutResponse.LOGOUT_SUCCESSFULL, true, childApplication.getChildApplicationName());
        }
        else{
            return new LogoutResponse(nativeSessionHash, LogoutResponse.INVALID_SERVICE_TICKET, false , childApplication.getChildApplicationName());
        }
    }

    @Override
    public LogoutResponse sessionRegistryLogout(SessionRegistryLogoutRequest request,
                                                String loggedInUserName, Boolean forceLogoutViaAppLayer) {
        if(request.getChildApplication()==null){
            request.setChildApplication(getChildApplicationByServiceUrlOrAppKey(request.getService(), request.getAppKey()));
        }
        if (request.getLogoutAction().getCode()
                .equals(RBACUtil.LOGOUT_ACTION.LOGOUT_SSO_TICKET.getCode())
                || request
                .getLogoutAction()
                .getCode()
                .equals(RBACUtil.LOGOUT_ACTION.LOGOUT_NON_SSO_TICKET
                        .getCode())) {
            if (request.getChildApplication() != null
                    && request.getChildApplication().getAppType() != null
                    && ChildApplication.isSSO(request.getChildApplication()
                    .getAppType())) {
                request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_SSO_TICKET);
            } else if (request.getChildApplication() != null
                    && request.getChildApplication().getAppType() != null
                    && ChildApplication.isNonSSO(request.getChildApplication()
                    .getAppType())) {
                request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_NON_SSO_TICKET);
            }
        }
        if(request.getLogoutAction().getCode()
                .equals(RBACUtil.LOGOUT_ACTION.LOGOUT_LINK_CLICK.getCode())){
            if (request.getChildApplication() != null
                    && request.getChildApplication().getAppType() != null
                    && ChildApplication.isSSO(request.getChildApplication()
                    .getAppType())) {
                request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_SSO);
            } else if (request.getChildApplication() != null
                    && request.getChildApplication().getAppType() != null
                    && ChildApplication.isNonSSO(request.getChildApplication()
                    .getAppType())) {
                request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_NON_SSO);
            }
        }
        return sessionRegistry.logout(request.getUserName(), request.getSessionHash(),
                request.getService(), request.getClientIp(),
                request.getLogoutType(), request.getChildApplication(),
                request.getLogoutAction(),
                request.getRequestId(), request.getTicketToLogout(), request.getAppKey(), request.getTag(), request.getCutOffDate(), forceLogoutViaAppLayer);
    }

    @Override
    public boolean isChangePasswordValid(String sessionHash) {
        return sessionRegistry.isUserSessionActive(sessionHash);
    }

    @Override
    public AppUrlData getAppUrlDataByTicket(String ticket) {
        if(ticket!=null && !ticket.isEmpty()){
            Integer appUrlId = sessionDal.getAppUrlIdByTicket(ticket);
            if(appUrlId!=null){
                return Lookup.getAppUrlDataByAppUrlIdNew(appUrlId);
            }
        }
        return null;
    }

    @Override
    public AppUrlData getFirstAppUrlDataByUserNameAndAppKey(String userName, String appKey) {
        if(userName!=null && !userName.isEmpty() && appKey!=null && !appKey.isEmpty()){
            Integer appUrlId = sessionDal.getAppUrlIdByUserNameAndAppKey(userName, appKey);
            if(appUrlId!=null){
                return Lookup.getAppUrlDataByAppUrlIdNew(appUrlId);
            }
        }
        return null;
    }

    @Override
    public ChangePasswordPolicy checkChangePasswordPolicy(String userName) {
        readConfiguration();
        ChangePasswordPolicy policy = new ChangePasswordPolicy();
        policy.setIsPolicyViolated(Boolean.TRUE);

        Integer count = null;
        Integer hour = null;
        Integer maxCount = null;
        if (userName != null && !userName.isEmpty()) {
            try {
                User user = userDal.getByUserName(userName);
                maxCount = noOfTimesPwdChange;
                hour = ageOfPasswordInHour;
                policy.setAllowedPasswordChanges(maxCount);
                policy.setPasswordChangeTimeLimitInHrs(hour);
                policy.setLastPasswordSetTime(user.getPasswordSetTime());
                // System.out.println("maxCount =======>"+maxCount +"==============>"+hour);
                if (maxCount <= 0 || hour <= 0) {
                    policy.setIsPolicyViolated(Boolean.FALSE);
                }
                count = userDal.noOfPasswordChanged(user.getUserId(), hour);
                count--;
            } catch (Exception e) {
                log.error("{}",e);
                return null;
            }
        }
        else
            return null;


        if (count < maxCount) {
            policy.setIsPolicyViolated(Boolean.FALSE);
        }

        return policy;
    }

    @Override
    public Boolean isChangePasswordReturnURLExits(String returnUrl) {
        return sessionDal.isChangePasswordReturnURLExits(returnUrl);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse loginNativeLDAP(LoginRequest nativeLoginRequest) {
        log.trace("loginNativeLDAP; {}", nativeLoginRequest);
        LoginResponse loginResponse = null;
        ChildApplication childApplication = Lookup.getChildApplicationByAppKeyNew(nativeLoginRequest.getAppKey());
        if(childApplication==null || childApplication.getAppKey()==null){
            loginLogDal.create(LoginLog.createLoginLog(nativeLoginRequest.getUserName(), LoginLog.LOG_TYPE_LOGIN_NATIVE_LDAP, false, nativeLoginRequest.getClientIP(), nativeLoginRequest.getService(), LoginResponse.UNRECOGNISED_APP, null, nativeLoginRequest.getAppKey()));
            loginResponse = new LoginResponse(LoginResponse.UNRECOGNISED_APP, null);
            loginResponse.setIsSuccess(false);
            return loginResponse;
        }

        String applicationName = childApplication!=null?childApplication.getApplication().getName():null;
        if (userDal.isRevokedApplicationsForUserName(nativeLoginRequest.getUserName(), childApplication)) {
            log.info("loginNativeLDAP; {} not Authorized for application={}", nativeLoginRequest.getUserName(),
                    applicationName);
            loginLogDal.create(
                    LoginLog.createLoginLog(nativeLoginRequest.getUserName(), LoginLog.LOG_TYPE_LOGIN_NATIVE_LDAP,
                            false, nativeLoginRequest.getClientIP(), nativeLoginRequest.getService(),
                            LoginResponse.NOT_AUTHORIZED_APP, null, nativeLoginRequest.getAppKey()));
            loginResponse = new LoginResponse(LoginResponse.NOT_AUTHORIZED_APP, null);
            loginResponse.setIsSuccess(false);
            return loginResponse;

        }

        if(!ChildApplication.isNative(childApplication.getAppType()))
        {
            loginLogDal.create(LoginLog.createLoginLog(nativeLoginRequest.getUserName(), LoginLog.LOG_TYPE_LOGIN_NATIVE_LDAP,
                    false, nativeLoginRequest.getClientIP(), nativeLoginRequest.getService(), LoginResponse.APP_NOT_CONFIGURED_NATIVE,
                    null, nativeLoginRequest.getAppKey()));
            loginResponse = new LoginResponse(LoginResponse.APP_NOT_CONFIGURED_NATIVE, null);
            loginResponse.setIsSuccess(false);
            return loginResponse;
        }
        nativeLoginRequest.setService(null);
//      validateLoginRequest(nativeLoginRequest, LoginLog.LOG_TYPE_LOGIN_NATIVE);
        loginResponse = checkLoginLDAP(nativeLoginRequest, LoginType.LOGIN_NATIVE, LoginLog.LOG_TYPE_LOGIN_NATIVE_LDAP);
        if(loginResponse.getResultCode().equals(LoginResponse.LOGIN_SUCCESSFULL)){
            loginResponse.setIsSuccess(true);
        }
        else{
            loginResponse.setIsSuccess(false);
        }
        loginResponse.setRedirectUrl(null);
        loginResponse.setUserSessionData(null);
        return loginResponse;
    }

    /**
     *@Description To implement Two Factor Auth
     *@JIRAID RBAC-1562
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse twoFactorAuthRequest(LoginRequest loginRequest) {
        LoginResponse loginResponse = new LoginResponse();
        log.info("twoFactorAuthRequest; twoFactorAuthRequest = {};", loginRequest);
        return validateTwoFactorAuthRequest(loginResponse, loginRequest);
    }

    /**
     * @param loginResponse
     * @param loginRequest
     * @return
     * @JIRAID RBAC-1562
     */
    private LoginResponse validateTwoFactorAuthRequest(LoginResponse loginResponse, LoginRequest loginRequest) {
        if (loginRequest != null) {
            String loginType = loginRequest.getLoginType();
            String loginLogType = loginRequest.getLoginLogType();
            User user = userDal.getByUserName(loginRequest.getUserName());
            if (loginRequest.getIsInvalidToken()) {
                user.setAccountLockedReason("InvalidOTP");
                consecutiveLoginFailures = otpAttempts;
                LoginResponse returnResponse = failedLogin(user, loginRequest, loginRequest.getLoginLogType());
                loginLogDal.create(LoginLog.createLoginLog(loginRequest.getUserName(), loginRequest.getLoginLogType(),
                        false, loginRequest.getClientIP(), loginRequest.getService(),
                        LoginResponse.AUTHENTICATION_FAILED
                                + (user.getAccountLockedReason() != null ? "-" + user.getAccountLockedReason() : ""),
                        loginRequest.getSessionHash(), loginRequest.getAppKey()));
                return returnResponse;

            } else {

                loginResponse = successfullLogin(loginRequest, user, loginRequest.getLoginType(), user.getUserName());
                if (loginType.equals(LoginType.LOGIN_NATIVE)) {
                    loginLogDal.create(LoginLog.createLoginLog(loginRequest.getUserName(), loginLogType,
                            LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse.getResultCode()),
                            loginRequest.getClientIP(), loginRequest.getService(),
                            LoginResponse.OTP_LOGIN_SUCCESSFULL,
                            NativeApplicationSession.generateSessionHash(loginResponse.getServiceTicket(),
                                    loginRequest.getAppKey()),
                            loginRequest.getAppKey()));
                } else {
                    loginLogDal.create(LoginLog.createLoginLog(loginRequest.getUserName(), loginLogType,
                            LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse.getResultCode()),
                            loginRequest.getClientIP(), loginRequest.getService(),
                            LoginResponse.OTP_LOGIN_SUCCESSFULL,
                            loginRequest.getSessionHash(), loginRequest.getAppKey()));
                }

                return loginResponse;
            }

        }
        return loginResponse;

    }

    /**
     *@Description: To get Channel details of a user for two factor auth
     *@JIRAID RBAC-1562
     */
    @Override
    public TwoFactorAuthVO getAuthChanelDetails(String userName) {
        TwoFactorAuthVO twoFact = new TwoFactorAuthVO();
        if (userName != null) {

            User user = userDal.getByUserName(userName);
            if (user != null) {
                String channelType = LoginResponse.CHANNEL_EMAIL;
                Boolean isEmail =  false;
                Boolean isSMS = false;
                if (user.getTwoFactorAuthChannelType() != null && !user.getTwoFactorAuthChannelType().isEmpty()) {
                    String chanelArr[] = user.getTwoFactorAuthChannelType().split(",");

                    for (int i = 0; i < chanelArr.length; i++) {
                        String code = Lookup.getCodeValueById(Long.valueOf(chanelArr[i]));
                        if (code.equalsIgnoreCase(LoginResponse.CHANNEL_SMS)) {
                            twoFact.setPhoneNumber(user.getPhoneNumber());
                            channelType = LoginResponse.CHANNEL_SMS;
                            isSMS = true;
                        }else if (code.equalsIgnoreCase(LoginResponse.CHANNEL_EMAIL)) {
                            twoFact.setEmailId(user.getEmailAddress());
                            channelType = LoginResponse.CHANNEL_EMAIL;
                            isEmail = true;
                        }
                    }
                } else
                    twoFact.setEmailId(user.getEmailAddress()); // setting default channel to email

                if(isEmail && isSMS)
                    channelType = LoginResponse.CHANNEL_SMS_EMAIL;

                twoFact.setChannelType(channelType);
                twoFact.setUserName(user.getUserName());
                String firstName = user.getFirstName() != null && !user.getFirstName().isEmpty() ? user.getFirstName()
                        : "";
                String lastName = user.getLastName() != null && !user.getLastName().isEmpty() ? user.getLastName() : "";
                twoFact.setUserFullName(
                        (!firstName.isEmpty() ? firstName + " " : "") + (!lastName.isEmpty() ? lastName : ""));

                if (!twoFact.getUserFullName().trim().isEmpty())
                    twoFact.setUserFullName(user.getUserName());

                twoFact.setMaxOtpAttempts(otpAttempts);
                twoFact.setOtpTimeout(otpTimeout);
                twoFact.setOtpSessionTimeout(otpSessionTimeout);
            }
        }
        return twoFact;
    }

    @Override
    public User getByUserName(String userName) {
        return userDal.getByUserName(userName);
    }

    /**
     * @Description To check whether two Factor is enabled for loginType
     * @param loginType
     * @return
     * @JIRAID RBAC-1562
     */
    private Boolean checkTwoFactEnabledForLoginType(String loginType) {
        Boolean isenabled = false;
        String loginTypesToEnable = deploymentUtil.getEnableTwoFactorForLoginTypes();
        if (loginTypesToEnable != null && !loginTypesToEnable.isEmpty()) {

            String loginTypeArr[] = loginTypesToEnable.split(",");
            for (String type : loginTypeArr) {

                if (type.equalsIgnoreCase("rbacLoginType") && loginType.equalsIgnoreCase(LoginType.LOGIN_RBAC)) {
                    isenabled = true;
                    break;
                }

                if (type.equalsIgnoreCase("siteMinderLoginType")
                        && loginType.equalsIgnoreCase(LoginType.LOGIN_SITEMINDER)) {
                    isenabled = true;
                    break;
                }

                if (type.equalsIgnoreCase("integratedWindowsLoginType")
                        && loginType.equalsIgnoreCase(LoginType.LOGIN_INTEGRATED_WINDOWS)) {
                    isenabled = true;
                    break;
                }

                if (type.equalsIgnoreCase("windowsLoginType")
                        && loginType.equalsIgnoreCase(LoginType.LOGIN_WINDOWS_AD)) {
                    isenabled = true;
                    break;
                }

            }
        }
        return isenabled;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse loginAzureActiveDirectory(LoginRequest request) {
        log.debug("loginAzureActiveDirectory; {}", request);
//		User user = userDal.getByUserName(request.getUserName());
//		if (user == null)
//			user = userDal.getByEmailAddress(request.getUserName());

        Boolean userNameFound = Boolean.TRUE;
        log.info("Check1: RequestUsername {} with identity",request.getUserName());
        User user = userDal.getByIdentity(UserIdentity.AZURE_AD_ACCOUNT, request.getUserName());
        if(user == null) {
            userNameFound = Boolean.FALSE;
            log.info("NotFound; Next; Check2: RequestUsername {} with Username",request.getUserName());
            user = userDal.getByUserName(request.getUserName());
            if(user == null) {
                userNameFound = Boolean.FALSE;
                log.info("NotFound; Next; Check3: RequestUsername {} with Email Address",request.getUserName());
                user = userDal.getByEmailAddress(request.getUserName());
                if(user == null) {
                    userNameFound = Boolean.FALSE;
                    String extractedId = null;
                    try {
                        String urname = request.getUserName();
                        String splitter = "@"+deploymentUtil.getAzureUserIdentityIssuer();
                        String arr[] = urname.split(splitter);
                        extractedId = arr[0];
                        log.info("NotFound; Next; Check4: RequestUsername {} with ObjectId {}",request.getUserName(),extractedId);
                        user = userDal.getByIdentity(UserIdentity.AZURE_AD_ACCOUNT, extractedId);
                        if(user != null) {
                            userNameFound = Boolean.TRUE;
                            log.debug("RequestUsername found in ObjectId {}",user);
                        }
                    }catch(Exception e) {
                        //do nothing
                    }
                }else {
                    userNameFound = Boolean.TRUE;
                    log.debug("RequestUsername found in email address {}",user);
                }
            }else {
                userNameFound = Boolean.TRUE;
                log.debug("RequestUsername found in username {}",user);
            }

        }


        if (!userNameFound) {
            log.info("loginAzureActiveDirectory; user not found; username={}",
                    request.getUserName());
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(),
                    LoginLog.LOG_TYPE_LOGIN_AZURE_AD, false,
                    request.getClientIP(), request.getService(),
                    LoginResponse.USERNAME_NOT_FOUND,
                    request.getSessionHash(), request.getAppKey()));
            return new LoginResponse(LoginResponse.INVALID_CREDENTIALS,
                    request.getService());
        }

        LoginResponse loginResponse = null;
        String username = user.getUserName();
        ChildApplication childApplication = Lookup.getChildApplicationByAppKeyNew(request.getAppKey());
        if(childApplication==null || childApplication.getAppKey()==null){
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_LOGIN_AZURE_AD, false, request.getClientIP(), request.getService(), LoginResponse.UNRECOGNISED_APP, null, request.getAppKey()));
            loginResponse = new LoginResponse(LoginResponse.UNRECOGNISED_APP, null);
            loginResponse.setIsSuccess(false);
            return loginResponse;
        }
        if(request.getService() == null || request.getService().isEmpty()) {
            Set<AppUrlData> appData = childApplication.getAppUrlDataSet();
            request.setService(appData.iterator().next().getHomeUrl());

        }
        //request.setService(null);
        loginResponse = checkUserAccount(request, user, LoginType.LOGIN_AZURE_ACTIVE_DIRECTORY, username);
        LoginLog loginLog = LoginLog.createLoginLog(request.getUserName(),
                LoginLog.LOG_TYPE_LOGIN_AZURE_AD,
                (LoginResponse.LOGIN_SUCCESSFULL.equals(loginResponse
                        .getResultCode())), request.getClientIP(), request
                        .getService(), loginResponse.getResultCode(), request
                        .getSessionHash(), request.getAppKey());
        loginLog.setUserId(user.getUserId());
        loginLogDal.create(loginLog);

//		 validateLoginRequest(request, LoginLog.LOG_TYPE_LOGIN_AZURE_AD);
//	        loginResponse =  checkLogin(request, LoginType.LOGIN_AZURE_ACTIVE_DIRECTORY, LoginLog.LOG_TYPE_LOGIN_AZURE_AD);
//		return new LoginResponse(LoginResponse.LOGIN_SUCCESSFULL, request.getService());
        return loginResponse;
    }

    @Override
    public LoginResponse validateUserName(LoginRequest request) {
        log.trace("validateUserNameForAzureADLogin; {}", request);
        return checkLoginAzure(request, LoginType.LOGIN_AZURE_ACTIVE_DIRECTORY, LoginLog.LOGIN_TYPE_AZURE_AD);
    }

    private void sendFailedLoginAlert(User user, LoginRequest request) {
        String channelType = getUserChannelDetails(user);
        if (channelType != null) {
            emailDal.sendFailedLoginAlert(user, request, channelType);
        } else
            log.error("Email sending for login attempt failed. Missing emailAddress and/or phone number for user {}",user.getUserName());

    }

    private String getUserChannelDetails(User user) {
        String channelType = null;
        if (user == null)
            return null;

        Boolean isEmail = false;
        Boolean isSMS = false;
        if (userDal.checkTwoFactorActiveForUserAndTenant(Lookup.getTenantIdByOrganizationId(user.getOrganizationId()))
                && user.getTwoFactorAuthChannelType() != null && !user.getTwoFactorAuthChannelType().isEmpty()) {
            String chanelArr[] = user.getTwoFactorAuthChannelType().split(",");

            for (int i = 0; i < chanelArr.length; i++) {
                String code = Lookup.getCodeValueById(Long.valueOf(chanelArr[i]));
                if (code.equalsIgnoreCase(LoginResponse.CHANNEL_SMS) && user.getPhoneNumber() != null
                        && !user.getPhoneNumber().isEmpty()) {
                    channelType = LoginResponse.CHANNEL_SMS;
                    isSMS = true;
                } else if (code.equalsIgnoreCase(LoginResponse.CHANNEL_EMAIL) && user.getEmailAddress() != null
                        && !user.getEmailAddress().isEmpty()) {
                    channelType = LoginResponse.CHANNEL_EMAIL;
                    isEmail = true;
                }
            }
        } else {
            if (user.getEmailAddress() != null && !user.getEmailAddress().isEmpty()) {

                channelType = LoginResponse.CHANNEL_EMAIL;
                isEmail = true;
            }
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                channelType = LoginResponse.CHANNEL_SMS;
                isSMS = true;
            }
        }

        if (isEmail && isSMS)
            channelType = LoginResponse.CHANNEL_SMS_EMAIL;


        return channelType;

    }

}

