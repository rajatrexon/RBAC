package com.esq.rbac.service.loginservice.embedded;

import com.esq.rbac.service.user.vo.SSOLogoutData;

import java.util.List;

public class LoginResponse {

    public static final String LOGIN_SUCCESSFULL = "loginSuccessful";
    public static final String LOGIN_FAILED = "loginFailed";

    //same for user present or not/incorrect password, prevents user presence detection
    public static final String INVALID_CREDENTIALS = "invalidCredentials";
    public static final String CHANGE_PASSWORD_REQUIRED = "changePasswordRequired";
    public static final String PASSWORD_EXPIRED_CHANGE_PASSWORD = "changePasswordRequiredForExpiry";
    public static final String ACCOUNT_DISSABLED = "accountDisabled";
    public static final String ACCOUNT_LOCKED = "accountLocked";
    public static final String ACCOUNT_LOCKED_INACTIVITY = "accountLockedInactivity";
    public static final String TIME_RESTRICTION = "timeRestriction";
    public static final String IP_RESTRICTION = "ipRestriction";
    public static final String MISSING_GROUP = "missingGroup";
    public static final String NOT_AUTHORIZED_APP = "notAuthorizedForApp";
    public static final String UNKNOWN_SERVICE_URL = "unknownServiceUrl";
    public static final String NO_SERVICE_URL = "noServiceUrl";
    public static final String PASSWORD_CHANGE_SUCCESSFULL = "passwordChangeSuccessful";
    public static final String IVR_PASSWORD_CHANGE_SUCCESSFULL = "ivrPasswordChangeSuccessful";
    public static final String PASSWORD_POLICY_VIOLATED = "passwordPolicyViolated";
    public static final String CHANGE_PASSWORD_POLICY_VIOLATED = "changePasswordPolicyViolated";// RBAC-1465
    public static final String INVALID_OLD_PASSWORD = "invalidOldPassword";
    public static final String USERNAME_NULL = "userNameNull";
    public static final String USERNAME_NOT_FOUND = "userNotFound";
    public static final String USER_IDENTITY_MAPPING_NOT_FOUND = "userIdentityMappingNotFound";
    public static final String AUTHENTICATION_FAILED = "authenticationFailed";
    public static final String UNRECOGNISED_APP = "unRecognisedApp";
    public static final String APP_NOT_CONFIGURED_NATIVE = "appNotConfiguredAsNative";
    public static final String APP_SWITCHED = "appSwitched";
    public static final String APP_UNDER_MAINTENANCE = "maintenance";
    public static final String IVR_LOGIN_SUCCESSFULL = "ivrLoginSuccessful";
    public static final String IVR_LOGIN_FAILED = "ivrLoginFailed";
    public static final String AZURE_AD_REQUEST_REDIRECT = "redirectedToAzureADLogin";
    public static final String AZURE_VALIDATION_FAILED = "azureValidateFailed";
    public static final String AZURE_VALIDATION_SUCCESS = "azureValidateSucess";
    public static final String CAPTCHA_INVALID="inValidCaptcha";
    public static final String CAPTCHA_VALID = "validCaptcha";
    public static final String CAPTCHA_DISABLED = "disabledCaptcha";

    //RBAC-1562 Starts
    public static final String CHANNEL_SMS = "SMS";
    public static final String CHANNEL_EMAIL = "EMAIL";
    public static final String CHANNEL_SMS_EMAIL = "BOTH";
    public static final String OTP_TIMEOUT = "OTPTimeout";
    public static final String OTP_SESSION_TIMEOUT = "OTPSessionTimeout";
    public static final String VALID_TOKEN = "validToken";
    public static final String INVALID_TOKEN = "inValidToken";
    public static final String MAX_OTP_ATTEMPTS = "maximumOTPAttempts";
    public static final String TWOFACTOR_AUTH_ENABLED = "twoFactorAuthEnabled";
    public static final String MISSING_TWO_FACTOR_CHANNELS="noChannelForTFA";
    public static final String INVALID_REQUEST_ID_FOUND = "invalidRequestId";
    public static final String OTP_SENT = "otpSent";
    public static final String OTP_SENDING_FAILED = "otpFailed";
    public static final String OTP_LOGIN_SUCCESSFULL = "OTPVerificationSuccessful";
    private Boolean twoFactorEnabled=false;

    //RBAC-1562 Ends


    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }
    public static final String USERNAME_FOUND = "usernameValid";

    private String resultCode;
    private String serviceTicket;
    private String service;
    private String redirectUrl;
    private String userName;
    private UserSessionData userSessionData;
    private Boolean isSuccess;
    private String resultMessage;
    private List<SSOLogoutData> ssoLogoutDataList;
    //RBAC-1747 Start
    private Integer enableCaptchaCount;
    private Integer captchaTimeoutInMinutes;
    private Integer loginAttemptsFailed;
    private String captchaUUID;
    //RBAC-1747 End
    private Integer audioCaptchaTimeoutInSeconds;
    public LoginResponse() {
        // empty
    }

    public LoginResponse(String resultCode, String service) {
        this.resultCode = resultCode;
        this.service = service;
    }

    public Integer getCaptchaTimeoutInMinutes() {
        return captchaTimeoutInMinutes;
    }

    public void setCaptchaTimeoutInMinutes(Integer captchaTimeoutInMinutes) {
        this.captchaTimeoutInMinutes = captchaTimeoutInMinutes;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getServiceTicket() {
        return serviceTicket;
    }

    public void setServiceTicket(String serviceTicket) {
        this.serviceTicket = serviceTicket;
    }

    public String getService() {
        return service;
    }

    //let service be set with response code
    /*public void setService(String service) {
        this.service = service;
    }*/

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UserSessionData getUserSessionData() {
        return userSessionData;
    }

    public void setUserSessionData(UserSessionData userSessionData) {
        this.userSessionData = userSessionData;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public List<SSOLogoutData> getSsoLogoutDataList() {
        return ssoLogoutDataList;
    }

    public void setSsoLogoutDataList(List<SSOLogoutData> ssoLogoutDataList) {
        this.ssoLogoutDataList = ssoLogoutDataList;
    }

    public String getCaptchaUUID() {
        return captchaUUID;
    }

    public void setCaptchaUUID(String captchaUUID) {
        this.captchaUUID = captchaUUID;
    }

    public Integer getEnableCaptchaCount() {
        return enableCaptchaCount;
    }

    public void setEnableCaptchaCount(Integer enableCaptchaCount) {
        this.enableCaptchaCount = enableCaptchaCount;
    }

    public Integer getLoginAttemptsFailed() {
        return loginAttemptsFailed;
    }

    public void setLoginAttemptsFailed(Integer loginAttemptsFailed) {
        this.loginAttemptsFailed = loginAttemptsFailed;
    }
    public Integer getAudioCaptchaTimeoutInSeconds() {
        return audioCaptchaTimeoutInSeconds;
    }

    public void setAudioCaptchaTimeoutInSeconds(Integer audioCaptchaTimeoutInSeconds) {
        this.audioCaptchaTimeoutInSeconds = audioCaptchaTimeoutInSeconds;
    }
}

