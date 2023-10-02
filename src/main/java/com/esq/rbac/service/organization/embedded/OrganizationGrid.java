package com.esq.rbac.service.organization.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationGrid {
    public String itemId;
    public String type;
    public String label;
    Long parentOrgId;
    Long tenantId;
    public Object details;
}
