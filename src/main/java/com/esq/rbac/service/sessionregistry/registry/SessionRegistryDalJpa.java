package com.esq.rbac.service.sessionregistry.registry;

import com.esq.rbac.service.application.childapplication.appurldata.AppUrlData;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.loginservice.embedded.LogoutRequest;
import com.esq.rbac.service.loginservice.embedded.LogoutResponse;
import com.esq.rbac.service.loginservice.embedded.UserSessionData;
import com.esq.rbac.service.loginservice.service.LoginService;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.sessiondata.domain.Session;
import com.esq.rbac.service.sessiondata.service.SessionService;
import com.esq.rbac.service.user.vo.SSOLogoutData;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.LogoutRunnerUtil;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.Options;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Service
@Slf4j
public class SessionRegistryDalJpa implements SessionRegistry{

    private SessionService sessionDal;
    private DeploymentUtil deploymentUtil;
    private LoginService loginService;

    private static final ExecutorService loginLogExecutorService = Executors
            .newCachedThreadPool();
    //added just to track sso logout login log entry
    private static final ConcurrentMap<String, Set<String>> ssoAppMap = new ConcurrentHashMap<String, Set<String>>();

    private int ssoLogoutConnTimeoutMs = 10000;
    private int ssoLogoutSoTimeoutMs = 10000;

    public SessionRegistryDalJpa(DeploymentUtil deploymentUtil,
                                 LoginService loginService,SessionService sessionDal){
        this.loginService=loginService;
        this.sessionDal=sessionDal;
        this.deploymentUtil=deploymentUtil;
        ssoAppMap.putAll(sessionDal.getSSOHashAppKey());
    }

    private synchronized void cleanUp() {
        log.info("SessionRegistry; cleanUp();  cleaning started; at={};",
                new Date());
        if (sessionDal != null) {
            OptionPage optionPage = new OptionPage(0, Integer.MAX_VALUE);
            OptionFilter optionFilter = new OptionFilter();
            Options options = new Options(optionPage, optionFilter);
            log.info("SessionRegistry; cleanUp(); Records Found; {}",
                    sessionDal.getAppWiseLoggedInCount(options));
            Integer nativeSessionsRemoved = sessionDal.removeAllNativeSessions(
                    LogoutRequest.LOGOUT_TYPE_RBAC_RESTART,
                    LogoutRequest.LOGOUT_TYPE_RBAC_RESTART, new Date());
            log.info("SessionRegistry; cleanUp(); Native Sessions removed; {}",
                    nativeSessionsRemoved);
        }
        log.info("SessionRegistry; cleanUp(); cleaning ended; at={};",
                new Date());
    }

    @Override
    public void start() {
        log.info("SessionRegistry; start; starting at={};", new Date());
        this.ssoLogoutConnTimeoutMs = deploymentUtil.getSsoLogoutConnTimeoutMs();
        this.ssoLogoutSoTimeoutMs = deploymentUtil.getSsoLogoutSoTimeoutMs();
        log.info("SessionRegistry; start(); ssoLogoutConnTimeoutMs={}; ssoLogoutSoTimeoutMs={};", ssoLogoutConnTimeoutMs,
                ssoLogoutSoTimeoutMs);
        log.info("SessionRegistry; start(); action={};",
                deploymentUtil.getSessionRegistryStartAction());
        if (deploymentUtil.getSessionRegistryStartAction() != null
                && !deploymentUtil.getSessionRegistryStartAction().isEmpty()) {
            if (deploymentUtil.getSessionRegistryStartAction()
                    .equalsIgnoreCase(RBACUtil.SESSION_REGISTRY_ACTION_DESTROY)) {
                cleanUp();
            }
        }
        log.info("SessionRegistry; start; started at={};", new Date());
    }

