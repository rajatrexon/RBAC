package com.esq.rbac.service.organization.organizationlogo.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class OrganizationLogo {
//
//    private Long organizationId;
//    private String contentType;
//    private byte[] logo;
//    private Integer updatedBy;
//    private Date updatedOn;
//}




@Entity
@Table(schema = "rbac", name = "organizationLogo")
@Data
public class OrganizationLogo {

    @Id
    @Column(name = "organizationId")
    private Long organizationId;

    @Column(name = "contentType")
    private String contentType;

    @Column(name = "logo")
    private byte[] logo;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    @Convert(converter = UtcDateConverter.class)
    @Column(name = "updatedOn")
    private Date updatedOn;
}
