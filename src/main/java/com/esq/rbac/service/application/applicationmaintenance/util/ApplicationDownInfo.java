package com.esq.rbac.service.application.applicationmaintenance.util;

import java.util.Date;

public class ApplicationDownInfo {

    private String message;
    private Date fromDate;
    private Date toDate;
    private String childApplicationName;

    public ApplicationDownInfo(){

    }

    public ApplicationDownInfo(String childApplicationName){
        this.childApplicationName = childApplicationName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public String getChildApplicationName() {
        return childApplicationName;
    }

    public void setChildApplicationName(String childApplicationName) {
        this.childApplicationName = childApplicationName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ApplicationDownInfo{childApplicationName=").append(childApplicationName);
        sb.append("; message=").append(message);
        sb.append("; fromDate=").append(fromDate == null ? "" : fromDate);
        sb.append("; toDate=").append(toDate == null ? "" : toDate);
        sb.append("}");
        return sb.toString();
    }

}