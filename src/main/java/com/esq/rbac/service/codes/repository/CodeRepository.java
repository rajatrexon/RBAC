package com.esq.rbac.service.codes.repository;

import com.esq.rbac.service.codes.domain.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeRepository extends JpaRepository<Code,Long> {
    @Query("SELECT c FROM Code c JOIN ApplicationCodesMap ac ON c.codeId = ac.codeId JOIN Application a ON ac.applicationId = a.applicationId WHERE c.codeType = :codeType AND a.name = :applicationName")
    List<Code> getByCodeTypeAndApplication(@Param("codeType") String codeType, @Param("applicationName") String applicationName);

    @Query("SELECT c FROM Code c JOIN ApplicationCodesMap ac ON c.codeId = ac.codeId JOIN Application a ON ac.applicationId = a.applicationId WHERE a.name = :applicationName")
    List<Code> getByApplication(@Param("applicationName") String applicationName);

    @Query("select c from Code c where c.scopeData is not null")
    List<Code> getAllCodesWithScope();

}
