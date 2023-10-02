package com.esq.rbac.service.role.operationsubdomain.repository;

import com.esq.rbac.service.role.operationsubdomain.domain.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OperationRepository extends JpaRepository<Operation,Integer> {
    @Modifying
    @Query("delete from Operation o where o.operationId = :operationId")
    void deleteOperationById(@Param("operationId") Integer operationId);


    @Query("select case when count(os) >= 1 then 1 else 0 end from OperationScope os where os.scopeId = :scopeId")
    int isScopeInOperationScope(@Param("scopeId") Integer scopeId);


    @Query("select COUNT(DISTINCT o.operationId) from Operation o " +
            "join o.target t where t.application.applicationId = :applicationId and t = o.target " +
            "and o.operationId in :operationIds")
    Long areOperationsInApplication(@Param("applicationId") Integer applicationId, @Param("operationIds")Set<Integer> operationIds);

}
