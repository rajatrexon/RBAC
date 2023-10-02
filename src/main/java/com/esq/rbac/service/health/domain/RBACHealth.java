package com.esq.rbac.service.health.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "health", schema = "rbac")
public class RBACHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "healthId")
    private Long rbacHealthId;

    @Column(name = "componentName")
    private String componentName;

    @Column(name = "healthUpdateTime")
    @Convert(converter = UtcDateConverter.class)
    private Date healthUpdateTime;

    @Column(name = "updateTime")
    @Convert(converter = UtcDateConverter.class)
    private Date updateTime;

}
