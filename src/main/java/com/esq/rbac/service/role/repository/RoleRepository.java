package com.esq.rbac.service.role.repository;

import com.esq.rbac.service.role.domain.Role;
import com.esq.rbac.service.scope.domain.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {
    List<Role> findByNameAndApplicationId(String name, Integer applicationId);


    @Modifying
    @Query("delete from Role r where r.name = :name")
    void deleteRoleByName(@Param("name") String name);


    @Query("select r.name from Role r")
    List<String> getAllRoleNames();

    @Query("select r.name from Role r where r.applicationId = :applicationId")
    List<String> getAllRoleNamesByApplicationId(Integer applicationId);


    @Query(nativeQuery = true, value = "select os.scopeId " +
            "from rbac.rolePermission ro " +
            "join rbac.operationScope os on (os.operationId = ro.operationId) " +
            "where ro.roleId = ?1")
    List<Integer> getRoleScopeIds(int roleId);


    @Query(nativeQuery = true,value = "select r.roleId,r.name,gr.groupId from rbac.role r JOIN rbac.groupRole gr ON r.roleId = gr.roleId")
    List<Object[]> getRolesByGroup();



}
