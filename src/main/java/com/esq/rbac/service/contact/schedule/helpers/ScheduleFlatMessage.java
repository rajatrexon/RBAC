package com.esq.rbac.service.contact.schedule.helpers;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
public class ScheduleFlatMessage {

    private String name;
    private String startHour;
    private String endHour;
    private String AvlCategoryName;
}