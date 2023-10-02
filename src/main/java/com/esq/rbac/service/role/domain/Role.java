package com.esq.rbac.service.role.domain;

import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "role",schema="rbac")
@Data
@Where(clause = "roleId > 0")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "roleIdGenerator")
    @TableGenerator(name = "roleIdGenerator", schema = "rbac", table = "idSequence",
            pkColumnName = "idName", valueColumnName = "idValue",
            pkColumnValue = "roleId", initialValue = 1,
            allocationSize = 10)
    @Column(name = "roleId")
    private Integer roleId;

    @Column(name = "applicationId", nullable = false)
    private Integer applicationId;

    @Column(name = "name", nullable = false, length = 32)
    @SpecialCharValidator
    private String name;

    @Column(name = "description", nullable = true, length = 500)
    private String description;

    @ElementCollection
    @CollectionTable(schema = "rbac", name = "label", joinColumns = @JoinColumn(name = "roleId"))
    @Column(name = "labelName")
    private Set<String> labels;

    @ElementCollection
    @CollectionTable(schema = "rbac", name = "rolePermission", joinColumns = @JoinColumn(name = "roleId"))
    @Column(name = "operationId")
    private Set<Integer> operationIds;

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


    public Role() {
        // empty
    }


    public Role(String name, Application application) {
        this.name = name;
        this.applicationId = application.getApplicationId();
    }

    // Add getters and setters, and any additional methods
}

