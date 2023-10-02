package com.esq.rbac.service.group.rest;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.group.json.UsersInGroupJson;
import com.esq.rbac.service.group.service.GroupDal;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import com.esq.rbac.service.scope.service.ScopeDal;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.tenant.service.TenantDal;
import com.esq.rbac.service.util.AuditLogger;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.validation.annotation.ValidationRules;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;


@RestController
@RequestMapping("/groups")
@Slf4j
public class GroupRest {

    private GroupDal groupDal;
    private TenantDal tenantDal;
    private ScopeDal scopeDal;
    private AuditLogger auditLogger;
    private Validator validator;

    public GroupRest(AuditLogService auditLogService, Validator validator, ScopeDal scopeDal, TenantDal tenantDal, GroupDal groupDal) {
        this.auditLogger = new AuditLogger(auditLogService);
        this.validator = validator;
        this.scopeDal = scopeDal;
        this.tenantDal = tenantDal;
        this.groupDal = groupDal;
    }

    public GroupRest() {
        log.trace("GroupRest");
    }

    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }

    @Autowired
    public void setGroupDal(GroupDal groupDal, AuditLogService auditLogDal, TenantDal tenantDal, ScopeDal scopeDal) {
        log.trace("setGroupDal");
        this.groupDal = groupDal;
        this.tenantDal = tenantDal;
        this.scopeDal = scopeDal;
        this.auditLogger = new AuditLogger(auditLogDal);

    }

    @EventListener
    public void fillGroupLookupTable(ApplicationStartedEvent event) {
        log.trace("fillGroupLookupTable");
        Lookup.fillGroupLookupTable(groupDal.getGroupIdNames(null));
    }


    @PostMapping
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<Group> create(@RequestHeader HttpHeaders headers, @RequestBody Group group) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("create; group:{}", group);
        }
        validate(group);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        Group savedGroup = groupDal.create(group, userId);
        //Lookup.fillGroupLookupTable(groupDal.getGroupIdNames(null));
        Lookup.updateGroupLookupTable(savedGroup);
        //auditLogger.logCreate(Integer.parseInt(headers.getRequestHeader("userId").get(0)), group.getName(), "Group", "Create");
        // auditLogger.logCreate(Integer.parseInt(headers.getRequestHeader("userId").get(0)), group.getName(), "Group", "Create", groupDal.getObjectChangeSet());
        return ResponseEntity.ok().body(savedGroup);
    }


    @PutMapping
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<Group> update(@RequestHeader HttpHeaders headers, @RequestBody Group group) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("update; group={}", group);
        }
        validate(group);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        Group retGroup = groupDal.update(group, userId);
        // Lookup.fillGroupLookupTable(groupDal.getGroupIdNames(null));
        Lookup.updateGroupLookupTable(retGroup);
        //auditLogger.logCreate(userId, group.getName(), "Group", "Update", groupDal.getObjectChangeSet());

        //handle for constraint enabled scope
        //log.info("update; scopes={}",retGroup.getScopeDefinitions());
        //scopeConstraintDal.executeExternaUpdate(retGroup);
        return ResponseEntity.ok().body(retGroup);
    }

    @PutMapping("/roles")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<Group> updateRoles(@RequestHeader HttpHeaders headers, @RequestBody Group group, @QueryParam("isUndefinedScopesAllowed") boolean isUndefinedScopesAllowed) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("updateRoles; group={}", group);
        }
        validate(group);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));

        Group retGroup = groupDal.updateRoles(group, isUndefinedScopesAllowed, userId);
        //auditLogger.logCreate(userId, group.getName(), "Group", "Update", groupDal.getObjectChangeSet());
        //handle for constraint enabled scope
        if (log.isTraceEnabled()) {
            log.trace("updateRoles; scopes={}", retGroup.getScopeDefinitions());
        }
        //scopeConstraintDal.executeExternaUpdate(retGroup);
        return ResponseEntity.ok().body(retGroup);
    }


    @PutMapping("/cloneGroup")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<Group> cloneGroup(@QueryParam("fromGroupId") int fromGroupId, @QueryParam("toGroupId") int toGroupId, @RequestHeader HttpHeaders headers, @QueryParam("isUndefinedScopesAllowed") boolean isUndefinedScopesAllowed) throws Exception {
        log.trace("cloneGroup; fromGroupId={}", fromGroupId);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));

        Group retGroup = groupDal.cloneGroup(fromGroupId, toGroupId, isUndefinedScopesAllowed, userId);
        //auditLogger.logCreate(userId, Lookup.getGroupName(toGroupId), "Group", "Update", groupDal.getObjectChangeSet());
        //handle for constraint enabled scope
        log.info("cloneGroup; scopes={}", retGroup.getScopeDefinitions());
        //scopeConstraintDal.executeExternaUpdate(retGroup);
        return ResponseEntity.ok().body(retGroup);
    }


    @PutMapping("/cloneScopeDefinitionForGroup/{fromGroupId}")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public void cloneScopeDefinitionForGroup(@RequestHeader HttpHeaders headers, @PathVariable("fromGroupId") int fromGroupId, Map<Integer, List<Integer>> fromScopeToGroupIds, @QueryParam("isUndefinedScopesAllowed") boolean isUndefinedScopesAllowed) throws Exception {
        log.trace("cloneScopeDefinitionForGroup; fromGroupId={}", fromGroupId);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        groupDal.cloneScopeDefinitionFromGroup(fromGroupId, fromScopeToGroupIds, isUndefinedScopesAllowed, userId);

    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getById(@PathVariable("groupId") int groupId) {
        log.trace("getById; groupId={}", groupId);
        return ResponseEntity.ok().body(groupDal.getById(groupId));
    }


    @DeleteMapping("/{groupId}")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public void deleteById(@RequestHeader HttpHeaders headers, @PathVariable("groupId") int groupId) throws IOException {
        log.trace("deleteById; groupId={}", groupId);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        try {
            String groupName = groupDal.getById(groupId).getName();
            groupDal.deleteById(groupId);
            auditLogger.logCreate(userId, groupName, "Group", "Delete");
            Lookup.deleteFromGroupLookupTable(groupId);
        } catch (ErrorInfoException e) {
            throw e;
        }
    }


    @GetMapping("/{groupId}/scopeIds")
    public ResponseEntity<List> getScopeIds(@PathVariable("groupId") int groupId) {
        log.trace("getScopeIds; groupId={}", groupId);
        List<Integer> result = groupDal.getScopeIds(groupId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{groupId}/getScopeIdsWithDefaultScopes")
    @Parameters({@Parameter(name = "loggedInUser", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<List> getScopeIdsWithDefaultScopes(@RequestHeader HttpHeaders headers, @PathVariable("groupId") int groupId) {
        log.trace("getScopeIdsWithDefaultScopes; groupId={}", groupId);
        String loggedInUser = headers.get("loggedInUser").get(0);
        List<Scope> result = groupDal.getScopes(groupId, true, loggedInUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Parameters({@Parameter(name = "name", description = "groupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "appRole", description = "applicationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.GROUP_SCOPE_QUERY, description = RBACUtil.GROUP_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Group[]> list(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("list; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo, "");
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Group> list = new ArrayList<>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = groupDal.searchList(options);
        } else {
            list = groupDal.getList(options);
        }
        try {
            Scope tenatScope = scopeDal.getByScopeKey(RBACUtil.SCOPE_KEY_TENANT);
            for (int i = 0; i < list.size(); i++) {
                Set<ScopeDefinition> updatedScopes = new HashSet<>();
                Set<ScopeDefinition> scopes = list.get(i).getScopeDefinitions();
                Iterator<ScopeDefinition> itr = scopes.iterator();
                while (itr.hasNext()) {
                    ScopeDefinition scopeDef = itr.next();
                    if (scopeDef.getScopeId() == tenatScope.getScopeId() && scopeDef.getScopeAdditionalData() != null) {
                        JsonParser jsonParser = new JsonParser();
                        JsonElement jsonScope = jsonParser.parse(scopeDef.getScopeAdditionalData());
                        JsonObject jsonObj = jsonScope.getAsJsonObject();
                        JsonObject rulesJson = jsonObj.get("rules").getAsJsonArray().get(0).getAsJsonObject();
                        JsonElement pluginElement = rulesJson.get("pluginValue");
                        if (pluginElement != null) log.info("get pluginVal of element {}", pluginElement);
                        else {
                            log.info("No Plugin Value Found for {}. Creating Plugin Value for JSON", scopeDef.getScopeDefinition());
                            JsonArray valueArray = rulesJson.get("value").getAsJsonArray();
                            JsonArray newArray = new JsonArray();
                            for (int j = 0; j < valueArray.size(); j++) {
                                //{"text":"Host","id":"100"},{"text":"Switch","id":"300"},{"text":"TENANT2","id":"400"}
                                Long tenantId = valueArray.get(j).getAsLong();
                                Tenant tenantDet = tenantDal.getById(tenantId);
                                JsonObject newJsonObj = new JsonObject();
                                newJsonObj.addProperty("text", tenantDet.getTenantName());
                                newJsonObj.addProperty("id", tenantDet.getTenantId() + "");
                                newArray.add(newJsonObj);
                            }
                            rulesJson.add("pluginValue", newArray);
                            JsonArray newRuleArr = new JsonArray();
                            newRuleArr.add(rulesJson);
                            jsonObj.add("rules", newRuleArr);
                            scopeDef.setScopeAdditionalData(jsonObj + "");
                        }
                    }
                    updatedScopes.add(scopeDef);
                }
                list.get(i).setScopeDefinitions(updatedScopes);
            }
        } catch (Exception e) {
            log.info("Multi Tenant Not Enable");
        }
        Group[] array = new Group[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }


    @GetMapping("/count")
    @Parameters({@Parameter(name = "name", description = "groupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "appRole", description = "applicationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.GROUP_SCOPE_QUERY, description = RBACUtil.GROUP_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public int count(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("count; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo, "");
        Options options = new Options(optionSort, optionFilter);

        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return groupDal.getSearchCount(options);
        } else {
            return groupDal.getCount(options);
        }
    }


    @GetMapping("/allGroupRoleScopes")
    @Parameters({@Parameter(name = "name", description = "groupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "appRole", description = "applicationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.GROUP_SCOPE_QUERY, description = RBACUtil.GROUP_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Map<String, List<String>>> getAllGroupRoleScopes(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getAllGroupRoleScopes; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        List<String> sortProperties = new LinkedList<String>();
        sortProperties.add("name");
        sortProperties.add("roleName");
        OptionSort optionSort = new OptionSort(sortProperties);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        return ResponseEntity.ok().body(groupDal.getAllGroupRoleScopes(options));
    }

    //    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/getAllGroupWithIdenticalScopeDefinition")
    @GetMapping("/getAllGroupWithIdenticalScopeDefinition")
    @Parameters({@Parameter(name = "groupId", description = "groupId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "scopeId", description = "scopeId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isSelfUpdateAllowed", description = "isSelfUpdateAllowed value like: true|false. If false then userId is required in userId field", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userId", description = "userId is required if isSelfUpdateAllowed is false", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "name", description = "groupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "appRole", description = "applicationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.GROUP_SCOPE_QUERY, description = RBACUtil.GROUP_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<List<Map<String, String>>> getAllGroupWithIdenticalScopeDefinition(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getAllGroupWithIdenticalScopeDefinition; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        List<String> sortProperties = new LinkedList<String>();
        OptionSort optionSort = new OptionSort(sortProperties);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        return ResponseEntity.ok().body(groupDal.getAllGroupWithIdenticalScopeDefinition(options));
    }


    @GetMapping(value = "/getAllGroupWithUndefinedScopes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "groupId", description = "groupId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "scopeId", description = "scopeId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isSelfUpdateAllowed", description = "isSelfUpdateAllowed value like: true|false", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userId", description = "userId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "name", description = "groupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "appRole", description = "applicationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.GROUP_SCOPE_QUERY, description = RBACUtil.GROUP_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<List<Map<String, String>>> getAllGroupWithUndefinedScopes(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getAllGroupWithUndefinedScopes; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        List<String> sortProperties = new LinkedList<String>();
        OptionSort optionSort = new OptionSort(sortProperties);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        return ResponseEntity.ok().body(groupDal.getAllGroupWithUndefinedScopes(options));
    }


    @GetMapping("/validationRules")
    public ResponseEntity<ValidationRules> getValidationRules() {
        ValidationRules validationRules = new ValidationRules();
        validationRules.getFieldRulesList().addAll(ValidationUtil.retrieveValidationRules(Group.class));
        return ResponseEntity.ok().body(validationRules);
    }

    private void validate(Group group) {
        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        if (violations.size() > 0) {
            log.warn("validate; violations={}", violations);

            ConstraintViolation<Group> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }

    @GetMapping("/groupIdNames")
    @Parameters({@Parameter(name = "name", description = "groupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "appRole", description = "applicationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.GROUP_SCOPE_QUERY, description = RBACUtil.GROUP_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<StreamingResponseBody> getGroupIdNames(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getGroupIdNames; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        final List<Map<String, Object>> list = groupDal.getGroupIdNames(options);
//        StreamingOutput stream = new StreamingOutput() {
//            @Override
//            public void write(OutputStream os) throws IOException, WebApplicationException
//            {
//                Gson gson = new Gson();
//                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
//                writer.beginArray();
//                for (Map<String, Object> message : list) {
//                    gson.toJson(message, Map.class, writer);
//                }
//                writer.endArray();
//                writer.close();
//            }
//        };

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


    @GetMapping("/groupIdNamesWithScope")
    @Parameters({@Parameter(name = "name", description = "groupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "appRole", description = "applicationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.GROUP_SCOPE_QUERY, description = RBACUtil.GROUP_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<StreamingResponseBody> getGroupIdNamesWithScope(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getGroupIdNamesWithScope; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        final List<Map<String, Object>> list = groupDal.getGroupIdNamesWithScope(options);
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


    @GetMapping(value = "/groupName/{groupId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getGroupName(@PathVariable("groupId") int groupId) {
        return ResponseEntity.ok(Lookup.getGroupName(groupId));
    }

    @PostMapping("/usersInGroupsData")
    public ResponseEntity<StreamingResponseBody> getUsersInGroupsData(@RequestBody Map<String, String> scopeMap) {
        log.trace("getUsersInGroupsData; scopeMap={}", scopeMap);
        final List<UsersInGroupJson> result = groupDal.getUsersInGroupsData(scopeMap);
        StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.beginArray();
                for (UsersInGroupJson message : result) {
                    gson.toJson(message, UsersInGroupJson.class, writer);
                }
                writer.endArray();
                writer.close();
            }
        };
        return ResponseEntity.ok(stream);
    }

    @PutMapping("/updateUsersInGroups")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<List<Map<String, Object>>> updateUsersInGroups(@RequestBody List<Map<String, Object>> userGroupList) {
        log.trace("updateUsersInGroups; userGroupList={}", userGroupList);
        Integer userId = Integer.parseInt(String.valueOf(100));
        return ResponseEntity.ok().body(groupDal.updateUsersInGroups(userGroupList, userId));
    }


    @PostMapping("/rolesInGroupsData")
    public ResponseEntity<String> getRolesInGroupsData(@RequestBody Map<String, String> scopeMap) {
        log.trace("getRolesInGroupsData; scopeMap={}", scopeMap);
        return ResponseEntity.ok().body(new Gson().toJson(groupDal.getRolesInGroupsData(scopeMap)));
    }

    //    @GET
//    @Path("/groupRoleScopeNames/{groupId}")
//    @Produces(MediaType.APPLICATION_JSON)
    @GetMapping("/groupRoleScopeNames/{groupId}")
    public ResponseEntity<String> getGroupRoleScopeNames(@PathVariable("groupId") int groupId) {
        log.trace("getGroupRoleScopeNames; groupId={}", groupId);
        return ResponseEntity.ok().body(new Gson().toJson(groupDal.getGroupRoleScopeNames(groupId)));
    }


    @GetMapping("/checkEntityPermission")
    @Parameters({@Parameter(name = RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, description = "groupId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "name", description = "groupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "appRole", description = "applicationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.GROUP_SCOPE_QUERY, description = RBACUtil.GROUP_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "label", description = "label", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Boolean> checkEntityPermission(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getGroupIdNamesWithScope; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionFilter);
        return ResponseEntity.ok().body(groupDal.checkEntityPermission(Integer.parseInt(optionFilter.getFilter(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER)), options));
    }


    @GetMapping("users")
    public ResponseEntity<Set<UsersInGroupJson.UserJson>> getUsersInGroup(@QueryParam("userName") String userName, @QueryParam("groupId") Integer groupId, @QueryParam("userId") Integer userId) throws Exception {
        log.debug("getUsersInGroup; userName={}; groupId={}; userId={}", userName, groupId, userId);

        Set<UsersInGroupJson.UserJson> userJsonSet = groupDal.getUserListByGroupId(userName, groupId, userId);
        return ResponseEntity.ok().body(userJsonSet);
    }
}
