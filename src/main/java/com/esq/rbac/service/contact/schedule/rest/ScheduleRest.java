package com.esq.rbac.service.contact.schedule.rest;

import com.esq.rbac.service.base.error.RestErrorMessages;
import com.esq.rbac.service.base.exception.RestException;
import com.esq.rbac.service.base.rest.BaseRest;
import com.esq.rbac.service.contact.helpers.ContactUserRest;
import com.esq.rbac.service.contact.schedule.domain.Schedule;
import com.esq.rbac.service.contact.schedule.embedded.ScheduleRule;
import com.esq.rbac.service.contact.schedule.helpers.ScheduleFlatMessage;
import com.esq.rbac.service.contact.schedule.helpers.ScheduleFlatMessageContainer;
import com.esq.rbac.service.contact.schedule.repository.ScheduleRepository;
import com.esq.rbac.service.targetoperations.TargetOperations;
import com.esq.rbac.service.util.ContactAuditUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
@RestController
@RequestMapping("/schedule")
//Todo Same Resource_Path as MaintenanceRest
//@ManagedResource(objectName = "com.esq.dispatcher.contacts:type=REST,name=ScheduleRest")
public class ScheduleRest extends BaseRest<Schedule> {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String TIME_ZONE = "timeZone";
    private static final String UPDATED_TIME = "updatedTime";
    private static final Set<String> FILTER_COLUMNS;
    private static final Set<String> ORDER_COLUMNS;
    private static final Set<String> SEARCH_COLUMNS;

    static {
        FILTER_COLUMNS = new HashSet<String>(Arrays.asList(NAME, TIME_ZONE));

        ORDER_COLUMNS = new HashSet<String>(Arrays.asList(ID, NAME, TIME_ZONE, UPDATED_TIME, PARAM_TENANT_ID));

        SEARCH_COLUMNS = new HashSet<String>(Arrays.asList(NAME, TIME_ZONE));
    }

    private final ScheduleRepository scheduleRepository;
    private ContactUserRest userRest;

    @Autowired
    public ScheduleRest(ScheduleRepository scheduleRepository) {
        super(Schedule.class, scheduleRepository);
        this.scheduleRepository = scheduleRepository;
    }

    private static void verifySchedule(Schedule schedule) throws Exception {
        if (schedule.getRules() == null) {
            return;
        }
        for (ScheduleRule rule : schedule.getRules()) {
            verifyScheduleRule(rule);
        }
    }

    protected static void verifyScheduleRule(ScheduleRule rule) throws Exception {
        Date fromDate = parseDate(rule.getFromDate());
        Date toDate = parseDate(rule.getToDate());

        if (fromDate != null && toDate != null && fromDate.getTime() > toDate.getTime()) {
            throw new RestException(RestErrorMessages.INVALID_FROM_TO_DATE, "Invalid from/to date", rule.getFromDate(), rule.getToDate());
        }
    }

    private static Date parseDate(String date) {
        try {
            return (date != null && date.length() > 0) ? dateFormat.parse(date) : null;
        } catch (Exception e) {
            throw new RestException(RestErrorMessages.INVALID_DATE, "Invalid date", date);
        }
    }

