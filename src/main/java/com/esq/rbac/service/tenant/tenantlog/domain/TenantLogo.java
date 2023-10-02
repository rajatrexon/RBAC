package com.esq.rbac.service.tenant.tenantlog.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(schema = "rbac", name = "tenantLogo")
@Data
public class TenantLogo implements Serializable {
    @Id
    private Long tenantId;
    private String contentType;
    @Lob
    private byte[] logo;
    private Integer updatedBy;
    @Convert(converter = UtcDateConverter.class)
    private Date updatedOn;
}
