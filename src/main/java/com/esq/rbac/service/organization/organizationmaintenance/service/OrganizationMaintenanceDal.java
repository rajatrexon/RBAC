package com.esq.rbac.service.organization.organizationmaintenance.service;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.vo.OrganizationHierarchy;
import com.esq.rbac.service.organization.embedded.OrganizationGrid;
import com.esq.rbac.service.util.dal.Options;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OrganizationMaintenanceDal extends BaseDal {

    Organization create(
            Organization OrganizationMaintenance, int userId, String target, String operation);

    Organization update(
            Organization OrganizationMaintenance, int userId, String target, String operation);

    void deleteById(long organizationId, int userId);

    List<Organization> getList(Options options);

    int getCount(Options options);

    Organization getById(long organizationId);

    //void deleteByChildApplicationId(int childApplicationId);
    List<Map<String,Object>> getCustomOrganizationInfo(Options options);

    List<Map<String,Object>> searchCustomOrganizationInfo(Options options);

    Organization getOrganizationByOrganizationName(String orgName, Long tenantId);
    List<Map<String,Object>> getOrganizationIdNamesDetails(Options options);
    List<Map<String,Object>> getOrganizationIdNames(Options options);
    List<Map<String,Object>> getOrganizationIdNamesWithScope(Options options);

    Map<Long, Set<OrganizationHierarchy>> getOrganizationHierarchy(Options options);

    List<Map<String, Object>> getOrganizationInfo(Options options);
    List<Map<String,Object>> getOrganizationByTenantId(Options options);


    /******* RBAC-1656 Start ******/
    List<OrganizationGrid> getOrganizationHierarchyGridView(Options options);
    Map<String, List<Map<String, Object>>> getSearchBoxData(Options options);
    Map<String, Integer> getNodesCount(Options options);
    OrganizationGrid getTenantOrganizationGrid(Options options);
    Map<String, OrganizationGrid> getSearchData(Options options);
    Integer getBatchSizeForData();
    /******* RBAC-1656 END ******/
}

