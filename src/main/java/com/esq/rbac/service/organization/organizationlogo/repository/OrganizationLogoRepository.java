package com.esq.rbac.service.organization.organizationlogo.repository;
import com.esq.rbac.service.organization.organizationlogo.domain.OrganizationLogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationLogoRepository extends JpaRepository<OrganizationLogo,Long> {
}
