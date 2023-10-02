/*
 * Copyright (c)2016 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

public class Code {
	private Long codeId;
	private Integer displayOrder;
	private Boolean isActive;
	private String name;
	private String parentType;
	private String codeType;
	private String validationRegex;
	private String parentCodeValue;
	private String codeValue;
	private Integer createdBy;
	private Date createdOn;
	private Integer updatedBy;
	private Date updatedOn;
	private String scopeData;
	private String remarks;
	
	public Long getCodeId() {
		return codeId;
	}

	public void setCodeId(Long codeId) {
		this.codeId = codeId;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentType() {
		return parentType;
	}

	public void setParentType(String parentType) {
		this.parentType = parentType;
	}

	public String getCodeType() {
		return codeType;
	}

	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}

	public String getValidationRegex() {
		return validationRegex;
	}

	public void setValidationRegex(String validationRegex) {
		this.validationRegex = validationRegex;
	}

	public String getParentCodeValue() {
		return parentCodeValue;
	}

	public void setParentCodeValue(String parentCodeValue) {
		this.parentCodeValue = parentCodeValue;
	}

	public String getCodeValue() {
		return codeValue;
	}

	public void setCodeValue(String codeValue) {
		this.codeValue = codeValue;
	}

	public Integer getCreatedBy() {
		return createdBy;
	}

	@JsonIgnore
	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

	@JsonIgnore
	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	@JsonIgnore
	public Integer getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
	}

	@JsonIgnore
	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	@JsonIgnore
	public String getScopeData() {
		return scopeData;
	}

	public void setScopeData(String scopeData) {
		this.scopeData = scopeData;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public static Code copyOf(Code c){
		Code output = new Code();
		output.codeId = c.codeId;
		output.displayOrder = c.displayOrder;
		output.isActive = c.isActive;
		output.name = c.name;
		output.parentType = c.parentType;
		output.codeType = c.codeType;
		output.validationRegex = c.validationRegex;
		output.parentCodeValue = c.parentCodeValue;
		output.codeValue = c.codeValue;
		output.createdBy = c.createdBy;
		output.createdOn = c.createdOn;
		output.updatedBy = c.updatedBy;
		output.updatedOn = c.updatedOn;
		output.scopeData = c.scopeData;
		return output;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Code{codeId=").append(codeId);
		sb.append("; displayOrder=").append(displayOrder);
		sb.append("; isActive=").append(isActive);
		sb.append("; name=").append(name);
		sb.append("; parentType=").append(parentType);
		sb.append("; codeType=").append(codeType);
		sb.append("; validationRegex=").append(validationRegex);
		sb.append("; parentCodeValue=").append(parentCodeValue);
		sb.append("; codeValue=").append(codeValue);
		sb.append("; createdBy=").append(createdBy);
		sb.append("; createdOn=").append(createdOn);
		sb.append("; updatedBy=").append(updatedBy);
		sb.append("; updatedOn=").append(updatedOn);
		sb.append("; scopeData=").append(scopeData);
		sb.append("}");
		return sb.toString();
	}

}