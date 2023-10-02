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
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Calendar {

	private Long calendarId;
	@Size(min = 1, max = 200)
	//@SpecialCharValidator
	private String name;
	private String timeZone;
	private Code calendarType;
	private Code calendarSubType;
	private String sharingType;
	private boolean isActive;
	private List<Long> ruleIdList = new LinkedList<Long>();

	private List<ScheduleRule> rules = new LinkedList<ScheduleRule>();

	private Integer createdBy;
	private Date createdOn;
	private Integer updatedBy;
	private Date updatedOn;
	private boolean isDeleted;
	//@Transient
	private String assigned;
	//@Transient
	private Boolean isDefaultCalendar;
	//@Transient
	private Long organizationId;
	private Long tenantId;
	//@Transient
	private String organizationName;
	
	//@SpecialCharValidator
	private String description;
	
	public Long getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(Long calendarId) {
		this.calendarId = calendarId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public Code getCalendarType() {
		return calendarType;
	}

	public void setCalendarType(Code calendarType) {
		this.calendarType = calendarType;
	}

	public Code getCalendarSubType() {
		return calendarSubType;
	}

	public void setCalendarSubType(Code calendarSubType) {
		this.calendarSubType = calendarSubType;
	}

	public String getSharingType() {
		return sharingType;
	}

	public void setSharingType(String sharingType) {
		this.sharingType = sharingType;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public List<ScheduleRule> getRules() {
		return rules;
	}

	public void setRules(List<ScheduleRule> rules) {
		this.rules = rules;
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
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Calendar{calendarId=").append(calendarId);
		sb.append("; name=").append(name);
		sb.append("; timeZone=").append(timeZone);
		if(calendarType != null){
			sb.append("; calendarType=").append(calendarType.getCodeValue());
		}
		if(calendarSubType != null){
			sb.append("; calendarSubType=").append(calendarSubType.getCodeValue());
		}
		sb.append("; sharingType=").append(sharingType);
		sb.append("; isActive=").append(isActive);
		sb.append("; tenantId=").append(tenantId);
		sb.append("; organizationName=").append(organizationName);
		sb.append("; ruleIdList=").append(ruleIdList);
		sb.append("; rules=").append(rules);
		sb.append("; description=").append(description);
		sb.append("}");
		return sb.toString();
	}

	
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}



	public Boolean getIsDefaultCalendar() {
		return isDefaultCalendar;
	}

	public void setIsDefaultCalendar(Boolean isDefaultCalendar) {
		this.isDefaultCalendar = isDefaultCalendar;
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public String getAssigned() {
		return assigned;
	}

	public void setAssigned(String assigned) {
		this.assigned = assigned;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

}
