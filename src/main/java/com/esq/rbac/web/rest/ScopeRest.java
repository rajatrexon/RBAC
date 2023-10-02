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
import com.esq.rbac.web.vo.Scope;
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
@RequestMapping(ScopeRest.RESOURCE_PATH)
public class ScopeRest {

    public static final String RESOURCE_PATH = "scopes";
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
    public Mono<ResponseEntity<Scope>> create(@RequestBody Scope scope) throws Exception {
        log.trace("create; scope={}", scope);
        userDetailsService.verifyPermission("Scope.Create");
        return restClient.resource(RESOURCE_PATH)
                .build().post()
                .bodyValue(scope)
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Scope.class)
                .map(data -> ResponseEntity.ok(data));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Scope>> update(@RequestBody Scope scope) throws Exception {
        log.trace("update; scope={}", scope);
        userDetailsService.verifyPermission("Scope.Update");
        return restClient.resource(RESOURCE_PATH)
                .build().put()
                .bodyValue(scope)
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Scope.class)
                .map(data -> ResponseEntity.ok(data));
    }

    @GetMapping(value = "/{scopeId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Scope>>  getById(@PathVariable("scopeId") Integer scopeId) {
        log.trace("getById; scopeId={}", scopeId);
        userDetailsService.verifyPermission("Scope.View");
        return restClient.resource(RESOURCE_PATH, Integer.toString(scopeId))
                .build().get()
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(Scope.class).map(data->ResponseEntity.ok(data));
    }

    @DeleteMapping("/{scopeId}")
    public void deleteById(@RequestParam MultiValueMap<String, String> uriInfo, @PathParam("scopeId") int scopeId) {
        log.trace("deleteById; scopeId={}", scopeId);
        userDetailsService.verifyPermission("Scope.Delete");
        restClient.resource(RESOURCE_PATH, Integer.toString(scopeId))
                .build().delete()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(uriInfo).build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .retrieve().toEntity(Scope.class).subscribe();
//         .retrieve().toBodilessEntity().subscribe();
    }

    @PostMapping(value = "/customScopeInfo",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Scope[]>> list(HttpServletRequest httpRequest) {
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
        log.trace("list; requestUri={}", params);
        userDetailsService.verifyPermission("Scope.View");
        return restClient.resource(RESOURCE_PATH)
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Scope[].class)
                .map(scopes -> ResponseEntity.ok(scopes));
    }

    @GetMapping(value = "/global",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Scope[]>> globalList(@RequestParam MultiValueMap uriInfo) {
        userDetailsService.verifyPermission("Scope.View");
        return restClient.resource(RESOURCE_PATH, "global")
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Scope[].class)
                .map(scopes -> ResponseEntity.ok(scopes));
    }

    @PostMapping(value = "/count",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Integer>> count(HttpServletRequest httpRequest) {
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
        log.trace("count; requestUri={}", params);
        userDetailsService.verifyPermission("Scope.View");
        return restClient.resource(RESOURCE_PATH, "count")
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Integer.class)
                .map(scopes -> ResponseEntity.ok(scopes));
    }

    @GetMapping(value = "/validationRules",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getValidationRules() {
        userDetailsService.verifyPermission("Scope.View");
        return restClient.resource(RESOURCE_PATH, "validationRules")
                .build().get()
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(String.class).map(data->ResponseEntity.ok(data));
    }
}
