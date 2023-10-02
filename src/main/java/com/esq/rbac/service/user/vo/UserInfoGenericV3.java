package com.esq.rbac.service.user.vo;

public class UserInfoGenericV3 extends UserInfoGenericV2{

    private String timezone;
    private String preferredLanguage;
    private String lastSuccessfulLoginTimeDisplay;
    private String dateTimeDisplayFormat;

    public String getDateTimeDisplayFormat() {
        return dateTimeDisplayFormat;
    }

    public void setDateTimeDisplayFormat(String dateTimeDisplayFormat) {
        this.dateTimeDisplayFormat = dateTimeDisplayFormat;
    }

    public String getLastSuccessfulLoginTimeDisplay() {
        return lastSuccessfulLoginTimeDisplay;
    }

    public void setLastSuccessfulLoginTimeDisplay(String lastSuccessfulLoginTimeDisplay) {
        this.lastSuccessfulLoginTimeDisplay = lastSuccessfulLoginTimeDisplay;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public static UserInfoDetails toUserInfoDetails(UserInfoGenericV3 userInfoGenericV3){
        if(userInfoGenericV3!=null){
            UserInfoDetails userInfoDetails = new UserInfoDetails(UserInfo.fromUserInfoGenericV3(userInfoGenericV3));
            userInfoDetails.setLastSuccessfulLoginTime(userInfoGenericV3.getLastSuccessfulLoginTime());
            userInfoDetails.setDateTimeDisplayFormat(userInfoGenericV3.getDateTimeDisplayFormat());
            userInfoDetails.setLastSuccessfulLoginTimeDisplay(userInfoGenericV3.getLastSuccessfulLoginTimeDisplay());
            return userInfoDetails;
        }
        return null;
    }

    public UserInfoGenericV3 toUserInfoDetailsV3(UserInfoGenericV2 userInfoGenericV2) {
        UserInfoGenericV3 userInfoGenericV3 = new UserInfoGenericV3();
        userInfoGenericV3.setApplicationName(userInfoGenericV2.getApplicationName());
        userInfoGenericV3.setDisplayName(userInfoGenericV2.getDisplayName());
        userInfoGenericV3.setFirstName(userInfoGenericV2.getFirstName());
        userInfoGenericV3.setGroup(userInfoGenericV2.getGroup());
        userInfoGenericV3.setLastName(userInfoGenericV2.getLastName());
        userInfoGenericV3.setLastSuccessfulLoginTime(userInfoGenericV2.getLastSuccessfulLoginTime());
        userInfoGenericV3.setPermissions(userInfoGenericV2.getPermissions());
        userInfoGenericV3.setRoles(userInfoGenericV2.getRoles());
        userInfoGenericV3.setScopes(userInfoGenericV2.getScopes());
        userInfoGenericV3.setUserId(userInfoGenericV2.getUserId());
        userInfoGenericV3.setUserName(userInfoGenericV2.getUserName());
        userInfoGenericV3.setVariables(userInfoGenericV2.getVariables());
        if (userInfoGenericV2.getAdditionalData() != null && !userInfoGenericV2.getAdditionalData().isEmpty()) {
            for (String key : userInfoGenericV2.getAdditionalData().keySet()) {
                userInfoGenericV3.addAdditionalData(key, userInfoGenericV2.getAdditionalData().get(key));
            }
        }

        userInfoGenericV3.setGroupId(userInfoGenericV2.getGroupId());
        userInfoGenericV3.setTenantId(userInfoGenericV2.getTenantId());
        userInfoGenericV3.setTenantName(userInfoGenericV2.getTenantName());
        userInfoGenericV3.setTenantLogoPath(userInfoGenericV2.getTenantLogoPath());
        userInfoGenericV3.setTenantType(userInfoGenericV2.getTenantType());
        userInfoGenericV3.setTenantSubType(userInfoGenericV2.getTenantSubType());
        userInfoGenericV3.setMakerCheckerEnabledInTenant(userInfoGenericV2.isMakerCheckerEnabledInTenant());
        userInfoGenericV3.setSystemMultiTenant(userInfoGenericV2.isSystemMultiTenant());
        return userInfoGenericV3;
    }

    @Override
    public String toString() {
        return "UserInfoGenericV3 [timezone=" + timezone + ", preferredLanguage=" + preferredLanguage + "]";
    }



}
