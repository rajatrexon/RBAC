package com.esq.rbac.service.organization.organizationattribte.repository;

import com.esq.rbac.service.organization.organizationattribte.domain.OrganizationAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationAttributeRepository extends JpaRepository<OrganizationAttribute,Integer> {
}
