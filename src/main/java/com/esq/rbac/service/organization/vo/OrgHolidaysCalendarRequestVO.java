package com.esq.rbac.service.organization.vo;

import com.esq.rbac.service.util.LocalDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;

public class OrgHolidaysCalendarRequestVO {

    OrgHolidaysCalendarRequestVO() {
    }

    public OrgHolidaysCalendarRequestVO(LocalDate startDate, LocalDate endDate, int orgId) {
        super();
        this.startDate = startDate;
        this.endDate = endDate;
        this.orgId = orgId;
    }

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate startDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate endDate;

    private int orgId;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }
}
