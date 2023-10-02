package com.esq.rbac.service.application.applicationmaintenance.service;

import com.esq.rbac.service.application.applicationmaintenance.domain.ApplicationMaintenance;
import com.esq.rbac.service.application.applicationmaintenance.repository.ApplicationMaintenanceRepository;
import com.esq.rbac.service.application.applicationmaintenance.util.ApplicationMaintenanceRefreshDal;
import com.esq.rbac.service.application.childapplication.repository.ChildApplicationRepository;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class ApplicationMaintenanceDalJpa extends BaseDalJpa implements ApplicationMaintenanceDal{


    public static final String DATE_RANGE_ENTRY_FOUND = "dateRangeEntryFound";
    private static final String INVALID_DATE_ENTRY = "invalidDateEntry";
    private static final String DATE_ENTRY_INCORRECT = "dateEntryIncorrect";

    private static final Map<String, String> SORT_COLUMNS;

    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("isEnabled", "a.isEnabled");
        SORT_COLUMNS.put("fromDate", "a.fromDate");
    }


    private ThreadPoolTaskScheduler taskScheduler;


    @Autowired
    private ApplicationMaintenanceRepository applicationMaintenanceRepository;



    private ApplicationMaintenanceRefreshDal applicationMaintenanceRefreshDal;


    @Autowired
    private ChildApplicationRepository childApplicationRepository;



    @Autowired
    public void setApplicationMaintenanceRefreshDal(
            ApplicationMaintenanceRefreshDal applicationMaintenanceRefreshDal) {
        log.trace("setApplicationMaintenanceRefreshDal; {}",
                applicationMaintenanceRefreshDal);
        this.applicationMaintenanceRefreshDal = applicationMaintenanceRefreshDal;
        refreshAppMaintenanceCache(DateTime.now());
    }

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager; {}", em);
        this.em = em;
        this.entityClass = ApplicationMaintenance.class;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApplicationMaintenance create(
            ApplicationMaintenance applicationMaintenance, int userId) {
        validateEntry(applicationMaintenance);
        applicationMaintenance.setCreatedBy(userId);
        applicationMaintenance.setCreatedOn(DateTime.now().toDate());
        applicationMaintenance.setIsExpired(false);
//        em.persist(applicationMaintenance);
        applicationMaintenanceRepository.save(applicationMaintenance);
        setObjectChangeSet(null, applicationMaintenance);
        return applicationMaintenance;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApplicationMaintenance update(
            ApplicationMaintenance applicationMaintenance, int userId) {
        if (applicationMaintenance.getMaintenanceId() == null) {
            throw new IllegalArgumentException("maintenanceId missing");
        }
//        ApplicationMaintenance existingApplicationMaintenance = em.find(
//                ApplicationMaintenance.class,
//                applicationMaintenance.getMaintenanceId());
        ApplicationMaintenance existingApplicationMaintenance = applicationMaintenanceRepository.findById(applicationMaintenance.getMaintenanceId()).orElse(null);
        if (existingApplicationMaintenance == null) {
            throw new IllegalArgumentException("maintenanceId invalid");
        }
        validateExistingEntry(applicationMaintenance);
        setObjectChangeSet(existingApplicationMaintenance,
                applicationMaintenance);

        existingApplicationMaintenance.setCreatedBy(userId);
        existingApplicationMaintenance.setCreatedOn(DateTime.now().toDate());
        existingApplicationMaintenance.setFromDate(applicationMaintenance
                .getFromDate());
        existingApplicationMaintenance.setToDate(applicationMaintenance
                .getToDate());
        existingApplicationMaintenance.setIsEnabled(applicationMaintenance
                .getIsEnabled());
        existingApplicationMaintenance.setMessage(applicationMaintenance
                .getMessage());
        existingApplicationMaintenance.setIsExpired(applicationMaintenance
                .getIsExpired());
//        ApplicationMaintenance returnObj = em
//                .merge(existingApplicationMaintenance);
//        return returnObj;
        return applicationMaintenanceRepository.save(existingApplicationMaintenance);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(int maintenanceId) {
//        ApplicationMaintenance applicationMaintenance = em.find(
//                ApplicationMaintenance.class, maintenanceId);
        ApplicationMaintenance applicationMaintenance = applicationMaintenanceRepository.findById(maintenanceId).orElse(null);
        if (applicationMaintenance == null) {
            throw new IllegalArgumentException("maintenanceId invalid");
        }
        setObjectChangeSet(applicationMaintenance, null);
//        em.remove(applicationMaintenance);
        applicationMaintenanceRepository.delete(applicationMaintenance);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<ApplicationMaintenance> getList(Options options) {
        Filters filters = prepareFilters(options);
        return filters
                .getList(em, ApplicationMaintenance.class,
                        "select a from ApplicationMaintenance a", options,
                        SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int getCount(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getCount(em,
                "select count(a) from ApplicationMaintenance a");
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public ApplicationMaintenance getById(int maintenanceId) {
//        return em.find(ApplicationMaintenance.class, maintenanceId);
        return applicationMaintenanceRepository.findById(maintenanceId).orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<ApplicationMaintenance> getAppsUnderMaintenance() {
        try {
//            TypedQuery<ApplicationMaintenance> query = em.createNamedQuery(
//                    "getAppsUnderMaintenance", ApplicationMaintenance.class);
//            query.setParameter("currentTime", new Date());
//            return query.getResultList();
            return applicationMaintenanceRepository.getAppsUnderMaintenance(new Date());
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Date getMinimumRefreshTime() {
        try {
//            TypedQuery<Date> query = em.createNamedQuery(
//                    "getMinimumRefreshTime", Date.class);
//            return query.getSingleResult();
            return applicationMaintenanceRepository.getMinimumRefreshTime();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Date getNextRefreshTime(Date currentTime) {
        if (currentTime == null) {
            return null;
        }
        try {
//            TypedQuery<Date> query = em.createNamedQuery("getNextRefreshTime",
//                    Date.class);
//            query.setParameter("currentTime", new Date());
//            query.setParameter("toDateParam", currentTime);
//            query.setMaxResults(1);
//            return query.getSingleResult();
            return applicationMaintenanceRepository.getNextRefreshTime(new Date(), currentTime).get(0);
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void updateExpiredFlag() {
        try {
//            Query query = em.createNamedQuery("updateExpiredFlag");
//            query.setParameter("currentTime", new Date());
//            int result = query.executeUpdate();
//            log.info("updateExpiredFlag; expiredCount={}", result);
            int result = applicationMaintenanceRepository.updateExpiredFlag(new Date());
            log.info("updateExpiredFlag; expiredCount={}", result);
        } catch (NoResultException e) {
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public synchronized void refreshAppMaintenanceCache(DateTime nextRefreshTime) {
        if (taskScheduler != null) {
            taskScheduler.destroy();
            log.info("refreshAppMaintenanceCache; taskScheduler destroyed");
            taskScheduler = null;
        }
        log.info("refreshAppMaintenanceCache; nextRefreshTime={}",
                nextRefreshTime);
        if (nextRefreshTime == null) {
            return;
        }
//Todo        taskScheduler = new ThreadPoolTaskScheduler();
//        taskScheduler.initialize();
//        taskScheduler.schedule(runNewRefresh(), nextRefreshTime.toDate());
    }



// Todo   public Runnable runNewRefresh() {
//        return new Runnable() {
//            @Override
//            public void run() {
//                    log.info("run; started updating ApplicationMaintenanceCache");
//                    applicationMaintenanceRefreshDal.updateExpiredFlag();
//                    List<ApplicationMaintenance> appmaintenanceList = applicationMaintenanceRefreshDal
//                            .getAppsUnderMaintenance();
//                    Map<String, ApplicationDownInfo> newCacheMap = new HashMap<String, ApplicationDownInfo>();
//                    if (appmaintenanceList != null && !appmaintenanceList.isEmpty()) {
//                        log.info("run; appmaintenanceList={}", appmaintenanceList);
//                        for (ApplicationMaintenance appMaintenance : appmaintenanceList) {
//                            log.info("run; maintenanceId={}; currentDate={}; maintenanceToDate={};", appMaintenance.getMaintenanceId(), new Date(), appMaintenance.getToDate());
//                            if (appMaintenance.getToDate().before(new Date())) {
//                                // appMaintenance.setIsExpired(true);
//                                applicationMaintenanceRefreshDal
//                                        .updateExpiredFlag();
//                            } else {
////                            String childAppName = em.find(ChildApplication.class,
////                                            appMaintenance.getChildApplicationId())
////                                    .getChildApplicationName();
//                                String childAppName = childApplicationRepository.findById(appMaintenance.getMaintenanceId()).get().getChildApplicationName();
//                                ApplicationDownInfo temp = new ApplicationDownInfo(childAppName);
//                                temp.setMessage(appMaintenance.getMessage());
//                                temp.setFromDate(appMaintenance.getFromDate());
//                                temp.setToDate(appMaintenance.getToDate());
//                                newCacheMap.put(childAppName.toLowerCase()
//                                        , temp);
//                            }
//                        }
//                        log.info("run; newCacheMap={}", newCacheMap);
//                        refreshAppMaintenanceCache(getCurrentTimeZoneTime(getNextRefreshTime(getCurrentTimeZoneTime(getMinimumRefreshTime()).toDate())));
//                    } else {
//                        refreshAppMaintenanceCache(getCurrentTimeZoneTime(getMinimumRefreshTime()));
//                    }
//                    ApplicationMaintenanceCache.replaceCache(newCacheMap);
//                }
//
//
//        };
//    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteByChildApplicationId(int childApplicationId) {
//        Query query = em.createQuery("delete from ApplicationMaintenance a where a.childApplicationId= :childApplicationId");
//        query.setParameter("childApplicationId", childApplicationId);
//        int result = query.executeUpdate();
        int result = applicationMaintenanceRepository.deleteByChildApplicationId(childApplicationId);
        log.info("deleteByChildApplicationId; result={}",result);
    }



    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public void validateEntry(ApplicationMaintenance appMaintenance) {
        if(appMaintenance.getFromDate()==null || appMaintenance.getToDate()==null){
            ErrorInfoException errorInfo = new ErrorInfoException(
                    DATE_ENTRY_INCORRECT);
            throw errorInfo;
        }
        if(appMaintenance.getFromDate().after(appMaintenance.getToDate())){
            ErrorInfoException errorInfo = new ErrorInfoException(
                    INVALID_DATE_ENTRY);
            throw errorInfo;
        }
        try {
//            TypedQuery<Long> query = em.createNamedQuery(
//                    "checkForDuplicate", Long.class);
//            query.setParameter("childApplicationId", appMaintenance.getChildApplicationId());
//            query.setParameter("fromDate", appMaintenance.getFromDate());
//            query.setParameter("toDate", appMaintenance.getToDate());
//            long count = query.getSingleResult();
            long count = applicationMaintenanceRepository.checkForDuplicate(appMaintenance.getChildApplicationId(),
                    appMaintenance.getFromDate(), appMaintenance.getToDate());
            if (count != 0) {
                ErrorInfoException errorInfo = new ErrorInfoException(
                        DATE_RANGE_ENTRY_FOUND);
                throw errorInfo;
            }
        } catch (NoResultException e) {
        }
    }

    private void setObjectChangeSet(ApplicationMaintenance oldAppMaintain,
                                    ApplicationMaintenance newAppMaintain) {
        clearObjectChangeSet();

//        String applicationName = Lookup
//                .getChildApplicationName(oldAppMaintain != null ? oldAppMaintain
//                        .getChildApplicationId() : newAppMaintain.getChildApplicationId());

        String applicationName = Lookup
                .getChildApplicationName(oldAppMaintain != null ? oldAppMaintain
                        .getChildApplicationId() : newAppMaintain.getChildApplicationId());

        /*
         * putToObjectChangeSet(OBJECTCHANGES_MAINTENANCEID, newAppMaintain
         * .getMaintenanceId().toString());
         */
        putToObjectChangeSet(OBJECTNAME, applicationName);

        checkPutToObjectChangeSet(OBJECTCHANGES_FROMDATE,
                newAppMaintain != null ? new DateTime(newAppMaintain.getFromDate(), DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z") : null,
                oldAppMaintain != null ? new DateTime(oldAppMaintain.getFromDate(), DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z") : null,
                null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_TODATE,
                newAppMaintain != null ? new DateTime(newAppMaintain.getToDate(), DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z") : null,
                oldAppMaintain != null ? new DateTime(oldAppMaintain.getToDate(), DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z") : null,
                null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_MESSAGE,
                newAppMaintain != null ? newAppMaintain.getMessage() : null,
                oldAppMaintain != null ? oldAppMaintain.getMessage() : null,
                null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_MAINTENANCEISENABLED,
                newAppMaintain != null ? newAppMaintain.getIsEnabled() : null,
                oldAppMaintain != null ? oldAppMaintain.getIsEnabled() : null,
                null, null);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public void validateExistingEntry(ApplicationMaintenance appMaintenance) {
        if(appMaintenance.getFromDate()==null || appMaintenance.getToDate()==null){
            ErrorInfoException errorInfo = new ErrorInfoException(
                    DATE_ENTRY_INCORRECT);
            throw errorInfo;
        }
        if(appMaintenance.getFromDate().after(appMaintenance.getToDate())){
            ErrorInfoException errorInfo = new ErrorInfoException(
                    INVALID_DATE_ENTRY);
            throw errorInfo;
        }
        try {
//            TypedQuery<Long> query = em.createNamedQuery(
//                    "checkForDuplicateWithMaintenanceId", Long.class);
//            query.setParameter("childApplicationId", appMaintenance.getChildApplicationId());
//            query.setParameter("fromDate", appMaintenance.getFromDate());
//            query.setParameter("toDate", appMaintenance.getToDate());
//            query.setParameter("maintenanceId", appMaintenance.getMaintenanceId());
//            long count = query.getSingleResult();
            long count = applicationMaintenanceRepository.checkForDuplicateWithMaintenanceId(appMaintenance.getChildApplicationId(),
                    appMaintenance.getFromDate(),
                    appMaintenance.getToDate(),
                    appMaintenance.getMaintenanceId());
            if (count != 0) {
                ErrorInfoException errorInfo = new ErrorInfoException(
                        DATE_RANGE_ENTRY_FOUND);
                throw errorInfo;
            }
        } catch (NoResultException e) {
        }
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options
                .getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null
                : optionFilter.getFilters();
        if (filters != null) {

            String applicationId = filters.get("applicationId");
            if (applicationId != null && applicationId.length() > 0) {
                result.addCondition("a.applicationId = :applicationId");
                result.addParameter("applicationId",
                        Integer.valueOf(applicationId));
            }

            String childApplicationId = filters.get("childApplicationId");
            if (childApplicationId != null && !childApplicationId.equals("null") && childApplicationId.length() > 0) {
                result.addCondition("a.childApplicationId = :childApplicationId");
                result.addParameter("childApplicationId",
                        Integer.valueOf(childApplicationId));
            }

            String fromDate = filters.get("fromDate");
            if (fromDate != null && fromDate.length() > 0) {
                result.addCondition("a.fromDate = :fromDate");
                result.addParameter("fromDate", fromDate);
            }

            String toDate = filters.get("toDate");
            if (toDate != null && toDate.length() > 0) {
                result.addCondition("a.toDate = :toDate");
                result.addParameter("toDate", toDate);
            }

            String message = filters.get("message");
            if (message != null && message.length() > 0) {
                result.addCondition("a.message = :message");
                result.addParameter("message", message);
            }

            String isEnabled = filters.get("isEnabled");
            if (isEnabled != null && isEnabled.length() > 0) {
                result.addCondition("a.isEnabled = :isEnabled");
                result.addParameter("isEnabled", Boolean.valueOf(isEnabled));
            }

            String isExpired = filters.get("isExpired");
            if (isExpired != null && isExpired.length() > 0) {
                result.addCondition("a.isExpired = :isExpired");
                result.addParameter("isExpired", Boolean.valueOf(isExpired));
            }

        }
        return result;
    }
    public static DateTime getCurrentTimeZoneTime(Date currentTime) {
        if (currentTime == null) {
            return null;
        }
        long mills = currentTime.getTime();
        return new DateTime(mills + TimeZone.getDefault().getOffset(mills));
    }
}
