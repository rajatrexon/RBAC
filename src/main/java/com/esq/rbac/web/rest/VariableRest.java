package com.esq.rbac.web.rest;

import com.esq.rbac.web.client.RestClient;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.VariableInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(VariableRest.RESOURCE_PATH)
public class VariableRest {

    public static final String RESOURCE_PATH = "variables";
    private static final Logger log = LoggerFactory.getLogger(VariableRest.class);
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

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VariableInfo[]>> list(HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        return restClient.resource(RESOURCE_PATH).build().get().uri(uriBuilder -> uriBuilder
                .queryParams(params).build()).accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(VariableInfo[].class).map(variableInfos -> ResponseEntity.ok(variableInfos));
    }

    @PostMapping(path = "/userVariables/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> createUserVariables(@RequestBody VariableInfo[] variableInfoArray, @PathVariable("userId") Integer userId) throws Exception {
         restClient.resource(RESOURCE_PATH, "userVariables", Integer.toString(userId)).build().post()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails()
                        .getUserInfo().getUserId())).accept(MediaType.APPLICATION_JSON).bodyValue(variableInfoArray)
                 .retrieve()
                 .toBodilessEntity() // If you don't expect a response body.
                 .subscribe();
         return Mono.just(ResponseEntity.ok().build());
    }

    @PostMapping(path = "/groupVariables/{groupId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> createGroupVariables(@RequestBody VariableInfo[] variableInfoArray, @PathVariable("groupId") Integer groupId) throws Exception {
         restClient.resource(RESOURCE_PATH, "groupVariables", Integer.toString(groupId)).build().post()
                 .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails()
                         .getUserInfo().getUserId())) .accept(MediaType.APPLICATION_JSON)
                 .bodyValue(variableInfoArray)
                 .retrieve()
                 .toBodilessEntity() // If you don't expect a response body.
                 .subscribe();
         return Mono.just(ResponseEntity.ok().build());
    }
}
