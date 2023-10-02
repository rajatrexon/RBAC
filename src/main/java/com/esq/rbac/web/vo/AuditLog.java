/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.web.vo;

import java.util.Date;

public class AuditLog {

    //public static Integer LOG_BUFFER_MAX_SIZE = 2000;
    private Integer auditLogId;
    private Date createdTime;
    private Integer userId;
    private Integer applicationId;
    private Integer targetId;
    private Integer operationId;
    private String queryField1;
    private String queryField2;
    private Boolean isAlertable;
    private Boolean isSuccess;
    private Boolean isCompressed;
    private String logBuffer;

    public Integer getAuditLogId() {
        return auditLogId;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public Integer getOperationId() {
        return operationId;
    }

    public void setOperationId(Integer operationId) {
        this.operationId = operationId;
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

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Boolean getIsCompressed() {
        return isCompressed;
    }

    public void setIsCompressed(Boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    public String getLogBuffer() {
        return logBuffer;
    }

    public void setLogBuffer(String logBuffer) {
        this.logBuffer = logBuffer;
    }
}
