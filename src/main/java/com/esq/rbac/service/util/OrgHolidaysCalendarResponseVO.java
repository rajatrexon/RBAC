package com.esq.rbac.service.util;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDate;
import java.time.LocalTime;

public class OrgHolidaysCalendarResponseVO {


    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDate holidayDate;

    @JsonSerialize(using = ToStringSerializer.class)
    private LocalTime startTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private LocalTime endTime;

    private int orgId;

    OrgHolidaysCalendarResponseVO() {
    }

    public OrgHolidaysCalendarResponseVO(LocalDate holidayDate, LocalTime startTime, LocalTime endTime, int orgId) {
        super();
        this.holidayDate = holidayDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.orgId = orgId;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }
}
