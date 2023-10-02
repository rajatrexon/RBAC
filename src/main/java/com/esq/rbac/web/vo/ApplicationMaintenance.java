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
import java.util.Date;

public class ApplicationMaintenance {

	private Integer maintenanceId;
	private Integer childApplicationId;
	//@NotNull
	private Date fromDate;
	//@NotNull
	private Date toDate;
	private Boolean isEnabled;
	private Boolean isExpired;
	//@Size(min = 1, max = 4000)
	private String message;
	private Integer createdBy;
	private Date createdOn;
	private Integer updatedBy;
	private Date updatedOn;

	public Integer getMaintenanceId() {
		return maintenanceId;
	}

	public void setMaintenanceId(Integer maintenanceId) {
		this.maintenanceId = maintenanceId;
	}

	public Integer getChildApplicationId() {
		return childApplicationId;
	}

	public void setChildApplicationId(Integer childApplicationId) {
		this.childApplicationId = childApplicationId;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Boolean getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public Boolean getIsExpired() {
		return isExpired;
	}

	public void setIsExpired(Boolean isExpired) {
		this.isExpired = isExpired;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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
		sb.append("ApplicationMaintenance{maintenanceId=")
				.append(maintenanceId);
		sb.append("; childApplicationId=").append(childApplicationId);
		sb.append("; fromDate=").append(fromDate == null ? "" : fromDate);
		sb.append("; toDate=").append(toDate == null ? "" : toDate);
		sb.append("; message=").append(message);
		sb.append("; isEnabled=").append(isEnabled);
		sb.append("; isExpired=").append(isExpired);
		sb.append("; createdOn=").append(createdOn == null ? "" : createdOn);
		sb.append("; createdBy=").append(createdBy == null ? "0" : createdBy);
		sb.append("; updatedOn=").append(updatedOn == null ? "" : updatedOn);
		sb.append("; updatedBy=").append(updatedBy == null ? "0" : updatedBy);
		sb.append("}");
		return sb.toString();
	}

}
