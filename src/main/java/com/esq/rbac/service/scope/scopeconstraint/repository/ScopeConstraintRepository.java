package com.esq.rbac.service.scope.scopeconstraint.repository;

import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScopeConstraintRepository extends JpaRepository<ScopeConstraint, Integer> {

    @Query("select sc from ScopeConstraint sc where lower(sc.scopeName)=:scopeName")
   // @QueryHints(@QueryHint(name = "hibernate.cache.use_query_cache", value = "true"))
    ScopeConstraint getByScopeName(@Param("scopeName") String scopeName);

    @Query("select sc from ScopeConstraint sc where sc.applicationId=:applicationId and lower(sc.comparators)='negation'")
    ScopeConstraint getDenyScope(@Param("applicationId") Integer applicationId);

    @Query("select sc from ScopeConstraint sc where sc.scopeId=:scopeId")
    ScopeConstraint getByScopeId(@Param("scopeId") Integer scopeId);

    @Query(nativeQuery = true,
            value =  "select gr.groupId as groupId, gt.name as groupName, rp.roleId as roleId, r.name as roleName,  t.name as target, o.name as operation, sd.definition as scope"+
            "from rbac.scopeDefinition sd"+
            "join rbac.groupRole gr on (sd.groupId=gr.groupId)"+
            "join rbac.rolePermission rp on (rp.roleId=gr.roleId)"+
            "join rbac.operation o on (o.operationId = rp.operationId)"+
            "join rbac.operationScope os on (os.operationId = rp.operationId and os.scopeId=sd.scopeId)"+
    "join rbac.target t on (t.targetId = o.targetId)"+
    "join rbac.groupTable gt on (gt.groupId=gr.groupId)"+
    "join rbac.role r on (r.roleId=gr.roleId)"+
    "where gr.groupId = ?1")
    List<Object[]> getTargetDetailsByGroupId(Integer groupId);

    @Query(nativeQuery = true,
            value =  "select gr.groupId as groupId, gt.name as groupName, rp.roleId as roleId, r.name as roleName,  t.name as target, o.name as operation, sd.definition as scope"+
                    "from rbac.scopeDefinition sd"+
                    "join rbac.groupRole gr on (sd.groupId=gr.groupId)"+
                    "join rbac.rolePermission rp on (rp.roleId=gr.roleId)"+
                    "join rbac.operation o on (o.operationId = rp.operationId)"+
                    "join rbac.operationScope os on (os.operationId = rp.operationId and os.scopeId=sd.scopeId)"+
                    "join rbac.target t on (t.targetId = o.targetId)"+
                    "join rbac.groupTable gt on (gt.groupId=gr.groupId)"+
                    "join rbac.role r on (r.roleId=gr.roleId)"+
                    "where gr.groupId = ?1 and sd.scopeId = ?2")
    List<Object[]> getTargetDetailsByGroupScopeId(Integer groupId, Integer scopeId);
}
