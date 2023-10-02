package com.esq.rbac.service.group.domain;

import com.esq.fd.common.security.Restrictions;
import com.esq.rbac.service.attributes.domain.AttributesData;
import com.esq.rbac.service.calendar.domain.Calendar;
import com.esq.rbac.service.restriction.domain.Restriction;
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.esq.rbac.service.util.UtcDateConverter;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(schema = "rbac", name = "groupTable")
@Data
@Where(clause = "groupId > 0")
//Todo@Customizer(GroupVariableCustomizer.class)
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "groupIdGenerator")
    @TableGenerator(name = "groupIdGenerator", schema = "rbac", table = "idSequence",
            pkColumnName = "idName", valueColumnName = "idValue",
            pkColumnValue = "groupId", initialValue = 1, allocationSize = 1)
    @Column(name = "groupId")
    private Integer groupId;

    @Column(name = "name", nullable = false, length = 1000)
    private String name;

    @Column(name = "description", length = 128)
    private String description;

    @Column(name = "tenantId")
    private Long tenantId;

    @Column(name = "isTemplate")
    private Boolean isTemplate;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Convert(converter = UtcDateConverter.class)
    @Column(name = "createdOn")
    private Date createdOn;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    @Convert(converter = UtcDateConverter.class)
    @Column(name = "updatedOn")
    private Date updatedOn;

    @ElementCollection
    @CollectionTable(schema = "rbac", name = "label", joinColumns = @JoinColumn(name = "groupId"))
    @Column(name = "labelName")
    private List<String> labels;

    @OneToOne
    @JoinColumn(name = "restrictionId")
    private Restriction restrictions;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendarId")
    private Calendar calendar;

    @OneToMany(mappedBy = "group",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<AttributesData> attributesData;

    @ElementCollection
    @CollectionTable(schema = "rbac", name = "groupRole", joinColumns = @JoinColumn(name = "groupId"))
    @Column(name = "roleId")
    private Set<Integer> rolesIds;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<ScopeDefinition> scopeDefinitions;


    public Group() {

    }

    public Group(String name) {
        setName(name);
    }

    public Set<ScopeDefinition> getScopeDefinitions() {
        if (scopeDefinitions != null && !(scopeDefinitions instanceof TreeSet)) {
            scopeDefinitions = new TreeSet<ScopeDefinition>(scopeDefinitions);
        }
        return scopeDefinitions;
    }

    public Set<AttributesData> getAttributesData() {
        if (attributesData != null && !(attributesData instanceof TreeSet)) {
            attributesData = new TreeSet<AttributesData>(attributesData);
        }
        return attributesData;
    }
}