    @Override
    public void stop() {
        log.info("SessionRegistry; stop(); stopping; at={};", new Date());
        log.info("SessionRegistry; stop(); action={};",
                deploymentUtil.getSessionRegistryStopAction());
        if (deploymentUtil.getSessionRegistryStopAction() != null
                && !deploymentUtil.getSessionRegistryStopAction().isEmpty()) {
            if (deploymentUtil.getSessionRegistryStopAction().equalsIgnoreCase(
                    RBACUtil.SESSION_REGISTRY_ACTION_DESTROY)) {
                cleanUp();
            }
        }
        log.info("SessionRegistry; stop(); stopped; at={};", new Date());
    }

    @Override
    public List<SSOLogoutData> login(String userName, String service, String ticket, String sessionHashToLogin, String clientIp, ChildApplication childApplication, Map<String, String> headerMap, String deviceType, String loginType, String deviceId) {
        log.debug(
                "login; userName={}; service={}; sessionHashToLogin={}; clientIp={}; childApplication={}; ticket={};",
                userName, service, sessionHashToLogin, clientIp,
                childApplication, ticket);
        return validateAndClearSessionDataForLogin(userName, service, ticket,
                sessionHashToLogin, clientIp, childApplication, headerMap,
                deviceType, loginType, deviceId);
    }

