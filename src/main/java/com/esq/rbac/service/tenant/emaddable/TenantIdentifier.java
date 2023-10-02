package com.esq.rbac.service.tenant.emaddable;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.Date;

@Embeddable
@Data
public class TenantIdentifier implements Comparable<TenantIdentifier>{

    @Column(name = "tenantIdentifier")
    private String tenantIdentifier;

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

    @Override
    public int compareTo( TenantIdentifier o) {
        if (this.tenantIdentifier != null) {
            return this.tenantIdentifier.compareTo(o.tenantIdentifier);
        }
        return 0;
    }


    // Constructors, getters, and setters
}

