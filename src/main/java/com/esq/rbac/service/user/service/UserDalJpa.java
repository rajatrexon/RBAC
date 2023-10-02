/*
 * Copyright (c)2013,2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.service.user.service;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.service.ApplicationDal;
import com.esq.rbac.service.attributes.domain.AttributesData;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.calendar.domain.Calendar;
import com.esq.rbac.service.calendar.service.CalendarDal;
import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.config.CacheConfig;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.group.service.GroupDal;
import com.esq.rbac.service.ivrpasswordhistory.domain.IVRPasswordHistory;
import com.esq.rbac.service.ivrpasswordhistory.repository.IVRPasswordHistoryRepository;
import com.esq.rbac.service.ldapuserservice.service.LdapUserServiceImpl;
import com.esq.rbac.service.loginservice.email.EmailDal;
import com.esq.rbac.service.loginservice.embedded.LoginResponse;
import com.esq.rbac.service.loginservice.embedded.LogoutRequest;
import com.esq.rbac.service.loginservice.embedded.SessionRegistryLogoutRequest;
import com.esq.rbac.service.loginservice.service.LoginService;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.makerchecker.domain.MakerChecker;
import com.esq.rbac.service.makerchecker.service.MakerCheckerDal;
import com.esq.rbac.service.makerchecker.service.MakerCheckerDalJpa;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.password.ivrpasswordpolicy.IVRPasswordPolicy;
import com.esq.rbac.service.password.passwordpolicy.service.PasswordPolicy;
import com.esq.rbac.service.password.paswordHistory.domain.PasswordHistory;
import com.esq.rbac.service.password.paswordHistory.repository.PasswordHistoryRepository;
import com.esq.rbac.service.scope.builder.ScopeBuilder;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.tenant.repository.TenantRepository;
import com.esq.rbac.service.user.azurmanagementconfig.PasswordProfile;
import com.esq.rbac.service.user.azurmanagementconfig.service.AzureManagementConfig;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.embedded.UserIdentity;
import com.esq.rbac.service.user.repository.UserRepository;
import com.esq.rbac.service.user.vo.SSOLogoutData;
import com.esq.rbac.service.user.vo.UserWithLogoutData;
import com.esq.rbac.service.usersync.domain.UserSync;
import com.esq.rbac.service.usersync.service.UserSyncService;
import com.esq.rbac.service.util.*;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.variable.domain.Variable;
import com.esq.rbac.service.variable.repository.VariableRepository;
import com.esq.rbac.service.variable.service.VariableDal;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserDalJpa extends BaseDalJpa implements UserDal {

    public static final String INVALID_USERNAME = "invalidUsername";
    public static final String INVALID_PASSWORD = "invalidPassword";
    public static final String USERNAME_UPDATE_NOT_ALLOWED = "userNameUpdateNotAllowed";
    private static final int PASSWORD_SALT_LENGTH = 64;
    public static final String DUPLICATED_USER = "duplicatedUser";
    public static final String DUPLICATED_IVR_USER_ID = "duplicatedIVRUserId";
    public static final String DUPLICATED_USER_NAME = "duplicatedName";
    public static final String VOILATED_MINIMUM_LENGTH = "voilatedMinimumLength";
    public static final String VOILATED_MINIMUM_LENGTH_VALUE = "voilatedMinimumLengthValue";
    public static final String IVR_PIN_REQUIRED = "ivrPinRequired";
    public static final String DUPLICATE_USER_IDENTITY_MAPPING = "duplicatedUserIdentityMapping";
    public static final String CHANGE_PASSWORD_ON_NEXT_LOGIN="changePasswordOnNextLogin";
    public static final String UNAUTHORIZED_ACCESS="unauthorizedAccess";
    public static final String INVALID_USERNAME_FORMAT = "invalidUserNameFormat";
    public static final String INVALID_MOBILE_FORMAT = "invalidPhoneNumberFormat";
    public static final String USERNAME_EMAIL_MISMATCH = "mismatchInUsernameEmail";
    public static final String DUPLICATED_EMAIL = "duplicateEmail";
    public static final String DUPLICATED_ALTERNATE_EMAIL = "duplicateAlternateEmailAddress";
    private static final Map<String, String> SORT_COLUMNS;


    private EntityManager em;

    @Autowired
    public void setEntityManager(EntityManager em){
        this.em = em;
    }




    private PasswordPolicy passwordPolicy;



    @Autowired
    public void setPasswordPolicy(@Qualifier("passwordPolicyImpl") PasswordPolicy passwordPolicy) {
        this.passwordPolicy = passwordPolicy;
    }



    private IVRPasswordPolicy ivrPasswordPolicy;

    @Autowired
    public void setIVRPasswordPolicy(IVRPasswordPolicy ivrPasswordPolicy) {
        this.ivrPasswordPolicy = ivrPasswordPolicy;
    }

    private VariableDal variableDal;

    @Autowired
    public void setVariableDal(VariableDal variableDal){
        this.variableDal = variableDal;
    }



    private GroupDal groupDal;

    @Autowired
    public void setGroupDal(GroupDal groupDal){
        this.groupDal = groupDal;
    }

    private EmailDal emailDal;

    @Autowired
    public void setEmailDal(EmailDal emailDal){
        this.emailDal = emailDal;
    }


    private DeploymentUtil deploymentUtil;

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil){
        this.deploymentUtil = deploymentUtil;
    }

    private ApplicationDal applicationDal;

    @Autowired
    public void setApplicationDal(ApplicationDal applicationDal){
        this.applicationDal = applicationDal;
    }

    private ChildAppPermValidatorUtil childAppPermValidatorUtil;

    @Autowired
    public void setChildAppPermValidatorUtil(@Lazy ChildAppPermValidatorUtil childAppPermValidatorUtil){
        this.childAppPermValidatorUtil = childAppPermValidatorUtil;
    }

    private AuditLogService auditLogDal;

    @Autowired
    public void setAuditLogDal(AuditLogService auditLogDal){
        this.auditLogDal = auditLogDal;
    }

    private LoginService loginService;

    @Autowired
    public void setLoginService(LoginService loginService){
        this.loginService = loginService;
    }

    private CalendarDal calendarDal;

    @Autowired
    public void setCalendarDal(CalendarDal calendarDal){
        this.calendarDal = calendarDal;
    }

    private ContactDispatcherUtil contactDispatcherUtil;

    @Autowired
    public void setContactDispatcherUtil(ContactDispatcherUtil contactDispatcherUtil){
        this.contactDispatcherUtil = contactDispatcherUtil;
    }

    private Configuration configuration;

    @Autowired
    public void setConfiguration(@Qualifier("DatabaseConfigurationWithCache") Configuration configuration){
        this.configuration = configuration;
    }

    private MakerCheckerDal makerCheckerDal;
    @Autowired
    public void setMakerCheckerDal(MakerCheckerDal makerCheckerDal){
        this.makerCheckerDal = makerCheckerDal;
    }

    private UserSyncService userSyncDal;

    @Autowired
    public void setUserSyncService(UserSyncService userSync){
        this.userSyncDal = userSync;
    }



    private CacheService cacheService;

    @Autowired
    public void setCacheService(CacheService cacheService){
        this.cacheService = cacheService;
    }




    IVRPasswordHistoryRepository ivrPasswordHistoryRepository;

    @Autowired
    public void setIVRPasswordHistoryRepository(IVRPasswordHistoryRepository ivrPasswordHistoryRepository) {
        this.ivrPasswordHistoryRepository = ivrPasswordHistoryRepository;
    }




    PasswordHistoryRepository passwordHistoryRepository;

    @Autowired
    public void setIvrPasswordHistoryRepository(PasswordHistoryRepository passwordHistoryRepository){
        this.passwordHistoryRepository=passwordHistoryRepository;
    }

    private AzureManagementConfig azureManagementConfig;

    @Autowired
    public void setAzureManagementConfig(AzureManagementConfig azureManagementConfig) {
        this.azureManagementConfig = azureManagementConfig;
    }


    private TenantRepository tenantRepository;

    @Autowired
    public void setTenantRepository(TenantRepository tenantRepository){
        this.tenantRepository=tenantRepository;
    }
    public static final String INVALID_EMAIL = "invalidEmail"; //RBAC-1562
    public static final String INVALID_MOBILENO = "invalidMobile"; //RBAC-1562


    private OrganizationMaintenanceDal organizationMaintenanceDal;

    @Autowired
    public void setOrganizationMaintenanceDal(OrganizationMaintenanceDal organizationMaintenanceDal) {
        this.organizationMaintenanceDal = organizationMaintenanceDal;
    }

    private VariableRepository variableRepository;

    @Autowired
    public void setVariableRepository(VariableRepository variableRepository){
        this.variableRepository=variableRepository;
    }

    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("isEnabled", "u.isEnabled");
        SORT_COLUMNS.put("userName", "u.userName");
    }

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository){
        this.userRepository=userRepository;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public User create(User user, int loggedInUserId, String target, String operation) {
    	cacheService.clearCache(CacheConfig.CLEAR_ALL_ORG_CACHE);//CACHE:: CLEAR
        if (user == null) {
            throw new IllegalArgumentException();
        }

        user.setUserName(user.getUserName().trim());   // RBAC-1868
        // RBAC-1868 Start
        if(user.getUserName().length() < deploymentUtil.getUserNameMinLength()) {
        	StringBuilder sb = new StringBuilder();
            sb.append(VOILATED_MINIMUM_LENGTH).append("; ");
            sb.append(VOILATED_MINIMUM_LENGTH_VALUE).append("=").append(deploymentUtil.getUserNameMinLength().toString());
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(VOILATED_MINIMUM_LENGTH, sb.toString());
            errorInfo.getParameters().put(VOILATED_MINIMUM_LENGTH_VALUE, deploymentUtil.getUserNameMinLength().toString());
            log.info("create; usererrorInfo={}", errorInfo);
            throw errorInfo;
        }
     // RBAC-1868 End

		if(user.getUseEmailAsUserid() != null && user.getUseEmailAsUserid()) {

			if(!user.getEmailAddress().equalsIgnoreCase(user.getUserName())) {
				StringBuilder sb = new StringBuilder();
				sb.append(USERNAME_EMAIL_MISMATCH).append("; ");
				ErrorInfoException errorInfo = new ErrorInfoException(USERNAME_EMAIL_MISMATCH, sb.toString());
				log.info("mismatchInUsernameEmail={}", errorInfo);
				throw errorInfo;
			}

			boolean validEmail = Pattern.matches(DeploymentUtil.EMAIL_PATTERN, user.getUserName());
			if(!validEmail) {
				StringBuilder sb = new StringBuilder();
				sb.append(INVALID_USERNAME_FORMAT).append("; ");
				ErrorInfoException errorInfo = new ErrorInfoException(INVALID_USERNAME_FORMAT, sb.toString());
				log.info("invalidUserName={}", errorInfo);
				throw errorInfo;
			}

			int emailDuplicate = isEmailDuplicate(user.getUserId(), user.getEmailAddress());
	        if (emailDuplicate > 0) {
	            StringBuilder sb = new StringBuilder();
	            sb.append(DUPLICATED_USER).append("; ");
	            sb.append(DUPLICATED_USER_NAME).append("=").append(user.getEmailAddress());
	            log.info("create; {}", sb.toString());
	            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_USER, sb.toString());
	            errorInfo.getParameters().put(DUPLICATED_USER_NAME, user.getUserName());
	            log.info("create; usererrorInfo={}", errorInfo);
	            throw errorInfo;
	        }
		}


		if(deploymentUtil.isAzureUserMgmtEnabled()) {
			boolean validUserNameEmailFormat = Pattern.matches(DeploymentUtil.EMAIL_PATTERN, user.getUserName());
        	if(deploymentUtil.getUsernameValidationRegex() != null && !deploymentUtil.getUsernameValidationRegex().isEmpty())
        		validUserNameEmailFormat = Pattern.matches(deploymentUtil.getUsernameValidationRegex(), user.getUserName());
			if (!validUserNameEmailFormat) {
				StringBuilder sb = new StringBuilder();
				sb.append(INVALID_USERNAME_FORMAT).append("; ");
				ErrorInfoException errorInfo = new ErrorInfoException(INVALID_USERNAME_FORMAT, sb.toString());
				if (deploymentUtil.getUsernameValidationCustomMessage() != null
						&& !deploymentUtil.getUsernameValidationCustomMessage().isEmpty()) {
					errorInfo = new ErrorInfoException("genError");
					errorInfo.getParameters().put("value", deploymentUtil.getUsernameValidationCustomMessage());
				}

				log.info("invalidUserName={}", errorInfo);
				throw errorInfo;
			}

			boolean validMobileFormat = Pattern.matches(DeploymentUtil.MOBILE_PATTERN_COUNTRY_CODE, user.getPhoneNumber());
			if(!validMobileFormat) {
				StringBuilder sb = new StringBuilder();
				sb.append(INVALID_MOBILE_FORMAT).append("; ");
				ErrorInfoException errorInfo = new ErrorInfoException(INVALID_MOBILE_FORMAT, sb.toString());
				log.info("invalidMobileFormat={}", errorInfo);
				throw errorInfo;
			}
		}
		/*Boolean isValidGroupAndOrgId=isGroupIdAndOrganizationIdForLoggedInTenant(user.getLoggedInTenantId(),user.getOrganizationId(),user.getGroupId());



        if(!isValidGroupAndOrgId) {
        	StringBuilder sb = new StringBuilder();
            sb.append(UNAUTHORIZED_ACCESS).append("; ");
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(UNAUTHORIZED_ACCESS, sb.toString());
            log.info("create; usererrorInfo={}", errorInfo);
            throw errorInfo;
        }*/

		 /*added by pankaj for RBAC-1559
		Enforce password change in first login
		start
		*/
    	/*Remarks Only in case of new user*/
        if(user.getIdentities()!=null && user.getIdentities().size()==0) {

        	if(!azureManagementConfig.isAzureUserMgmtEnabled() && !user.getChangePasswordFlag()) {
        		StringBuilder sb = new StringBuilder();
                sb.append(CHANGE_PASSWORD_ON_NEXT_LOGIN).append("; ");
                log.info("create; {}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(CHANGE_PASSWORD_ON_NEXT_LOGIN, sb.toString());
                log.info("create; usererrorInfo={}", errorInfo);
                throw errorInfo;
        	}

        }


        /*added by pankaj for RBAC-1559
		Enforce password change in first login
		end
		*/
        int userDuplicate = isUserNameDuplicate(user.getUserId(), user.getUserName());
        if (userDuplicate > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_USER).append("; ");
            sb.append(DUPLICATED_USER_NAME).append("=").append(user.getUserName());
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_USER, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_USER_NAME, user.getUserName());
            log.info("create; usererrorInfo={}", errorInfo);
            throw errorInfo;
        }

        //check for duplicate user email address
        if(deploymentUtil.isUniqueEmailId()) {
        	if(user.getEmailAddress()!= null && !user.getEmailAddress().isEmpty() && user.getHomeEmailAddress()!= null && !user.getHomeEmailAddress().isEmpty()) {
	        	if(user.getEmailAddress().equalsIgnoreCase(user.getHomeEmailAddress())) {
        		StringBuilder sb = new StringBuilder();
        		sb.append(DUPLICATED_ALTERNATE_EMAIL).append("; ");
        		log.info("create: {}", sb.toString());
        		ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_ALTERNATE_EMAIL, sb.toString());
        		log.info("create; usererrorInfo={}", errorInfo);
        		throw errorInfo;
        	}
        	}
        	if(user.getEmailAddress() != null && !user.getEmailAddress().isEmpty()) {
        		int userEmailDuplicate = isEmailDuplicate(user.getUserId(), user.getEmailAddress()); //isUserEmailIdDuplicate(user.getEmailAddress(), user.getUserName());
            	if(userEmailDuplicate > 0) {
        		StringBuilder sb = new StringBuilder();
        		sb.append(DUPLICATED_EMAIL).append("; ");
        		log.info("create: {}", sb.toString());
        		ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_EMAIL, sb.toString());
        		log.info("create; usererrorInfo={}", errorInfo);
        		throw errorInfo;
        	}
        }
        }
        boolean makerCheckerCheck = Lookup.checkMakerCheckerEnabledInTenant(Lookup.getTenantIdByOrganizationId(user.getOrganizationId()));
        if(makerCheckerCheck && deploymentUtil.getIsMakercheckerActivated()) {
			String userNameTemp = MakerCheckerDalJpa.convertToMKRUserName(user.getUserName(),null);
			userDuplicate = isUserNameDuplicateLike(user.getUserId(), userNameTemp);
			if (userDuplicate > 0 || userDuplicate == -1) {
				StringBuilder sb = new StringBuilder();
				sb.append(DUPLICATED_USER).append("; ");
				sb.append(DUPLICATED_USER_NAME).append("=").append(user.getUserName());
				log.info("update; {}", sb.toString());
				ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_USER, sb.toString());
				errorInfo.getParameters().put(DUPLICATED_USER_NAME, user.getUserName());
				log.info("update; usererrorInfo={}", errorInfo);
				throw errorInfo;
			}
        }
        User newUser = new User();
        // values for newly created user
        newUser.setConsecutiveLoginFailures(0);
        // copy only allowed properties
        newUser.setUserName(user.getUserName());
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setEmailAddress(user.getEmailAddress());
        newUser.setHomeEmailAddress(user.getHomeEmailAddress());
        newUser.setPhoneNumber(user.getPhoneNumber());
        newUser.setHomePhoneNumber(user.getHomePhoneNumber());
        newUser.setNotes(user.getNotes());
        newUser.setLabels(user.getLabels());
        //newUser.setVariables(user.getVariables());
        newUser.setIsEnabled(user.getIsEnabled() != null ? user.getIsEnabled() : false);
        newUser.setIsLocked(user.getIsLocked() != null ? user.getIsLocked() : false);
        newUser.setIsShared(user.getIsShared() != null ? user.getIsShared() : false);
        newUser.setChangePasswordFlag(user.getChangePasswordFlag() != null ? user.getChangePasswordFlag() : false);
        newUser.setGroupId(user.getGroupId() != null && user.getGroupId() > -1?user.getGroupId(): null);
        newUser.setCreatedBy(loggedInUserId);
        Date createdDate = DateTime.now().toDate();
        newUser.setCreatedOn(createdDate);
        newUser.setRestrictions(user.getRestrictions());
        newUser.setOrganizationId(user.getOrganizationId());
        String preferredLanguage = (user.getPreferredLanguage() != null && !user.getPreferredLanguage().isEmpty() && Lookup.isLanguageValid(user.getPreferredLanguage()))? user.getPreferredLanguage() : null;
        newUser.setPreferredLanguage(preferredLanguage);
        newUser.setTimeZone((user.getTimeZone() != null && !user.getTimeZone().isEmpty() && Lookup.isTimeZoneValid(user.getTimeZone()))? user.getTimeZone() : null);
        if (newUser.getRestrictions() != null) {
            newUser.getRestrictions().setRestrictionId(null);
        }
        if (user.getUserCalendar()!=null && user.getUserCalendar().getCalendarId()!=null && user.getUserCalendar().getCalendarId()==0) {
        	com.esq.rbac.service.calendar.domain.Calendar persistedCalendar = calendarDal.create(user.getUserCalendar(), new AuditLogInfo(loggedInUserId, null));
        	user.setUserCalendar(persistedCalendar);
        }
        if(user.getUserCalendar()!=null && (user.getUserCalendar().getCreatedBy()==null || user.getUserCalendar().getCreatedOn()==null)){
        	user.getUserCalendar().setCreatedBy(loggedInUserId);
        	user.getUserCalendar().setCreatedOn(createdDate);
        }
        newUser.setUserCalendar(user.getUserCalendar());
        if(user.getOrgCalendar()!=null && user.getOrgCalendar().getCalendarId()!=null){
            newUser.setOrgCalendar(user.getOrgCalendar());
        }
        if (!isUserIdentityAssociationValid(user.getIdentities(), 0)) {
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATE_USER_IDENTITY_MAPPING);
            throw errorInfo;
        }
        newUser.setIdentities(user.getIdentities());
        newUser.setUseEmailAsUserid(user.getUseEmailAsUserid());

        if(!StringUtils.isEmpty(user.getIvrUserId())){
        	long ivrUserIdDuplicate = isIvrUserIdDuplicate(user.getUserId(), user.getIvrUserId());
        	if (ivrUserIdDuplicate > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(DUPLICATED_IVR_USER_ID).append("; ");
                sb.append(DUPLICATED_IVR_USER_ID).append("=").append(user.getIvrUserId());
                log.info("update; {}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_IVR_USER_ID, sb.toString());
                errorInfo.getParameters().put(DUPLICATED_IVR_USER_ID, user.getIvrUserId());
                log.info("update; usererrorInfo={}", errorInfo);
                throw errorInfo;
            }else{
            	newUser.setIvrUserId(user.getIvrUserId());
            }

        	if(StringUtils.isEmpty(user.getIvrPin()) && !StringUtils.isEmpty(user.getIvrUserId())){
        	    StringBuilder sb = new StringBuilder();
                sb.append(IVR_PIN_REQUIRED).append("; ");
                sb.append(IVR_PIN_REQUIRED).append("=").append(user.getIvrUserId());
                log.info("update; {}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(IVR_PIN_REQUIRED, sb.toString());
                errorInfo.getParameters().put(IVR_PIN_REQUIRED, user.getIvrUserId());
                log.info("update; usererrorInfo={}", errorInfo);
                throw errorInfo;
            }

            newUser.setIsIVRUserLocked(user.getIsIVRUserLocked());

        }
        //RBAC-1562 Start
        if (checkTwoFactorActiveForUserAndTenant(Lookup.getTenantIdByOrganizationId(user.getOrganizationId()))) {
			newUser = getCodesIdFromChannel(user,newUser);
		}
        //End RBAC-1562

        newUser.setIsStatus(1); /** Added By Fazia for maker checker **/

        String uniqueId =  azureManagementConfig.createUser(user);
        if(uniqueId != null && !uniqueId.isEmpty()) {
        	List<UserIdentity> userIdentity = newUser.getIdentities();
        	if(userIdentity == null || userIdentity.isEmpty())
        	userIdentity = new ArrayList<>();
			log.debug("Azure user response Id {}", uniqueId);
			String identityType = UserIdentity.AZURE_AD_ACCOUNT;
			if (identityType != null && !identityType.isEmpty()) {
				userIdentity.add(new UserIdentity(identityType, uniqueId));
				newUser.setIdentities(userIdentity);
			}
        }

        // persist
//        em.persist(newUser);
        userRepository.save(newUser);

        if(!StringUtils.isEmpty(user.getIvrPin())){
        	setIVRPin(newUser, user.getIvrPin());
        }

       /* if (user.getVariables() != null && !user.getVariables().isEmpty()) {
            for (Variable variable : user.getVariables()) {
                variable.setUserId(newUser.getUserId());
            }
        }
        newUser.setVariables(user.getVariables());*/

        //if generatePasswordFlag is set, ignore password sent by user.
        if(user.getGeneratePasswordFlag()!=null && user.getGeneratePasswordFlag().equals(Boolean.TRUE)){
        	user.setChangePassword(passwordPolicy.generateRandomPassword(newUser, 1));
        }
        // in case changePassword is set
        if (user.getChangePassword() != null) {
            // in order to get userId
            //em.flush();
            setPassword(newUser, user.getChangePassword());
        }
        //set attributes
        if(user.getAttributesData()!=null && !user.getAttributesData().isEmpty()){
        	for(AttributesData attrData: user.getAttributesData()){
        		attrData.setUser(newUser);
        	}
        }
        newUser.setAttributesData(user.getAttributesData());
        /* RBAC-1475 MakerChecker Start */
       if(makerCheckerCheck && deploymentUtil.getIsMakercheckerActivated()) {
    	   if(user.getLoggedInTenantId() == null)
    		   user.setLoggedInTenantId(Lookup.getTenantIdByOrganizationId(user.getOrganizationId()));
        	MakerChecker makerChecker = makerCheckerDal.createEntry(newUser, User.class, loggedInUserId, target, operation,newUser.getUserId(),user.getLoggedInTenantId());
     	   newUser.setMakerCheckerId(makerChecker.getId());
     	// Added maker checker Id to username to enable user creation for tenants when maker checker is disabled.
     	  String username = MakerCheckerDalJpa.convertToMKRUserName(newUser.getUserName(),makerChecker.getId());  // Added the ID to username to identify temporary user
     	  newUser.setUserName(username);
     	   newUser.setIsStatus(0);
        }
        /* RBAC-1475 MakerChecker End */
	//	User addedUser = em.merge(newUser);
        User addedUser = userRepository.save(newUser);
		//setNewObjectChangeSet(newUser);
		if (!makerCheckerCheck || !deploymentUtil.getIsMakercheckerActivated()) {
			addedUser.setExternalRecordId(user.getExternalRecordId());
			Map<String, String> objectChanges = setNewObjectChangeSetLocal(addedUser);
			emailDal.sendAlert(user, addedUser.getUserId().toString(), "newUserCreation");
			auditLogDal.createAsyncLog(loggedInUserId, user.getUserName(), target, operation, objectChanges);
		}else
		{
			log.info("===============Mail not sent, User sent to maker chekcer=======================");
		}
        return addedUser;
    }

    private int isUserNameDuplicateLike(Integer userId, String userName) {
    	try {
            User user = getByUserNameLike(userName);
            if (user != null) {
                if (userId != null && userId.intValue() > 0) {
                    if (userId.equals(user.getUserId())) {
                        return 0;
                    }
                }
                return 1;
            }
            // TODO Auto-generated method stub
            return 0;
        	}catch(Exception e) {
        		log.error("{}",e.getMessage());
        		return -1;
        	}
        }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @CachePut(value = CacheConfig.USER_BY_USERNAME_LIKE_CACHE, unless="#result == null")
    public User getByUserNameLike(String userName) {
        try {
//            TypedQuery<User> query = em.createNamedQuery("getUserByUserNameLike", User.class);
//            query.setParameter(
//                    "userName", userName+"%");
//            return query.getSingleResult();

            return userRepository.getUserByUserNameLike(userName);
        } catch (NoResultException e) {
            return null;
        }
    }

	@Override
    @Transactional(propagation = Propagation.REQUIRED)
	public UserWithLogoutData update(User user, int loggedInUserId, String clientIp) {
		cacheService.clearCache(CacheConfig.CLEAR_ALL_CACHE);//CACHE:: CLEAR
        if (user == null) {
            throw new IllegalArgumentException();
        }
        Boolean isPasswordChanged = Boolean.FALSE;
        /** RBAC-1475 MakerChecker Start **/
		if (user.getUserId() == null) {
			throw new ErrorInfoException("userIdMissing", "userId missing");
		}
		Map<String, Object> updateObj = new HashMap<String, Object>();
		Set<String> otheMails = new HashSet<String>();
		int userId = user.getUserId();
//		User existingUser = em.find(User.class, userId);
        Optional<User> existingUser = userRepository.findById(userId);
		if (existingUser == null) {
			ErrorInfoException e = new ErrorInfoException("invalidUserId", "userId invalid");
			e.getParameters().put("userId", Integer.toString(userId));
			throw e;
		}

		  boolean makerCheckerCheck = Lookup.checkMakerCheckerEnabledInTenant(Lookup.getTenantIdByOrganizationId(existingUser.get().getOrganizationId()));
	        if(makerCheckerCheck && deploymentUtil.getIsMakercheckerActivated()) {
				String userNameTemp = MakerCheckerDalJpa.convertToMKRUserName(user.getUserName(),null);
				Integer userDuplicate = isUserNameDuplicateLike(existingUser.get().getUserId(), userNameTemp);
				if (userDuplicate > 0 || userDuplicate == -1) {
					StringBuilder sb = new StringBuilder();
					sb.append(DUPLICATED_USER).append("; ");
					sb.append(DUPLICATED_USER_NAME).append("=").append(user.getUserName());
					log.info("update; {}", sb.toString());
					ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_USER, sb.toString());
					errorInfo.getParameters().put(DUPLICATED_USER_NAME, user.getUserName());
					log.info("update; usererrorInfo={}", errorInfo);
					throw errorInfo;
				}
	        }

		if (user.getIsStatus() != null && user.getIsStatus() == 1 && existingUser.get().getIsStatus() == 0 && existingUser.get().getMakerCheckerId() != null) {
			// Removing maker checker Id from username and checking if the username already
			// exists in system.
			String username = MakerCheckerDalJpa.extractUserNameFromMKR(existingUser.get().getUserName());
			existingUser.get().setUserName(username);

			Integer userDuplicate = isUserNameDuplicate(existingUser.get().getUserId(), existingUser.get().getUserName());
			if (userDuplicate > 0 || userDuplicate == -1) {
				StringBuilder sb = new StringBuilder();
				sb.append(DUPLICATED_USER).append("; ");
				sb.append(DUPLICATED_USER_NAME).append("=").append(username);
				log.info("update; {}", sb.toString());
				ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_USER, sb.toString());
				errorInfo.getParameters().put(DUPLICATED_USER_NAME, username);
				log.info("update; usererrorInfo={}", errorInfo);
				throw errorInfo;
			}

			existingUser.get().setIsStatus(1);
			azureManagementConfig.createUser(existingUser.get());
//			User retUser = em.merge(existingUser);
            User retUser = userRepository.save(existingUser.orElse(null));

			UserSync usersyncDetails = userSyncDal.getByUserId(retUser.getUserId());
			if(usersyncDetails != null && userSyncDal != null && usersyncDetails.getStatus().equals(LdapUserServiceImpl.LDAP_CREATED))
				retUser.setExternalRecordId(usersyncDetails.getExternalRecordId());

			Map<String, String> objectChanges = setNewObjectChangeSetLocal(retUser);
			emailDal.sendAlert(retUser, retUser.getUserId().toString(), "newUserCreation");
			log.info(
					"================sent maker checker entity creation mail to user =====================================");
			auditLogDal.createAsyncLog(loggedInUserId, retUser.getUserName(), "User", "Create", objectChanges);
			return new UserWithLogoutData(retUser, new ArrayList<>());
		} else {
			if (existingUser.get().getIsStatus() == 0 && existingUser.get().getMakerCheckerId() != null) {
				MakerChecker mkr = makerCheckerDal.getById(existingUser.get().getMakerCheckerId());
				if (mkr.getCreatedBy() != loggedInUserId) {
					ErrorInfoException e = new ErrorInfoException(MKR_UPDATE_NOT_ALLOWED);
					throw e;
				}
				// Added maker checker Id to username to enable user creation for tenants when
				// maker checker is disabled.
				String username = MakerCheckerDalJpa.convertToMKRUserName(user.getUserName(),existingUser.get().getMakerCheckerId());
				user.setUserName(username);
			}
        	/** RBAC-1475 MakerChecker End **/

		user.setUserName(user.getUserName().trim());  // RBAC-1868
		// RBAC-1868 Start
        if(user.getUserName().length() < deploymentUtil.getUserNameMinLength()) {
        	StringBuilder sb = new StringBuilder();
            sb.append(VOILATED_MINIMUM_LENGTH).append("; ");
            sb.append(VOILATED_MINIMUM_LENGTH_VALUE).append("=").append(deploymentUtil.getUserNameMinLength().toString());
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(VOILATED_MINIMUM_LENGTH, sb.toString());
            errorInfo.getParameters().put(VOILATED_MINIMUM_LENGTH_VALUE, deploymentUtil.getUserNameMinLength().toString());
            log.info("create; usererrorInfo={}", errorInfo);
            throw errorInfo;
        }
        // RBAC-1868 End
        int userDuplicate = isUserNameDuplicate(user.getUserId(), user.getUserName());
        if (userDuplicate > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_USER).append("; ");
            sb.append(DUPLICATED_USER_NAME).append("=").append(user.getUserName());
            log.info("update; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_USER, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_USER_NAME, user.getUserName());
            log.info("update; usererrorInfo={}", errorInfo);
            throw errorInfo;
        }

        //check for duplicate user email address
        if(deploymentUtil.isUniqueEmailId()) {
        	if(user.getEmailAddress()!= null && !user.getEmailAddress().isEmpty() && user.getHomeEmailAddress()!= null && !user.getHomeEmailAddress().isEmpty()) {
	        	if(user.getEmailAddress().equalsIgnoreCase(user.getHomeEmailAddress())) {
        		StringBuilder sb = new StringBuilder();
        		sb.append(DUPLICATED_ALTERNATE_EMAIL).append("; ");
        		log.info("update: {}", sb.toString());
        		ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_ALTERNATE_EMAIL, sb.toString());
        		log.info("update; usererrorInfo={}", errorInfo);
        		throw errorInfo;
        	}
        	}
        	if(user.getEmailAddress() != null && !user.getEmailAddress().isEmpty()) {
        		int userEmailDuplicate = isEmailDuplicate(user.getUserId(), user.getEmailAddress());//(user.getEmailAddress(), user.getUserName());
            	if(userEmailDuplicate > 0) {
        		StringBuilder sb = new StringBuilder();
        		sb.append(DUPLICATED_EMAIL).append("; ");
        		log.info("update: {}", sb.toString());
        		ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_EMAIL, sb.toString());
        		log.info("update; usererrorInfo={}", errorInfo);
        		throw errorInfo;
        	}
        }
        }

        boolean existingIdMappingFound = false;
        if(existingUser.get().getIdentities()!=null && !existingUser.get().getIdentities().isEmpty()){
        	existingIdMappingFound = true;
        }

        if(StringUtils.isEmpty(existingUser.get().getIvrPin()) && StringUtils.isEmpty(user.getIvrPin()) && StringUtils.isEmpty(existingUser.get().getIvrUserId()) && !StringUtils.isEmpty(user.getIvrUserId())){
    	    StringBuilder sb = new StringBuilder();
            sb.append(IVR_PIN_REQUIRED).append("; ");
            sb.append(IVR_PIN_REQUIRED).append("=").append(user.getIvrUserId());
            log.info("update; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(IVR_PIN_REQUIRED, sb.toString());
            errorInfo.getParameters().put(IVR_PIN_REQUIRED, user.getIvrUserId());
            log.info("update; usererrorInfo={}", errorInfo);
            throw errorInfo;
        }

        //if generatePasswordFlag is set, ignore password sent by user.
        if(user.getGeneratePasswordFlag()!=null && user.getGeneratePasswordFlag().equals(Boolean.TRUE)){
        	user.setChangePassword(passwordPolicy.generateRandomPassword(existingUser.get(), 1));
        }
        // in case changePassword is set
        if (user.getChangePassword() != null) {
        	PasswordProfile pwdProfile = new PasswordProfile();
			pwdProfile.setForceChangePasswordNextSignIn(user.getChangePasswordFlag());
			pwdProfile.setPassword(user.getChangePassword());
			updateObj.put("passwordProfile", pwdProfile);
            setPassword(existingUser.get(), user.getChangePassword());
            isPasswordChanged = Boolean.TRUE;
        }
        boolean isUserRenamed = false;
        String existingUserName = existingUser.get().getUserName();
        if(!user.getUserName().
        		equals(existingUserName)){
        	 isUserRenamed = true;
        }

        if(user.getUseEmailAsUserid() != null && user.getUseEmailAsUserid()) {
        	if(!user.getEmailAddress().equalsIgnoreCase(user.getUserName())) {
				StringBuilder sb = new StringBuilder();
				sb.append(USERNAME_EMAIL_MISMATCH).append("; ");
				ErrorInfoException errorInfo = new ErrorInfoException(USERNAME_EMAIL_MISMATCH, sb.toString());
				log.info("mismatchInUsernameEmail={}", errorInfo);
				throw errorInfo;
			}

			boolean validEmail = Pattern.matches(DeploymentUtil.EMAIL_PATTERN, user.getUserName());
			if(!validEmail) {
				StringBuilder sb = new StringBuilder();
				sb.append(INVALID_USERNAME_FORMAT).append("; ");
				ErrorInfoException errorInfo = new ErrorInfoException(INVALID_USERNAME_FORMAT, sb.toString());
				log.info("invalidUserName={}", errorInfo);
				throw errorInfo;
			}

			int emailDuplicate = isEmailDuplicate(user.getUserId(), user.getEmailAddress());
	        if (emailDuplicate > 0) {
	            StringBuilder sb = new StringBuilder();
	            sb.append(DUPLICATED_USER).append("; ");
	            sb.append(DUPLICATED_USER_NAME).append("=").append(user.getEmailAddress());
	            log.info("update; {}", sb.toString());
	            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_USER, sb.toString());
	            errorInfo.getParameters().put(DUPLICATED_USER_NAME, user.getUserName());
	            log.info("update; usererrorInfo={}", errorInfo);
	            throw errorInfo;
	        }
		}

        if(deploymentUtil.isAzureUserMgmtEnabled()) {
        	if(!user.getUserName().equalsIgnoreCase(existingUser.get().getUserName())){
				ErrorInfoException errorInfo = new ErrorInfoException(USERNAME_UPDATE_NOT_ALLOWED);
				log.info("update; usererrorInfo={}", errorInfo);
				throw errorInfo;
        	}

        	boolean validUserNameEmailFormat = Pattern.matches(DeploymentUtil.EMAIL_PATTERN, user.getUserName());
        	if(deploymentUtil.getUsernameValidationRegex() != null && !deploymentUtil.getUsernameValidationRegex().isEmpty())
        		validUserNameEmailFormat = Pattern.matches(deploymentUtil.getUsernameValidationRegex(), user.getUserName());
			if(!validUserNameEmailFormat) {
				StringBuilder sb = new StringBuilder();
				sb.append(INVALID_USERNAME_FORMAT).append("; ");
				ErrorInfoException errorInfo = new ErrorInfoException(INVALID_USERNAME_FORMAT, sb.toString());
				if (deploymentUtil.getUsernameValidationCustomMessage() != null
						&& !deploymentUtil.getUsernameValidationCustomMessage().isEmpty()) {
					errorInfo = new ErrorInfoException("genError");
					errorInfo.getParameters().put("value", deploymentUtil.getUsernameValidationCustomMessage());
				}
				log.info("invalidUserName={}", errorInfo);
				throw errorInfo;
			}

			boolean validMobileFormat = Pattern.matches(DeploymentUtil.MOBILE_PATTERN_COUNTRY_CODE, user.getPhoneNumber());
			if(!validMobileFormat) {
				StringBuilder sb = new StringBuilder();
				sb.append(INVALID_MOBILE_FORMAT).append("; ");
				ErrorInfoException errorInfo = new ErrorInfoException(INVALID_MOBILE_FORMAT, sb.toString());
				log.info("invalidMobileFormat={}", errorInfo);
				throw errorInfo;
			}
        }

        //setObjectChangeSet(existingUser, user);
        Map<String, String> objectChanges = setObjectChangeSetLocal(existingUser.orElse(null), user);

        // only copy allowed properties
        existingUser.get().setUserName(user.getUserName());
        if(user.getFirstName() != null)
        existingUser.get().setFirstName(user.getFirstName());
        if(user.getLastName() != null)
        existingUser.get().setLastName(user.getLastName());
        if(user.getEmailAddress() != null) {
        	if (user.getEmailAddress() != null && !user.getEmailAddress().equals(existingUser.get().getEmailAddress())) {
    			updateObj.put("mail", user.getEmailAddress());
    			updateObj.put("mailNickname",
    					azureManagementConfig.getMailNickName(user.getEmailAddress() != null ? user.getEmailAddress() : user.getUserName()));
    		}
        existingUser.get().setEmailAddress(user.getEmailAddress());
        }
        if(user.getHomeEmailAddress() != null) {
        	if (!user.getHomeEmailAddress().equals(existingUser.get().getHomeEmailAddress())) {
        		if(user.getHomeEmailAddress() != "")
    			otheMails.add(user.getHomeEmailAddress());
        		updateObj.put("otherMails", otheMails);
    		}
            existingUser.get().setHomeEmailAddress(user.getHomeEmailAddress());
        }
        if(user.getPhoneNumber() != null) {
        	if (user.getPhoneNumber() != null && !user.getPhoneNumber().equals(existingUser.get().getPhoneNumber())) {
    			updateObj.put("mobilePhone", user.getPhoneNumber());
    		}
            existingUser.get().setPhoneNumber(user.getPhoneNumber());
        }
        if(user.getHomePhoneNumber() != null) {
        	if (user.getHomePhoneNumber() != null && !user.getHomePhoneNumber().equals(existingUser.get().getHomePhoneNumber())) {
    			String[] businessPhones = new String[] { user.getHomePhoneNumber() };
    			updateObj.put("businessPhones", businessPhones);
    		}
        existingUser.get().setHomePhoneNumber(user.getHomePhoneNumber());
        }
        if(user.getNotes() != null)
        existingUser.get().setNotes(user.getNotes());
        if(user.getLabels() != null)
        existingUser.get().setLabels(user.getLabels());

        String preferredLanguage = (user.getPreferredLanguage() != null && !user.getPreferredLanguage().isEmpty() && Lookup.isLanguageValid(user.getPreferredLanguage()))? user.getPreferredLanguage() : null;
        if(preferredLanguage != null)
        existingUser.get().setPreferredLanguage(preferredLanguage);


        String timezone = (user.getTimeZone() != null && !user.getTimeZone().isEmpty() && Lookup.isTimeZoneValid(user.getTimeZone()))? user.getTimeZone() : null;
        if(timezone != null)
		existingUser.get().setTimeZone(timezone);
        //RBAC-1562 Start
 		if (checkTwoFactorActiveForUserAndTenant(Lookup.getTenantIdByOrganizationId(user.getOrganizationId()))) {
 			user = getChannelTypesForTwoFactorAuth(user);
             User existinguser = existingUser.orElse(null);
 			existingUser = Optional.of(getCodesIdFromChannel(user, existinguser));
 		}
 		// RBAC-1562 End
		if(user.getUseEmailAsUserid() != null)
        existingUser.get().setUseEmailAsUserid(user.getUseEmailAsUserid());
        if(!StringUtils.isEmpty(user.getIvrUserId())){
        	long ivrUserIdDuplicate = isIvrUserIdDuplicate(user.getUserId(), user.getIvrUserId());
        	if (ivrUserIdDuplicate > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(DUPLICATED_IVR_USER_ID).append("; ");
                sb.append(DUPLICATED_IVR_USER_ID).append("=").append(user.getIvrUserId());
                log.info("update; {}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_IVR_USER_ID, sb.toString());
                errorInfo.getParameters().put(DUPLICATED_IVR_USER_ID, user.getIvrUserId());
                log.info("update; usererrorInfo={}", errorInfo);
                throw errorInfo;
            }else{
            	existingUser.get().setIvrUserId(user.getIvrUserId());
            }

        	existingUser.get().setIsIVRUserLocked(user.getIsIVRUserLocked());

        }

        if(!StringUtils.isEmpty(user.getIvrPin())){
        	setIVRPin(existingUser.orElse(null), user.getIvrPin());
        }
        //existingUser.setVariables(user.getVariables());
        /* Hide Variable work
         if(user.getVariables()!=null && !user.getVariables().isEmpty()){
         for(Variable userVariable:user.getVariables()){
         if(existingUser.getVariables().contains(userVariable) == false && userVariable.getVariableId() == null){
         existingUser.getVariables().add(userVariable);
         }
         }
         existingUser.getVariables().retainAll(user.getVariables());
         }
         else{
         existingUser.setVariables(null);
         }
         **/
        Boolean loginEnabledChanged = false;
        if (user.getIsEnabled() != null) {
        	if (!user.getIsEnabled().equals(existingUser.get().getIsEnabled())) {
    			updateObj.put("accountEnabled", user.getIsEnabled());
    			loginEnabledChanged = true;
    		}
            existingUser.get().setIsEnabled(user.getIsEnabled());
        }
        boolean isSharedMadeFalse = false;
        if (user.getIsShared() != null) {
        	if(Boolean.TRUE.equals(existingUser.get().getIsShared()) && Boolean.FALSE.equals(user.getIsShared())){
        		isSharedMadeFalse = true;
        	}
            existingUser.get().setIsShared(user.getIsShared());
        }
        if (user.getIsLocked() != null) {
        	//detect user unlock operation
            if(!user.getIsLocked() && !existingUser.get().getIsLocked().equals(user.getIsLocked())){
                // added to handle for no login activity account locked re-occurrence
            	existingUser.get().setLastSuccessfulLoginTime(existingUser.get().getLoginTime()!=null?existingUser.get().getLoginTime():existingUser.get().getLastSuccessfulLoginTime());
            	existingUser.get().setLoginTime(null);
            	// set failure attempts to 0
            	existingUser.get().setConsecutiveLoginFailures(0);
            }
        	existingUser.get().setIsLocked(user.getIsLocked());
        }
        if (user.getChangePasswordFlag() != null) {
            existingUser.get().setChangePasswordFlag(user.getChangePasswordFlag());
        }

		if (isPasswordChanged && existingUser.get().getUserId().compareTo(loggedInUserId) != 0) {
			existingUser.get().setChangePasswordFlag(true);
			existingUser.get().setLastSuccessfulLoginTime(null);
		}

		if (existingUser.get().getUserId().compareTo(loggedInUserId) != 0
				&& existingUser.get().getLastSuccessfulLoginTime() == null)
			existingUser.get().setChangePasswordFlag(true);


        if(user.getGroupId() != null)
        existingUser.get().setGroupId(user.getGroupId() > 0 ? user.getGroupId() : null);

        if (existingUser.get().getRestrictions() != null && user.getRestrictions() != null) {
            existingUser.get().getRestrictions().setTimeZone(timezone);
            existingUser.get().getRestrictions().setFromDate(user.getRestrictions().getFromDate());
            existingUser.get().getRestrictions().setToDate(user.getRestrictions().getToDate());
            existingUser.get().getRestrictions().setDayOfWeek(user.getRestrictions().getDayOfWeek());
            existingUser.get().getRestrictions().setHours(user.getRestrictions().getHours());
            existingUser.get().getRestrictions().setAllowedIPs(user.getRestrictions().getAllowedIPs());
            existingUser.get().getRestrictions().setDisallowedIPs(user.getRestrictions().getDisallowedIPs());

        } else if (user.getRestrictions() != null) {

            user.getRestrictions().setRestrictionId(null);
            existingUser.get().setRestrictions(user.getRestrictions());
        }
        Date updatedDate = DateTime.now().toDate();
        if (user.getUserCalendar()!=null && user.getUserCalendar().getCalendarId()!=null && user.getUserCalendar().getCalendarId()==0) {

        	com.esq.rbac.service.calendar.domain.Calendar persistedCalendar = calendarDal.create(user.getUserCalendar(), new AuditLogInfo(loggedInUserId, clientIp));
        	user.setUserCalendar(persistedCalendar);
        }else if(user.getUserCalendar()!=null && user.getUserCalendar().getCalendarId()!=null && user.getUserCalendar().getCalendarId()!=0){
        	Calendar persistedCalendar = calendarDal.update(user.getUserCalendar(), new AuditLogInfo(loggedInUserId, clientIp));
        	user.setUserCalendar(persistedCalendar);
        }
        if(user.getUserCalendar()!=null && (user.getUserCalendar().getCreatedBy()==null || user.getUserCalendar().getCreatedOn()==null)){
        	user.getUserCalendar().setCreatedBy(loggedInUserId);
        	user.getUserCalendar().setCreatedOn(updatedDate);
        }
        if(user.getUserCalendar() != null)
        existingUser.get().setUserCalendar(user.getUserCalendar());
        if(user.getOrgCalendar()!=null && user.getOrgCalendar().getCalendarId()!=null){
        	existingUser.get().setOrgCalendar(user.getOrgCalendar());
        }
//        else{
//        	existingUser.setOrgCalendar(null);
//        }
        if (!isUserIdentityAssociationValid(user.getIdentities(), existingUser.get().getUserId())) {
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATE_USER_IDENTITY_MAPPING);
            throw errorInfo;
        }
        if (existingUser.get().getIdentities() != null) {
              if (user.getIdentities() != null) {
            	existingUser.get().getIdentities().clear();
                existingUser.get().getIdentities().addAll(user.getIdentities());
            }
        }

      //set attributes
        if(user.getAttributesData()!=null && !user.getAttributesData().isEmpty()){
        	for(AttributesData attrData: user.getAttributesData()){
        		attrData.setUser(existingUser.get());
        	}
        }
        existingUser.get().setAttributesData(user.getAttributesData());
        existingUser.get().setOrganizationId(user.getOrganizationId());
        existingUser.get().setUpdatedBy(loggedInUserId);
        existingUser.get().setUpdatedOn(updatedDate);
        if(user.getChangePassword() != null){
        	emailDal.sendAlert(user, existingUser.get().getUserId().toString(), "passwordReset");
        }

        String azureIdentity = null;
	 	List<UserIdentity> userIdentity = existingUser.get().getIdentities();
	 	if(userIdentity != null && !userIdentity.isEmpty()) {
	 		for(UserIdentity ident: userIdentity) {
	 			if(ident.getIdentityType().equalsIgnoreCase(UserIdentity.AZURE_AD_ACCOUNT))
	 				azureIdentity = ident.getIdentityId();
	 		}
	 	}

	 	log.info("Azure updateObj {}",updateObj);
	 	if(existingUser.get().getIsEnabled() && !loginEnabledChanged)
	 		azureManagementConfig.updateUser(updateObj, azureIdentity,user.getUserName());

       // User retUser = em.merge(existingUser);
            User retUser = userRepository.save(existingUser.orElse(null));
        auditLogDal.createAsyncLog(loggedInUserId, user.getUserName(), "User", "Update", objectChanges);

        if(user.getIdentities() != null && !user.getIdentities().isEmpty() || existingIdMappingFound){
	    	evictSecondLevelCacheById(user.getUserId());
	    }
        List<SSOLogoutData> returnList = null;
        boolean logoutAlreadyDone = false;
		if (deploymentUtil.isLogoutUserOnDeactivationOrLock()) {
			if ((user.getIsEnabled() == null || user.getIsEnabled().equals(Boolean.FALSE)
					|| (user.getIsLocked() != null && user.getIsLocked().equals(Boolean.TRUE)))) {
				logoutAlreadyDone = true;
				String loggedInUserName = Lookup.getUserName(loggedInUserId);
				SessionRegistryLogoutRequest request = new SessionRegistryLogoutRequest();
				request.setUserName(existingUserName);
				request.setLogoutType(LogoutRequest.LOGOUT_TYPE_USER_DEACTIVED_OR_LOCKED + loggedInUserName);
				request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_ALL);
				request.setRequestId(RBACUtil.generateLogoutRequestId());
				request.setClientIp(clientIp);
				returnList = loginService.sessionRegistryLogout(request, loggedInUserName, null).getSsoLogoutDataList();
			}
		}
		if(isUserRenamed && !logoutAlreadyDone){
				String loggedInUserName = Lookup.getUserName(loggedInUserId);
				SessionRegistryLogoutRequest request = new SessionRegistryLogoutRequest();
				request.setUserName(existingUserName);
				request.setLogoutType(LogoutRequest.LOGOUT_TYPE_USER_RENAMED + loggedInUserName);
				request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_ALL);
				request.setRequestId(RBACUtil.generateLogoutRequestId());
				request.setClientIp(clientIp);
				returnList = loginService.sessionRegistryLogout(request, loggedInUserName, null).getSsoLogoutDataList();
		}
		contactDispatcherUtil.updateContactsForUserUpdation(retUser, loggedInUserId, isSharedMadeFalse);
		/** START: Added By Fazia for maker checker **/
		 if(retUser.getIsStatus() != 1) {
	     	  makerCheckerDal.updateEntry(retUser, loggedInUserId,retUser.getUserId(),user.getIsStatus(),retUser.getMakerCheckerId(),false);
	        }
	       /** END: Added By Fazia for maker checker **/

