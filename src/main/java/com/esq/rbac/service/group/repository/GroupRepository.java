package com.esq.rbac.service.group.repository;

import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.scope.domain.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group,Integer> {
    @Query("delete from Group g where g.groupId = :groupId")
    void deleteGroupById(@Param("groupId") Integer groupId);


    @Query("select g from Group g where g.name=:name")
    List<Group> getGroupByName(@Param("name") String name);

    @Query("delete from Group g where g.name = :name")
    void deleteGroupByName(@Param("name")String name);

    @Query("select g.name from Group g")
    List<String> getAllGroupNames();

    @Query(nativeQuery = true, value = "select os.scopeId\n" +
            "                from rbac.groupRole gr\n" +
            "                join rbac.rolePermission ro on (ro.roleId = gr.roleId)\n" +
            "                join rbac.operationScope os on (os.operationId = ro.operationId)\n" +
            "                where gr.groupId = ?1")
    List<Long> getGroupScopeIdData( Integer groupId);

    @Query(nativeQuery = true, value = "SELECT ut.userId FROM rbac.userTable ut WHERE ut.groupId = :groupId")
    List<Integer> getUserIdsFromUserTableByGroup(@Param("groupId") Integer groupId);

    @Query(nativeQuery = true, value = "SELECT os.scopeId FROM rbac.groupRole gr " +
            "JOIN rbac.rolePermission ro ON ro.roleId = gr.roleId " +
            "JOIN rbac.operationScope os ON os.operationId = ro.operationId " +
            "WHERE gr.groupId = :groupId")
    List<Integer> getGroupScopeIds(@Param("groupId") Integer groupId);
    @Query("select count(1) from Organization o where o.tenantId = :tenantId")
    Long getGroupByTenantId(@Param("tenantId") Long tenantId);


    @Query(nativeQuery = true, value = "select gt.groupId from rbac.groupTable gt join rbac.groupRole gr on (gr.groupId = gt.groupId) where gr.roleId = ?1")
    List<Integer> getGroupIdsFromGroupRole(@Param("roleId") Integer roleId);


    @Query("SELECT DISTINCT s FROM Scope s " +
            "JOIN OperationScope os ON (os.scopeId = s.scopeId) " +
            "JOIN RolePermission rp ON (rp.operationId = os.operationId) " +
            "JOIN GroupRole gr ON (rp.roleId = gr.roleId) " +
            "WHERE gr.roleId IN :roleIds " +
            "ORDER BY s.name")
    List<Scope> getGroupRoleScopeIds(@Param("roleIds") List<Integer> roleIds);






}
