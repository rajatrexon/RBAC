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
import com.esq.rbac.web.util.DeploymentUtil;
import com.esq.rbac.web.vo.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ConfigurationRest.RESOURCE_PATH)
public class ConfigurationRest {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationRest.class);
    public static final String RESOURCE_PATH = "configuration";
    private RestClient restClient;
    private UserDetailsService userDetailsService;
    private DeploymentUtil deploymentUtil;

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        log.debug("setDeploymentUtil; {}", deploymentUtil);
        this.deploymentUtil = deploymentUtil;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        log.debug("setUserDetailsService; {}", userDetailsService);
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setRestClient(RestClient restClient) {
        log.debug("setRestClient");
        this.restClient = restClient;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Configuration[]>> getConfiguration() {
        userDetailsService.verifyPermission("Configuration.View");
        return restClient.resource(RESOURCE_PATH).build().get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Configuration[].class)
                .map(configurations -> ResponseEntity.ok(configurations));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Configuration[]>> updateConfiguration(@RequestHeader HttpHeaders headers,@RequestBody Configuration[] configuration){
        userDetailsService.verifyPermission("Configuration.Update");
        return restClient.resource(RESOURCE_PATH)
                .build().put()
                .header("userId",String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(configuration)
                .retrieve().bodyToMono(Configuration[].class)
                .map(configurations -> ResponseEntity.ok(configurations));

    }

    @GetMapping(value = "/ivrConfiguration", produces = MediaType.APPLICATION_JSON_VALUE)
    public  Mono<ResponseEntity<Configuration[]>> getIVRConfiguration() {
        userDetailsService.verifyPermission("User.View");
        return restClient.resource(RESOURCE_PATH).build().get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Configuration[].class)
                .map(configurations -> ResponseEntity.ok(configurations));
    }

    @GetMapping(value = "/passwordPolicyVisibility", produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean getPasswordPolicyVisibility() {
    	log.trace("passwordPolicyVisibility; {}",deploymentUtil.getPasswordPolicyVisibility());
    	 return deploymentUtil.getPasswordPolicyVisibility();
    }
}
