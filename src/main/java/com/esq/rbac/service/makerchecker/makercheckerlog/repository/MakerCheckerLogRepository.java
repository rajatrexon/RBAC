package com.esq.rbac.service.makerchecker.makercheckerlog.repository;

import com.esq.rbac.service.makerchecker.makercheckerlog.domain.MakerCheckerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MakerCheckerLogRepository extends JpaRepository<MakerCheckerLog, Long> {

    List<MakerCheckerLog> findByMakerCheckerId(Integer makerCheckerId);

    void deleteByMakerCheckerId(Long makerCheckerId);

    @Query("SELECT m FROM MakerCheckerLog m WHERE m.makerCheckerId = :makerCheckerId ORDER BY m.transactionOn")
    List<MakerCheckerLog> getHistoryByMakerCheckerId(@Param("makerCheckerId") Long makerCheckerId);

    @Modifying
    @Query("DELETE FROM MakerCheckerLog m WHERE m.makerCheckerId = :makerCheckerId")
    void deleteHistoryByMakerCheckerId(@Param("makerCheckerId") Long makerCheckerId);
}
