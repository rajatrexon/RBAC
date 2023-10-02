package com.esq.rbac.service.user.vo;

import java.util.List;
import java.util.Map;

public class UserInfo {

    private Integer userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String displayName;
    private String applicationName;
    private Map<String, List<String>> permissions;
    private Map<String, String> scopes;
    private Map<String, Map<String, String>> variables;
    private String applicationContextName;
    private String timezone;
    private String preferredLanguage;
    private String lastSuccessfulLoginTimeDisplay;
    private String dateTimeDisplayFormat;


    public String getLastSuccessfulLoginTimeDisplay() {
        return lastSuccessfulLoginTimeDisplay;
    }

    public void setLastSuccessfulLoginTimeDisplay(String lastSuccessfulLoginTimeDisplay) {
        this.lastSuccessfulLoginTimeDisplay = lastSuccessfulLoginTimeDisplay;
    }

    public String getDateTimeDisplayFormat() {
        return dateTimeDisplayFormat;
    }

    public void setDateTimeDisplayFormat(String dateTimeDisplayFormat) {
        this.dateTimeDisplayFormat = dateTimeDisplayFormat;
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

    public String getApplicationContextName() {
        return applicationContextName;
    }

    public void setApplicationContextName(String applicationContextName) {
        this.applicationContextName = applicationContextName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Map<String, List<String>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, List<String>> permissions) {
        this.permissions = permissions;
    }

    public Map<String, String> getScopes() {
        return scopes;
    }

    public void setScopes(Map<String, String> scopes) {
        this.scopes = scopes;
    }

    public Map<String, Map<String, String>> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Map<String, String>> variables) {
        this.variables = variables;
    }

    public static UserInfo fromUserInfoDetails(UserInfoDetails userInfoDetails){
        if(userInfoDetails!=null){
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userInfoDetails.getUserId());
            userInfo.setUserName(userInfoDetails.getUserName());
            userInfo.setFirstName(userInfoDetails.getFirstName());
            userInfo.setLastName(userInfoDetails.getLastName());
            userInfo.setDisplayName(userInfoDetails.getDisplayName());
            userInfo.setApplicationName(userInfoDetails.getApplicationName());
            userInfo.setPermissions(userInfoDetails.getPermissions());
            userInfo.setScopes(userInfoDetails.getScopes());
            userInfo.setVariables(userInfoDetails.getVariables());
            userInfo.setTimezone(userInfoDetails.getTimezone());
            userInfo.setPreferredLanguage(userInfoDetails.getPreferredLanguage());
            userInfo.setDateTimeDisplayFormat(userInfoDetails.getDateTimeDisplayFormat());
            userInfo.setLastSuccessfulLoginTimeDisplay(userInfoDetails.getLastSuccessfulLoginTimeDisplay());
            return userInfo;
        }
        return null;
    }

    public static UserInfo fromUserInfoRBAC(UserInfoRBAC userInfoRBAC){
        if(userInfoRBAC!=null){
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userInfoRBAC.getUserId());
            userInfo.setUserName(userInfoRBAC.getUserName());
            userInfo.setFirstName(userInfoRBAC.getFirstName());
            userInfo.setLastName(userInfoRBAC.getLastName());
            userInfo.setDisplayName(userInfoRBAC.getDisplayName());
            userInfo.setApplicationName(userInfoRBAC.getApplicationName());
            userInfo.setPermissions(userInfoRBAC.getPermissions());
            userInfo.setScopes(userInfoRBAC.getScopes());
            userInfo.setVariables(userInfoRBAC.getVariables());
            return userInfo;
        }
        return null;
    }

    public static UserInfo fromUserInfoGenericV2(UserInfoGenericV2 userInfoGenericV2){
        if(userInfoGenericV2!=null){
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userInfoGenericV2.getUserId());
            userInfo.setUserName(userInfoGenericV2.getUserName());
            userInfo.setFirstName(userInfoGenericV2.getFirstName());
            userInfo.setLastName(userInfoGenericV2.getLastName());
            userInfo.setDisplayName(userInfoGenericV2.getDisplayName());
            userInfo.setApplicationName(userInfoGenericV2.getApplicationName());
            userInfo.setPermissions(userInfoGenericV2.getPermissions());
            userInfo.setScopes(userInfoGenericV2.getScopes());
            userInfo.setVariables(userInfoGenericV2.getVariables());
            return userInfo;
        }
        return null;
    }
    public static UserInfo fromUserInfoGenericV3(UserInfoGenericV3 userInfoGenericV3){
        if(userInfoGenericV3!=null){
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userInfoGenericV3.getUserId());
            userInfo.setUserName(userInfoGenericV3.getUserName());
            userInfo.setFirstName(userInfoGenericV3.getFirstName());
            userInfo.setLastName(userInfoGenericV3.getLastName());
            userInfo.setDisplayName(userInfoGenericV3.getDisplayName());
            userInfo.setApplicationName(userInfoGenericV3.getApplicationName());
            userInfo.setPermissions(userInfoGenericV3.getPermissions());
            userInfo.setScopes(userInfoGenericV3.getScopes());
            userInfo.setVariables(userInfoGenericV3.getVariables());
            userInfo.setPreferredLanguage(userInfoGenericV3.getPreferredLanguage());
            userInfo.setTimezone(userInfoGenericV3.getTimezone());
            userInfo.setDateTimeDisplayFormat(userInfoGenericV3.getDateTimeDisplayFormat());
            userInfo.setLastSuccessfulLoginTimeDisplay(userInfoGenericV3.getLastSuccessfulLoginTimeDisplay());
            return userInfo;
        }
        return null;
    }
}
