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
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.Calendar;
import com.esq.rbac.web.vo.ScheduleRuleDefault;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(CalendarRest.RESOURCE_PATH)
public class CalendarRest {
    public static final String RESOURCE_PATH = "calendars";
    private static final Logger log = LoggerFactory.getLogger(CalendarRest.class);
    private RestClient restClient;
	private UserDetailsService userDetailsService;

    @Autowired
    public void setRestClient(RestClient restClient) {
        log.trace("setRestClient");
        this.restClient = restClient;
    }

	@Autowired
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		log.debug("setUserDetailsService; {}", userDetailsService);
		this.userDetailsService = userDetailsService;
	}


    @GetMapping(path = "/validationRules", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getValidationRules() {
        	userDetailsService.verifyPermission("Calendar.View");
        return restClient.resource(RESOURCE_PATH, "validationRules").build().get().accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class).map(string -> ResponseEntity.ok(string));
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Calendar>> create(@RequestBody Calendar calendar, HttpServletRequest request, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("create; calendar={}", calendar);

        userDetailsService.verifyPermission("Calendar.Create");
        return restClient.resource(RESOURCE_PATH).build().post()
			.header("userId",String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .header("clientIp", RBACUtil.getRemoteAddress(request))
                .bodyValue(Calendar.class).accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Calendar.class).map(calendar1 -> ResponseEntity.ok(calendar1));
    }


    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Calendar>> update(@RequestBody Calendar calendar, HttpServletRequest request, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("update; calendar={}", calendar);

        userDetailsService.verifyPermission("Calendar.Update");
        return restClient.resource(RESOURCE_PATH).build().put()
				.header("userId",
                        String.valueOf(userDetailsService.getCurrentUserDetails()
								.getUserInfo().getUserId()))
                .header("clientIp", RBACUtil.getRemoteAddress(request))
                .accept(MediaType.APPLICATION_JSON).bodyValue(Calendar.class)
                .retrieve().bodyToMono(Calendar.class).map(calendar1 -> ResponseEntity.ok(calendar1));
    }


    @PostMapping(path = "/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Calendar>> assign(@RequestBody Calendar calendar, HttpServletRequest servletRequest, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("assign; calendar={}", calendar);
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("assign; requestUri={}", servletRequest.getRequestURI());
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        String organizationId = uriInfo.getFirst("organizationId");
        userDetailsService.verifyPermission("Calendar.Create");
        return restClient.resource(RESOURCE_PATH + "/assign").build().post().uri(uriBuilder -> uriBuilder.queryParam("organizationId", organizationId).build())
				.header("userId",
						String.valueOf(userDetailsService.getCurrentUserDetails()
								.getUserInfo().getUserId()))
                .header("clientIp", RBACUtil.getRemoteAddress(servletRequest))
                .accept(MediaType.APPLICATION_JSON).bodyValue(Calendar.class)
                .retrieve().bodyToMono(Calendar.class).map(calendar1 -> ResponseEntity.ok(calendar1));
    }


    @PostMapping(path = "/unassign", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Calendar>> unassign(@RequestBody Calendar calendar, HttpServletRequest servletRequest, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("unassign; calendar={}", calendar);
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("unassign; requestUri={}", servletRequest.getRequestURI());
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        String organizationId = uriInfo.getFirst("organizationId");

        userDetailsService.verifyPermission("Calendar.Create");
        return restClient.resource(RESOURCE_PATH + "/unassign").build().post().uri(uriBuilder -> uriBuilder.queryParam("organizationId", organizationId).build())
                .header("userId",
                        String.valueOf(userDetailsService.getCurrentUserDetails()
                                .getUserInfo().getUserId()))
                .header("clientIp", RBACUtil.getRemoteAddress(servletRequest))
                .accept(MediaType.APPLICATION_JSON).bodyValue(Calendar.class)
                .retrieve().bodyToMono(Calendar.class).map(calendar1 -> ResponseEntity.ok(calendar1));
    }

    @PostMapping(path = "/customCalendarInfo", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Calendar[]>> list(HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("list; requestUri={}", params);

        userDetailsService.verifyPermission("Calendar.View");
        return restClient.resource(RESOURCE_PATH).build().get()
                .uri(uriBuilder -> uriBuilder.queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Calendar[].class)
                .map(calendars -> ResponseEntity.ok(calendars));
    }


    @PostMapping(path = "/getDataByAssignedStatus", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Calendar>> getDataByAssignedStatus(HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("getDataByAssignedStatus ; requestUri={}", params);

        userDetailsService.verifyPermission("Calendar.View");
        return restClient.resource(RESOURCE_PATH + "/getDataByAssignedStatus").build().get()
                .uri(uriBuilder -> uriBuilder.queryParams(params).build()).accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Calendar.class).map(calendar -> ResponseEntity.ok(calendar));
    }


    @PostMapping(path = "/count", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Long>> count(HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        userDetailsService.verifyPermission("Calendar.View");
        log.trace("count; requestUri={}", params);
        return restClient.resource(RESOURCE_PATH, "count").build().get()
                .uri(uriBuilder -> uriBuilder.queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Long.class)
                .map(count -> ResponseEntity.ok(count));
    }


    @PostMapping(path = "/getCountByAssignedStatus", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Long>> getCountByAssignedStatus(HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        userDetailsService.verifyPermission("Calendar.View");
        log.trace("getCountByAssignedStatus ; requestUri={}", params);
        return restClient.resource(RESOURCE_PATH, "getCountByAssignedStatus").build().get()
                .uri(uriBuilder -> uriBuilder.queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Long.class)
                .map(count -> ResponseEntity.ok(count));
    }

    @GetMapping(path = "/{calendarId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Calendar>> getById(@PathVariable("calendarId") Long calendarId) {
        log.trace("getById; calendarId={};", calendarId);

        	userDetailsService.verifyPermission("Calendar.View");
        return restClient.resource(RESOURCE_PATH, Long.toString(calendarId)).build().get()
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Calendar.class)
                .map(calendar -> ResponseEntity.ok(calendar));
    }


    @PostMapping(path = "/getCalendarWithIsDefault", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Calendar>> getCalendarWithIsDefault(@FormParam(value = "calendarId") long calendarId, @FormParam(value = "organizationId") long organizationId) {
        log.trace("getCalendarWithIsDefault; calendarId={},organizationId={};", calendarId, organizationId);

        	userDetailsService.verifyPermission("Calendar.View");
        return restClient.resource(RESOURCE_PATH + "/getCalendarWithIsDefault").build().get()
                .uri(uriBuilder -> uriBuilder.queryParam("calendarId", Long.toString(calendarId))
                        .queryParam("organizationId", Long.toString(organizationId)).build())
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Calendar.class).map(calendar -> ResponseEntity.ok(calendar));
    }


    @DeleteMapping("/{calendarId}")
    public void deleteById(@PathVariable("calendarId") Long calendarId, @RequestHeader HttpHeaders headers, HttpServletRequest request) {
        log.trace("deleteById; calendarId={};", calendarId);

        userDetailsService.verifyPermission("Calendar.Delete");
        restClient.resource(RESOURCE_PATH, Long.toString(calendarId))
                .build()
                .delete()
                .header("userId",String.valueOf(userDetailsService.getCurrentUserDetails()
                                .getUserInfo().getUserId()))
                .header("clientIp", headers.get("clientIp").get(0))
                .retrieve().toBodilessEntity().subscribe();
    }


    @GetMapping(path = "/defaults", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ScheduleRuleDefault[]>> listDefaultScheduleRules(HttpServletRequest servletRequest) {

        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("listDefaultScheduleRules; requestUri={}", servletRequest.getRequestURI());
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        userDetailsService.verifyPermission("Calendar.View");
        return restClient.resource(RESOURCE_PATH, "defaults").build().get()
                .uri(uriBuilder -> uriBuilder.queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ScheduleRuleDefault[].class)
                .map(scheduleRuleDefaults -> ResponseEntity.ok(scheduleRuleDefaults));
    }


    @PostMapping(path = "/defaultWorkCalendarByOrganization", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Calendar>> getDefaultWorkCalendarByOrganization(HttpServletRequest request, @RequestHeader HttpHeaders headers) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(request);
        log.trace("getDefaultWorkCalendarByOrganization; requestUri={}", params);
        return restClient.resource(RESOURCE_PATH, "defaultWorkCalendarByOrganization")
                .build().get().uri(uriBuilder -> uriBuilder.queryParams(params).build())
                .header("userId",String.valueOf(userDetailsService
                        .getCurrentUserDetails() .getUserInfo().getUserId()))
                .header("clientIp",headers.get("clientIp").get(0))
                .retrieve().bodyToMono(Calendar.class).map(calendar -> ResponseEntity.ok(calendar));
    }


    @GetMapping(path = "/defaultHolidayCalendarsByOrganization", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<Calendar>>> getDefaultHolidayCalendarsByOrganization(HttpServletRequest servletRequest, @RequestHeader HttpHeaders headers) {

        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getDefaultHolidayCalendarsByOrganization; requestUri={}", servletRequest.getRequestURI());
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        return restClient.resource(RESOURCE_PATH, "defaultHolidayCalendarsByOrganization")
                .build().get().uri(uriBuilder -> uriBuilder.queryParams(uriInfo).build())
                .header("userId",String.valueOf(userDetailsService
                        .getCurrentUserDetails() .getUserInfo().getUserId()))
                .header("clientIp", RBACUtil.getRemoteAddress(servletRequest))
                .retrieve().bodyToMono(new ParameterizedTypeReference<List<Calendar>>() {
                }).map(defaultHolidayCalendarsByOrganization -> ResponseEntity.ok(defaultHolidayCalendarsByOrganization));
    }
}
