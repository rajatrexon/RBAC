package com.esq.rbac.service.calendar.repository;

import com.esq.rbac.service.calendar.domain.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar,Integer> {
//    @Modifying
//    @Query("update Calendar c set c.isDeleted = true, c.updatedBy = :userId, c.updatedOn = :currDateTime where c.calendarId = :calendarId")
//    void deleteCalendarById(@Param("calendarId") Long calendarId,@Param("userId") Long userId,@Param("currDateTime") Date currDateTime);


    @Modifying
    @Query("update Calendar c set c.isDeleted = true, c.updatedBy = :userId, c.updatedOn = :currDateTime where c.calendarId = :calendarId")
    void deleteCalendarById(@Param("calendarId") Long calendarId, @Param("userId") Integer userId, @Param("currDateTime") Date currDateTime);

    @Query("select c from Calendar c where c.name=:calName")
    Calendar getCalendarByName(@Param("calName") String calName);


    @Modifying
//    @Query("update User u set u.orgCalendar = null where u.userId in :userIds")
    @Query(value = "update rbac.user u set u.orgCalendar = null where u.userId in :userIds",nativeQuery = true)
    void removeUserOrgCalendarMapping(@Param("userIds") Collection<Long> userIds);

    @Query(nativeQuery = true,value = "select u.userId from rbac.userTable u where u.orgCalendarId = ?1")
    List<Long> getUserIdsForOrgCD(Long orgCalendarId);

    @Query(nativeQuery = true, value = "select sr.scheduleRuleId from rbac.scheduleRule sr where sr.scheduleRuleId not in (select csr.scheduleRuleId from rbac.calendarScheduleRuleMapping csr)")
    List<Long> getUnmappedScheduleRules();

    @Modifying
    @Transactional
    @Query("delete from ScheduleRule sr where sr.scheduleRuleId in :scheduleRuleIds")
    void deleteByScheduleRuleIds(@Param("scheduleRuleIds") List<Long> scheduleRuleIds);

}