    @Autowired
    public void setUserRest(ContactUserRest userRest) {
        log.trace("setUserRest;");
        this.userRest = userRest;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Schedule> create(@RequestBody Schedule schedule, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        int result = 0;
        try {
            result = scheduleRepository.scheduleNameSearch(schedule.getName().trim(), schedule.getTenantId());
        } catch (Exception e1) {
            log.warn("create;exception={}", e1.getMessage());
        }
        if (result != 0) {
            logException("create;exception1={}", new RestException(RestErrorMessages.CREATE_SCHEDULE_FAILED, "Failed to create resource"));
            throw new RestException(RestErrorMessages.CREATE_SCHEDULE_FAILED, "Failed to create resource", schedule.getName().trim());
        }
        verifySchedule(schedule);
        ResponseEntity<Schedule> response = super.create(schedule);
        Schedule createdSchedule = super.readById(schedule.getId()).getBody();
        log.debug("create; response={}", response);
        try {
            userRest.createAuditLog(TargetOperations.SCHEDULE_TARGET_NAME, TargetOperations.CREATE_OPERATION, ContactAuditUtil.convertToJSON(createdSchedule, TargetOperations.CREATE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("create;exception={}", e);
        }
        return response;
    }

    @SuppressWarnings("unused")
    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Schedule> update(@PathVariable("id") long id, @RequestBody Schedule schedule, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        log.debug("update; {}", schedule.toString());
        Schedule savedSchedule = super.readById(id).getBody();
        if (!savedSchedule.getName().trim().equalsIgnoreCase(schedule.getName().trim())) {
            int result = 0;
            try {
                result = scheduleRepository.scheduleNameSearch(schedule.getName().trim(), schedule.getTenantId());
            } catch (Exception e1) {
                log.warn("update;exception={}", e1.getMessage());
            }
            if (result != 0) {
                logException("update;exception={}", new RestException(RestErrorMessages.UPDATE_SCHEDULE_FAILED, "Failed to update resource"));
                throw new RestException(RestErrorMessages.UPDATE_SCHEDULE_FAILED, "Failed to update resource", schedule.getName().trim());
            }
        }
        verifySchedule(schedule);
        ResponseEntity<Schedule> response = null;
        Map<String, String> compareStringMap = ContactAuditUtil.compareObject(savedSchedule, schedule);
        response = super.update(id, schedule);
        Schedule newSchedule = super.readById(id).getBody();
        log.debug("update; response={}", response);
        try {
            userRest.createAuditLog(TargetOperations.SCHEDULE_TARGET_NAME, TargetOperations.UPDATE_OPERATION, compareStringMap, headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("update;exception={}", e);
        }
        return ResponseEntity.ok().body(newSchedule);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public ResponseEntity<Object[]> list(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) {
        //To get the default schedule for the all the users we have to remove the tenant scope from schedule list
        if (headers != null && headers.get(PARAM_TENANT_SCOPE) != null) {
            headers.get(PARAM_TENANT_SCOPE).add(0, null);
        }
        return super.list(request, headers);
    }

    @GetMapping(value = "/flat", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<ScheduleFlatMessageContainer> listFlat() {
        List<Schedule> list = scheduleRepository.getQuery().list();
        ScheduleFlatMessageContainer result = new ScheduleFlatMessageContainer();
        for (Schedule s : list) {

            //schedule rules
            List<ScheduleRule> rules = s.getRules();
            for (ScheduleRule sr : rules) {
                ScheduleFlatMessage schedule = new ScheduleFlatMessage();
                schedule.setName(s.getName());

                schedule.setStartHour(sr.getFromDate());
                schedule.setEndHour(sr.getToDate());

                boolean open = sr.getIsOpen();
                if (open) {
                    schedule.setAvlCategoryName("IN_SCHEDULE");
                } else {
                    schedule.setAvlCategoryName("NOT_IN_SCHEDULE");
                }
                result.getSchedules().add(schedule);
            }
        }
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(result);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") long id, @RequestHeader org.springframework.http.HttpHeaders headers) {
        ResponseEntity<Schedule> res = super.readById(id);
        Schedule objectSchedule = res.getBody();
        int result = 0;
        try {
            result = scheduleRepository.slaSearch(id);
        } catch (Exception e1) {
            log.warn("deleteById;exception={}", e1);
        }
        if (result != 0) {
            logException("deleteById;exception={}", new RestException(RestErrorMessages.DELETE_NOT_ALLOWED_SCHEDULE, "Failed to delete resource"));
            throw new RestException(RestErrorMessages.DELETE_NOT_ALLOWED_SCHEDULE, "Failed to delete resource");
        }

        try {
            super.deleteById(id);
            userRest.createAuditLog(TargetOperations.SCHEDULE_TARGET_NAME, TargetOperations.DELETE_OPERATION, ContactAuditUtil.convertToJSON(objectSchedule, TargetOperations.DELETE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("deleteById;exception={}", e);
        }
    }

    @Override
    protected Set<String> getFilterColumns() {
        return FILTER_COLUMNS;
    }

    @Override
    protected Set<String> getSearchColumns() {
        return SEARCH_COLUMNS;
    }

    @Override
    protected Set<String> getOrderColumns() {
        return ORDER_COLUMNS;
    }
}
