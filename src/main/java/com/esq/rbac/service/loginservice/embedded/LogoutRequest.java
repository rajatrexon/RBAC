package com.esq.rbac.service.loginservice.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    private String service;
    private String userName;
    private String clientIP;
    private Map<String, String> parameters;
    private String sessionHash;
    private String logoutType;
    private String loginType;
    private String serviceTicket;
    private String appKey;

    public static final String LOGOUT_TYPE_USER = "userLogout";
    public static final String LOGOUT_TYPE_CONCURRENT_LOGIN_NOT_ACTIVE = "concurrentLoginNotAllowedPreviousSessionTerminated";
    public static final String LOGOUT_TYPE_CHANGE_PASSWORD = "passwordChangeSessionTermination";
    public static final String LOGOUT_TYPE_CONCURRENT_LOGIN_NOT_ACTIVE_CHILD_APP = "concurrentLoginNotAllowedPreviousSessionTerminatedApplication";
    public static final String LOGOUT_TYPE_NATIVE_APP = "nativeLogout";
    public static final String LOGOUT_TYPE_SESSION_KILL = "sessionKilledBy-";
    public static final String LOGOUT_TYPE_USER_DELTED = "userDeletedBy-";
    public static final String LOGOUT_TYPE_USER_DEACTIVED_OR_LOCKED = "userDeactivedLockedBy-";
    public static final String LOGOUT_TYPE_USER_RENAMED = "userRenamedLockedBy-";
    public static final String LOGOUT_TYPE_WINDOW_CLOSED = "windowClosed";
    public static final String LOGOUT_TYPE_APP_RESTARTED = "appRestarted";
    public static final String LOGOUT_TYPE_SESSION_TIMEOUT = "sessionTimeout";
    public static final String LOGOUT_TYPE_RBAC_RESTART = "rbacStartStop";
    public static final String LOGOUT_TYPE_SSO_REFRESH = "ssoSessionRefresh";
    public static final String LOGOUT_EXT_URL_REDIRECT_FROM_CHANGE_PSWD = "redirectedToExternalUrlfromChangePasswordScreen";

}
