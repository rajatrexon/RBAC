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
import com.esq.rbac.web.client.UserDetailsRBAC;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.exception.ErrorInfo;
import com.esq.rbac.web.util.DeploymentUtil;
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@RequestMapping("currentUser")
public class CurrentUserRest {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserRest.class);
    private UserDetailsService userDetailsService;
    private RestClient restClient;
    private DeploymentUtil deploymentUtil;

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        log.debug("setUserDetailService; {}", userDetailsService);
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setRestClient(RestClient restClient) {
        log.debug("setRestClient; {}", restClient);
        this.restClient = restClient;
    }

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        log.debug("setDeploymentUtil; {}", deploymentUtil);
        this.deploymentUtil = deploymentUtil;
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CurrentUser> getCurrentUser(HttpServletRequest httpRequest) {
        if (userDetailsService == null) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.SERVER_ERROR);
            errorInfo.setExceptionMessage("userDetailsService=null");
            log.error("getCurrentUser; {}", errorInfo);
            throw new WebApplicationException(Response
                    .serverError()
                    .entity(errorInfo)
                    .build());
        }

        UserDetailsRBAC currentUserDetails = userDetailsService.getCurrentUserDetailsRBAC();
        if (currentUserDetails == null) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.SERVER_ERROR);
            errorInfo.setExceptionMessage("currentUserDetails=null");
            log.error("getCurrentUser; {}", errorInfo);
            throw new WebApplicationException(Response
                    .serverError()
                    .entity(errorInfo)
                    .build());
        }
        CurrentUser currentUser = null;
        UserInfo userInfo = currentUserDetails.getUserInfo();

        if (userInfo != null) {
            //added to identify logged in user in destroySession call on window close
            httpRequest.getSession().setAttribute("userName", userInfo.getUserName());
            currentUser = new CurrentUser();
            currentUser.setApplicationName(userInfo.getApplicationName());
            currentUser.setDisplayName(userInfo.getDisplayName());
            currentUser.setFirstName(userInfo.getFirstName());
            currentUser.setScopes(userInfo.getScopes());
            currentUser.setUserId(userInfo.getUserId());
            currentUser.setUserName(userInfo.getUserName());
            currentUser.setLastSuccessfulLoginTime(currentUserDetails.getLastSuccessfulLoginTime());
            currentUser.setGroupId(currentUserDetails.getGroupId());
            currentUser.setGroupName(currentUserDetails.getGroup());
            currentUser.setTenantLogoUrl(currentUserDetails.getTenantLogoPath());
            currentUser.setSelectedTenantList(currentUserDetails.getSelectedTenantList());
            currentUser.setTenantId(currentUserDetails.getTenantId());
            currentUser.setMakerCheckerEnabledInTenant(currentUserDetails.isMakerCheckerEnabledInTenant()); /* Added By Fazia 19-Dec-2018 */
            if (currentUserDetails.getTenantId() != null) {
                if (RBACUtil.HOST_TENANT_TYPE_CODE_VALUE.equalsIgnoreCase(currentUserDetails.getTenantType())) {
                    currentUser.setHostLoggedIn(true);
                }
            }
            Set<String> userPermissions = new TreeSet<String>();
            for (String target : userInfo.getPermissions().keySet()) {
                for (String operation : userInfo.getPermissions().get(target)) {
                    userPermissions.add(target + "." + operation);
                }
            }
            currentUser.setPermissions(userPermissions);
            currentUser.setSystemMultiTenant(currentUserDetails.isSystemMultiTenant());
            Mono<UserInfo> newUserInfo = userDetailsService.getUserInfo(userInfo.getUserName(), userInfo.getApplicationName(), httpRequest.getSession().getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE).toString());
            if (newUserInfo != null && newUserInfo.block().getApplicationContextName() != null) {
                currentUser.setApplicationContextName(newUserInfo.block().getApplicationContextName());
            }

            Mono<UserInfoGenericV3> userGenericDet = userDetailsService.getUserInfoGenericV3(currentUser.getUserName(),
                    currentUser.getApplicationName(), currentUser.getApplicationContextName());
            if (userGenericDet != null) {
                currentUser.setPreferredLanguage(userGenericDet.block().getPreferredLanguage());
                currentUser.setTimezone(userGenericDet.block().getTimezone());
                currentUser.setDateTimeDisplayFormat(userGenericDet.block().getDateTimeDisplayFormat());
                currentUser.setLastSuccessfulLoginTimeDisplay(userGenericDet.block().getLastSuccessfulLoginTimeDisplay());
            }
        }

        return Mono.just(currentUser);
    }

    @GetMapping(value = "/apps", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ApplicationInfo[]> getUserAuthorizedApps(HttpServletRequest httpRequest) {
        if (userDetailsService == null) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.SERVER_ERROR);
            errorInfo.setExceptionMessage("userDetailsService=null");
            log.error("getUserAuthorizedApps; {}", errorInfo);
            throw new WebApplicationException(Response
                    .serverError()
                    .entity(errorInfo)
                    .build());
        }
        String casTicket = null;
        if (httpRequest.getSession().getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE) != null) {
            casTicket = httpRequest.getSession().getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE).toString();
        }
        return userDetailsService.getUserAuthorizedApps(casTicket);
    }

    @GetMapping(value = "/appsDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ApplicationInfoDetails[]> getUserAuthorizedAppsDetails(
            HttpServletRequest httpRequest) {
        if (userDetailsService == null) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.SERVER_ERROR);
            errorInfo.setExceptionMessage("userDetailsService=null");
            log.error("getUserAuthorizedAppsDetails; {}", errorInfo);
            throw new WebApplicationException(Response.serverError()
                    .entity(errorInfo).build());
        }
        String casTicket = null;
        if (httpRequest.getSession().getAttribute(
                RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE) != null) {
            casTicket = httpRequest.getSession()
                    .getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE)
                    .toString();
        }
        return userDetailsService.getUserAuthorizedAppsDetails(casTicket);
    }

    @SuppressWarnings({"rawtypes"})
    @GetMapping(value = "/appsDetailsInHtml", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<Map>> getUserAuthorizedAppsDetailsInHtml(HttpServletRequest httpRequest, @RequestParam("appKey") String appKey) {
        if (userDetailsService == null) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.SERVER_ERROR);
            errorInfo.setExceptionMessage("userDetailsService=null");
            log.error("getUserAuthorizedAppsDetailsInHtml; {}", errorInfo);
            throw new WebApplicationException(Response
                    .serverError()
                    .entity(errorInfo)
                    .build());
        }
        String casTicket = null;
        if (httpRequest.getSession().getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE) != null) {
            casTicket = httpRequest.getSession().getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE).toString();
        }
        return userDetailsService.getUserAuthorizedAppsDetailsInHtml(casTicket, appKey);
    }

    @GetMapping(value = "/appsDetailsInJson", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getUserAuthorizedAppsDetailsInJson(HttpServletRequest httpRequest, @RequestParam("appKey") String appKey) {
        if (userDetailsService == null) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.SERVER_ERROR);
            errorInfo.setExceptionMessage("userDetailsService=null");
            log.error("getUserAuthorizedAppsDetailsInJson; {}", errorInfo);
            throw new WebApplicationException(Response
                    .serverError()
                    .entity(errorInfo)
                    .build());
        }
        String casTicket = null;
        if (httpRequest.getSession().getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE) != null) {
            casTicket = httpRequest.getSession().getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE).toString();
        }
        return userDetailsService.getUserAuthorizedAppsDetailsInJson(casTicket, appKey);
    }

    @GetMapping(value = "/userSwitchHash")
    public String getUserSwitchHash(HttpServletRequest httpRequest) {
        return (String) httpRequest.getSession(true).getAttribute("userSwitchHash");
    }

    @GetMapping(value = "/changePassword")
    public String changePassword(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {

        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        String redirectUrl = deploymentUtil.getChangePasswordUrl();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(redirectUrl);
        uriComponentsBuilder.queryParam("userName", userDetailsService.getCurrentUserDetails().getUsername());
        uriComponentsBuilder.queryParam("returnUrl", uriInfo.getFirst("returnUrl"));
        uriComponentsBuilder.queryParam("serviceUrl", uriInfo.getFirst("serviceUrl"));
        if (restClient.getAppKeyHeader() != null && !restClient.getAppKeyHeader().isEmpty() && !restClient.getAppKeyHeader().equals("")) {
            uriComponentsBuilder.queryParam(RBACUtil.APP_KEY_IDENTIFIER_PARAM, restClient.getAppKeyHeader());
        }
        httpResponse.sendRedirect(uriComponentsBuilder.build().toString());
        return null;
    }

    @GetMapping(value = "/sessionAlive", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> isSessionAlive() {
        return ResponseEntity.ok().body(null).status(HttpStatus.OK).build();

    }

    /* @Produces(MediaType.APPLICATION_JSON)*/
    @PostMapping(value = "/setSelectedTenants")
    public ResponseEntity setSelectedTenants(HttpServletRequest httpRequest, @RequestBody long[] selectedTenantList) {
        if (selectedTenantList != null) {
            try {
                restClient.resource("tenants", "validateTenantIds")
                        .build().post()
                        .header("userId", String.valueOf(100))
                        // .header("userId", userDetailsService.getCurrentUserDetails().getUserInfo().getUserId())
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(selectedTenantList)
                        .retrieve().toBodilessEntity();
            } catch (Exception e) {
                log.error("setSelectedTenants; Exception={};", e);
                ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.SERVER_ERROR);
                errorInfo.setExceptionMessage(e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorInfo);
            }
            userDetailsService.setSelectedTenants(Arrays.asList(ArrayUtils.toObject(selectedTenantList)));
        }
        return ResponseEntity.ok().body(null).status(HttpStatus.OK).build();
    }

    @GetMapping(value = "/getSessionTimeout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getSessionInactivityTimeout() {
        Integer sessionInactivityTimeoutInMinutes = deploymentUtil.getSessionInactivityTimeoutSeconds() / 60;
        return ResponseEntity.ok().body(sessionInactivityTimeoutInMinutes).status(HttpStatus.OK).build();
    }

    @GetMapping(value = "/csrfTokenSessionAlive", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity csrfTokenSessionAlive() {
        return ResponseEntity.ok().body(null).status(HttpStatus.OK).build();

    }
}
