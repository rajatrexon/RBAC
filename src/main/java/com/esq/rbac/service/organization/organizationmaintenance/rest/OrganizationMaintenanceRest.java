package com.esq.rbac.service.organization.organizationmaintenance.rest;
import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.embedded.OrganizationGrid;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.organization.vo.OrganizationHierarchy;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.validation.annotation.ValidationRules;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.ConstraintViolation;
import jakarta.ws.rs.core.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping(value = "/organizations")
//@ManagedResource(objectName="org.springbyexample.jmx:name=ServerManager",description="Server manager.")
public class OrganizationMaintenanceRest {

    private Validator validator;

    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }
    private OrganizationMaintenanceDal orgMaintenanceDal;

//    @Autowired
//    public void setDependencies(OrganizationMaintenanceDal orgMaintenanceDal) {
//        log.trace("setDependencies; orgMaintenanceDal={}", orgMaintenanceDal);
//        this.orgMaintenanceDal = orgMaintenanceDal;
//        CommonLookup.fillorganizations(orgMaintenanceDal.getOrganizationIdNamesDetails(null));
//    }

    @Autowired
    public OrganizationMaintenanceRest(OrganizationMaintenanceDal orgMaintenanceDal) {
        log.trace("setDependencies; orgMaintenanceDal={}", orgMaintenanceDal);
        this.orgMaintenanceDal = orgMaintenanceDal;
    }

    @EventListener
    public void fillOrganisationsLookupTable(ApplicationStartedEvent event){
        log.trace("fillOrganisationLookupTable");
        Lookup.fillorganizations(orgMaintenanceDal.getOrganizationIdNamesDetails(null));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Organization[]> list(HttpServletRequest request) {
        log.trace("list; requestUri={}", request.getRequestURI());

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Organization> list = new LinkedList<Organization>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = orgMaintenanceDal.getList(options);
        } else {
            list = orgMaintenanceDal.getList(options);
        }
        Organization[] array = new Organization[list.size()];
        list.toArray(array);

        return ResponseEntity.ok().header("Expires",new Date().toString()).body(array);
    }

    @GetMapping(value="/organizationList", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<String[]> organizationList(HttpServletRequest request) {
        log.trace("list; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Organization> list = new LinkedList<Organization>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = orgMaintenanceDal.getList(options);
        } else {
            list = orgMaintenanceDal.getList(options);
        }
        Organization[] array = new Organization[list.size()];

        ArrayList<String> organizationList = new ArrayList<String>();
        list.toArray(array);
        JsonArray organizationJsonArray = new JsonArray();
        for(Organization org : list) {
            String id = org.getOrganizationId().toString();
            String value = org.getOrganizationName();
            JsonObject json = new JsonObject();
            try {
                json.addProperty("id", id);
                json.addProperty("value", value);
                //json.append("id", id);
                //json.append("value", value);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            organizationJsonArray.add(json);
        }

        for(int i=0;i<organizationJsonArray.size();i++) {
            try {
                String test = organizationJsonArray.get(i).toString();
                organizationList.add( organizationJsonArray.get(i).toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String[] organizationArray = new String[organizationList.size()];
        organizationList.toArray(organizationArray);
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(organizationArray);
    }

    @GetMapping(value="/shared", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "host", description = "host", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Organization[]> getSharedOrganizations(HttpServletRequest request) {
        log.trace("getSharedOrganizations; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Organization> list = new LinkedList<Organization>();
        String tenantIdFilter = null;
        if(optionFilter.getFilter("tenantId")!=null && !optionFilter.getFilter("tenantId").isEmpty()){
            tenantIdFilter = optionFilter.getFilter("tenantId");
            if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
                list = orgMaintenanceDal.getList(options);
            } else {
                list = orgMaintenanceDal.getList(options);
            }
        }

        String hostIncluded = optionFilter.getFilter("host");
        if(hostIncluded!=null && !hostIncluded.isEmpty() && hostIncluded.equalsIgnoreCase("true")){
            Tenant hostTenant = Lookup.getHostTenant();
            String hostTenantId = hostTenant!=null?hostTenant.getTenantId().toString():"100";
            if(tenantIdFilter==null || !(tenantIdFilter.equalsIgnoreCase(hostTenantId)) ){
                MultivaluedMap<String, String> queryMap = new MultivaluedHashMap<>();
                queryMap.putSingle("tenantId", hostTenantId);
                queryMap.putSingle("isShared", "true");
                optionFilter = new OptionFilter(queryMap);
                options = new Options(optionPage, optionSort, optionFilter);
                list.addAll(orgMaintenanceDal.getList(options));
            }
        }

        Organization[] array = new Organization[list.size()];
        list.toArray(array);

        return ResponseEntity.ok().header("Expires",new Date().toString()).body(array);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public Organization create(@RequestHeader org.springframework.http.HttpHeaders headers,@RequestBody Organization orgMaintain) throws Exception {
        log.trace("create; Organization={}", orgMaintain);
        validate(orgMaintain);
        Integer userId = Integer.parseInt(headers.get("userId")
                .get(0));
        synchronized (this) {
            Organization retapp = orgMaintenanceDal.create(
                    orgMaintain, userId, "Organization", "Create");
            //	String applicationName = "";Lookup.getChildApplicationName(appMaintain.getChildApplicationId());
//			auditLogger.logCreate(userId, applicationName,
//					"Organization Maintenance", "Create",
//					appMaintenanceDal.getObjectChangeSet());
            //appMaintenanceDal.refreshAppMaintenanceCache(DateTime.now());
            // Lookup.fillorganizations(orgMaintenanceDal.getOrganizationIdNamesDetails(null));
            Lookup.updateOrganizationLookupTable(retapp);
            return retapp;
        }
    }

	/*@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ResponseEntity<> list(HttpServletRequest request) {
		log.trace("list; ApplicationMaintenance--requestUri={}",
				request.getRequestURI());

			OptionPage optionPage = new OptionPage(
					uriInfo.getQueryParameters(), 0, 10);
			OptionSort optionSort = new OptionSort(uriInfo.getQueryParameters());
			OptionFilter optionFilter = new OptionFilter(
					uriInfo.getQueryParameters());
			Options options = new Options(optionPage, optionSort, optionFilter);

			List<ApplicationMaintenance> list = new ArrayList<ApplicationMaintenance>();

			 * if
			 * (uriInfo.getQueryParameters().containsKey(SearchUtils.SEARCH_PARAM
			 * )) { list = appMaintenanceDal.searchList(options); } else {

			list = appMaintenanceDal.getList(options);
			// }
			ApplicationMaintenance[] array = new ApplicationMaintenance[list
					.size()];
			list.toArray(array);

			return ResponseEntity.ok().header("Expires",new Date().toString()).body(array);;
	}*/

    private void validate(Organization orgMaintain) {
        Set<ConstraintViolation<Organization>> violations = validator.validate(orgMaintain);
        if (violations.size() > 0) {
            log.warn("validate; violations={}", violations);

            ConstraintViolation<Organization> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }

    }

    @GetMapping(value="/count", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public int count(HttpServletRequest request) {
        log.trace("count; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);
        /*
         * if
         * (uriInfo.getQueryParameters().containsKey(SearchUtils.SEARCH_PARAM
         * )) { return scopeDal.getSearchCount(options); } else {
         */
        return orgMaintenanceDal.getCount(options);
        // }
    }

	/*@DELETE
	@Path("/{maintenanceId}")
	public void deleteById(HttpServletRequest request,
			@RequestHeader org.springframework.http.HttpHeaders headers,
			@PathParam("maintenanceId") int maintenanceId) throws IOException {
		Integer userId = Integer.parseInt(headers.get("userId")
				.get(0));
		Boolean force = Boolean.parseBoolean(uriInfo.getQueryParameters()
				.getFirst("force"));
		String applicationName = Lookup.getChildApplicationName(appMaintenanceDal
				.getById(maintenanceId).getChildApplicationId());
		log.trace("deleteById; scopeId={}", maintenanceId);
		log.trace("deleteById; userId={}", userId);
		log.trace("deleteById; uriInfo={}", request.getRequestURI());
		log.trace("deleteById; force={}", force);
		synchronized (this) {
			appMaintenanceDal.deleteById(maintenanceId);
			auditLogger.logCreate(userId, applicationName,
					"Schedule Maintenance", "Delete",
					appMaintenanceDal.getObjectChangeSet());
			appMaintenanceDal.refreshAppMaintenanceCache(DateTime.now());
		}
	}*/

    @GetMapping(value="/{organizationId}", produces = MediaType.APPLICATION_JSON)
    public Organization getById(@PathVariable("organizationId") long organizationId) {
        log.trace("getById; organizationId={}", organizationId);
        return orgMaintenanceDal.getById(organizationId);
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public Organization update(@RequestHeader org.springframework.http.HttpHeaders headers,@RequestBody Organization orgMaintain) throws Exception {
        log.info("update; Organization={}", orgMaintain);
        validate(orgMaintain);
        Organization retAppMaintain;
        synchronized (this) {
            Integer userId = Integer.parseInt(headers
                    .get("userId").get(0));
            retAppMaintain = orgMaintenanceDal.update(orgMaintain, userId, "Organization", "Update");
            //Lookup.fillorganizations(orgMaintenanceDal.getOrganizationIdNamesDetails(null));
            Lookup.updateOrganizationLookupTable(retAppMaintain);

            // Lookup.fillScopeLookupTable(scopeDal.getList(null));
		/*	String applicationName = Lookup.getChildApplicationName(appMaintain
					.getChildApplicationId());
			auditLogger.logCreate(userId, applicationName,
					"Schedule Maintenance", "Update",
					orgMaintenanceDal.getObjectChangeSet());*/
            //orgMaintenanceDal.refreshAppMaintenanceCache(DateTime.now());
        }
        return retAppMaintain;
    }

    @DeleteMapping(value="/{organizationId}")
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public void deleteById(@RequestHeader org.springframework.http.HttpHeaders headers, HttpServletRequest request,
                           @PathVariable("organizationId") int organizationId) {
        log.trace("deleteById; organizationId={}", organizationId);
        log.trace("deleteById; uriInfo={}", request.getRequestURI());
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
        queryParams.add("organizationId", Long.valueOf(organizationId).toString());
        Options options = new Options(new OptionFilter(queryParams));
        List<Organization> orgList = orgMaintenanceDal.getList(options);
        Lookup.deleteFromOrganizationtLookupTable(orgList);
        orgMaintenanceDal.deleteById(organizationId, userId);
    }

    @GetMapping(value="/validationRules", produces = MediaType.APPLICATION_JSON)
    public ValidationRules getValidationRules() {
        ValidationRules validationRules = new ValidationRules();
        validationRules.getFieldRulesList().addAll(
                ValidationUtil.retrieveValidationRules(Organization.class));
        return validationRules;
    }

    @GetMapping(value="/hierarchy", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Map<Long, Set<OrganizationHierarchy>>> getOrganizationHierarchy(HttpServletRequest request) {
        log.trace("getOrganizationHierarchy; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        Map<Long, Set<OrganizationHierarchy>> list = new TreeMap<Long, Set<OrganizationHierarchy>>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = orgMaintenanceDal.getOrganizationHierarchy(options);
        } else {
            list = orgMaintenanceDal.getOrganizationHierarchy(options);
        }
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(list);
    }

    @GetMapping(value="/info", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<List<Map<String, Object>>> getOrganizationInfo(HttpServletRequest request) {
        log.trace("getOrganizationInfo; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = orgMaintenanceDal.getOrganizationInfo(options);
        } else {
            list = orgMaintenanceDal.getOrganizationInfo(options);
        }
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(list);
    }

    @GetMapping(value="/customOrganizationInfo", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<List<Map<String, Object>>> getCustomOrganizationInfo(HttpServletRequest request) {
        log.trace("getCustomOrganizationInfo; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = orgMaintenanceDal.searchCustomOrganizationInfo(options);
        } else {
            list = orgMaintenanceDal.getCustomOrganizationInfo(options);
        }
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(list);
    }

    @GetMapping(value="/organizationIdNames", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<StreamingResponseBody>  getOrganizationIdNames(HttpServletRequest request) {
        log.trace("getOrganizationIdNames; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        final List<Map<String, Object>> list = orgMaintenanceDal.getOrganizationIdNames(options);
        StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.beginArray();
                for (Map<String, Object> message : list) {
                    gson.toJson(message, Map.class, writer);
                }
                writer.endArray();
                writer.close();
            }
        };
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(stream);
    }

    @GetMapping(value="/organizationIdNamesWithScope", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<StreamingResponseBody> getOrganizationIdNamesWithScope(HttpServletRequest request) {
        log.trace("getOrganizationIdNamesWithScope; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        final List<Map<String, Object>> list = orgMaintenanceDal.getOrganizationIdNamesWithScope(options);
        StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.beginArray();
                for (Map<String, Object> message : list) {
                    gson.toJson(message, Map.class, writer);
                }
                writer.endArray();
                writer.close();
            }
        };
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(stream);
    }

    @GetMapping(value="/organizationByTenantId", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<StreamingResponseBody> getOrganizationByTenantId(HttpServletRequest request) {
        log.trace("getOrganizationByTenantId; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        final List<Map<String, Object>> list = orgMaintenanceDal.getOrganizationByTenantId(options);
        StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.beginArray();
                for (Map<String, Object> message : list) {
                    gson.toJson(message, Map.class, writer);
                }
                writer.endArray();
                writer.close();
            }
        };
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(stream);
    }



    /******* RBAC-1656 Start ******/
    @GetMapping(value="/hierarchyGridView", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "type", description = "type", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "itemId", description = "itemId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "showAll", description = "showAll", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "sortOrder", description = "sortOrder", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "startNo", description = "startNo", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<List<OrganizationGrid>> getOrganizationHierarchyGridView(HttpServletRequest request) {
        log.trace("getOrganizationHierarchyGridView; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<OrganizationGrid> list = new ArrayList<OrganizationGrid>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = orgMaintenanceDal.getOrganizationHierarchyGridView(options);
        } else {
            list = orgMaintenanceDal.getOrganizationHierarchyGridView(options);
        }
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(list);
    }

    @GetMapping(value="/hierarchyGridSearchData", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "tenantId", description = "tenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getSearchBoxData(HttpServletRequest request) {
        log.trace("getOrganizationHierarchyGridViewSearchBoxData; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        Map<String, List<Map<String, Object>>> list = new TreeMap<String, List<Map<String, Object>>>();
        list = orgMaintenanceDal.getSearchBoxData(options);
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(list);
    }

    @GetMapping(value="/nodesCount", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "tenantId", description = "tenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Map<String,Integer>> getNodeCount(HttpServletRequest request) {
        log.trace("getNodesCount; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(orgMaintenanceDal.getNodesCount(options));
    }

    @GetMapping(value="/tenantOrgView", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "tenantId", description = "tenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<OrganizationGrid> getTenantOrganizationView(HttpServletRequest request) {
        log.trace("getOrganizationHierarchyGridView; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        OrganizationGrid organizationGrid = new OrganizationGrid();
        organizationGrid = orgMaintenanceDal.getTenantOrganizationGrid(options);
        return 	ResponseEntity.ok().header("Expires",new Date().toString()).body(organizationGrid);
    }

    @GetMapping(value="/gridSearchData", produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "searchId", description = "organizationId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantId", description = "tenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "filterOrganizationId", description = "filterOrganizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationId", description = "organizationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationName", description = "organizationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.ORGANIZATION_SCOPE_QUERY, description = RBACUtil.ORGANIZATION_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationTypeCode", description = "organizationTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "organizationSubTypeCode", description = "organizationSubTypeCode", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "isShared", description = "isShared", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<Map<String, OrganizationGrid>> getSearchedData(HttpServletRequest request) {
        log.trace("getOrganizationHierarchySearchData; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        Map<String, OrganizationGrid> list = new TreeMap<String, OrganizationGrid>();
        list = orgMaintenanceDal.getSearchData(options);
        return ResponseEntity.ok().header("Expires",new Date().toString()).body(list);
    }

    @GetMapping(value="/getBatchSizeForData", produces = MediaType.APPLICATION_JSON)
    public Integer getBatchSizeForData(HttpServletRequest request) {
        return orgMaintenanceDal.getBatchSizeForData();
    }
    /******* RBAC-1656 Ends ******/
}

