package com.esq.rbac.service.ivrpasswordhistory.repository;

import com.esq.rbac.service.ivrpasswordhistory.domain.IVRPasswordHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IVRPasswordHistoryRepository extends JpaRepository<IVRPasswordHistory, Integer> {

    @Query(value = "SELECT h FROM User u JOIN IVRPasswordHistory h ON (h.userId = u.userId) WHERE u.userName = :userName ORDER BY h.setTime DESC")
    List<IVRPasswordHistory> getIVRPasswordHistoryByUserName(@Param("userName") String userName);

    @Query("SELECT h FROM IVRPasswordHistory h WHERE h.userId = :userId ORDER BY h.setTime DESC")
    Page<IVRPasswordHistory> getIVRPasswordHistoryByUserId(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT h FROM User u JOIN IVRPasswordHistory h ON (h.userId = u.userId) WHERE u.ivrUserId = :ivrUserId ORDER BY h.setTime DESC")
    List<IVRPasswordHistory> getIVRPasswordHistoryByIVRUserId(String ivrUserId);
}
