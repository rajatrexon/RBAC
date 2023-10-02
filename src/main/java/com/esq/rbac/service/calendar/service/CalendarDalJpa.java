package com.esq.rbac.service.calendar.service;

import com.esq.rbac.service.calendar.helpers.OrgWorkTimeDetails;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.organizationcalendar.domain.OrganizationCalendar;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.schedulerule.domain.ScheduleRule;
import com.esq.rbac.service.schedulerule.repository.ScheduleRuleRepository;
import com.esq.rbac.service.schedulerule.scheduledefaultsubdomain.domain.ScheduleRuleDefault;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.calendar.domain.Calendar;
import com.esq.rbac.service.calendar.repository.CalendarRepository;
import com.esq.rbac.service.config.CacheConfig;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.organization.organizationcalendar.repository.OrganizationCalendarRepository;
import com.esq.rbac.service.organization.organizationcalendar.service.OrganizationCalendarDal;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.tenant.service.TenantDal;
import com.esq.rbac.service.util.AuditLogHelperUtil;
import com.esq.rbac.service.util.OrgHolidaysCalendarResponseVO;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CalendarDalJpa extends BaseDalJpa implements CalendarDal{

    private static final String CALENDAR_BLANK = "blankCalendarName";
    private static final String CALENDAR_TIMEZONE_INVALID = "invalidTimezone";
    private static final String DUPLICATED_CALENDAR = "duplicateCalendar";
    private static final String DUPLICATED_CALENDAR_NAME = "duplicateCalendarName";
    private static final String DUPLICATED_SCHEDULE = "duplicateSchedule";
    private static final String DUPLICATED_SCHEDULE_NAME = "duplicateScheduleName";
    private static final Map<String, String> SORT_COLUMNS;
    private static final Map<String, String> SCHEDULE_RULES_SORT_COLUMNS;
    private static final Map<String, String> SORT_COLUMNS_ASSIGNED;
    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("name", "c.name");
        SORT_COLUMNS.put("calendarId", "c.calendarId");
        SORT_COLUMNS.put("displayOrder", "c.displayOrder");

        SORT_COLUMNS_ASSIGNED = new TreeMap<String, String>();
        SORT_COLUMNS_ASSIGNED.put("name", "name");
    }

    static {
        SCHEDULE_RULES_SORT_COLUMNS = new TreeMap<String, String>();
        SCHEDULE_RULES_SORT_COLUMNS.put("name", "s.name");
        SCHEDULE_RULES_SORT_COLUMNS.put("calendarId", "s.calendarId");
        SCHEDULE_RULES_SORT_COLUMNS.put("displayOrder", "s.displayOrder");
    }

    protected EntityManager em;

    protected Class entityClass;

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager");
        this.em = em;
        this.entityClass = Calendar.class;
    }



    private ScheduleRuleRepository scheduleRuleRepository;

    @Autowired
    public void setScheduleRuleRepository(ScheduleRuleRepository scheduleRuleRepository) {
        log.trace("setScheduleRuleRepository; {};", scheduleRuleRepository);
        this.scheduleRuleRepository = scheduleRuleRepository;

    }


    private AuditLogService auditLogDal;

    @Autowired
    public void setAuditLogService(AuditLogService auditLogDal) {
        log.trace("setAuditLogService; {};", auditLogDal);
        this.auditLogDal = auditLogDal;

    }



    private OrganizationCalendarDal orgCalendarDal;

    @Autowired
    public void setOrganizationCalendarDal(OrganizationCalendarDal orgCalendarDal) {
        log.trace("setOrganizationCalendarDal; {};", orgCalendarDal);
        this.orgCalendarDal = orgCalendarDal;

    }

    private OrganizationCalendarRepository organizationCalendarRepository;

    @Autowired
    public void setOrganizationCalendarRepository(OrganizationCalendarRepository organizationCalendarRepository) {
        log.trace("setOrganizationCalendarRepository; {};", organizationCalendarRepository);
        this.organizationCalendarRepository =organizationCalendarRepository;

    }

    private CalendarRepository calendarRepository;

    @Autowired
    public void setCalendarRepository(CalendarRepository calendarRepository) {
        log.trace("setOrganizationCalendarRepository; {};", calendarRepository);
        this.calendarRepository =calendarRepository;

    }



    private TenantDal tenantDal;


    @Autowired
    public void setTenantDal(TenantDal tenantDal) {
        log.trace("setTenantDal; {};", tenantDal);
        this.tenantDal =tenantDal;

    }

    private OrganizationMaintenanceDal organizationMaintenanceDal;

    @Autowired
    public void setOrganizationMaintenanceDal(OrganizationMaintenanceDal organizationMaintenanceDal) {
        this.organizationMaintenanceDal = organizationMaintenanceDal;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_LIST_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_CACHE, allEntries = true)
    })
    public Calendar create(Calendar calendar, AuditLogInfo auditLogInfo) {

        if (calendar == null) {
            throw new IllegalArgumentException();
        }
        /*added by pankaj
         * RBAC-1035 Missing Server Side Validations
         * START
         * */
        if(!isValidTimezone(calendar.getTimeZone())) {
            ErrorInfoException errorInfo = new ErrorInfoException(CALENDAR_TIMEZONE_INVALID);
            throw errorInfo;
        }
        /*added by pankaj
         * RBAC-1035 Missing Server Side Validations
         * END
         * */
        validateEntry(calendar);
        Date createdDate = new Date();
        calendar.setCreatedBy(auditLogInfo.getLoggedInUserId());
        calendar.setCreatedOn(createdDate);
        em.persist(calendar);


        if (calendar.getRules() != null && !calendar.getRules().isEmpty()) {
            for (ScheduleRule scheduleRule : calendar.getRules()) {
                validateScheduleRuleDefault(scheduleRule);
                if (scheduleRule.getScheduleRuleId() == null) {

                    scheduleRule.setCreatedBy(auditLogInfo.getLoggedInUserId());
                    scheduleRule.setCreatedOn(createdDate);

                    em.persist(scheduleRule);
                } else {
                    ScheduleRule scheduleRuleDb = em.find(ScheduleRule.class,
                            scheduleRule.getScheduleRuleId());
                    if (scheduleRuleDb == null) {
                        throw new IllegalArgumentException();
                    }
                    updateScheduleRule(scheduleRuleDb, scheduleRule);

                    scheduleRuleDb.setUpdatedBy(auditLogInfo
                            .getLoggedInUserId());
                    scheduleRuleDb.setUpdatedOn(createdDate);

                    em.merge(scheduleRuleDb);
                }
            }
            validateScheduleRuleName(calendar);
        }
        Map<String, String> objectChanges = setNewObjectChangeSetLocal(null, calendar);
        auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), calendar.getName(),"Calendar", "Create", objectChanges);
        em.merge(calendar);
        if(calendar.getOrganizationId()!=null && calendar.getOrganizationId() >=0 )
        {

            calendar = orgCalendarDal.assign(calendar, auditLogInfo, String.valueOf(calendar.getOrganizationId()));
        }
        return calendar;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CALENDAR_BY_CALENDARID_CACHE, key = "#calendar?.calendarId"),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_LIST_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_CACHE, allEntries = true)
    })
    public Calendar assign(Calendar calendar, AuditLogInfo auditLogInfo,String organizationId) {

        if (calendar == null) {
            throw new IllegalArgumentException();
        }
        //validateEntry(calendar);


        calendar = orgCalendarDal.assign( calendar,  auditLogInfo, organizationId);

        String assigned = calendar.getAssigned();
        Map<String, String> objectChanges = new TreeMap<String, String>();
        objectChanges.put((assigned != null) ? assigned + " Organization Name" + ":new" : "", Lookup.getOrganizationNameById(Long.parseLong(organizationId)));
        auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), calendar.getName(),"Calendar", "Update", objectChanges);
        //em.merge(calendar);
        return calendar;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CALENDAR_BY_CALENDARID_CACHE, key = "#calendar?.calendarId"),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_LIST_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_CACHE, allEntries = true)
    })
    public Calendar unassign(Calendar calendar, AuditLogInfo auditLogInfo,String organizationId) {

        if (calendar == null) {
            throw new IllegalArgumentException();
        }

        calendar = orgCalendarDal.unassign( calendar,  auditLogInfo, organizationId);
        String assigned = calendar.getAssigned();
        Map<String, String> objectChanges = new TreeMap<String, String>();
        objectChanges.put(StringUtils.isEmpty(assigned) ? "Un-Assigned Organization Name"+ ":new" : "", Lookup.getOrganizationNameById(Long.parseLong(organizationId)));
        auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), calendar.getName(),"Calendar", "Update", objectChanges);
        return calendar;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CALENDAR_BY_CALENDARID_CACHE, key = "#calendar?.calendarId"),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_LIST_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_CACHE, allEntries = true)
    })
    public Calendar update(Calendar calendar, AuditLogInfo auditLogInfo) {

        if (calendar.getCalendarId() == null) {
            throw new IllegalArgumentException("calendarId missing");
        }
        Calendar dbCalendar = em.find(Calendar.class, calendar.getCalendarId());
        if (dbCalendar == null) {
            throw new IllegalArgumentException("calendarId invalid");
        }

        if(!dbCalendar.getName().equalsIgnoreCase(calendar.getName())) {
            Calendar cal = getCalendarByCalendarName(calendar.getName());
            if(cal!=null && cal.getCalendarId() != dbCalendar.getCalendarId()){
                StringBuilder sb = new StringBuilder();
                sb.append(DUPLICATED_CALENDAR).append("; ");
                sb.append(DUPLICATED_CALENDAR_NAME).append("=").append(calendar.getName());
                log.info("update; {}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_CALENDAR, sb.toString());
                errorInfo.getParameters().put(DUPLICATED_CALENDAR_NAME, calendar.getName());
                log.info("update; calendarerrorInfo={}", errorInfo);
                throw errorInfo;
            }
        }
        Map<String, String> objectChanges = setNewObjectChangeSetLocal(dbCalendar, calendar);

        dbCalendar.setIsActive(calendar.getIsActive());
        dbCalendar.setName(calendar.getName());
        dbCalendar.setSharingType(calendar.getSharingType());
        dbCalendar.setCalendarSubType(calendar.getCalendarSubType());
        dbCalendar.setCalendarType(calendar.getCalendarType());
        dbCalendar.setTimeZone(calendar.getTimeZone());
        dbCalendar.setDescription(calendar.getDescription());

        Date updatedDate = new Date();
        dbCalendar.setUpdatedBy(auditLogInfo.getLoggedInUserId());
        dbCalendar.setUpdatedOn(updatedDate);
        //set for new object as well, this method doesn't return merged object
        calendar.setUpdatedBy(auditLogInfo.getLoggedInUserId());
        calendar.setUpdatedOn(updatedDate);
        calendar.setCreatedBy(dbCalendar.getCreatedBy());
        calendar.setCreatedOn(dbCalendar.getCreatedOn());

        if (calendar.getRules() != null && !calendar.getRules().isEmpty()) {
            for (ScheduleRule scheduleRule : calendar.getRules()) {
                validateScheduleRuleDefault(scheduleRule);
                if (scheduleRule.getScheduleRuleId() == null) {
                    scheduleRule.setCreatedBy(auditLogInfo.getLoggedInUserId());
                    scheduleRule.setCreatedOn(updatedDate);
                    em.persist(scheduleRule);
                } else {
                    ScheduleRule scheduleRuleDb = em.find(ScheduleRule.class,
                            scheduleRule.getScheduleRuleId());
                    updateScheduleRule(scheduleRuleDb, scheduleRule);

                    scheduleRuleDb.setUpdatedBy(auditLogInfo
                            .getLoggedInUserId());
                    scheduleRuleDb.setUpdatedOn(updatedDate);
                    em.merge(scheduleRuleDb);
                }
            }
        }

        dbCalendar.setRules(calendar.getRules());
        validateScheduleRuleName(dbCalendar);
        em.merge(dbCalendar);
        auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), dbCalendar.getName(),"Calendar", "Update", objectChanges);
        if(calendar.getOrganizationId()!=null && calendar.getOrganizationId() >=0 && Boolean.TRUE.equals(calendar.getIsDefaultCalendar()))
        {
            calendar = orgCalendarDal.assign(calendar, auditLogInfo, String.valueOf(calendar.getOrganizationId()));
        }

        return calendar;

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CALENDAR_BY_CALENDARID_CACHE, key = "#p0"),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_LIST_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_CACHE, allEntries = true)
    })
    public void deleteById(Long calendarId, AuditLogInfo auditLogInfo) {
        Calendar dbCalendar = calendarRepository.findAll()
                .stream()
                .filter(calendar -> Objects.equals(calendar.getCalendarId(), calendarId)).findFirst().orElse(null);
        if (dbCalendar == null) {
            throw new IllegalArgumentException("Invalid CalendarId  "+ dbCalendar.getCalendarId());
        }

//        TypedQuery<Long> queryOrgCalPresence = em.createQuery("select count(1) from OrganizationCalendar orgCal where orgCal.calendarId = :calendarId and orgCal.isDefaultCalendar=1", Long.class);
//        queryOrgCalPresence.setParameter("calendarId", calendarId);
//        Long userCount = queryOrgCalPresence.getSingleResult();
        Long userCount = organizationCalendarRepository.getUserCountByCalendarId(calendarId);
        if(organizationCalendarRepository.getUserCountByCalendarId(calendarId) !=null && userCount > 0){
            throw new ErrorInfoException("calendarOrganizationAssociationFound", "Calendar can't be deleted, associated with user");
        }

//        TypedQuery<Organization> query = em.createNamedQuery("deleteCalendarById", Organization.class);
//        query.setParameter("calendarId", calendarId);
//        query.setParameter("userId", auditLogInfo.getLoggedInUserId());
//        query.setParameter("currDateTime", DateTime.now().toDate());
//        query.executeUpdate();
            calendarRepository.deleteCalendarById(calendarId,auditLogInfo.getLoggedInUserId(),DateTime.now().toDate());
        auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), dbCalendar.getName(),"Calendar", "Delete", setNewObjectChangeSetLocal(null, dbCalendar));

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.CALENDAR_BY_LIST_CACHE, unless = "#result == null")
    public List<Calendar> list(Options options) {
        Filters filters = prepareFilters(options);
        filters.addCondition("c.sharingType='Public'");
        return filters.getList(em, Calendar.class, "select c from Calendar c",
                options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_CACHE, unless = "#result == null")
    public List<Calendar> searchList(Options options) {
        Filters filters = prepareFilters(options);
        filters.addCondition(" c.sharingType='Public' ");
        filters.addCondition(" ( c.name like :q or c.timeZone like :q ) ");
        filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase()));
        return filters.getList(em, Calendar.class, "select c from Calendar c",
                options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.CALENDAR_BY_LIST_COUNT_CACHE, unless = "#result == null")
    public long count(Options options){
        Filters filters = prepareFilters(options);
        filters.addCondition("c.sharingType='Public'");
        return filters.getCount(em, "select count(c) from Calendar c");
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.CALENDAR_BY_SEARCH_LIST_COUNT_CACHE, unless = "#result == null")
    public long searchCount(Options options){
        Filters filters = prepareFilters(options);
        filters.addCondition(" c.sharingType='Public' ");
        filters.addCondition(" ( c.name like :q or c.timeZone like :q ) ");
        filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase()));
        return filters.getCount(em, "select count(c) from Calendar c");
    }
    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<ScheduleRuleDefault> listDefaultScheduleRules(Options options) {
        Filters filters = prepareFiltersForScheduleRules(options);
        return filters.getList(em, ScheduleRuleDefault.class, "select s from ScheduleRuleDefault s",
                options, SCHEDULE_RULES_SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.CALENDAR_BY_CALENDARID_CACHE, key = "#calendarId", unless = "#result == null")
    public Calendar getById(Long calendarId) {
        return em.find(Calendar.class, calendarId);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Calendar getCalendarWithIsDefault(Long calendarId,Long organizationId) {
        Query query = em.createNativeQuery("select c.*, orgcal.isDefaultCalendar as isDefault from rbac.calendar c, rbac.organizationCalendar orgcal where c.calendarId=orgcal.calendarId and c.isDeleted=0  and c.calendarId=? and orgcal.organizationId=?","CalendarAssignMapping");
        query.setParameter(1, calendarId);
        query.setParameter(2, organizationId);
        List<Object[]> cals = query.getResultList();

        List<Calendar> resultCals = new ArrayList<Calendar>(cals.size());
        for(int i=0;i<cals.size();i++){
            Object[] cal = cals.get(i);
            Calendar resultCal=(Calendar)cal[0];
            Boolean isDefault = (Boolean)cal[2];

            if(isDefault){
                resultCal.setIsDefaultCalendar(true);
            }else{
                resultCal.setIsDefaultCalendar(false);
            }
            resultCals.add((Calendar)resultCal);
        }
        if(!resultCals.isEmpty())
            return resultCals.get(0);
        else
            return em.find(Calendar.class, calendarId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ScheduleRuleDefault createScheduleRuleDefault(
            ScheduleRuleDefault scheduleRuleDefault, AuditLogInfo auditLogInfo) {
        Date createdDate = new Date();
        scheduleRuleDefault.setCreatedBy(auditLogInfo.getLoggedInUserId());
        scheduleRuleDefault.setCreatedOn(createdDate);
        em.persist(scheduleRuleDefault);
        return scheduleRuleDefault;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Calendar getCalendarByCalendarName(String calName){
        try {
            return calendarRepository.getCalendarByName(calName);
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_CACHE, unless = "#result == null")
    public List<Calendar> getDataByAssignedStatus(Options options, boolean isSearch) {
        return getCalendarListByAssignedStatus(em, Calendar.class, "select c from Calendar c",
                options, SORT_COLUMNS_ASSIGNED, isSearch);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    private <T> List<Calendar> getCalendarListByAssignedStatus(EntityManager em,
                                                               Class<T> type, String queryText, Options options,
                                                               Map<String, String> sortColumnsMap, boolean isSearch){
        Query query = null;
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        String tenantId="";
        String calendarTypeId="";
        if (filters != null) {
            tenantId = filters.get("tenantId");
            calendarTypeId = filters.get("calendarTypeId");
        }
        Tenant dbTenant =null;
        if(tenantId!=null && !tenantId.isEmpty())
        {
            dbTenant = tenantDal.getById(Long.parseLong(tenantId));
        }
        String tenantName="";
        String sortString = "";
        String sortBy = "";
        if(dbTenant!=null){
            tenantName = dbTenant.getTenantName();
        }
        OptionPage optionPage = options != null ? options
                .getOption(OptionPage.class) : null;
        OptionSort optionSort = options != null ? options
                .getOption(OptionSort.class) : null;
        if (optionSort != null) {
            if(optionSort.getSortProperties()!=null && !optionSort.getSortProperties().isEmpty()){
                String property = optionSort.getSortProperties().get(0);
                sortBy = " asc ";
                if (property.startsWith("-")) {
                    sortBy = " desc ";
                    property = property.substring(1);
                }
                if(sortColumnsMap.get(property)!=null){
                    sortString = " order by "+sortColumnsMap.get(property) + sortBy;
                }
            }
        }
        if(isSearch){
            if(tenantName.equalsIgnoreCase("Host")){
                if(calendarTypeId!=null && !"".equalsIgnoreCase(calendarTypeId)){
                    query = em.createNativeQuery("select * from (select *, ROW_NUMBER() OVER (order by name asc) AS RowNum from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                            " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union "+
                            " select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 ) and c.calendarType=? ) as tmp where (name like ? or timeZone like ?)) as temp1 where RowNum BETWEEN ?  AND ? order by name asc","CalendarAssignMapping");


                    query.setParameter(1, calendarTypeId);
                    query.setParameter(2, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
                    query.setParameter(3, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
                    if (optionPage != null) {
                        query.setParameter(4, optionPage.getFirstResult()+1);
                        query.setParameter(5, optionPage.getFirstResult()+optionPage.getMaxResults());
                    } else {
                        query.setParameter(4, 1);
                        query.setParameter(5, Integer.MAX_VALUE);
                    }
                }else{
                    query = em.createNativeQuery("select * from (select *, ROW_NUMBER() OVER (order by name asc) AS RowNum from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                            " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union "+
                            " select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 )) as tmp where (name like ? or timeZone like ?)) as temp1 where RowNum BETWEEN ?  AND ?  order by name asc","CalendarAssignMapping");

                    query.setParameter(1, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
                    query.setParameter(2, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
                    if (optionPage != null) {
                        query.setParameter(3, optionPage.getFirstResult()+1);
                        query.setParameter(4, optionPage.getFirstResult()+optionPage.getMaxResults());
                    } else {
                        query.setParameter(3, 1);
                        query.setParameter(4, Integer.MAX_VALUE);
                    }

                }

            }else{
                if(calendarTypeId!=null && !"".equalsIgnoreCase(calendarTypeId)){
                    query = em.createNativeQuery("select * from (select *, ROW_NUMBER() OVER (order by name asc) AS RowNum from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                            " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId " +
                            " from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') " +
                            " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0)   and c.calendarType=? ) as tmp where (name like ? or timeZone like ?)) as temp1 where RowNum BETWEEN ?  AND ?  order by name asc","CalendarAssignMapping");

                    query.setParameter(1, tenantId);
                    query.setParameter(2, tenantId);
                    query.setParameter(3, tenantId);
                    query.setParameter(4, tenantId);
                    query.setParameter(5, calendarTypeId);
                    query.setParameter(6, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
                    query.setParameter(7, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
                    if (optionPage != null) {
                        query.setParameter(8, optionPage.getFirstResult()+1);
                        query.setParameter(9, optionPage.getFirstResult()+optionPage.getMaxResults());
                    } else {
                        query.setParameter(8, 1);
                        query.setParameter(9, Integer.MAX_VALUE);
                    }
                }else{
                    query = em.createNativeQuery("select * from (select *, ROW_NUMBER() OVER (order by name asc) AS RowNum from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                            " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId " +
                            " from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') " +
                            " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0  ) ) as tmp where (name like ? or timeZone like ?)) as temp1 where RowNum BETWEEN ?  AND ?  order by name asc","CalendarAssignMapping");
                    query.setParameter(1, tenantId);
                    query.setParameter(2, tenantId);
                    query.setParameter(3, tenantId);
                    query.setParameter(4, tenantId);
                    query.setParameter(5, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
                    query.setParameter(6, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
                    if (optionPage != null) {
                        query.setParameter(7, optionPage.getFirstResult()+1);
                        query.setParameter(8, optionPage.getFirstResult()+optionPage.getMaxResults());
                    } else {
                        query.setParameter(7, 1);
                        query.setParameter(8, Integer.MAX_VALUE);
                    }
                }



            }

        }
        else{
            if(tenantName.equalsIgnoreCase("Host")){
                if(calendarTypeId!=null && !"".equalsIgnoreCase(calendarTypeId)){
                    query = em.createNativeQuery("select * from (select *, ROW_NUMBER() OVER (order by name asc) AS RowNum from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                            " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union "+
                            " select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 ) and c.calendarType=? ) as tmp) as temp1 where RowNum BETWEEN ?  AND ?  order by name asc","CalendarAssignMapping");


                    query.setParameter(1, calendarTypeId);
                    if (optionPage != null) {
                        query.setParameter(2, optionPage.getFirstResult()+1);
                        query.setParameter(3, optionPage.getFirstResult()+optionPage.getMaxResults());
                    } else {
                        query.setParameter(2, 1);
                        query.setParameter(3, Integer.MAX_VALUE);
                    }
                }else{
                    query = em.createNativeQuery("select * from (select *, ROW_NUMBER() OVER (order by name asc) AS RowNum from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                            " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union "+
                            " select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 )) as tmp) as temp1 where RowNum BETWEEN ?  AND ? order by name asc","CalendarAssignMapping");
                    if (optionPage != null) {
                        query.setParameter(1, optionPage.getFirstResult()+1);
                        query.setParameter(2, optionPage.getFirstResult()+optionPage.getMaxResults());
                    } else {
                        query.setParameter(1, 1);
                        query.setParameter(2, Integer.MAX_VALUE);
                    }

                }

            }else{
                if(calendarTypeId!=null && !"".equalsIgnoreCase(calendarTypeId)){
                    query = em.createNativeQuery("select * from (select *, ROW_NUMBER() OVER (order by name asc) AS RowNum from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                            " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId " +
                            " from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') " +
                            " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0)   and c.calendarType=? ) as tmp) as temp1 where RowNum BETWEEN ?  AND ? order by name asc","CalendarAssignMapping");

                    query.setParameter(1, tenantId);
                    query.setParameter(2, tenantId);
                    query.setParameter(3, tenantId);
                    query.setParameter(4, tenantId);
                    query.setParameter(5, calendarTypeId);
                    if (optionPage != null) {
                        query.setParameter(6, optionPage.getFirstResult()+1);
                        query.setParameter(7, optionPage.getFirstResult()+optionPage.getMaxResults());
                    } else {
                        query.setParameter(6, 1);
                        query.setParameter(7, Integer.MAX_VALUE);
                    }
                }else{
                    query = em.createNativeQuery("select * from (select *, ROW_NUMBER() OVER (order by name asc) AS RowNum from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                            " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                            " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId " +
                            " from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') " +
                            " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                            " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                            " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                            " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0  ) ) as tmp) as temp1 where RowNum BETWEEN ?  AND ? order by name asc","CalendarAssignMapping");
                    query.setParameter(1, tenantId);
                    query.setParameter(2, tenantId);
                    query.setParameter(3, tenantId);
                    query.setParameter(4, tenantId);
                    if (optionPage != null) {
                        query.setParameter(5, optionPage.getFirstResult()+1);
                        query.setParameter(6, optionPage.getFirstResult()+optionPage.getMaxResults());
                    } else {
                        query.setParameter(5, 1);
                        query.setParameter(6, Integer.MAX_VALUE);
                    }
                }



            }
        }
        List<Object[]> cals = query.getResultList();

        List<Calendar> resultCals = new ArrayList<Calendar>(cals.size());
        for(int i=0;i<cals.size();i++){
            Object[] cal = cals.get(i);
            Calendar resultCal;
            try {
                resultCal = (Calendar) BeanUtils.cloneBean(cal[0]);
                Integer status = (Integer)cal[1];
                Integer isDefault = (Integer)cal[2];
                String orgName = (String)cal[3];
                Long orgId = (Long)cal[4];
                if(status==1){
                    resultCal.setAssigned("Assigned");
                }else{
                    resultCal.setAssigned("");
                }
                if(isDefault==1){
                    resultCal.setIsDefaultCalendar(true);
                }else{
                    resultCal.setIsDefaultCalendar(false);
                }
                resultCal.setOrganizationName(orgName);
                resultCal.setOrganizationId(orgId);
                resultCals.add(resultCal);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        List<Calendar> resultCalsDesc = new ArrayList<>();
        if(sortBy.trim().equalsIgnoreCase("desc")){
            resultCalsDesc = resultCals.stream().sorted(Comparator.comparing(Calendar::getName).reversed()).collect(Collectors.toList());
            return resultCalsDesc;
        }else {
            return resultCals;
        }


    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.CALENDAR_BY_ASSIGNED_STATUS_COUNT_CACHE, unless = "#result == null")
    public long getCountByAssignedStatus(Options options, boolean isSearch){
        return getCountByAssignedStatus(em, "select count(c) from Calendar c",options, isSearch);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    private int getCountByAssignedStatus(EntityManager em, String queryText,Options options, boolean isSearch) {
        Query query = null;
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        String tenantId="";
        if (filters != null) {
            tenantId = filters.get("tenantId");
        }
        Tenant dbTenant =null;
        if(tenantId!=null && !tenantId.isEmpty())
        {
            dbTenant = tenantDal.getById(Long.parseLong(tenantId));
        }
        String tenantName="";
        if(dbTenant!=null){
            tenantName = dbTenant.getTenantName();
        }
        if(isSearch){
            if(tenantName.equalsIgnoreCase("Host")){
                query = em.createNativeQuery("select count(*) from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                        " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                        " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                        " where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                        " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                        " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union "+
                        " select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                        " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                        " left join rbac.organization org on (orgCal.organizationId =org.organizationId) where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                        " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                        " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 )) a where (name like ? or timeZone like ?)");
                query.setParameter(1, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
                query.setParameter(2, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));

            }else{
                query = em.createNativeQuery("select count(*) from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                        " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                        " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                        " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                        " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId " +
                        " from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') " +
                        " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                        " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                        " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                        " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0  ) ) a where (name like ? or timeZone like ?)");
                query.setParameter(1, tenantId);
                query.setParameter(2, tenantId);
                query.setParameter(3, tenantId);
                query.setParameter(4, tenantId);
                query.setParameter(5, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
                query.setParameter(6, SearchUtils.wildcarded(filters.get(SearchUtils.SEARCH_PARAM)));
            }
        }
        else{
            if(tenantName.equalsIgnoreCase("Host")){
                query = em.createNativeQuery("select count(*) from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                        " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                        " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                        " where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                        " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                        " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union "+
                        " select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                        " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId) "+
                        " left join rbac.organization org on (orgCal.organizationId =org.organizationId) where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                        " and org.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                        " and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 )) a");
            }else{
                query = em.createNativeQuery("select count(*) from (select c.*, 1 as status,ISNULL(orgcal.isDefaultCalendar,0) as isDefault,org.organizationName as OrgName,org.organizationId as OrgId "+
                        " from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                        " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                        " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') "+
                        " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0 union select c.*, 1 as status, 0 as isDefault, NULL as OrgName, NULL as OrgId " +
                        " from rbac.calendar c where c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host') " +
                        " and c.sharingType='Public' and c.isDeleted = 0 and c.calendarId not in (select c.calendarId from rbac.calendar c left outer join rbac.organizationCalendar orgcal on (c.calendarId=orgcal.calendarId ) "+
                        " left join rbac.organization org on (orgCal.organizationId =org.organizationId) "+
                        " where (c.tenantId=? or (c.tenantId in (select tenantId from rbac.tenant t, rbac.codes cd where t.tenantType=cd.codeId and cd.codeType='TENANT_TYPE' and cd.codeValue='Host')"+
                        " and org.tenantId=?)) and c.calendarId not in (select u.calendarId from rbac.userTable u where u.calendarId is not null) and c.isDeleted = 0  ) ) a");
                query.setParameter(1, tenantId);
                query.setParameter(2, tenantId);
                query.setParameter(3, tenantId);
                query.setParameter(4, tenantId);
            }
        }
        Integer result = (Integer) query.getSingleResult();
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUnmappedScheduleRules() {
        // get list of scheduleRule id where calendar(deleted/not-deleted) mapping is not found
        List<Long> resultList = calendarRepository.getUnmappedScheduleRules();
        if (resultList != null && !resultList.isEmpty()) {
            calendarRepository.deleteByScheduleRuleIds(resultList);
        }
    }

    @Override
    public List<OrgHolidaysCalendarResponseVO> getHolidaysByOrganization(LocalDate startDateToValidate,
                                                                         LocalDate endDateToValidate, int orgId) throws Exception {
        List<OrgHolidaysCalendarResponseVO> holidays = new ArrayList<OrgHolidaysCalendarResponseVO>();
        try {

            // if any of the inputs is received as null, it will return empty response
            if (startDateToValidate == null || endDateToValidate == null || orgId == 0) {
                // return holidays;
                throw new Exception("Either one of the inputs parameters is received as null or empty.");
            }

            if (startDateToValidate.isAfter(endDateToValidate)) {
                throw new Exception("Start date can't be after end date.");
            }

            // getting holiday calendar details:
            List<Object[]> results = null;
            try {
                TypedQuery<Object[]> query = em.createNamedQuery("getHolidaysByOrganization", Object[].class);
                query.setParameter(1, 19);
                query.setParameter(2, orgId);
                results = query.getResultList();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Exception occurred while fetching organization calendar w.r.t organizationId {}", orgId);
                throw new Exception("Exception occurred while fetching organization calendar w.r.t organizationId", e);
            }

            // getting work calendar details:
            Map<LocalDate, OrgWorkTimeDetails> orgWorkDateTimeDetails = getOrgWorkDateTimeDetails(orgId,
                    startDateToValidate, endDateToValidate);

            if (CollectionUtils.isNotEmpty(results)) {
                holidays = prepareHolidaysList(results, startDateToValidate, endDateToValidate, orgId,
                        orgWorkDateTimeDetails);
            }
        } catch (Exception e) {
            log.error("Unable to prepare the list of holidays from organization calendar w.r.t organizationId {}",
                    orgId);
            throw new Exception(e.getMessage(), e);
        }
        return holidays;
    }

    private LocalDate parseDate(String str) throws Exception {
        LocalDate date = null;
        try {
            date = LocalDate.parse(str);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            log.error("Exception occurred while parsing date from the fetched data from DB, due to :" + e.getMessage());
            throw new DateTimeParseException("failed to parse into String into LocalDate", str, 0, e);
        }
        return date;
    }

    private boolean isCalendarMatchedInputDates(LocalDate startDate, LocalDate endDate, LocalDate startDateToValidate,
                                                LocalDate endDateToValidate) {
        boolean isCalMatchedInDateRange = false;
        if (startDate != null & endDate != null) {
            if (startDateToValidate.equals(startDate) || startDateToValidate.equals(endDate)
                    || endDateToValidate.equals(startDate) || endDateToValidate.equals(endDate)) {
                isCalMatchedInDateRange = true;
            }
            if (startDateToValidate.isBefore(startDate) && endDateToValidate.isAfter(startDate)
                    && endDateToValidate.isBefore(endDate)) {
                isCalMatchedInDateRange = true;
            }
            if (startDateToValidate.isAfter(startDate) && startDateToValidate.isBefore(endDate)
                    && endDateToValidate.isAfter(endDate)) {
                isCalMatchedInDateRange = true;
            }
            if (startDateToValidate.isAfter(startDate) && startDateToValidate.isBefore(endDate)
                    && endDateToValidate.isAfter(startDate) && endDateToValidate.isBefore(endDate)) {
                isCalMatchedInDateRange = true;
            }
            if (startDate.isAfter(startDateToValidate) && startDate.isBefore(endDateToValidate)
                    && endDate.isAfter(startDateToValidate) && endDate.isBefore(endDateToValidate)) {
                isCalMatchedInDateRange = true;
            }
        }
        return isCalMatchedInDateRange;
    }

    private Map<LocalDate, OrgWorkTimeDetails> getOrgWorkDateTimeDetails(int orgId, LocalDate startDateToValidate,
                                                                         LocalDate endDateToValidate) throws Exception {
        Map<LocalDate, OrgWorkTimeDetails> orgWorkDateTimeDetails = new HashMap<LocalDate, OrgWorkTimeDetails>();
        List<Object[]> workCalResults = null;
        try {
            try {
                TypedQuery<Object[]> query = em.createNamedQuery("getHolidaysByOrganization", Object[].class);
                query.setParameter(1, 20);
                query.setParameter(2, orgId);
                workCalResults = query.getResultList();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Exception occurred while fetching organization calendar w.r.t organizationId {}", orgId);
                throw new Exception("Exception occurred while fetching organization calendar w.r.t organizationId", e);
            }

            for (Object[] workCalRecord : workCalResults) {
                String startDateStr = null;
                String endDateStr = null;

                if (workCalRecord[17] != null)
                    startDateStr = (String) workCalRecord[17];

                if (workCalRecord[18] != null)
                    endDateStr = (String) workCalRecord[18];

                LocalDate startDate = null;
                LocalDate endDate = null;
                if (startDateStr != null & endDateStr != null) {
                    try {
                        startDate = parseDate(startDateStr);
                        endDate = parseDate(endDateStr);
                    } catch (DateTimeParseException e) {
                        e.printStackTrace();
                        log.error(
                                "Exception occurred while parsing start & end date from the fetched work calendar data from DB, due to - "
                                        + e.getMessage());
                        throw new Exception(
                                "Exception occurred while parsing start & end date from the fetched work calendar data from DB.",
                                e);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        throw new Exception(
                                "Exception occurred while parsing start & end date from the fetched work calendar data from DB.",
                                e1);
                    }
                } else {
                    log.error(
                            "Failed due to - starteDate and endDate are received as null in work calendar details from DB.");
                    throw new Exception(
                            "Failed due to - starteDate and endDate are received as null in work calendar details from DB.");
                }

                boolean isCalMatchedInDateRange = isCalendarMatchedInputDates(startDate, endDate, startDateToValidate,
                        endDateToValidate);

                if (isCalMatchedInDateRange) {

                    int startTimeHours = 0;
                    int startTimeMins = 0;
                    int endTimeHours = 0;
                    int endTimeMins = 0;
                    try {
                        if (workCalRecord[21] != null) {
                            String hours = (String) workCalRecord[21];
                            char hoursArr[] = hours.toCharArray();
                            for (int i = 0; i <= hoursArr.length - 1; i++) {
                                if (hoursArr[i] != '0') {
                                    startTimeHours = i;
                                    if (hoursArr[startTimeHours] == '1')
                                        startTimeMins = 00;
                                    else if (hoursArr[startTimeHours] == '2' || hoursArr[startTimeHours] == '3')
                                        startTimeMins = 30;
                                    break;
                                }
                            }
                            for (int i = startTimeHours; i <= hoursArr.length - 1; i++) {
                                if (hoursArr[i] == '0') {
                                    endTimeHours = i - 1;
                                    if (hoursArr[endTimeHours] == '1')
                                        endTimeMins = 00;
                                    else if (hoursArr[endTimeHours] == '2' || hoursArr[endTimeHours] == '3')
                                        endTimeMins = 30;
                                    break;
                                }
                            }
                        } else {
                            throw new Exception(
                                    "Failed due to - fetched 'hours' as null form DB for work calendar details.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Exception occurred while finding the working hours duration of the organization.");
                        throw new Exception(
                                "Exception occurred while finding the working hours duration of the organization.", e);
                    }

                    LocalTime startTime = null;
                    LocalTime endTime = null;
                    try {
                        startTime = LocalTime.of(startTimeHours, startTimeMins);
                        endTime = LocalTime.of(endTimeHours, endTimeMins);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error(
                                "Exception occurred due to - couldn't parse the startTime and endTime for working hours of the organziation");
                        throw new Exception(
                                "Couldn't parse the startTime and endTime for working hours of the organziation", e);
                    }

                    LocalDate nextDayDate = startDate;
                    while (!nextDayDate.isAfter(endDate)) {
                        OrgWorkTimeDetails owtDetails = new OrgWorkTimeDetails();
                        owtDetails.setStartTime(startTime);
                        owtDetails.setEndTime(endTime);
                        orgWorkDateTimeDetails.put(nextDayDate, owtDetails);
                        nextDayDate = nextDayDate.plusDays(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(
                    "Exception occurred while preparing organization work calendar date hour details w.r.t organizationId {}",
                    orgId);
            throw new Exception(
                    "Exception occurred while preparing organization work calendar date hour details w.r.t organizationId",
                    e);
        }

        return orgWorkDateTimeDetails;
    }

    private List<OrgHolidaysCalendarResponseVO> prepareHolidaysList(List<Object[]> results,
                                                                    LocalDate startDateToValidate, LocalDate endDateToValidate, int orgId,
                                                                    Map<LocalDate, OrgWorkTimeDetails> orgWorkDateTimeDetails) throws Exception {
        List<OrgHolidaysCalendarResponseVO> holidays = new ArrayList<OrgHolidaysCalendarResponseVO>();
        try {
            Set<LocalDate> orgWorkDateTimeDetailsSet = orgWorkDateTimeDetails.keySet();
            for (Object[] record : results) {

                String startDateStr = null;
                String endDateStr = null;

                if (record[17] != null)
                    startDateStr = (String) record[17];

                if (record[18] != null)
                    endDateStr = (String) record[18];

                LocalDate startDate = null;
                LocalDate endDate = null;
                if (startDateStr != null & endDateStr != null) {
                    try {
                        startDate = parseDate(startDateStr);
                        endDate = parseDate(endDateStr);
                    } catch (DateTimeParseException e) {
                        e.printStackTrace();
                        log.error(
                                "Exception occurred while parsing start & end date from the fetched data from DB, due to :"
                                        + e.getMessage());
                        throw new Exception(
                                "Exception occurred while parsing start & end date from the fetched data from DB.", e);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        throw new Exception(
                                "Exception occurred while parsing start & end date from the fetched data from DB.", e1);
                    }
                } else {
                    log.error("Failed due to - starteDate and endDate are received as null from DB.");
                    throw new Exception("Failed due to - starteDate and endDate are received as null from DB.");
                }

                boolean isCalMatchedInDateRange = isCalendarMatchedInputDates(startDate, endDate, startDateToValidate,
                        endDateToValidate);

                List<String> nonWorkingDaysOfWeek = new ArrayList<String>();
                if (isCalMatchedInDateRange) {

                    if (record[2] == null) {
                        log.debug(
                                "there is no timeZone specified in the DB, so proceeding with the date as given in the DB.");
                    } else {
                        String timeZone = (String) record[2];
                        if (!StringUtils.equalsIgnoreCase("UTC", timeZone)) {
                            LocalDateTime localDateTime = null;

                            try {
                                // for start date
                                localDateTime = startDate.atTime(LocalTime.of(0, 0));
                                startDate = localDateTime.atZone(ZoneOffset.UTC).toLocalDateTime().toLocalDate();

                                // for end date
                                localDateTime = endDate.atTime(LocalTime.of(0, 0));
                                endDate = localDateTime.atZone(ZoneOffset.UTC).toLocalDateTime().toLocalDate();
                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error("Exception occurred while converting start/end date into UTC timezone.",
                                        e.getMessage());
                                throw new Exception(
                                        "Exception occurred while converting start/end date into UTC timezone.", e);
                            }
                        }
                    }

                    if (record[20] != null) {
                        String dayOfWeek = (String) record[20];
                        char dayOfWeekArr[] = dayOfWeek.toCharArray();
                        for (int i = 0; i <= dayOfWeekArr.length - 1; i++) {
                            if (dayOfWeekArr[i] != '-') {
                                if (dayOfWeekArr[i] == '1')
                                    nonWorkingDaysOfWeek.add("SUNDAY");
                                if (dayOfWeekArr[i] == '2')
                                    nonWorkingDaysOfWeek.add("MONDAY");
                                if (dayOfWeekArr[i] == '3')
                                    nonWorkingDaysOfWeek.add("TUESDAY");
                                if (dayOfWeekArr[i] == '4')
                                    nonWorkingDaysOfWeek.add("WEDNESDAY");
                                if (dayOfWeekArr[i] == '5')
                                    nonWorkingDaysOfWeek.add("THURSDAY");
                                if (dayOfWeekArr[i] == '6')
                                    nonWorkingDaysOfWeek.add("FRIDAY");
                                if (dayOfWeekArr[i] == '7')
                                    nonWorkingDaysOfWeek.add("SATURDAY");
                            }
                        }
                    } else {
                        throw new Exception("Failed due to - fetched 'dayOfWeek' as null from DB");
                    }

                    int startTimeHours = 0;
                    int startTimeMins = 0;
                    int endTimeHours = 0;
                    int endTimeMins = 0;
                    try {
                        if (record[21] != null) {
                            String hours = (String) record[21];
                            char hoursArr[] = hours.toCharArray();
                            for (int i = 0; i <= hoursArr.length - 1; i++) {
                                if (hoursArr[i] != '0') {
                                    startTimeHours = i;
                                    if (hoursArr[startTimeHours] == '1')
                                        startTimeMins = 00;
                                    else if (hoursArr[startTimeHours] == '2' || hoursArr[startTimeHours] == '3')
                                        startTimeMins = 30;
                                    break;
                                }
                            }
                            for (int i = startTimeHours; i <= hoursArr.length - 1; i++) {
                                if (hoursArr[i] == '0') {
                                    endTimeHours = i;
                                    if (hoursArr[endTimeHours] == '1')
                                        endTimeMins = 00;
                                    else if (hoursArr[endTimeHours - 1] == '2' || hoursArr[endTimeHours - 1] == '3') {
                                        endTimeHours = endTimeHours - 1;
                                        endTimeMins = 30;
                                    }
                                    break;
                                }
                            }
                        } else {
                            throw new Exception("Failed due to - fetched 'hours' as null form DB.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error(
                                "Exception occurred while finding the non-working hours duration of the organization.");
                        throw new Exception(
                                "Exception occurred while finding the non-working hours duration of the organization.",
                                e);
                    }

                    LocalTime startTime = null;
                    LocalTime endTime = null;
                    try {
                        startTime = LocalTime.of(startTimeHours, startTimeMins);
                        endTime = LocalTime.of(endTimeHours, endTimeMins);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error(
                                "Exception occurred due to - couldn't parse the startTime and endTime for non-working hours of the organziation");
                        throw new Exception(
                                "Couldn't parse the startTime and endTime for non-working hours of the organziation",
                                e);
                    }

                    try {
                        LocalDate nextDayDate = startDateToValidate;

                        // if in case startDateToValidate is very earlier date than the startDate,
                        // then we will update startDateToValidate to the startDate of the holiday
                        // period defined in database so that while loop not have to execute for the
                        // dates which are irrelevant to the holiday period for particular calendar
                        // record fetched from database
                        int diff = startDate.compareTo(nextDayDate);
                        if (diff > 0) {
                            nextDayDate = nextDayDate.plusDays(diff);
                        }

                        while (!nextDayDate.isAfter(endDateToValidate)) {
                            // condition that date should either be lying in between the the start and end
                            // date given or should be equals to either one of them
                            if ((nextDayDate.isAfter(startDate) && nextDayDate.isBefore(endDate))
                                    || nextDayDate.equals(startDate) || nextDayDate.equals(endDate)) {

                                if (nonWorkingDaysOfWeek.contains(nextDayDate.getDayOfWeek().name())) {

                                    // checking if the same date is existing there in the work calendar
                                    if (orgWorkDateTimeDetailsSet.contains(nextDayDate)) {
                                        OrgWorkTimeDetails orgWorkTimeDetails = orgWorkDateTimeDetails.get(nextDayDate);

                                        // if the working hours are lying within the start & end time of the holiday
                                        // (in case of same date), then we need to send start & end time as 00:00
                                        if ((orgWorkTimeDetails.getStartTime().equals(startTime)
                                                && orgWorkTimeDetails.getEndTime().equals(endTime))
                                                || (orgWorkTimeDetails.getStartTime().isAfter(startTime)
                                                && orgWorkTimeDetails.getEndTime().isBefore(endTime))
                                                || (orgWorkTimeDetails.getStartTime().isAfter(startTime)
                                                && orgWorkTimeDetails.getEndTime().equals(endTime))
                                                || (orgWorkTimeDetails.getStartTime().equals(startTime)
                                                && orgWorkTimeDetails.getEndTime().isBefore(endTime))) {
                                            holidays.add(new OrgHolidaysCalendarResponseVO(nextDayDate,
                                                    LocalTime.of(0, 0), LocalTime.of(0, 0), orgId));
                                        } else {
                                            holidays.add(new OrgHolidaysCalendarResponseVO(nextDayDate, startTime,
                                                    endTime, orgId));
                                        }
                                    } else {
                                        holidays.add(new OrgHolidaysCalendarResponseVO(nextDayDate, startTime, endTime,
                                                orgId));
                                    }
                                }
                                nextDayDate = nextDayDate.plusDays(1);
                            } else {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Exception occurred while preparing the list of holidays, due to -", e.getMessage());
                        throw new Exception("Exception occurred while preparing the list of holidays", e);
                    }
                } else {
                    log.debug("No calendar found from DB, matching with the input date range.");
                }
            }
        } catch (Exception e) {
            log.error("Unable to prepare the list of holidays from organization calendar w.r.t organizationId {}",
                    orgId);
            throw new Exception(e.getMessage(), e);
        }
        return holidays;
    }

    @Override
    public Boolean isValidTimezone(String timezone) {
        return Lookup.isTimeZoneValid(timezone);
    }

    @Override
    public String getTimeZoneByOrganizationId(Long organizationId) {
        Organization org = organizationMaintenanceDal.getById(organizationId);
        if(org != null && org.getOrganizationTimeZone() != null && !org.getOrganizationTimeZone().isEmpty())
            return org.getOrganizationTimeZone();
        else
            return "UTC";
    }

    @Override
    public String getTimezoneFromOrganization(Long organizationId, Long tenantId) {
        String timezone = RBACUtil.UTC;
        // If no organization Time zone then get Organization Calendar time zone
        try {
            OrganizationCalendar orgcal = orgCalendarDal.getByOrganizationId(organizationId);
            if (orgcal != null) {
                Calendar orgCalendarAssign = getById(orgcal.getCalendarId());
                if (orgCalendarAssign != null && orgCalendarAssign.getTimeZone() != null
                        && !orgCalendarAssign.getTimeZone().isEmpty())
                    timezone = orgCalendarAssign.getTimeZone();
                return timezone;
            }

            // If no organization time zone then check tenant calendar timezone
            OrganizationCalendar organizationCalendarForTenant = orgCalendarDal.getByOrganizationId(tenantId);
            Calendar calendarForTenant = null;
            /* Check if the tenant Has calendar or not */
            if (organizationCalendarForTenant != null) {
                calendarForTenant = getById(organizationCalendarForTenant.getCalendarId());
            } else {
                /* if the tenant Has no calendar assign then check for host */
                organizationCalendarForTenant = orgCalendarDal.getByOrganizationId(100L);
                if(organizationCalendarForTenant != null)
                    calendarForTenant = getById(organizationCalendarForTenant.getCalendarId());
            }

            /* Check if calendar has timezone or not */
            if (calendarForTenant != null && calendarForTenant.getTimeZone() != null
                    && !calendarForTenant.getTimeZone().isEmpty())
                timezone = calendarForTenant.getTimeZone();
            else {
                // if no organization time zone then return default calendar time zone
                Long defaultCalendar = Lookup.getDefaultWorkCalendarIdByOrganization(organizationId);
                if (defaultCalendar != null) {
                    Calendar defaultOrgCalendar = getById(defaultCalendar);
                    timezone = defaultOrgCalendar != null ? defaultOrgCalendar.getTimeZone() : null;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("{}", ex.getMessage());
            return "UTC";
        }

        log.info("Organization {} timezone  = {}", organizationId, timezone);
        return timezone;
    }

    private void validateEntry(Calendar calendar) {
        if(calendar.getName()==null){
            ErrorInfoException errorInfo = new ErrorInfoException(CALENDAR_BLANK);
            throw errorInfo;
        }

        Calendar cal = getCalendarByCalendarName(calendar.getName());
        if(cal!=null){
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_CALENDAR).append("; ");
            sb.append(DUPLICATED_CALENDAR_NAME).append("=").append(calendar.getName());
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_CALENDAR, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_CALENDAR_NAME, calendar.getName());
            log.info("create; calendarerrorInfo={}", errorInfo);
            throw errorInfo;
        }

    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    private void validateScheduleRuleDefault(ScheduleRule scheduleRule) {
        if(scheduleRule.getScheduleRuleDefault()!=null){
            if(scheduleRule.getScheduleRuleDefault().getScheduleRuleDefaultId()==null){
                scheduleRule.setScheduleRuleDefault(null);
                return;
            }
            if(scheduleRule.getScheduleRuleDefault().getScheduleRuleDefaultId()!=null && em.find(ScheduleRuleDefault.class, scheduleRule.getScheduleRuleDefault().getScheduleRuleDefaultId())==null){
                throw new IllegalArgumentException();
            }
        }
    }

    private static void updateScheduleRule(ScheduleRule scheduleRuleDb,
                                           ScheduleRule scheduleRule) {
        scheduleRuleDb.setDescription(scheduleRule.getDescription());
        scheduleRuleDb.setDayOfWeek(scheduleRule.getDayOfWeek());
        scheduleRuleDb.setFromDate(scheduleRule.getFromDate());
        scheduleRuleDb.setHour(scheduleRule.getHour());
        scheduleRuleDb.setIsOpen(scheduleRule.getIsOpen());
        scheduleRuleDb.setMonthOfYear(scheduleRule.getMonthOfYear());
        scheduleRuleDb.setName(scheduleRule.getName());
        scheduleRuleDb.setRepeatInterval(scheduleRule.getRepeatInterval());
        scheduleRuleDb.setScheduleRuleSubType(scheduleRule
                .getScheduleRuleSubType());
        scheduleRuleDb.setScheduleRuleType(scheduleRule.getScheduleRuleType());
        scheduleRuleDb.setToDate(scheduleRule.getToDate());
        scheduleRuleDb.setScheduleRuleDefault(scheduleRule.getScheduleRuleDefault());
    }

    private void validateScheduleRuleName(Calendar calendar) {
        if(calendar.getName()==null){
            ErrorInfoException errorInfo = new ErrorInfoException(CALENDAR_BLANK);
            throw errorInfo;
        }
        if (calendar.getRules() != null && !calendar.getRules().isEmpty()) {
            for (ScheduleRule scheduleRule : calendar.getRules()) {
//                Query query = em.createNativeQuery("select * from rbac.scheduleRule sr,rbac.calendarScheduleRuleMapping csrm where sr.scheduleRuleId=csrm.scheduleRuleId and  sr.name=? and csrm.calendarId  = ?");
//                query.setParameter(1, scheduleRule.getName());
//                query.setParameter(2, calendar.getCalendarId());
//                List QueryResult = query.getResultList();
                List QueryResult = scheduleRuleRepository.getScheduleRule(scheduleRule.getName(), calendar.getCalendarId());
                if(QueryResult.size() > 1){
                    StringBuilder sb = new StringBuilder();
                    sb.append(DUPLICATED_SCHEDULE).append("; ");
                    sb.append(DUPLICATED_SCHEDULE_NAME).append("=").append(scheduleRule.getName());
                    log.info("create/update; {}", sb.toString());
                    ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_SCHEDULE, sb.toString());
                    errorInfo.getParameters().put(DUPLICATED_SCHEDULE_NAME, scheduleRule.getName());
                    log.info("create/update; calendarerrorInfo={}", errorInfo);
                    throw errorInfo;
                }
            }
        }
    }

    private Map<String, String> setNewObjectChangeSetLocal(Calendar oldCalendar, Calendar newCalendar) {
        //clearObjectChangeSet();
        AuditLogHelperUtil logHelperUtil =  new AuditLogHelperUtil();
        logHelperUtil.putToObjectChangeSet(OBJECTNAME, newCalendar!=null?newCalendar.getName():oldCalendar.getName());
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_CALENDARID, newCalendar.getCalendarId(), (oldCalendar!=null)?oldCalendar.getCalendarId(): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_CALENDARNAME, newCalendar.getName(), (oldCalendar!=null)?oldCalendar.getName(): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_CALENDARTIMEZONE, newCalendar.getTimeZone(), (oldCalendar!=null)?oldCalendar.getTimeZone(): null, null, null);
        if(newCalendar.getCalendarType() != null){
            logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_CALENDARTYPE, Lookup.getCodeValueById(newCalendar.getCalendarType().getCodeId()), (oldCalendar!=null && oldCalendar.getCalendarSubType() != null)?Lookup.getCodeValueById(oldCalendar.getCalendarType().getCodeId()) : null, null, null);
        }
        if(newCalendar.getCalendarSubType() != null){
            logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_CALENDARSUBTYPE, Lookup.getCodeValueById(newCalendar.getCalendarSubType().getCodeId()), (oldCalendar!=null && oldCalendar.getCalendarSubType() != null)?Lookup.getCodeValueById(oldCalendar.getCalendarSubType().getCodeId()) : null, null, null);
        }
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_CALENDAR_SHARINGTYPE, newCalendar.getSharingType(), (oldCalendar!=null)?newCalendar.getSharingType(): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_CALENDAR_ISACTIVE, newCalendar.getIsActive(), (oldCalendar!=null)?oldCalendar.getIsActive(): null, null, null);

        return logHelperUtil.getObjectChangeSet();
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options
                .getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null
                : optionFilter.getFilters();
        if (filters != null) {
            String name = filters.get("name");
            if (name != null && name.length() > 0) {
                result.addCondition("c.name = :name");
                result.addParameter("name", name);
            }

        }
        return result;
    }
    private Filters prepareFiltersForScheduleRules(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options
                .getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null
                : optionFilter.getFilters();
        if (filters != null) {
            String name = filters.get("name");
            if (name != null && name.length() > 0) {
                result.addCondition("s.name = :name");
                result.addParameter("name", name);
            }

        }
        return result;
    }
}
