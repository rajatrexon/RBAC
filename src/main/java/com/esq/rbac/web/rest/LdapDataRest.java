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
import com.esq.rbac.web.vo.User;
import jakarta.ws.rs.FormParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(LdapDataRest.RESOURCE_PATH)
public class LdapDataRest {
	//private static final Logger log = LoggerFactory.getLogger(ScopeRest.class);
	public static final String RESOURCE_PATH = "ldapData";
	private RestClient restClient;

	@Autowired
	public void setRestClient(RestClient restClient) {
		log.trace("setRestClient");
		this.restClient = restClient;
	}


    @GetMapping(path = "/isUserImportEnabled", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Boolean>> isUserImportEnabled() {
        log.trace("isUserImportEnabled;");
        return restClient.resource(RESOURCE_PATH, "isUserImportEnabled").build().get().accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Boolean.class).map(booleanValue -> ResponseEntity.ok(booleanValue));
    }


    @GetMapping(path = "/isBulkImportEnabled", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Boolean>> isBulkImportEnabled() {
        log.trace("isBulkImportEnabled;");
        return restClient.resource(RESOURCE_PATH, "isBulkImportEnabled").build().get().accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Boolean.class).map(booleanValue -> ResponseEntity.ok(booleanValue));
    }


    @GetMapping(path = "/isLdapEnabled", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Boolean>> isLdapEnabled() {
        log.trace("isLdapEnabled;");
        return restClient.resource(RESOURCE_PATH, "isLdapEnabled").build().get().accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Boolean.class).map(booleanValue -> ResponseEntity.ok(booleanValue));
    }


    @PostMapping(path = "/userDetails", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<User>> getUserDetails(@FormParam("searchParam") String searchParam) throws Exception {
        log.trace("getUserDetails; searchParam={}", searchParam);
        return restClient.resource(RESOURCE_PATH, "userDetails").build().get().uri(uriBuilder -> uriBuilder.queryParam("searchParam", searchParam).build()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(User.class).map(user -> ResponseEntity.ok(user));
    }

    @SuppressWarnings("unchecked")
    @GetMapping(path = "/allUserNames", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<String>>> getAllUserNames() throws Exception {
        log.trace("getAllUserNames;");
        return restClient.resource(RESOURCE_PATH, "allUserNames").build().get().accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(List.class).map(usernames -> ResponseEntity.ok(usernames));
    }

    @SuppressWarnings("unchecked")

    @PostMapping(path = "/testConnection", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<ResponseEntity<Map<String, List<String>>>> testConnection(@FormParam("url") String url, @FormParam("userDn") String userDn, @FormParam("password") String password, @FormParam("base") String base) throws Exception {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("url", url);
        formData.add("userDn", userDn);
        formData.add("password", password);
        formData.add("base", base);
        return restClient.resource(RESOURCE_PATH, "testConnection").build().post().accept(MediaType.APPLICATION_FORM_URLENCODED).bodyValue(formData).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Map.class).map(map -> ResponseEntity.ok(map));
    }


    @GetMapping(path = "/isLdapDetailsSyncEnabled", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Boolean>> isLdapDetailsSyncEnabled() {
        log.trace("isLdapDetailsSyncEnabled;");
        return restClient.resource(RESOURCE_PATH, "isLdapDetailsSyncEnabled").build().get().accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Boolean.class).map(booleanValue -> ResponseEntity.ok(booleanValue));
    }
}
