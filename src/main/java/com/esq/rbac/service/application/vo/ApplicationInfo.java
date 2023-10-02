package com.esq.rbac.service.application.vo;

public class ApplicationInfo {
    private String name;
    private String description;
    private String homeUrl;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ApplicationInfo{name=").append(name);
        sb.append("; description=").append(description);
        sb.append("; homeUrl=").append(homeUrl == null ? "" : homeUrl);
        sb.append("}");
        return sb.toString();
    }
}
