/*
 * Copyright (c)2013,2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

import java.io.Serializable;
import java.util.Date;

public class MakerChecker implements Serializable {

	/**
	 * 
	 */
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
//	@Size(min = 0, max = 500)
//	@Pattern(regexp = "^([^<>=]*)$", message = "Reject Reason should not have <,> and =")
//	@SpecialCharValidator
	private String rejectReason;
	private Integer entityStatus;
	private Long organizationId;
	private Boolean isValid;
	private Integer entityId;
	private Long tenantId;
	
	
	//@Transient
	String makerCheckerIdsForAction;
	
	//@Transient
	Integer isApproveFlag;
	
	//@Transient
	String transactionByName;
	
	//@Transient
	String operation;
	
	public String getEntityValue() {
		return entityValue;
	}
	public void setEntityValue(String entityValue) {
		this.entityValue = entityValue;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getTransactionByName() {
		return transactionByName;
	}
	public void setTransactionByName(String transactionByName) {
		this.transactionByName = transactionByName;
	}
	public String getMakerCheckerIdsForAction() {
		return makerCheckerIdsForAction;
	}
	public void setMakerCheckerIdsForAction(String makerCheckerIdsForAction) {
		this.makerCheckerIdsForAction = makerCheckerIdsForAction;
	}
	public Integer getIsApproveFlag() {
		return isApproveFlag;
	}
	public void setIsApproveFlag(Integer isApproveFlag) {
		this.isApproveFlag = isApproveFlag;
	}
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
	@Override
	public String toString() {
		return "MakerChecker [id=" + id + ", entityType=" + entityType + ", entityName=" + entityName + ", entityValue="
				+ entityValue + ", entityJson=" + entityJson + ", transactionBy=" + transactionBy + ", transactionOn="
				+ transactionOn + ", createdBy=" + createdBy + ", createdOn=" + createdOn + ", rejectReason="
				+ rejectReason + ", entityStatus=" + entityStatus + ", organizationId=" + organizationId + ", isValid="
				+ isValid + ", entityId=" + entityId + ", tenantId=" + tenantId + ", makerCheckerIdsForAction="
				+ makerCheckerIdsForAction + ", isApproveFlag=" + isApproveFlag + ", transactionByName="
				+ transactionByName + ", operation=" + operation + "]";
	}

	
}
