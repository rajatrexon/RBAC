package com.esq.rbac.web.rest;

import com.esq.rbac.web.client.RestClient;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.Code;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Date;


@RestController
@RequestMapping(CodeRest.RESOURCE_PATH)
public class CodeRest {

    private static final Logger log = LoggerFactory.getLogger(CodeRest.class);
    public static final String RESOURCE_PATH = "codes";

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

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Code[]>> list(HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("list; requestUri={}", params);

         userDetailsService.verifyPermission("User.View");
        return restClient
                        .resource(RESOURCE_PATH).build()
                        .get().uri(uriBuilder -> uriBuilder.queryParams(params).build())
                        .header("userId",String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(Code[].class)
                        .map(codes -> ResponseEntity.ok(codes));
    }

    @PostMapping(path = "/application",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Code[]>> getCodesByApplication(HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("getCodesByApplication; requestUri={}", params);

         userDetailsService.verifyPermission("User.View");
        return restClient.resource(RESOURCE_PATH, "application").build()
                .get().uri(uriBuilder -> uriBuilder.queryParams(params).build())
                .header("userId",String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                        .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Code[].class)
                .map(codes -> ResponseEntity.ok(codes));
    }
}
