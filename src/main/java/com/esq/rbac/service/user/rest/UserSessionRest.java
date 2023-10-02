package com.esq.rbac.service.user.rest;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.service.ApplicationDal;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.loginservice.embedded.LogoutRequest;
import com.esq.rbac.service.loginservice.embedded.LogoutResponse;
import com.esq.rbac.service.loginservice.embedded.SessionRegistryLogoutRequest;
import com.esq.rbac.service.loginservice.service.LoginService;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.sessiondata.domain.Session;
import com.esq.rbac.service.sessiondata.service.SessionService;
import com.esq.rbac.service.util.AuditLogger;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//@Path("/usersSession")
@RestController
@RequestMapping("/usersSession")
@Slf4j
public class UserSessionRest {
    private final SessionService sessionDal;
    private final AuditLogger auditLogger;
    private final LoginService loginService;

//    @Inject
//    public void setDependencies(SessionDal sessionDal, AuditLogDal auditLogDal, LoginService loginService, ApplicationDal applicationDal) {
//        log.trace("setUserSessionDal sessionDal={};", sessionDal);
//        this.sessionDal = sessionDal;
//        this.auditLogger = new AuditLogger(auditLogDal);
//        this.loginService = loginService;
//    }

    public UserSessionRest(SessionService sessionDal, AuditLogService auditLogDal, LoginService loginService, ApplicationDal applicationDal) {
        log.trace("setUserSessionDal sessionDal={};", sessionDal);
        this.sessionDal = sessionDal;
        this.auditLogger = new AuditLogger(auditLogDal);
        this.loginService = loginService;
    }

    public static Map<String, String> generateSessionTerminationLog(String userName, Map<String, List<String>> sessionHashChildApplicationNames) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("name", userName);
        if (sessionHashChildApplicationNames != null && !sessionHashChildApplicationNames.isEmpty()) {
            for (String sessionHash : sessionHashChildApplicationNames.keySet()) {
                if (sessionHashChildApplicationNames.get(sessionHash) != null && !sessionHashChildApplicationNames.get(sessionHash).isEmpty()) {
                    result.put("Session-" + sessionHash.substring(0, 15) + "..." + ":old", sessionHashChildApplicationNames.get(sessionHash).toString());
                    for (String appName : sessionHashChildApplicationNames.get(sessionHash)) {
                        result.put("SessionHashAppName", sessionHash + "-" + appName);
                    }
                }
            }
        }
        return result;
    }

    @GetMapping(value = "/userSessionInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Session[]> getUserSessionInfo(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getUserSessionInfo; queryParams={}", servletRequest.getParameterMap());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Session> list = new ArrayList<Session>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM) && uriInfo.get(SearchUtils.SEARCH_PARAM) != null && !uriInfo.get(SearchUtils.SEARCH_PARAM).get(0).isEmpty()) {
            list = sessionDal.searchList(options);
        } else {
            list = sessionDal.getList(options);
        }
        Session[] array = new Session[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }

    @GetMapping(value = "/userCount", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Integer> count(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("count; queryParams={}", servletRequest.getParameterMap());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);

        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM) && uriInfo.get(SearchUtils.SEARCH_PARAM) != null && !uriInfo.get(SearchUtils.SEARCH_PARAM).get(0).isEmpty()) {
            return ResponseEntity.ok(sessionDal.getSearchCount(options));
        } else {
            return ResponseEntity.ok(sessionDal.getUserNameCount(options));
        }
    }

    @PostMapping(value = "/destroySession/all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "loggedInUserName", description = "loggedInUserName", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<LogoutResponse> destroySessionAll(@RequestBody SessionRegistryLogoutRequest request, @RequestHeader HttpHeaders headers) throws Exception {
        String loggedInUserName = headers.get("loggedInUserName") != null ? headers.get("loggedInUserName").get(0) : null;
        request.setLogoutType(LogoutRequest.LOGOUT_TYPE_SESSION_KILL + loggedInUserName);
        request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_ALL);
        request.setRequestId(RBACUtil.generateLogoutRequestId());
        LogoutResponse response = loginService.sessionRegistryLogout(request, loggedInUserName, null);
        auditLogger.logCreate(Lookup.getUserId(loggedInUserName), request.getUserName(), "UserSession", "Terminate", generateSessionTerminationLog(request.getUserName(), response.getSessionHashChildApplicationNames()));
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/destroySession", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "loggedInUserName", description = "loggedInUserName", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<LogoutResponse> destroySessionBySessionHash(@RequestBody SessionRegistryLogoutRequest request, @RequestHeader HttpHeaders headers) throws Exception {
        String loggedInUserName = headers.get("loggedInUserName") != null ? headers.get("loggedInUserName").get(0) : null;
        request.setLogoutType(LogoutRequest.LOGOUT_TYPE_SESSION_KILL + loggedInUserName);
        request.setRequestId(RBACUtil.generateLogoutRequestId());
        LogoutResponse response = null;
        if (ChildApplication.isSSO(request.getAppType())) {
            request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_SSO);
            response = loginService.sessionRegistryLogout(request, loggedInUserName, null);
        } else if (ChildApplication.isNonSSO(request.getAppType())) {
            request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_NON_SSO);
            request.setChildApplication(Lookup.getChildApplicationByNameNew(request.getChildAppName()));
            response = loginService.sessionRegistryLogout(request, loggedInUserName, null);
        } else if (ChildApplication.isNative(request.getAppType())) {
            request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_NATIVE);
            ChildApplication childApplication = Lookup.getChildApplicationByNameNew(request.getChildAppName());
            response = loginService.destroyNativeSession(request.getSessionHash(), childApplication, request.getClientIp(), request.getLogoutType());
        }
        auditLogger.logCreate(Lookup.getUserId(loggedInUserName), request.getUserName(), "UserSession", "Terminate", generateSessionTerminationLog(request.getUserName(), response.getSessionHashChildApplicationNames()));
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/appWiseCount", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<String> getAppWiseLoggedInCount(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getAppWiseLoggedInCount; queryParams={}", servletRequest.getParameterMap());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list = sessionDal.getAppWiseLoggedInCount(options);
        return ResponseEntity.ok().body(new Gson().toJson(list));
    }

    @GetMapping(value = "/loginType", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "userName", description = "userName", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<String> getCurrentUserLoginType(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getCurrentUserLoginType; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        List<Session> list = sessionDal.getSessionsByUserName(uriInfo.getFirst("userName"));
        return ResponseEntity.ok().body(new Gson().toJson(list));
    }

}
