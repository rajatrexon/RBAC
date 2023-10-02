package com.esq.rbac.service.jointables.operationscope.repository;

import com.esq.rbac.service.jointables.operationscope.domain.OperationScope;
import com.esq.rbac.service.jointables.operationscope.domain.OperationScopePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationScopeRepository extends JpaRepository<OperationScope, OperationScopePK> {

    @Query("select case when count(os) >= 1 then 1 else 0 end from OperationScope os where os.scopeId = :scopeId")
    int isScopeInOperationScope(@Param("scopeId") Integer scopeId);
}
