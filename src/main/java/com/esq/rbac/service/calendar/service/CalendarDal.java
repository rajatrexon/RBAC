package com.esq.rbac.service.calendar.service;

import com.esq.rbac.service.schedulerule.scheduledefaultsubdomain.domain.ScheduleRuleDefault;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.calendar.domain.Calendar;
import com.esq.rbac.service.util.OrgHolidaysCalendarResponseVO;
import com.esq.rbac.service.util.dal.Options;

import java.time.LocalDate;
import java.util.List;

public interface CalendarDal {

    Calendar create(Calendar calendar, AuditLogInfo auditLogInfo);
    Calendar assign(Calendar calendar, AuditLogInfo auditLogInfo,String organizationId);
    Calendar unassign(Calendar calendar, AuditLogInfo auditLogInfo,String organizationId);

    Calendar update(Calendar calendar, AuditLogInfo auditLogInfo);

    void deleteById(Long calendarId, AuditLogInfo auditLogInfo);

    List<Calendar> list(Options options);

    List<Calendar> searchList(Options options);

    long count(Options options);

    long searchCount(Options options);

    List<ScheduleRuleDefault> listDefaultScheduleRules(Options options);

    Calendar getById(Long calendarId);
    Calendar getCalendarWithIsDefault(Long calendarId,Long organizationId);
    ScheduleRuleDefault createScheduleRuleDefault(ScheduleRuleDefault scheduleRuleDefault, AuditLogInfo auditLogInfo);
    Calendar getCalendarByCalendarName(String calName);
    List<Calendar> getDataByAssignedStatus(Options options, boolean isSearch);
    long getCountByAssignedStatus(Options options, boolean isSearch);
    void deleteUnmappedScheduleRules();
    List<OrgHolidaysCalendarResponseVO> getHolidaysByOrganization(LocalDate sDateStr, LocalDate eDateStr,
                                                                  int orgId) throws Exception;
    /*added by pankaj
     * RBAC-1035 Missing Server Side Validations
     * START
     * */
    Boolean isValidTimezone(String timezone);
    /*added by pankaj
     * RBAC-1035 Missing Server Side Validations
     * END
     * */
    /*(CM-545) Consider organization time zone from calendar for organizational attributes*/
    String getTimeZoneByOrganizationId(Long organizationId);
    String getTimezoneFromOrganization(Long organizationId, Long tenantId);
}
