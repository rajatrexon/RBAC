package com.esq.rbac.service.schedulerule.repository;

import com.esq.rbac.service.schedulerule.domain.ScheduleRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRuleRepository extends JpaRepository<ScheduleRule,Long> {
    @Modifying
    @Query("delete from ScheduleRule sr where sr.scheduleRuleId in :scheduleRuleIds")
    void deleteScheduleRuleByIds(List<Long> scheduleRuleIds);

    @Modifying
    @Query(value = "select * from rbac.scheduleRule sr,rbac.calendarScheduleRuleMapping csrm where sr.scheduleRuleId=csrm.scheduleRuleId and  sr.name=? and csrm.calendarId  = ?",nativeQuery = true)
    List getScheduleRule(String name,Long calendarId);
}
