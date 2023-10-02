package com.esq.rbac.service.scope.scopedefinition.repository;

import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScopeDefinitionRepository extends JpaRepository<ScopeDefinition,Integer> {

    @Query("select count(1) from ScopeDefinition sd where sd.scopeId = :scopeId")
    Number getScopeDefCountByScopeKey(@Param("scopeId") Integer scopeId);

    @Query("select case when count(sd) >= 1 then 1 else 0 end from ScopeDefinition sd where sd.scopeId = :scopeId")
    int isScopeInScopeDefinition(Integer scopeId);

    @Query("select distinct sd from ScopeDefinition sd " +
            "join Group g on (g.groupId = sd.groupId) " +
            "join GroupRole gr on (g.groupId = gr.groupId) " +
            "join RolePermission rp on (rp.roleId = gr.roleId) " +
            "join OperationScope os on (os.operationId = rp.operationId and os.scopeId = sd.scopeId) " +
            "where gr.roleId = :roleId and g.groupId = :groupId")
    List<ScopeDefinition> getRoleScopeDefinitions(@Param("roleId") Integer roleId,@Param("groupId") Integer groupId);


    @Modifying
    @Query("delete from ScopeDefinition sd where sd.groupId=:groupId and sd.scopeId not in " +
            "(select distinct s.scopeId from Scope s " +
            "join OperationScope os on (os.scopeId = s.scopeId) " +
            "join RolePermission rp on (rp.operationId = os.operationId) " +
            "join GroupRole gr on (rp.roleId = gr.roleId) " +
            "where gr.groupId = :groupId) " +
            "and sd.scopeId not in (select s2.scopeId from Scope s2 where s2.applicationId IS NULL)")
    void deleteScopeDefinitonForRole(Integer groupId);


    @Query("select count(sd.scopeId) from ScopeDefinition sd where sd.groupId=:groupId and sd.scopeId in " +
            "(select distinct s.scopeId from Scope s " +
            "join OperationScope os on (os.scopeId = s.scopeId) " +
            "join RolePermission rp on (rp.operationId = os.operationId) " +
            "join GroupRole gr on (rp.roleId = gr.roleId) " +
            "where gr.groupId = :groupId)")
    Long getDefinedScopeCountForGroup(Integer groupId);


    @Query("SELECT sd.scopeId FROM ScopeDefinition sd WHERE sd.groupId = :groupId AND sd.scopeId NOT IN (SELECT DISTINCT s.scopeId FROM Scope s JOIN OperationScope os ON (os.scopeId = s.scopeId) JOIN RolePermission rp ON (rp.operationId = os.operationId) JOIN GroupRole gr ON (rp.roleId = gr.roleId) WHERE gr.groupId = :groupId) AND sd.scopeId NOT IN (SELECT s2.scopeId FROM Scope s2 WHERE s2.applicationId IS NULL)")
    List<Integer> getScopeDefinitionForRole(Integer groupId);
}
