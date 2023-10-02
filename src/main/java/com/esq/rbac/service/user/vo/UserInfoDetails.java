package com.esq.rbac.service.user.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserInfoDetails {

    private Integer userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String displayName;
    private String applicationName;
    private Map<String, List<String>> permissions;
    private Map<String, String> scopes;
    private Map<String, Map<String, String>> variables;
    private String group;
    private List<String> roles;
    private Date lastSuccessfulLoginTime;
    private String timezone;
    private String preferredLanguage;
    private String lastSuccessfulLoginTimeDisplay;
    private String dateTimeDisplayFormat;
    public UserInfoDetails(){

    }

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

    public UserInfoDetails(UserInfo userInfo){
        this.userId = userInfo.getUserId();
        this.userName = userInfo.getUserName();
        this.firstName = userInfo.getFirstName();
        this.lastName = userInfo.getLastName();
        this.displayName = userInfo.getDisplayName();
        this.applicationName = userInfo.getApplicationName();
        this.permissions = userInfo.getPermissions();
        this.scopes = userInfo.getScopes();
        this.variables = userInfo.getVariables();
        this.preferredLanguage = userInfo.getPreferredLanguage();
        this.timezone = userInfo.getTimezone();
        this.lastSuccessfulLoginTimeDisplay = userInfo.getLastSuccessfulLoginTimeDisplay();
        this.dateTimeDisplayFormat = userInfo.getDateTimeDisplayFormat();
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
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
    public List<String> getRoles() {
        return roles;
    }
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @JsonSerialize()
    @JsonInclude(value= JsonInclude.Include.ALWAYS)
    public Date getLastSuccessfulLoginTime() {
        return lastSuccessfulLoginTime;
    }

    public void setLastSuccessfulLoginTime(Date lastSuccessfulLoginTime) {
        this.lastSuccessfulLoginTime = lastSuccessfulLoginTime;
    }

}
