package com.esq.rbac.service.tenant.util;

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.user.domain.User;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

public interface TenantStructureGenerator extends ApplicationContextAware {
    List<Organization> createOrganizationForTenants(Tenant tenant);
    List<Group> createGroupsForOrganization(Organization organization);
    List<User> createUsersForOrganizationAndGroup(Organization organization, Group group);
    ScopeDefinition createDefaultScopeForTenant(Long tenantId);
    Group handleGroupCreation(Group group);
    Group handleGroupUpdation(Group existingGroup, Group group);
    Tenant handleTenantCreation(Tenant tenant, AuditLogInfo auditLogInfo);
    Tenant handleTenantUpdation(Tenant existingTenant, Tenant tenant, AuditLogInfo auditLogInfo);
}
