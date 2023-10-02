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
import com.esq.rbac.web.vo.ConstraintData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(ScopeConstraintRest.RESOURCE_PATH)
public class ScopeConstraintRest {

	public static final String RESOURCE_PATH = "scopeConstraints";
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

	@GetMapping(value = "/{constraintId}/data",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ConstraintData[]>> getConstraintsData(
			@PathVariable("constraintId") int constraintId) {
		return restClient
				.resource(RESOURCE_PATH, Integer.toString(constraintId), "data")
				.build().get()
				.accept(MediaType.APPLICATION_JSON)
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.retrieve().bodyToMono(ConstraintData[].class)
				.map(data -> ResponseEntity.ok(data));
	}

	@GetMapping(value = "/{constraintId}/inListData",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ConstraintData[]>>  getConstraintsInListData(
			@PathVariable("constraintId") int constraintId, @RequestParam MultiValueMap<String, String> uriInfo) {
		Integer userId = userDetailsService.getCurrentUserDetails().getUserInfo().getUserId();
		return restClient.resource(RESOURCE_PATH, Integer.toString(constraintId), "inListData")
		.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo)
						.queryParam("userId", userId.toString()).build())
				.retrieve().bodyToMono(ConstraintData[].class)
				.map(data -> ResponseEntity.ok(data));
	}

	@PostMapping(value = "/{scopeName}/customData",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getCustomData(
			@PathVariable("scopeName") String scopeName, HttpServletRequest httpRequest) {
   	 	MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
		return restClient
				.resource(RESOURCE_PATH, scopeName, "customData")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(String.class)
				.map(data -> ResponseEntity.ok(data));
	}

	@GetMapping(value = "/{scopeConstraintId}/attributeData",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getAttributeData(
			@PathVariable("scopeConstraintId") String scopeConstraintId,
			@RequestParam MultiValueMap<String,String> uriInfo) {
		String userScopeQuery = userDetailsService.extractScopeForUserView();
		uriInfo.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(userScopeQuery));
		String groupScopeQuery = userDetailsService.extractScopeForGroupView();
		uriInfo.add(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(groupScopeQuery));
		return restClient
				.resource(RESOURCE_PATH, scopeConstraintId, "attributeData")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(String.class)
				.map(data -> ResponseEntity.ok(data));
	}

	@PostMapping(value = "/{scopeConstraintId}/attributeData",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> updateAttributeData(
			@PathVariable("scopeConstraintId") String scopeConstraintId,@RequestBody String data,
			@HeaderParam("Content-Type") String contentType) {
		return restClient
				.resource(RESOURCE_PATH, scopeConstraintId, "attributeData")
				.build().post()
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.header("Content-Type", contentType)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(data)
				.retrieve().bodyToMono(String.class)
				.map(attributeData->ResponseEntity.ok(attributeData));
	}
	

	@GetMapping(value = "/getPartyAttributeAssociation",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getPartyAttributeAssociation(@RequestParam MultiValueMap<String, String> uriInfo,@RequestParam HttpHeaders headers) {
		return restClient.resource(RESOURCE_PATH, "getPartyAttributeAssociation")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String.class).map(attribute->ResponseEntity.ok(attribute));
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ConstraintData[]>> getConstraints(@RequestParam MultiValueMap<String, String> uriInfo) {
		return restClient.resource(RESOURCE_PATH)
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(ConstraintData[].class).map(attribute->ResponseEntity.ok(attribute));
	}

	@GetMapping(value = "/scopeConstraintsCheck/{scopeId}/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String[]>> checkConstraints(@PathVariable("scopeId") int scopeId,
			@PathVariable("groupId") int groupId) {
		return restClient.resource(RESOURCE_PATH, "scopeConstraintsCheck",Integer.toString(scopeId),
						Integer.toString(groupId)).build().get()
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String[].class).map(attribute->ResponseEntity.ok(attribute));
	}

	// construct scope definition based on various Operators INLIST, EQ etc
	@GetMapping(value = "/constructScopeDefinition/{scopeMap}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> constructScopeDefinition(
			@PathVariable("scopeMap") String scopeMap) {
		return restClient.resource(RESOURCE_PATH, "constructScopeDefinition", scopeMap)
				.build().get()
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String.class).map(scopeDefinition->ResponseEntity.ok(scopeDefinition));
	}
}
