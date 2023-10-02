package com.esq.rbac.service.masterattributes.repository;

import com.esq.rbac.service.masterattributes.domain.MasterAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterAttributesRepository extends JpaRepository<MasterAttributes,Integer> {
    @Query("select m from MasterAttributes m where m.isEnabled = TRUE")
    List<MasterAttributes> getAllEnabledMasterAttributes();

    @Query("SELECT COUNT(attributesData.group) FROM AttributesData attributesData WHERE attributesData.attributeId IN " +
            "(SELECT masterAttributes.attributeId FROM MasterAttributes masterAttributes " +
            "WHERE masterAttributes.attributeName IN ('dispatchParty', 'rbacDispatchPartyNameAsGroupName')) " +
            "AND attributesData.valueReferenceId = :valueReferenceId AND attributesData.group IS NOT NULL")
    Integer getAssociatedRBACGroups(@Param("valueReferenceId") String valueReferenceId);
}
