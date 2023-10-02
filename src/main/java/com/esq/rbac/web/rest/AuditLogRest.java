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
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.AuditLog;
import com.esq.rbac.web.vo.AuditLogJson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(AuditLogRest.RESOURCE_PATH)
public class AuditLogRest {

   // private static final Logger log = LoggerFactory.getLogger(AuditLogRest.class);
    public static final String RESOURCE_PATH = "auditlog";
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

    @PostMapping
    public Mono<ResponseEntity<AuditLogJson>> create(@RequestBody AuditLogJson auditLogJson) throws Exception {
        log.trace("create; AuditLogJson={}", auditLogJson);

        return restClient.resource(RESOURCE_PATH) // Replace with the actual endpoint to save a Group
                .build().post()
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(auditLogJson).retrieve()
                .bodyToMono(AuditLogJson.class) .map(savedGroup -> ResponseEntity.ok(savedGroup));
    }

    @PostMapping(value = "/{userId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AuditLogJson[]>>  getAuditLogByUserId(@PathVariable("userId") int userId, HttpServletRequest httpRequest) throws Exception {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("getAuditLogByUserId; userId={}", userId);
        return restClient.resource(RESOURCE_PATH, Integer.toString(userId))
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(AuditLogJson[].class)
                .map(auditLogJsons -> ResponseEntity.ok(auditLogJsons));
    }

    @PostMapping(value = "/historyFeed",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AuditLogJson[]>> getAuditLogHistoryFeed(HttpServletRequest httpRequest) throws Exception {
		MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        System.out.println("getAuditLogHistoryFeed; userId={}"+ userDetailsService.getCurrentUserDetails().getUserInfo().getUserId());

        return restClient.resource(RESOURCE_PATH, "historyFeed", Integer.toString(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .build().get().uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(AuditLogJson[].class)
                .map(auditLog -> ResponseEntity.ok(auditLog));
    }
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AuditLogJson[]>> list(HttpServletRequest request) throws Exception {

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

       // log.trace("list; requestUri={}", uriInfo.getRequestUri());
       return restClient.resource(RESOURCE_PATH)
                .build().get().uri(uriBuilder -> uriBuilder
                        .queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(AuditLogJson[].class)
                .map(auditLogJsons -> ResponseEntity.ok(auditLogJsons));
    }

    @GetMapping("/count")
    public Mono<ResponseEntity<Integer>> count(HttpServletRequest request) throws Exception {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        //log.trace("count; requestUri={}", uriInfo.getRequestUri());
        return restClient.resource(RESOURCE_PATH)
                .build().get().uri(uriBuilder -> uriBuilder
                        .path("/count")
                        .queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Integer.class).map(integer -> ResponseEntity.ok(integer));
    }
}
