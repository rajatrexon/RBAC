package com.esq.rbac.service.tenant.domain;


import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.tenant.emaddable.TenantIdentifier;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.esq.rbac.service.util.UtcDateConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "tenant", schema = "rbac")
@Data
@Where(clause = "isDeleted = false")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenantId")
    private Long tenantId;

    @Column(name = "tenantName")
    @Size(min = 1, max = 300)
    @SpecialCharValidator
    private String tenantName;

    @Column(name = "startDate")
    @Convert(converter = UtcDateConverter.class)
    private Date startDate;

    @Column(name = "billDate")
    @Convert(converter = UtcDateConverter.class)
    private Date billDate;

    @Column(name = "accountManager")
    private String accountManager;

    @Column(name = "createdBy")
    private Long createdBy;

    @Column(name = "createdOn")
    @Convert(converter = UtcDateConverter.class)
    private Date createdOn;

    @Column(name = "updatedBy")
    private Long updatedBy;

    @Column(name = "updatedOn")
    @Convert(converter = UtcDateConverter.class)
    private Date updatedOn;

    @Column(name = "remarks", length = 4000, nullable = true)
    private String remarks;

    @Column(name = "tenantURL", length = 1000, nullable = false)
    private String tenantURL;

    @Column(name = "isDeleted")
    private boolean isDeleted;

    @Column(name = "twoFactorAuthEnabled")
    private Boolean twoFactorAuthEnabled;
    /* Start
     * Added By Fazia 19-Dec-2018
     * This Flag was added to enable or disable the maker checker feature for the tenant*/
    @Column(name = "makerCheckerEnabled")
    private Boolean makerCheckerEnabled;

    @ManyToOne
    @JoinColumn(name = "tenantType", nullable = false, insertable = false,referencedColumnName = "codeId")
    private Code tenantType;

    @ManyToOne
    @JoinColumn(name = "tenantSubType", nullable = false, insertable = false,referencedColumnName = "codeId")
    private Code tenantSubType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "rbac", name = "tenantIdentifier", joinColumns = @JoinColumn(name = "tenantId"))
    private Set<TenantIdentifier> identifiers;

    // Constructors, getters, and setters

    public Boolean isTwoFactorAuthEnabled() {
        return twoFactorAuthEnabled;
    }

    public void setTwoFactorAuthEnabled(Boolean twoFactorAuthEnabled) {
        this.twoFactorAuthEnabled = twoFactorAuthEnabled;
    }

    public Boolean isMakerCheckerEnabled() {
        return makerCheckerEnabled;
    }

    public void setMakerCheckerEnabled(Boolean makerCheckerEnabled) {
        this.makerCheckerEnabled = makerCheckerEnabled;
    }


    @JsonIgnore
    public boolean isDeleted() {
        return isDeleted;
    }


    public Set<TenantIdentifier> getIdentifiers() {
        if (identifiers != null && !(identifiers instanceof TreeSet)) {
            identifiers = new TreeSet<TenantIdentifier>(identifiers);
        }
        return identifiers;
    }
}
