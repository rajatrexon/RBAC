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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ScopeBuilderRest.RESOURCE_PATH)
public class ScopeBuilderRest {

	public static final String RESOURCE_PATH = "scopeBuilder";
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

	@PostMapping(value = "/filters",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getScopeFilters(HttpServletRequest httpRequest) {
		Map<String, String[]> parameterMap = httpRequest.getParameterMap();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("Group.View");
		params.add("userName",userDetailsService.getCurrentUserDetails().getUsername());
		return restClient.resource(RESOURCE_PATH, "filters")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String.class).map(role -> ResponseEntity.ok(role));
	}

	@PostMapping(value = "/filterData/{sourcePath}",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getFilterKeyData(HttpServletRequest httpRequest, @PathParam("sourcePath") String sourcePath) {
		Map<String, String[]> parameterMap = httpRequest.getParameterMap();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("Group.View");
		params.add("userName",userDetailsService.getCurrentUserDetails().getUsername());
		
		//fetching only rbac scopes to avoid all scope data being passed to app layer
		Map<String, String> scopeMap = new LinkedHashMap<String, String>();
		
		String excludeSessionTenants = params.getFirst("excludeSessionTenants");
		if(excludeSessionTenants!=null){
			scopeMap.put(RBACUtil.SCOPE_KEY_USER_VIEW, userDetailsService.extractScopeForUserView(Boolean.valueOf(excludeSessionTenants)));
			scopeMap.put(RBACUtil.SCOPE_KEY_GROUP_VIEW, userDetailsService.extractScopeForGroupView(Boolean.valueOf(excludeSessionTenants)));
			scopeMap.put(RBACUtil.SCOPE_KEY_TENANT, userDetailsService.extractScopeForTenant(Boolean.valueOf(excludeSessionTenants)));
			scopeMap.put(RBACUtil.ORGANIZATION_SCOPE_QUERY, userDetailsService.extractScopeForOrganization(Boolean.valueOf(excludeSessionTenants)));
        }
        else{
        	scopeMap.put(RBACUtil.SCOPE_KEY_USER_VIEW, userDetailsService.extractScopeForUserView());
    		scopeMap.put(RBACUtil.SCOPE_KEY_GROUP_VIEW, userDetailsService.extractScopeForGroupView());
    		scopeMap.put(RBACUtil.SCOPE_KEY_TENANT, userDetailsService.extractScopeForTenant());
    		scopeMap.put(RBACUtil.ORGANIZATION_SCOPE_QUERY, userDetailsService.extractScopeForOrganization());
        }
		scopeMap.put(RBACUtil.SCOPE_KEY_ROLE_VIEW, userDetailsService.extractScopeForRoleView());

		return restClient.resource(RESOURCE_PATH, "filterData",sourcePath)
				.build().post()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.bodyValue(scopeMap)
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String.class).map(keys -> ResponseEntity.ok(keys));
	}

	@PostMapping(value = "/filterData",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getFilterKeyData(HttpServletRequest httpRequest) {
		Map<String, String[]> parameterMap = httpRequest.getParameterMap();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("Group.View");
		params.add("userName",userDetailsService.getCurrentUserDetails().getUsername());
		
		//fetching only rbac scopes to avoid all scope data being passed to app layer
		Map<String, String> scopeMap = new LinkedHashMap<String, String>();
		String excludeSessionTenants = params.getFirst("excludeSessionTenants");
		if(excludeSessionTenants!=null){
			scopeMap.put(RBACUtil.SCOPE_KEY_USER_VIEW, userDetailsService.extractScopeForUserView(Boolean.valueOf(excludeSessionTenants)));
			scopeMap.put(RBACUtil.SCOPE_KEY_GROUP_VIEW, userDetailsService.extractScopeForGroupView(Boolean.valueOf(excludeSessionTenants)));
			scopeMap.put(RBACUtil.SCOPE_KEY_TENANT, userDetailsService.extractScopeForTenant(Boolean.valueOf(excludeSessionTenants)));
			scopeMap.put(RBACUtil.ORGANIZATION_SCOPE_QUERY, userDetailsService.extractScopeForOrganization(Boolean.valueOf(excludeSessionTenants)));
        }
        else{
        	scopeMap.put(RBACUtil.SCOPE_KEY_USER_VIEW, userDetailsService.extractScopeForUserView());
    		scopeMap.put(RBACUtil.SCOPE_KEY_GROUP_VIEW, userDetailsService.extractScopeForGroupView());
    		scopeMap.put(RBACUtil.SCOPE_KEY_TENANT, userDetailsService.extractScopeForTenant());
    		scopeMap.put(RBACUtil.ORGANIZATION_SCOPE_QUERY, userDetailsService.extractScopeForOrganization());
        }
		scopeMap.put(RBACUtil.SCOPE_KEY_ROLE_VIEW, userDetailsService.extractScopeForRoleView());
		return restClient
				.resource(RESOURCE_PATH, "filterData")
				.build().post()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.bodyValue(scopeMap)
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String.class).map(keys -> ResponseEntity.ok(keys));
	}

	@PostMapping(value = "/validateScopeBuilderOutput",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> validateScopeBuilderOutput(@RequestBody String data, HttpServletRequest httpServletRequest){
		Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("Group.View");
		params.add("userName",userDetailsService.getCurrentUserDetails().getUsername());
		return restClient
				.resource(RESOURCE_PATH, "validateScopeBuilderOutput")
				.build().post()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.bodyValue(data)
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String.class).map(keys -> ResponseEntity.ok(keys));
	}
}
