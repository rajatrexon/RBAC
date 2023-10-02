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
import com.esq.rbac.web.exception.ErrorInfo;
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.Group;
import com.esq.rbac.web.vo.Scope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
@RequestMapping(GroupRest.RESOURCE_PATH)
public class GroupRest {

    private static final Logger log = LoggerFactory.getLogger(GroupRest.class);
    public static final String RESOURCE_PATH = "groups";

    private UserDetailsService userDetailsService;

    @Value("${ssoServer.username}")
    private String username;

    @Value("${ssoServer.password}")
    private String password;

    @Value("${ssoServer.readTimeoutMs}")
    private int readTimeout;

    @Value("${ssoServer.connectTimeoutMs}")
    private int connectTimeout;

    @Value("${ssoServer.maxTotalConnections}")
    private int maxTotalCon;

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    private RestClient restClient;

    private RestTemplate newRestTemplate() {
        return new RestTemplate(getHttpComponentsClientHttpRequestFactory());
    }

    private HttpComponentsClientHttpRequestFactory getHttpComponentsClientHttpRequestFactory() {
        final SSLConnectionSocketFactory sslsf;
        try {
            sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(),
                    NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslsf)
                .build();

        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(maxTotalCon);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManager(cm)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        //factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectTimeout);
       // factory.setHttpClient(httpClient);
        return factory;
    }

