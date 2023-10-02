package com.esq.rbac.service.organization.vo;

import com.esq.rbac.service.organization.domain.Organization;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OrganizationHierarchy extends Organization implements
        Comparable<OrganizationHierarchy> {
        public OrganizationHierarchy() {

        }

    public String getId() {
        return "o" + this.getOrganizationId();
    }

    public OrganizationHierarchy(Organization org) {
        super();
        this.setOrganizationId(org.getOrganizationId());
        this.setOrganizationFullName(org.getOrganizationFullName());
        this.setOrganizationName(org.getOrganizationName());
        this.setOrganizationType(org.getOrganizationType());
        this.setOrganizationSubType(org.getOrganizationSubType());
        this.setParentOrganizationId(org.getParentOrganizationId());
        this.setRemarks(org.getRemarks());
        this.setTenantId(org.getTenantId());
        this.setOrganizationTimeZone(org.getOrganizationTimeZone());
    }

    private List<Object> children = new LinkedList<Object>();
    private List<Map<String, Object>> selectBoxData = new LinkedList<Map<String, Object>>();

    public List<Object> getChildren() {
        return children;
    }

    public List<Map<String, Object>> getSelectBoxData() {
        return selectBoxData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrganizationHierarchy orgHierarchy = (OrganizationHierarchy) o;
        if (!this.getOrganizationId().equals(orgHierarchy.getOrganizationId()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.getOrganizationId().hashCode();
    }

    @Override
    public int compareTo(OrganizationHierarchy o) {
        if (this.getOrganizationName() != null) {
            return this.getOrganizationName()
                    .compareTo(o.getOrganizationName());
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OrganizationHierarchy{organizationId=").append(
                this.getOrganizationId());
        sb.append("; organizationName=").append(this.getOrganizationName());
        sb.append("; organizationFullName=").append(
                this.getOrganizationFullName());
        sb.append("; remarks=").append(this.getRemarks());
        sb.append("; organizationType=").append(this.getOrganizationType());
        sb.append("; organizationSubType=").append(
                this.getOrganizationSubType());
        sb.append("; parentOrganizationId=").append(
                this.getParentOrganizationId());
        sb.append("; tenantId=").append(this.getTenantId());
        sb.append("; children=").append(children);
        sb.append("}");
        return sb.toString();
    }
}