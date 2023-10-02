package com.esq.rbac.service.application.applicationmaintenance.rest;

import com.esq.rbac.service.application.applicationmaintenance.domain.ApplicationMaintenance;
import com.esq.rbac.service.application.applicationmaintenance.service.ApplicationMaintenanceDal;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.util.AuditLogger;
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
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/schedule")
//Todo Same Resource_Path as ScheduleRest
public class ApplicationMaintenanceRest {

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private ApplicationMaintenanceDal appMaintenanceDal;
    private AuditLogger auditLogger;
    private Validator validator = factory.getValidator();

    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }

    @Autowired
    public void dependencies(ApplicationMaintenanceDal appMaintenanceDal, AuditLogService auditLogDal) {
        log.trace("setDependencies; appMaintenanceDal={}", appMaintenanceDal);
        this.appMaintenanceDal = appMaintenanceDal;
        this.auditLogger = new AuditLogger(auditLogDal);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<ApplicationMaintenance> create(@RequestHeader HttpHeaders headers, @RequestBody ApplicationMaintenance appMaintain) throws Exception {
        log.trace("create; ApplicationMaintenance={}", appMaintain);
        validate(appMaintain);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        synchronized (this) {
            ApplicationMaintenance retapp = appMaintenanceDal.create(appMaintain, userId);
            String applicationName = Lookup.getChildApplicationName(appMaintain.getChildApplicationId());
            auditLogger.logCreate(userId, applicationName, "Schedule Maintenance", "Create", appMaintenanceDal.getObjectChangeSet());
            appMaintenanceDal.refreshAppMaintenanceCache(DateTime.now());
            return ResponseEntity.ok(retapp);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "maintenanceId", description = "maintenanceId", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "childApplicationId", description = "childApplicationId", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "fromDate", description = "fromDate", in = ParameterIn.QUERY, schema = @Schema(type = "date")), @Parameter(name = "toDate", description = "toDate", in = ParameterIn.QUERY, schema = @Schema(type = "date")), @Parameter(name = "message", description = "message", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "isEnabled", description = "isEnabled", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "isExpired", description = "isExpired", in = ParameterIn.QUERY, schema = @Schema(type = "string")),})
    public ResponseEntity<ApplicationMaintenance[]> list(HttpServletRequest httpServletRequest) {
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        log.trace("list; ApplicationMaintenance--requestUri={}", httpServletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<ApplicationMaintenance> list = new ArrayList<ApplicationMaintenance>();

        // if(uriInfo.getQueryParameters().containsKey(SearchUtils.SEARCH_PARAM)) { list = appMaintenanceDal.searchList(options); } else {

        list = appMaintenanceDal.getList(options);
        // }
        ApplicationMaintenance[] array = new ApplicationMaintenance[list.size()];
        list.toArray(array);

        return ResponseEntity.ok().header("Expires", new Date().toString()).body(array);
    }

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "maintenanceId", description = "maintenanceId", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "childApplicationId", description = "childApplicationId", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "fromDate", description = "fromDate", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "toDate", description = "toDate", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "message", description = "message", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "isEnabled", description = "isEnabled", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "isExpired", description = "isExpired", in = ParameterIn.QUERY, schema = @Schema(type = "string")),})
    public int count(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("count; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));


        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);

        // if(uriInfo.getQueryParameters().containsKey(SearchUtils.SEARCH_PARAM)) { return scopeDal.getSearchCount(options); } else {

        return appMaintenanceDal.getCount(options);
        // }
    }

    @DeleteMapping(value = "/{maintenanceId}")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
//        @ApiImplicitParam(name="force", value="force", paramType="query", dataType="string"),
    })
    public void deleteById(HttpServletRequest servletRequest, @RequestHeader HttpHeaders headers, @PathVariable("maintenanceId") int maintenanceId) throws IOException {
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        Boolean force = Boolean.parseBoolean(uriInfo.getFirst("force"));
        String applicationName = Lookup.getChildApplicationName(appMaintenanceDal.getById(maintenanceId).getChildApplicationId());
        log.trace("deleteById; scopeId={}", maintenanceId);
        log.trace("deleteById; userId={}", userId);
        log.trace("deleteById; uriInfo={}", servletRequest.getRequestURI());
        log.trace("deleteById; force={}", force);
        synchronized (this) {
            appMaintenanceDal.deleteById(maintenanceId);
            auditLogger.logCreate(userId, applicationName, "Schedule Maintenance", "Delete", appMaintenanceDal.getObjectChangeSet());
            appMaintenanceDal.refreshAppMaintenanceCache(DateTime.now());
        }
    }

    @GetMapping(value = "/{maintenanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationMaintenance> getById(@PathVariable("maintenanceId") int maintenanceId) {
        log.trace("getById; maintenanceId={}", maintenanceId);
        return ResponseEntity.ok(appMaintenanceDal.getById(maintenanceId));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<ApplicationMaintenance> update(@RequestHeader HttpHeaders headers, @RequestBody ApplicationMaintenance appMaintain) throws Exception {

        log.info("update; ApplicationMaintenance={}", appMaintain);
        validate(appMaintain);
        ApplicationMaintenance retAppMaintain;
        synchronized (this) {
            Integer userId = Integer.parseInt(headers.get("userId").get(0));
            retAppMaintain = appMaintenanceDal.update(appMaintain, userId);
            String applicationName = Lookup.getChildApplicationName(appMaintain.getChildApplicationId());
            auditLogger.logCreate(userId, applicationName, "Schedule Maintenance", "Update", appMaintenanceDal.getObjectChangeSet());
            appMaintenanceDal.refreshAppMaintenanceCache(DateTime.now());
        }
        return ResponseEntity.ok(retAppMaintain);
    }

    @GetMapping(value = "/validationRules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ValidationRules getValidationRules() {
        ValidationRules validationRules = new ValidationRules();
        validationRules.getFieldRulesList().addAll(ValidationUtil.retrieveValidationRules(ApplicationMaintenanceRest.class));
        return validationRules;
    }


    private void validate(ApplicationMaintenance appMaintain) {
        Set<ConstraintViolation<ApplicationMaintenance>> violations = validator.validate(appMaintain);
        if (violations.size() > 0) {
            log.warn("validate; violations={}", violations);
            ConstraintViolation<ApplicationMaintenance> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }
}
