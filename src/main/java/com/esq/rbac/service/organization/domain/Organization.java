package com.esq.rbac.service.organization.domain;

import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.Date;

@Entity
@Table(name = "Organization", schema = "rbac")
@Data
@Where(clause = "isDeleted = false")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "organizationIdGenerator")
    @TableGenerator(name = "organizationIdGenerator", schema = "rbac", table = "idSequence",
            pkColumnName = "idName", valueColumnName = "idValue",
            pkColumnValue = "organizationId", initialValue = 1,
            allocationSize = 10)
    @Column(name = "organizationId")
    private Long organizationId;

    @Column(name = "organizationName", length = 1000, nullable = false)
    @SpecialCharValidator
    private String organizationName;

    @Column(name = "organizationFullName", length = 4000, nullable = true)
    @SpecialCharValidator
    private String organizationFullName;

    @Column(name = "remarks", length = 4000, nullable = true)
    @SpecialCharValidator
    private String remarks;

    @Column(name = "parentOrganizationId", length = 1000, nullable = true)
    private Long parentOrganizationId;

    @Column(name = "organizationURL", length = 1000, nullable = false)
    private String organizationURL;

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

    @Column(name = "isDeleted")
    private Boolean isDeleted;

    @Column(name = "isShared")
    private Boolean isShared;

    @Column(name = "organizationTimeZone")
    private String organizationTimeZone;

    @ManyToOne
    @JoinColumn(name = "organizationType", nullable = true, insertable = false)
    private Code organizationType;

    @ManyToOne
    @JoinColumn(name = "organizationSubType", nullable = true, insertable = false)
    private Code organizationSubType;

    private Long tenantId;


    // Constructors, getters, and setters
}
