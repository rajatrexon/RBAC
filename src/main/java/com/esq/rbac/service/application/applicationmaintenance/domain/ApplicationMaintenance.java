package com.esq.rbac.service.application.applicationmaintenance.domain;


import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "applicationMaintenance", schema = "rbac")
public class ApplicationMaintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "maintenanceIdGenerator")
    @TableGenerator(name = "maintenanceIdGenerator", schema = "rbac", table = "idSequence",
            pkColumnName = "idName", valueColumnName = "idValue",
            pkColumnValue = "maintenanceId", initialValue = 1, allocationSize = 10)
    @Column(name = "maintenanceId")
    private Integer maintenanceId;

    @Column(name = "childApplicationId", nullable = false)
    private Integer childApplicationId;

    @Column(name = "fromDate")
    @Convert(converter = UtcDateConverter.class)
    @NotNull
    private Date fromDate;

    @Column(name = "toDate")
    @Convert(converter = UtcDateConverter.class)
    @NotNull
    private Date toDate;

    @Column(name = "isEnabled")
    private Boolean isEnabled;

    @Column(name = "isExpired")
    private Boolean isExpired;

    @Column(name = "message")
    private String message;

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


}
