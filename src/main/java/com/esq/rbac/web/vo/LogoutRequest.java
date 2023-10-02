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

import java.util.Map;

public class LogoutRequest {

    private String service;
    private String userName;
    private String clientIP;
    private Map<String, String> parameters;
    private String sessionHash;
    private String logoutType;
    private String loginType;
    private String serviceTicket;
    private String appKey;

	public static final String LOGOUT_TYPE_USER = "userLogout";
    public static final String LOGOUT_TYPE_CONCURRENT_LOGIN_NOT_ACTIVE = "concurrentLoginNotAllowedPreviousSessionTerminated";
    public static final String LOGOUT_TYPE_CHANGE_PASSWORD = "passwordChangeSessionTermination";
    public static final String LOGOUT_TYPE_CONCURRENT_LOGIN_NOT_ACTIVE_CHILD_APP = "concurrentLoginNotAllowedPreviousSessionTerminatedApplication";
    public static final String LOGOUT_TYPE_NATIVE_APP = "nativeLogout";
    public static final String LOGOUT_TYPE_SESSION_KILL = "sessionKilledBy-";
    public static final String LOGOUT_TYPE_USER_DELTED = "userDeletedBy-";
    public static final String LOGOUT_TYPE_USER_DEACTIVED_OR_LOCKED = "userDeactivedLockedBy-";
    public static final String LOGOUT_TYPE_USER_RENAMED = "userRenamedLockedBy-";
    public static final String LOGOUT_TYPE_WINDOW_CLOSED = "windowClosed";
    public static final String LOGOUT_TYPE_APP_RESTARTED = "appRestarted";
    public static final String LOGOUT_TYPE_SESSION_TIMEOUT = "sessionTimeout";
    public static final String LOGOUT_TYPE_RBAC_RESTART = "rbacStartStop";
    public static final String LOGOUT_TYPE_SSO_REFRESH = "ssoSessionRefresh";
    public static final String LOGOUT_EXT_URL_REDIRECT_FROM_CHANGE_PSWD = "redirectedToExternalUrlfromChangePasswordScreen";
    
    public LogoutRequest(){
    	
    }
    
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

	public String getSessionHash() {
		return sessionHash;
	}

	public void setSessionHash(String sessionHash) {
		this.sessionHash = sessionHash;
	}
	
    public String getLogoutType() {
		return logoutType;
	}

	public void setLogoutType(String logoutType) {
		this.logoutType = logoutType;
	}
	
	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}

	public String getServiceTicket() {
		return serviceTicket;
	}

	public void setServiceTicket(String serviceTicket) {
		this.serviceTicket = serviceTicket;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LogoutRequest{userName=").append(userName);
        if (clientIP != null) {
            sb.append("; clientIP=").append(clientIP);
        }
        if (service != null) {
            sb.append(";service=").append(service);
        }
        if (parameters != null) {
            sb.append("; parameters=").append(parameters);
        }
        if (sessionHash != null) {
            sb.append("; sessionHash=").append(sessionHash);
        }
        if (logoutType != null) {
            sb.append("; logoutType=").append(logoutType);
        }
        if (loginType != null) {
            sb.append("; loginType=").append(loginType);
        }
        if (serviceTicket != null) {
            sb.append("; serviceTicket=").append(serviceTicket);
        }
        if (appKey != null) {
            sb.append("; appKey=").append(appKey);
        }
        sb.append("}");
        return sb.toString();
    }
}
