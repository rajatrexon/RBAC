package com.esq.rbac.service.tenant.rest;

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.tenant.service.TenantDal;
import com.esq.rbac.service.util.*;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.validation.annotation.ValidationRules;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/tenants")
public class TenantRest {

    private final TenantDal tenantDal;
    private Validator validator;
    private TenantStructureGenerator tenantStructureGenerator;
    private CacheManagerUtil cacheManagerUtil;

    @Autowired
    public TenantRest(TenantDal tenantDal) {
        log.trace("setTenantDal; tenantDal={};", tenantDal);
        this.tenantDal = tenantDal;
    }

    @EventListener
    public void fillTenantsLookupTable(ApplicationStartedEvent event) {
        log.trace("fillTenantsLookupTable");
        Lookup.fillDetailTenants(tenantDal.list(null));
        Lookup.fillTenants(tenantDal.getTenantIdNames(null), tenantDal.getHostTenant());
        Lookup.fillDetailTenants(tenantDal.list(null));
    }

    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }

    @Autowired
    public void setTenantStructureGenerator(TenantStructureGenerator tenantStructureGenerator) {
        this.tenantStructureGenerator = tenantStructureGenerator;
    }

    @Autowired
    public void setCacheManagerUtil(CacheManagerUtil cacheManagerUtil) {
        this.cacheManagerUtil = cacheManagerUtil;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<Tenant> create(@RequestHeader org.springframework.http.HttpHeaders headers, @RequestBody Tenant tenant) throws Exception {
        log.trace("create; tenant={}", tenant);
        validate(tenant);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        try {
            AuditLogInfo auditLogInfo = new AuditLogInfo(userId, headers.get("clientIp").get(0), tenant.getTenantName(), "Tenant", "Create");
            Tenant tnt = tenantDal.create(tenant, auditLogInfo);
            //Lookup.fillLookupTables(tenantDal.getList(null));
            //tenantDal.refreshTenantMaintenanceCache(DateTime.now());
            Lookup.updateTenantLookupTable(tnt);
            tenantStructureGenerator.handleTenantCreation(tenant, auditLogInfo);

            //Lookup.fillTenants(tenantDal.getTenantIdNames(null), tenantDal.getHostTenant());
            //Lookup.fillorganizations(organizationDal.getOrganizationIdNamesDetails(null));
            if (tenant.getIdentifiers() != null && !tenant.getIdentifiers().isEmpty()) {
                tenantDal.evictSecondLevelCacheById(tnt.getTenantId());
            }
            return ResponseEntity.ok(tnt);
        } catch (ErrorInfoException e) {
            throw e;
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<Tenant> update(@RequestHeader org.springframework.http.HttpHeaders headers, @RequestBody Tenant tenant) throws Exception {
        log.trace("update; tenant={}", tenant);
        validate(tenant);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        try {
            Tenant tnt;
            synchronized (this) {
                tnt = tenantDal.update(tenant, new AuditLogInfo(userId, headers.get("clientIp").get(0), tenant.getTenantName(), "Tenant", "Update"));
                //Lookup.fillLookupTables(tenantDal.getList(null));
                //auditLogger.logCreate(userId, application.getName(), "Application", "Update", applicationDal.getObjectChangeSet());
                //tenantDal.refreshTenantMaintenanceCache(DateTime.now());
            }
            //Lookup.fillTenants(tenantDal.getTenantIdNames(null), tenantDal.getHostTenant());
            Lookup.updateTenantLookupTable(tnt);
            //Lookup.fillorganizations(organizationDal.getOrganizationIdNamesDetails(null));
            return ResponseEntity.ok(tnt);

        } catch (ErrorInfoException e) {
            throw e;
        }
    }

    @GetMapping(value = "/{tenantId}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Tenant> getById(@PathVariable("tenantId") long tenantId) {
        log.trace("getById; tenantId={}", tenantId);
        return ResponseEntity.ok(tenantDal.getById(tenantId));
    }


    @GetMapping(value = "/hostTenant", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tenant> getHostTenant() {
        return ResponseEntity.ok(Lookup.getHostTenant());
    }


    @DeleteMapping("/{tenantId}")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public void deleteById(@RequestHeader org.springframework.http.HttpHeaders headers, @PathVariable("tenantId") long tenantId) throws IOException {
        log.trace("deleteById; tenantId={}", tenantId);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        try {
            String tenantName = tenantDal.getById(tenantId).getTenantName();
            tenantDal.deleteById(tenantId, new AuditLogInfo(userId, headers.get("clientIp").get(0), tenantName, "Tenant", "Delete"));
            //Lookup.fillLookupTables(tenantDal.getTenantIdNames(null), tenantDal.getHostTenant());
            //tenantDal.refreshTenantMaintenanceCache(DateTime.now());
            //	Lookup.fillTenants(tenantDal.getTenantIdNames(null), tenantDal.getHostTenant());
            Lookup.deleteFromTenantLookupTable(tenantId);
        } catch (ErrorInfoException e) {
            throw e;
        }
    }

    @DeleteMapping(value = "/deleteForImport", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<String> deleteForImport(@RequestHeader org.springframework.http.HttpHeaders headers, @QueryParam("tenantName") String tenantName) throws IOException {
        log.trace("deleteForImport; tenantName={}", tenantName);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        try {
            return ResponseEntity.ok(cacheManagerUtil.deleteAndrefreshCacheForTenant(tenantName, new AuditLogInfo(userId, headers.get("clientIp").get(0), "tenantName", "Tenant", "Delete")));
        } catch (ErrorInfoException e) {
            throw e;
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.TENANT_SCOPE_QUERY, description = RBACUtil.TENANT_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantType", description = "tenantType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Tenant[]> list(HttpServletRequest request) {
        log.trace("list; requestUri={}", request.getRequestURI());

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Tenant> list = new LinkedList<Tenant>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = tenantDal.searchList(options);
        } else {
            list = tenantDal.list(options);
        }
        Tenant[] array = new Tenant[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }

    @GetMapping(value = "/searchTenantIdNames", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.TENANT_SCOPE_QUERY, description = RBACUtil.TENANT_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantType", description = "tenantType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Object[]> searchTenantIdNames(HttpServletRequest request) {
        log.trace("searchTenantIdNames; requestUri={}", request.getRequestURI());

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Map<String, Object>> list = tenantDal.searchTenantIdNames(options);
        Object[] array = new Object[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }

    @GetMapping(value = "/searchTenantIdNamesCount", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.TENANT_SCOPE_QUERY, description = RBACUtil.TENANT_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantType", description = "tenantType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public long searchTenantIdNamesCount(HttpServletRequest request) {
        log.trace("searchTenantIdNamesCount; requestUri={}", request.getRequestURI());

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);
        return tenantDal.searchTenantIdNamesCount(options);
    }

    @GetMapping(value = "/searchTenantIdNamesWithCount", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.TENANT_SCOPE_QUERY, description = RBACUtil.TENANT_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantType", description = "tenantType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Map<String, Object>> searchTenantIdNamesWithCount(HttpServletRequest request) throws Exception {
        log.trace("searchTenantIdNamesWithCount; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        Map<String, Object> returnObject = new HashMap<String, Object>();
        returnObject.put("totalCount", tenantDal.searchTenantIdNamesCount(options));
        returnObject.put("values", tenantDal.searchTenantIdNames(options));
        return ResponseEntity.ok(returnObject);
    }

    @GetMapping(value = "/tenantIds", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.TENANT_SCOPE_QUERY, description = RBACUtil.TENANT_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantType", description = "tenantType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Long[]> getTenantIds(HttpServletRequest request) {
        log.trace("getTenantIds; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Long> list = new LinkedList<Long>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = tenantDal.getTenantIds(options);
        } else {
            list = tenantDal.getTenantIds(options);
        }
        Long[] array = new Long[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }

    @GetMapping(value = "/defaultSelectedTenantList/{userTenantId}", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.TENANT_SCOPE_QUERY, description = RBACUtil.TENANT_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantType", description = "tenantType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Long[]> getDefaultSelectedTenantList(HttpServletRequest request, @PathVariable("userTenantId") Long userTenantId) {
	    	/*
			 *  tenantList empty      					:return userTenant
				tenantList size=1   					:return first in tenantList
				tenantList contains userTenant  		:return userTenant
				tenantList doesn't contain userTenant  	:return first in tenantList
			 * */
        log.trace("getDefaultSelectedTenantList; userTenantId ={}; requestUri={};", userTenantId, request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        HashSet<Long> tenantList = new LinkedHashSet<Long>();
        tenantList.addAll(tenantDal.getTenantIds(options));

        List<Long> resultList = new LinkedList<Long>();
        if (tenantList == null || tenantList.isEmpty()) {
            resultList.add(userTenantId);
        } else if (tenantList.size() == 1) {
            resultList.add(tenantList.iterator().next());
        } else {
            boolean found = false;
            if (tenantList.contains(userTenantId)) {
                resultList.add(userTenantId);
                found = true;
            }
            if (!found) {
                resultList.add(tenantList.iterator().next());
            }
        }
        Long[] array = new Long[resultList.size()];
        resultList.toArray(array);
        return ResponseEntity.ok(array);
    }

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.TENANT_SCOPE_QUERY, description = RBACUtil.TENANT_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantType", description = "tenantType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public long count(HttpServletRequest request) {
        log.trace("count; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);

        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return tenantDal.getSearchCount(options);
        } else {
            return tenantDal.count(options);
        }

    }

    @GetMapping(value = "/validationRules", produces = MediaType.APPLICATION_JSON)
    public ValidationRules getValidationRules() {
        ValidationRules validationRules = new ValidationRules();
        validationRules.getFieldRulesList().addAll(ValidationUtil.retrieveValidationRules(Tenant.class));
        return validationRules;
    }

    @GetMapping(value = "/tenantIdNames", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.TENANT_SCOPE_QUERY, description = RBACUtil.TENANT_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantType", description = "tenantType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<StreamingResponseBody> getTenantIdNames(HttpServletRequest request) {
        log.trace("getTenantIdNames; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        final List<Map<String, Object>> list = tenantDal.getTenantIdNames(options);
        StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.beginArray();
                for (Map<String, Object> message : list) {
                    gson.toJson(message, Map.class, writer);
                }
                writer.endArray();
                writer.close();
            }
        };
        return ResponseEntity.ok(stream);
    }

    //Todo parameter of this API ( long[] selectedTenantList )
    @PostMapping(value = "/validateTenantIds", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> validateTenantIds(@Context HttpServletRequest httpRequest, long[] selectedTenantList) {
        if (selectedTenantList != null) {
            for (long tenantId : selectedTenantList) {
                if (Lookup.getTenantNameById(tenantId) == null) {
                    throw new ErrorInfoException("invalidTenantIds", "invalidTenantIds");
                }
            }
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping(value = "/checkEntityPermission", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, description = "tenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.TENANT_SCOPE_QUERY, description = RBACUtil.TENANT_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantType", description = "tenantType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public Boolean checkEntityPermission(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionFilter);
        return tenantDal.checkEntityPermission(Long.parseLong(optionFilter.getFilter(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER)), options);
    }

    private void validate(Tenant tenant) {
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);
        if (violations.size() > 0) {
            log.warn("validate; violations={}", violations);

            ConstraintViolation<Tenant> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }
}
