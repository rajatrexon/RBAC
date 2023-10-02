package com.esq.rbac.service.application.vo;

public class SwitcherApplicationInfo {

    private String name;
    private String homeUrl;
    private String applicationId;
    private String appLogoImageUrl;
    private String appKey;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHomeUrl() {
        return homeUrl;
    }
    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    public String getAppLogoImageUrl() {
        return appLogoImageUrl;
    }
    public void setAppLogoImageUrl(String appLogoImageUrl) {
        this.appLogoImageUrl = appLogoImageUrl;
    }
    public String getAppKey() {
        return appKey;
    }
    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

}
