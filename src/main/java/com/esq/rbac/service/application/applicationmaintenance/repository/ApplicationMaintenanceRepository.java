package com.esq.rbac.service.application.applicationmaintenance.repository;

import com.esq.rbac.service.application.applicationmaintenance.domain.ApplicationMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface ApplicationMaintenanceRepository extends JpaRepository<ApplicationMaintenance,Integer> {
    @Modifying
    @Query("delete from ApplicationMaintenance a where a.maintenanceId = :maintenanceId")
    void deleteScheduleById(@Param("maintenanceId") Integer maintenanceId);

    @Query("select a from ApplicationMaintenance a where a.fromDate <= :currentTime and a.isExpired = false and a.isEnabled = true order by a.fromDate")
    List<ApplicationMaintenance> getAppsUnderMaintenance(@Param("currentTime") Date currentTime);

    @Modifying
    @Query("update ApplicationMaintenance a set a.isExpired = true where a.toDate <= :currentTime and a.isExpired = false")
    int updateExpiredFlag(@Param("currentTime") Date currentTime);

    @Query("select case when min(a.fromDate) <= min(a.toDate) then min(a.fromDate) else min(a.toDate) end from ApplicationMaintenance a where a.isExpired = false and a.isEnabled = true")
    Date getMinimumRefreshTime();

    @Query("select case when a.fromDate > :currentTime then a.fromDate else min(a.toDate) end as resultDate from ApplicationMaintenance a where a.isExpired = false and a.isEnabled = true and a.toDate >= :toDateParam group by a.fromDate order by resultDate")
    List<Date> getNextRefreshTime(@Param("currentTime") Date currentTime, @Param("toDateParam") Date toDateParam);

    @Query("select count(1) from ApplicationMaintenance am where am.childApplicationId = :childApplicationId and am.isExpired = false and ((am.fromDate <= :fromDate and am.toDate >= :fromDate) or (am.fromDate <= :toDate and am.toDate >= :toDate))")
    int checkForDuplicate(@Param("childApplicationId") Integer childApplicationId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query("select count(1) from ApplicationMaintenance am where am.childApplicationId = :childApplicationId and am.isExpired = false and ((am.fromDate <= :fromDate and am.toDate >= :fromDate) or (am.fromDate <= :toDate and am.toDate >= :toDate)) and am.maintenanceId != :maintenanceId")
    int checkForDuplicateWithMaintenanceId(@Param("childApplicationId") Integer childApplicationId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate, @Param("maintenanceId") Integer maintenanceId);

    @Modifying
    @Query("delete from ApplicationMaintenance a where a.childApplicationId = :childApplicationId")
    int deleteByChildApplicationId(Integer childApplicationId);
}
