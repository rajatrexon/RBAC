package com.esq.rbac.service.jointables.grouprole.repository;

import com.esq.rbac.service.jointables.grouprole.domain.GroupRole;
import com.esq.rbac.service.jointables.grouprole.domain.GroupRolePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRoleRepository extends JpaRepository<GroupRole, GroupRolePK> {

}
