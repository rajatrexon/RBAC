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
import com.esq.rbac.web.exception.ErrorInfo;
import com.esq.rbac.web.exception.ForbiddenException;
import com.esq.rbac.web.util.DeploymentUtil;
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.MakerChecker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(MakerCheckerRest.RESOURCE_PATH)
public class MakerCheckerRest {

    public static final String RESOURCE_PATH = "makerChecker";
    public static final String ERROR_ENCRYPTION_CODE = "encryptionFailed";

    private RestClient restClient;
    private UserDetailsService userDetailsService;
	private DeploymentUtil deploymentUtil;
   
    
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

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        log.debug("setDeploymentUtil; {}", deploymentUtil);
        this.deploymentUtil = deploymentUtil;
    }

    @GetMapping(value="/{makerCheckerId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> getByMakerCheckerId(@PathParam("makerCheckerId") int makerCheckerId) {
        log.trace("getByMakerCheckerId; makerCheckerId={}", makerCheckerId);
//        userDetailsService.verifyPermission(RBACUtil.CHECKER_VIEW);
//        checkEntityPermission(makerCheckerId, RBACUtil.SCOPE_KEY_MAKERCHECKER_VIEW);
        return restClient.resource(RESOURCE_PATH, Integer.toString(makerCheckerId))
                .build().get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(Object.class).map(user -> ResponseEntity.ok(user));
    }

    @GetMapping(value="/isMakercheckerActivated",produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean isMakercheckerActivated() {
    	log.trace("isMakercheckerActivated(); {}",deploymentUtil.getIsMakercheckerActivated());
        return deploymentUtil.getIsMakercheckerActivated();
    }

/*    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@Context UriInfo uriInfo) {
        log.trace("list; requestUri={}", uriInfo.getRequestUri());
        userDetailsService.verifyPermission(RBACUtil.SCOPE_KEY_MAKERCHECKER_VIEW);
        String scopeQuery = userDetailsService.extractScopeForMakerChecker();
        log.trace("list; scopeQuery={}", scopeQuery);
        uriInfo.getQueryParameters().add(RBACUtil.MAKER_CHECKER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        MakerChecker[] array = restClient.resource(RESOURCE_PATH)
        		.queryParams(uriInfo.getQueryParameters())
                .accept(MediaType.APPLICATION_JSON)
                .get(MakerChecker[].class);
        return Response.ok().entity(array).expires(new Date()).build();
    }
*/

    @PostMapping(value="/count/{entityName}",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Integer>> count(HttpServletRequest httpRequest,@PathParam("entityName") String entityName) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("count; requestUri={}", params);
        userDetailsService.verifyPermission(entityName+".View");
//        String scopeQuery = userDetailsService.extractScopeForMakerChecker();
//        log.trace("count; scope={}", scopeQuery);
//        params.add(RBACUtil.MAKER_CHECKER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        params.add("entityToShow", entityName);
        return restClient.resource(RESOURCE_PATH, "count")
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .header("loggedInTenant", String.valueOf(userDetailsService.getCurrentUserDetailsRBAC().getSelectedTenantList().get(0)))
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Integer.class)
                .map(integer -> ResponseEntity.ok(integer));
    }

    @PostMapping(value="/customMakerCheckerInfo/{entityName})",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getMakerCheckerInfo(HttpServletRequest httpRequest,@PathParam("entityName") String entityName) {
    	 MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
    	 userDetailsService.verifyPermission(entityName+".View");
//    	 String scopeQuery = userDetailsService.extractScopeForMakerChecker();
//         log.trace("getCustomUserInfo; scopeQuery={}", scopeQuery);
//         params.add(RBACUtil.MAKER_CHECKER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
    	 params.add("entityToShow", entityName);
        return restClient.resource(RESOURCE_PATH, "customMakerCheckerInfo")
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .header("loggedInTenant", String.valueOf(userDetailsService.getCurrentUserDetailsRBAC().getSelectedTenantList().get(0)))
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class)
                .map(integer -> ResponseEntity.ok(integer));
    }

        @PostMapping(value="/approveOrRejectMakerCheckerEntity/{entityName})",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public Mono<ResponseEntity<ClientResponse>> approveOrRejectMakerCheckerEntity(@RequestBody MakerChecker makerChecker, HttpServletResponse httpResponse, HttpServletRequest httpRequest) {
		log.info("====================in approveOrRejectMakerCheckerEntity {}", makerChecker);
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

		String entityName = makerChecker.getEntityName();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		if(makerChecker.getIsApproveFlag() != 7)
		userDetailsService.verifyPermission(entityName + ".Approve");
		String clientIp = RBACUtil.getRemoteAddress(httpRequest);
		List<Long> list = userDetailsService.getCurrentUserDetailsRBAC().getSelectedTenantList();
		return restClient.resource(RESOURCE_PATH, "approveOrRejectMakerCheckerEntity")
                .build().post()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .header("clientIp", clientIp).header("loggedInTenant", String.valueOf(list.get(0)))
                .bodyValue(makerChecker)
                .accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(ClientResponse.class)
                .map(clientResponse1 -> ResponseEntity.ok(clientResponse1));
	}

    @GetMapping(value="/getHistoryOfMakerChecker/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getHistoryOfMakerChecker(HttpServletRequest httpRequest, @PathParam("id") String id) {
         return restClient.resource(RESOURCE_PATH, "getHistoryOfMakerChecker",id)
                 .build().get()
                 .header("loggedInTenant", String.valueOf(userDetailsService.getCurrentUserDetailsRBAC().getSelectedTenantList().get(0)))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class).map(s -> ResponseEntity.ok(s));
    }
    
    