    @Override
    public LogoutResponse logout(String userName, String sessionHashToLogout, String service, String clientIp, String logoutType, ChildApplication childApplication, RBACUtil.LOGOUT_ACTION logoutAction, String requestId, String ticketToLogout, String appKeyForAppRestartLogout, String tagToLogout, Date cutOffDate, Boolean forceLogoutViaAppLayer) {
        List<SSOLogoutData> ssoLogoutDataList = new LinkedList<SSOLogoutData>();
        Map<String, List<String>> sessionHashChildApplicationNames = new LinkedHashMap<String, List<String>>();
        LogoutResponse response = new LogoutResponse(
                sessionHashChildApplicationNames);
        log.info(
                "logout; {}; userName={}; sessionHashToLogout={}; service={}; clientIp={}; logoutType={}; childApplicationName={}; logoutAction={};"
                        + "ticketToLogout={}; appKeyForAppRestartLogout={}; tagToLogout={};",
                requestId, userName, sessionHashToLogout, service, clientIp, logoutType,
                childApplication != null ? childApplication.getChildApplicationName() : null, logoutAction,
                ticketToLogout, appKeyForAppRestartLogout, tagToLogout);
        //boolean isNonSSO = false;
        boolean isNonSSOAllowMultipleLogins = false;
        //String childApplicationName = "notSpecified-" + service;
        String appKey = "notSpecified-" + service;
        //Integer childApplicationId = -1;
        Integer appType = ChildApplication.APP_TYPE.SSO.getCode();
        if (childApplication != null) {
            //isNonSSO = ChildApplication.isNonSSO(childApplication.getAppType());
            isNonSSOAllowMultipleLogins = childApplication
                    .isNonSSOAllowMultipleLogins();
            //childApplicationName = childApplication.getChildApplicationName();
            appKey = childApplication.getAppKey();
            //childApplicationId = childApplication.getChildApplicationId();
            appType = childApplication.getAppType();
        } else {
            log.info(
                    "logout; {}; childApplication is null; userName={}; sessionHashToLogout={}; service={}; clientIp={}; logoutType={}; childApplicationName={}; logoutAction={};"
                            + "ticketToLogout={}; appKeyForAppRestartLogout={}; tagToLogout={};",
                    requestId, userName, sessionHashToLogout, service, clientIp, logoutType,
                    childApplication != null ? childApplication.getChildApplicationName() : null, logoutAction,
                    ticketToLogout, appKeyForAppRestartLogout, tagToLogout);
        }
        List<Session> sessions = new LinkedList<Session>();
        switch (logoutAction) {
            case LOGOUT_ALL: {
                if (userName != null && !userName.isEmpty()) {
                    sessions = sessionDal.getSessionsByUserName(userName);
                }
                break;
            }
            case LOGOUT_SSO: {
                if (userName != null && !userName.isEmpty()) {
                    if (!deploymentUtil.isAllowMultipleLogins()) {
                        sessions = sessionDal.getSSOSessionsByUserName(userName,
                                appType, null);
                    } else {
                        sessions = sessionDal.getSSOSessionsByUserName(userName,
                                appType, sessionHashToLogout);
                    }
                }
                else{
                    //RBAC-821
                    sessions = sessionDal.getSSOSessionsByUserName(null,
                            appType, sessionHashToLogout);
                }
                break;
            }
            case LOGOUT_NON_SSO: {
                if (userName != null && !userName.isEmpty()) {
                    if (!isNonSSOAllowMultipleLogins) {
                        sessions = sessionDal.getNonSSOSessionByUserNameAndAppKey(
                                userName, appType, appKey, null);
                    } else {
                        sessions = sessionDal.getNonSSOSessionByUserNameAndAppKey(
                                userName, appType, appKey, sessionHashToLogout);
                    }
                }
                break;
            }
            case LOGOUT_NATIVE: {
                if (sessionHashToLogout != null) {
                    Session session = sessionDal.getNativeSessionByAppKey(
                            sessionHashToLogout, appType, appKey);
                    if (session != null) {
                        sessions.add(session);
                    }
                }
                break;
            }
            case LOGOUT_SSO_TICKET:
            case LOGOUT_NON_SSO_TICKET: {
                if (userName != null && !userName.isEmpty()) {
                    if (ticketToLogout != null) {
                        Session session = sessionDal.getSessionByTicket(userName,
                                ticketToLogout);
                        if (session != null) {
                            sessions.add(session);
                        }
                    }
                }
                break;
            }
            case LOGOUT_APP_KEY: {
                if(tagToLogout!=null && !tagToLogout.isEmpty() && !tagToLogout.trim().equals("") && !tagToLogout.equals("*")){
                    sessions = sessionDal.getAllSessionsByAppKeyAndTag(appKeyForAppRestartLogout, tagToLogout);
                }
                else{
                    sessions = sessionDal.getAllSessionsByAppKey(appKeyForAppRestartLogout);
                }
                break;
            }
            case LOGOUT_SSO_RESTART: {
                if(cutOffDate==null){
                    cutOffDate = new Date();
                }
                if(tagToLogout!=null && !tagToLogout.isEmpty() && !tagToLogout.trim().equals("") && !tagToLogout.equals("*")){
                    sessions = sessionDal.getAllWebSessionsByTag(cutOffDate, tagToLogout);
                }
                else{
                    sessions = sessionDal.getAllWebSessions(cutOffDate);
                }
                break;
            }
            case LOGOUT_REFRESH_SSO: {
                if(sessionHashToLogout!=null && !sessionHashToLogout.isEmpty()){
                    sessions = sessionDal.getSSOSessionsByOnlySessionHash(sessionHashToLogout);
                }
                break;
            }
            default: {
                break;
            }
        }
        if (sessions != null && !sessions.isEmpty()) {
            ssoLogoutDataList.addAll(logoutSessionList(requestId, sessions,
                    sessionHashChildApplicationNames, clientIp, service,
                    logoutType, forceLogoutViaAppLayer));
        }
        response.setSsoLogoutDataList(ssoLogoutDataList);
        return response;
    }

    @Override
    public void loginNative(String userName, String clientIp, NativeApplicationSession nativeSession, ChildApplication childApplication, Map<String, String> headerMap, String deviceType, String deviceId, String loginType) {
        AppUrlData appUrlData = Lookup.getAppUrlDataByServiceUrlNew(nativeSession.getServiceUrl());
        createSession(nativeSession.getSessionHash(),
                childApplication.getChildApplicationId(), userName,
                nativeSession.getTicket(), clientIp,
                nativeSession.getServiceUrl(),
                ChildApplication.APP_TYPE.NATIVE.getCode(), deviceType,
                deviceId, headerMap,
                childApplication.getChildApplicationName(), loginType,
                childApplication.getAppKey(), appUrlData!=null?appUrlData.getAppUrlId():null);
    }

