/*
 * Copyright (c)2013,2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.service.role.rest;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.role.domain.Role;
import com.esq.rbac.service.role.service.RoleDal;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import com.esq.rbac.service.util.AuditLogger;
import com.esq.rbac.service.util.RBACUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@Tag(name="/roles")
@RequestMapping("/roles")
public class RoleRest {

    private static final Logger log = LoggerFactory.getLogger(RoleRest.class);
    private RoleDal roleDal;
    AuditLogger auditLogger;
    private Validator validator;

    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }

    @Autowired
    public void setRoleDal(RoleDal roleDal, AuditLogService auditLogDal) {
        log.trace("setRoleDal");
        this.roleDal = roleDal;
        this.auditLogger = new AuditLogger(auditLogDal);

    }

    @EventListener
    public void fillRoleLookupTable(ApplicationStartedEvent event) {
        log.trace("fillRoleLookupTable");
        Lookup.fillRoleLookupTable(roleDal.getList(null));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"),in = ParameterIn.HEADER),
    })
    public ResponseEntity<Role> create(@RequestHeader("userId") String userId, @RequestBody Role role) throws Exception {
        log.trace("create; role={}", role);
        validate(role);
        Integer userIdInt = Integer.parseInt(userId);
        Role retRole = roleDal.create(role, userIdInt);
        Lookup.fillRoleLookupTable(roleDal.getList(null));
        auditLogger.logCreate(userIdInt, role.getName(), "Role", "Create");
        return ResponseEntity.ok(retRole);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public ResponseEntity<Role> update(@RequestHeader("userId") String userId, @RequestBody Role role) throws Exception {
        log.trace("update; role={}", role);
        validate(role);
        Integer userIdInt = Integer.parseInt(userId);
        Role retRole;
        synchronized (this) {
            retRole = roleDal.update(role, userIdInt);
            Lookup.fillRoleLookupTable(roleDal.getList(null));
            auditLogger.logCreate(userIdInt, role.getName(), "Role", "Update", roleDal.getObjectChangeSet());
        }
        return ResponseEntity.ok(roleDal.update(retRole, userIdInt));
    }


    @PutMapping(path = "/permissions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public ResponseEntity<Role> updatePermissions(@RequestHeader HttpHeaders headers, @RequestBody Role role) throws Exception {
        log.trace("updatePermissions; role={}", role);
        validate(role);
        Integer userIdInt = Integer.parseInt(headers.get("userId").get(0));

        Role retRole = roleDal.updatePermissions(role);
        auditLogger.logCreate(userIdInt, role.getName(), "Role", "Update", roleDal.getObjectChangeSet());
        return ResponseEntity.ok(roleDal.update(retRole, userIdInt));
    }


    @GetMapping("/{roleId}")
    public ResponseEntity<Role> getById(@PathVariable int roleId) {
        log.trace("getById; roleId={}", roleId);
        return ResponseEntity.ok(roleDal.getById(roleId).get());
    }


    @DeleteMapping("/{roleId}")
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public void deleteById(@RequestHeader("userId") String userId, @PathVariable int roleId) throws IOException {
        log.trace("deleteById; roleId={}", roleId);
        Integer userIdInt = Integer.parseInt(userId);
        try {
            String roleName = roleDal.getById(roleId).get().getName();
            roleDal.deleteById(roleId);
            auditLogger.logCreate(userIdInt, roleName, "Role", "Delete");
        } catch (ErrorInfoException e) {
            throw e;
        }
    }


    @GetMapping("/{roleId}/scopeIds")
    public ResponseEntity<List<Integer>> getScopeIds(@PathVariable int roleId) {
        log.trace("getScopeIds; roleId={}", roleId);
        List<Integer> result = roleDal.getScopeIds(roleId);
        return ResponseEntity.ok().body(result);
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "name", description = "roleName", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "label", description = "label", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "applicationId", description = "applicationId", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = RBACUtil.ROLE_SCOPE_QUERY, description = RBACUtil.ROLE_SCOPE_QUERY, in = ParameterIn.QUERY, schema = @Schema(type = "string")),
    })
    public ResponseEntity<Role[]> list(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("list; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Role> list = new ArrayList<>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = roleDal.searchList(options);
        } else {
            list = roleDal.getList(options);
        }
        Role[] array = new Role[list.size()];
        list.toArray(array);

        return ResponseEntity.ok().body(array);
    }


    @GetMapping(path = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "name", description = "roleName", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "label", description = "label", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "applicationId", description = "applicationId", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = RBACUtil.ROLE_SCOPE_QUERY, description = RBACUtil.ROLE_SCOPE_QUERY, in = ParameterIn.QUERY, schema = @Schema(type = "string")),
    })
    public ResponseEntity<Integer> count(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("count; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);

        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return ResponseEntity.ok(roleDal.getSearchCount(options));
        } else {
            return ResponseEntity.ok(roleDal.getCount(options));
        }
    }


    @GetMapping(path = "/validationRules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValidationRules> getValidationRules() {
        ValidationRules validationRules = new ValidationRules();
        validationRules.getFieldRulesList().addAll(ValidationUtil.retrieveValidationRules(Role.class));
        return ResponseEntity.ok(validationRules);
    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/getRolesNotAssignedToAnyGroup")

    @GetMapping(path = "/getRolesNotAssignedToAnyGroup", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "name", description = "roleName", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "label", description = "label", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "applicationId", description = "applicationId", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = RBACUtil.ROLE_SCOPE_QUERY, description = RBACUtil.ROLE_SCOPE_QUERY, in = ParameterIn.QUERY, schema = @Schema(type = "string")),
    })
    public ResponseEntity<Role[]> getRolesNotAssignedToAnyGroup(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getRolesNotAssignedToAnyGroup; requestUri->{}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Role> list = roleDal.getRolesNotAssignedToAnyGroup(options);
        if (list == null) {
            list = Collections.emptyList();
        }
        log.trace("getRolesNotAssignedToAnyGroup; size={}", list.size());

        Role[] array = new Role[list.size()];
        return ResponseEntity.ok(list.toArray(array));
    }

    //    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/getRolesAssignedToOtherGroups/{groupId}")
    @GetMapping(path = "/getRolesAssignedToOtherGroups/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "name", description = "roleName", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "label", description = "label", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "applicationId", description = "applicationId", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = RBACUtil.ROLE_SCOPE_QUERY, description = RBACUtil.ROLE_SCOPE_QUERY, in = ParameterIn.QUERY, schema = @Schema(type = "string")),
    })
    public ResponseEntity<Role[]> getRolesAssignedToOtherGroups(@PathVariable("groupId") int groupId, HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getRolesAssignedToOtherGroups; groupId={}", groupId);
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Role> list = roleDal.getRolesAssignedToOtherGroups(groupId, options);
        if (list == null) {
            list = Collections.emptyList();
        }
        log.info("getRolesAssignedToOtherGroups; size={}", list.size());

        Role[] array = new Role[list.size()];
        return ResponseEntity.ok(list.toArray(array));
    }

    private void validate(Role role) {
        Set<ConstraintViolation<Role>> violations = validator.validate(role);
        if (violations.size() > 0) {
            log.warn("validate; violations={}", violations);

            ConstraintViolation<Role> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }


    @PostMapping(path = "/getScopeIds", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "loggedInUser", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public ResponseEntity<List<Scope>> getScopeIds(@RequestBody Map<String, List<Integer>> roleIdsList, @RequestHeader org.springframework.http.HttpHeaders headers, HttpServletRequest servletRequest) {
        log.trace("getScopeIds; roleIds={}", roleIdsList);
        List<Scope> result = roleDal.getGroupRoleScopeIds(roleIdsList, false, headers.get("loggedInUser").get(0));
        return ResponseEntity.ok().body(result);
    }


    @PostMapping(path = "/getScopeIdsWithDefaultScopes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "loggedInUser", description = "loggedInUserId", required = true, schema = @Schema(type = "string"),  in = ParameterIn.HEADER),
    })
    public ResponseEntity<List<Scope>> getScopeIdsWithDefaultScopes(@RequestBody Map<String, List<Integer>> roleIdsList,
                                                                    @RequestHeader org.springframework.http.HttpHeaders
                                                                            headers, HttpServletRequest servletRequestt)
    {
        log.trace("getScopeIdsWithDefaultScopes; roleIds={}", roleIdsList);
        List<Scope> result = roleDal.getGroupRoleScopeIds(roleIdsList, true, headers.get("loggedInUser").get(0));
        return ResponseEntity.ok().body(result);
    }


    @PostMapping(path = "/getRoleScopeDefinitions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ScopeDefinition>> getRoleScopeDefinitions(@RequestBody List<Map<String, Integer>> roleGroupIdList) {
        log.trace("getRoleScopeDefinitions; roleGroupIdList={}", roleGroupIdList);
        List<ScopeDefinition> result = roleDal.getRoleScopeDefinitions(roleGroupIdList);
        return ResponseEntity.ok().body(result);
    }

    //    @POST
//    @Path("/getRoleTransition")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
    @PostMapping(path = "/getRoleTransition", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> getRoleTransition(HttpServletRequest servletRequest, @RequestBody List<Map<String, List<Integer>>> roleList) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getRoleTransition; roleList={}", roleList);
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        boolean isTransitionAllowed = roleDal.getRoleTransition(roleList);
        return ResponseEntity.ok(isTransitionAllowed);
    }

    //    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Path("/getCountForDefinedScopeRoles")
    @PostMapping(path = "/getCountForDefinedScopeRoles", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> getCountForDefinedScopeRoles(HttpServletRequest servletRequest, Map<String, Integer> roleId) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getCountForDefinedScopeRoles; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        boolean isScopeDefined = roleDal.isScopeDefinedForRole(roleId);
        return ResponseEntity.ok(isScopeDefined);

    }

}