//  private void checkEntityPermission(Integer makerCheckerId, String permission) {
//      MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
//      String scopeQuery = userDetailsService.extractScopeForMakerChecker();
//      queryMap.add(RBACUtil.MAKER_CHECKER_SCOPE_QUERY,
//              RBACUtil.encodeForScopeQuery(scopeQuery));
//      queryMap.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, Integer.toString(makerCheckerId));
//      if (Boolean.TRUE.equals(restClient.resource(RESOURCE_PATH, "checkEntityPermission")
//              .queryParams(queryMap)
//              .accept(MediaType.APPLICATION_JSON).get(Boolean.class))) {
//          return;
//      }
//      ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
//      errorInfo.add("permission", permission);
//      errorInfo.add("entity", "MakerChecker");
//      errorInfo.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, makerCheckerId.toString());
//      throw new WebApplicationException(Response
//              .status(Response.Status.FORBIDDEN).entity(errorInfo).build());
//  }

    @PostMapping(value = "/entityPermission", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ClientResponse>> checkPermission(@RequestBody MakerChecker makerChecker,HttpServletResponse httpResponse,
                                    HttpServletRequest httpRequest) {
    	log.info("====================in checkPermission {}",makerChecker);
   	 	userDetailsService.verifyPermission(makerChecker.getEntityName()+"."+makerChecker.getOperation());
   	 	checkEntityPermissionForEntity(makerChecker.getEntityId(), makerChecker.getEntityName()+"."+makerChecker.getOperation(),makerChecker.getEntityName());
   	 	MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
   	 	queryMap.add("entityName", makerChecker.getEntityName());
		queryMap.add("loggedInUserName", userDetailsService.getCurrentUserDetails().getUserInfo().getUserName());
		queryMap.add("entityId",Integer.toString(makerChecker.getEntityId()));
		queryMap.add("makerCheckerId", Long.toString(makerChecker.getId()));

   	 	String resourcePath = "users";
        return  restClient.resource(resourcePath,"MakerCheckerResponse")
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(queryMap).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(ClientResponse.class)
                .map(response -> ResponseEntity.ok(response));
   }

	private void checkEntityPermissionForEntity(Integer entityId, String permission,String entity) {
    	MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
		String scopeQuery = userDetailsService.extractScopeForUserView();
		queryMap.add(RBACUtil.USER_SCOPE_QUERY,RBACUtil.encodeForScopeQuery(scopeQuery));
		queryMap.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, Integer.toString(entityId));
		queryMap.add("entityName", entity);
		queryMap.add("loggedInUserName", userDetailsService.getCurrentUserDetails().getUserInfo().getUserName());
		if (Boolean.TRUE.equals(restClient.resource(RESOURCE_PATH, "checkEntityPermissionForEntity")
                        .build().get()
                        .uri(uriBuilder -> uriBuilder.queryParams(queryMap).build())
                        .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Boolean.class))) {
			return;
		}
		ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
		errorInfo.add("permission", permission);
		errorInfo.add("entity", entity);
		errorInfo.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, entityId.toString());
		throw new ForbiddenException(errorInfo.getErrorCode());
	}
}
