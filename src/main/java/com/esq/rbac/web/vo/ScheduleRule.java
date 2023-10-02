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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleRule implements Serializable {

	private Long scheduleRuleId;
	//@SpecialCharValidator
	private String name;
	private String description;
	////@Pattern(regexp=DeploymentUtil.DATE_PATTERN,message="Invalid Date Format")
	private String fromDate;
	//@Pattern(regexp=DeploymentUtil.DATE_PATTERN,message="Invalid Date Format")
	private String toDate;
	//@Pattern(regexp=DeploymentUtil.NUMBER_PATTERN,message="Invalid Month of the year")
	private String monthOfYear;
	///@Pattern(regexp=DeploymentUtil.DOW_PATTERN,message="Invalid Day of the Week")
	private String dayOfWeek;
	//@Pattern(regexp=DeploymentUtil.NUMBER_PATTERN,message="Invalid Hour")
	private String hour;
	private boolean isOpen;
	private String scheduleRuleType;
	private String scheduleRuleSubType;
	private String repeatInterval;
	private ScheduleRuleDefault scheduleRuleDefault;

	private Integer createdBy;
	private Date createdOn;
	private Integer updatedBy;
	private Date updatedOn;

	public Long getScheduleRuleId() {
		return scheduleRuleId;
	}

	public void setScheduleRuleId(Long scheduleRuleId) {
		this.scheduleRuleId = scheduleRuleId;
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

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
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

	public String getScheduleRuleType() {
		return scheduleRuleType;
	}

	public void setScheduleRuleType(String scheduleRuleType) {
		this.scheduleRuleType = scheduleRuleType;
	}

	public String getScheduleRuleSubType() {
		return scheduleRuleSubType;
	}

	public void setScheduleRuleSubType(String scheduleRuleSubType) {
		this.scheduleRuleSubType = scheduleRuleSubType;
	}

	public String getRepeatInterval() {
		return repeatInterval;
	}

	public void setRepeatInterval(String repeatInterval) {
		this.repeatInterval = repeatInterval;
	}
	
	public ScheduleRuleDefault getScheduleRuleDefault() {
		return scheduleRuleDefault;
	}

	public void setScheduleRuleDefault(ScheduleRuleDefault scheduleRuleDefault) {
		this.scheduleRuleDefault = scheduleRuleDefault;
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
		sb.append("ScheduleRule{scheduleRuleId=").append(scheduleRuleId);
		sb.append("; name=").append(name);
		sb.append("; description=").append(description);
		sb.append("; fromDate=").append(fromDate);
		sb.append("; toDate=").append(toDate);
		sb.append("; monthOfYear=").append(monthOfYear);
		sb.append("; dayOfWeek=").append(dayOfWeek);
		sb.append("; hour=").append(hour);
		sb.append("; isOpen=").append(isOpen);
		sb.append("; scheduleRuleType=").append(scheduleRuleType);
		sb.append("; scheduleRuleSubType=").append(scheduleRuleSubType);
		sb.append("; repeatInterval=").append(repeatInterval);
		sb.append("}");
		return sb.toString();
	}
}
