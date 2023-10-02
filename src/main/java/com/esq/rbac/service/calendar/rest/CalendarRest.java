package com.esq.rbac.service.calendar.rest;

import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.schedulerule.domain.ScheduleRule;
import com.esq.rbac.service.schedulerule.scheduledefaultsubdomain.domain.ScheduleRuleDefault;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.calendar.domain.Calendar;
import com.esq.rbac.service.calendar.service.CalendarDal;
import com.esq.rbac.service.exception.ErrorInfo;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.organizationcalendar.service.OrganizationCalendarDal;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.organization.vo.OrgHolidaysCalendarRequestVO;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.*;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.validation.annotation.ValidationRules;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@RequestMapping("calendars")
@Slf4j
public class CalendarRest {

    public final CalendarDal calendarDal;
//    @Autowired
    public final OrganizationCalendarDal organizationCalendarDal;

    public final UserDal userDal;

    public final OrganizationMaintenanceDal organizationMaintenanceDal;

    public final DeploymentUtil deploymentUtil;

    public final Validator validator;

    public CalendarRest(CalendarDal calendarDal,OrganizationMaintenanceDal organizationMaintenanceDal,
                        OrganizationCalendarDal organizationCalendarDal,DeploymentUtil deploymentUtil,
                        Validator validator,UserDal userDal){
        this.calendarDal=calendarDal;
        this.deploymentUtil=deploymentUtil;
        this.validator=validator;
        this.organizationMaintenanceDal=organizationMaintenanceDal;
        this.userDal=userDal;
        this.organizationCalendarDal=organizationCalendarDal;
    }

    @EventListener
    public void fillOrganizationDefaultCalendars(ApplicationStartedEvent event){
        log.trace("fillOrganizationDefaultCalendars");
        Lookup.fillOrganizationDefaultCalendars(this.organizationCalendarDal
                        .getOrganizationDefaultWorkCalendar(),
                this.organizationCalendarDal
                        .getOrganizationDefaultHolidayCalendars());
    }

    @GetMapping("/validationRules")
    public ResponseEntity<ValidationRules> getValidationRules() {
        ValidationRules validationRules = new ValidationRules();
        validationRules.getFieldRulesList().addAll(ValidationUtil.retrieveValidationRules(Calendar.class));
        return ResponseEntity.ok().body(validationRules);
    }



    @PostMapping
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public ResponseEntity<Calendar> create(@RequestHeader HttpHeaders headers, @RequestBody Calendar calendar)
            throws Exception {
        log.trace("create; calendar={}", calendar);
        validate(calendar);
        synchronized(this){
            Calendar retCalendar = calendarDal.create(
                    calendar,
                    new AuditLogInfo(Integer.parseInt(Objects.requireNonNull(headers.get(
                            "userId")).get(0)), Objects.requireNonNull(headers.get(
                            "clientIp")).get(0)));
            Lookup.fillOrganizationDefaultCalendars(organizationCalendarDal
                    .getOrganizationDefaultWorkCalendar(), organizationCalendarDal
                    .getOrganizationDefaultHolidayCalendars());
            calendarDal.deleteUnmappedScheduleRules();
            return ResponseEntity.ok().body(retCalendar);
        }
    }

    @PutMapping
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public ResponseEntity<Calendar> update(@RequestHeader HttpHeaders headers, @RequestBody Calendar calendar)
            throws Exception {
        log.trace("update; calendar={}", calendar);
        validate(calendar);
        synchronized(this){
            Calendar retCalendar = calendarDal.update(
                    calendar,
                    new AuditLogInfo(Integer.parseInt(Objects.requireNonNull(headers.get(
                            "userId")).get(0)), Objects.requireNonNull(headers.get(
                            "clientIp")).get(0)));
            Lookup.fillOrganizationDefaultCalendars(organizationCalendarDal
                    .getOrganizationDefaultWorkCalendar(), organizationCalendarDal
                    .getOrganizationDefaultHolidayCalendars());
            calendarDal.deleteUnmappedScheduleRules();
            return ResponseEntity.ok().body(calendar);
        }
    }


