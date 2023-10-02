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
import com.esq.rbac.web.vo.User;
import com.esq.rbac.web.vo.UserSync;
import com.esq.rbac.web.vo.UserSyncDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(UserSyncRest.RESOURCE_PATH)
public class UserSyncRest {
	private static final Logger log = LoggerFactory.getLogger(UserRest.class);
	public static final String RESOURCE_PATH = "userSync";
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

	@GetMapping(value = "/isSync", produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean isSyncRunning(HttpServletRequest request) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
		return restClient.resource(RESOURCE_PATH, "isSync")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(Boolean.class).block();
	}
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<UserSync[]>> list(HttpServletRequest servletRequest) {
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("UserSync.View");
		String scopeQuery = userDetailsService.extractScopeForUserView();
		log.trace("list; scopeQuery={}", scopeQuery);
		params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH).build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(UserSync[].class)
				.map(userSyncs -> ResponseEntity.ok(userSyncs));
	}

	/*
	 * @GET
	 * 
	 * @Produces(MediaType.APPLICATION_JSON)
	 * 
	 * @Path("/IterateLdapData") public JSONObject
	 * readLdapInitProperties(@Context UriInfo uriInfo, JSONObject jsonObject)
	 * throws Exception {
	 * 
	 * return restClient.resource(RESOURCE_PATH,
	 * "IterateLdapData").queryParams(uriInfo.getQueryParameters())
	 * .accept(MediaType.APPLICATION_JSON).get(JSONObject.class);
	 * 
	 * }
	 */

	@PostMapping(value = "/user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void create(List<UserSyncDTO> jsonObject, HttpServletRequest httpServletRequest) throws Exception {
			restClient.resource(RESOURCE_PATH).build().post()
				.header("loggedInuserId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.header("loggedInTenant", String.valueOf(userDetailsService.getCurrentUserDetailsRBAC().getSelectedTenantList().get(0)))
				.header("clientIp", RBACUtil.getRemoteAddress(httpServletRequest))
					.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).bodyValue(jsonObject)
					.retrieve().bodyToMono(UserSyncDTO.class);
	}

	@PostMapping(value = "/LdapUser", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void create(@RequestBody User ldapUser,HttpServletRequest httpServletRequest) throws Exception {

		restClient.resource(RESOURCE_PATH,"LdapUser").build().post()
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.header("clientIp", RBACUtil.getRemoteAddress(httpServletRequest))
				.accept(MediaType.APPLICATION_JSON).bodyValue(ldapUser)
				.retrieve().bodyToMono(User.class);
	}

	@GetMapping(value = "/getLastSyncDate", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getLastSyncDate(@RequestParam MultiValueMap uriInfo, @RequestParam HttpServletRequest servletRequest) {
		return restClient.resource(RESOURCE_PATH, "getLastSyncDate")
				.build().get()
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.header("clientIp", RBACUtil.getRemoteAddress(servletRequest))
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String.class).map(string -> ResponseEntity.ok(string));

	}

	@GetMapping(value = "/actionSync", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> actionSync(@RequestParam MultiValueMap uriInfo, @RequestParam HttpServletRequest servletRequest) {
		return restClient.resource(RESOURCE_PATH, "actionSync").build().get()
				.header("clientIp", RBACUtil.getRemoteAddress(servletRequest))
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String.class).map(string -> ResponseEntity.ok(string));
	}

	@GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Integer>> count(HttpServletRequest servletRequest){
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
		userDetailsService.verifyPermission("UserSync.View");
		String scopeQuery = userDetailsService.extractScopeForUserView();
		log.trace("count; scope={}", scopeQuery);
		params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "count").build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Integer.class).map(integer -> ResponseEntity.ok(integer));
	}

	@DeleteMapping(value = "/{userSyncId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteById(@PathVariable("userSyncId") Integer userSyncId, HttpServletRequest httpRequest) {
		log.trace("deleteById; userSyncId={}", userSyncId);
		userDetailsService.verifyPermission("UserSync.Delete");
		 //checkEntityPermission(userSyncId, "UserSync.Delete");
		String clientIp = RBACUtil.getRemoteAddress(httpRequest);
		restClient.resource(RESOURCE_PATH, Integer.toString(userSyncId))
				.build().delete()
				//.header("userId", userDetailsService.getCurrentUserDetails().getUserInfo().getUserId())
				.header("userId", String.valueOf(100))
				.header("clientIp", clientIp)
				.retrieve().toBodilessEntity().subscribe();
	}

	@GetMapping(value="/{userSyncId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<UserSync>> getById(@PathVariable("userSyncId") Integer userSyncId) {
		log.trace("getById; userSyncId={}", userSyncId);
		userDetailsService.verifyPermission("UserSync.View");
		return restClient.resource(RESOURCE_PATH, Integer.toString(userSyncId))
				.build().get().accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(UserSync.class).map(userSync -> ResponseEntity.ok(userSync));
	}
}
