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
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.ApplicationMaintenance;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
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
@RequestMapping(ApplicationMaintenanceRest.RESOURCE_PATH)
public class ApplicationMaintenanceRest {

	public static final String RESOURCE_PATH = "schedule";
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
	public Mono<ResponseEntity<ApplicationMaintenance>> create(@RequestBody ApplicationMaintenance appMaintain) throws Exception {
		log.trace("create; ApplicationMaintenance={}", appMaintain);
		//userDetailsService.verifyPermission("ScheduleMaintenance.Create");
		return restClient
				.resource(RESOURCE_PATH) // Replace with the actual endpoint to save a Group
				.build()
				.post()
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(appMaintain)
				.retrieve()
				.bodyToMono(ApplicationMaintenance.class)
				.map(savedGroup -> ResponseEntity.ok(savedGroup));
	}

	@PostMapping(value = "/customScheduleInfo",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ApplicationMaintenance[]>> list(HttpServletRequest httpRequest) {
		Map<String, String[]> parameterMap = httpRequest.getParameterMap();
		log.trace("list; ApplicationMaintenance--requestUri={}", httpRequest.getRequestURI());
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
		log.trace("list; ApplicationMaintenance--requestUri={}",params);
		userDetailsService.verifyPermission("ScheduleMaintenance.View");
		return restClient.resource(RESOURCE_PATH)
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(ApplicationMaintenance[].class)
				.map(applicationMaintenances -> ResponseEntity.ok(applicationMaintenances));
	}

	@PostMapping(value = "/count",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Integer>> count(HttpServletRequest httpRequest) {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
		log.trace("count; requestUri={}", params);
		userDetailsService.verifyPermission("ScheduleMaintenance.View");
		return restClient.resource(RESOURCE_PATH, "/count")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Integer.class)
				.map(integer -> ResponseEntity.ok(integer));
	}

	@DeleteMapping("/{maintenanceId}")
	public void deleteById(HttpServletRequest servletRequest,@PathVariable("maintenanceId") int maintenanceId) {
		log.trace("deleteById; maintenanceId={}", maintenanceId);
//		log.trace("deleteById; uriInfo={}", uriInfo.getRequestUri());
		userDetailsService.verifyPermission("ScheduleMaintenance.Delete");
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
		restClient
				.resource(RESOURCE_PATH, Integer.toString(maintenanceId))
				.build().delete()
				.uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.header("userId",
						String.valueOf(userDetailsService.getCurrentUserDetails()
								.getUserInfo().getUserId()))
				.retrieve().toBodilessEntity().subscribe();
	}

	@GetMapping(value = "/{maintenanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ApplicationMaintenance>> getById(@PathVariable("maintenanceId") int maintenanceId) {
		log.trace("getById; maintenanceId={}", maintenanceId);
		userDetailsService.verifyPermission("ScheduleMaintenance.View");
		return restClient
				.resource(RESOURCE_PATH, Integer.toString(maintenanceId))
				.build().get().accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(ApplicationMaintenance.class)
				.map(applicationMaintenance -> ResponseEntity.ok(applicationMaintenance));
	}

	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ApplicationMaintenance>> update(@RequestBody ApplicationMaintenance appMaintain)
			throws Exception {
		log.trace("update; ApplicationMaintenance={}", appMaintain);
		//userDetailsService.verifyPermission("ScheduleMaintenance.Update");
		return restClient.resource(RESOURCE_PATH)
				.build()
				.put()
//				.header("userId", String.valueOf(100))
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(appMaintain)
				.retrieve()
				.bodyToMono(ApplicationMaintenance.class)
				.map(savedGroup -> ResponseEntity.ok(savedGroup));
	}

	@GetMapping(value = "/validationRules", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getValidationRules() {
		userDetailsService.verifyPermission("ScheduleMaintenance.View");
		return restClient.resource(RESOURCE_PATH, "/validationRules")
				.build().get()
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(String.class).map(response -> ResponseEntity.ok(response));
	}
}
