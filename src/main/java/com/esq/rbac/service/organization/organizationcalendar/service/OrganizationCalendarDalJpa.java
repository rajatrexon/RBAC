package com.esq.rbac.service.organization.organizationcalendar.service;

import com.esq.rbac.service.organization.organizationcalendar.domain.OrganizationCalendar;
import com.esq.rbac.service.organization.organizationcalendar.repository.OrganizationCalendarRepository;
import com.esq.rbac.service.auditlog.repository.AuditLogRepository;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.calendar.domain.Calendar;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class OrganizationCalendarDalJpa extends BaseDalJpa implements OrganizationCalendarDal{

    private static final Map<String, String> SORT_COLUMNS;
    private static final Map<String, String> SCHEDULE_RULES_SORT_COLUMNS;
    private static final String CALENDAR_BLANK = "blankCalendarName";
    private static final String DUPLICATED_CALENDAR = "duplicateCalendar";
    private static final String DUPLICATED_CALENDAR_NAME = "duplicateCalendarName";
    private static final String CALENDAR_TYPE_WORK = "Work";


    @Autowired
    private OrganizationCalendarRepository organizationCalendarRepository;



    private AuditLogRepository auditLogRepository;

    @Autowired
    public void setAuditLogRepository(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("name", "c.name");
        SORT_COLUMNS.put("calendarId", "c.calendarId");
        SORT_COLUMNS.put("displayOrder", "c.displayOrder");
    }
    static {
        SCHEDULE_RULES_SORT_COLUMNS = new TreeMap<String, String>();
        SCHEDULE_RULES_SORT_COLUMNS.put("name", "s.name");
        SCHEDULE_RULES_SORT_COLUMNS.put("calendarId", "s.calendarId");
        SCHEDULE_RULES_SORT_COLUMNS.put("displayOrder", "s.displayOrder");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Calendar assign(Calendar calendar, AuditLogInfo auditLogInfo,String organizationId) {

        OrganizationCalendar orgCal =null;
        if(organizationId!=null && !"".equalsIgnoreCase(organizationId)){
            try{
                orgCal = getByOrganizationId(Long.parseLong(organizationId));
                System.out.println("ORGANIZATION->"+orgCal);
            }catch( NonUniqueResultException nure  ){

                orgCal = new OrganizationCalendar();
            }catch(NoResultException nre ){

                orgCal = new OrganizationCalendar();
            }catch(Exception e){
                orgCal = new OrganizationCalendar();

            }
            Date createdDate = new Date();
            if(orgCal!=null && orgCal.getOrganizationId()!=0 ){
                if(orgCal.getCalendarId()!=calendar.getCalendarId()){
                    if(calendar.getIsDefaultCalendar().equals(true) ){
                        orgCal.setIsDefaultCalendar(false);
                        orgCal.setUpdatedBy(auditLogInfo.getLoggedInUserId());
                        orgCal.setUpdatedOn(createdDate);
                        organizationCalendarRepository.save(orgCal);

                        OrganizationCalendar orgCalNew = getByOrganizationIdCalendarId(Long.parseLong(organizationId),calendar.getCalendarId());
                        if(orgCalNew!=null && orgCalNew.getOrganizationCalendarId() > 0)
                        {
                            orgCalNew.setIsDefaultCalendar(true);
                            orgCalNew.setOrganizationId(Long.valueOf(organizationId));
                            orgCalNew.setCalendarId(calendar.getCalendarId());
                            orgCalNew.setCreatedBy(auditLogInfo.getLoggedInUserId());
                            orgCalNew.setCreatedOn(createdDate);
                            em.merge(orgCalNew);
                            calendar.setIsDefaultCalendar(true);
                            calendar.setAssigned("Assigned");
                        }else{
                            orgCalNew = new OrganizationCalendar();
                            orgCalNew.setIsDefaultCalendar(true);
                            orgCalNew.setOrganizationId(Long.valueOf(organizationId));
                            orgCalNew.setCalendarId(calendar.getCalendarId());
                            orgCalNew.setCreatedBy(auditLogInfo.getLoggedInUserId());
                            orgCalNew.setCreatedOn(createdDate);
                            em.persist(orgCalNew);
                            calendar.setIsDefaultCalendar(true);
                            calendar.setAssigned("Assigned");
                        }

                    }else{

                        OrganizationCalendar orgCalNew = getByOrganizationIdCalendarId(Long.valueOf(organizationId),calendar.getCalendarId());
                        if(orgCalNew!=null && orgCalNew.getOrganizationCalendarId() > 0)
                        {
                            orgCalNew.setIsDefaultCalendar(orgCalNew.getIsDefaultCalendar() != null && orgCalNew.getIsDefaultCalendar());
                            orgCalNew.setOrganizationId(Long.valueOf(organizationId));
                            orgCalNew.setCalendarId(calendar.getCalendarId());
                            orgCalNew.setCreatedBy(auditLogInfo.getLoggedInUserId());
                            orgCalNew.setCreatedOn(createdDate);
                            em.merge(orgCalNew);
                            calendar.setIsDefaultCalendar(orgCalNew.getIsDefaultCalendar());
                            calendar.setAssigned("Assigned");
                        }else{
                            orgCalNew = new OrganizationCalendar();
                            orgCalNew.setIsDefaultCalendar(orgCalNew.getIsDefaultCalendar() != null && orgCalNew.getIsDefaultCalendar());
                            orgCalNew.setOrganizationId(Long.valueOf(organizationId));
                            orgCalNew.setCalendarId(calendar.getCalendarId());
                            orgCalNew.setCreatedBy(auditLogInfo.getLoggedInUserId());
                            orgCalNew.setCreatedOn(createdDate);
                            em.persist(orgCalNew);
                            calendar.setIsDefaultCalendar(orgCalNew.getIsDefaultCalendar());
                            calendar.setAssigned("Assigned");
                        }

                    }
                }
            }else{
                OrganizationCalendar orgCalNew = getByOrganizationIdCalendarId(Long.valueOf(organizationId),calendar.getCalendarId());
                if(orgCalNew!=null && orgCalNew.getOrganizationCalendarId() > 0)
                {
                    if(calendar.getIsDefaultCalendar().equals(true))
                    {
                        orgCalNew.setIsDefaultCalendar(true);
                        orgCalNew.setOrganizationId(Long.valueOf(organizationId));
                        orgCalNew.setCalendarId(calendar.getCalendarId());
                        orgCalNew.setCreatedBy(auditLogInfo.getLoggedInUserId());
                        orgCalNew.setCreatedOn(createdDate);
                        em.merge(orgCalNew);
                        calendar.setIsDefaultCalendar(true);
                        calendar.setAssigned("Assigned");
                    }

                }else{


                    if(calendar.getCalendarType().getCodeValue().equalsIgnoreCase(CALENDAR_TYPE_WORK)){
                        orgCal = new OrganizationCalendar();
                        orgCal.setIsDefaultCalendar(true);
                        calendar.setIsDefaultCalendar(true);
                        orgCal.setCalendarId(calendar.getCalendarId());
                        orgCal.setCreatedBy(auditLogInfo.getLoggedInUserId());
                        orgCal.setCreatedOn(createdDate);
                        orgCal.setOrganizationId(Long.valueOf(organizationId));
                        em.persist(orgCal);
                        calendar.setAssigned("");
                    }else{
                        orgCal = new OrganizationCalendar();
                        orgCal.setIsDefaultCalendar(false);
                        calendar.setIsDefaultCalendar(false);
                        orgCal.setCalendarId(calendar.getCalendarId());
                        orgCal.setCreatedBy(auditLogInfo.getLoggedInUserId());
                        orgCal.setCreatedOn(createdDate);
                        orgCal.setOrganizationId(Long.valueOf(organizationId));
                        em.persist(orgCal);
                        calendar.setAssigned("");
                    }
                }

            }

        }
        return calendar;


		/*Map<String, String> objectChanges = setNewObjectChangeSetLocal(null, calendar);
	    auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), calendar.getName(),"Calendar", "Create", objectChanges);
		em.merge(calendar);*/
        //return calendar;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Calendar unassign(Calendar calendar, AuditLogInfo auditLogInfo,String organizationId) {

        int result = organizationCalendarRepository.deleteByCalendarIdAndOrganizationId(calendar.getCalendarId(), Long.valueOf(organizationId));
        if(result > 0){
            calendar.setAssigned("");
        }
        return calendar;

		/*Map<String, String> objectChanges = setNewObjectChangeSetLocal(null, calendar);
	    auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), calendar.getName(),"Calendar", "Create", objectChanges);
		em.merge(calendar);*/

    }
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public OrganizationCalendar getByOrganizationId(Long organizationId)throws NoResultException, NonUniqueResultException {

        List<OrganizationCalendar> orgCalList = organizationCalendarRepository.findDefaultCalendarByOrganizationId(organizationId);
        System.out.println("SIZE-->"+orgCalList.size());
        OrganizationCalendar orgCalReturn = null;
        if(!orgCalList.isEmpty())
        {
            for (OrganizationCalendar orgcal : orgCalList){
                orgCalReturn = orgcal;

            }
            return orgCalReturn;

        }
        else
            return null;

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public OrganizationCalendar getByOrganizationIdCalendarId(Long organizationId,Long calendarId)throws NoResultException, NonUniqueResultException {
        List<OrganizationCalendar> orgCalList= organizationCalendarRepository.getOrganizationCalendarByOrgIdCalId(organizationId,calendarId);
        OrganizationCalendar orgCalReturn = null;
        if(orgCalList.size() >= 0)
        {
            for (OrganizationCalendar orgcal : orgCalList){
                orgCalReturn = orgcal;

            }
            return orgCalReturn;

        }
        else
            return null;

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<Long, Long> getOrganizationDefaultWorkCalendar() {
        Map<Long, Long> result = new LinkedHashMap<Long, Long>();
        List<Object[]> orgCalList = organizationCalendarRepository.getOrganizationDefaultWorkCalendar();
        if(orgCalList!=null && !orgCalList.isEmpty()){
            for(Object[] obj:orgCalList){
                result.put(Long.parseLong(obj[0].toString()), Long.parseLong(obj[1].toString()));
            }
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<Long, List<Long>> getOrganizationDefaultHolidayCalendars() {
        Map<Long, List<Long>> result = new LinkedHashMap<Long, List<Long>>();
        List<Object[]> orgCalList = organizationCalendarRepository.getOrganizationDefaultHolidayCalendars();
        if(orgCalList!=null && !orgCalList.isEmpty()){
            for(Object[] obj:orgCalList){
                if(!result.containsKey(Long.parseLong(obj[0].toString()))){
                    result.put(Long.parseLong(obj[0].toString()), new LinkedList<Long>());
                }
                result.get(Long.parseLong(obj[0].toString())).add(Long.parseLong(obj[1].toString()));
            }
        }
        return result;
    }
}

