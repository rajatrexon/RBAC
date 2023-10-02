package com.esq.rbac.service.contact.embedded;


import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Date;

@XmlRootElement
public class AuditLogJsonV3 {

    private Integer auditLogId;
    private Date createdTime;
    private String appKey;
    private String targetName;
    private String operationName;
    private Integer userId;
    private String queryField1;
    private String queryField2;
    private Boolean isAlertable;
    private Object properties;

    public Integer getAuditLogId() {
        return auditLogId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void setAuditLogId(Integer auditLogId) {
        this.auditLogId = auditLogId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getQueryField1() {
        return queryField1;
    }

    public void setQueryField1(String queryField1) {
        this.queryField1 = queryField1;
    }

    public String getQueryField2() {
        return queryField2;
    }

    public void setQueryField2(String queryField2) {
        this.queryField2 = queryField2;
    }

    public Boolean getIsAlertable() {
        return isAlertable;
    }

    public void setIsAlertable(Boolean isAlertable) {
        this.isAlertable = isAlertable;
    }

    public Object getProperties() {
        return properties;
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }


}

