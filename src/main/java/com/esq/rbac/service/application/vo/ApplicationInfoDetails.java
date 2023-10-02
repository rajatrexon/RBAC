package com.esq.rbac.service.application.vo;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApplicationInfoDetails {
    private String name;
    private String description;
    private String homeUrl;
    private Map<String,String> additionalData = new LinkedHashMap<String, String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }

    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, String> additionalData) {
        this.additionalData = additionalData;
    }

    public void addAdditionalData(String key, String value) {
        this.additionalData.put(key, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ApplicationInfo{name=").append(name);
        sb.append("; description=").append(description);
        sb.append("; homeUrl=").append(homeUrl == null ? "" : homeUrl);
        sb.append("; additionalData=").append(additionalData == null ? "" : additionalData);
        sb.append("}");
        return sb.toString();
    }
}