    @PostMapping("/assign")
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name = "organizationId", description = "organizationId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Calendar> assign(@RequestHeader HttpHeaders headers, HttpServletRequest servletRequest,@RequestBody Calendar calendar)
            throws Exception {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("assign; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        log.debug("assign; calendar={}", calendar);
        validate(calendar);
        String organizationId = uriInfo.get("organizationId").toString();
        String organizationId1 = organizationId.replaceAll("[^0-9]", "");
        synchronized(this){
            Calendar retCalendar = calendarDal.assign(
                    calendar,
                    new AuditLogInfo(Integer.parseInt(Objects.requireNonNull(headers.get(
                            "userId")).get(0)), Objects.requireNonNull(headers.get(
                            "clientIp")).get(0)),organizationId1);
            Lookup.fillOrganizationDefaultCalendars(organizationCalendarDal
                    .getOrganizationDefaultWorkCalendar(), organizationCalendarDal
                    .getOrganizationDefaultHolidayCalendars());
            calendarDal.deleteUnmappedScheduleRules();
            return
                    ResponseEntity.ok().body(retCalendar);
        }
    }


    @PostMapping("/unassign")
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name = "organizationId", description = "organizationId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Calendar> unassign(@RequestHeader HttpHeaders headers, HttpServletRequest servletRequest, @RequestBody Calendar calendar)
            throws Exception {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("unassign; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        log.info("unassign; calendar={}", calendar);
        validate(calendar);
        String organizationId = uriInfo.getFirst("organizationId");
        synchronized(this){
            Calendar retCalendar = calendarDal.unassign(
                    calendar,
                    new AuditLogInfo(Integer.parseInt(Objects.requireNonNull(headers.get(
                            "userId")).get(0)), Objects.requireNonNull(headers.get(
                            "clientIp")).get(0)),organizationId);
            Lookup.fillOrganizationDefaultCalendars(organizationCalendarDal
                    .getOrganizationDefaultWorkCalendar(), organizationCalendarDal
                    .getOrganizationDefaultHolidayCalendars());
            calendarDal.deleteUnmappedScheduleRules();
            return ResponseEntity.ok().body(retCalendar);
        }
    }


    @GetMapping
    @Parameters({
            @Parameter(name = "name", description = "calendarName", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Calendar[]> list(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("list; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0,
                Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(
                uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Calendar> list = new ArrayList<Calendar>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = calendarDal.searchList(options);
        } else {
            list = calendarDal.list(options);
        }
        Calendar[] array = new Calendar[list.size()];
        list.toArray(array);

        return ResponseEntity.ok()
                .header("Expires",new Date().toString())
                .body(array);
    }


    private void validate(Calendar calendar) {
        if(calendar.getRules() != null) {
            for (ScheduleRule scheduleRule : calendar.getRules()) {

                Set<ConstraintViolation<ScheduleRule>> violationsScheduleRule = validator.validate(scheduleRule);
                if (violationsScheduleRule.size() > 0) {
                    log.warn("validate; violations={}", violationsScheduleRule);
                    ConstraintViolation<ScheduleRule> v = violationsScheduleRule.iterator().next();
                    ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
                    e.getParameters().put("value", v.getMessage()+" in "+v.getPropertyPath());
                    throw e;
                }
            }
        }

        Set<ConstraintViolation<Calendar>> violations = validator.validate(calendar);
        if (violations.size() > 0) {
            log.warn("validate; violations={}", violations);

            ConstraintViolation<Calendar> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError",v.getMessage());
            e.getParameters().put("value", v.getMessage()+" in "+v.getPropertyPath());
            throw e;
        }
    }


    @GetMapping("/getDataByAssignedStatus")
    @Parameters({
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "calendarTypeId", description = "calendarTypeId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Calendar[]> getDataByAssignedStatus(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getDataByAssignedStatus; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));


        OptionPage optionPage = new OptionPage(uriInfo, 0,
                Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(
                uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Calendar> list = new ArrayList<Calendar>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = calendarDal.getDataByAssignedStatus(options, true);
        } else {
            list = calendarDal.getDataByAssignedStatus(options, false);
        }

        Calendar[] array = new Calendar[list.size()];
        for (int i = 0; i < array.length; i++) {
            Object firstElement = list.get(i);
            array[i] = (Calendar) firstElement;
        }
        //list.toArray(array);
        return ResponseEntity
                .ok()
                .header("Expires",new Date().toString())
                .body(array);
    }

    @GetMapping("/count")
    @Parameters({
            @Parameter(name = "name", description = "calendarName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public long count(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("count; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return calendarDal.searchCount(options);
        } else {
            return calendarDal.count(options);
        }
    }

    @GetMapping("/getCountByAssignedStatus")
    @Parameters({
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public long getCountByAssignedStatus(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getCountByAssignedStatus; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return calendarDal.getCountByAssignedStatus(options, true);
        } else {
            return calendarDal.getCountByAssignedStatus(options, false);
        }
    }

    @GetMapping("/{calendarId}")
    public ResponseEntity<Calendar> getById(@PathVariable("calendarId") long calendarId) {
        log.trace("getById; calendarId={};", calendarId);
        return ResponseEntity.ok().body(calendarDal.getById(calendarId));
    }

    @GetMapping("/getCalendarWithIsDefault")
    public ResponseEntity<Calendar> getCalendarWithIsDefault(@RequestParam(value="calendarId") long calendarId,@RequestParam(value="organizationId") long organizationId) {
        log.trace("getCalendarWithIsDefault; calendarId={},organizationId={};", calendarId,organizationId);
        return ResponseEntity.ok().body(calendarDal.getCalendarWithIsDefault(calendarId,organizationId));
    }

    @DeleteMapping("/{calendarId}")
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public void deleteById(@PathVariable("calendarId") Long calendarId, @RequestHeader HttpHeaders headers) {
        log.trace("deleteById; calendarId={};", calendarId);
        synchronized(this){
            calendarDal.deleteById(calendarId, new AuditLogInfo(Integer.parseInt(Objects.requireNonNull(headers.get(
                    "userId")).get(0)), Objects.requireNonNull(headers.get(
                    "clientIp")).get(0)));
            Lookup.fillOrganizationDefaultCalendars(organizationCalendarDal
                    .getOrganizationDefaultWorkCalendar(), organizationCalendarDal
                    .getOrganizationDefaultHolidayCalendars());
            calendarDal.deleteUnmappedScheduleRules();
            userDal.deleteUserOrgCalendarMapping(calendarId);
        }
    }

    @GetMapping("/defaults")
    @Parameters({
            @Parameter(name = "name", description = "scheduleRuleName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<ScheduleRuleDefault[]> listDefaultScheduleRules(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("listDefaultScheduleRules; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0,
                Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(
                uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<ScheduleRuleDefault> list = new ArrayList<ScheduleRuleDefault>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = calendarDal.listDefaultScheduleRules(options);
        } else {
            list = calendarDal.listDefaultScheduleRules(options);
        }
        ScheduleRuleDefault[] array = new ScheduleRuleDefault[list.size()];
        list.toArray(array);

        return ResponseEntity.ok()
                .header("Expires",new Date().toString())
                .body(array);
    }

    @GetMapping("/defaultWorkCalendarByOrganization")
    @Parameters({
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Calendar> getDefaultWorkCalendarByOrganization(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getDataByAssignedStatus; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
           try{
            Long organizationId = Long
                    .valueOf(uriInfo
                            .getFirst("organizationId"));
            Long result =  Lookup.getDefaultWorkCalendarIdByOrganization(organizationId);
            if(result==null){
                return null;
            }
            return ResponseEntity.ok().body(calendarDal.getById(result));
        }
        catch(NumberFormatException e){
            return null;
        }
    }

    @GetMapping("/defaultHolidayCalendarsByOrganization")
    @Parameters({
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<List<Calendar>> getDefaultHolidayCalendarsByOrganization(
           HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getDataByAssignedStatus; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
         try {
            Long organizationId = Long.valueOf(uriInfo
                    .getFirst("organizationId"));
            List<Long> result = Lookup
                    .getDefaultHolidayCalendarsIdByOrganization(organizationId);
            if (result == null || result.isEmpty()) {
                return null;
            }
            List<Calendar> resultList = new LinkedList<Calendar>();
            for (Long calId : result) {
                resultList.add(calendarDal.getById(calId));
            }
            return ResponseEntity.ok().body(resultList);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    public static boolean isWithinPeriod(java.util.Calendar timestamp, String fromDate, String toDate) {
        // fromDate and toDate are in the same time zone as timestamp
        dateFormat.get().setTimeZone(timestamp.getTimeZone());

        if (fromDate != null) {
            try {
                Date date = dateFormat.get().parse(fromDate);
                if (timestamp.getTimeInMillis() < date.getTime()) {
                    return false;
                }
            } catch (Exception e) {
            }
        }

        if (toDate != null) {
            try {
                Date date = dateFormat.get().parse(toDate);
                java.util.Calendar endOfDay = java.util.Calendar.getInstance(timestamp.getTimeZone());
                endOfDay.setTimeInMillis(date.getTime());
                endOfDay.add(java.util.Calendar.DAY_OF_MONTH, 1);
                if (timestamp.getTimeInMillis() > endOfDay.getTimeInMillis()) {
                    return false;
                }
            } catch (Exception e) {
            }
        }

        return true;
    }

    private static final int calendarDayOfWeekIndex[] = {-1, 0, 1, 2, 3, 4, 5, 6};

    public static boolean isApplicableForToday(java.util.Calendar timestamp, String dayOfWeekArray) {
        if (timestamp == null || dayOfWeekArray == null) {
            return false;
        }
        // dayOfWeek is 7 character string, first character is Sunday, second Monday, etc.
        int dayOfWeekIndex = calendarDayOfWeekIndex[timestamp.get(java.util.Calendar.DAY_OF_WEEK)];
        if (dayOfWeekIndex < 0 || dayOfWeekIndex >= dayOfWeekArray.length()) {
            return false;
        }
        char dayOfWeekCode = dayOfWeekArray.charAt(dayOfWeekIndex);
        if (dayOfWeekCode == '-' || dayOfWeekCode == ' ') {
            return false;
        }
        return true;
    }

    @GetMapping("/organizationCalendars")
    @Parameters({
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "currentDate", description = "currentDate", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<List<Map<String, Object>>> getOrganizationCalendars(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getDataByAssignedStatus; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        String currDate = null;
        if (uriInfo != null && uriInfo.containsKey("currentDate")
                && uriInfo.getFirst("currentDate") != null
                && !uriInfo.getFirst("currentDate").isEmpty()) {
            currDate = uriInfo.getFirst("currentDate");
        }
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Map<String, Object>> resultList = new LinkedList<Map<String, Object>>();
        List<Organization> allOrgs = organizationMaintenanceDal.getList(options);
        if (allOrgs != null && !allOrgs.isEmpty()) {
            // use tempMap to avoid re-evaluation of a calendar but timezone should be handled.
            // Map<Long, ScheduleRule> tempMap = new LinkedHashMap<Long,ScheduleRule>();
            for (Organization org : allOrgs) {
                ScheduleRule rule = null;
                // ObjectRoleRest is not available here, so added similar checks
                Calendar orgCalendar = null;
                Long defaultCalendar = Lookup.getDefaultWorkCalendarIdByOrganization(org.getOrganizationId());
                if (defaultCalendar != null) {
                    orgCalendar = calendarDal.getById(defaultCalendar);
                }
                // org work calendar has to be there, otherwise it's a holiday
                if (orgCalendar != null) {
                    TimeZone scheduleTimeZone = TimeZone.getTimeZone(orgCalendar.getTimeZone());
                    java.util.Calendar timestamp = null;
                    if(currDate==null){
                        timestamp = java.util.Calendar.getInstance(scheduleTimeZone);
                    }
                    else{
                        try{
                            dateFormat.get().setTimeZone(scheduleTimeZone);
                            Date date1 = dateFormat.get().parse(currDate);
                            timestamp = java.util.Calendar.getInstance(scheduleTimeZone);
                            if(deploymentUtil.isIncludeTZOForOrgCal()){
                                timestamp.setTimeInMillis(date1.getTime()+scheduleTimeZone.getOffset(date1.getTime()));
                            }
                            else{
                                timestamp.setTimeInMillis(date1.getTime());
                            }
                        }
                        catch(Exception e){
                            log.warn("getOrganizationCalendars; exception occurred while parsing currentDate param, using today's date; {}", e);
                            timestamp = java.util.Calendar.getInstance(scheduleTimeZone);
                        }
                    }
                    List<Long> holidayCals = Lookup.getDefaultHolidayCalendarsIdByOrganization(org.getOrganizationId());
                    boolean isMatched = false;
                    // first check whether today is a holiday
                    if (holidayCals != null && !holidayCals.isEmpty()) {
                        for (Long calId : holidayCals) {
                            Calendar holCalendar = calendarDal.getById(calId);
                            if (holCalendar != null && holCalendar.getRules() != null
                                    && !holCalendar.getRules().isEmpty()) {
                                for (ScheduleRule rbacScheduleRule : holCalendar.getRules()) {
                                    if (isWithinPeriod(timestamp, rbacScheduleRule.getFromDate(),
                                            rbacScheduleRule.getToDate())
                                            && isApplicableForToday(timestamp, rbacScheduleRule.getDayOfWeek())) {
                                        // tempMap.put(calId, null);
                                        isMatched = true;
                                        rule = rbacScheduleRule;
                                        break;
                                    }
                                }
                                // if a holiday is found, don't check other rules.
                                if (isMatched) {
                                    break;
                                }
                            }
                        }
                    }
                    // if today is a holiday, dont check further. Else check in org work calendar
                    if (!isMatched) {
                        if (orgCalendar != null && orgCalendar.getRules() != null
                                && !orgCalendar.getRules().isEmpty()) {
                            for (ScheduleRule rbacScheduleRule : orgCalendar.getRules()) {
                                if (isWithinPeriod(timestamp, rbacScheduleRule.getFromDate(),
                                        rbacScheduleRule.getToDate())
                                        && isApplicableForToday(timestamp, rbacScheduleRule.getDayOfWeek())) {
                                    // tempMap.put(orgCalendar.getCalendarId(), rbacScheduleRule);
                                    rule = rbacScheduleRule;
                                    break;
                                }
                            }
                        }
                    }
                }
                Map<String, Object> tempResultMap = new LinkedHashMap<String, Object>();
                tempResultMap.put("organizationId", org.getOrganizationId());
                tempResultMap.put("organizationName", org.getOrganizationName());
                tempResultMap.put("isAvailable", (rule==null || !rule.getIsOpen())?Boolean.FALSE:Boolean.TRUE);
                tempResultMap.put("tenantId", org.getTenantId());
                tempResultMap.put("tenantName", Lookup.getTenantNameById(org.getTenantId()));
                if(rule!=null){
                    tempResultMap.put("rule", rule);
                }
                resultList.add(tempResultMap);
            }
        }
        return ResponseEntity.ok().body(resultList);
    }

    @PostMapping("/getHolidaysByOrganization")
    public ResponseEntity<List<OrgHolidaysCalendarResponseVO>> getHolidaysByOrganization(@RequestBody OrgHolidaysCalendarRequestVO orgHolidaysCalendarRequestVO)
            throws Exception {
        List<OrgHolidaysCalendarResponseVO> holidays = null;
        try {
            holidays = calendarDal.getHolidaysByOrganization(orgHolidaysCalendarRequestVO.getStartDate(),
                    orgHolidaysCalendarRequestVO.getEndDate(), orgHolidaysCalendarRequestVO.getOrgId());
        } catch (Exception ex) {
            ErrorInfoException e = new ErrorInfoException(ErrorInfo.XSS_ERROR_CODE, ex.getMessage());
            e.getParameters().put("value", ex.getMessage());
            throw e;
        }
        return ResponseEntity.ok().header("Expires",new Date().toString())
                .body(holidays);
    }

}
