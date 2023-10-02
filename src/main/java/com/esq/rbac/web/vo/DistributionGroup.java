package com.esq.rbac.web.vo;

import org.joda.time.DateTime;

import java.util.Date;
public class DistributionGroup {
	
	private Integer distId;
//	@NotNull
//	@Size(min = 1, max = 300)
//    @Pattern(regexp = "^([^<>=]*)$", message = "Name should not have <,> and =")
	private String distName;
	//@Size(max = 4000)
    //@Pattern(regexp = "^([^<>=]*)$", message = "Description should not have <,> and =")
	private String description;
	//@NotNull
	private Integer tenantId;
	private Integer updatedBy;
	private Integer createdBy;
	private Date createdOn = DateTime.now().toDate();;
	private Date updatedOn;
	private Integer isActive = 1;
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getDistId() {
		return distId;
	}
	public void setDistId(Integer distId) {
		this.distId = distId;
	}
	public String getDistName() {
		return distName;
	}
	public void setDistName(String distName) {
		this.distName = distName;
	}
	public Integer getTenantId() {
		return tenantId;
	}
	public void setTenantId(Integer tenantId) {
		this.tenantId = tenantId;
	}
	public Integer getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
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
	public Date getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}
	public Integer getIsActive() {
		return isActive;
	}
	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}
	
}
