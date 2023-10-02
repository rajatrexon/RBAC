package com.esq.rbac.service.variable.repository;

import com.esq.rbac.service.variable.domain.Variable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface VariableRepository extends JpaRepository<Variable,Integer> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Variable v WHERE v.childApplicationId IS NOT NULL AND v.childApplicationId NOT IN" +
            " (SELECT c.childApplicationId FROM ChildApplication c)")
    void deleteVariablesNotInChildApplications();


    @Query(value = "select 'application' as variableType, v.variableName, v.variableValue, v.applicationId from rbac.variable v " +
            "join rbac.application a on (v.applicationid=a.applicationId) " +
            "where v.userId is NULL and v.groupId is NULL and a.name = :appName " +
            "union " +
            "select 'group' as variableType, v.variableName, v.variableValue, v.applicationId from rbac.variable v " +
            "join rbac.groupTable g on (v.groupId=g.groupId) " +
            "join rbac.userTable u on (u.groupId=g.groupId) " +
            "where v.applicationId is NULL and v.userId is NULL and u.userName = :userName " +
            "union " +
            "select 'user' as variableType, v.variableName, v.variableValue, v.applicationId from rbac.variable v " +
            "join rbac.application a on (v.applicationid is NULL) " +
            "join rbac.userTable u on (v.userId=u.userId) " +
            "where v.applicationId is NULL and u.userName = :userName " +
            "union " +
            "select 'user' as variableType, v.variableName, v.variableValue, v.applicationId from rbac.variable v " +
            "join rbac.application a on (v.applicationid=a.applicationId) " +
            "join rbac.userTable u on (v.userId=u.userId) " +
            "where a.name = :appName and u.userName = :userName " +
            "order by applicationId",
            nativeQuery = true)
    List<Object[]> getUserVariables(@Param("appName") String appName, @Param("userName") String userName);

    @Query("select distinct(v.variableName) from Variable v where v.userId is NOT NULL")
    List<String> getVariableNameWithUserIdNotNull();

    @Query("select distinct(v.variableName) from Variable v where v.groupId is NOT NULL")
    List<String> getVariableNameWithGroupIdNotNull();

}
