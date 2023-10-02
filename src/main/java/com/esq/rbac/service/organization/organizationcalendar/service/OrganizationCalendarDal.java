package com.esq.rbac.service.organization.organizationcalendar.service;

import com.esq.rbac.service.organization.organizationcalendar.domain.OrganizationCalendar;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.calendar.domain.Calendar;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;

import java.util.List;
import java.util.Map;

public interface OrganizationCalendarDal {
    Calendar assign(Calendar calendar, AuditLogInfo auditLogInfo, String organizationId);
    Calendar unassign(Calendar calendar, AuditLogInfo auditLogInfo,String organizationId);
    OrganizationCalendar getByOrganizationId(Long organizationId) throws NoResultException, NonUniqueResultException;
    OrganizationCalendar getByOrganizationIdCalendarId(Long organizationId,Long calendarId) throws NoResultException, NonUniqueResultException ;
    Map<Long, Long> getOrganizationDefaultWorkCalendar();
    Map<Long, List<Long>> getOrganizationDefaultHolidayCalendars();
}