    @Override
    public UserSessionData getUserSessionData(String sessionHash, String serviceUrl, String appKey) {
        log.debug("Inside User Session Data");
        ChildApplication childApplication = loginService
                .getChildApplicationByServiceUrlOrAppKey(serviceUrl, appKey);
        log.debug("childApplication details {}",childApplication);
        if (childApplication.getAppType() != null
                && ChildApplication.isSSO(childApplication.getAppType())) {
            log.debug("in Application is SSO");
            return sessionToUserSessionData(sessionDal.getSsoUserData(
                    sessionHash, childApplication.getAppType()));
        } else if (childApplication.getAppType() != null
                && ChildApplication.isNonSSO(childApplication.getAppType())) {
            log.debug("in Application is Non SSO");
            return sessionToUserSessionData(sessionDal
                    .getNonSSOUserDataByAppKey(sessionHash,
                            childApplication.getAppType(),
                            childApplication.getAppKey()));
        }
        return null;
    }

    @Override
    public boolean isLogoutRequestDone(String requestId) {
        return LogoutRunnerUtil.isLogoutRequestDone(requestId);
    }

    @Override
    public boolean isUserSessionActive(String sessionHash) {
        return sessionDal.isAnySessionExistForSessionHash(sessionHash);
    }

    @Override
    public Integer userSessionDataCount(String ticket, String userName) {
        return null;
    }

    public static UserSessionData sessionToUserSessionData(Session session){
        if(session!=null){
            UserSessionData userSessionData = new UserSessionData(session.getUserName(), session.getLoginType(), session.getCreatedTime());
            userSessionData.setIdentityId(session.getIdentityId());
            if(session.getAdditionalAttributes()!=null){

            }
            log.debug("sessionToUserSessionData; userSessionData {}", userSessionData);
            return userSessionData;
        }
        return null;
    }
    private synchronized List<SSOLogoutData> logoutSessionList(String requestId,
                                                               List<Session> sessions,
                                                               Map<String, List<String>> sessionHashChildApplicationNames,
                                                               String clientIp, String serviceUrl, String logoutType, Boolean forceLogoutViaAppLayer) {
        List<SSOLogoutData> ssoLogoutDataList = new LinkedList<SSOLogoutData>();
        Set<String> ssoHashLoggedOut = new HashSet<String>();
        Map<String,Session> ssoHashToSession = new HashMap<String,Session>();
        if (sessions != null && !sessions.isEmpty()) {
            for (Session session : sessions) {
                if (sessionHashChildApplicationNames != null) {
                    if (sessionHashChildApplicationNames.containsKey(session
                            .getSessionHash()) == false) {
                        sessionHashChildApplicationNames.put(
                                session.getSessionHash(),
                                new LinkedList<String>());
                    }
                    sessionHashChildApplicationNames.get(
                            session.getSessionHash()).add(
                            session.getChildApplicationName());
                }
                if (ChildApplication.isSSO(session.getAppType())
                        || ChildApplication.isNonSSO(session.getAppType())) {
                    if(session.getServiceUrl()!=null){
                        SSOLogoutData logoutData = new SSOLogoutData();
                        logoutData.setService(session.getServiceUrl());
                        logoutData.setTicket(session.getTicket());
                        logoutData.setUrls(getLogoutUrls(session.getServiceUrl()));
                        if(deploymentUtil.isLogoutViaWebLayer() && (forceLogoutViaAppLayer==null || forceLogoutViaAppLayer.equals(Boolean.FALSE)) ){
                            ssoLogoutDataList.add(logoutData);
                        }
                        else{
                            LogoutRunnerUtil.executeLogoutCall(logoutData,
                                    requestId,
                                    ssoLogoutConnTimeoutMs, ssoLogoutSoTimeoutMs,
                                    deploymentUtil.getReplaceUrlByApplication());
                        }
                    }
                    sessionDal.deleteBySessionHashAndTicket(
                            session.getSessionHash(), session.getTicket());
                    if (ChildApplication.isNonSSO(session.getAppType())) {
                        loginLogExecutorService.execute(
                                getNewLoginLogRunnable(session.getUserName(),
                                        clientIp, serviceUrl,
                                        session.getSessionHash(),
                                        session.getLoginType(), logoutType,
                                        session.getTicket(),
                                        session.getAppKey()));
                    }
                    else if(ChildApplication.isSSO(session.getAppType())){
                        if(ssoAppMap.containsKey(session.getSessionHash()) == true){
                            ssoAppMap.get(session.getSessionHash()).remove(session.getAppKey());
                            if(ssoAppMap.get(session.getSessionHash()).isEmpty()){
                                ssoAppMap.remove(session.getSessionHash());
                                ssoHashLoggedOut.add(session.getSessionHash());
                                ssoHashToSession.put(session.getSessionHash(), session);
                            }
                        }
                    }

                } else if (ChildApplication.isNative(session.getAppType())) {
                    sessionDal.deleteBySessionHash(session.getSessionHash());
                    loginService.removeNativeTicket(session.getSessionHash());
                    loginLogExecutorService.execute(
                            getNewLoginLogRunnable(session.getUserName(),
                                    clientIp, serviceUrl,
                                    session.getSessionHash(),
                                    session.getLoginType(), logoutType,
                                    session.getTicket(), session.getAppKey()));
                }
            }
            if(ssoHashLoggedOut!=null && !ssoHashLoggedOut.isEmpty()){
                for(String ssoHash : ssoHashLoggedOut){
                    loginLogExecutorService.execute(
                            getNewLoginLogRunnable(ssoHashToSession.get(ssoHash).getUserName(),
                                    clientIp, serviceUrl,
                                    ssoHash,
                                    ssoHashToSession.get(ssoHash).getLoginType(), logoutType,
                                    null,
                                    null));
                }
            }
        }
        return ssoLogoutDataList;
    }

