package com.esq.rbac.web.rest;

import com.esq.rbac.web.client.RestClient;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.vo.OrganizationAttributeInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;


@RestController
@RequestMapping("/organizationAttributes")
public class OrganizationAttributeRest {

    public static final String RESOURCE_PATH = "organizationAttributes";
    private static final Logger log = LoggerFactory.getLogger(OrganizationAttributeRest.class);
    	private UserDetailsService userDetailsService;
    private RestClient restClient;

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

    @PostMapping(path = "/{orgId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OrganizationAttributeInfo[]>> create(@RequestBody OrganizationAttributeInfo[] organizationAttributeInfoArray, @PathVariable("orgId") Integer orgId, @RequestHeader HttpHeaders headers) throws Exception {

        return restClient.resource(RESOURCE_PATH + orgId).build().post().header("userId", headers.get("userId").get(0))
                	.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON).bodyValue(organizationAttributeInfoArray).retrieve().bodyToMono(OrganizationAttributeInfo[].class).map(organizationAttributeInfos -> ResponseEntity.ok(organizationAttributeInfos));


    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OrganizationAttributeInfo[]>> list(HttpServletRequest servletRequest) {

        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("list; requestUri={}", servletRequest.getRequestURI());
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));


        return restClient.resource(RESOURCE_PATH).build().get().uri(uriBuilder -> uriBuilder.queryParams(uriInfo).build()).retrieve().bodyToMono(OrganizationAttributeInfo[].class).map(organizationAttributeInfos -> ResponseEntity.ok(organizationAttributeInfos));


    }


}
