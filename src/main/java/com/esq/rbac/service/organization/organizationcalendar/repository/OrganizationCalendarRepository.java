package com.esq.rbac.service.organization.organizationcalendar.repository;

import com.esq.rbac.service.organization.organizationcalendar.domain.OrganizationCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationCalendarRepository extends JpaRepository<OrganizationCalendar,Long> {


    @Query("select count(1) from OrganizationCalendar orgCal where orgCal.calendarId = :calendarId and orgCal.isDefaultCalendar=true")
    Long getUserCountByCalendarId(@Param("calendarId") Long calendarIds);

    @Modifying
    @Query("DELETE FROM OrganizationCalendar orgCal WHERE orgCal.calendarId = :calendarId AND orgCal.organizationId = :organizationId")
    Integer deleteByCalendarIdAndOrganizationId(@Param("calendarId") Long calendarId, @Param("organizationId") Long organizationId);

    @Query("SELECT orgCal FROM OrganizationCalendar orgCal WHERE orgCal.organizationId = :organizationId AND orgCal.isDefaultCalendar = true")
    List<OrganizationCalendar> getOrganizationCalendarById(@Param("organizationId") Long organizationId);

    @Query("SELECT orgCal FROM OrganizationCalendar orgCal WHERE orgCal.organizationId = :organizationId AND orgCal.calendarId = :calendarId")
    List<OrganizationCalendar> getOrganizationCalendarByOrgIdCalId(@Param("organizationId") Long organizationId, @Param("calendarId") Long calendarId);

    @Query("DELETE FROM OrganizationCalendar orgCal WHERE orgCal.calendarId = :calendarId AND orgCal.organizationId = :organizationId")
    void unassignCalendarById(@Param("calendarId") Long calendarId, @Param("organizationId") Long organizationId);

    @Query("SELECT orgCal.organizationId, orgCal.calendarId FROM OrganizationCalendar orgCal JOIN Calendar c ON (c.calendarId = orgCal.calendarId) WHERE orgCal.isDefaultCalendar = TRUE AND c.calendarType != NULL AND c.calendarType.codeValue = 'WORK'")
    List<Object[]> getOrganizationDefaultWorkCalendar();

    @Query("SELECT orgCal.organizationId, orgCal.calendarId FROM OrganizationCalendar orgCal JOIN Calendar c ON (c.calendarId = orgCal.calendarId) WHERE c.calendarType != NULL AND c.calendarType.codeValue = 'Holiday'")
    List<Object[]> getOrganizationDefaultHolidayCalendars();

    @Query("SELECT orgCal FROM OrganizationCalendar orgCal WHERE orgCal.organizationId = :organizationId AND orgCal.isDefaultCalendar = true")
    List<OrganizationCalendar> findDefaultCalendarByOrganizationId(@Param("organizationId") Long organizationId);
}
