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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

public class Organization {

	private Long organizationId;
	
//	@Size(min = 1, max = 300)
//	@SpecialCharValidator
	private String organizationName;
//	@Size(min = 0, max = 4000)
//	@SpecialCharValidator
	private String organizationFullName;
//	@Size(min = 0, max = 4000)
//	@SpecialCharValidator
	private String remarks;
	
//	@NotNull
	private Code organizationType;
//	@NotNull
	private Code organizationSubType;
	private Long parentOrganizationId;
//	@Size(min = 0, max = 1000)
	private String organizationURL;
//	@NotNull
	private Long tenantId;
	private Integer createdBy;
	private Date createdOn;
	private Integer updatedBy;
	private Date updatedOn;
	private boolean isDeleted;
	private Boolean isShared;
	private String organizationTimeZone;
	
	public String getOrganizationTimeZone() {
		return organizationTimeZone;
	}

	public void setOrganizationTimeZone(String organizationTimeZone) {
		this.organizationTimeZone = organizationTimeZone;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OrganizationMaintenance{organizationId=")
				.append(organizationId);
		sb.append("; organizationName=").append(organizationName);
		sb.append("; organizationFullName=").append(organizationFullName);
		sb.append("; remarks=").append(remarks);
		sb.append("; organizationType=").append(organizationType);
		sb.append("; organizationSubType=").append(organizationSubType);
		sb.append("; parentOrganizationId=").append(parentOrganizationId);
		sb.append("; organizationURL=").append(organizationURL);
		sb.append("; tenantId=").append(tenantId);
		sb.append("; isShared=").append(isShared);
		sb.append("; createdOn=").append(createdOn == null ? "" : createdOn);
		sb.append("; createdBy=").append(createdBy == null ? "0" : createdBy);
		sb.append("; updatedOn=").append(updatedOn == null ? "" : updatedOn);
		sb.append("; updatedBy=").append(updatedBy == null ? "0" : updatedBy);
		sb.append("}");
		return sb.toString();
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getOrganizationFullName() {
		return organizationFullName;
	}

	public void setOrganizationFullName(String organizationFullName) {
		this.organizationFullName = organizationFullName;
	}

	public Code getOrganizationType() {
		return organizationType;
	}

	public void setOrganizationType(Code organizationType) {
		this.organizationType = organizationType;
	}

	public Code getOrganizationSubType() {
		return organizationSubType;
	}

	public void setOrganizationSubType(Code organizationSubType) {
		this.organizationSubType = organizationSubType;
	}

	public Long getParentOrganizationId() {
		return parentOrganizationId;
	}

	public void setParentOrganizationId(Long parentOrganizationId) {
		this.parentOrganizationId = parentOrganizationId;
	}

	public String getOrganizationURL() {
		return organizationURL;
	}

	public void setOrganizationURL(String organizationURL) {
		this.organizationURL = organizationURL;
	}
	
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	@JsonIgnore
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Boolean getIsShared() {
		return isShared;
	}

	public void setIsShared(Boolean isShared) {
		this.isShared = isShared;
	}
	
}
