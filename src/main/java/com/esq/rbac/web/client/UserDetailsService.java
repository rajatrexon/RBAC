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
package com.esq.rbac.web.client;

import com.esq.rbac.web.exception.ErrorInfo;
import com.esq.rbac.web.exception.ForbiddenException;
import com.esq.rbac.web.security.util.PermissionMaps;
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.vo.*;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;


@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsService.class);
    protected RestClient restClient;
    protected String applicationName = "";


    @Autowired
    public void setRestClient(RestClient restClient) {
        log.debug("setRestClient; {}", restClient);
        this.restClient = restClient;
    }

    public void setApplicationName(String applicationName) {
        log.debug("setApplicationName; {}", applicationName);
        this.applicationName = applicationName;
    }

    @SuppressWarnings("deprecation")

    @Override
    public UserDetailsRBAC loadUserByUsername(String userName) throws UsernameNotFoundException {
        log.trace("loadUserByUsername; userName={}", userName);
        try {
            Mono<UserInfoGenericV3> userInfoGenericV3 = restClient.resource("userInfo/v3/detailsWithAttributes")
                    .build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", userName)
                            .queryParam("applicationName", applicationName).build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(UserInfoGenericV3.class);
            Mono<List<Tenant>> tenantList = restClient.resource("tenants").build().get()
                    .uri(uriBuilder -> uriBuilder.queryParam("userName", userName)
                            .queryParam("applicationName", applicationName).build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(new ParameterizedTypeReference<List<Tenant>>() {
            });

            //      Mono<Tuple2<UserInfoGenericV3, List<Tenant>>> combined = Mono.zip(userInfoGenericV3, tenantList);

            return Mono.zip(userInfoGenericV3, tenantList).flatMap(tuple -> {
                UserInfoGenericV3 userInfoGenericV31 = tuple.getT1();
                List<Tenant> tenantList1 = tuple.getT2();
                // Create your UserDetailsRBAC here using userInfoGenericV31 and tenantList1
                UserDetailsRBAC userDetailsRBAC = new UserDetailsRBAC(userInfoGenericV31, tenantList1);
                return Mono.just(userDetailsRBAC);
            }).block();

        } catch (RuntimeException e) {
            log.error("loadUserByUsername; userInfo/v3/detailsWithAttributes; username={}; exception={}", userName, e);
            //added to support scenario where rbac-lib-client is on higher version than actual RBAC deployment
            if (RBACUtil.RBAC_UAM_APPLICATION_NAME.equalsIgnoreCase(applicationName)) {
                log.error("Request failed for userInfo/v3/detailsWithAttributes, won't try with any deprecated API for RBAC UAM application, update RBAC or check the configuration");
            } else {
                log.error("Request failed for userInfo/v3/detailsWithAttributes, trying with deprecated userInfo/rbacUserInfo, update RBAC or check the configuration");
                try {
                    Mono<UserInfoRBAC> userInfoRBAC = restClient.resource("userInfo/rbacUserInfo").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", userName).queryParam("applicationName", applicationName).build()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(UserInfoRBAC.class);

                    return new UserDetailsRBAC(userInfoRBAC.block());
                } catch (RuntimeException e1) {
                    log.error("loadUserByUsername; userInfo/rbacUserInfo; username={}; exception={}", userName, e1);
                    //added to support scenario where rbac-lib-client is on higher version than actual RBAC deployment
                    log.error("Request failed for userInfo/rbacUserInfo, trying with deprecated userInfo/details, update RBAC or check the configuration");
                    try {
                        Mono<UserInfoDetails> userInfoDetails = restClient.resource("userInfo/details").build().get()
                                .uri(uriBuilder -> uriBuilder.queryParam("userName", userName).build())
                                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(UserInfoDetails.class);

                        return new UserDetailsRBAC(userInfoDetails.block());
                    } catch (RuntimeException e2) {
                        log.error("loadUserByUsername; userInfo/details; username={}; exception={}", userName, e2);
                    }
                }
            }
        }
        throw new UsernameNotFoundException(userName);
    }

    public UserDetails getCurrentUserDetails() {
        org.springframework.security.core.context.SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null) {
            log.error("getCurrentUserDetails; SecurityContextHolder.getContext() returned null");
            return null;
        }
        Authentication auth = securityContext.getAuthentication();
        if (auth == null) {
            log.error("getCurrentUserDetails; Null Authentication in security context");
            return null;
        }
        if (!auth.isAuthenticated()) {
            log.error("getCurrentUserDetails; Authentication.isAuthenticated=false");
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal == null) {
            log.error("getCurrentUserDetails; principal={}", principal);
            return null;
        }
//        if (!(principal instanceof UserDetails)) {
//            log.error("getCurrentUserDetails; principal={}", principal);
//            return null;
//        }

        return loadUserByUsername(auth.getName());
    }

    public UserDetailsRBAC getCurrentUserDetailsRBAC() {
        org.springframework.security.core.context.SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null) {
            log.error("getCurrentUserDetailsRBAC; SecurityContextHolder.getContext() returned null");
            return null;
        }
        Authentication auth = securityContext.getAuthentication();
        if (auth == null) {
            log.error("getCurrentUserDetailsRBAC; Null Authentication in security context");
            return null;
        }
        if (!auth.isAuthenticated()) {
            log.error("getCurrentUserDetailsRBAC; Authentication.isAuthenticated=false");
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal == null) {
            log.error("getCurrentUserDetailsRBAC; principal={}", principal);
            return null;
        }
