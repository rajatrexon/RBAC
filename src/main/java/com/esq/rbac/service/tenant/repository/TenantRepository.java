package com.esq.rbac.service.tenant.repository;

import com.esq.rbac.service.tenant.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant,Long> {
    @Modifying
    @Query("update Tenant t set t.isDeleted = true, t.tenantName = :tenantName, t.updatedBy = :userId, t.updatedOn = :currDateTime where t.tenantId = :tenantId")
    void deleteTenantById(@Param("tenantId") Long tenantId, @Param("tenantName") String tenantName, @Param("userId") Long userId, @Param("currDateTime") Date currDateTime);

    @Query("select t from Tenant t where t.tenantName = :tName")
    Optional<Tenant> getTenantByTenantName(@Param("tName") String tenantName);

    @Query("select t from Tenant t where t.tenantType.codeValue = :tenantType")
    Optional<Tenant> getHostTenant(@Param("tenantType") String tenantType);

    @Query(nativeQuery = true,value = "select t.* from rbac.tenant t where t.tenantType=(select c.codeId from rbac.codes c where c.codeType='TENANT_TYPE' and c.codeValue='Host')"+
            " and t.tenantSubType=(select c.codeId from rbac.codes c where c.codeType='TENANT_SUBTYPE' and c.codeValue='HostOnly')")
    Tenant tenantByTypeAndSubType();

    @Query(nativeQuery = true,value = "select COUNT (1) from rbac.tenantIdentifier ti \n" +
            " where ti.tenantId != ? and ti.tenantIdentifier = ?")
    Integer isTenantIdentifierAssociationValid(Long tenantId, String tenantIdentifier);


    @Query(value = "SELECT count(1) FROM rbac.organization org " +
            "JOIN rbac.groupTable gt ON (gt.tenantId = org.tenantId) " +
            "WHERE org.organizationId = ?1 AND gt.groupId = ?2", nativeQuery = true)
    int checkTenantIdInOrgAndGroup(long organizationId, long groupId);
}
