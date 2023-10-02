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
import com.esq.rbac.web.vo.Scope;
import com.esq.rbac.web.vo.UserIdentity;
import jakarta.ws.rs.QueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping(UserIdentityRest.RESOURCE_PATH)
public class UserIdentityRest {

    public static final String RESOURCE_PATH = "userIdentities";
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

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void set(@RequestBody UserIdentity[] identities) throws Exception {
        log.trace("set; identities={}", Arrays.asList(identities));
        userDetailsService.verifyPermission("User.Update");
        restClient.resource(RESOURCE_PATH)
                .build().put()
                .bodyValue(identities)
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toBodilessEntity()
                .block();
    }


    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody UserIdentity[] identities) throws Exception {
        log.trace("delete; identities={}", Arrays.asList(identities));
        userDetailsService.verifyPermission("User.Update");
        restClient.resource(RESOURCE_PATH)
                .build().delete()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                //.bodyValue(identities)
                .retrieve().toBodilessEntity().block();
    }

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserIdentity[]>> getUserIdentites(@RequestParam("userId") Integer userId) throws Exception {
        log.trace("getUserIdentities; userId={}", userId);
        userDetailsService.verifyPermission("User.View");
        return restClient.resource(RESOURCE_PATH)
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("userId", Integer.toString(userId)).build())
                //.header("userId", userDetailsService.getCurrentUserDetails().getUserInfo().getUserId())
                .header("userId", String.valueOf(100))
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(UserIdentity[].class).map(userIdentities -> ResponseEntity.ok(userIdentities));
    }
}
