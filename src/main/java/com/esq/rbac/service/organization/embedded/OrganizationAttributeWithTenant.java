package com.esq.rbac.service.organization.embedded;

import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.organization.organizationattribte.domain.OrganizationAttribute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationAttributeWithTenant {

    private Long organizationId;
    private String organizationName;
    private String organizationFullName;
    private String remarks;
    private Code organizationType;
    private Code organizationSubType;
    private Long parentOrganizationId;
    private String organizationURL;
    private Long tenantId;
    private Integer createdBy;
    private Date createdOn;
    private Integer updatedBy;
    private Date updatedOn;
    private boolean isDeleted;
    private Boolean isShared;
    private String appKey;
    private List<OrganizationAttribute> organizationAttributes;
}
