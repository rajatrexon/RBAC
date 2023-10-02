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


import com.esq.rbac.web.util.RBACUtil;

import java.util.Date;

public class SessionRegistryLogoutRequest {
	private String userName;
	private String sessionHash;
	private String service;
	private String clientIp;
	private String logoutType;
	private ChildApplication childApplication;
	private RBACUtil.LOGOUT_ACTION logoutAction;
	private String requestId;
	private Integer appType;
	private String childAppName;
	private String ticketToLogout;
	private String appKey;
	private String tag;
	private Date cutOffDate;

	public SessionRegistryLogoutRequest() {

	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSessionHash() {
		return sessionHash;
	}

	public void setSessionHash(String sessionHash) {
		this.sessionHash = sessionHash;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getLogoutType() {
		return logoutType;
	}

	public void setLogoutType(String logoutType) {
		this.logoutType = logoutType;
	}

	public ChildApplication getChildApplication() {
		return childApplication;
	}

	public void setChildApplication(ChildApplication childApplication) {
		this.childApplication = childApplication;
	}

	public RBACUtil.LOGOUT_ACTION getLogoutAction() {
		return logoutAction;
	}

	public void setLogoutAction(RBACUtil.LOGOUT_ACTION logoutAction) {
		this.logoutAction = logoutAction;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Integer getAppType() {
		return appType;
	}

	public void setAppType(Integer appType) {
		this.appType = appType;
	}

	public String getChildAppName() {
		return childAppName;
	}

	public void setChildAppName(String childAppName) {
		this.childAppName = childAppName;
	}

	public String getTicketToLogout() {
		return ticketToLogout;
	}

	public void setTicketToLogout(String ticketToLogout) {
		this.ticketToLogout = ticketToLogout;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Date getCutOffDate() {
		return cutOffDate;
	}

	public void setCutOffDate(Date cutOffDate) {
		this.cutOffDate = cutOffDate;
	}
}
