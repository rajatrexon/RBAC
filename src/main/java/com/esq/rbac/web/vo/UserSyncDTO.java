package com.esq.rbac.web.vo;

public class UserSyncDTO {

	private Integer userSyncId;	
	private String sAMAccountName;
	private String userNameLdap;
	private String name;
	private String distinguishedName;
	private String cn;
	private String externalRecordId;
	private String userPrincipalName;
	private Integer organizationId;
	private Integer fromDataTable;
	private String objectCategory;
	private Integer userId;
	private Integer groupId;

	public UserSyncDTO() {

	}


	public Integer getGroupId() {
		return groupId;
	}


	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}


	public Integer getUserId() {
		return userId;
	}



	public void setUserId(Integer userId) {
		this.userId = userId;
	}



	public String getObjectCategory() {
		return objectCategory;
	}



	public void setObjectCategory(String objectCategory) {
		this.objectCategory = objectCategory;
	}



	public Integer getUserSyncId() {
		return userSyncId;
	}



	public void setUserSyncId(Integer userSyncId) {
		this.userSyncId = userSyncId;
	}



	public String getsAMAccountName() {
		return sAMAccountName;
	}



	public void setsAMAccountName(String sAMAccountName) {
		this.sAMAccountName = sAMAccountName;
	}



	public String getUserNameLdap() {
		return userNameLdap;
	}



	public void setUserNameLdap(String userNameLdap) {
		this.userNameLdap = userNameLdap;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getDistinguishedName() {
		return distinguishedName;
	}



	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}



	public String getCn() {
		return cn;
	}



	public void setCn(String cn) {
		this.cn = cn;
	}



	public String getExternalRecordId() {
		return externalRecordId;
	}



	public void setExternalRecordId(String externalRecordId) {
		this.externalRecordId = externalRecordId;
	}



	public String getUserPrincipalName() {
		return userPrincipalName;
	}



	public void setUserPrincipalName(String userPrincipalName) {
		this.userPrincipalName = userPrincipalName;
	}



	public Integer getOrganizationId() {
		return organizationId;
	}



	public void setOrganizationId(Integer organizationId) {
		this.organizationId = organizationId;
	}



	public Integer getFromDataTable() {
		return fromDataTable;
	}



	public void setFromDataTable(Integer fromDataTable) {
		this.fromDataTable = fromDataTable;
	}


}