//		 retUser.setUserName(MakerCheckerDalJpa.extractUserNameFromMKR(retUser.getUserName()));
        return new UserWithLogoutData(retUser, returnList);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int updateAllUsersForOrganization(Long organizationId,  Boolean isShared, int userId) {
    		cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);//CACHE:: CLEAR
//    		Query query = null;
    		Date updatedDate = DateTime.now().toDate();
//    	 	query = em.createNativeQuery("update rbac.userTable set isShared =?,updatedBy=?,updatedOn=? where organizationId=? ");
//    	 	query.setParameter(1, isShared);
//    	 	query.setParameter(2, userId);
//			query.setParameter(3, updatedDate);
//			query.setParameter(4, organizationId);

        Integer result = userRepository.updateAllUsersForOrganization(isShared,userId,updatedDate,organizationId);

//			Integer result = (Integer) query.executeUpdate();


			return result;

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value=CacheConfig.CALENDAR_BY_CALENDARID_CACHE, allEntries=true)
	public void deleteUserOrgCalendarMapping(Long calendarId) {
		// get list of user ids where org calendar mapping is found
//		TypedQuery<Integer> fetchQuery = em.createNamedQuery(
//				"getUserIdsForOrgCalendarDeletion", Integer.class);
//		fetchQuery.setParameter(1, calendarId);
//		List<Integer> resultList = fetchQuery.getResultList();
        List<Integer> resultList = userRepository.getUserIdsForOrgCalendarDeletion(calendarId);
		if (resultList != null && !resultList.isEmpty()) {
//			Query deleteQuery = em.createNamedQuery("removeUserOrgCalendarMapping");
//			deleteQuery.setParameter("userIds", resultList);
//			deleteQuery.executeUpdate();
            userRepository.removeUserOrgCalendarMapping(resultList);
		}
		if (resultList != null && !resultList.isEmpty()) {
			for(Integer userId: resultList){
				evictSecondLevelCacheById(userId);
			}
		}
	}

    @Transactional(propagation = Propagation.REQUIRED)
    public void evictSecondLevelCacheById(Integer userId){
    	em.getEntityManagerFactory().getCache().evict(User.class, userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void setPassword(int userId, String password) {
        User user = getById(userId);
        setPassword(user, password);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void setPassword(String userName, String password) {
        User user = getByUserName(userName);
        setPassword(user, password);
    }

    // added to support existing SHA1 passwords
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void overrideSHA1Password(User user, String password) {
        if (user == null) {
            throw new IllegalArgumentException("user==null");
        }

        String salt = User.generateSalt(PASSWORD_SALT_LENGTH);
        String hash = User.hashPassword(salt, password);

        user.setPasswordSalt(salt);
        user.setPasswordHash(hash);
        user.setPasswordSetTime(new Date());

        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUserId(user.getUserId());
        passwordHistory.setPasswordSalt(salt);
        passwordHistory.setPasswordHash(hash);
        passwordHistory.setSetTime(new Date());
      //  em.persist(passwordHistory);
        passwordHistoryRepository.save(passwordHistory);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void changePassword(String userName, String oldPassword, String newPassword) {
    	cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);//CACHE:: CLEAR
        User user = getByUserName(userName);
        if (user == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Change password; cannot find user with username: ").append(userName);
            ErrorInfoException errorInfo = new ErrorInfoException(INVALID_USERNAME, sb.toString());
            errorInfo.getParameters().put("userName", userName);
            throw errorInfo;
        }
        if (user.checkPassword(oldPassword) == false) {
            final String message = "Change password; invalid old password";
            ErrorInfoException errorInfo = new ErrorInfoException(INVALID_PASSWORD, message);
            throw errorInfo;
        }
        azureManagementConfig.updateAzurePassword(user,newPassword);
        setPassword(user, newPassword);
        user.setChangePasswordFlag(false);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void changeIVRPassword(String userName, String oldPassword,
    		String newPassword) {
    	cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);//CACHE:: CLEAR
    	 User user = getByUserName(userName);
         if (user == null) {
             StringBuilder sb = new StringBuilder();
             sb.append("Change IVR password; cannot find user with username: ").append(userName);
             ErrorInfoException errorInfo = new ErrorInfoException(INVALID_USERNAME, sb.toString());
             errorInfo.getParameters().put("userName", userName);
             throw errorInfo;
         }
         if (user.checkIVRPin(oldPassword, user.getIvrUserId()) == false) {
             final String message = "Change IVR password; invalid old password";
             ErrorInfoException errorInfo = new ErrorInfoException(INVALID_PASSWORD, message);
             throw errorInfo;
         }

         setIVRPin(user, newPassword);
    }

   /* @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public User getById(int userId) {
        return em.find(User.class, userId);
    }*/

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public User getByIVRUserId(String ivrUserId) {
        try {
//            TypedQuery<User> query = em.createNamedQuery("getUserByIVRUserId", User.class);
//            query.setParameter(
//                    "ivrUserId", ivrUserId);
//            return query.getSingleResult();
            return userRepository.getUserByIVRUserId(ivrUserId);
        } catch (NoResultException e) {
            return null;
        }
    }

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	@CachePut(value = CacheConfig.USER_BY_USERID_CACHE, unless="#result == null")
    public User getById(int userId) {
//		User user = em.find(User.class, userId);
        Optional<User> optionalUser = userRepository.findById(userId);
        User user = optionalUser.orElse(null);
		user = getUserTimeZoneByUserId(user);
		//RBAC-1562 Start
		if (user != null && checkTwoFactorActiveForUserAndTenant(Lookup.getTenantIdByOrganizationId(user.getOrganizationId()))) {
			String userTimeZone = user.getTimeZone();
			user = getChannelTypesForTwoFactorAuth(user);
			user.setTimeZone(userTimeZone);
		}
		//RBAC-1562  End
		 boolean makerCheckerCheck = Lookup.checkMakerCheckerEnabledInTenant(Lookup.getTenantIdByOrganizationId(user.getOrganizationId()));
	        if(makerCheckerCheck && deploymentUtil.getIsMakercheckerActivated()) {
	        	user.setUserName(MakerCheckerDalJpa.extractUserNameFromMKR(user.getUserName()));
	        }
		return user;
	}

	private User getUserTimeZoneByUserId(User user) {
		if (user != null && (user.getTimeZone() == null || user.getTimeZone().isEmpty())) {
			//Get OrganizationTimeZone
			String getTimezone = getOrganizationTimeZone(user.getOrganizationId());
			if (getTimezone == null)
				getTimezone = RBACUtil.UTC;
			user.setTimeZone(getTimezone);
		}
		return user;
	}

	private String getOrganizationTimeZone(Long organizationId) {
		Organization organization = organizationMaintenanceDal.getById(organizationId);
		if (organization != null && organization.getOrganizationTimeZone() != null
				&& !organization.getOrganizationTimeZone().isEmpty()) {
			return organization.getOrganizationTimeZone();
		} else {
			return null;
		}
	}

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean checkEntityPermission(int userId, Options options) {
    	Filters filters = prepareFilters(options,true);/**  Added By Fazia for maker checker to check permissions for editng inactive users in makerchecker **/
    	filters.addCondition(" u.userId = "+ userId + " ");
    	if(filters.getCount(em, "select count(u) from User u")==1){
    		return true;
    	}
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @CachePut(value = CacheConfig.USER_BY_USERNAME_CACHE, unless="#result == null")
    public User getByUserName(String userName) {
        try {
//            TypedQuery<User> query = em.createNamedQuery("getUserByUserName", User.class);
//            query.setParameter(
//                    "userName", userName);
//            return getUserTimeZoneByUserId(query.getSingleResult());
            return getUserTimeZoneByUserId(userRepository.findByUserName(userName));
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public User getByIdentity(String identityType, String identityId) {
//        TypedQuery<User> query = em.createNamedQuery("getUsersByIdentity", User.class);
//        query.setParameter("identityType", identityType);
//        query.setParameter("identityId", identityId);
//        List<User> list = query.getResultList();
        List<User> list = userRepository.getUsersByIdentity(identityType,identityId);
        if (list.size() == 1) {
            return list.get(0);
        }
        if (list.size() > 1) {
            log.warn("getByIdentity; more users match identityType={} and identityId={}",
                    identityType, identityId);
        }
        return null;
    }

    /** RBAC-2730 */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UserWithLogoutData softDeleteById(int userId, Integer loggedInUserId, String clientIp,int approveDelete,Long loggedInTenantId) {
    	cacheService.clearCache(CacheConfig.CLEAR_ALL_CACHE);//CACHE:: CLEAR
    	//temporary - added for variable deletion with appId or groupId
    	/** START: Added By Fazia for maker checker **/
    //	 User user = em.find(User.class, userId);
        Optional<User> optionalUser = userRepository.findById(userId);
        User user = optionalUser.orElse(null);
    	if(Lookup.checkMakerCheckerEnabledInTenant(loggedInTenantId) && deploymentUtil.getIsMakercheckerActivated() && user!=null && user.getIsStatus() == 1)
    	{
    		List<MakerChecker> alreadyExist = makerCheckerDal.getByEntityIdAndEntity(user.getUserId(),User.class);
    		if(user.getMakerCheckerId() != null) {
    			MakerChecker mkrckr = makerCheckerDal.getById(user.getMakerCheckerId());
    			if(mkrckr.getEntityStatus() != MakerCheckerDalJpa.REQUEST_TO_DELETE && alreadyExist == null)//not already submitted for deletion
    				makerCheckerDal.createEntry(user, User.class, loggedInUserId, "User", "Delete", userId,loggedInTenantId);
    			else if (alreadyExist != null && mkrckr.getEntityStatus() != MakerCheckerDalJpa.REQUEST_TO_DELETE)  //user entry already present for deletion
    				makerCheckerDal.updateEntry(user, loggedInUserId, user.getUserId(), mkrckr.getEntityStatus(),alreadyExist.get(0).getId(),true);
    			else if(approveDelete != 1 && alreadyExist != null && mkrckr.getEntityStatus().equals(MakerCheckerDalJpa.REQUEST_TO_DELETE))
    			{
    				String entityValue = user.getUserName() +" Sumitted By "+Lookup.getUserName(mkrckr.getCreatedBy());
    				StringBuilder sb = new StringBuilder();
		            sb.append(MKR_ENTITY_ALREADY_DELETE).append("; ");
		            sb.append(MKR_ENTITY_ALREADY_DELETE_NAME).append("=").append(entityValue);
		            log.info("delete User; {}", sb.toString());
		            ErrorInfoException errorInfo = new ErrorInfoException(MKR_ENTITY_ALREADY_DELETE, sb.toString());
		            errorInfo.getParameters().put(MKR_ENTITY_ALREADY_DELETE_NAME, entityValue);
		            log.info("delete User error={}", errorInfo);
		            throw errorInfo;
    			}
    		}
    		else
    		{
    			if(alreadyExist == null) {
    				MakerChecker makerChecker=makerCheckerDal.createEntry(user, User.class, loggedInUserId, "User", "Delete", userId,loggedInTenantId);
    				user.setMakerCheckerId(makerChecker.getId());
    			}else
    				makerCheckerDal.updateEntry(user, loggedInUserId, user.getUserId(),null, alreadyExist.get(0).getId(),true);
    		}
    		if(approveDelete != 1) {
    		/** used this setter only to show a proper message on the UI **/
    		user.setNotes(MakerCheckerDalJpa.REQUEST_TO_DELETE+"");
    		return new UserWithLogoutData(user, null);
    		}

    	}else {
            /**
             * RBAC-2730
             * User Soft Delete Operation performed
             */
    		if(user.getMakerCheckerId() != null)
    			makerCheckerDal.deleteEntryByEntityId(user.getUserId(), User.class);
    		user.setNotes("");
    	}
    	/** END: Added By Fazia for maker checker **/
        /**
         * RBAC-2730
         * User Soft Delete Operation performed
         * No require to delete from Variable
         */
    	//variableDal.deleteForCascade(userId, null, 4);
        List<SSOLogoutData> returnList = null;
        if(deploymentUtil.isLogoutUserOnDeletion()){
	        String loggedInUserName = Lookup.getUserName(loggedInUserId);
	        SessionRegistryLogoutRequest request = new SessionRegistryLogoutRequest();
	        request.setUserName(user.getUserName());
	        request.setLogoutType(LogoutRequest.LOGOUT_TYPE_USER_DELTED+loggedInUserName);
	        request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_ALL);
	        request.setRequestId(RBACUtil.generateLogoutRequestId());
	        request.setClientIp(clientIp);
	        returnList = loginService.sessionRegistryLogout(request, loggedInUserName, null).getSsoLogoutDataList();
        }
        Long userCalendarId =  user.getUserCalendar()!=null?user.getUserCalendar().getCalendarId():null;
        String userCalendarName = user.getUserCalendar()!=null?user.getUserCalendar().getName():null;

	 	/* RBAC-1259 START */
	 	userSyncDal.deleteUserFromUserSync(userId,loggedInUserId, clientIp,user.getMakerCheckerId(),user.getIsStatus());
 		/* RBAC-1259 END */
	 	makerCheckerDal.removeUserFromMakerCheckerTransactions(userId);
	 	String azureIdentity = null;
	 	List<UserIdentity> userIdentity = user.getIdentities();
	 	if(userIdentity != null && !userIdentity.isEmpty()) {
	 		for(UserIdentity ident: userIdentity) {
	 			if(ident.getIdentityType().equalsIgnoreCase(UserIdentity.AZURE_AD_ACCOUNT))
	 				azureIdentity = ident.getIdentityId();
	 		}
	 	}
	 	azureManagementConfig.deleteUser(azureIdentity);
        if(deploymentUtil.isUserSoftDelete()){
            if(!user.getIsDeleted()){
                user.setUserName(user.getUserName().concat("_"+user.getUserId()));
                user.setIsDeleted(true);
                user.setIsEnabled(false);
                user.setIsStatus(0);
//                em.merge(user);
                userRepository.save(user);
            }else {
                throw new ErrorInfoException("User Info","User already marked as deleted");
            }
        }else {
            contactDispatcherUtil.updateContactsForUserDeletion(userId, loggedInUserId);
            if(userCalendarId!=null){
                calendarDal.deleteById(userCalendarId, new AuditLogInfo(loggedInUserId, clientIp, userCalendarName, "Calendar", "Delete"));
            }
//            em.remove(user);
            userRepository.delete(user);
        }
        return new UserWithLogoutData(user, returnList);
    }

    @Deprecated
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<SSOLogoutData> deleteByUserName(String userName, Integer loggedInUserId, String clientIp) {
    	cacheService.clearCache(CacheConfig.CLEAR_ALL_CACHE);//CACHE:: CLEAR
    	//temporary - added for variable deletion with appId or groupId
    	//variableDal.deleteForCascade(userId, null, null);
    	List<SSOLogoutData> returnList = null;
    	if(deploymentUtil.isLogoutUserOnDeletion()){
 	        String loggedInUserName = Lookup.getUserName(loggedInUserId);
 	        SessionRegistryLogoutRequest request = new SessionRegistryLogoutRequest();
 	        request.setUserName(userName);
 	        request.setLogoutType(LogoutRequest.LOGOUT_TYPE_USER_DELTED+loggedInUserName);
 	        request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_ALL);
 	        request.setRequestId(RBACUtil.generateLogoutRequestId());
	        request.setClientIp(clientIp);
	        returnList = loginService.sessionRegistryLogout(request, loggedInUserName, null).getSsoLogoutDataList();
        }
//        Query query = em.createNamedQuery("deleteUserByUserName");
//        query.setParameter("userName", userName);
//        query.executeUpdate();
        userRepository.deleteUserByUserName(userName);
        contactDispatcherUtil.updateContactsForUserDeletion(Lookup.getUserId(userName), loggedInUserId);
        return returnList;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.ALL_USER_BY_USERNAME, unless="#result == null")
    public List<String> getAllUserNames() {
//        TypedQuery<String> query = em.createNamedQuery("getAllUserNames", String.class);
//        return query.getResultList();
        return userRepository.getAllUserNames();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value=CacheConfig.ALL_USER_LIST_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<User> getList(Options options) {
        Filters filters = prepareFilters(options,false);
        return filters.getList(em, User.class, "select u from User u", options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.USER_LIST_FOR_DISPATCH_CACHE,  keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<User> getListForDispatch(Options options) {
        Filters filters = prepareFilters(options,false);
        /*TypedQuery<User> query = em.createQuery("select u from User u left join Variable v on (v.userId = u.userId and v.variableName = 'CallSeq') "
        		+ " order by (CASE WHEN v.variableValue is NULL THEN 99999 ELSE CAST(v.variableValue AS INT) END )", User.class);
        return query.getResultList();*/
        Map<String, String> additionalSortMap = new LinkedHashMap<String, String>();
        additionalSortMap.putAll(SORT_COLUMNS);
        additionalSortMap.put("variableValue", deploymentUtil.getSortOrderForDispatch());
        return filters.getList(em, User.class, deploymentUtil.getQueryForDispatch(),
        		options, additionalSortMap);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.USER_COUNT,  keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR,unless="#result == null")
    public int getCount(Options options) {
        Filters filters = prepareFilters(options,false);
        return filters.getCount(em, "select count(u) from User u");
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @CachePut(value = CacheConfig.USER_APP_PERMISSION_CACHE, unless="#result == null")
    public List<String> getUserPermissions(String userName, String applicationName) {
    	List<String> result = new LinkedList<String>();
//        TypedQuery<Object[]> query = em.createNamedQuery("getUserPermissions", Object[].class);
//        query.setParameter(
//                1, userName);
//        query.setParameter(
//                2, applicationName);
//        List<Object[]> tempResult = query.getResultList();
        List<Object[]> tempResult = userRepository.getUserPermissions(userName,applicationName);
        if(tempResult!=null && !tempResult.isEmpty()){
        	for(Object[] data: tempResult){
        		result.add(data[0].toString()+"."+data[1].toString());
        	}
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @CachePut(value = CacheConfig.USER_TENANT_SCOPE_CACHE, unless="#result == null")
    public String getUserTenantScope(String userName) {
//    	TypedQuery<Object[]> query = em.createNamedQuery("getUserTenantScope", Object[].class);
//        query.setParameter(
//                1, userName);
//        query.setParameter(
//                2, RBACUtil.SCOPE_KEY_TENANT);
//        List<Object[]> list = query.getResultList();
        List<Object[]> list = userRepository.getUserTenantScope(userName,RBACUtil.SCOPE_KEY_TENANT);
        if(list!=null && !list.isEmpty()){
        	return list.get(0)[0].toString();
        }
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @CachePut(value = CacheConfig.USER_SCOPE_CACHE, unless="#result == null")
    public Map<String, String> getUserScopes(String userName, String applicationName, boolean isParsingRequired) {

//        TypedQuery<Object[]> query = em.createNamedQuery("getUserScopes", Object[].class);
//        query.setParameter(
//                1, userName);
//        query.setParameter(
//                2, applicationName);
//        List<Object[]> list = query.getResultList();

        List<Object[]> list = userRepository.getUserScopes(userName,applicationName);
        Map<String, String> result = new TreeMap<String, String>();
        for (Object[] pair : list) {
        	if(isParsingRequired){
        		if(pair[1]!=null){
        			User user = getByUserName(userName);
        			pair[1] = replaceRuntimeVariables(pair[1].toString(), user, groupDal.getById(user.getGroupId()));
        			try{
	        			if(pair[2]!=null){
	            			pair[2] = replaceRuntimeVariables(pair[2].toString(), user, groupDal.getById(user.getGroupId()));
	        			}
        			}
        			catch(ArrayIndexOutOfBoundsException e){

        			}
         		}
        	}
        	if(pair[0]!=null && pair[0].toString().equalsIgnoreCase(RBACUtil.SCOPE_KEY_TENANT) && pair[2]!=null){
        		try {
        			ArrayNode jsonArray = ScopeBuilder.tenantScopeToJson(pair[2].toString());
					if(jsonArray!=null){
						JsonNode tenantObject = new ObjectMapper().createObjectNode();
						List<Tenant> tenantList = new LinkedList<Tenant>();
								List<Long> tenantIdList = new LinkedList<Long>();
								for(int i=0; i<jsonArray.size();i++){
									tenantIdList.add(jsonArray.path(i).asLong());
									Tenant t = new Tenant();
									t.setTenantName(Lookup.getTenantNameById(jsonArray.path(i).asLong()));
									t.setTenantId(jsonArray.path(i).asLong());
									tenantList.add(t);
								}
								try {
									ObjectMapper objectMapper = new ObjectMapper();
									objectMapper.setSerializationInclusion(Include.NON_NULL);
									((ObjectNode)tenantObject).put("tenantDetails", objectMapper.writeValueAsString(tenantList));
								} catch (Exception e) {
									log.error("getUserScopes; Exception={};", e);
								}
								((ObjectNode)tenantObject).putPOJO("tenantIdList", tenantIdList);
								pair[1] = tenantObject;
							}

				} catch (Exception e) {
					log.error("getUserScopes; Exception={};", e);
				}
        	}
            result.put(pair[0].toString(), pair[1].toString());
        }
        result = checkDefaultTenantScope(result, userName);//RBAC-2619
        return result;
    }

	public Map<String, String> checkDefaultTenantScope(Map<String, String> result, String userName) {
		User user = getByUserName(userName);
		if (user != null) {
			Long userTenantId = Lookup.getTenantIdByOrganizationId(user.getOrganizationId());
			Boolean tenantScopeExists = false;
			log.trace("scopes before {}", result);
			// check for tenant scope
			if (result != null && result.containsKey(RBACUtil.SCOPE_KEY_TENANT)) {
				tenantScopeExists = true;
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					Set<Long> tenantIdList = new HashSet<Long>();
					Boolean isTenantExistsInScope = false;
					JsonNode tenantScope = objectMapper.readTree(result.get(RBACUtil.SCOPE_KEY_TENANT));
					JsonNode arrNode = tenantScope.get("tenantIdList");
					List<Tenant> tenantList = new LinkedList<Tenant>();
					if (arrNode.isArray()) {
						for (final JsonNode objNode : arrNode) {
							Long tenantId = objNode.asLong();
							tenantIdList.add(tenantId);
							if (tenantId.equals(userTenantId)) {
								isTenantExistsInScope = true;
							}else{
                                Tenant ten = new Tenant();
                                ten.setTenantId(tenantId);
                                ten.setTenantName(Lookup.getTenantNameById(tenantId));
                                tenantList.add(ten);
                            }
						}
					}
					if (isTenantExistsInScope) {
						// user tenant does not exists in the scope
						// Step: Create a new object JSON and add the user tenant in the existing scope
						JsonNode tenantObject = new ObjectMapper().createObjectNode();
						Tenant ten = new Tenant();
						ten.setTenantId(userTenantId);
						ten.setTenantName(Lookup.getTenantNameById(userTenantId));
						tenantList.add(ten);
						tenantIdList.add(userTenantId);
						objectMapper.setSerializationInclusion(Include.NON_NULL);
						((ObjectNode) tenantObject).put("tenantDetails", objectMapper.writeValueAsString(tenantList));
						((ObjectNode) tenantObject).putPOJO("tenantIdList", tenantIdList);
						result.put(RBACUtil.SCOPE_KEY_TENANT, tenantObject.toString());
					}

				} catch (Exception e) {
					log.error("Exception in checkDefaultTenantScope {}", e);
				}

			}

			if (!tenantScopeExists) {
				// no default tenant scope exists. Add the user tenant to scope
				// only to tenants other than host

				if (!Lookup.getHostTenant().getTenantId().equals(userTenantId)) {
					ObjectMapper objectMapper = new ObjectMapper();
					try {
						Set<Long> tenantIdList = new HashSet<Long>();
						tenantIdList.add(userTenantId);

						JsonNode tenantObject = new ObjectMapper().createObjectNode();
						List<Tenant> tenantList = new LinkedList<Tenant>();
						Tenant ten = new Tenant();
						ten.setTenantId(userTenantId);
						ten.setTenantName(Lookup.getTenantNameById(userTenantId));
						tenantList.add(ten);

						objectMapper.setSerializationInclusion(Include.NON_NULL);
						((ObjectNode) tenantObject).put("tenantDetails", objectMapper.writeValueAsString(tenantList));
						((ObjectNode) tenantObject).putPOJO("tenantIdList", tenantIdList);
						result.put(RBACUtil.SCOPE_KEY_TENANT, tenantObject.toString());

					} catch (Exception e) {
						log.error("Exception in no scope for tenant checkDefaultTenantScope {}", e);
					}
				}

			}

			log.trace("scopes after {}", result);

		}
		return result;

	}

	@Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @CachePut(value = CacheConfig.USER_LIST_SCOPES_CACHE, unless="#result == null")
    public Map<String, List<Integer>> getUserInListScopesDetails(String userName, String applicationName) {

//        TypedQuery<Object[]> query = em.createNamedQuery("getUserInListScopesDetails", Object[].class);
//        query.setParameter(
//                1, userName);
//        query.setParameter(
//                2, applicationName);
//        List<Object[]> list = query.getResultList();
        List<Object[]> list = userRepository.getUserInListScopesDetails(userName,applicationName);
        Map<String, List<Integer>> result = new TreeMap<String, List<Integer>>();
        if(list!=null && !list.isEmpty()){
	        for (Object[] pair : list) {
	        	try{
		        	if(pair[0]!=null){
		        		if(result.containsKey(pair[0].toString())==false){
		        			result.put(pair[0].toString(), new LinkedList<Integer>());
		        		}
		        	}
		            result.get(pair[0].toString()).add(Integer.parseInt(pair[1].toString()));
	        	}
	        	catch(Exception e){
	        		log.warn("getUserInListScopesDetails; Exception={};", e);
	        	}
	        }
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @CachePut(value = CacheConfig.USER_ROLES_CACHE, unless="#result == null")
    public List<String> getUserRoles(String userName, String applicationName) {
//        TypedQuery<String> query = em.createNamedQuery("getUserRoles", String.class);
//        query.setParameter(
//                1, userName);
//        query.setParameter(
//                2, applicationName);
//        return query.getResultList();
        return  userRepository.getUserRoles(userName,applicationName);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<PasswordHistory> getPasswordHistory(String userName, int maxEntries) {
//        TypedQuery<PasswordHistory> query = em.createNamedQuery("getPasswordHistoryByUserName", PasswordHistory.class);
//        query.setParameter(
//                "userName", userName);
//        query.setFirstResult(
//                0);
//        query.setMaxResults(maxEntries);


        Page<PasswordHistory> page = passwordHistoryRepository.getPasswordHistoryByUserName(userName, PageRequest.of(0,maxEntries));


        return page.getContent();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<PasswordHistory> getPasswordHistory(int userId, int maxEntries) {
//        TypedQuery<PasswordHistory> query = em.createNamedQuery("getPasswordHistoryByUserId", PasswordHistory.class);
//        query.setParameter(
//                "userId", userId);
//        query.setFirstResult(
//                0);
//        query.setMaxResults(maxEntries);
//
//        return query.getResultList();

        Page<PasswordHistory> page =  passwordHistoryRepository.getPasswordHistoryByUserId(userId,PageRequest.of(0,maxEntries));
        return page.getContent();
    }

    @SuppressWarnings("unchecked")
	@Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<IVRPasswordHistory> getIVRPasswordHistory(int userId, int maxEntries) {
//        TypedQuery<IVRPasswordHistory> query = em.createNamedQuery("getIVRPasswordHistoryByUserId", IVRPasswordHistory.class);
//        query.setParameter(
//                "userId", userId);
//        query.setFirstResult(
//                0);
//        query.setMaxResults(maxEntries);
//
//        List<IVRPasswordHistory> ivrPasswordHistory = query.getResultList();


   Page<IVRPasswordHistory> page = ivrPasswordHistoryRepository.getIVRPasswordHistoryByUserId(userId,PageRequest.of(0,maxEntries));

        List<IVRPasswordHistory> ivrPasswordHistory = page.getContent();
        return ivrPasswordHistory == null ? Collections.EMPTY_LIST: ivrPasswordHistory;
    }

    private Filters prepareFilters(Options options, Boolean includeInactiveUsers) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();

        if (filters!= null) {
            String userName = filters.get("userName");
            if (userName != null && userName.length() > 0) {
                result.addCondition("u.userName = :userName");
                result.addParameter("userName", userName);
            }

            String firstName = filters.get("firstName");
            if (firstName != null && firstName.length() > 0) {
                result.addCondition("u.firstName=:firstName");
                result.addParameter("firstName", firstName);
            }

            String scopeQuery = filters.get(RBACUtil.USER_SCOPE_QUERY);
            if (scopeQuery != null && scopeQuery.length() > 1) {
            	result.addCondition("("+scopeQuery+")");
            }

            String groupId = filters.get("groupId");
            if (groupId != null && groupId.length() > 0) {
            	result.addCondition(" u.groupId != :groupId ");
            	result.addParameter("groupId", Integer.valueOf(groupId));
            }

            String roleName = filters.get("roleName");
            if (roleName != null && roleName.length() > 0) {
            	result.addCondition(" u.groupId in (select gr.groupId from GroupRole gr where gr.roleId = (Select r.roleId from Role r where r.name = :roleName)) ");
            	result.addParameter("roleName", roleName);
            }

            String tenantId = filters.get("tenantId");
            if (tenantId != null && tenantId.length() > 0) {
            	result.addCondition(" u.organizationId in (select o.organizationId from Organization o where o.tenantId = :tenantId ) " );
            	result.addParameter("tenantId", Long.valueOf(tenantId));
            }

            String tenantName = filters.get("tenantName");
            if (tenantName != null && tenantName.length() > 0) {
            	Long tenantIdFrReq = Lookup.getTenantIdByName(tenantName);
            	result.addCondition(" u.organizationId in (select o.organizationId from Organization o where o.tenantId = :tenantId ) " );
            	result.addParameter("tenantId", tenantIdFrReq);
            }

            String organizationId = filters.get("organizationId");
            if (organizationId != null && organizationId.length() > 0) {
            	result.addCondition(" u.organizationId = :organizationId ");
            	result.addParameter("organizationId", Long.valueOf(organizationId));
            }

            //added if else to ensure lookup by name doesn't fail,
            //when more than one organization with same name is present across multiple tenants
            String organizationName = filters.get("organizationName");
            if (organizationName != null && organizationName.length() > 0) {
            	Long organizationIdFrReq = null;
            	if (tenantId != null && tenantId.length() > 0) {
            		organizationIdFrReq = Lookup.getOrganizationIdByNameWithTenantId(organizationName,
            				Long.valueOf(tenantId));
            		result.addCondition(" u.organizationId = :organizationId ");
                	result.addParameter("organizationId", organizationIdFrReq);
            	}
            	else if(tenantName != null && tenantName.length() > 0){
            		organizationIdFrReq = Lookup.getOrganizationIdByNameWithTenantId(organizationName,
                            Lookup.getTenantIdByName(tenantName));
            		result.addCondition(" u.organizationId = :organizationId ");
                	result.addParameter("organizationId", organizationIdFrReq);
            	}
            	else{
            		List<Long> resList = Lookup.getOrganizationIdByName(organizationName);
            		if(resList==null || resList.isEmpty()){
                		result.addCondition(" u.organizationId = :organizationIds ");
                    	result.addParameter("organizationIds", null);
            		}
            		else{
            			result.addCondition(" u.organizationId in :organizationIds ");
                    	result.addParameter("organizationIds", resList);
            		}
            	}

            }

            String isShared = filters.get("isShared");
            if (isShared != null && isShared.length() > 0) {
            	boolean isSharedBool = Boolean.valueOf(isShared);
            	result.addCondition(" u.isShared = :isSharedBool ");
            	result.addParameter("isSharedBool", isSharedBool);
            }

            String variableName = filters.get("variableName");
            if (variableName != null && variableName.length() > 0) {
            	result.addParameter("variableName", variableName);
            }

			String phoneNumber = filters.get("phoneNumber");
			if (phoneNumber != null && phoneNumber.length() > 0) {
				result.addCondition("(u.phoneNumber = :phoneNumber or u.homePhoneNumber = :phoneNumber)");
            	result.addParameter("phoneNumber", phoneNumber);
			}

            String loggedInUserName = filters.get("loggedInUserName");
			if (loggedInUserName != null && loggedInUserName.length() > 0) {
				String scope = RBACUtil.extractScopeForUser(
						this.getUserScopes(loggedInUserName, RBACUtil.RBAC_UAM_APPLICATION_NAME, true), null, false);
				if(scope!=null && !scope.isEmpty()){
					result.addCondition(" (" + scope + ") ");
				}
			}

			String emailAddress = filters.get("emailAddress");
			if (emailAddress != null && emailAddress.length() > 0) {
				result.addCondition("(u.emailAddress = :emailAddress)");
				result.addParameter("emailAddress", emailAddress);
			}

			/** START: Added By Fazia for maker checker **/
			if(includeInactiveUsers)
				result.addCondition(" u.isStatus > -1");
			else
				result.addCondition(" u.isStatus = 1 ");
			/** END: Added By Fazia for maker checker **/

			//RBAC-1921 start
			String userIdList = optionFilter.getFilters().get("userIdList");
            if (userIdList != null && userIdList.length() > 0 && !userIdList.isEmpty()) {
            	result.addCondition(" u.userId in("+userIdList+")");
            }
           //RBAC-1921 end



        }
        return result;
    }

    private void setPassword(User user, String newPassword) {
        if (user == null) {
            throw new IllegalArgumentException("user==null");
        }
        if (passwordPolicy == null) {
            throw new IllegalStateException("passwordPolicy==null");
        }

        passwordPolicy.checkNewPassword(user, newPassword);

        String salt = User.generateSalt(PASSWORD_SALT_LENGTH);
        String hash = User.hashPassword(salt, newPassword);

        user.setPasswordSalt(salt);
        user.setPasswordHash(hash);
        user.setPasswordSetTime(new Date());

        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUserId(user.getUserId());
        passwordHistory.setPasswordSalt(salt);
        passwordHistory.setPasswordHash(hash);
        passwordHistory.setSetTime(new Date());
//        em.persist(passwordHistory);
        passwordHistoryRepository.save(passwordHistory);
    }

    private void setIVRPin(User user, String newPassword) {
        if (user == null) {
            throw new IllegalArgumentException("user==null");
        }
        if (ivrPasswordPolicy == null) {
            throw new IllegalStateException("ivrPasswordPolicy==null");
        }

        ivrPasswordPolicy.checkNewPassword(user, newPassword);

        String salt = User.generateSalt(PASSWORD_SALT_LENGTH);
        String hash = User.hashPassword(salt, newPassword);

        user.setIvrPinSalt(salt);
        user.setIvrPinHash(hash);
        user.setUpdatedOn(new Date());

        IVRPasswordHistory ivrPasswordHistory = new IVRPasswordHistory();
        ivrPasswordHistory.setUserId(user.getUserId());
        ivrPasswordHistory.setIvrPasswordSalt(salt);
        ivrPasswordHistory.setIvrPasswordHash(hash);
        ivrPasswordHistory.setSetTime(new Date());
       // em.persist(ivrPasswordHistory);
        ivrPasswordHistoryRepository.save(ivrPasswordHistory);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @CachePut(value = CacheConfig.USER_TARGET_OPERATION_CACHE, unless="#result == null")
    public Map<String, List<String>> getUserTargetOperations(String userName, String applicationName) {
//        TypedQuery<Object[]> query = em.createNamedQuery("getUserTargetOperations", Object[].class);
//        query.setParameter(
//                1, userName);
//        query.setParameter(
//                2, applicationName);
//        List<Object[]> list = query.getResultList();

        List<Object[]> list = userRepository.getUserTargetOperations(userName,applicationName);
        Map<String, List<String>> result = new TreeMap<String, List<String>>();
        for (Object[] pair : list) {
            if (!result.containsKey(pair[0].toString())) {
                result.put(pair[0].toString(), new LinkedList<String>());
            }
            result.get(pair[0].toString()).add(pair[1].toString());
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @CachePut(value = CacheConfig.USER_VARIABLE_CACHE, unless="#result == null")
    public Map<String, Map<String, String>> getUserVariables(String userName, String applicationName) {
//        TypedQuery<Object[]> query = em.createNamedQuery("getUserVariables", Object[].class);
//        query.setParameter(
//                1, applicationName);
//        query.setParameter(
//                2, userName);
//        query.setParameter(
//                3, userName);
//        query.setParameter(
//                4, applicationName);
//        query.setParameter(
//                5, userName);
     //   List<Object[]> list = query.getResultList();
        List<Object[]> list = variableRepository.getUserVariables(applicationName,userName);
        Map<String, Map<String, String>> result = new TreeMap<String, Map<String, String>>();
        for (Object[] pair : list) {
            if (!result.containsKey(pair[0].toString())) {
                result.put(pair[0].toString(), new HashMap<String, String>());
            }
            else{
            	if(result.get(pair[0].toString()).get(pair[1].toString())!=null){
            		try{
            			//handling for variables containing app id to override default values
            			if(pair[3]!=null){
            				result.get(pair[0].toString()).remove(pair[1].toString());
            			}
            			else{
            				continue;
            			}
            		}
            		catch(Exception e){
            			//ignore array exceptions
            		}
            	}
            }
            result.get(pair[0].toString()).put(pair[1].toString(), pair[2]!=null?pair[2].toString():null);
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @CachePut(value = CacheConfig.USER_ATTRIBUTES_CACHE, unless="#result == null")
    public Map<String, List<Map<String,String>>> getUserAttributes(String userName) {
//        TypedQuery<Object[]> query = em.createNamedQuery("getUserAttributes", Object[].class);
//        query.setParameter(
//                1, userName);
//        query.setParameter(
//                2, userName);
//        List<Object[]> list = query.getResultList();
        List<Object[]> list = userRepository.getUserAttributes(userName);
        Map<String, List<Map<String,String>>> result = new TreeMap<String, List<Map<String,String>>>();
        for (Object[] pair : list) {
            if (!result.containsKey(pair[0].toString())) {
                result.put(pair[0].toString(), new LinkedList<Map<String,String>>());
            }
            Map<String, String> valueMap = new LinkedHashMap<String, String>();
            valueMap.put("name", pair[1].toString());
            valueMap.put("id", pair[2]!=null?pair[2].toString():null);
            valueMap.put("value", pair[3]!=null?pair[3].toString():null);
            result.get(pair[0].toString()).add(valueMap);
        }
        return result;
    }

    @Override
    public int isUserNameDuplicate(Integer userId, String userName) {
    	try {
        User user = getByUserName(userName);
        if (user != null) {
            if (userId != null && userId.intValue() > 0) {
                if (userId.equals(user.getUserId())) {
                    return 0;
                }
            }
            return 1;
        }
        // TODO Auto-generated method stub
        return 0;
    	}catch(Exception e) {
    		log.error("{}",e.getMessage());
    		return -1;
    	}
    }
    public int isEmailDuplicate(Integer userId, String emailAddress) {
    	if(emailAddress == null)
    		return 0;
    	try {
        User user = getByEmailAddress(emailAddress);
        if (user != null) {
            if (userId != null && userId.intValue() > 0) {
                if (userId.equals(user.getUserId())) {
                    return 0;
                }
            }
            return 1;
        }
        // TODO Auto-generated method stub
        return 0;
    	}catch(Exception e) {
    		log.error("{}",e.getMessage());
    		return -1;
    	}
    }

    @Override
    public int isIvrUserIdDuplicate(Integer userId, String ivrUserId){
    	User user = getByIVRUserId(ivrUserId);
        if (user != null) {
        	if (userId != null && userId.intValue() > 0) {
        		if (userId.equals(user.getUserId())) {
                    return 0;
                }
            }
            return 1;
        }
        return 0;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<User> getUsersNotAssignToGroup(Options options) {
    	Filters filters = prepareFilters(options,false);
    	filters.addCondition("u.groupId is NULL");
    	List<User> userList = new LinkedList<User>();
    	List<Object[]> userObjectList = filters.getList(em, Object[].class, "select u.userId, u.userName from User u", options, SORT_COLUMNS);
    	if(userObjectList!=null && !userObjectList.isEmpty()) {
        	for(Object[] userObject: userObjectList){
        		User user = new User();
        		user.setUserId(Integer.parseInt(userObject[0].toString()));
        		user.setUserName(userObject[1].toString());
        		userList.add(user);
        	}
        }
    	return userList;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<User> getUsersOfAnotherGroup(Options options) {
    	Filters filters = prepareFilters(options,false);
    	filters.addCondition("u.groupId is not null");
    	List<User> userList = new LinkedList<User>();
        List<Object[]> userObjectList = filters.getList(em, Object[].class, "select u.userId, u.userName, u.groupId from User u", options, SORT_COLUMNS);
        if(userObjectList!=null && !userObjectList.isEmpty()) {
        	for(Object[] userObject: userObjectList){
        		User user = new User();
        		user.setUserId(Integer.parseInt(userObject[0].toString()));
        		user.setUserName(userObject[1].toString());
        		user.setGroupId(userObject[2]==null?null:Integer.parseInt(userObject[2].toString()));
        		userList.add(user);
        	}
        }
        return userList;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.USER_SEARCH_LIST_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<User> searchList(Options options) {

    	 Filters filters = prepareFilters(options,false);


		filters.addCondition("(" + "lower(u.userName) like :q or lower(u.emailAddress) like :q "
				+ " or u.groupId IN ( select g.groupId from Group g where lower(g.name) like :q )"
				+ "or u.groupId IN ( select g.groupId from Group g where g.groupId IN ( select gr.groupId from GroupRole gr where gr.roleId IN ( select r.roleId from Role r where lower(r.name) like :q ) ) )  "
				+ ")");

    	 filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase()));


         return filters.getList(em, User.class, "select distinct u from User u ", options, SORT_COLUMNS);

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int getSearchCount(Options options) {
    	Filters filters = prepareFilters(options,false);


    	filters.addCondition("(" + "lower(u.userName) like :q or lower(u.emailAddress) like :q "
				+ " or u.groupId IN ( select g.groupId from Group g where lower(g.name) like :q )"
				+ "or u.groupId IN ( select g.groupId from Group g where g.groupId IN ( select gr.groupId from GroupRole gr where gr.roleId IN ( select r.roleId from Role r where lower(r.name) like :q ) ) )  "
				+ ")");

   	 filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
               .getSearchParam(options, SearchUtils.SEARCH_PARAM)
               .toLowerCase()));


        return filters.getCount(em, "select count(distinct u) from User u ");
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean isUserIdentityAssociationValid(List<UserIdentity> userIdentities, Integer userId) {
        if (userIdentities != null && !userIdentities.isEmpty()) {
            for (UserIdentity userIdentity : userIdentities) {
//                TypedQuery<Integer> query = em.createNamedQuery("isUserIdentityAssociationValid", Integer.class);
//                query.setParameter(1, userId);
//                query.setParameter(2, userIdentity.getIdentityType());
//                query.setParameter(3, userIdentity.getIdentityId());
//                Integer result = query.getSingleResult();
                Integer result = userRepository.isUserIdentityAssociationValid(userId,userIdentity.getIdentityType(),userIdentity.getIdentityId());
                if (result != null) {
                    log.debug("isUserIdentityAssociationValid; queryResult={}", result);
                    if (result.intValue() != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected void setObjectChangeSet(User oldUser, User newUser) {
        clearObjectChangeSet();

        putToObjectChangeSet(OBJECTCHANGES_USERID, newUser.getUserId().toString());
        putToObjectChangeSet(OBJECTNAME, oldUser.getUserName());

        checkPutToObjectChangeSet(OBJECTCHANGES_USERGROUPASSIGN, newUser.getGroupId(), oldUser.getGroupId(), Lookup.getGroupName(newUser.getGroupId()), Lookup.getGroupName(oldUser.getGroupId()));
        checkPutToObjectChangeSet(OBJECTCHANGES_ISENABLED, newUser.getIsEnabled(), oldUser.getIsEnabled(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_FIRSTNAME, newUser.getFirstName(), oldUser.getFirstName(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_LASTNAME, newUser.getLastName(), oldUser.getLastName(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_USERNAME, newUser.getUserName(), oldUser.getUserName(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_EMAIL, newUser.getEmailAddress(), oldUser.getEmailAddress(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_HOME_EMAIL, newUser.getHomeEmailAddress(), oldUser.getHomeEmailAddress(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_PHONE_NUMBER, newUser.getPhoneNumber(), oldUser.getPhoneNumber(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_HOME_PHONE_NUMBER, newUser.getHomePhoneNumber(), oldUser.getHomePhoneNumber(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_NOTES, newUser.getNotes(), oldUser.getNotes(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_ISLOCKED, newUser.getIsLocked(), oldUser.getIsLocked(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_IVR_USERID, newUser.getIvrUserId(), oldUser.getIvrUserId(), null, null);
        if((newUser.getLabels() != null && newUser.getLabels().size() > 0) || (oldUser.getLabels() != null && oldUser.getLabels().size() > 0))
			checkPutToObjectChangeSet(OBJECTCHANGES_LABELS, newUser.getLabels(), oldUser.getLabels(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_CHANGE_PASSWORD_LOGON, newUser.getChangePasswordFlag(), oldUser.getChangePasswordFlag(), null, null);

        /*User Attributes Audit LOg Entries*/
        if((newUser.getAttributesData() != null && newUser.getAttributesData().size() > 0) || (oldUser.getAttributesData() != null && oldUser.getAttributesData().size() > 0)){
        	for(AttributesData newAD: newUser.getAttributesData()){
        		for(AttributesData oldAD: oldUser.getAttributesData()){
        			if(oldAD.getAttributeDataId() == newAD.getAttributeDataId()){
        				checkPutToObjectChangeSet(Lookup.getMasterAttributeNameById(newAD.getAttributeId()), newAD.getAttributeDataValue(), oldAD.getAttributeDataValue(), null, null);
        				break;
        			}
        		}
	        }
        	for(AttributesData newAD: newUser.getAttributesData()){
	            if(!oldUser.getAttributesData().contains(newAD))
        			checkPutToObjectChangeSet(Lookup.getMasterAttributeNameById(newAD.getAttributeId()), newAD.getAttributeDataValue(), null, null, null);
	        }
        	for(AttributesData oldAD: oldUser.getAttributesData()){
	            if(!newUser.getAttributesData().contains(oldAD))
        			checkPutToObjectChangeSet(Lookup.getMasterAttributeNameById(oldAD.getAttributeId()), null, oldAD.getAttributeDataValue(), null, null);
	        }
        }

        if (newUser.getChangePassword() != null) {
            putToObjectChangeSet(OBJECTCHANGES_PASSWORD, null, "********", null, "********", null);
        }

        if (newUser.getIvrPin() != null) {
            putToObjectChangeSet(OBJECTCHANGES_IVR_PIN, null, "********", null, "********", null);
        }

        //checkMapPutToObjectChangeSet(Variable.convertSetOfVariablesToMap(newUser.getVariables()), Variable.convertSetOfVariablesToMap(oldUser.getVariables()));

        List<UserIdentity> newIdentities = newUser.getIdentities() != null ? newUser.getIdentities() : new ArrayList<UserIdentity>();
        List<UserIdentity> oldIdentities = oldUser.getIdentities() != null ? oldUser.getIdentities() : new ArrayList<UserIdentity>();

        for (UserIdentity newIdentity : newIdentities) {
            if (!oldIdentities.contains(newIdentity)) {
                putToObjectChangeSet(OBJECTCHANGES_IDENTITY, newIdentity.getIdentityType(), null, null, newIdentity.getIdentityId(), null);
            }
        }
        for (UserIdentity oldIdentity : oldIdentities) {
            if (!newIdentities.contains(oldIdentity)) {
                putToObjectChangeSet(OBJECTCHANGES_IDENTITY, oldIdentity.getIdentityType(), oldIdentity.getIdentityId(), null, null, null);
            }
        }

        checkRestrictionPutToObjectChangeSet(newUser.getRestrictions(), oldUser.getRestrictions());
    }

    protected Map<String, String> setObjectChangeSetLocal(User oldUser, User newUser) {
    	//Map<String, String> objectChanges = Collections.synchronizedMap(new TreeMap<String, String>());
    	AuditLogHelperUtil logHelperUtil =  new AuditLogHelperUtil();

    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_USERID, newUser.getUserId().toString());
    	logHelperUtil.putToObjectChangeSet( OBJECTNAME, oldUser.getUserName());

    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_USERGROUPASSIGN, newUser.getGroupId(), oldUser.getGroupId(), Lookup.getGroupName(newUser.getGroupId()), Lookup.getGroupName(oldUser.getGroupId()));
    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_ISENABLED, newUser.getIsEnabled(), oldUser.getIsEnabled(), null, null);
    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_FIRSTNAME, newUser.getFirstName(), oldUser.getFirstName(), null, null);
    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_LASTNAME, newUser.getLastName(), oldUser.getLastName(), null, null);
    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_USERNAME, newUser.getUserName(), oldUser.getUserName(), null, null);
    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_EMAIL, newUser.getEmailAddress(), oldUser.getEmailAddress(), null, null);
    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_HOME_EMAIL, newUser.getHomeEmailAddress(), oldUser.getHomeEmailAddress(), null, null);
    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_PHONE_NUMBER, newUser.getPhoneNumber(), oldUser.getPhoneNumber(), null, null);
    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_HOME_PHONE_NUMBER, newUser.getHomePhoneNumber(), oldUser.getHomePhoneNumber(), null, null);
    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_NOTES, newUser.getNotes(), oldUser.getNotes(), null, null);
    	logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_IVR_USERID, newUser.getIvrUserId(), oldUser.getIvrUserId(), null, null);
		logHelperUtil.checkPutToObjectChangeSet(
				OBJECTCHANGES_ORGANIZATION,
				newUser.getOrganizationId() != null ? Lookup
						.getOrganizationNameById(newUser.getOrganizationId()) : null,
				oldUser.getOrganizationId() != null ? Lookup
						.getOrganizationNameById(oldUser.getOrganizationId()) : null, null,
				null);
    	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_ISLOCKED, newUser.getIsLocked(), oldUser.getIsLocked(), null, null);
        if((newUser.getLabels() != null && newUser.getLabels().size() > 0) || (oldUser.getLabels() != null && oldUser.getLabels().size() > 0))
        	logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_LABELS, newUser.getLabels(), oldUser.getLabels(), null, null);
        logHelperUtil.checkPutToObjectChangeSet( OBJECTCHANGES_CHANGE_PASSWORD_LOGON, newUser.getChangePasswordFlag(), oldUser.getChangePasswordFlag(), null, null);

        /*User Attributes Audit LOg Entries*/
        if((newUser.getAttributesData() != null && newUser.getAttributesData().size() > 0) || (oldUser.getAttributesData() != null && oldUser.getAttributesData().size() > 0)){
        	for(AttributesData newAD: newUser.getAttributesData()){
        		for(AttributesData oldAD: oldUser.getAttributesData()){
        			if(oldAD.getAttributeDataId() == newAD.getAttributeDataId()){
        				logHelperUtil.checkPutToObjectChangeSet(Lookup.getMasterAttributeNameById(newAD.getAttributeId()), newAD.getAttributeDataValue(), oldAD.getAttributeDataValue(), null, null);
        				break;
        			}
        		}
	        }
        	for(AttributesData newAD: newUser.getAttributesData()){
	            if(!oldUser.getAttributesData().contains(newAD))
	            	logHelperUtil.checkPutToObjectChangeSet( Lookup.getMasterAttributeNameById(newAD.getAttributeId()), newAD.getAttributeDataValue(), null, null, null);
	        }
        	for(AttributesData oldAD: oldUser.getAttributesData()){
	            if(!newUser.getAttributesData().contains(oldAD))
	            	logHelperUtil.checkPutToObjectChangeSet( Lookup.getMasterAttributeNameById(oldAD.getAttributeId()), null, oldAD.getAttributeDataValue(), null, null);
	        }
        }

        if (newUser.getChangePassword() != null) {
        	logHelperUtil.putToObjectChangeSet( OBJECTCHANGES_PASSWORD, null, "********", null, "********", null);
        }

        if (newUser.getIvrPin() != null) {
        	logHelperUtil.putToObjectChangeSet( OBJECTCHANGES_IVR_PIN, null, "********", null, "********", null);
        }

        //checkMapPutToObjectChangeSet(Variable.convertSetOfVariablesToMap(newUser.getVariables()), Variable.convertSetOfVariablesToMap(oldUser.getVariables()));

        List<UserIdentity> newIdentities = newUser.getIdentities() != null ? newUser.getIdentities() : new ArrayList<UserIdentity>();
        List<UserIdentity> oldIdentities = oldUser.getIdentities() != null ? oldUser.getIdentities() : new ArrayList<UserIdentity>();

        for (UserIdentity newIdentity : newIdentities) {
            if (!oldIdentities.contains(newIdentity)) {
            	logHelperUtil.putToObjectChangeSet( OBJECTCHANGES_IDENTITY, newIdentity.getIdentityType(), null, null, newIdentity.getIdentityId(), null);
            }
        }
        for (UserIdentity oldIdentity : oldIdentities) {
            if (!newIdentities.contains(oldIdentity)) {
            	logHelperUtil.putToObjectChangeSet( OBJECTCHANGES_IDENTITY, oldIdentity.getIdentityType(), oldIdentity.getIdentityId(), null, null, null);
            }
        }

        logHelperUtil.checkRestrictionPutToObjectChangeSet(newUser.getRestrictions(), oldUser.getRestrictions());


        if (oldUser != null && newUser != null && checkTwoFactorActiveForUserAndTenant(Lookup.getTenantIdByOrganizationId(oldUser.getOrganizationId()))) {

        	String oldUserChannels = "";
        	if (oldUser.getTwoFactorAuthChannelType() != null && !oldUser.getTwoFactorAuthChannelType().isEmpty()) {
    			String chanelArr[] = oldUser.getTwoFactorAuthChannelType().split(",");
    			for (int i = 0; i < chanelArr.length; i++) {
    				String code = Lookup.getCodeValueById(Long.valueOf(chanelArr[i]));
    				if (code.equalsIgnoreCase(LoginResponse.CHANNEL_SMS))
    					oldUserChannels += ", "+LoginResponse.CHANNEL_SMS;
    				else if (code.equalsIgnoreCase(LoginResponse.CHANNEL_EMAIL))
    					oldUserChannels += ", "+LoginResponse.CHANNEL_EMAIL;

    			}
    		}else {
    			if(oldUser.getIsChannelTypeEmail() != null && oldUser.getIsChannelTypeEmail())
    				oldUserChannels += ", "+LoginResponse.CHANNEL_EMAIL;
    			if(oldUser.getIsChannelTypeSMS() != null && oldUser.getIsChannelTypeSMS())
    				oldUserChannels += ", "+LoginResponse.CHANNEL_SMS;
    		}

        	if(oldUserChannels != "") {
        		oldUserChannels = oldUserChannels.substring(1);
        	}

        	String newUserChannels = "";
        	if (newUser.getTwoFactorAuthChannelType() != null && !newUser.getTwoFactorAuthChannelType().isEmpty()) {
    			String chanelArr[] = newUser.getTwoFactorAuthChannelType().split(",");
    			for (int i = 0; i < chanelArr.length; i++) {
    				String code = Lookup.getCodeValueById(Long.valueOf(chanelArr[i]));
    				if (code.equalsIgnoreCase(LoginResponse.CHANNEL_SMS))
    					newUserChannels += ", "+LoginResponse.CHANNEL_SMS;
    				else if (code.equalsIgnoreCase(LoginResponse.CHANNEL_EMAIL))
    					newUserChannels += ", "+LoginResponse.CHANNEL_EMAIL;

    			}
    		}else {
    			if(newUser.getIsChannelTypeEmail() != null && newUser.getIsChannelTypeEmail())
    				newUserChannels += ", "+LoginResponse.CHANNEL_EMAIL;
    			if(newUser.getIsChannelTypeSMS() != null && newUser.getIsChannelTypeSMS())
    				newUserChannels += ", "+LoginResponse.CHANNEL_SMS;
    		}

        	if(newUserChannels != "") {
        		newUserChannels = newUserChannels.substring(1);
        	}

        	logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_2STEP_CHANNEL_TYPE, newUserChannels, oldUserChannels, null, null);
        }
        return logHelperUtil.getObjectChangeSet();
    }


    private Map<String, String> setNewObjectChangeSetLocal(User newUser) {
        //clearObjectChangeSet();
    	AuditLogHelperUtil logHelperUtil =  new AuditLogHelperUtil();
    	if(newUser.getExternalRecordId() != null && !newUser.getExternalRecordId().isEmpty()) {
    		logHelperUtil.putToObjectChangeSet("CreationType", OBJECTCHANGES_LDAPIMPORT);
    		logHelperUtil.putToObjectChangeSet("UserSyncRecordId", newUser.getExternalRecordId());
    	}
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_USERID, newUser.getUserId().toString());
    	logHelperUtil.putToObjectChangeSet(OBJECTNAME, newUser.getUserName());
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_ISENABLED, newUser.getIsEnabled().toString());
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_FIRSTNAME, newUser.getFirstName());
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_LASTNAME, newUser.getLastName());
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_USERNAME, newUser.getUserName());
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_EMAIL, newUser.getEmailAddress());
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_HOME_EMAIL, newUser.getHomeEmailAddress());
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_PHONE_NUMBER, newUser.getPhoneNumber());
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_HOME_PHONE_NUMBER, newUser.getHomePhoneNumber());
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_NOTES, newUser.getNotes());
		if (newUser.getOrganizationId() != null) {
			logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_ORGANIZATION,
                    Lookup.getOrganizationNameById(newUser.getOrganizationId()));
		}
    	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_ISLOCKED, newUser.getIsLocked().toString());
        if(newUser.getLabels() != null && newUser.getLabels().size() > 0)
        	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_LABELS, StringUtils.join(newUser.getLabels(), ','));
        logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_CHANGE_PASSWORD_LOGON, newUser.getChangePasswordFlag().toString());

        if(newUser.getAttributesData() != null && newUser.getAttributesData().size() > 0){
	        for(AttributesData newAD: newUser.getAttributesData()){
	        	logHelperUtil.putToObjectChangeSet(Lookup.getMasterAttributeNameById(newAD.getAttributeId()), newAD.getAttributeDataValue());
	        }
        }
        if (checkTwoFactorActiveForUserAndTenant(Lookup.getTenantIdByOrganizationId(newUser.getOrganizationId()))) {

        	String newUserChannels = "";
        	if (newUser.getTwoFactorAuthChannelType() != null && !newUser.getTwoFactorAuthChannelType().isEmpty()) {
    			String chanelArr[] = newUser.getTwoFactorAuthChannelType().split(",");
    			for (int i = 0; i < chanelArr.length; i++) {
    				String code = Lookup.getCodeValueById(Long.valueOf(chanelArr[i]));
    				if (code.equalsIgnoreCase(LoginResponse.CHANNEL_SMS))
    					newUserChannels += ", "+LoginResponse.CHANNEL_SMS;
    				else if (code.equalsIgnoreCase(LoginResponse.CHANNEL_EMAIL))
    					newUserChannels += ", "+LoginResponse.CHANNEL_EMAIL;

    			}
    		}else {
    			if(newUser.getIsChannelTypeEmail() != null && newUser.getIsChannelTypeEmail())
    				newUserChannels += ", "+LoginResponse.CHANNEL_EMAIL;
    			if(newUser.getIsChannelTypeSMS() != null && newUser.getIsChannelTypeSMS())
    				newUserChannels += ", "+LoginResponse.CHANNEL_SMS;
    		}

        	if(newUserChannels != "") {
        		newUserChannels = newUserChannels.substring(1);
        	}

        	logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_2STEP_CHANNEL_TYPE, newUserChannels);
        }
        return logHelperUtil.getObjectChangeSet();
    }



    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public boolean isUserAuthorizedForApp(String userName,
			String applicationName, String appKey) {
    	 if(applicationName!=null && !applicationName.isEmpty()){
    		 if(deploymentUtil.getShowAppDashboardToAllUsers()!=null && deploymentUtil.getShowAppDashboardToAllUsers().equals(Boolean.TRUE)){
    				if(deploymentUtil.getAppDashboardApplicationId()!=null){
    					Application app = applicationDal.getById(deploymentUtil.getAppDashboardApplicationId());
    					if(app!=null && app.getChildApplications()!=null && !app.getChildApplications().isEmpty()){
    						if(app.getName().equalsIgnoreCase(applicationName)){
    							return true;
    						}
    					}
    				}
    			}
    	 }
//		 TypedQuery<Integer> query = em.createNamedQuery("isUserAuthorizedForApp", Integer.class);
//         query.setParameter(1, userName);
//         query.setParameter(2, applicationName);
//         Integer result = query.getSingleResult();

        Integer result = userRepository.isUserAuthorizedForApp(userName,applicationName);
         if (result != null) {
             log.debug("isUserAuthorizedForApp; queryResult={}", result);
             if (result.intValue() == 0) {
                 return false;
             }
             return childAppPermValidatorUtil.validate(appKey, userName);
         }
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Map<String,Object>> getUserIdNames(Options options) {
		 List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
		 Filters filters = prepareFilters(options,false);
		// add default sort by name
		 OptionSort optionSort = options != null ? options
		 					.getOption(OptionSort.class) : null;
		 if (optionSort == null) {
		 	optionSort = new OptionSort(new LinkedList<String>());
		 }
		 if(optionSort.getSortProperties().isEmpty()){
		 	optionSort.getSortProperties().add("userName");
		 }
		 options = new Options(optionSort, options != null ? options
					.getOption(OptionPage.class) : null, options != null ? options
		 					.getOption(OptionFilter.class) : null);
	     List<Object[]> result = filters.getList(em, Object[].class, "select u.userId, u.userName, u.groupId from User u", options, SORT_COLUMNS);
	     if(result!=null && !result.isEmpty()){
	    	 for(Object[] obj:result){
	    		 Map<String, Object> temp = new HashMap<String, Object>();
	    		 temp.put("userId", obj[0]);
	    		 temp.put("userName", obj[1].toString());
				 temp.put("groupId", obj[2]);
	    		 returnObj.add(temp);
	    	 }
	     }
	     return returnObj;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Map<String,Object>> getUserIdNamesWithScope(Options options) {
		 List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
		 Filters filters = prepareFilters(options,false);
		// add default sort by name
		 OptionSort optionSort = options != null ? options
		 					.getOption(OptionSort.class) : null;
		 if (optionSort == null) {
		 	optionSort = new OptionSort(new LinkedList<String>());
		 }
		 if(optionSort.getSortProperties().isEmpty()){
		 	optionSort.getSortProperties().add("userName");
		 }
		 options = new Options(optionSort, options != null ? options
					.getOption(OptionPage.class) : null, options != null ? options
		 					.getOption(OptionFilter.class) : null);
	     List<Object[]> result = filters.getList(em, Object[].class, "select u.userId, u.userName, u.groupId, u.organizationId from User u", options, SORT_COLUMNS);
	     if(result!=null && !result.isEmpty()){
	    	 Map<Long, Map<String, Object>> tenantSet = new LinkedHashMap<Long, Map<String, Object>>();
	    	 for(Object[] obj:result){
	    		 if(obj[3]!=null){
		    		 Long tenantId = Lookup.getTenantIdByOrganizationId(Long.parseLong(obj[3].toString()));
		    		 if(!tenantSet.containsKey(tenantId)){
		    			 Map<String, Object> tenantMap = new LinkedHashMap<String, Object>();
		    			 tenantMap.put("tenantId", tenantId);
		    			 tenantMap.put("tenantName", tenantId);
		    			 tenantMap.put("users", new LinkedList<Map<String, Object>>());
		    			 tenantSet.put(tenantId, tenantMap);
		    		 }
		    		 Map<String, Object> temp = new HashMap<String, Object>();
		    		 temp.put("userId", obj[0]);
		    		 temp.put("userName", obj[1].toString());
		    		 ((List<Map<String, Object>>)tenantSet.get(tenantId).get("users")).add(temp);
	    		 }
	    	 }
    		 returnObj.addAll(tenantSet.values());
	     }
	     return returnObj;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	// @Cacheable(value = CacheConfig.USER_CUSTOM_INFO_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
	public List<Map<String,Object>> getCustomUserInfo(Options options) {
		 Filters filters = prepareFilters(options,false);
		 List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
		 List<Object[]> result = filters
				.getList(
						em,
						Object[].class,
						"select u.userId, u.isEnabled, u.userName, u.emailAddress, u.loginTime, u.groupId from User u",
						options, SORT_COLUMNS);
		 Map<Integer, List<Set<String>>> groupIdMap = new LinkedHashMap<Integer, List<Set<String>>>();
		if(result!=null && !result.isEmpty()){
	    	 for(Object[] obj:result){
	    		 Map<String, Object> temp = new HashMap<String, Object>();
	    		 temp.put("userId", obj[0]);
	    		 temp.put("isEnabled", obj[1]);
	    		 temp.put("userName", obj[2]);
	    		 temp.put("emailAddress", obj[3]);
	    		 if(obj[4]!=null){
	    			 temp.put("loginTime", new DateTime(obj[4], DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z"));
	    		 }
	    		 Object groupId = null;
	    		 Integer groupIdInt = null;
	    		 try{
	    			groupId = obj[5];
	    		 }
	    		 catch(ArrayIndexOutOfBoundsException e){
	    			 log.debug("getCustomUserInfo; groupId is NULL");
	    		 }
	    		 Set<String> roleList = new TreeSet<String>();
	    		 if(groupId!=null){
	    			 groupIdInt = Integer.parseInt(groupId.toString());
		    		 temp.put("groupId", groupIdInt);
	    			 temp.put("groupName", Lookup.getGroupName(groupIdInt));
	    			 if(groupIdMap.containsKey(groupIdInt)==false){
	    				 groupIdMap.put(groupIdInt, new LinkedList<Set<String>>());
	    			 }
	    			 groupIdMap.get(groupIdInt).add(roleList);
	    		 }
	    		 else{
		    		 temp.put("groupId", null);
	    		 }
    			 temp.put("roleList", roleList);

	    		 returnObj.add(temp);
	    	 }
	    	 //get roles
	    	 if(groupIdMap!=null && !groupIdMap.isEmpty()){
	    		 Map<Integer, Set<String>> groupRolesMap = groupDal.getRoleNamesInGroups(groupIdMap.keySet());
	    		 for(Integer storedGroupId: groupIdMap.keySet()){
	    			 for(Set<String> internalList:groupIdMap.get(storedGroupId)){
	    				 if(groupRolesMap.get(storedGroupId)!=null){
		    				 internalList.addAll(groupRolesMap.get(storedGroupId));
	    				 }
	    			 }
	    		 }
	    	 }
	     }
	     return returnObj;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Map<String,Object>> searchCustomUserInfo(Options options) {
		 Filters filters = prepareFilters(options,false);
		 filters.addCondition("(" + "lower(u.userName) like :q or lower(u.emailAddress) like :q "
					+ " or u.groupId IN ( select g.groupId from Group g where lower(g.name) like :q )"
					+ "or u.groupId IN ( select g.groupId from Group g where g.groupId IN ( select gr.groupId from GroupRole gr where gr.roleId IN ( select r.roleId from Role r where lower(r.name) like :q ) ) )  "
					+ ")");

	    	 filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
	                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
	                .toLowerCase()));
		 List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
		 List<Object[]> result = filters
				.getList(
						em,
						Object[].class,
						"select u.userId, u.isEnabled, u.userName, u.emailAddress, u.loginTime, u.groupId from User u",
						options, SORT_COLUMNS);
		 Map<Integer, List<Set<String>>> groupIdMap = new LinkedHashMap<Integer, List<Set<String>>>();
		if(result!=null && !result.isEmpty()){
	    	 for(Object[] obj:result){
	    		 Map<String, Object> temp = new HashMap<String, Object>();
	    		 temp.put("userId", obj[0]);
	    		 temp.put("isEnabled", obj[1]);
	    		 temp.put("userName", obj[2]);
	    		 temp.put("emailAddress", obj[3]);
	    		 if(obj[4]!=null){
	    			 temp.put("loginTime", new DateTime(obj[4], DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z"));
	    		 }
	    		 Object groupId = null;
	    		 Integer groupIdInt = null;
	    		 try{
	    			groupId = obj[5];
	    		 }
	    		 catch(ArrayIndexOutOfBoundsException e){
	    			 log.debug("searchCustomUserInfo; groupId is NULL");
	    		 }
	    		 Set<String> roleList = new TreeSet<String>();
	    		 if(groupId!=null){
	    			 groupIdInt = Integer.parseInt(groupId.toString());
		    		 temp.put("groupId", groupIdInt);
	    			 temp.put("groupName", Lookup.getGroupName(groupIdInt));
	    			 if(groupIdMap.containsKey(groupIdInt)==false){
	    				 groupIdMap.put(groupIdInt, new LinkedList<Set<String>>());
	    			 }
	    			 groupIdMap.get(groupIdInt).add(roleList);
	    		 }
	    		 else{
		    		 temp.put("groupId", null);
	    		 }
    			 temp.put("roleList", roleList);

	    		 returnObj.add(temp);
	    	 }
	    	 //get roles
	    	 if(groupIdMap!=null && !groupIdMap.isEmpty()){
	    		 Map<Integer, Set<String>> groupRolesMap = groupDal.getRoleNamesInGroups(groupIdMap.keySet());
	    		 for(Integer storedGroupId: groupIdMap.keySet()){
	    			 for(Set<String> internalList:groupIdMap.get(storedGroupId)){
	    				 if(groupRolesMap.get(storedGroupId)!=null){
		    				 internalList.addAll(groupRolesMap.get(storedGroupId));
	    				 }
	    			 }
	    		 }
	    	 }
	     }
	     return returnObj;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public String replaceRuntimeVariables(String scope, User user, Group group){
		if(scope!=null && !scope.isEmpty()){
			if(user!=null){
				if(user.getAttributesData()!=null && !user.getAttributesData().isEmpty()){
						for(AttributesData atrData:user.getAttributesData()){
							scope = scope.replaceAll(Matcher.quoteReplacement("$currentuser."+Lookup.getMasterAttributeNameById(atrData.getAttributeId())+"$"), atrData.getAttributeDataValue());
						}
				}
				if(user.getOrganizationId()!=null){
					scope = scope.replaceAll(Matcher.quoteReplacement("$currentuser.organization.organizationName$"), Lookup.getOrganizationNameById(user.getOrganizationId()));
				}

				TypedQuery<Variable> queryUserVariables = em
						.createQuery(
								"select distinct v from Variable v where v.userId =:userId",
								Variable.class);
				queryUserVariables.setParameter("userId", user.getUserId());
				List<Variable> userVariables = queryUserVariables.getResultList();
				if(userVariables!=null && !userVariables.isEmpty()){
					for(Variable var: userVariables){
						scope = scope.replaceAll(Matcher.quoteReplacement("$currentuser.variable."+var.getVariableName().toLowerCase()+"$"), var.getVariableValue());
					}
				}
			}
			if(group!=null){
				scope = scope.replaceAll(Matcher.quoteReplacement("$currentuser.group.id$"), group.getGroupId().toString());
				if(group.getAttributesData()!=null && !group.getAttributesData().isEmpty()){
					for(AttributesData atrData:group.getAttributesData()){
						scope = scope.replaceAll(Matcher.quoteReplacement("$currentgroup."+Lookup.getMasterAttributeNameById(atrData.getAttributeId())+"$"), atrData.getAttributeDataValue());
						scope = scope.replaceAll(Matcher.quoteReplacement("$currentuser.group."+Lookup.getMasterAttributeNameById(atrData.getAttributeId())+"$"), atrData.getAttributeDataValue());
					}
				}
				TypedQuery<Variable> queryGroupVariables = em
						.createQuery(
								"select distinct v from Variable v where v.groupId =:groupId",
								Variable.class);
				queryGroupVariables.setParameter("groupId", group.getGroupId());
				List<Variable> groupVariables = queryGroupVariables.getResultList();
				if(groupVariables!=null && !groupVariables.isEmpty()){
					for(Variable var: groupVariables){
						scope = scope.replaceAll(Matcher.quoteReplacement("$currentuser.group.variable."+var.getVariableName().toLowerCase()+"$"), var.getVariableValue());
					}
				}
			}
		}
		return scope;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public boolean checkTenantIdInOrgAndGroup(long organizationId, long groupId) {
//		 TypedQuery<Integer> query = em.createNamedQuery("checkTenantIdInOrgAndGroup", Integer.class);
//         query.setParameter(1, organizationId);
//         query.setParameter(2, groupId);
//         Integer result = query.getSingleResult();
        Integer result = tenantRepository.checkTenantIdInOrgAndGroup(organizationId,groupId);
         if (result != null) {
             log.debug("checkTenantIdInOrgAndGroup; queryResult={}", result);
             if (result.intValue() == 0) {
                 return false;
             }else{
            	 return true;
             }
         }

		return false;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public boolean isUserAssociatedinDispatchContact(Integer userId) {
		TypedQuery<Long> query = em.createNamedQuery(
				"isUserAssociatedinDispatchContact", Long.class);
		query.setParameter("userId", userId);
		Long count =  query.getSingleResult();
		if(count > 0)
			return true;
		else
			return false;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateConsecutiveIVRLoginFailures(int userId, int consecutiveIVRLoginFailures)
    {
//		User user = em.find(User.class, userId);
        User user = userRepository.findById(userId).get();
		user.setConsecutiveIVRLoginFailures(consecutiveIVRLoginFailures);
		lockIVRUser(user, consecutiveIVRLoginFailures);
		//em.persist(user);
        userRepository.save(user);
    }

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void lockIVRUser(User user, int consecutiveIVRLoginFailures)
    {
		if(configuration.containsKey("rbac.ivrLoginPolicy.failedAttempts") && consecutiveIVRLoginFailures >= configuration.getInt("rbac.ivrLoginPolicy.failedAttempts")){
			 user.setIsIVRUserLocked(true);
		}else if(configuration.containsKey("rbac.ivrLoginPolicy.failedAttempts") && consecutiveIVRLoginFailures == 0){
			 user.setIsIVRUserLocked(false);
		}
    }

/*Added By Pankaj for global user search get user data info*/

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Map<String,Object>> searchGlobalCustomUserInfo(String searchText,String tenantList,Integer userId,Options options) {

		if(searchText==null || searchText.trim().equals("") ||searchText.trim().length()<3) {

			 ErrorInfoException errorInfo = new ErrorInfoException("Please Enter Atleast Three Character's");
	            //errorInfo.getParameters().put("searchLength", "Please Enter Atleast Three Character's");
	            log.info("search; globalusersearcherrorInfo={}", errorInfo);
	            throw errorInfo;
		}

		List<Integer> tenantsId=new ArrayList<Integer>();
		Filters filters = new Filters();
		if(!tenantList.trim().equals("")) {

			List<String> items = Arrays.asList(tenantList.split("\\s*,\\s*"));
				for(String s : items) {tenantsId.add(Integer.valueOf(s));}

				filters.addCondition("(" + "(lower(u.userName) like :q or lower(u.emailAddress) like :q )"
						+ " and u.organizationId IN ( select o.organizationId from Organization o where o.tenantId in :tenantList )"
					    + ")");

				filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded("%"+searchText.toLowerCase()+"%"));
			     filters.addParameter("tenantList",tenantsId);

			}else {
				filters.addCondition("(" + "(lower(u.userName) like :q or lower(u.emailAddress) like :q )"
					    + ")");
				filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded("%"+searchText.toLowerCase()+"%"));
			}

		 List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
		 /*List<Object[]> result = filters
				.getList(
						em,
						Object[].class,
						"select u.userId, u.isEnabled, u.userName, u.emailAddress, u.loginTime, u.groupId  from User u ",
						options, SORT_COLUMNS);*/
		 List<Object[]> result = filters
					.getList(
							em,
							Object[].class,
							"select u.userId, u.isEnabled, u.userName, u.emailAddress, u.loginTime, u.groupId,t.tenantName  from User u left join Organization o on (u.organizationId=o.organizationId) left join Tenant t on o.tenantId=t.tenantId",
							options, SORT_COLUMNS);
		 Map<Integer, List<Set<String>>> groupIdMap = new LinkedHashMap<Integer, List<Set<String>>>();
		if(result!=null && !result.isEmpty()){
	    	 for(Object[] obj:result){
	    		 Map<String, Object> temp = new HashMap<String, Object>();
	    		// temp.put("userId", obj[0]);

	    		 temp.put("userName", obj[2]);
	    		 if(obj[3]!=null && !obj[3].equals("")) {
	    		 temp.put("emailAddress", obj[3]);
	    		 }else {

	    			 temp.put("emailAddress", "-");
	    		 }
	    		 if(obj[1]!=null && (Boolean)obj[1]==true ) {
	    			 temp.put("isEnabled", "Yes");
	    		 }else if(obj[1]!=null && (Boolean)obj[1]==false){
	    			 temp.put("isEnabled", "No");
	    		 }else {
	    			 temp.put("isEnabled", obj[1]);
	    		 }
	    		 /*if(obj[4]!=null){
	    			 temp.put("loginTime", new DateTime(obj[4], DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z"));
	    		 }else {
	    			 temp.put("loginTime","-");
	    		 }*/

	    		Object groupId = null;
	    		 Integer groupIdInt = null;
	    		 try{
	    			groupId = obj[5];
	    		 }
	    		 catch(ArrayIndexOutOfBoundsException e){
	    			 log.debug("searchCustomUserInfo; groupId is NULL");
	    		 }
	    		 Set<String> roleList = new TreeSet<String>();
	    		 if(groupId!=null){
	    			 groupIdInt = Integer.parseInt(groupId.toString());
		    		// temp.put("groupId", groupIdInt);
	    			 temp.put("groupName", Lookup.getGroupName(groupIdInt));
	    			 if(groupIdMap.containsKey(groupIdInt)==false){
	    				 groupIdMap.put(groupIdInt, new LinkedList<Set<String>>());
	    			 }
	    			 groupIdMap.get(groupIdInt).add(roleList);
	    		 }
	    		 else{
		    		// temp.put("groupId", null);
		    		 temp.put("groupName", "-");
	    		 }
    			// temp.put("roleList", roleList);

    			 if(obj[6]!=null) {

    				 temp.put("tenantName",obj[6] );
    			 }else {
    				 temp.put("tenantName","-");
    			 }

	    		 returnObj.add(temp);
	    	 }
	    	 //get roles
	    	/* if(groupIdMap!=null && !groupIdMap.isEmpty()){
	    		 Map<Integer, Set<String>> groupRolesMap = groupDal.getRoleNamesInGroups(groupIdMap.keySet());
	    		 for(Integer storedGroupId: groupIdMap.keySet()){
	    			 for(Set<String> internalList:groupIdMap.get(storedGroupId)){
	    				 if(groupRolesMap.get(storedGroupId)!=null){
		    				 internalList.addAll(groupRolesMap.get(storedGroupId));
	    				 }
	    			 }
	    		 }
	    	 }*/
	     }
	     return returnObj;
	}
	/*Added By Pankaj for global user search get user data info count*/
	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Map<String,Object>> searchGlobalCustomUserInfoCount(String searchText,String tenantList,Integer userId) {
		List<Integer> tenantsId=new ArrayList<Integer>();
		Filters filters = new Filters();
		List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
		if(!tenantList.trim().equals("")) {

			List<String> items = Arrays.asList(tenantList.split("\\s*,\\s*"));
				for(String s : items) {tenantsId.add(Integer.valueOf(s));}

				filters.addCondition("(" + "(lower(u.userName) like :q or lower(u.emailAddress) like :q)"
						+ " and u.organizationId IN ( select o.organizationId from Organization o where o.tenantId in :tenantList )"
					    + ")");

				filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded("%"+searchText.toLowerCase()+"%"));
			     filters.addParameter("tenantList",tenantsId);
			}else {

				filters.addCondition("(" + "(lower(u.userName) like :q or lower(u.emailAddress) like :q)"
					    + ")");

				filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded("%"+searchText.toLowerCase()+"%"));
			}
		 List<Object[]> result = filters
				.getList(
						em,
						Object[].class,
						"select u.userId, u.isEnabled, u.userName, u.emailAddress, u.loginTime, u.groupId from User u ",
						null, SORT_COLUMNS);
		if(result!=null && !result.isEmpty()){
			Map<String, Object> temp = new HashMap<String, Object>();
			temp.put("Count", result.size());
			returnObj.add(temp);
		}else {
			Map<String, Object> temp = new HashMap<String, Object>();
			temp.put("Count", 0);
			returnObj.add(temp);
		}
	     return returnObj;
	}

	@Override
	public Integer noOfPasswordChanged(Integer userId,Integer hour) {
		// TODO Auto-generated method stub
//		TypedQuery<Integer> query = em.createNamedQuery("noOfPasswordChanged", Integer.class);
//        query.setParameter(1, userId);
//        query.setParameter(2,(-hour));

        Integer result =null;
        try {
//        	result=query.getSingleResult();
            result = userRepository.noOfPasswordChanged(userId, hour);
        	}catch(Exception e) {
        	log.error(e.getMessage());
        }
        return result;
	}

    @Override
    public User getUserByMakerCheckerId(Long makerCheckerId) {
        // TODO Auto-generated method stub
        try {
//            TypedQuery<User> query = em.createNamedQuery("getUserMakerCheckerId", User.class);
//            query.setParameter(
//                    "makerCheckerId", makerCheckerId);
//            return query.getSingleResult();
           return  userRepository.getUserMakerCheckerId(makerCheckerId);

        } catch (NoResultException e) {
            return null;
        }
    }

	public Boolean isGroupIdAndOrganizationIdForLoggedInTenant(Long loggedInTenantId,Long organizationId,Integer groupId) {
		/**/
		OptionFilter filter=new OptionFilter();
        filter.addFilter("tenantId", loggedInTenantId.toString());
        Options options=new Options(filter);
        List<Map<String, Object>> list= groupDal.getGroupIdNames(options);
        Boolean groupIdValid=false;
        Boolean organizationIdValid=false;
        if(groupId!=null) {
        for(Map<String, Object> map:list) {
        	if(map.containsKey("groupId")) {

        		if(Integer.compare((Integer)map.get("groupId"),groupId)==0) {
        			groupIdValid=true;
            		break;
        		}
        	}
        }
        }else {
        	groupIdValid=true;
        }
        Organization organization=organizationMaintenanceDal.getById(organizationId);
        if(organization.getTenantId().compareTo(loggedInTenantId)==0) {
        	organizationIdValid=true;
        }

        if(groupIdValid && organizationIdValid) {
        	return true;
        }
        /**/
		return false;
	}

	@Override
	public List<User> getUsersByIdIn(List<Integer> userIds) {
//		TypedQuery<User> query = em.createNamedQuery("getUsersByIdIn", User.class);
//   		query.setParameter("userIdIn", userIds);
//   		List<User> users = query.getResultList();

		return userRepository.getUsersByIdIn(userIds);
	}




	 /**
   * @Desription Sets Channel Types for User
   * @param user
   * @return
   * @JIRAID RBAC-1562
   */
	@Override
  public User getChannelTypesForTwoFactorAuth(User user) {
  	if (user.getTwoFactorAuthChannelType() != null && !user.getTwoFactorAuthChannelType().isEmpty()) {
			String chanelArr[] = user.getTwoFactorAuthChannelType().split(",");
			for (int i = 0; i < chanelArr.length; i++) {
				String code = Lookup.getCodeValueById(Long.valueOf(chanelArr[i]));
				if (code.equalsIgnoreCase(LoginResponse.CHANNEL_SMS))
					user.setIsChannelTypeSMS(true);
				else if (code.equalsIgnoreCase(LoginResponse.CHANNEL_EMAIL))
					user.setIsChannelTypeEmail(true);
				else
					user.setIsChannelTypeEmail(true);
			}
		} else {
			if (user.getIsChannelTypeEmail() != null && user.getIsChannelTypeEmail()) {
				user.setIsChannelTypeEmail(true);
			} else if (user.getIsChannelTypeSMS() != null && user.getIsChannelTypeSMS()) {
				user.setIsChannelTypeSMS(true);
			} else {
				user.setIsChannelTypeEmail(true); // setting default channel to email
			}
		}
		return user;
	}

  /**
	 * @Description to Return CodeId from Channel
	 * @param user
	 * @param newUser
	 * @return
	 * @JIRAID RBAC-1562
	 */
	private User getCodesIdFromChannel(User user, User newUser) {
		List<Code> lstCodes = Lookup.codesTableAll;
		Long emailCodeId = 0L;
		Long smsCodeId = 0L;

		for (int i = 0; i < lstCodes.size(); i++) {
			if (emailCodeId == 0L || smsCodeId == 0L) {
				if (lstCodes.get(i).getCodeValue().equalsIgnoreCase(LoginResponse.CHANNEL_EMAIL)) {
					emailCodeId = lstCodes.get(i).getCodeId();
				}else if (lstCodes.get(i).getCodeValue().equalsIgnoreCase(LoginResponse.CHANNEL_SMS)) {
					smsCodeId = lstCodes.get(i).getCodeId();
				}
			} else
				break;
		}


		String chanelTypes = "";

		if (user.getIsChannelTypeEmail()) {
			chanelTypes += "," + emailCodeId;
			if (user.getEmailAddress() == null || user.getEmailAddress().isEmpty()) {
				ErrorInfoException errorInfo = new ErrorInfoException(INVALID_EMAIL,"Invalid / missing email id");
				throw errorInfo;
			}

			if (user.getEmailAddress() != null
					&& !Pattern.matches(DeploymentUtil.EMAIL_PATTERN, user.getEmailAddress())) {
				ErrorInfoException errorInfo = new ErrorInfoException(INVALID_EMAIL, "Invalid / missing email id");
				throw errorInfo;
			}
			newUser.setIsChannelTypeEmail(true);
		}

		if (user.getIsChannelTypeSMS()) {
			chanelTypes += "," + smsCodeId;
			if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
				ErrorInfoException errorInfo = new ErrorInfoException(INVALID_MOBILENO,
						"Invalid / missing mobile number");
				throw errorInfo;
			}
			if (user.getPhoneNumber() != null
					&& !Pattern.matches(DeploymentUtil.MOBILE_PATTERN, user.getPhoneNumber())) {
				ErrorInfoException errorInfo = new ErrorInfoException(INVALID_MOBILENO,
						"Invalid / missing mobile number");
				throw errorInfo;
			}
			newUser.setIsChannelTypeSMS(true);
		}
		if (!user.getIsChannelTypeEmail() && !user.getIsChannelTypeSMS()) {
			Boolean isModeSet = false;
			if (user.getUserId() == null || user.getUserId() < 1) {
				// if new user then set mode as per the details
				if (user.getEmailAddress() != null && !user.getEmailAddress().isEmpty()
						&& Pattern.matches(DeploymentUtil.EMAIL_PATTERN, user.getEmailAddress())) {
					chanelTypes += "," + emailCodeId;
					isModeSet = true;
					newUser.setIsChannelTypeEmail(true);
				}

				if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()
						&& Pattern.matches(DeploymentUtil.MOBILE_PATTERN, user.getPhoneNumber())) {
					chanelTypes += "," + smsCodeId;
					isModeSet = true;
					newUser.setIsChannelTypeSMS(true);
				}

			}

			if (!isModeSet) {
				chanelTypes += "," + emailCodeId;
				if (user.getEmailAddress() == null || user.getEmailAddress().isEmpty()) {
					ErrorInfoException errorInfo = new ErrorInfoException(INVALID_EMAIL, "Invalid / missing email id");
					throw errorInfo;
				}

				if (user.getEmailAddress() != null
						&& !Pattern.matches(DeploymentUtil.EMAIL_PATTERN, user.getEmailAddress())) {
					ErrorInfoException errorInfo = new ErrorInfoException(INVALID_EMAIL, "Invalid / missing email id");
					throw errorInfo;
				}
				newUser.setIsChannelTypeEmail(true);
			}
		}

		if (chanelTypes != "")
			chanelTypes = chanelTypes.substring(1);

		newUser.setTwoFactorAuthChannelType(chanelTypes);
		return newUser;
	}

	@Override
	public Boolean checkTwoFactorActiveForUserAndTenant(Long tenantId) {
		return (deploymentUtil.isEnableTwoFactorAuth() && Lookup.checkTwoFactorAuthEnabledInTenant(tenantId));
	}
//RBAC-1562 Ends

	@Override
    public int isUserEmailIdDuplicate(String emailAddress, String userName) {
    	List<User> user = getByEmailAddress(emailAddress, userName);
    	if (user != null && !user.isEmpty()) {
    		return 1;
    	}
    	return 0;
    }

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User getByEmailAddress(String emailAddress) {
		try {
//			TypedQuery<User> query = em.createNamedQuery("getByEmailAddress", User.class);
//			query.setParameter("emailAddress", emailAddress);
//			return query.getSingleResult();
            return userRepository.getByEmailAddress(emailAddress);
		} catch (NoResultException e) {
			return null;
		}
	}


    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<User> getByEmailAddress(String emailAddress, String userName) {
        try {
//            TypedQuery<User> query = em.createNamedQuery("getUserByEmailAddress", User.class);
//            query.setParameter(
//                    "emailAddress", emailAddress);
//            query.setParameter(
//                    "userName", userName);
//            return query.getResultList();
          return  userRepository.getUserByEmailAddressAndUserName(emailAddress,userName);
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
	public Boolean isAzureUserMgmtEnabled() {
		return azureManagementConfig.isAzureUserMgmtEnabled();
	}


	@Override
	public boolean isRevokedApplicationsForUserName(String username, ChildApplication childApplication) {
		log.debug("Checking for revoked permission of Application from Scope");
		String scopeQuery = null;
		Map<String, String> scopeMap = getUserScopes(username, RBACUtil.RBAC_UAM_APPLICATION_NAME, true);
		if (scopeMap != null) {
			scopeQuery = scopeMap.get(RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS);
		}
		if (scopeQuery != null && !scopeQuery.isEmpty()) {
			log.debug("scopeQuery {}",scopeQuery);
			OptionFilter optionFilter = new OptionFilter();
			optionFilter.addFilter(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
			Options options = new Options(optionFilter);

			List<Integer> revokedApps = applicationDal.getRevokedChildApplicationIds(options);
			log.debug("accessedAppId {}, revokedApps {}",childApplication,revokedApps);
			if (revokedApps.contains(childApplication.getChildApplicationId()))
				return true;
		}
		return false;
	}

    @Override
    public Boolean isAssertPasswordsEnabled() {
        return deploymentUtil.isAssertPasswords();
    }

}