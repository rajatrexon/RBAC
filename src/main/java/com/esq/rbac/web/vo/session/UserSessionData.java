package com.esq.rbac.web.vo.session;

import java.util.Date;
import java.util.Map;

public class UserSessionData {

	private final String userName;
	private final String loginType;
	private final Date loginTime;
	private String identityId;
	Map<String, String> additionalAttributes;

	public UserSessionData(String userName, String loginType, Date loginTime) {
		this.userName = userName;
		this.loginType = loginType;
		this.loginTime = loginTime;
	}
	
	public UserSessionData(){
		this.userName = null;
		this.loginType = null;
		this.loginTime = null;
	}

	public String getUserName() {
		return userName;
	}

	public String getLoginType() {
		return loginType;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public String getIdentityId() {
		return identityId;
	}

	public void setIdentityId(String identityId) {
		this.identityId = identityId;
	}
	
	public UserSessionData identityId(String identityId) {
		this.identityId = identityId;
		return this;
	}

	public Map<String, String> getAdditionalAttributes() {
		return additionalAttributes;
	}

	public void setAdditionalAttributes(Map<String, String> additionalAttributes) {
		this.additionalAttributes = additionalAttributes;
	}

	@Override
	public String toString() {
		return "UserSessionData [userName=" + userName + ", loginType=" + loginType + ", loginTime=" + loginTime
				+ ", identityId=" + identityId + ", additionalAttributes=" + additionalAttributes + "]";
	}

}
