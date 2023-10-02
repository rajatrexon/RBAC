package com.esq.rbac.web.vo;

import org.springframework.beans.BeanUtils;

import java.util.Date;

public class UserSync {
	
private Integer userSyncId;	
private String externalRecordId;
private String syncData;
private String updatedSyncData;
private Integer createdBy;
private Date createdOn;
private Integer updatedBy;
private Date updatedOn;	
private boolean isDeleted;
private Integer status;

public UserSync(){
	
}

public UserSync(UserSync us, User user){
	BeanUtils.copyProperties(us, this);
	this.user = user;
}

private User user;

public Integer getStatus() {
	return status;
}

public void setStatus(Integer status) {
	this.status = status;
}

public Integer getUserSyncId(){
	return userSyncId;
}
public void setUserSyncId(Integer userSyncId){
	this.userSyncId = userSyncId;
}
public String getExternalRecordId(){
	return externalRecordId;
}
public void setExternalRecordId(String externalRecordId){
	this.externalRecordId = externalRecordId;
}

public String getSyncData(){
	return syncData;
}
public void setSyncData(String syncData){
	this.syncData = syncData;
}

public String getUpdatedSyncData() {
	return updatedSyncData;
}

public void setUpdatedSyncData(String updatedSyncData) {
	this.updatedSyncData = updatedSyncData;
}

public Integer getCreatedBy() {
	return createdBy;
}
public void setCreatedBy(Integer createdBy) {
	this.createdBy = createdBy;
}

public Date getCreatedOn() {
	return createdOn;
}

public void setCreatedOn(Date createdOn) {
	this.createdOn = createdOn;
}

public Integer getUpdatedBy() {
	return updatedBy;
}

public void setUpdatedBy(Integer updatedBy) {
	this.updatedBy = updatedBy;
}

public Date getUpdatedOn() {
	return updatedOn;
}

public void setUpdatedOn(Date updatedOn) {
	this.updatedOn = updatedOn;
}
public boolean isDeleted(){
	return isDeleted;
}
public void setDeleted(boolean isDeleted) {
	this.isDeleted = isDeleted;
}
public User getUser(){
	return user;
}
public void setUser(User user){
	this.user = user;
}


	}
