package com.esq.rbac.service.contact.schedule.domain;

import com.esq.rbac.service.calendar.domain.Calendar;
import com.esq.rbac.service.contact.schedule.embedded.ScheduleRule;
import com.esq.rbac.service.contact.util.JpaDateConvertor;
import com.esq.rbac.service.validation.annotation.Mandatory;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
@XmlRootElement
@Entity
@Table(schema = "contact", name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    @Mandatory
    @Length(min = 0, max = 50)
    private String name;

    @Column(name = "time_zone", nullable = false, length = 50)
    @Mandatory
    @Length(min = 0, max = 50)
    private String timeZone;

    @Column(name = "created_time", nullable = false, updatable = false)
    @Convert(converter = JpaDateConvertor.class)
    private Date createdTime;

    @Column(name = "updated_time", nullable = false)
    @Convert(converter = JpaDateConvertor.class)
    private Date updatedTime;

    @Column(name = "tenant_id")
    private Long tenantId;


    @ElementCollection
    @OrderColumn(name = "seq_num")
    @CollectionTable(schema = "contact", name = "schedule_rule", joinColumns = @JoinColumn(name = "schedule_id"))
    private List<ScheduleRule> rules = new LinkedList<ScheduleRule>();

    // Constructors, getters, setters, and other methods...

    public static Schedule fromCalendar(Calendar userCalendar, Calendar orgCalendar) {
        Schedule schedule = new Schedule();
        schedule.setName(userCalendar != null && userCalendar.getName() != null ? userCalendar.getName() : orgCalendar != null && orgCalendar.getName() != null ? orgCalendar.getName() : "");
        schedule.setTimeZone(orgCalendar != null ? orgCalendar.getTimeZone() : null);
        List<ScheduleRule> rules = new LinkedList<ScheduleRule>();
        if (userCalendar != null && userCalendar.getRules() != null && !userCalendar.getRules().isEmpty()) {
            for (com.esq.rbac.service.schedulerule.domain.ScheduleRule rbacScheduleRule : userCalendar.getRules()) {
                ScheduleRule dspScheduleRule = new ScheduleRule();
                dspScheduleRule.setDayOfWeek(rbacScheduleRule.getDayOfWeek());
                dspScheduleRule.setDescription(rbacScheduleRule.getDescription());
                dspScheduleRule.setFromDate(rbacScheduleRule.getFromDate());
                dspScheduleRule.setHour(rbacScheduleRule.getHour());
                dspScheduleRule.setIsOpen(rbacScheduleRule.getIsOpen());
                dspScheduleRule.setToDate(rbacScheduleRule.getToDate());
                rules.add(dspScheduleRule);
            }
        }
        if (orgCalendar != null && orgCalendar.getRules() != null && !orgCalendar.getRules().isEmpty()) {
            for (com.esq.rbac.service.schedulerule.domain.ScheduleRule rbacScheduleRule : orgCalendar.getRules()) {
                ScheduleRule dspScheduleRule = new ScheduleRule();
                dspScheduleRule.setDayOfWeek(rbacScheduleRule.getDayOfWeek());
                dspScheduleRule.setDescription(rbacScheduleRule.getDescription());
                dspScheduleRule.setFromDate(rbacScheduleRule.getFromDate());
                dspScheduleRule.setHour(rbacScheduleRule.getHour());
                dspScheduleRule.setIsOpen(rbacScheduleRule.getIsOpen());
                dspScheduleRule.setToDate(rbacScheduleRule.getToDate());
                rules.add(dspScheduleRule);
            }
        }
        schedule.setRules(rules);
        return schedule;
    }

    @PrePersist
    protected void preCreate() {
        Date now = new Date();
        this.createdTime = now;
        this.updatedTime = now;
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedTime = new Date();
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        String sb = "{Schedule: {id:'" + getId() +
                "', name:'" + name +
                "', timeZone:'" + timeZone +
                "', createdTime:'" + getCreatedTime() +
                "', updatedTime:'" + getUpdatedTime() +
                "', rules:" + rules +
                "; tenantId=" + tenantId +
                "}}";
        return sb;
    }
}
