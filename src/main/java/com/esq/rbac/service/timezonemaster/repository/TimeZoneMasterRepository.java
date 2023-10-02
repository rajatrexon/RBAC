package com.esq.rbac.service.timezonemaster.repository;

import com.esq.rbac.service.timezonemaster.domain.TimeZoneMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeZoneMasterRepository extends JpaRepository<TimeZoneMaster,String> {

//    @Query(value = "SELECT tz.timezoneValue FROM TimeZoneMaster tz ORDER BY tz.timeOffset ASC", nativeQuery = true)
    @Query("SELECT tz.timezoneValue FROM TimeZoneMaster tz ORDER BY tz.timeOffset ASC")
    public List<String> getTimeZoneDisplayNames();

//    @Query(value = "SELECT tz FROM TimeZoneMaster tz ORDER BY tz.timeOffset ASC", nativeQuery = true)
    @Query("SELECT tz FROM TimeZoneMaster tz ORDER BY tz.timeOffset ASC")
    public List<TimeZoneMaster> getTimeZones();

    @Query("SELECT tz.timeOffset FROM TimeZoneMaster tz WHERE tz.timezoneValue = :timeZone ORDER BY tz.timeOffset ASC")
    public String getOffsetOfTimeZone(@Param("timeZone") String timeZone);
}
