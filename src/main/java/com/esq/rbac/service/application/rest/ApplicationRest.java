package com.esq.rbac.service.application.rest;

import com.esq.rbac.service.application.applicationmaintenance.service.ApplicationMaintenanceDal;
import com.esq.rbac.service.application.childapplication.appurldata.AppUrlData;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.service.ApplicationDal;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.util.AuditLogger;
import com.esq.rbac.service.util.ChildAppPermValidatorUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.validation.annotation.ValidationRules;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import java.util.*;

//TODO LOOKUP
@Slf4j
@RestController
@RequestMapping("/applications")
public class ApplicationRest {

    private ApplicationDal applicationDal;

    private AuditLogger auditLogger;


    private Validator validator;


    private ApplicationMaintenanceDal applicationMaintenanceDal;


    private ChildAppPermValidatorUtil childAppPermValidatorUtil;


    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }

    @Autowired
    public void dependencies(ApplicationDal applicationDal, AuditLogService auditLogDal, @Lazy ChildAppPermValidatorUtil childAppPermValidatorUtil) {
        log.trace("setDependencies; applicationDal={}; auditLogDal={}; childAppPermValidatorUtil={};", applicationDal, auditLogDal, childAppPermValidatorUtil);
        this.applicationDal = applicationDal;

        this.auditLogger = new AuditLogger(auditLogDal);
        this.childAppPermValidatorUtil = childAppPermValidatorUtil;
        this.childAppPermValidatorUtil.initializeValidators(applicationDal.getList(null));
    }


    @EventListener
    public void fillLookupTables(ApplicationStartedEvent event) {
        log.trace("fillApplicationLookupTable");
        Lookup.fillLookupTables(applicationDal.getList(null));
    }

    @Autowired
    public void setApplicationMaintenanceDal(ApplicationMaintenanceDal applicationMaintenanceDal) {
        log.trace("setApplicationMaintenanceDal");
        this.applicationMaintenanceDal = applicationMaintenanceDal;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<Application> create(HttpServletRequest servletRequest, @RequestBody Application application) throws Exception {
        log.trace("create; application={}", application);
        Integer userId = Integer.parseInt(servletRequest.getHeader("userId"));
        applicationDal.validate(application);
        try {
            Application app = applicationDal.create(application, new AuditLogInfo(userId, null));
            Lookup.fillLookupTables(applicationDal.getList(null));
            childAppPermValidatorUtil.initializeValidators(applicationDal.getList(null));
            //auditLogger.logCreate(userId, application.getName(), "Application", "Create");
            applicationMaintenanceDal.refreshAppMaintenanceCache(DateTime.now());
            return ResponseEntity.ok().header("Expires",new Date().toString()).body(app);

        } catch (ErrorInfoException e) {
            throw e;
        }
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<Application> update(@RequestHeader org.springframework.http.HttpHeaders headers,@RequestBody Application application) throws Exception {
        log.trace("update; application={}", application);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        applicationDal.validate(application);
        try {
            Application app;
            synchronized (this) {
                // Assuming the applicationId is part of the Application model
               // application.setApplicationId(applicationId);
                app = applicationDal.update(application, new AuditLogInfo(userId, null));
                Lookup.fillLookupTables(applicationDal.getList(null));
                childAppPermValidatorUtil.initializeValidators(applicationDal.getList(null));
                //auditLogger.logCreate(userId, application.getName(), "Application", "Update", applicationDal.getObjectChangeSet());
                applicationMaintenanceDal.refreshAppMaintenanceCache(DateTime.now());
            }
            return ResponseEntity.ok().header("Expires",new Date().toString()).body(app);
        } catch (ErrorInfoException e) {
            throw e;
        }
    }

    @PutMapping(value = "/targetOperations", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<Application> updateTargetOperations(@RequestHeader org.springframework.http.HttpHeaders headers, @RequestBody Application application) throws Exception {
        log.trace("updatetargetOperations; application={}", application);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        applicationDal.validate(application);

        Application app = applicationDal.updateTargetOperations(application);
        Lookup.fillLookupTables(applicationDal.getList(null));
        childAppPermValidatorUtil.initializeValidators(applicationDal.getList(null));
        auditLogger.logCreate(userId, application.getName(), "Application", "Update", applicationDal.getObjectChangeSet());
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(app);
    }

    @GetMapping(value = "/{applicationId}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Application> getById(@PathVariable int applicationId) {
        log.trace("getById; applicationId={}", applicationId);
        Application applicationData = applicationDal.getById(applicationId);
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(applicationData);
    }

    @DeleteMapping("/{applicationId}")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public void deleteById(@RequestHeader org.springframework.http.HttpHeaders headers, @PathVariable int applicationId) {
        log.trace("deleteById; applicationId={}", applicationId);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        try {
            //String appName = Lookup.getApplicationName(applicationId);
            applicationDal.deleteById(applicationId, new AuditLogInfo(userId, null));
            Lookup.fillLookupTables(applicationDal.getList(null));
            //auditLogger.logCreate(userId, appName, "Application", "Delete");
            applicationMaintenanceDal.refreshAppMaintenanceCache(DateTime.now());
        } catch (ErrorInfoException e) {
            throw e;
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "name", description = "applicationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Application[]> list(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        log.trace("list; requestUri={}", request.getRequestURI());
        System.out.println("inside the get all user api...");

        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Application> list = new ArrayList<Application>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = applicationDal.searchList(options);
        } else {
            list = applicationDal.getList(options);
        }
        Application[] array = new Application[list.size()];
        list.toArray(array);
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(array);
    }

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "name", description = "applicationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public Integer count(HttpServletRequest request) {

        Map<String, String[]> parameterMap = request.getParameterMap();
        log.trace("list; requestUri={}", request.getRequestURI());
        System.out.println("inside the get all user api...");

        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);

        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return applicationDal.getSearchCount(options);
        } else {
            return applicationDal.getCount(options);
        }
    }

    // Todo TBD
    @GetMapping(value = "/validationRules", produces = MediaType.APPLICATION_JSON)
    public ValidationRules getValidationRules() {
        ValidationRules validationRules = new ValidationRules();
        validationRules.getFieldRulesList().addAll(ValidationUtil.retrieveValidationRules(Application.class));
        return validationRules;
    }

    @GetMapping(value = "/applicationIdNames", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "name", description = "applicationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<String> getApplicationIdNames(HttpServletRequest request) {

        Map<String, String[]> parameterMap = request.getParameterMap();
        log.trace("list; requestUri={}", request.getRequestURI());
        System.out.println("inside the get all user api...");

        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Map<String, Object>> list = applicationDal.getApplicationIdNames(options);

        return ResponseEntity.ok().header("Expires",new Date().toString())
                .body(new Gson().toJson(list));
    }

    @GetMapping(value = "/rbacContextName", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<String> getRBACContextName() {
        String rbacContextName = applicationDal.getRBACContextName();
        return ResponseEntity.ok().header("Expires",new Date().toString())
                .body(new Gson().toJson(rbacContextName));
    }

    @GetMapping(value = "/appDashboardChildApplication", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<ChildApplication> getAppDashboardChildApplication() {
        log.trace("getAppDashboardChildApplication");
        return ResponseEntity.status(HttpStatus.OK).body(applicationDal.getAppDashboardChildApplication());
    }

    public void validate(Application application) {
        Set<ConstraintViolation<Application>> violations = validator.validate(application);
        if (!violations.isEmpty()) {
            ConstraintViolation<Application> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }

        if (application.getChildApplications() != null) {
            for (ChildApplication childApp : application.getChildApplications()) {
                for (AppUrlData appUrlData : childApp.getAppUrlDataSet()) {
                    Set<ConstraintViolation<AppUrlData>> violationsAppUrlData = validator.validate(appUrlData);
                    if (!violationsAppUrlData.isEmpty()) {
                        ConstraintViolation<AppUrlData> v = violationsAppUrlData.iterator().next();
                        ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
                        e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
                        throw e;
                    }
                }
            }
        }
    }

    @PostMapping(value = "/rolesInApplicationsData", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<String> getRolesInApplicationsData(Map<String, String> scopeMap) {
        log.trace("getRolesInApplicationsData; scopeMap={}", scopeMap);

        return ResponseEntity.ok().header("Expires",new Date().toString())
                .body(new Gson().toJson(applicationDal.getRolesInApplicationsData(scopeMap)));
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity getStatus() {
        try {
            Date date = applicationDal.getStatus();
            if (date != null) {
                return  ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Error in getStatus {}", e);
            throw new HttpServerErrorException(HttpStatus.NO_CONTENT, "Database connectivity lost");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/getChildApplicationInfo", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<String> getChildApplicationNamesForScheduleMaintenence(HttpServletRequest request) {

        Map<String, String[]> parameterMap = request.getParameterMap();
        log.trace("list; requestUri={}", request.getRequestURI());
        System.out.println("inside the get all user api...");
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
//
//        OptionPage optionPage = new OptionPage(uriInfo.getQueryParameters(), 0, Integer.MAX_VALUE);
//        OptionSort optionSort = new OptionSort(uriInfo.getQueryParameters());
//        OptionFilter optionFilter = new OptionFilter(uriInfo.getQueryParameters());// Pass filtering options if needed
//        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Map<String, Object>> list = applicationDal.getChildApplicationNamesForScheduleMaintenence(options);
        return ResponseEntity.ok().header("Expires",new Date().toString())
                .body(new Gson().toJson(list));
    }

    @GetMapping("/applicationIdNamesByLoggedInUser")
    public ResponseEntity<String> getApplicationIdNamesByLoggedInUser(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        log.trace("list; requestUri={}", request.getRequestURI());
        System.out.println("inside the get all user api...");
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        String loggedInUserName = uriInfo.getFirst("userName");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list = applicationDal.getApplicationIdNamesForLoggedInUser(loggedInUserName);
        return ResponseEntity.ok().header("Expires",new Date().toString())
                .body(new Gson().toJson(list));
    }
}

