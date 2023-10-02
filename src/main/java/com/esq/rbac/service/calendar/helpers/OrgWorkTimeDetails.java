package com.esq.rbac.service.calendar.helpers;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
public class OrgWorkTimeDetails {

    private LocalTime startTime;
    private LocalTime endTime;

}
