package com.esq.rbac.service.jointables.rolepermission.repository;

import com.esq.rbac.service.jointables.rolepermission.domain.RolePermission;
import com.esq.rbac.service.jointables.rolepermission.domain.RolePermissionPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionPK> {

}
