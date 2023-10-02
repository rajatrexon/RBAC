package com.esq.rbac.web.vo;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.Set;

public class DistUserMap {
	
	private Integer id;
	//@NotNull
	private Integer distId;
	
	private Integer userId;
	private Integer createdBy;
	private Date createdOn = DateTime.now().toDate();
	
//	@Transient
//	@NotNull
	Set<Integer> userIdSet;
	
	public Set<Integer> getUserIdSet() {
		return userIdSet;
	}
	public void setUserIdSet(Set<Integer> userIdSet) {
		this.userIdSet = userIdSet;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getDistId() {
		return distId;
	}
	public void setDistId(Integer distId) {
		this.distId = distId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
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
	

}
