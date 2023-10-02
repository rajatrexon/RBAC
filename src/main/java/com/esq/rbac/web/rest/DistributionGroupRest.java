package com.esq.rbac.web.rest;

import com.esq.rbac.web.client.RestClient;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.exception.ErrorInfo;
import com.esq.rbac.web.util.DeploymentUtil;
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.DistUserMap;
import com.esq.rbac.web.vo.DistributionGroup;
import com.esq.rbac.web.vo.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
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
@RequestMapping(DistributionGroupRest.RESOURCE_PATH)
public class DistributionGroupRest {

	public static final String RESOURCE_PATH = "distGroup";
	private RestClient restClient;
	private UserDetailsService userDetailsService;

	private DeploymentUtil deploymentUtil;

	@Autowired
	public void setRestClient(RestClient restClient) {
		log.trace("setRestClient; restClient={};", restClient);
		this.restClient = restClient;
	}

	@Autowired
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		log.trace("setUserDetailsService; userDetailsService={};", userDetailsService);
		this.userDetailsService = userDetailsService;
	}

	@Autowired
	public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
		log.trace("setDeploymentUtil; deploymentUtil={};", deploymentUtil);
		this.deploymentUtil = deploymentUtil;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<DistributionGroup>>  create(@RequestBody DistributionGroup distributionGroup, HttpServletRequest httpRequest)
			throws Exception {
		log.trace("create; distribution group={}", distributionGroup);
		userDetailsService.verifyPermission("DistributionGroup.Create");
		return restClient.resource(RESOURCE_PATH)
				.build().post()
				.header("clientIp", RBACUtil.getRemoteAddress(httpRequest))
				.header("userId",String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(distributionGroup)
				.retrieve()
				.bodyToMono(DistributionGroup.class)
				.map(savedGroup -> ResponseEntity.ok(savedGroup));
	}

	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<DistributionGroup>>  update(@RequestBody DistributionGroup distributionGroup, HttpServletRequest httpRequest)
			throws Exception {
		log.trace("update; distributionGroup={}", distributionGroup);
		userDetailsService.verifyPermission("DistributionGroup.Update");
		return restClient.resource(RESOURCE_PATH)
				.build().put()
				.header("userId",String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(distributionGroup)
				.retrieve()
				.bodyToMono(DistributionGroup.class)
				.map(savedGroup -> ResponseEntity.ok(savedGroup));
	}

	@DeleteMapping
	public void deleteDistributionGroupById(HttpServletRequest servletRequest) {
		//log.trace("deleteDistributionGroupById; distId={}", uriInfo.getQueryParameters());
		userDetailsService.verifyPermission("DistributionGroup.Delete");
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
		uriInfo.add("userId",userDetailsService.getCurrentUserDetails().getUserInfo().getUserId() + "");
		uriInfo.add("userId",100 + "");
		 restClient.resource(RESOURCE_PATH)
				.build().delete()
				.uri(uriBuilder -> uriBuilder.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().toBodilessEntity().subscribe();
	}

	@PostMapping(value = "/assignUsers", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> assignMultipleUserToDistGroup(@RequestBody DistUserMap distUserMap, HttpServletRequest httpRequest)
			throws Exception {
		log.trace("assignMultipleUserToDistGroup; distribution group={}", distUserMap);
		userDetailsService.verifyPermission("DistributionGroup.UserMap");
		return restClient.resource(RESOURCE_PATH, "assignUsers")
				.build().post()
				.header("userId",String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(distUserMap)
				.retrieve()
				.bodyToMono(String.class)
				.map(savedGroup -> ResponseEntity.ok(savedGroup));
	}

	@PostMapping(value = "/unassignUsers", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> unassignMultipleUserToDistGroup(@RequestBody DistUserMap distUserMap, HttpServletRequest httpRequest)
			throws Exception {
		log.trace("assignMultipleUserToDistGroup; distribution group={}", distUserMap);
		userDetailsService.verifyPermission("DistributionGroup.UserMap");

		return restClient.resource(RESOURCE_PATH, "unassignUsers")
				.build().post()
				.header("userId",String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(distUserMap)
				.retrieve()
				.bodyToMono(String.class)
				.map(savedGroup -> ResponseEntity.ok(savedGroup));
	}

	@GetMapping(value = "/customDistGroupInfo",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getCustomDistGroupInfo(@PathParam("distName") String distName, HttpServletRequest httpRequest) {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
		userDetailsService.verifyPermission("DistributionGroup.View");
		String scopeQuery = userDetailsService.extractScopeForUserView();
		log.trace("getCustomDistGroupInfo; scopeQuery={}", scopeQuery);
		params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		//String response = restClient.resource(RESOURCE_PATH).queryParams(params).accept(MediaType.APPLICATION_JSON).get(String.class);
		return restClient.resource(RESOURCE_PATH)
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
						.accept(MediaType.APPLICATION_JSON)
						.retrieve()
						.bodyToMono(String.class)
						.map(savedGroup -> ResponseEntity.ok(savedGroup));
	}

	@GetMapping(value="/{distId}",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<DistributionGroup>> getDistGroupById(@PathVariable("distId") int distId) {
		log.trace("getDistGroupById; distId={}", distId);
		userDetailsService.verifyPermission("DistributionGroup.View");
		return restClient.resource(RESOURCE_PATH, Integer.toString(distId))
				.build().get()
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(DistributionGroup.class)
				.map(savedGroup -> ResponseEntity.ok(savedGroup));
	}

	@GetMapping(value="/count",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Integer>> count(HttpServletRequest httpRequest) {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
		log.trace("count; requestUri={}", params);

		userDetailsService.verifyPermission("DistributionGroup.View");
		String scopeQuery = userDetailsService.extractScopeForUserView();
		log.trace("count; scope={}", scopeQuery);
		params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "count")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Integer.class)
				.map(integer -> ResponseEntity.ok(integer));
	}

	@PostMapping(value="/countAssigned",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Integer>>  countAssigned(HttpServletRequest httpRequest) {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
		log.trace("count; requestUri={}", params);

		userDetailsService.verifyPermission("DistributionGroup.View");
		String scopeQuery = userDetailsService.extractScopeForUserView();
		log.trace("count; scope={}", scopeQuery);
		params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "countAssigned")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Integer.class)
				.map(integer -> ResponseEntity.ok(integer));
	}

	@Deprecated
	@DeleteMapping(value = "/distUserMap")
	public void deleteUsersFromDistGroup(HttpServletRequest servletRequest) {
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

		log.debug("deleteUsersFromDistGroup; distUserMap={};" + uriInfo);
		userDetailsService.verifyPermission("DistributionGroup.Delete");
		restClient.resource(RESOURCE_PATH, "distUserMap")
				.build().delete()
				.uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.retrieve().toBodilessEntity().subscribe();
	}

	@GetMapping(value="/unassignedUserListInDistGroup",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<User[]>> unassignedUserListInDistGroup(HttpServletRequest servletRequest) {
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

		log.trace("unassignedUserListInDistGroup; requestUri={}", servletRequest.getRequestURI());
		userDetailsService.verifyPermission("DistributionGroup.View");
		return restClient.resource(RESOURCE_PATH, "unassignedUserListInDistGroup")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(User[].class)
				.map(users -> ResponseEntity.ok(users));
	}

	@GetMapping(value="/assignedUserListInDistGroup",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<User[]>> assignDistGroupUserList(HttpServletRequest servletRequest) {
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

		log.trace("assignDistGroupUserList; requestUri={}", servletRequest.getRequestURI());
		userDetailsService.verifyPermission("DistributionGroup.View");
		return restClient.resource(RESOURCE_PATH, "assignedUserListInDistGroup")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(User[].class)
				.map(users -> ResponseEntity.ok(users));
	}

	@PostMapping(value="/countUnAssigned",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Integer>> countUnAssigned(HttpServletRequest httpRequest) {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
		log.trace("count; requestUri={}", params);

		userDetailsService.verifyPermission("DistributionGroup.View");
		String scopeQuery = userDetailsService.extractScopeForUserView();
		log.trace("count; scope={}", scopeQuery);
		params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "countUnAssigned")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Integer.class).map(integer -> ResponseEntity.ok(integer));
	}

	@GetMapping(value="/isDistGroupEnabled",produces = MediaType.APPLICATION_JSON_VALUE)
	public Boolean isDistGroupEnabled(HttpServletRequest servletRequest) {
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		log.trace("isDistGroupEnabled; requestUri={}", servletRequest.getRequestURI());
		return deploymentUtil.getEnableDistributionGroup();
	}

	@GetMapping(value="/distListByUserChoice",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<User>> getById(@PathParam("distId") int distId) {
		log.trace("getById; userId={}", distId);
		userDetailsService.verifyPermission("User.View");
		checkEntityPermission(distId, "User.View");
		return restClient.resource(RESOURCE_PATH, Integer.toString(distId))
				.build().get()
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(User.class).map(user -> ResponseEntity.ok(user));
	}

	private void checkEntityPermission(Integer userId, String permission) {
		MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
		String scopeQuery = userDetailsService.extractScopeForUserView();
		queryMap.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		queryMap.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, Integer.toString(userId));
		if (Boolean.TRUE.equals(restClient.resource(RESOURCE_PATH, "checkEntityPermission")
						.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(queryMap).build())
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Boolean.class))){
			return;
		}
		ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
		errorInfo.add("permission", permission);
		errorInfo.add("entity", "User");
		errorInfo.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, userId.toString());
		throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(errorInfo).build());
	}

}
