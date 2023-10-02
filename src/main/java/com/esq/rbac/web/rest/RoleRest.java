/*
 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.rest;
import com.esq.rbac.web.client.RestClient;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.Role;
import com.esq.rbac.web.vo.Scope;
import com.esq.rbac.web.vo.ScopeDefinition;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(RoleRest.RESOURCE_PATH)
public class RoleRest {

    private static final Logger log = LoggerFactory.getLogger(RoleRest.class);
    public static final String RESOURCE_PATH = "roles";
    private RestClient restClient;
    private UserDetailsService userDetailsService;

    @Autowired
    public void setRestClient(RestClient restClient) {
        log.debug("setRestClient");
        this.restClient = restClient;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        log.debug("setUserDetailsService; {}", userDetailsService);
        this.userDetailsService = userDetailsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Role>> create(@RequestBody Role role) throws Exception {
        log.trace("create; role={}", role);
        userDetailsService.verifyPermission("Role.Create");
        return restClient.resource(RESOURCE_PATH)
               .build().post()
                .accept(MediaType.APPLICATION_JSON)
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(role)
                .retrieve().bodyToMono(Role.class)
                .map(roles -> ResponseEntity.ok(roles));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Role>> update(@RequestBody Role role) throws Exception {
        log.trace("update; role={}", role);
        userDetailsService.verifyPermission("Role.Update");
        return restClient.resource(RESOURCE_PATH)
                .build().put()
                .accept(MediaType.APPLICATION_JSON)
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(role)
                .retrieve().bodyToMono(Role.class)
                .map(roles -> ResponseEntity.ok(roles));
    }

    @PutMapping(value = "/permissions",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Role>> updastePermissions(@RequestBody Role role) throws Exception {
        log.trace("updatePermissions; role={}", role);
        userDetailsService.verifyPermission("Role.Update");
        return restClient.resource(RESOURCE_PATH,"permissions")
                .build().put()
                .accept(MediaType.APPLICATION_JSON)
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(role)
                .retrieve().bodyToMono(Role.class)
                .map(roles -> ResponseEntity.ok(roles));
    }

    @GetMapping(value = "/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Role>> getById(@PathVariable("roleId") int roleId) {
        log.trace("getById; roleId={}", roleId);
        userDetailsService.verifyPermission("Role.View");
        return restClient.resource(RESOURCE_PATH, Integer.toString(roleId))
                .build().get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Role.class).map(role -> ResponseEntity.ok(role));
    }

    @DeleteMapping(value = "/{roleId}")
    public void deleteById(@PathVariable("roleId") int roleId) {
        log.trace("deleteById; roleId={}", roleId);
        userDetailsService.verifyPermission("Role.Delete");
        restClient.resource(RESOURCE_PATH, Integer.toString(roleId))
                .build().delete()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toBodilessEntity().subscribe();
    }

    @GetMapping(value = "/{roleId}/scopeIds", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Integer[]>> getScopeIds(@PathVariable("roleId") int roleId) {
        log.trace("getScopeIds; roleId={}", roleId);
        userDetailsService.verifyPermission("Role.View");
        return restClient.resource(RESOURCE_PATH, Integer.toString(roleId), "scopeIds")
                .build().get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Integer[].class).map(role -> ResponseEntity.ok(role));
    }

    @PostMapping(value = "/customRoleInfo",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Role[]>> list(HttpServletRequest httpRequest) {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("list; requestUri={}", params);
        userDetailsService.verifyPermission("Role.View");
        String scopeQuery = userDetailsService.extractScopeForRoleView();
        log.trace("list; scopeQuery={}", scopeQuery);
        params.add(RBACUtil.ROLE_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH)
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(Role[].class).map(roles->ResponseEntity.ok(roles));
    }

    @PostMapping(value = "/count",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Integer>> count(HttpServletRequest httpRequest) {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("count; requestUri={}", params);
        userDetailsService.verifyPermission("Role.View");
        String scopeQuery = userDetailsService.extractScopeForRoleView();
        log.trace("count; scopeQuery={}", scopeQuery);
        params.add(RBACUtil.ROLE_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH, "count")
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(Integer.class).map(count->ResponseEntity.ok(count));
    }

    @GetMapping(value = "/validationRules", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getValidationRules() {
        userDetailsService.verifyPermission("Role.View");
        return restClient.resource(RESOURCE_PATH, "validationRules")
                .build().get()
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(String.class).map(rules->ResponseEntity.ok(rules));
    }

    @GetMapping(value = "/getRolesNotAssignedToAnyGroup", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Role[]>>  getRolesNotAssignedToAnyGroup(@RequestParam MultiValueMap<String, String> uriInfo) {
        log.trace("getRolesNotAssignedToAnyGroup");
        userDetailsService.verifyPermission("Role.View");
        String scopeQuery = userDetailsService.extractScopeForRoleView();
        log.trace("getRolesNotAssignedToAnyGroup; scopeQuery={}", scopeQuery);
        uriInfo.add(RBACUtil.ROLE_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH, "getRolesNotAssignedToAnyGroup")
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(Role[].class).map(roles->ResponseEntity.ok(roles));
    }

    @GetMapping(value = "/getRolesAssignedToOtherGroups/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public  Mono<ResponseEntity<Role[]>>  getRolesAssignedToOtherGroups(@PathVariable("groupId") int groupId, @RequestParam MultiValueMap<String, String> uriInfo) {
        log.trace("getRolesAssignedToOtherGroups; groupId={}", groupId);
        userDetailsService.verifyPermission("Role.View");
        String scopeQuery = userDetailsService.extractScopeForRoleView();
        log.trace("getRolesAssignedToOtherGroups; scopeQuery={}", scopeQuery);
        uriInfo.add(RBACUtil.ROLE_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH, "getRolesAssignedToOtherGroups", Integer.toString(groupId))
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(Role[].class).map(roles->ResponseEntity.ok(roles));
    }

    @PostMapping(value = "/getScopeIds",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Scope[]>>   getScopeIds(@RequestParam MultiValueMap<String, String> uriInfo , @RequestBody Map<String, List<Integer>> roleIdsList){
    	log.trace("getScopeIds; roleIdsList={}", roleIdsList);
        userDetailsService.verifyPermission("Role.View"); 
        return restClient.resource(RESOURCE_PATH, "getScopeIds")
                .build().post()
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(roleIdsList)
                .retrieve().bodyToMono(Scope[].class)
                .map(roles -> ResponseEntity.ok(roles));
    }

    @PostMapping(value = "/getScopeIdsWithDefaultScopes",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Scope[]>> getScopeIdsWithDefaultScopes(@RequestParam MultiValueMap<String, String> uriInfo , @RequestBody Map<String, List<Integer>> roleIdsList){
    	log.trace("getScopeIdsWithDefaultScopes; roleIdsList={}", roleIdsList);
        userDetailsService.verifyPermission("Role.View"); 
       return restClient.resource(RESOURCE_PATH, "getScopeIdsWithDefaultScopes")
                .build().post()
                .header("loggedInUser", userDetailsService.getCurrentUserDetailsRBAC().getUsername())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(roleIdsList)
                .retrieve().bodyToMono(Scope[].class)
                .map(roles -> ResponseEntity.ok(roles));
    }

    @PostMapping(value = "/getRoleScopeDefinitions",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ScopeDefinition[]>>  getRoleScopeDefinitions(@RequestParam MultiValueMap<String, String> uriInfo , @RequestBody List<Map<String, Integer>> roleGroupIdList){
    	log.trace("getRoleScopeDefinitions; roleGroupIdList={}", roleGroupIdList);
        userDetailsService.verifyPermission("Role.View"); 
        return restClient.resource(RESOURCE_PATH, "getRoleScopeDefinitions")
                .build().post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleGroupIdList)
                .retrieve().bodyToMono(ScopeDefinition[].class)
                .map(roles -> ResponseEntity.ok(roles));
    }

    @PostMapping(value = "/getRoleTransition",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Boolean>> getRoleTransition(@RequestParam MultiValueMap<String, String> uriInfo , @RequestBody List<Map<String, List<Integer>>> roleList){
    	log.trace("getRoleTransition; roleList={}", roleList);
        userDetailsService.verifyPermission("Role.View"); 
        return restClient.resource(RESOURCE_PATH, "getRoleTransition")
        .build().post()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleList)
                .retrieve().bodyToMono(Boolean.class)
                .map(roles -> ResponseEntity.ok(roles));
    }

    @PostMapping(value = "/getCountForDefinedScopeRoles",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Boolean>> getCountForDefinedScopeRoles(@RequestParam MultiValueMap<String, String> uriInfo , @RequestBody Map<String, Integer> roleId){
    	log.trace("getCountForDefinedScopeRoles; roleId={}", roleId);
        userDetailsService.verifyPermission("Role.View"); 
        return restClient.resource(RESOURCE_PATH, "getCountForDefinedScopeRoles")
                .build().post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleId)
                .retrieve().bodyToMono(Boolean.class)
                .map(roles -> ResponseEntity.ok(roles));
    }
}
