package com.esq.rbac.service.scope.rest;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.scope.service.ScopeDal;
import com.esq.rbac.service.util.AuditLogger;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.validation.annotation.ValidationRules;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/scopes")
@Tag(name="/scopes")
@Slf4j
public class ScopeRest {

    private final ScopeDal scopeDal;
    private final AuditLogger auditLogger;
    private Validator validator;

    public ScopeRest(ScopeDal scopeDal, AuditLogService auditLogDal, Validator validator){
        log.trace("setScopeDal");
        this.scopeDal = scopeDal;
        this.auditLogger = new AuditLogger(auditLogDal);
        this.validator =validator;
    }

    @EventListener
    public void fillScopeLookupTable(ApplicationStartedEvent event){
        log.trace("fillScopeLookupTable");
        Lookup.fillScopeLookupTable(scopeDal.getList(null));
    }


   @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public ResponseEntity<Scope> create(@RequestHeader HttpHeaders headers, @RequestBody Scope scope) throws Exception {
        log.trace("create; scope={}", scope);
        validate(scope);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        Scope retScope = scopeDal.create(scope, userId);
        Lookup.fillScopeLookupTable(scopeDal.getList(null));
        auditLogger.logCreate(userId, scope.getName(), "Scope", "Create");
        return ResponseEntity.ok(retScope);
    }


    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public ResponseEntity<Scope> update(@RequestHeader org.springframework.http.HttpHeaders headers, @RequestBody Scope scope) throws Exception {
        log.trace("update; scope={}", scope);
        validate(scope);
        Scope retScope;
        synchronized (this) {
            Integer userId = Integer.parseInt(headers.get("userId").get(0));
            retScope = scopeDal.update(scope, userId);
            Lookup.fillScopeLookupTable(scopeDal.getList(null));
            auditLogger.logCreate(userId, scope.getName(), "Scope", "Update", scopeDal.getObjectChangeSet());
        }
        return ResponseEntity.ok(retScope);
    }



    @GetMapping(value = "/{scopeId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Scope> getById(@PathVariable("scopeId") int scopeId) {
        log.trace("getById; scopeId={}", scopeId);
        return ResponseEntity.ok(scopeDal.getById(scopeId));
    }


    @DeleteMapping("/{scopeId}")
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name = "force", description = "force", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public void deleteById(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers, @PathVariable("scopeId") int scopeId) throws IOException {
        Integer userId = Integer.parseInt(headers.get("userId").get(0));

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));

        Boolean force = Boolean.parseBoolean(Arrays.toString(parameterMap.get("force")));
        String scopeName = scopeDal.getById(scopeId).getName();
        log.trace("deleteById; scopeId={}", scopeId);
        log.trace("deleteById; userId={}", userId);
        log.trace("deleteById; uriInfo={}", request.getRequestURI());
        log.trace("deleteById; force={}", force);
        scopeDal.deleteById(scopeId, force);
        auditLogger.logCreate(userId, scopeName, "Scope", "Delete");
    }


    @GetMapping(MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "applicationId", description = "applicationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "name", description = "scopeName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "mandatory", description = "mandatory value like: true|false", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "label", description = "label", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Scope[]> list(HttpServletRequest request) {
        log.trace("list; requestUri={}", request.getRequestURI());

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Scope> list = new ArrayList<Scope>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = scopeDal.searchList(options);
        } else {
            list = scopeDal.getList(options);
        }
        Scope[] array = new Scope[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }


    @GetMapping(value = "/global",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Scope[]> globalList(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("GlobalList; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        List<Scope> list = scopeDal.getListGlobal();
        Scope[] array = new Scope[list.size()];
        list.toArray(array);
        return ResponseEntity.ok(array);
    }


    @GetMapping(value = "/count",produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "applicationId", description = "applicationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "name", description = "scopeName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "mandatory", description = "mandatory value like: true|false", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "label", description = "label", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public Integer count(HttpServletRequest request) {
        log.trace("count; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return scopeDal.getSearchCount(options);
        } else {
            return scopeDal.getCount(options);
        }
    }

    @GetMapping(value = "/validationRules",produces = MediaType.APPLICATION_JSON_VALUE)
    public ValidationRules getValidationRules() {
        ValidationRules validationRules = new ValidationRules();
        validationRules.getFieldRulesList().addAll(ValidationUtil.retrieveValidationRules(Scope.class));
        return validationRules;
    }

    private void validate(Scope scope) {
        Set<ConstraintViolation<Scope>> violations = validator.validate(scope);
        if (violations.size() > 0) {
            log.warn("validate; violations={}", violations);
            ConstraintViolation<Scope> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage()+" in "+v.getPropertyPath());
            throw e;
        }
    }
}
