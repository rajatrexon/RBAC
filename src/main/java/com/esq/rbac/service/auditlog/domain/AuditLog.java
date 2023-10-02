package com.esq.rbac.service.auditlog.domain;


import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "auditLog", schema = "rbac")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auditLogId")
    private Integer auditLogId;

    @Column(name = "createdTime", nullable = false)
    @Convert(converter = UtcDateConverter.class)
    private Date createdTime;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "applicationId", nullable = false)
    private Integer applicationId;

    @Column(name = "targetId", nullable = false)
    private Integer targetId;

    @Column(name = "operationId", nullable = false)
    private Integer operationId;

    @Column(name = "queryField1", length = 30)
    private String queryField1;

    @Column(name = "queryField2", length = 30)
    private String queryField2;

    @Column(name = "isAlertable", nullable = false)
    private Boolean isAlertable;

    @Column(name = "isSuccess", nullable = false)
    private Boolean isSuccess;

    @Column(name = "isCompressed", nullable = false)
    private Boolean isCompressed;

    @Column(name = "logBuffer", length = 2000)
    private String logBuffer;

    // Getters and Setters
}

