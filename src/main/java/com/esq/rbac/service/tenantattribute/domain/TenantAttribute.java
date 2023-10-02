package com.esq.rbac.service.tenantattribute.domain;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(schema = "rbac", name = "tenantAttributes")
@Data
public class TenantAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attributeId;

    private String attributeName;

    private String attributeValue;

    private Integer applicationId;

    private Long tenantId;

    private Long codeId;

    @Transient
    private String codeName;

    @Transient
    private String appKey;

    // Add getters and setters for each field

    // Constructors, methods, etc.
}
