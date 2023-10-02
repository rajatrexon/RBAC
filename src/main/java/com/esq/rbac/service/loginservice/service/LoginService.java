package com.esq.rbac.service.loginservice.service;

import com.esq.rbac.service.application.applicationmaintenance.util.ApplicationDownInfo;
import com.esq.rbac.service.application.childapplication.appurldata.AppUrlData;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.loginlog.domain.LoginType;
import com.esq.rbac.service.loginservice.embedded.*;
import com.esq.rbac.service.user.domain.User;

public interface LoginService {

    AppInfoResponse getAppInfo(AppInfoRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse loginWindows(LoginRequest request);

    LoginResponse loginAuto(LoginRequest request);

    LoginResponse loginSiteMinder(LoginRequest request);

    LoginResponse loginForgeRock(LoginRequest request);

    LoginResponse loginNative(LoginRequest request);

    ServiceValidateResponse serviceValidate(ServiceValidateRequest request);

    ServiceValidateResponse validateNativeTicket(ServiceValidateRequest request);

    LoginType[] getLoginTypes();

    void changePasswordWindows(ChangePasswordRequest request);

    String[] getChangePasswordsAllowed();

    String getHomeUrlByServiceUrl(String serviceUrl);

    Boolean validateLogoutRequest(LogoutRequest request);

    ApplicationDownInfo isApplicationDown(String childApplicationName);

    void logout(LogoutRequest request);

    String getLogoutServiceUrl(String serviceUrl);

    ChildApplication getChildApplicationByServiceUrlOrAppKey(String serviceUrl, String appKey);

    ChildApplication getChildApplicationByServiceUrlAndAppKey(String serviceUrl, String appKey);

    LogoutResponse logoutNative(LogoutRequest request);

    LogoutResponse destroyNativeSession(String nativeSessionHash, ChildApplication childApplication, String clientIp, String logoutType);

    LogoutResponse sessionRegistryLogout(SessionRegistryLogoutRequest request, String loggedInUserName, Boolean forceLogoutViaAppLayer);

    void removeNativeTicket(String nativeSessionHash);

    boolean isChangePasswordValid(String sessionHash);

    AppUrlData getAppUrlDataByTicket(String ticket);

    AppUrlData getFirstAppUrlDataByUserNameAndAppKey(String userName, String appKey);

    /*added by fazia*/
	/*RBAC / IT Request RBAC-1536 and RBAC-1465
	FISRMM | Password Change Policy [FIS-RMM] */
    ChangePasswordPolicy checkChangePasswordPolicy(String userName);
    /*Added By Pankaj for BreakFix BRKFIX-450 Un-validated redirect and forwardsBRKFIX-451
       Un-validated redirect and forwards */
    Boolean isChangePasswordReturnURLExits(String returnUrl);
    LoginResponse loginNativeLDAP(LoginRequest loginRequest);

    //RBAC-1562 Starts
    LoginResponse twoFactorAuthRequest(LoginRequest twoFactorAuthRequest);
    TwoFactorAuthVO getAuthChanelDetails(String userName);
    User getByUserName(String userName);
    //RBAC-1562 Ends
    LoginResponse loginAzureActiveDirectory(LoginRequest request);

    LoginResponse validateUserName(LoginRequest request);

    LoginResponse genericHeaderLoginService(LoginRequest request);
}
