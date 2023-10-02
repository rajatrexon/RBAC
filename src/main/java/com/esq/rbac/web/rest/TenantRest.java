/*
 * Copyright (c)2016 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
import com.esq.rbac.web.exception.ForbiddenException;
import com.esq.rbac.web.util.DeploymentUtil;
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.Tenant;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(TenantRest.RESOURCE_PATH)
public class TenantRest {


    public static final String RESOURCE_PATH = "tenants";
    private RestClient restClient;
    private UserDetailsService userDetailsService;
    private DeploymentUtil deploymentUtil;

    @Autowired
    public void setRestClient(RestClient restClient,DeploymentUtil deploymentUtil) {
        log.trace("setRestClient; restClient={};", restClient);
        this.restClient = restClient;
        this.deploymentUtil = deploymentUtil;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        log.trace("setUserDetailsService; userDetailsService={};", userDetailsService);
        this.userDetailsService = userDetailsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Tenant>> create(@RequestBody Tenant tenant, HttpServletRequest httpRequest) throws Exception {
        log.trace("create; tenant={}", tenant);
        userDetailsService.verifyPermission("Tenant.Create");
        return restClient.resource(RESOURCE_PATH)
                .build().post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tenant)
                .header("clientIp", RBACUtil.getRemoteAddress(httpRequest))
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .retrieve().bodyToMono(Tenant.class)
                .map(createTenant -> ResponseEntity.ok(createTenant));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Tenant>> update(@RequestBody Tenant tenant,  HttpServletRequest httpRequest) throws Exception {
        log.trace("update; tenant={}", tenant);
        userDetailsService.verifyPermission("Tenant.Update");
        checkEntityPermission(tenant.getTenantId(), "Tenant.Update");
        return restClient.resource(RESOURCE_PATH)
                .build().put()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tenant)
                .header("clientIp", RBACUtil.getRemoteAddress(httpRequest))
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .retrieve().bodyToMono(Tenant.class)
                .map(updateTenant-> ResponseEntity.ok(updateTenant));
    }

    @GetMapping(value = "/{tenantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Tenant>>  getById(@PathVariable("tenantId") Long tenantId) {
        log.trace("getById; tenantId={}", tenantId);
        if (userDetailsService.getCurrentUserDetailsRBAC().isSystemMultiTenant()) 
        	userDetailsService.verifyPermission("Tenant.View");
        checkEntityPermission(tenantId, "Tenant.View");
        return restClient.resource(RESOURCE_PATH, Long.toString(tenantId))
                .build().get()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .retrieve().bodyToMono(Tenant.class)
                .map(updateTenant-> ResponseEntity.ok(updateTenant));
    }

    @DeleteMapping("/{tenantId}")
    public void deleteById(@PathVariable("tenantId") Long tenantId, HttpServletRequest httpRequest) {
        log.trace("deleteById; tenantId={}", tenantId);
        userDetailsService.verifyPermission("Tenant.Delete");
        checkEntityPermission(tenantId, "Tenant.Delete");
        restClient.resource(RESOURCE_PATH, Long.toString(tenantId))
                .build().delete()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .header("clientIp", RBACUtil.getRemoteAddress(httpRequest))
                .retrieve().toBodilessEntity().subscribe();
    }

    @PostMapping(value = "/customTenantInfo",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Tenant[]>> list(HttpServletRequest httpRequest) {
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
        log.trace("tenants list; requestUri={}", params);
        if (!userDetailsService.getCurrentUserDetailsRBAC().isSystemMultiTenant()) {
			//dont check for permission, app layer would only return host tenant
		} else {
			userDetailsService.verifyPermission("Tenant.View");
		}
        String excludeSessionTenants = params.getFirst("excludeSessionTenants");
        String scopeQuery = null;
        if(excludeSessionTenants!=null){
        	scopeQuery = userDetailsService.extractScopeForTenant(Boolean.valueOf(excludeSessionTenants));
        }
        else{
        	scopeQuery = userDetailsService.extractScopeForTenant();
        }
        params.add(RBACUtil.TENANT_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH)
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(Tenant[].class).map(tenants->ResponseEntity.ok(tenants));
    }

    @PostMapping(value = "/searchTenantIdNamesWithCount",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<JsonNode>> searchTenantIdNamesWithCount(HttpServletRequest httpRequest) throws Exception {
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
		log.trace("tenants searchTenantIdNamesWithCount; requestUri={}", params);
		if (!userDetailsService.getCurrentUserDetailsRBAC().isSystemMultiTenant()) {
			// dont check for permission, app layer would only return host
			// tenant
		} else {
			userDetailsService.verifyPermission("Tenant.View");
		}
		String includeSessionTenants = params.getFirst("includeSessionTenants");
		String scopeQuery = null;
		if (includeSessionTenants != null) {
			scopeQuery = userDetailsService.extractScopeForTenant(!Boolean.valueOf(includeSessionTenants));
		} else {
			scopeQuery = userDetailsService.extractScopeForTenant(true);
		}
		params.add(RBACUtil.TENANT_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));

		return restClient.resource(RESOURCE_PATH, "searchTenantIdNamesWithCount")
                .build().get().uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class)
                .map(jsonNode -> ResponseEntity.ok(jsonNode));
	}

    @PostMapping(value = "/count",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Long>> count(HttpServletRequest httpRequest) {
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
        log.trace("count; requestUri={}", params);
        if (userDetailsService.getCurrentUserDetailsRBAC().isSystemMultiTenant()) 
        	userDetailsService.verifyPermission("Tenant.View");
        String scopeQuery = null;
        String excludeSessionTenants = params.getFirst("excludeSessionTenants");
        if(excludeSessionTenants!=null){
        	scopeQuery = userDetailsService.extractScopeForTenant(Boolean.valueOf(excludeSessionTenants));
        }
        else{
        	scopeQuery = userDetailsService.extractScopeForTenant();
        }
        params.add(RBACUtil.TENANT_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH, "count")
                .build().get().uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(Long.class).map(count->ResponseEntity.ok(count));
    }

    @GetMapping(value = "/validationRules", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getValidationRules() {
        userDetailsService.verifyPermission("Tenant.View");
        return restClient.resource(RESOURCE_PATH, "validationRules")
                .build().get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class)
                .map(rules->ResponseEntity.ok(rules));
    }

    @GetMapping(value = "/tenantIdNames", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getTenantIdNames(@RequestParam MultiValueMap<String, String> uriInfo) {
		if (!userDetailsService.getCurrentUserDetailsRBAC().isSystemMultiTenant()) {
			//dont check for permission, app layer would only return host tenant
		} else {
			userDetailsService.verifyPermission("Tenant.View");
		}
    	 String scopeQuery = userDetailsService.extractScopeForTenant();
         uriInfo.add(RBACUtil.TENANT_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
         return restClient.resource(RESOURCE_PATH, "tenantIdNames")
                 .build().get().uri(uriBuilder -> uriBuilder
                         .queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON).retrieve()
                 .bodyToMono(String.class).map(tenantId->ResponseEntity.ok(tenantId));
    }
    
    private void checkEntityPermission(Long tenantId, String permission){
    	MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
		String scopeQuery = userDetailsService.extractScopeForTenant(true);
		queryMap.add(RBACUtil.TENANT_SCOPE_QUERY,
				RBACUtil.encodeForScopeQuery(scopeQuery));
		queryMap.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, Long.toString(tenantId));
		if (Boolean.TRUE.equals(restClient.resource(RESOURCE_PATH, "checkEntityPermission")
                .build().get()
                .uri(uriBuilder -> uriBuilder.queryParams(queryMap).build())
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Boolean.class))) {
			return;
		}
		ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
		errorInfo.add("permission", permission);
		errorInfo.add("entity", "Tenant");
		errorInfo.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, tenantId.toString());
		throw new ForbiddenException(errorInfo.getErrorCode());
    }
    
    //RBAC-1562 Starts
    @GetMapping(value = "/isTwoFactorActiveForTenant", produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean isTwoFactorActiveForTenant() {
    	log.trace("isTwoFactorActiveForTenant(); {}",deploymentUtil.isEnableTwoFactorAuth());
        return deploymentUtil.isEnableTwoFactorAuth();
    }
    //RBAC-1562 Ends
    
}
