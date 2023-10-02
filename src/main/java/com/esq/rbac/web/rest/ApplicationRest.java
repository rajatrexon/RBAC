package com.esq.rbac.web.rest;

import com.esq.rbac.web.client.RestClient;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.Application;
import com.esq.rbac.web.vo.ChildApplication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.*;


@Slf4j
@RestController
@RequestMapping(ApplicationRest.RESOURCE_PATH)
public class ApplicationRest {

    protected RestClient restClient;
    protected String applicationName = "";

    public static final String RESOURCE_PATH = "applications";

    private UserDetailsService userDetailsService;

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setRestClient(RestClient restClient) {
        log.debug("setRestClient; {}", restClient);
        this.restClient = restClient;
    }

    public void setApplicationName(String applicationName) {
        log.debug("setApplicationName; {}", applicationName);
        this.applicationName = applicationName;
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Application>> create(@RequestBody Application application) throws Exception {
        log.trace("create; application={}", application);
       userDetailsService.verifyPermission("Application.Create");
        if(application!=null && application.getChildApplications()!=null && !application.getChildApplications().isEmpty()){
            for(ChildApplication childApp: application.getChildApplications()){
                if(childApp.getChangeLicense()!=null && childApp.getChangeLicense().getLicense()!=null
                        && !childApp.getChangeLicense().getLicense().isEmpty()){
                    userDetailsService.verifyPermission("Application.ManageLicense");
                }
            }
        }
        return restClient.resource(RESOURCE_PATH).build()
                 .post()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(application)
                .retrieve()
                .bodyToMono(Application.class)
                .map(savedApp -> ResponseEntity.ok(savedApp));
    }


    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Application>> update(@RequestBody Application application) throws Exception {
        log.trace("update; application={}", application);
       userDetailsService.verifyPermission("Application.Update");
        if(application!=null && application.getChildApplications()!=null && !application.getChildApplications().isEmpty()){
            for(ChildApplication childApp: application.getChildApplications()){
                if(childApp.getChangeLicense()!=null && childApp.getChangeLicense().getLicense()!=null
                        && !childApp.getChangeLicense().getLicense().isEmpty()){
                    userDetailsService.verifyPermission("Application.ManageLicense");
                }
            }
        }
        return restClient.resource(RESOURCE_PATH).build()
                .put()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(application)
                .retrieve()
                .bodyToMono(Application.class)
                .map(savedApplication->ResponseEntity.ok(savedApplication));
    }



    @PutMapping(path="/targetOperations",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Application>> updateTargetOperations(@RequestBody Application application) throws Exception {
        log.trace("updateTargetOperations; application={}", application);
            userDetailsService.verifyPermission("Application.Update");
        return restClient.resource(RESOURCE_PATH,"targetOperations").build()
                .put()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON).bodyValue(application)
                .retrieve()
                .bodyToMono(Application.class)
                .map(application1->ResponseEntity.ok(application1));
    }



    @GetMapping(path = "/{applicationId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Application>> getById(@PathVariable("applicationId") int applicationId) {
        log.trace("getById; applicationId={}", applicationId);
        userDetailsService.verifyPermission("Application.View");
        return restClient.resource(RESOURCE_PATH, Integer.toString(applicationId)).build()
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Application.class)
                .map(application -> ResponseEntity.ok(application));

    }



    @DeleteMapping("/{applicationId}")
    public void deleteById(@PathVariable("applicationId") Integer applicationId ,@RequestHeader HttpHeaders headers) {
        log.trace("deleteById; applicationId={}", applicationId);
         userDetailsService.verifyPermission("Application.Delete");
        restClient.resource(RESOURCE_PATH, Integer.toString(applicationId)).build()
                .delete()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .retrieve()
                .toBodilessEntity() // If you don't expect a response body.
                .subscribe();
    }


//Todo Disscussion
    @PostMapping(path="/customApplicationInfo",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Application[]>> list(HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("list; requestUri={}", params);
        userDetailsService.verifyPermission("Application.View");

        return  restClient.resource(RESOURCE_PATH).build()
                .get()
                .uri(uriBuilder -> uriBuilder.queryParams(params).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Application[].class)
                .map(application -> ResponseEntity.ok(application));

    }



    @PostMapping(path = "/count",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Integer>> count(HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("count; requestUri={}", params);
        userDetailsService.verifyPermission("Application.View");
        return restClient.resource(RESOURCE_PATH, "count").build()
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Integer.class)
                .map(Integer -> ResponseEntity.ok(Integer));

    }



    @GetMapping(path="/validationRules",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getValidationRules() {
     //   userDetailsService.verifyPermission("Application.View");
        return restClient.resource(RESOURCE_PATH, "validationRules").build()
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(str -> ResponseEntity.ok(str));
    }


    @GetMapping(path = "/applicationIdNames",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getApplicationIdNames(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getApplicationIdNames; requestUri={}", servletRequest.getRequestURI());
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

       return   restClient.resource(RESOURCE_PATH, "applicationIdNames").build()
                .get()
                .uri(uriBuilder
                        -> uriBuilder
                        .queryParams(uriInfo)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> ResponseEntity.ok(s));

    }


    @GetMapping(path = "/rbacContextName",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getRBACContextName(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getApplicationIdNames; requestUri={}", servletRequest.getRequestURI());
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        return restClient.resource(RESOURCE_PATH, "rbacContextName")
                .build().get()
                .uri(uriBuilder-> uriBuilder
                        .queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(String.class).map(string -> ResponseEntity.ok(string));

    }


   @GetMapping(path = "/rolesInApplicationsData",produces =MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getRolesInApplicationsData(HttpServletRequest servletRequest) {
        userDetailsService.verifyPermission("Application.View");
        userDetailsService.verifyPermission("Role.View");
        Map<String, String> scopeMap = new LinkedHashMap<String, String>();
        scopeMap.put(RBACUtil.SCOPE_KEY_ROLE_VIEW, userDetailsService.extractScopeForRoleView());
       //Todo scopeMap.put("Application.View", userDetailsService.getCurrentUserDetails().getUserInfo().getScopes().get("Application.View"));
        log.trace("getRolesInApplicationsData; scopeMap={}", scopeMap);

        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getApplicationIdNames; requestUri={}", servletRequest.getRequestURI());
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
       return restClient.resource(RESOURCE_PATH, "rolesInApplicationsData").build()
                .post().uri(uriBuilder-> uriBuilder.queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(scopeMap).retrieve().bodyToMono(String.class)
                .map(data->ResponseEntity.ok(data));
    }


    @GetMapping(path = "/getChildApplicationInfo",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getChildApplicationInfo(HttpServletRequest servletRequest){

        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getApplicationIdNames; requestUri={}", servletRequest.getRequestURI());
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

       return restClient.resource(RESOURCE_PATH, "getChildApplicationInfo").build()
               .get()
               .uri(uriBuilder-> uriBuilder.queryParams(uriInfo).build())
               .accept(MediaType.APPLICATION_JSON)
               .retrieve().bodyToMono(String.class)
               .map(string -> ResponseEntity.ok(string));

    }


    @GetMapping(path = "/applicationIdNamesByLoggedInUser",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getApplicationIdNamesByLoggedInUserName(HttpServletRequest servletRequest) {

        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getApplicationIdNames; requestUri={}", servletRequest.getRequestURI());
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        return restClient.resource(RESOURCE_PATH, "applicationIdNamesByLoggedInUser").build()
                .get().uri(uriBuilder-> uriBuilder.queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(string -> ResponseEntity.ok(string));
    }
}
