package com.esq.rbac.service.schedulerule.scheduledefaultsubdomain.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(schema = "rbac", name = "scheduleRuleDefault")
@Data
public class ScheduleRuleDefault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheduleRuleDefaultId")
    private Long scheduleRuleDefaultId;

    @Column(name = "scheduleRuleKey")
    private String scheduleRuleKey;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "monthOfYear")
    private String monthOfYear;

    @Column(name = "dayOfWeek")
    private String dayOfWeek;

    @Column(name = "hours")
    private String hour;

    @Column(name = "isOpen")
    private boolean isOpen;

    @Column(name = "displayOrder")
    private int displayOrder;

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

    // Constructors, getters, and setters omitted for brevity
}
