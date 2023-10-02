package com.esq.rbac.service.masterattributes.domain;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(schema = "rbac", name = "master_attributes")
@Data
public class MasterAttributes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attributeId;

    private String attributeName;

    private Boolean isUser;

    private Boolean isGroup;

    private Integer scopeConstraintId;

    private Boolean isUpdateAllowed;

    private String description;

    private String attributeType;

    private Boolean isEnabled;

    private String additionalData;

    private Boolean isUserScopeable;

    private Boolean isGroupScopeable;

    private String scopeData;

    // Add getters and setters for each field

    // Constructors, methods, etc.
}

