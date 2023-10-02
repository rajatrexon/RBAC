package com.esq.rbac.service.scope.repository;

import com.esq.rbac.service.scope.domain.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ScopeRepository extends JpaRepository<Scope,Integer> {
    @Query("select distinct s.scopeId from Scope s " +
            "join OperationScope os ON (os.scopeId=s.scopeId)" +
            "join RolePermission rp ON (rp.operationId=os.operationId)" +
            "where rp.roleId IN :roleIds and s.isMandatory = TRUE")
    List<Integer> getMandatoryScopeIdsForSelectedRoles(@Param("roleIds")Set<Integer> roleIds);
    @Query("SELECT DISTINCT s.scopeId FROM Scope s WHERE s.isMandatory = TRUE AND s.applicationId IS NULL")
    List<Integer> getMandatoryScopeIdsForGlobalScopes();

    @Query("SELECT DISTINCT s.scopeId FROM Scope s WHERE s.scopeId IN :scopeIds AND s.isMandatory = TRUE")
    List<Integer> getMandatoryScopeIdsForSelectedScopeIds(@Param("scopeIds") Set<Integer> scopeIds);



    @Query("select s from Scope s where s.scopeKey = :scopeKey")
    Scope getScopeByScopeKey(@Param("scopeKey") String scopeKey);


    @Query("select s from Scope s where s.applicationId IS NULL")
    List<Scope> getGlobalScope();


    @Query("select s from Scope s where lower(s.name) = :name and s.applicationId = :applicationId")
   List<Scope> getScopeByNameAndApplicationId(@Param("name")String name ,@Param("applicationId")Integer applicationId );

    @Query("select distinct s from Scope s " +
            "join OperationScope os on (os.scopeId = s.scopeId) " +
            "join RolePermission rp on (rp.operationId = os.operationId) " +
            "join GroupRole gr on (rp.roleId = gr.roleId) " +
            "where gr.roleId in :newRoleIds " +
            "intersect " +
            "select distinct s from Scope s " +
            "join OperationScope os on (os.scopeId = s.scopeId) " +
            "join RolePermission rp on (rp.operationId = os.operationId) " +
            "join GroupRole gr on (rp.roleId = gr.roleId) " +
            "where gr.roleId in :existingRoleIds")
    List<Scope> getIsRoleTransitionAllowed(List<Integer> newRoleIds, List<Integer> existingRoleIds);


}
