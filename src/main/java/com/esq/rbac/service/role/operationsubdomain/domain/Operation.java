package com.esq.rbac.service.role.operationsubdomain.domain;

import com.esq.rbac.service.role.targetsubdomain.domain.Target;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "operation", schema = "rbac")
@Data
@NoArgsConstructor
public class Operation implements Comparable<Operation>{

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "operationIdGenerator")
    @TableGenerator(name = "operationIdGenerator", schema = "rbac", table = "idSequence",
            pkColumnName = "idName", valueColumnName = "idValue",
            pkColumnValue = "operationId", initialValue = 1, allocationSize = 10)
    @Column(name = "operationId")
    private Integer operationId;

    @ManyToOne
    @JoinColumn(name = "targetId", nullable = false)
    @JsonBackReference("TargetOperations")
    private Target target;

    @Column(name = "name", nullable = false, length = 32)
    @Size(min = 1, max = 32)
    private String name;

    @Column(name = "operationKey", nullable = false)
    @Size(min = 1, max = 100)
    @SpecialCharValidator
    private String operationKey;

    @Column(name = "description", nullable = true, length = 128)
    private String description;

    @ElementCollection
    @CollectionTable(name = "label", schema = "rbac", joinColumns = @JoinColumn(name = "operationId"))
    @Column(name = "labelName")
    private Set<String> labels;

    @ElementCollection
    @CollectionTable(name = "operationScope", schema = "rbac", joinColumns = @JoinColumn(name = "operationId"))
    @Column(name = "scopeId")
    private Set<Integer> scopeIds;

    public Operation(String name, Target target) {
        setName(name);
        setTarget(target);
    }

    @Override
    public int compareTo(Operation o) {
        if (this.name != null) {
            return this.name.compareTo(o.name);
        }
        return 0;
    }

    public  void setTarget(Target target) {
        // set new target
        this.target = target;

        // add this operation to new target
        if (this.target != null) {
            if (this.target.getOperations() == null) {
                this.target.setOperations(new HashSet<Operation>());
            }
            if (!this.target.getOperations().contains(this)) {
                this.target.getOperations().add(this);
            }
        }
    }

    // Constructors, getters, and setters
}

