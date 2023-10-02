package com.esq.rbac.service.organization.organizationcalendar.domain;


import com.esq.rbac.service.util.UtcDateConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "organizationCalendar", schema = "rbac")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organizationCalendarId")
    private Long organizationCalendarId;

    @Column(name = "organizationId")
    private Long organizationId;

    @Column(name = "calendarId")
    private Long calendarId;

    @Column(name = "isDefaultCalendar", nullable = false)
    private Boolean isDefaultCalendar;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "createdOn")
    @Convert(converter = UtcDateConverter.class)
    private Date createdOn;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    @Column(name = "updatedOn")
    @Convert(converter = UtcDateConverter.class)
    private Date updatedOn;
}
