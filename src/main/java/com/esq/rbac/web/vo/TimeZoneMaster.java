package com.esq.rbac.web.vo;

import java.io.Serializable;

public class TimeZoneMaster implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String timezoneId;//timezoneId
	
	private String timezoneValue;//timeZoneValue
	
	private String timeOffset;
	
	private Integer timeOffsetMinute;
	
	public TimeZoneMaster() {
		
	}
	
	public TimeZoneMaster(String timezoneId, String timezoneValue, String timeOffset, Integer timeOffsetMinute) {
		super();
		this.timezoneId = timezoneId;
		this.timezoneValue = timezoneValue;
		this.timeOffset = timeOffset;
		this.timeOffsetMinute = timeOffsetMinute;
	}
	
	public String getTimezoneId() {
		return timezoneId;
	}

	public void setTimezoneId(String timezoneId) {
		this.timezoneId = timezoneId;
	}

	public String getTimezoneValue() {
		return timezoneValue;
	}

	public void setTimezoneValue(String timezoneValue) {
		this.timezoneValue = timezoneValue;
	}

	public String getTimeOffset() {
		return timeOffset;
	}

	public void setTimeOffset(String timeOffset) {
		this.timeOffset = timeOffset;
	}

	public Integer getTimeOffsetMinute() {
		return timeOffsetMinute;
	}

	public void setTimeOffsetMinute(Integer timeOffsetMinute) {
		this.timeOffsetMinute = timeOffsetMinute;
	}

	@Override
	public String toString() {
		return "TimeZoneMaster [timezoneId=" + timezoneId
				+ ", timezoneValue=" + timezoneValue + "timeOffset=" + timeOffset + "timeOffsetMinute=" + timeOffsetMinute + "]";
	}
}
