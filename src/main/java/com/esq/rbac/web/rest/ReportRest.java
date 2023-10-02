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
import com.esq.rbac.web.exception.ErrorInfoException;
import com.esq.rbac.web.util.DeploymentUtil;
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.util.WebParamsUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping(ReportRest.RESOURCE_PATH)
public class ReportRest {

    public static final String RESOURCE_PATH = "reports";
    private static final Logger log = LoggerFactory
            .getLogger(ReportRest.class);
    private DeploymentUtil deploymentUtil;
    private RestClient restClient;
    private UserDetailsService userDetailsService;

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        this.deploymentUtil = deploymentUtil;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        log.debug("setUserDetailService; {}", userDetailsService);
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setRestClient(RestClient restClient) {
        log.debug("setRestClient;");
        this.restClient = restClient;
    }


    @PostMapping(path = "/{scopeName}/customData", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getCustomData(
            @PathVariable("scopeName") String scopeName, HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        verifyReportPermissions(scopeName);
        if (!params.isEmpty() && params.containsKey("userId")) {
            String userId = String.valueOf(params.get("userId")).replaceAll("[\\[\\]]", "");
            String regex = "^[,0-9]+$";
            if (!userId.equalsIgnoreCase("All")) {
                if (!Pattern.compile(regex).matcher(userId).matches()) {
                    throw new ErrorInfoException("validationError", "valid userId");
                }
            }
        }
        if (scopeName.equals("auditLog") || scopeName.equals("auditLogCount") || scopeName.equals("accessMatrix") || scopeName.equals("accessMatrixCount") || scopeName.equals("loginLog") || scopeName.equals("loginLogCount") ||
                scopeName.equals("auditLogApp") || scopeName.equals("auditLogAppCount") || scopeName.equals("auditLogAppTarget") || scopeName.equals("auditLogAppTargetCount") || scopeName.equals("auditLogAppTargetOperation") || scopeName.equals("auditLogAppTargetOperationCount")) {
            String userScopeQuery = userDetailsService.extractScopeForUserView();
            params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(userScopeQuery));
        } else if (scopeName.equals("accessMatrixGroup") || scopeName.equals("accessMatrixGroupCount") || scopeName.equals("groupScopeDescription") || scopeName.equals("groupScopeDescriptionCount") || scopeName.equals("userActivity") || scopeName.equals("userActivityCount")) {
            String groupScopeQuery = userDetailsService.extractScopeForGroupView();
            params.add(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(groupScopeQuery));
        }

        /*added by pankaj for global user search*/
        else if (scopeName.equals("globalUserSearch")) {

            return restClient.resource(ScopeConstraintRest.RESOURCE_PATH, scopeName, "globalUserSearchCustomData").build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParams(params).build())
                    .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
        }
        /*added by pankaj for global user search result count*/
        else if (scopeName.equals("globalUserSearchCount")) {

            return restClient.resource(ScopeConstraintRest.RESOURCE_PATH, scopeName, "globalUserSearchCustomDataCount").build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParams(params)
                            .build())
                    .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class);
        }

        return restClient
                .resource(ScopeConstraintRest.RESOURCE_PATH, scopeName, "customData").build()
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .queryParams(params).build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }


    @PostMapping(path = "/{scopeName}/ExportCSVData", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = "application/vnd.ms-excel;charset=utf-8")
    public Mono<ResponseEntity> getCustomDataExport(
            @PathVariable("scopeName") String scopeName, HttpServletRequest httpRequest) throws IOException {
//        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        log.trace("list; ApplicationMaintenance--requestUri={}", httpRequest.getRequestURI());
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> params.addAll(key, Arrays.asList(values)));
     //   verifyReportPermissions(scopeName);
        log.trace("getCustomDataExport; scopeName={};", scopeName);
        if (scopeName.equals("auditLog") || scopeName.equals("auditLogCount") || scopeName.equals("accessMatrix") || scopeName.equals("accessMatrixCount") ||
                scopeName.equals("auditLogApp") || scopeName.equals("auditLogAppCount") || scopeName.equals("auditLogAppTarget") || scopeName.equals("auditLogAppTargetCount") ||
                scopeName.equals("auditLogAppTargetOperation") || scopeName.equals("auditLogAppTargetOperationCount")) {
            String userScopeQuery = userDetailsService.extractScopeForUserView();
            params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(userScopeQuery));
        } else if (scopeName.equals("accessMatrixGroup") || scopeName.equals("accessMatrixGroupCount") || scopeName.equals("groupScopeDescription") || scopeName.equals("groupScopeDescriptionCount") || scopeName.equals("userActivity") || scopeName.equals("userActivityCount")) {
            String groupScopeQuery = userDetailsService.extractScopeForGroupView();
            params.add(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(groupScopeQuery));
        }
        String fileName = params.containsKey("fileName") ? params.get("fileName").get(0) : scopeName;
        final Mono<ResponseEntity<byte[]>> clientResponse = restClient
                .resource(deploymentUtil.getReportRestReadTimeout(), ScopeConstraintRest.RESOURCE_PATH, scopeName, "ExportData")
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder.queryParams(params).build())
                .accept(MediaType.valueOf("application/vnd.ms-excel;charset=utf-8"))
                .retrieve()
                .toEntity(byte[].class);

        return clientResponse.flatMap(responseEntity -> {
            if (HttpStatus.OK.isSameCodeAs(responseEntity.getStatusCode())) {
                byte[] bytes = responseEntity.getBody();
                InputStream inputStream = new ByteArrayInputStream(bytes);

                StreamingResponseBody stream = os -> {
                    try {
                        byte[] buffer = new byte[deploymentUtil.getReportResponseWriteSizeBytes()];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                    } catch (Exception e) {
                        log.error("getCustomDataExport; Exception={};", e);
                    }
                };


                return Mono.just(ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=" + fileName)
                        .header("Set-Cookie", "fileDownload=true; path=/")
                        .body(stream));
            } else {
                return Mono.just(ResponseEntity.status(responseEntity.getStatusCode())
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(responseEntity.toString()));
            }
        });

    }


    @PostMapping(path = "/{scopeName}/ExportPDFData", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity> getCustomPDFDataExport(
            @PathVariable("scopeName") String scopeName, HttpServletRequest httpRequest) throws IOException {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        verifyReportPermissions(scopeName);

        if (scopeName.equals("auditLog") || scopeName.equals("auditLogCount") || scopeName.equals("accessMatrix") || scopeName.equals("accessMatrixCount") ||
                scopeName.equals("auditLogApp") || scopeName.equals("auditLogAppCount") || scopeName.equals("auditLogAppTarget") || scopeName.equals("auditLogAppTargetCount") ||
                scopeName.equals("auditLogAppTargetOperation") || scopeName.equals("auditLogAppTargetOperationCount")) {
            String userScopeQuery = userDetailsService.extractScopeForUserView();
            params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(userScopeQuery));
        } else if (scopeName.equals("accessMatrixGroup") || scopeName.equals("accessMatrixGroupCount") || scopeName.equals("groupScopeDescription") || scopeName.equals("groupScopeDescriptionCount") || scopeName.equals("userActivity") || scopeName.equals("userActivityCount")) {
            String groupScopeQuery = userDetailsService.extractScopeForGroupView();
            params.add(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(groupScopeQuery));
        }
        String fileName = params.containsKey("fileName") ? params.get("fileName").get(0) : scopeName;
        final Mono<ResponseEntity<byte[]>> clientResponse = restClient
                .resource(deploymentUtil.getReportRestReadTimeout(), ScopeConstraintRest.RESOURCE_PATH, scopeName, "ExportPDFData").build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .toEntity(byte[].class);


        return clientResponse.flatMap(responseEntity -> {
            if (HttpStatus.OK.isSameCodeAs(responseEntity.getStatusCode())) {
                byte[] bytes = responseEntity.getBody();
                InputStream inputStream = new ByteArrayInputStream(bytes);

                StreamingResponseBody stream = os -> {
                    try {
                        byte[] buffer = new byte[deploymentUtil.getReportResponseWriteSizeBytes()];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                    } catch (Exception e) {
                        log.error("getCustomDataExport; Exception={};", e);
                    }
                };


                return Mono.just(ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + fileName).header("Set-Cookie", "pdfFileDownload=true; path=/").body(stream));
            } else {
                return Mono.just(ResponseEntity.status(responseEntity.getStatusCode()).contentType(MediaType.TEXT_PLAIN)
                        .body(responseEntity.toString()));
            }
        });

    }

    private void verifyReportPermissions(String scopeName) {
        if (userDetailsService != null && userDetailsService.getCurrentUserDetails() != null) {
            Map<String, List<String>> userPermissions = userDetailsService.getCurrentUserDetails().getPermissions();
            List<String> reportPermission = userPermissions.get("Report");
            if ((reportPermission == null && (scopeName.equals("accessMatrixGroup") || scopeName.equals("accessMatrixGroupCount"))) ||
                    (reportPermission != null && !reportPermission.contains("Access.Matrix.Group-wise") && (scopeName.equals("accessMatrixGroup") || scopeName.equals("accessMatrixGroupCount")))) {
                ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
                errorInfo.add("permission", "Access.Matrix.Group-wise");
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(errorInfo).build());
            }
            if ((reportPermission == null && (scopeName.equals("accessMatrix") || scopeName.equals("accessMatrixCount"))) ||
                    (reportPermission != null && !reportPermission.contains("Access.Matrix.User-wise") && (scopeName.equals("accessMatrix") || scopeName.equals("accessMatrixCount")))) {
                ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
                errorInfo.add("permission", "Access.Matrix.User-wise");
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(errorInfo).build());
            }
            if ((reportPermission == null && (scopeName.equals("auditLog") || scopeName.equals("auditLogCount"))) ||
                    (reportPermission != null && !reportPermission.contains("Audit.Log") && (scopeName.equals("auditLog") || scopeName.equals("auditLogCount")))) {
                ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
                errorInfo.add("permission", "Audit.Log");
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(errorInfo).build());
            }
            if ((reportPermission == null && (scopeName.equals("groupScopeDescription") || scopeName.equals("groupScopeDescriptionCount"))) ||
                    (reportPermission != null && !reportPermission.contains("Scope.Details") && (scopeName.equals("groupScopeDescription") || scopeName.equals("groupScopeDescriptionCount")))) {
                ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
                errorInfo.add("permission", "Scope.Details");
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(errorInfo).build());
            }
            if ((reportPermission == null && (scopeName.equals("userActivity") || scopeName.equals("userActivityCount"))) ||
                    (reportPermission != null && !reportPermission.contains("User.Activity") && (scopeName.equals("userActivity") || scopeName.equals("userActivityCount")))) {
                ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
                errorInfo.add("permission", "User.Activity");
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(errorInfo).build());
            }
            if ((reportPermission == null && (scopeName.equals("loginLog") || scopeName.equals("loginLogCount"))) ||
                    (reportPermission != null && !reportPermission.contains("LoginLog") && (scopeName.equals("loginLog") || scopeName.equals("loginLogCount")))) {
                ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
                errorInfo.add("permission", "LoginLog");
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(errorInfo).build());
            }
            /* Added by pankaj for global user search */

            if ((reportPermission == null
                    && (scopeName.equals("globalUserSearch") || scopeName.equals("globalUserSearchCount")))
                    || (reportPermission != null && !reportPermission.contains("Global.User.Search")
                    && (scopeName.equals("globalUserSearch") || scopeName.equals("globalUserSearchCount")))) {
                ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
                errorInfo.add("permission", "globalUserSearch");
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(errorInfo).build());
            }
        }
    }

    /*added by pankaj for global user search csv generation request*/


    @PostMapping(path = "/{scopeName}/ExportCSVDataGlobalUserSearch", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = "application/vnd.ms-excel;charset=utf-8")
    public Mono<ResponseEntity> getCustomDataExportGlobalUserSearch(@PathVariable("scopeName") String scopeName,
                                                                    HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        verifyReportPermissions(scopeName);
        log.trace("getCustomDataExport; scopeName={};", scopeName);

        String fileName = params.containsKey("fileName") ? params.get("fileName").get(0) : scopeName;

        final Mono<ResponseEntity<byte[]>> clientResponse = restClient
                .resource(deploymentUtil.getReportRestReadTimeout(), ScopeConstraintRest.RESOURCE_PATH, scopeName,
                        "ExportCSVDataGlobalUserSearch").build()
                .get()
                .uri(uriBuilder -> uriBuilder.queryParams(params).build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.valueOf("application/vnd.ms-excel;charset=utf-8")).retrieve()
                .toEntity(byte[].class);

        return clientResponse.flatMap(responseEntity -> {
            if (HttpStatus.OK.isSameCodeAs(responseEntity.getStatusCode())) {
                byte[] bytes = responseEntity.getBody();
                InputStream inputStream = new ByteArrayInputStream(bytes);

                StreamingResponseBody stream = os -> {
                    try {
                        byte[] buffer = new byte[deploymentUtil.getReportResponseWriteSizeBytes()];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                    } catch (Exception e) {
                        log.error("getCustomDataExport; Exception={};", e);
                    }
                };


                return Mono.just(ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + fileName).header("Set-Cookie", "pdfFileDownload=true; path=/").body(stream));
            } else {
                return Mono.just(ResponseEntity.status(responseEntity.getStatusCode()).contentType(MediaType.TEXT_PLAIN)
                        .body(responseEntity.toString()));
            }
        });
    }

    /*added by pankaj for global user search pdf generation request*/

    @PostMapping(path = "/{scopeName}/ExportPDFDataGlobalUserSearch", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity> getCustomPDFDataExportGlobalUserSearch(@PathVariable("scopeName") String scopeName,
                                                                       HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        verifyReportPermissions(scopeName);

        String fileName = params.containsKey("fileName") ? params.get("fileName").get(0) : scopeName;
        final Mono<ResponseEntity<byte[]>> clientResponse = restClient
                .resource(deploymentUtil.getReportRestReadTimeout(), ScopeConstraintRest.RESOURCE_PATH, scopeName,
                        "ExportPDFDataGlobalUserSearch").build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParams(params).build())
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
                .accept(MediaType.APPLICATION_OCTET_STREAM).retrieve()
                .toEntity(byte[].class);
        return clientResponse.flatMap(responseEntity -> {
            if (HttpStatus.OK.isSameCodeAs(responseEntity.getStatusCode())) {
                byte[] bytes = responseEntity.getBody();
                InputStream inputStream = new ByteArrayInputStream(bytes);

                StreamingResponseBody stream = os -> {
                    try {
                        byte[] buffer = new byte[deploymentUtil.getReportResponseWriteSizeBytes()];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                    } catch (Exception e) {
                        log.error("getCustomDataExport; Exception={};", e);
                    }
                };
                return Mono.just(ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + fileName)
                        .header("Set-Cookie", "pdfFileDownload=true; path=/").body(stream));
            } else {
                return Mono.just(ResponseEntity.status(responseEntity.getStatusCode()).contentType(MediaType.TEXT_PLAIN)
                        .body(responseEntity.toString()));
            }
        });
    }


}