    public Runnable getNewLoginLogRunnable(final String userName,
                                           final String clientIp, final String service,
                                           final String sessionHash, final String loginType,
                                           final String logoutType, final String serviceTicket,
                                           final String appKey) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    LogoutRequest request = new LogoutRequest();
                    request.setUserName(userName);
                    request.setClientIP(clientIp);
                    request.setService(service);
                    request.setSessionHash(sessionHash);
                    request.setLoginType(loginType);
                    request.setLogoutType(logoutType);
                    request.setServiceTicket(serviceTicket);
                    request.setAppKey(appKey);
                    loginService.logout(request);
                } catch (Exception e) {
                    log.error("run; Exception={}", e);
                }
            }
        };
    }

    private List<String> getLogoutUrls(String service){
        List<String> urls = new LinkedList<String>();
        int pos = service.indexOf("?");
        urls.add(pos > 0 ? service.substring(0, pos) : service);
        String logoutServiceUrls = loginService
                .getLogoutServiceUrl(urls.get(0));
        if (logoutServiceUrls != null
                && !logoutServiceUrls.isEmpty()) {
            urls = Arrays
                    .asList(new String[] { logoutServiceUrls });
        }
        return urls;
    }

    private List<SSOLogoutData> validateAndClearSessionDataForLogin(String userName,
                                                                    String service, String ticket, String sessionHashToLogin,
                                                                    String clientIp, ChildApplication childApplication,
                                                                    Map<String, String> headerMap, String deviceType, String loginType,
                                                                    String deviceId) {
        List<SSOLogoutData> ssoLogoutDataList = new LinkedList<SSOLogoutData>();
        boolean isNonSSO = false;
        boolean isNonSSOAllowMultipleLogins = false;
        String childApplicationName = "notSpecified-" + service;
        String appKey = "notSpecified-" + service;
        Integer childApplicationId = -1;
        Integer appType = ChildApplication.APP_TYPE.SSO.getCode();
        if (childApplication != null) {
            isNonSSO = ChildApplication.isNonSSO(childApplication.getAppType());
            isNonSSOAllowMultipleLogins = childApplication
                    .isNonSSOAllowMultipleLogins();
            childApplicationName = childApplication.getChildApplicationName();
            appKey = childApplication.getAppKey();
            childApplicationId = childApplication.getChildApplicationId();
            appType = childApplication.getAppType();
        } else {
            log.error(
                    "validateAndClearSessionDataForLogin; childApplication is null; userName={}; service={}; sessionHashToLogin={}; clientIp={}; childApplication={};",
                    userName, service, sessionHashToLogin, clientIp,
                    childApplication);
        }
        if (!deploymentUtil.isAllowMultipleLogins() && !isNonSSO) {
            if (sessionDal
                    .isSSOSessionExistForOtherSessionHash(userName,
                            sessionHashToLogin,
                            ChildApplication.APP_TYPE.SSO.getCode())) {
                ssoLogoutDataList.addAll(logout(userName, null, service, clientIp,
                        LogoutRequest.LOGOUT_TYPE_CONCURRENT_LOGIN_NOT_ACTIVE,
                        childApplication, RBACUtil.LOGOUT_ACTION.LOGOUT_SSO,
                        RBACUtil.generateLogoutRequestId(), null, null, null, null, null).getSsoLogoutDataList());
            }
        } else if (isNonSSO && !isNonSSOAllowMultipleLogins) {
            List<Session> existingSessions = sessionDal
                    .getExistingNonSSOSessions(userName, appKey,
                            childApplicationName);
            if (existingSessions != null && !existingSessions.isEmpty()) {
                for (Session session : existingSessions) {
                    ssoLogoutDataList.addAll(logout(userName,
                            session.getSessionHash(),
                            service,
                            clientIp,
                            LogoutRequest.LOGOUT_TYPE_CONCURRENT_LOGIN_NOT_ACTIVE_CHILD_APP,
                            childApplication,
                            RBACUtil.LOGOUT_ACTION.LOGOUT_NON_SSO,
                            RBACUtil.generateLogoutRequestId(), null, null, null, null, null).getSsoLogoutDataList());
                }
            }
        }
        if(!isNonSSO){
            if(ssoAppMap.containsKey(sessionHashToLogin) == false){
                ssoAppMap.put(sessionHashToLogin, new HashSet<String>());
            }
            ssoAppMap.get(sessionHashToLogin).add(appKey);
        }
        AppUrlData appUrlData = Lookup.getAppUrlDataByServiceUrlNew(service);
        createSession(sessionHashToLogin, childApplicationId, userName, ticket,
                clientIp, service, appType, deviceType, deviceId, headerMap,
                childApplicationName, loginType, appKey, appUrlData!=null?appUrlData.getAppUrlId():null);
        return ssoLogoutDataList;
    }

    public void createSession(String sessionHashToLogin,
                              Integer childApplicationId, String userName, String ticket,
                              String clientIp, String service, Integer appType,
                              String deviceType, String deviceId, Map<String, String> headerMap,
                              String childApplicationName, String loginType, String appKey, Integer appUrlId) {

        /*added to remove the multiple instance of same session hash */
        sessionDal.isSSOSessionExistForSameSessionHash(userName,sessionHashToLogin,ChildApplication.APP_TYPE.SSO.getCode(),childApplicationId);

        Session session = new Session();
        session.setSessionHash(sessionHashToLogin);
        session.setCreatedTime(new Date());
        session.setChildApplicationId(childApplicationId);
        session.setUserId(Lookup.getUserId(userName));
        session.setTicket(ticket);
        session.setClientIp(clientIp);
        session.setServiceUrl(service);
        session.setAppType(appType);
        session.setLastActivityTime(new Date());
        session.setDeviceType(deviceType);
        session.setDeviceId(deviceId);
        session.setHeaderInfo(RBACUtil.writeMapAsString(headerMap));
        session.setUserName(userName);
        session.setChildApplicationName(childApplicationName);
        session.setLoginType(loginType);
        session.setAppKey(appKey);
        session.setAppUrlId(appUrlId);
        sessionDal.createSession(session);
    }
}
