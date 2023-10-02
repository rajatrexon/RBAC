package com.esq.rbac.service.tenantattribute.repository;

import com.esq.rbac.service.tenantattribute.domain.TenantAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantAttributeRepository extends JpaRepository<TenantAttribute,Integer> {
    @Modifying
    @Query("delete from TenantAttribute t where t.attributeId=:attributeId")
    void deleteTenantAttributeByAttributeId(@Param("attributeId") Integer attributeId);

    @Query("select t from TenantAttribute t where t.applicationId = (select a.application.applicationId from ChildApplication a where a.appKey = :appKey) and t.tenantId IN (select t1.tenantId from Tenant t1 where t1.isDeleted = false)")
    List<TenantAttribute> getTenantAttributesByAppKey(@Param("appKey")String appKey);

    @Query("select t from TenantAttribute t where t.applicationId = (select a.application.applicationId from ChildApplication a where a.appKey = :appKey) and t.tenantId IN (select t1.tenantId from Tenant t1 where t1.tenantId = :tenantId and t1.isDeleted = false)")
    List<TenantAttribute> getTenantAttributesByTenantIdAndAppKey(@Param("tenantId")Long tenantId, @Param("appKey")String appKey);
}
