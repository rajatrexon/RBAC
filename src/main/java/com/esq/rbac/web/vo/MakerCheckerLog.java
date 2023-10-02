package com.esq.rbac.web.vo;

/**
* The MakerCheckerLog is an Entity class
* Used to store log data MakerChecker
* makerCheckerId is foreign key here
* @author  Pankaj
* @version 1.0
* @company liberin technologies pvt limited
* @since   2018-12-05 
*/

import java.io.Serializable;
import java.util.Date;

public class MakerCheckerLog implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	//@NotNull
	private String entityType;
	//@NotNull
	private String entityName;
	//@NotNull
	private String entityValue;
	//@NotNull
	private String entityJson;
	private Integer transactionBy;
	//@NotNull
	private Date transactionOn;
	private Integer createdBy;
	//@NotNull
	private Date createdOn;
	private String rejectReason;
	private Integer entityStatus;
	//@NotNull
	private Long organizationId;
	private Boolean isValid;
	private Integer entityId;
	//@NotNull
	private Long makerCheckerId;
	private Long tenantId;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String getEntityJson() {
		return entityJson;
	}
	public void setEntityJson(String entityJson) {
		this.entityJson = entityJson;
	}
	public Integer getTransactionBy() {
		return transactionBy;
	}
	public void setTransactionBy(Integer transactionBy) {
		this.transactionBy = transactionBy;
	}
	public Date getTransactionOn() {
		return transactionOn;
	}
	public void setTransactionOn(Date transactionOn) {
		this.transactionOn = transactionOn;
	}
	public String getRejectReason() {
		return rejectReason;
	}
	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}
	public Integer getEntityStatus() {
		return entityStatus;
	}
	public void setEntityStatus(Integer entityStatus) {
		this.entityStatus = entityStatus;
	}
	public Long getOrganizationId() {
		return organizationId;
	}
	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}
	public Boolean getIsValid() {
		return isValid;
	}
	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}
	public Integer getEntityId() {
		return entityId;
	}
	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}
	public Long getMakerCheckerId() {
		return makerCheckerId;
	}
	public void setMakerCheckerId(Long makerCheckerId) {
		this.makerCheckerId = makerCheckerId;
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
	public Long getTenantId() {
		return tenantId;
	}
	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}
	public String getEntityValue() {
		return entityValue;
	}
	public void setEntityValue(String entityValue) {
		this.entityValue = entityValue;
	}
	@Override
	public String toString() {
		return "MakerCheckerLog [id=" + id + ", entityType=" + entityType + ", entityName=" + entityName
				+ ", entityValue=" + entityValue + ", entityJson=" + entityJson + ", transactionBy=" + transactionBy
				+ ", transactionOn=" + transactionOn + ", createdBy=" + createdBy + ", createdOn=" + createdOn
				+ ", rejectReason=" + rejectReason + ", entityStatus=" + entityStatus + ", organizationId="
				+ organizationId + ", isValid=" + isValid + ", entityId=" + entityId + ", makerCheckerId="
				+ makerCheckerId + ", tenantId=" + tenantId + "]";
	}

	

}
