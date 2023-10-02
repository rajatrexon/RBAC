/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
import com.esq.rbac.web.vo.DistributionGroup;
import com.esq.rbac.web.vo.Organization;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(OrganizationMaintenanceRest.RESOURCE_PATH)
public class OrganizationMaintenanceRest {

	public static final String RESOURCE_PATH = "organizations";
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
	public Mono<ResponseEntity<Organization>> create(@RequestBody Organization orgMaintenance)
			throws Exception {
		log.trace("create; Organization={}", orgMaintenance);
		userDetailsService.verifyPermission("Organization.Create");
		return restClient.resource(RESOURCE_PATH).build().post()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(orgMaintenance)
				.retrieve().bodyToMono(Organization.class)
				.map(organization -> ResponseEntity.ok(organization));
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Organization[]>> list(HttpServletRequest servletRequest) {
		log.trace("list; Organization--requestUri={}",servletRequest.getRequestURI());

		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("Organization.View");
		String scopeQuery = userDetailsService.extractScopeForOrganization();
        uriInfo.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH)
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Organization[].class).map(organizations -> ResponseEntity.ok(organizations));
	}

	@PostMapping(value = "/count" ,consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Integer>> count(HttpServletRequest httpRequest) {
   	 	MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
		log.trace("count; requestUri={}", params);
		userDetailsService.verifyPermission("Organization.View");
		String scopeQuery = userDetailsService.extractScopeForOrganization();
		params.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "count")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Integer.class).map(integer -> ResponseEntity.ok(integer));
	}

	@DeleteMapping("/{organizationId}")
	public void deleteById(HttpServletRequest servletRequest,
			@PathParam("organizationId") int organizationId) {
		log.trace("deleteById; organizationId={}", organizationId);
		log.trace("deleteById; uriInfo={}", servletRequest.getRequestURI());

		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("Organization.Delete");
		restClient
				.resource(RESOURCE_PATH, Integer.toString(organizationId))
				.build().delete().uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.retrieve().toBodilessEntity().subscribe();
	}

	@GetMapping(value = "/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Organization>> getById(
			@PathVariable("organizationId") int organizationId) {
		log.trace("getById; organizationId={}", organizationId);
		//userDetailsService.verifyPermission("Organization.View");
		return restClient
				.resource(RESOURCE_PATH, Integer.toString(organizationId))
				.build().get()
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(Organization.class)
				.map(organization -> ResponseEntity.ok(organization));
	}

	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Organization>>  update(@RequestBody Organization orgMaintain)
			throws Exception {
		log.trace("update; Organization={}", orgMaintain);
		userDetailsService.verifyPermission("Organization.Update");
		return restClient
				.resource(RESOURCE_PATH).build().put()
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(Organization.class)
				.map(organization -> ResponseEntity.ok(organization));
	}

	@GetMapping(value = "/validationRules", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getValidationRules() {
		userDetailsService.verifyPermission("Organization.View");
		return restClient.resource(RESOURCE_PATH, "validationRules")
				.build().get()
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(String.class).map(s -> ResponseEntity.ok(s));
	}


	@PostMapping(value = "/hierarchy", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getOrganizationHierarchy(HttpServletRequest httpRequest) {
   	 	MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
		userDetailsService.verifyPermission("Organization.View");
		String scopeQuery = userDetailsService.extractScopeForOrganization();
		params.add(RBACUtil.ORGANIZATION_SCOPE_QUERY,RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "hierarchy")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String.class).map(s->ResponseEntity.ok(s));
	}
    

		@PostMapping(value = "/customOrganizationInfo", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		public Mono<ResponseEntity<String>> getCustomOrganizationInfo(HttpServletRequest httpRequest) {
	    	 MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
	    	 userDetailsService.verifyPermission("Organization.View");
	    	 String scopeQuery = userDetailsService.extractScopeForOrganization();
	    	 params.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
	         return restClient.resource(RESOURCE_PATH, "customOrganizationInfo")
					 .build().get()
					 .uri(uriBuilder -> uriBuilder
							 .queryParams(params).build())
	                .accept(MediaType.APPLICATION_JSON).retrieve()
					 .bodyToMono(String.class).map(s -> ResponseEntity.ok(s));
	    }

		@GetMapping(value = "/organizationIdNames", produces = MediaType.APPLICATION_JSON_VALUE)
	    public Mono<ResponseEntity<String>> getOrganizationIdNames(HttpServletRequest servletRequest) {

			Map<String, String[]> parameterMap = servletRequest.getParameterMap();
			MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
			parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
	    	 userDetailsService.verifyPermission("Organization.View");
	    	 String scopeQuery = userDetailsService.extractScopeForOrganization();
	         uriInfo.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
	         return restClient.resource(RESOURCE_PATH, "organizationIdNames")
					 .build().get()
					 .uri(uriBuilder -> uriBuilder
							 .queryParams(uriInfo).build())
	                .accept(MediaType.APPLICATION_JSON).retrieve()
					 .bodyToMono(String.class).map(s -> ResponseEntity.ok(s));
	    }

		@GetMapping(value = "/organizationByTenantId", produces = MediaType.APPLICATION_JSON_VALUE)
	    public Mono<ResponseEntity<String>> getOrganizationByTenantId(HttpServletRequest servletRequest) {
			Map<String, String[]> parameterMap = servletRequest.getParameterMap();
			MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
			parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
	    	 userDetailsService.verifyPermission("Organization.View");
	    	 String scopeQuery = userDetailsService.extractScopeForOrganization();
	         uriInfo.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
	         return restClient.resource(RESOURCE_PATH, "organizationByTenantId")
					 .build().get()
					 .uri(uriBuilder -> uriBuilder
							 .queryParams(uriInfo).build())
	                .accept(MediaType.APPLICATION_JSON)
					 .retrieve().bodyToMono(String.class).map(s -> ResponseEntity.ok(s));
	    }
	   	
	   	
	   	
	/******* RBAC-1656 Start ******/
	@GetMapping(value = "/hierarchyGridView", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getOrganizationGridView(HttpServletRequest httpRequest) {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);

		Map<String, String[]> parameterMap = httpRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

		userDetailsService.verifyPermission("Organization.View");
		String scopeQuery = userDetailsService.extractScopeForOrganization();
		params.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "hierarchyGridView")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(String.class).map(s -> ResponseEntity.ok(s));
	}

	@GetMapping(value = "/nodesCount", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getNodeCount(HttpServletRequest servletRequest) {
		log.trace("count; requestUri={}", servletRequest.getRequestURI());

		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("Organization.View");
		String scopeQuery = userDetailsService.extractScopeForOrganization();
		uriInfo.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "nodesCount")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(String.class).map(s -> ResponseEntity.ok(s));
	}

	@GetMapping(value = "/hierarchyGridSearchData", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getSearchBoxData(HttpServletRequest servletRequest) {
		log.trace("getOrganizationHierarchyGridViewSearchBoxData; requestUri={}", servletRequest.getRequestURI());

		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("Organization.View");
		String scopeQuery = userDetailsService.extractScopeForOrganization();
		uriInfo.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "hierarchyGridSearchData")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class)
				.map(s -> ResponseEntity.ok(s));
	}

	@GetMapping(value = "/tenantOrgView", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getTenantOrganizationView(HttpServletRequest httpRequest) {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
		Map<String, String[]> parameterMap = httpRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("Organization.View");
		String scopeQuery = userDetailsService.extractScopeForOrganization();
		params.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "tenantOrgView")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(String.class).map(s -> ResponseEntity.ok(s));
	}

	@GetMapping(value = "/gridSearchData", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>>  getSearchedData(HttpServletRequest httpRequest) {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
		Map<String, String[]> parameterMap = httpRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

		log.trace("getOrganizationHierarchySearchData; requestUri={}", httpRequest.getRequestURI());
		userDetailsService.verifyPermission("Organization.View");
		String scopeQuery = userDetailsService.extractScopeForOrganization();
		params.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "gridSearchData")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class)
				.map(s -> ResponseEntity.ok(s));
	}


	@GetMapping(value = "/getBatchSizeForData", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Integer>> getBatchSizeForData(HttpServletRequest httpRequest) {
		Map<String, String[]> parameterMap = httpRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

		return restClient.resource(RESOURCE_PATH, "getBatchSizeForData")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Integer.class)
				.map(integer -> ResponseEntity.ok(integer));
	}
	/******* RBAC-1656 End ******/
}
