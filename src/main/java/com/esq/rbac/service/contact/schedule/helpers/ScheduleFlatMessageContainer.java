package com.esq.rbac.service.contact.schedule.helpers;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@XmlRootElement
public class ScheduleFlatMessageContainer {

    private List<ScheduleFlatMessage> schedules = new ArrayList<ScheduleFlatMessage>();

    public List<ScheduleFlatMessage> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleFlatMessage> schedules) {
        this.schedules = schedules;
    }
}