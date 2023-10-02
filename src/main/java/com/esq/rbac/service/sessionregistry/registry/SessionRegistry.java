package com.esq.rbac.service.sessionregistry.registry;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.loginservice.embedded.LogoutResponse;
import com.esq.rbac.service.loginservice.embedded.UserSessionData;
import com.esq.rbac.service.user.vo.SSOLogoutData;
import com.esq.rbac.service.util.RBACUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SessionRegistry {

    void start();

    void stop();

    List<SSOLogoutData> login(String userName, String service, String ticket,
                              String sessionHashToLogin, String clientIp,
                              ChildApplication childApplication, Map<String, String> headerMap,
                              String deviceType, String loginType, String deviceId);

    LogoutResponse logout(final String userName, String sessionHashToLogout,
                          final String service, final String clientIp,
                          final String logoutType, ChildApplication childApplication,
                          RBACUtil.LOGOUT_ACTION logoutAction, String requestId,
                          String ticketToLogout, String appKeyForAppRestartLogout, String tagToLogout, Date cutOffDate, Boolean forceLogoutViaAppLayer);

    void loginNative(String userName, String clientIp,
                     NativeApplicationSession nativeSession,
                     ChildApplication childApplication, Map<String, String> headerMap,
                     String deviceType, String deviceId, String loginType);

    UserSessionData getUserSessionData(String sessionHash, String serviceUrl, String appKey);

    boolean isLogoutRequestDone(String requestId);

    boolean isUserSessionActive(String sessionHash);

    Integer userSessionDataCount(String ticket, String userName);
}
