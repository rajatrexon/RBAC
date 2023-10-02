/*
 * Copyright (c)2013,2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorInfo {

    public static final String INTERNAL_ERROR = "internalError";
    public static final String USER_NOT_FOUND = "userNotFound";
    public static final String APPLICATION_NOT_FOUND = "applicationNotFound";
    public static final String APPLICATION_USERS_NOT_FOUND = "noUsersFound";
    public static final String USER_DISABLED = "userDisabled";
    public static final String ROLES_NOT_FOUND = "noRolesFound";
    public static final String NO_AUTH_APPS_FOUND = "noAuthorizedAppsFound";
    public static final String USER_LOCKED = "userLocked";
    public static final String INVALID_PASSWORD = "invalidPassword";
    public static final String LOGIN_POLICY_VIOLATED = "loginPolicyViolated";
    public static final String SERVER_ERROR = "serverError";
    public static final String ACCESS_DENIED = "accessDenied";
    public static final String LOGIN_REJECTED = "loginRejected";
    public static final String XSS_ERROR_CODE = "invalidInputData";
    public static final String XSS_ERROR_MESSAGE = "XSS";
    public static final String NON_MULTI_TENANT_ENVIRONMENT = "multiTenantNotLicensed";
    
    private String errorCode;
    private String exceptionMessage;
    private Map<String, String> parameters;

    public ErrorInfo() {
        // empty
    }

    public ErrorInfo(ErrorInfoException e) {
        this.errorCode = e.getCode();
        this.exceptionMessage = e.getMessage();
        this.parameters = e.getParameters();
    }

    public ErrorInfo(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void add(String parameterName, String parameterValue) {
        if (parameters == null) {
            parameters = new LinkedHashMap<String, String>();
        }
        parameters.put(parameterName, parameterValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ErrorInfo{errorCode=").append(errorCode);
        sb.append("; exceptionMessage=").append(exceptionMessage);
        sb.append("; parameters=").append(parameters);
        sb.append("}");
        return sb.toString();
    }
}
