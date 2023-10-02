package com.esq.rbac.service.schedulerule.domain;

import com.esq.rbac.service.schedulerule.scheduledefaultsubdomain.domain.ScheduleRuleDefault;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.esq.rbac.service.util.UtcDateConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(schema = "rbac", name = "scheduleRule")
@Data
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleRule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheduleRuleId")
    private Long scheduleRuleId;

    @Column(name = "name")
    @SpecialCharValidator
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "fromDate")
    @Pattern(regexp = DeploymentUtil.DATE_PATTERN, message = "Invalid Date Format")
    private String fromDate;

    @Column(name = "toDate")
    @Pattern(regexp = DeploymentUtil.DATE_PATTERN, message = "Invalid Date Format")
    private String toDate;

    @Column(name = "monthOfYear")
    @Pattern(regexp = DeploymentUtil.NUMBER_PATTERN, message = "Invalid Month of the year")
    private String monthOfYear;

    @Column(name = "dayOfWeek")
    @Pattern(regexp = DeploymentUtil.DOW_PATTERN, message = "Invalid Day of the Week")
    private String dayOfWeek;

    @Column(name = "hours")
    @Pattern(regexp = DeploymentUtil.NUMBER_PATTERN, message = "Invalid Hour")
    private String hour;

    @Column(name = "isOpen")
    private boolean isOpen;

    @Column(name = "scheduleRuleType")
    private String scheduleRuleType;

    @Column(name = "scheduleRuleSubType")
    private String scheduleRuleSubType;

    @Column(name = "repeatInterval")
    private String repeatInterval;

    @OneToOne
    @JoinColumn(name = "scheduleRuleDefaultId")
    @Valid
    private ScheduleRuleDefault scheduleRuleDefault;

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

    public boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    // Constructors, getters, and setters omitted for brevity
}

