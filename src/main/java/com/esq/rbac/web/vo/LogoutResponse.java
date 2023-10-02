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

import com.esq.rbac.web.vo.session.SSOLogoutData;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LogoutResponse {
    public static final String INVALID_APP_KEY = "invalidAppKey";
    public static final String INVALID_SERVICE_TICKET = "invalidServiceTicket";
    public static final String LOGOUT_SUCCESSFULL = "logoutSuccessful";
    private String resultCode;
    private Boolean isSuccess;
    private String resultMessage;
    private Map<String, List<String>> sessionHashChildApplicationNames = new LinkedHashMap<String, List<String>>();
    private List<SSOLogoutData> ssoLogoutDataList;

    public LogoutResponse(String sessionHash, String resultCode, Boolean isSuccess, String... childApplicationNames) {
        this.resultCode = resultCode;
        this.isSuccess = isSuccess;
        if (childApplicationNames != null && childApplicationNames.length != 0) {
            this.sessionHashChildApplicationNames.put(sessionHash, Arrays.asList(childApplicationNames));
        }
    }

    public LogoutResponse(Map<String, List<String>> sessionHashChildApplicationNames) {
        this.sessionHashChildApplicationNames = sessionHashChildApplicationNames;
    }

    public LogoutResponse() {

    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Map<String, List<String>> getSessionHashChildApplicationNames() {
        return sessionHashChildApplicationNames;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public List<SSOLogoutData> getSsoLogoutDataList() {
        return ssoLogoutDataList;
    }

    public void setSsoLogoutDataList(List<SSOLogoutData> ssoLogoutDataList) {
        this.ssoLogoutDataList = ssoLogoutDataList;
    }

}
