package com.esq.rbac.service.role.targetsubdomain.domain;

import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.role.operationsubdomain.domain.Operation;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

//TODO
@Entity
@Table(name = "target", schema = "rbac")
@Getter
@Setter
@Data
@NoArgsConstructor
//@Where(clause = "this.targetKey not in (:multiTenantTargetIgnoreList)")
public class Target implements Comparable<Target>{

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "targetIdGenerator")
    @TableGenerator(name = "targetIdGenerator", schema = "rbac", table = "idSequence",
            pkColumnName = "idName", valueColumnName = "idValue",
            pkColumnValue = "targetId", initialValue = 1, allocationSize = 10)
    @Column(name = "targetId")
    private Integer targetId;

    @ManyToOne
    @JoinColumn(name = "applicationId")
    @JsonBackReference("ApplicationTargets")
    private Application application;

    @Column(name = "name", nullable = false, length = 32)
    @SpecialCharValidator
    private String name;

    @Column(name = "targetKey", nullable = false,length = 100)
    @SpecialCharValidator
    private String targetKey;

    @Column(name = "description", nullable = true, length = 128)
    private String description;

    @ElementCollection
    @CollectionTable(name = "label", schema = "rbac", joinColumns = @JoinColumn(name = "targetId"))
    @Column(name = "labelName")
    private Set<String> labels;

    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL)
    @Valid
    @JsonManagedReference("TargetOperations")
    private Set<Operation> operations;


    public Target(String name, Application application) {
        setName(name);
        setApplication(application);
    }


    public  void setApplication(Application application) {
        // set new application
        this.application = application;

        // add this target to newly set application
        if (this.application != null) {
            if (this.application.getTargets() == null) {
                this.application.setTargets(new HashSet<Target>());
            }
            if (!this.application.getTargets().contains(this)) {
                this.application.getTargets().add(this);
            }
        }
    }

    public Set<Operation> getOperations() {
        if (operations!=null && !(operations instanceof TreeSet)) {
            operations = new TreeSet<Operation>(operations);
        }
        return operations;
    }

    @Override
    public int compareTo(Target o) {
        if (this.name != null) {
            return this.name.compareTo(o.name);
        }
        return 0;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Target{targetId=").append(targetId);
        sb.append("; application=").append(application != null ? Integer.toHexString(application.hashCode()) : "null");
        sb.append("; name=").append(name);
        sb.append("; targetKey=").append(targetKey);
        sb.append("; description=").append(description);
        sb.append("; labels=").append(labels);
        sb.append("; operations=").append(operations);
        sb.append('}');
        return sb.toString();
    }


}

