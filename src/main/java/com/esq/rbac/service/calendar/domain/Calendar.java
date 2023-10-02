package com.esq.rbac.service.calendar.domain;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.esq.rbac.service.schedulerule.domain.ScheduleRule;
import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.esq.rbac.service.util.UtcDateConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.Data;
import org.hibernate.annotations.Where;


@Entity
@Table(schema = "rbac", name = "calendar")
@Data
@Where(clause = "isDeleted = false")
@SqlResultSetMapping(
        name = "CalendarAssignMapping",
        entities = @EntityResult(
                entityClass = Calendar.class,
                fields = {/*
                    @FieldResult(name = "id", column = "id"),
                  */}),
        columns = {@ColumnResult(name = "status", type = Integer.class), @ColumnResult(name = "isDefault", type = Integer.class), @ColumnResult(name = "OrgName", type = String.class),@ColumnResult(name = "OrgId", type = String.class)})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendarId")
    private Long calendarId;

    @Column(name = "name")
    @SpecialCharValidator
    private String name;

    @Column(name = "timeZone")
    private String timeZone;

    @Column(name = "description")
    @SpecialCharValidator
    private String description;

    @Column(name = "sharingType")
    private String sharingType;

    @Column(name = "isActive")
    private Boolean isActive;

    @Column(name = "tenantId")
    private Long tenantId;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "rbac", name = "calendarScheduleRuleMapping", joinColumns = @JoinColumn(name = "calendarId", nullable = false))
    @Column(name = "scheduleRuleId", nullable = false)
    @OrderColumn(name = "seqNum")
    private List<Long> ruleIdList = new LinkedList<Long>();

    @ManyToOne
    @JoinColumn(name = "calendarType", nullable = true, insertable = false)
    private Code calendarType;

    @ManyToOne
    @JoinColumn(name = "calendarSubType", nullable = true, insertable = false)
    private Code calendarSubType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(schema = "rbac", name = "calendarScheduleRuleMapping",
            joinColumns = @JoinColumn(name = "calendarId", referencedColumnName = "calendarId", insertable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "scheduleRuleId", referencedColumnName = "scheduleRuleId", insertable = false, updatable = false))
    @OrderColumn(name = "seqNum")
    @Valid
    private List<ScheduleRule> rules = new LinkedList<ScheduleRule>();

    @Transient
    private Long organizationId;

    private boolean isDeleted;

    @Transient
    private String assigned;

    @Transient
    private Boolean isDefaultCalendar;

    @Transient
    private String organizationName;


    @OneToOne(mappedBy = "userCalendar")
    private User user;

}