//        if (!(principal instanceof UserDetailsRBAC)) {
//            log.error("getCurrentUserDetailsRBAC; principal={}", principal);
//            return null;
//        }

        return loadUserByUsername(auth.getName());
    }


    public void verifyPermission(String targetOperation) {
        UserDetails userDetails = getCurrentUserDetails();
        String userName = "<unknown>";
        Set<String> userPermissions = PermissionMaps.getListOfPermissions();
        // Manual Created Map APIs testing purposes
//        Map<String, List<String>> getMaps = PermissionMaps.getPermissionsMaps(targetOperation);
//        if (userDetails != null) {
//            userName = userDetails.getUsername();
//            for (String target : getMaps.keySet()) {
//                for (String operation : getMaps.get(target)) {
//                    userPermissions.add(target + "." + operation);
//                }
//            }
//        }

        log.trace("verifyPermission; userName={}; permission={}", userName, targetOperation);

        if (!userPermissions.contains(targetOperation)) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
            errorInfo.add("permission", targetOperation);

            throw new WebApplicationException(String.valueOf(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorInfo)));
        }
    }

    public void verifyPermission(String target, String operation) {
        final Map<String, List<String>> EMPTY_MAP = Collections.emptyMap();

        UserDetails userDetails = getCurrentUserDetails();
        String userName = "<unknown>";
        Map<String, List<String>> userPermissions = EMPTY_MAP;

        if (userDetails != null) {
            userName = userDetails.getUsername();
            userPermissions = userDetails.getPermissions();
            if (userPermissions == null) {
                userPermissions = EMPTY_MAP;
            }
        }

        log.trace("verifyPermission; userName={}; permission={}", userName, target + "." + operation);

        if (!userPermissions.containsKey(target)) {
            if (!userPermissions.get(target).contains(operation)) {
                ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
                errorInfo.add("permission", target + "." + operation);

                throw new WebApplicationException(String.valueOf(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorInfo)));
            }
        }
    }

    public Mono<ApplicationInfo[]> getUserAuthorizedApps(String casTicket) {
        if (casTicket != null && !casTicket.isEmpty()) {
            Mono clientResponse = null;
            String scopeQuery = extractScopeForRevokedApplicationAccess();
            log.debug("getUserAuthorizedAppsCasTicket; scopeQueryForRevokedAccess={}", scopeQuery);

            if (restClient.getAppKeyHeader() != null && !restClient.getAppKeyHeader().isEmpty()) {
                clientResponse = restClient.resource("userInfo", "apps").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.APP_KEY_IDENTIFIER_HEADER, restClient.getAppKeyHeader()).header(RBACUtil.TICKET_HEADER_IDENTIFIER, casTicket).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ApplicationInfo[].class).map(ResponseEntity::ok);
            } else {
                clientResponse = restClient.resource("userInfo", "apps").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.TICKET_HEADER_IDENTIFIER, casTicket).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ApplicationInfo[].class).map(ResponseEntity::ok);
            }
            //HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(responseBody).getStatusCode())
            if (HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(clientResponse).getStatusCode())) {
                return clientResponse.defaultIfEmpty(ApplicationInfoDetails[].class);
            }

            return Mono.just(new ApplicationInfo[0]);
        } else {
            return getUserAuthorizedApps();
        }
    }

    public Mono<ApplicationInfo[]> getUserAuthorizedApps() {
        Mono clientResponse = null;
        String scopeQuery = extractScopeForRevokedApplicationAccess();
        log.debug("getUserAuthorizedApps; scopeQueryForRevokedAccess={}", scopeQuery);

        if (restClient.getAppKeyHeader() != null && !restClient.getAppKeyHeader().isEmpty()) {
            clientResponse = restClient.resource("userInfo", "apps").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.APP_KEY_IDENTIFIER_HEADER, restClient.getAppKeyHeader()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ApplicationInfo[].class).map(ResponseEntity::ok);
        } else {
            clientResponse = restClient.resource("userInfo", "apps").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ApplicationInfo[].class).map(ResponseEntity::ok);
        }

        if (HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(clientResponse).getStatusCode())) {
            return clientResponse.defaultIfEmpty(ApplicationInfoDetails[].class);
        }

        return Mono.just(new ApplicationInfo[0]);
    }

    @SuppressWarnings("rawtypes")
    public Mono<List<Map>> getUserAuthorizedAppsDetailsInHtml(String casTicket, String appKey) {
        if (appKey == null || appKey.isEmpty()) {
            appKey = restClient.getAppKeyHeader();
        }
        if (casTicket != null && !casTicket.isEmpty()) {
            Mono clientResponse = null;
            String scopeQuery = extractScopeForRevokedApplicationAccess();
            log.debug("getUserAuthorizedAppsDetailsInHtmlCasTicket; scopeQueryForRevokedAccess={}", scopeQuery);

            if (appKey != null && !appKey.isEmpty()) {
                clientResponse = restClient.resource("userInfo", "appsDetailsInHtml").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.APP_KEY_IDENTIFIER_HEADER, appKey).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.TICKET_HEADER_IDENTIFIER, casTicket).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(List.class).map(ResponseEntity::ok);
            } else {
                clientResponse = restClient.resource("userInfo", "appsDetailsInHtml").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.TICKET_HEADER_IDENTIFIER, casTicket).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(List.class).map(ResponseEntity::ok);
            }

            if (HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(clientResponse).getStatusCode())) {
                return clientResponse;
            }
            return Mono.just(new ArrayList<Map>());
        } else {
            return getUserAuthorizedAppsDetailsInHtml(appKey);
        }
    }

    public Mono<List<Map>> getUserAuthorizedAppsDetailsInHtml() {
        return getUserAuthorizedAppsDetailsInHtml(null);
    }

    @SuppressWarnings("rawtypes")
    public Mono<List<Map>> getUserAuthorizedAppsDetailsInHtml(String appKey) {
        if (appKey == null || appKey.isEmpty()) {
            appKey = restClient.getAppKeyHeader();
        }
        Mono clientResponse = null;
        String scopeQuery = extractScopeForRevokedApplicationAccess();
        log.debug("getUserAuthorizedAppsDetailsInHtml; scopeQueryForRevokedAccess={}", scopeQuery);


        if (appKey != null && !appKey.isEmpty()) {
            clientResponse = restClient.resource("userInfo", "appsDetailsInHtml").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.APP_KEY_IDENTIFIER_HEADER, appKey).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(List.class).map(ResponseEntity::ok);
        } else {
            clientResponse = restClient.resource("userInfo", "appsDetailsInHtml").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(List.class).map(ResponseEntity::ok);
        }

        if (HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(clientResponse).getStatusCode())) {
            return clientResponse.defaultIfEmpty(new ParameterizedTypeReference<List<Map>>() {
            });
        }


        return Mono.just(new ArrayList<Map>());

    }

    public Mono<String> getUserAuthorizedAppsDetailsInJson() {
        return getUserAuthorizedAppsDetailsInJson(null);
    }

    public Mono<String> getUserAuthorizedAppsDetailsInJson(String appKey) {
        if (appKey == null || appKey.isEmpty()) {
            appKey = restClient.getAppKeyHeader();
        }
        Mono clientResponse = null;
        String scopeQuery = extractScopeForRevokedApplicationAccess();
        log.debug("getUserAuthorizedAppsDetails; scopeQueryForRevokedAccess={}", scopeQuery);

        if (appKey != null && !appKey.isEmpty()) {
            clientResponse = restClient.resource("userInfo", "appsDetailsInJson").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.APP_KEY_IDENTIFIER_HEADER, appKey).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class).map(ResponseEntity::ok);
        } else {
            clientResponse = restClient.resource("userInfo", "appsDetailsInJson").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class).map(ResponseEntity::ok);
        }

        if (HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(clientResponse).getStatusCode())) {
            return clientResponse.defaultIfEmpty(String.class);
        }


        return Mono.just("[]");

    }

    public Mono<String> getUserAuthorizedAppsDetailsInJson(String casTicket, String appKey) {
        if (appKey == null || appKey.isEmpty()) {
            appKey = restClient.getAppKeyHeader();
        }
        if (casTicket != null && !casTicket.isEmpty()) {
            Mono clientResponse = null;
            String scopeQuery = extractScopeForRevokedApplicationAccess();
            log.debug("getUserAuthorizedAppsDetails; scopeQueryForRevokedAccess={}", scopeQuery);


            if (appKey != null && !appKey.isEmpty()) {
                clientResponse = restClient.resource("userInfo", "appsDetailsInJson").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.APP_KEY_IDENTIFIER_HEADER, appKey).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.TICKET_HEADER_IDENTIFIER, casTicket).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class).map(ResponseEntity::ok);
            } else {
                clientResponse = restClient.resource("userInfo", "appsDetailsInJson").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.TICKET_HEADER_IDENTIFIER, casTicket).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class).map(ResponseEntity::ok);
            }

            if (HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(clientResponse).getStatusCode())) {
                return clientResponse.defaultIfEmpty(String.class);
            }

            return Mono.just("[]");
        } else {
            return getUserAuthorizedAppsDetailsInJson(appKey);
        }
    }

    public Mono<ApplicationInfoDetails[]> getUserAuthorizedAppsDetails(String casTicket) {
        if (casTicket != null && !casTicket.isEmpty()) {
            Mono clientResponse = null;
            String scopeQuery = extractScopeForRevokedApplicationAccess();
            log.debug("getUserAuthorizedAppsDetails; scopeQueryForRevokedAccess={}", scopeQuery);


            if (restClient.getAppKeyHeader() != null && !restClient.getAppKeyHeader().isEmpty()) {
                clientResponse = restClient.resource("userInfo", "appsDetails").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.APP_KEY_IDENTIFIER_HEADER, restClient.getAppKeyHeader()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.TICKET_HEADER_IDENTIFIER, casTicket).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ApplicationInfoDetails[].class).map(ResponseEntity::ok);
            } else {
                clientResponse = restClient.resource("userInfo", "appsDetails").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery)).header(RBACUtil.TICKET_HEADER_IDENTIFIER, casTicket).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ApplicationInfoDetails[].class).map(ResponseEntity::ok);
            }

            if (HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(clientResponse).getStatusCode())) {
                return clientResponse.defaultIfEmpty(ApplicationInfoDetails[].class);
            }

            return Mono.just(new ApplicationInfoDetails[0]);
        } else {
            return getUserAuthorizedAppsDetails();
        }
    }

    public Mono<ApplicationInfoDetails[]> getUserAuthorizedAppsDetails() {
        Mono clientResponse = null;

        if (restClient.getAppKeyHeader() != null && !restClient.getAppKeyHeader().isEmpty()) {
            clientResponse = restClient.resource("userInfo", "appsDetails").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.APP_KEY_IDENTIFIER_HEADER, restClient.getAppKeyHeader()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ApplicationInfoDetails[].class).map(ResponseEntity::ok);
        } else {
            clientResponse = restClient.resource("userInfo", "appsDetails").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ApplicationInfoDetails[].class).map(ResponseEntity::ok);
        }

        if (HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(clientResponse).getStatusCode())) {
            return clientResponse.defaultIfEmpty(ApplicationInfoDetails[].class);
        }

        return Mono.just(new ApplicationInfoDetails[0]);
    }

    public Mono<ApplicationInfoDetails[]> getUserAuthorizedAppsDetailsByAppKey(String appKey) {
        Mono clientResponse = null;

        if (appKey != null && !appKey.isEmpty()) {
            clientResponse = restClient.resource("userInfo", "appsDetails").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).header(RBACUtil.APP_KEY_IDENTIFIER_HEADER, appKey).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ApplicationInfoDetails[].class).map(ResponseEntity::ok);
        } else {
            clientResponse = restClient.resource("userInfo", "appsDetails").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", getCurrentUserDetails().getUsername()).build()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(ApplicationInfoDetails[].class).map(ResponseEntity::ok);
        }

        if (HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(clientResponse).getStatusCode())) {
            return clientResponse.defaultIfEmpty(ApplicationInfoDetails[].class);
        }

        return Mono.just(new ApplicationInfoDetails[0]);
    }

    public Integer getCurrentUserGroupId() {
        Mono<User> clientResponse = restClient
                .resource("users", getCurrentUserDetails()
                        .getUserInfo().getUserId().toString()).build().get().accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(User.class);
        if (clientResponse == null) {
            return -1;
        } else {
            return clientResponse.block().getGroupId();
        }
    }

    public Mono<String> getCurrentUserGroupName(Integer groupId) {
        Mono<String> groupName = restClient.resource("groups", "groupName", groupId.toString()).build().get()
                .accept(MediaType.TEXT_PLAIN).retrieve().bodyToMono(String.class);
        if (groupName == null) {
            return Mono.just("");
        } else {
            return groupName;
        }
    }

    public List<Long> getTenantIds() {
        return getCurrentUserDetailsRBAC().getAvailableTenantIds();
    }

    public List<Tenant> getTenantDetails() {
        return getCurrentUserDetailsRBAC().getAvailableTenants();
    }

    public String extractScopeForRoleView() {
        Map<String, String> scopeMap = getCurrentUserDetailsRBAC().getUserInfo().getScopes();
        if (scopeMap != null) {
            return scopeMap.get(RBACUtil.SCOPE_KEY_ROLE_VIEW);
        }
        return null;
    }

    public String extractScopeForGroupView() {
        return extractScopeForGroupView(false);
    }

    public String extractScopeForGroupView(boolean excludeSession) {
        Map<String, String> scopeMap = getCurrentUserDetailsRBAC().getUserInfo().getScopes();
        List<Long> selectedTenantList = getCurrentUserDetailsRBAC().getSelectedTenantList();
        return RBACUtil.extractScopeForGroup(scopeMap, selectedTenantList, excludeSession);
    }

    public String extractScopeForUserView() {
        return extractScopeForUserView(false);
    }

    public String extractScopeForUserView(boolean excludeSession) {
        Map<String, String> scopeMap = getCurrentUserDetailsRBAC().getUserInfo().getScopes();
        List<Long> selectedTenantList = getCurrentUserDetailsRBAC().getSelectedTenantList();
        return RBACUtil.extractScopeForUser(scopeMap, selectedTenantList, excludeSession);
    }

    public String extractScopeForTenant() {
        return extractScopeForTenant(false);
    }

    public String extractScopeForTenant(boolean excludeSession) {
        Map<String, String> scopeMap = getCurrentUserDetailsRBAC().getUserInfo().getScopes();
        List<Long> selectedTenantList = getCurrentUserDetailsRBAC().getSelectedTenantList();
        return RBACUtil.extractScopeForTenant(scopeMap, selectedTenantList, excludeSession);
    }

    public String extractScopeForOrganization() {
        return extractScopeForOrganization(false);
    }

    public String extractScopeForOrganization(boolean excludeSession) {
        Map<String, String> scopeMap = getCurrentUserDetailsRBAC().getUserInfo().getScopes();
        List<Long> selectedTenantList = getCurrentUserDetailsRBAC().getSelectedTenantList();
        return RBACUtil.extractScopeForOrganization(scopeMap, selectedTenantList, excludeSession);
    }

    public void setSelectedTenants(List<Long> selectedTenantList) {
        getCurrentUserDetailsRBAC().setSelectedTenantList(selectedTenantList);
        /** Added By Fazia to get maker checker enabled flag for the switched tenant **/
        Mono<Tenant> tenant = restClient.resource("tenants", selectedTenantList.get(0) + "").build().get()
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Tenant.class);
        getCurrentUserDetailsRBAC().setMakerCheckerEnabledInTenant(tenant.block().isMakerCheckerEnabled());

    }

    public Mono<UserInfo> getUserInfo(String userName, String applicatioName, String ticket) {
        Mono<UserInfo> userInfo = restClient.resource("userInfo").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", userName).queryParam("applicationName", applicatioName).queryParam("ticket", ticket).build()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(UserInfo.class);
        return userInfo;
    }

    public Mono<UserInfoGenericV3> getUserInfoGenericV3(String userName, String applicationName, String childAppName) {
        Mono<UserInfoGenericV3> userInfo = restClient.resource("userInfo/v3/detailsWithAttributes").build().get().uri(uriBuilder -> uriBuilder.queryParam("userName", userName).queryParam("applicationName", applicationName).queryParam("childAppName", childAppName).build()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(UserInfoGenericV3.class);
        return userInfo;
    }


    public String extractScopeForRevokedApplicationAccess() {
        Map<String, String> scopeMap = getCurrentUserDetailsRBAC().getUserInfo().getScopes();
        if (scopeMap != null) {
            return scopeMap.get(RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS);
        }
        return null;
    }
}
