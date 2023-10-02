package com.esq.rbac.service.timezonemaster.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "rbac", name = "timeZoneMaster")
public class TimeZoneMaster implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Id
    @Column(name = "timezoneId", nullable = false, length = 200)
    private String timezoneId;

    @Column(name = "timezoneValue", nullable = false, length = 200)
    private String timezoneValue;

    @Column(name = "timeOffset", nullable = false, length = 50)
    private String timeOffset;

    @Column(name = "timeOffsetMinute", nullable = false)
    private Integer timeOffsetMinute;
}

