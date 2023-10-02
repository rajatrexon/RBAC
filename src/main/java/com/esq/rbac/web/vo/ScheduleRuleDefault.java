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

import java.util.Date;

public class ScheduleRuleDefault {
	
	private Long scheduleRuleDefaultId;
	private String scheduleRuleKey;
	private String name;
	private String description;
	private String monthOfYear;
	private String dayOfWeek;
	private String hour;
	private boolean isOpen;
	private int displayOrder;
	
	private Integer createdBy;
	private Date createdOn;
	private Integer updatedBy;
	private Date updatedOn;

	public Long getScheduleRuleDefaultId() {
		return scheduleRuleDefaultId;
	}

	public void setScheduleRuleDefaultId(Long scheduleRuleDefaultId) {
		this.scheduleRuleDefaultId = scheduleRuleDefaultId;
	}

	public String getScheduleRuleKey() {
		return scheduleRuleKey;
	}

	public void setScheduleRuleKey(String scheduleRuleKey) {
		this.scheduleRuleKey = scheduleRuleKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMonthOfYear() {
		return monthOfYear;
	}

	public void setMonthOfYear(String monthOfYear) {
		this.monthOfYear = monthOfYear;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	
	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public boolean getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
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
		sb.append("ScheduleRuleDefault{scheduleRuleDefaultId=").append(scheduleRuleDefaultId);
		sb.append("; scheduleRuleKey=").append(scheduleRuleKey);
		sb.append("; name=").append(name);
		sb.append("; description=").append(description);
		sb.append("; monthOfYear=").append(monthOfYear);
		sb.append("; dayOfWeek=").append(dayOfWeek);
		sb.append("; hour=").append(hour);
		sb.append("; isOpen=").append(isOpen);
		sb.append("; displayOrder=").append(displayOrder);
		sb.append("}");
		return sb.toString();
	}
}
