/*
 * Copyright (c)2015 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class UserInfoRBAC {
	private Integer userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String displayName;
    private String applicationName;
    private Map<String, List<String>> permissions;
    private Map<String, String> scopes;
	private Map<String, Map<String, String>> variables;
	private String group;
	private Integer groupId;
	private Date lastSuccessfulLoginTime;
	
	public UserInfoRBAC(){
		
	}
	
	public UserInfoRBAC(UserInfo userInfo){
		this.userId = userInfo.getUserId();
		this.userName = userInfo.getUserName();
		this.firstName = userInfo.getFirstName();
		this.lastName = userInfo.getLastName();
		this.displayName = userInfo.getDisplayName();
		this.applicationName = userInfo.getApplicationName();
		this.permissions = userInfo.getPermissions();
		this.scopes = userInfo.getScopes();
		this.variables = userInfo.getVariables();
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public Map<String, List<String>> getPermissions() {
		return permissions;
	}
	public void setPermissions(Map<String, List<String>> permissions) {
		this.permissions = permissions;
	}
	public Map<String, String> getScopes() {
		return scopes;
	}
	public void setScopes(Map<String, String> scopes) {
		this.scopes = scopes;
	}
	public Map<String, Map<String, String>> getVariables() {
		return variables;
	}
	public void setVariables(Map<String, Map<String, String>> variables) {
		this.variables = variables;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	
	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	@JsonSerialize()
	@JsonInclude(value=Include.ALWAYS)
	public Date getLastSuccessfulLoginTime() {
		return lastSuccessfulLoginTime;
	}

	public void setLastSuccessfulLoginTime(Date lastSuccessfulLoginTime) {
		this.lastSuccessfulLoginTime = lastSuccessfulLoginTime;
	}
	
	public static UserInfoDetails toUserInfoDetails(UserInfoRBAC userInfoRBAC){
		if(userInfoRBAC!=null){
			UserInfoDetails userInfoDetails = new UserInfoDetails(UserInfo.fromUserInfoRBAC(userInfoRBAC));
			userInfoDetails.setLastSuccessfulLoginTime(userInfoRBAC.getLastSuccessfulLoginTime());
			return userInfoDetails;
		}
		return null;
	}
}
