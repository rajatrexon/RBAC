package com.esq.rbac.service.contact.sla.domain;

import com.esq.rbac.service.contact.schedule.domain.Schedule;
import com.esq.rbac.service.validation.annotation.Length;
import com.esq.rbac.service.validation.annotation.Mandatory;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sla", schema = "contact")
@XmlRootElement
public class SLA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name", nullable = false, length = 256)
    @Mandatory
    @Length(min = 0, max = 50)
    private String name;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "schedule_id", nullable = false)
    private long scheduleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private Schedule schedule;

    @Column(name = "basevalue_mins", nullable = false)
    private int baseValueMinutes;

    @Column(name = "gracevalue_mins", nullable = false)
    private int graceValueMinutes;

    @Column(name = "tenant_id")
    private long tenantId;


    public int getBaseValueMinutes() {
        return baseValueMinutes;
    }

    public void setBaseValueMinutes(int baseValueMinutes) {
        this.baseValueMinutes = baseValueMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getGraceValueMinutes() {
        return graceValueMinutes;
    }

    public void setGraceValueMinutes(int graceValueMinutes) {
        this.graceValueMinutes = graceValueMinutes;
    }

//    public long getId() {
//        return id;
//    }
//
//    public void setId(long id) {
//        this.id = id;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{SLA: {id:'").append(getId());
        sb.append("', name:'").append(name);
        sb.append("', description:'").append(description);
        sb.append("', scheduleId:'").append(scheduleId);
        if (schedule != null) {
            sb.append("', schedule:").append(schedule);
        }
        sb.append(", baseValueMinutes:'").append(baseValueMinutes);
        sb.append("', graceValueMinutes:'").append(graceValueMinutes);
        sb.append("', tenantId:'").append(tenantId);
        sb.append("'}}");
        return sb.toString();
    }
}