package com.esq.rbac.service.tenant.tenantlog.repository;

import com.esq.rbac.service.tenant.tenantlog.domain.TenantLogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantLogRepository extends JpaRepository<TenantLogo,Long> {
}