    @PostMapping
    public Mono<ResponseEntity<Group>> create(@RequestBody Group group, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("create; group:{}", group);
        userDetailsService.verifyPermission("Group.Create");
        return restClient
                .resource(RESOURCE_PATH) // Replace with the actual endpoint to save a Group
                .build()
                .post()
                .header("userId",String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(group)
                .retrieve()
                .bodyToMono(Group.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @PutMapping
    public Mono<ResponseEntity<Group>> update(@RequestBody Group group, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("update; group={}", group);
        userDetailsService.verifyPermission("Group.Update");
        checkEntityPermission(group.getGroupId(), "Group.Update");
        if(group.getGroupId().equals(userDetailsService.getCurrentUserGroupId())){
            userDetailsService.verifyPermission("Group.SelfUpdate");
        }
        return restClient.resource(RESOURCE_PATH)
               .build()
                .put()
                .header("userId",String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(group)
                .retrieve()
                .bodyToMono(Group.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @PutMapping("/roles")
    public Mono<ResponseEntity<Group>> updateRoles(@RequestBody Group group, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("updateRoles; group={}", group);
        userDetailsService.verifyPermission("Group.Update");
        if(group.getGroupId().equals(userDetailsService.getCurrentUserGroupId())){
        	userDetailsService.verifyPermission("Group.SelfUpdate");
        }
        Boolean isUndefinedScopesAllowed = true;
        try{
        	userDetailsService.verifyPermission("Group.Allow.Undefined.Scopes");
        }
        catch(WebApplicationException e){
        	isUndefinedScopesAllowed = false;
        }
        Boolean isUndefinedScopes = isUndefinedScopesAllowed;
        String authStr = username+":"+password;
        String base64Creds = Base64.toBase64String(authStr.getBytes());
        return restClient.resource(RESOURCE_PATH)
                .build()
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path("/roles")
                        .queryParam("isUndefinedScopesAllowed", isUndefinedScopes)
                        .build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .header("Authorization", "Basic " +base64Creds)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(group)
                .retrieve()
                .bodyToMono(Group.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @PutMapping("/cloneScopeFromGroup/{groupId}")
    public Mono<ResponseEntity<Group>> cloneScopeDefinitionFromGroup(@PathVariable("groupId") int fromGroupId, Map<Integer, List<Integer>> fromScopeToGroupIds,@RequestHeader HttpHeaders headers) throws Exception {
        log.trace("cloneScopeDefinitionFromGroup; fromScopeToGroupIds={}", fromScopeToGroupIds);
        userDetailsService.verifyPermission("Group.Update");
        Boolean isUndefinedScopesAllowed = true;
        try{
        	userDetailsService.verifyPermission("Group.Allow.Undefined.Scopes");
        }
        catch(WebApplicationException e){
        	isUndefinedScopesAllowed = false;
        }
        Boolean isUndefinedScopes = isUndefinedScopesAllowed;
        return restClient.resource(RESOURCE_PATH)
                .build()
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path("/cloneScopeDefinitionForGroup/"+fromGroupId)
                        .queryParam("isUndefinedScopesAllowed", isUndefinedScopes)
                        .build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(fromScopeToGroupIds)
                .retrieve()
                .bodyToMono(Group.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @PutMapping("/cloneGroup")
    public Mono<ResponseEntity<Group>> cloneGroup(@QueryParam("fromGroupId") int fromGroupId, @QueryParam("toGroupId") int toGroupId, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("cloneGroup; fromGroupId={}", fromGroupId);
        userDetailsService.verifyPermission("Group.Update");
        Boolean isUndefinedScopesAllowed = true;
        try{
        	userDetailsService.verifyPermission("Group.Allow.Undefined.Scopes");
        }
        catch(WebApplicationException e){
            isUndefinedScopesAllowed = false;
        }
        Boolean isUndefinedScopes = isUndefinedScopesAllowed;
        return restClient.resource(RESOURCE_PATH)
                .build()
                .put()
               .uri(uriBuilder -> uriBuilder
                        .path("/cloneGroup")
                        .queryParam("fromGroupId", fromGroupId)
                        .queryParam("toGroupId", toGroupId)
                        .queryParam("isUndefinedScopesAllowed", isUndefinedScopes)
                        .build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Group.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }


    @GetMapping("/{groupId}")
    public Mono<ResponseEntity<Group>> getById(@PathVariable("groupId") int groupId) {
        log.trace("getById; groupId={}", groupId);
        userDetailsService.verifyPermission("Group.View");
        checkEntityPermission(groupId, "Group.View");
        Mono<Group> applicationsMono =restClient
                .resource("groups/"+groupId)
                .build().get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Group.class);
        return applicationsMono.map(groups -> ResponseEntity.ok(groups));
    }

    @DeleteMapping("/{groupId}")
    public void deleteById(@PathVariable("groupId") int groupId) {
        log.trace("deleteById; groupId={}", groupId);
        userDetailsService.verifyPermission("Group.Delete");
        checkEntityPermission(groupId, "Group.Delete");

        restClient.resource("groups/"+Integer.toString(groupId))
                .build()
                .delete()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .retrieve()
                .toBodilessEntity() // If you don't expect a response body.
                .subscribe();
    }

    @GetMapping("/{groupId}/scopeIds")
    public Mono<ResponseEntity<Integer[]>> getScopeIds(@PathVariable("groupId") int groupId) {
        log.trace("getScopeIds; groupId={}", groupId);
        userDetailsService.verifyPermission("Group.View");
        return restClient.resource("groups/"+groupId)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/scopeIds")
                        .build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                    .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Integer[].class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @GetMapping("/{groupId}/scopeIdsWithDefaultScopes")
    public  Mono<ResponseEntity<Scope[]>> getScopeIdsWithDefaultScopes(@PathParam("groupId") int groupId) {
        log.trace("getScopeIdsWithDefaultScopes; groupId={}", groupId);
        userDetailsService.verifyPermission("Group.View");
        return restClient.resource("groups/"+groupId)
                .build()
                .get()
               .uri(uriBuilder -> uriBuilder
                        .path("/getScopeIdsWithDefaultScopes")
                        .build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Scope[].class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @PostMapping(value = "/customGroupInfo", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<ResponseEntity<Group[]>> list(HttpServletRequest httpRequest) {
   	 	MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("list; requestUri={}", params);
        userDetailsService.verifyPermission("Group.View");
        String scopeQuery = userDetailsService.extractScopeForGroupView();
        log.trace("list; scopeQuery={}", scopeQuery);
        params.add(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH)
                .build().get().uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Group[].class)
                .map(groups -> ResponseEntity.ok(groups));
    }

    @PostMapping("/count")
    public Mono<ResponseEntity<Integer>> count(HttpServletRequest httpRequest) {

        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        //MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        userDetailsService.verifyPermission("Group.View");
        String scopeQuery = userDetailsService.extractScopeForGroupView();
        log.trace("count; scopeQuery={}", scopeQuery);
        params.add(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH+"/count") .build() .get()
               .uri(uriBuilder -> uriBuilder
                        .queryParams(params)
                        .build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Integer.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @GetMapping("/allGroupRoleScopes")
    public Mono<ResponseEntity<Map<Group,List<String>>>> getAllGroupRoleScopes(HttpServletRequest httpRequest) {
        userDetailsService.verifyPermission("Group.View");
        String scopeQuery = userDetailsService.extractScopeForGroupView();
        log.trace("getAllGroupRoleScopes; scopeQuery={}", scopeQuery);
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        //MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        MultiValueMap<String, String> uriInfo = WebParamsUtil.paramsToMap(httpRequest);
        uriInfo.add(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH+"allGroupRoleScopes")
                .build().get().uri(uriBuilder -> uriBuilder
                        .queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/getAllGroupWithIdenticalScopeDefinition")
    public Mono<ResponseEntity<List<Map<String, String>>>> getAllGroupWithIdenticalScopeDefinition(HttpServletRequest httpRequest) {
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));

        userDetailsService.verifyPermission("Group.View");
        Boolean isSelfUpdateAllowed = true;
        try{
        	userDetailsService.verifyPermission("Group.SelfUpdate");
        }
        catch(WebApplicationException e){
        	isSelfUpdateAllowed = false;

        }
        params.add("isSelfUpdateAllowed", isSelfUpdateAllowed.toString());
        params.add("userId", userDetailsService.getCurrentUserDetails().getUserInfo().getUserId().toString());
        String scopeQuery = userDetailsService.extractScopeForGroupView();
        log.trace("getAllGroupWithIdenticalScopeDefinition; scopeQuery={}", scopeQuery);
        params.add(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource("groups/")
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getAllGroupWithIdenticalScopeDefinition")
                        .queryParams(params)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(List.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @SuppressWarnings("unchecked")
    @PostMapping(value = "/getAllGroupWithUndefinedScopes", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public  Mono<ResponseEntity<List<Map<String, String>>>> getAllGroupWithUndefinedScopes(HttpServletRequest httpRequest) {
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));

//        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        userDetailsService.verifyPermission("Group.View");
        Boolean isSelfUpdateAllowed = true;
        try{
        	userDetailsService.verifyPermission("Group.SelfUpdate");
        }
        catch(WebApplicationException e){
        	isSelfUpdateAllowed = false;

        }
        params.add("isSelfUpdateAllowed", isSelfUpdateAllowed.toString());
        params.add("userId", userDetailsService.getCurrentUserDetails().getUserInfo().getUserId().toString());
        String scopeQuery = userDetailsService.extractScopeForGroupView();
        log.trace("getAllGroupWithUndefinedScopes; scopeQuery={}", scopeQuery);
        params.add(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH,"getAllGroupWithUndefinedScopes")
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build()).retrieve()
                .bodyToMono(List.class).map(savedGroup -> ResponseEntity.ok(savedGroup));
    }
    @GetMapping(value = "/validationRules")
    public Mono<ResponseEntity<String>> getValidationRules() {
        userDetailsService.verifyPermission("Group.View");
        return restClient.resource("groups/")
                .build().get()
                .uri(uriBuilder -> uriBuilder.path("/validationRules")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @GetMapping(value = "/groupIdNames")
    public Mono<ResponseEntity<String>> getGroupIdNames(HttpServletRequest request) {
    	 userDetailsService.verifyPermission("Group.View");
    	 String scopeQuery = userDetailsService.extractScopeForGroupView();
         log.trace("getGroupIdNames; scopeQuery={}", scopeQuery);
         MultiValueMap<String, String> uriInfo = WebParamsUtil.paramsToMap(request);
        uriInfo.add(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource("groups/")
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/groupIdNames")
                        .queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }


    @GetMapping("/usersInGroupsData")
    public Mono<ResponseEntity<String>> getUsersInGroupsData(HttpServletRequest httpRequest) {

        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));

        userDetailsService.verifyPermission("Group.View");
        userDetailsService.verifyPermission("User.View");
        Map<String, String> scopeMap = new LinkedHashMap<String, String>();
        scopeMap.put(RBACUtil.SCOPE_KEY_USER_VIEW, userDetailsService.extractScopeForUserView());
        scopeMap.put(RBACUtil.SCOPE_KEY_GROUP_VIEW, userDetailsService.extractScopeForGroupView());
        log.trace("getUsersInGroupsData; scopeMap={}", scopeMap);
        log.trace("getUsersInGroupsData; scopeMap={}", scopeMap);

        return restClient
                .resource(RESOURCE_PATH,"usersInGroupsData") // Replace with the actual endpoint to save a Group
                .build().post()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(scopeMap)
                .retrieve()
                .bodyToMono(String.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }
    @PostMapping(value = "/updateUsersInGroups", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<Map<String, Object>>>> updateUsersInGroups(@RequestBody List<Map<String, Object>> userGroupList, HttpServletRequest servletRequest) {
        log.debug("updateUsersInGroups;  userGroupList={};", userGroupList);
        userDetailsService.verifyPermission("User.Update");
        return restClient
                .resource(RESOURCE_PATH, "updateUsersInGroups")
                .build().put()
                .uri(uriBuilder -> uriBuilder
                        .build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userGroupList)
                .retrieve()
                .bodyToMono(List.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }


    @GetMapping(value = "/rolesInGroupsData")
    public Mono<ResponseEntity<String>> getRolesInGroupsData(HttpServletRequest servletRequest) {
        userDetailsService.verifyPermission("Group.View");
        userDetailsService.verifyPermission("Role.View");
        log.trace("getGroupIdNamesWithScope; requestUri={}", servletRequest.getRequestURI());
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        Map<String, String> scopeMap = new LinkedHashMap<String, String>();
        scopeMap.put(RBACUtil.SCOPE_KEY_ROLE_VIEW, userDetailsService.extractScopeForRoleView());
        scopeMap.put(RBACUtil.SCOPE_KEY_GROUP_VIEW, userDetailsService.extractScopeForGroupView());
        return restClient.resource(RESOURCE_PATH,"rolesInGroupsData")
                .build().post()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(uriInfo)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(scopeMap)
                .retrieve()
                .bodyToMono(String.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }
    @GetMapping("/groupRoleScopeNames/{groupId}")
    public Mono<ResponseEntity<String>> getGroupRoleScopeNames(@PathVariable("groupId") int groupId, HttpServletRequest uriInfo) {
        MultiValueMap valueMap = WebParamsUtil.paramsToMap(uriInfo);
        log.trace("getGroupRoleScopeNames; groupId={}", groupId);
        userDetailsService.verifyPermission("Group.View");
       return restClient.resource("groups/")
                .build().get().uri(uriBuilder -> uriBuilder
                        .path("/groupRoleScopeNames/"+groupId)
                        .queryParams(valueMap).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(savedGroup -> ResponseEntity.ok(savedGroup));
        }

    private void checkEntityPermission(Integer groupId, String permission){
        MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
        String scopeQuery = userDetailsService.extractScopeForGroupView();
        queryMap.add(RBACUtil.GROUP_SCOPE_QUERY,
                RBACUtil.encodeForScopeQuery(scopeQuery));
        queryMap.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, Integer.toString(groupId));
        if (Boolean.TRUE.equals(restClient.resource(RESOURCE_PATH, "checkEntityPermission")
                .build().get().uri(uriBuilder -> uriBuilder
                        .queryParams(queryMap).build())
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Boolean.class))){
            return;
        };
        ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
        errorInfo.add("permission", permission);
        errorInfo.add("entity", "Group");
        errorInfo.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, groupId.toString());
        throw new WebApplicationException(Response
                .status(Response.Status.FORBIDDEN).entity(errorInfo).build());
    }
}
