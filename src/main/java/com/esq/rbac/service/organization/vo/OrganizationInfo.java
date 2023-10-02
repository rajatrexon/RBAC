package com.esq.rbac.service.organization.vo;

import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.user.embedded.OrganizationInfoUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationInfo extends Organization implements
        Comparable<OrganizationInfo> {

    public OrganizationInfo(Organization org) {
        super();
        this.setOrganizationId(org.getOrganizationId());
        this.setOrganizationFullName(org.getOrganizationFullName());
        this.setOrganizationName(org.getOrganizationName());
        this.setOrganizationType(org.getOrganizationType());
        this.setOrganizationSubType(org.getOrganizationSubType());
        this.setParentOrganizationId(org.getParentOrganizationId());
        this.setRemarks(org.getRemarks());
        this.setTenantId(org.getTenantId());
    }

    private List<OrganizationInfo> organizations = new LinkedList<OrganizationInfo>();
    private List<OrganizationInfoUser> users = new LinkedList<OrganizationInfoUser>();

    public List<OrganizationInfo> getOrganizations() {
        return organizations;
    }

    public List<OrganizationInfoUser> getUsers() {
        return users;
    }

    @Override
    public int compareTo(OrganizationInfo o) {
        if (this.getOrganizationName() != null) {
            return this.getOrganizationName()
                    .compareTo(o.getOrganizationName());
        }
        return 0;
    }
}
