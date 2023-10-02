package com.esq.rbac.service.distributiongroup.distusergroup.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.Set;


@Entity
@Table(schema = "rbac", name = "userDistGroup")
public class DistUserMap {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "distId", nullable = false, length = 32)
	private Integer distId;



	@Column(name = "userId")
	private Integer userId;


	@Column(name = "createdBy")
	private Integer createdBy;
	@Column(name = "createdOn")
	@Convert(converter = UtcDateConverter.class)
	private Date createdOn = DateTime.now().toDate();
	
	@Transient
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
