package com.esq.rbac.service.user.vo;

import java.util.List;

public class SSOLogoutData {
    private String service;
    private String ticket;
    private List<String> urls;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SSOLogoutData{service=").append(service);
        sb.append("; ticket=").append(ticket);
        sb.append("; urls=").append(urls!=null?urls:"");
        sb.append("}");
        return sb.toString();
    }
}
