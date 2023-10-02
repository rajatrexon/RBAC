package com.esq.rbac.service.auditlog.repository;

import com.esq.rbac.service.auditlog.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId ORDER BY a.createdTime DESC")
    Page<AuditLog> findAuditLogByUserId(@Param("userId") Integer userId, Pageable pageable);

    @Query("select a.auditLogId, a.createdTime, a.applicationId, a.targetId, a.operationId from AuditLog a where a.userId = :userId order by a.createdTime desc")
    List<Object[]> findAuditLogHistoryFeedByUserId(Integer userId, Pageable pageable);
}

