package com.esq.rbac.service.user.rest;


import com.esq.rbac.service.application.childapplication.appurldata.AppUrlData;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.service.ApplicationDal;
import com.esq.rbac.service.application.vo.ApplicationInfo;
import com.esq.rbac.service.application.vo.ApplicationInfoDetails;
import com.esq.rbac.service.application.vo.SwitcherApplicationInfo;
import com.esq.rbac.service.culture.embedded.ApplicationCulture;
import com.esq.rbac.service.culture.service.CultureDal;
import com.esq.rbac.service.exception.ErrorInfo;
import com.esq.rbac.service.externaldb.service.ExternalDbDal;
import com.esq.rbac.service.group.service.GroupDal;
import com.esq.rbac.service.loginservice.service.LoginService;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import com.esq.rbac.service.scope.scopeconstraint.service.ScopeConstraintDal;
import com.esq.rbac.service.sessiondata.domain.Session;
import com.esq.rbac.service.sessiondata.service.SessionService;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.tenant.service.TenantDal;
import com.esq.rbac.service.timezonemaster.domain.TimeZoneMaster;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.user.vo.*;
import com.esq.rbac.service.util.*;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/userInfo")
@Slf4j
public class UserInfoRest {

    private static final int CACHE_MAX_SIZE = 1000;
    private static final int CACHE_MAX_MILLISECONDS = 5000;
    private final UserDal userDal;
    private final ApplicationDal applicationDal;
    private final GroupDal groupDal;
    private final ScopeConstraintDal scopeConstraintDal;
    private final ExternalDbDal externalDbDal;
    private final DeploymentUtil deploymentUtil;
    private final LoginService loginService;
    private final ChildAppPermValidatorUtil childAppPermValidatorUtil;
    private final TenantDal tenantDal;
    private final EnvironmentUtil environmentUtil;
    private final SessionService sessionDal;
    private final CultureDal cultureDal;
    private final Cache<String, UserInfo> cache = CacheBuilder.newBuilder().maximumSize(CACHE_MAX_SIZE).expireAfterWrite(CACHE_MAX_MILLISECONDS, TimeUnit.MILLISECONDS).recordStats().build();
    private final Cache<String, UserInfoDetails> cacheUserInfoDetails = CacheBuilder.newBuilder().maximumSize(CACHE_MAX_SIZE).expireAfterWrite(CACHE_MAX_MILLISECONDS, TimeUnit.MILLISECONDS).recordStats().build();
    private final Cache<String, UserInfoGenericV2> cacheUserInfoGeneric = CacheBuilder.newBuilder().maximumSize(CACHE_MAX_SIZE).expireAfterWrite(CACHE_MAX_MILLISECONDS, TimeUnit.MILLISECONDS).recordStats().build();
    private final Cache<String, UserInfoGenericV3> cacheUserInfoGenericV3 = CacheBuilder.newBuilder().maximumSize(CACHE_MAX_SIZE).expireAfterWrite(CACHE_MAX_MILLISECONDS, TimeUnit.MILLISECONDS).recordStats().build();
    private final Cache<String, UserInfoRBAC> cacheUserInfoRBAC = CacheBuilder.newBuilder().maximumSize(CACHE_MAX_SIZE).expireAfterWrite(CACHE_MAX_MILLISECONDS, TimeUnit.MILLISECONDS).recordStats().build();
    private SwitcherHtmlUtil switcherHtmlUtil;

    @Autowired
    public UserInfoRest(UserDal userDal, ApplicationDal applicationDal, GroupDal groupDal, ScopeConstraintDal scopeConstraintDal, ExternalDbDal externalDbDal, DeploymentUtil deploymentUtil, LoginService loginService, ChildAppPermValidatorUtil childAppPermValidatorUtil, SessionService sessionDal, SwitcherHtmlUtil switcherHtmlUtil, TenantDal tenantDal, EnvironmentUtil environmentUtil, CultureDal cultureDal) {
        log.debug("setDependencies; userDal={}; applicationDal={}; groupDal={}; scopeConstraintDal={}; externalDbDal={}; deploymentUtil={}; loginService={}; childAppPermValidatorUtil={}; sessionDal={};", userDal, applicationDal, groupDal, scopeConstraintDal, externalDbDal, deploymentUtil, loginService, childAppPermValidatorUtil, sessionDal);
        this.userDal = userDal;
        this.applicationDal = applicationDal;
        this.groupDal = groupDal;
        this.scopeConstraintDal = scopeConstraintDal;
        this.externalDbDal = externalDbDal;
        this.deploymentUtil = deploymentUtil;
        this.loginService = loginService;
        this.childAppPermValidatorUtil = childAppPermValidatorUtil;
        this.sessionDal = sessionDal;
        this.switcherHtmlUtil = switcherHtmlUtil;
        this.tenantDal = tenantDal;
        this.environmentUtil = environmentUtil;
        this.cultureDal = cultureDal;
    }

    public static String getDisplayName(User user) {
        StringBuilder sb = new StringBuilder();
        if (Strings.isNullOrEmpty(user.getFirstName()) && Strings.isNullOrEmpty(user.getLastName())) {
            sb.append(user.getUserName());

        } else if (Strings.isNullOrEmpty(user.getFirstName())) {
            sb.append(user.getUserName()).append(" (");
            sb.append(user.getLastName()).append(")");

        } else if (Strings.isNullOrEmpty(user.getLastName())) {
            sb.append(user.getUserName()).append(" (");
            sb.append(user.getFirstName()).append(")");

        } else {
            sb.append(user.getUserName()).append(" (");
            sb.append(user.getFirstName()).append(" ");
            sb.append(user.getLastName()).append(")");

        }
        return sb.toString();
    }

//    @Inject
//    public void setTenantDal(TenantDal tenantDal){
//        this.tenantDal = tenantDal;
//    }
//
//    @Inject
//    public void setEnvironmentUtil(EnvironmentUtil environmentUtil){
//        this.environmentUtil = environmentUtil;
//    }
//
//    @Inject
//    public void setCultureDal(CultureDal cultureDal){
//        this.cultureDal = cultureDal;
//    }

    @Autowired
    public void setSwitcherHtmlUtil(SwitcherHtmlUtil switcherHtmlUtil) {
        this.switcherHtmlUtil = switcherHtmlUtil;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfo> getUserInfo(@RequestParam(name = "userName", required = false) String userName, @RequestParam(name = "applicationName", required = false) String applicationName, @RequestParam(name = "ticket", required = false) String ticket) {
        log.trace("getUserInfo; userName={}; applicationName={}; ticket={};", userName, applicationName, ticket);
        String key = getKey(userName, applicationName);
        UserInfo userInfo = cache.getIfPresent(key);
        if (userInfo == null) {
            userInfo = loadUserInfo(userName, applicationName, ticket);
            cache.put(key, userInfo);
        }
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfoDetails> getUserInfoDetails(@RequestParam(name = "userName", required = false) String userName, @RequestParam(name = "applicationName", required = false) String applicationName) {
        log.trace("getUserInfoDetails; userName={}; applicationName={}", userName, applicationName);

        String key = getKey(userName, applicationName);
        UserInfoDetails userInfoDetails = cacheUserInfoDetails.getIfPresent(key);
        if (userInfoDetails == null) {
            userInfoDetails = loadUserInfoDetails(userName, applicationName);
            cacheUserInfoDetails.put(key, userInfoDetails);
        }
        return ResponseEntity.ok(userInfoDetails);
    }

    @GetMapping(value = "/rbacUserInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfoRBAC> getUserInfoRBAC(@RequestParam(name = "userName", required = false) String userName, @RequestParam(name = "applicationName", required = false) String applicationName) {
        log.trace("getUserInfoRBAC; userName={}; applicationName={}", userName, applicationName);

        String key = getKey(userName, applicationName);
        UserInfoRBAC userInfoRBAC = cacheUserInfoRBAC.getIfPresent(key);
        if (userInfoRBAC == null) {
            userInfoRBAC = loadUserInfoRBAC(userName, applicationName);
            cacheUserInfoRBAC.put(key, userInfoRBAC);
        }
        return ResponseEntity.ok(userInfoRBAC);
    }

    @GetMapping(value = "/detailsWithAttributes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfoGeneric> getUserInfoGeneric(@RequestParam(name = "userName", required = false) String userName, @RequestParam(name = "applicationName", required = false) String applicationName) {
        log.trace("getUserInfoGeneric; userName={}; applicationName={}", userName, applicationName);

        String key = getKey(userName, applicationName);
        UserInfoGenericV2 userInfoGeneric = cacheUserInfoGeneric.getIfPresent(key);
        if (userInfoGeneric == null) {
            userInfoGeneric = loadUserInfoGenericV2(userName, applicationName);
            cacheUserInfoGeneric.put(key, userInfoGeneric);
        }
        return ResponseEntity.ok(UserInfoGenericV2.toUserInfoGeneric(userInfoGeneric));
    }

    @GetMapping(value = "/v2/detailsWithAttributes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfoGeneric> getUserInfoGenericV2(@RequestParam(name = "userName", required = false) String userName, @RequestParam(name = "applicationName", required = false) String applicationName, @RequestParam(name = "appKey", required = false) String appKey) {
        log.trace("getUserInfoGenericV2; userName={}; applicationName={}", userName, applicationName);
        if (appKey != null && !appKey.isEmpty() && (applicationName == null || applicationName.isEmpty()))
            applicationName = applicationDal.getApplicationNameByAppKey(appKey);

        String key = getKey(userName, applicationName);
        UserInfoGenericV2 userInfoGeneric = cacheUserInfoGeneric.getIfPresent(key);
        if (userInfoGeneric == null) {
            userInfoGeneric = loadUserInfoGenericV2(userName, applicationName);
            cacheUserInfoGeneric.put(key, userInfoGeneric);
        }
        return ResponseEntity.ok(userInfoGeneric);
    }

    @GetMapping(value = "/v3/detailsWithAttributesByAppKey", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfoGenericV3> getUserInfoGenericV2ByAppKey(@RequestParam(name = "userName", required = false) String userName, @RequestParam(name = "applicationName", required = false) String applicationName, @RequestParam(name = "appKey", required = false) String appKey) {
        log.trace("getUserInfoGenericV3; userName={}; applicationName={}", userName, applicationName);

        if (appKey != null && !appKey.isEmpty() && (applicationName == null || applicationName.isEmpty()))
            applicationName = applicationDal.getApplicationNameByAppKey(appKey);

        String key = getKey(userName, applicationName);
        UserInfoGenericV3 userInfoGeneric = cacheUserInfoGenericV3.getIfPresent(key);
        if (userInfoGeneric == null) {
            userInfoGeneric = loadUserInfoGenericV3(userName, applicationName, null);
            cacheUserInfoGenericV3.put(key, userInfoGeneric);
        }
        return ResponseEntity.ok(userInfoGeneric);
    }

    @GetMapping(value = "/v3/detailsWithAttributes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfoGenericV3> getUserInfoGenericV3(@RequestParam(name = "userName", required = false) String userName, @RequestParam(name = "applicationName", required = false) String applicationName, @RequestParam(name = "appKey", required = false) String appKey, @RequestParam(name = "childAppName", required = false) String childAppName) {
        log.trace("getUserInfoGenericV3; userName={}; applicationName={}", userName, applicationName);
        if (appKey != null && !appKey.isEmpty() && (applicationName == null || applicationName.isEmpty()))
            applicationName = applicationDal.getApplicationNameByAppKey(appKey);

        String key = getKey(userName, applicationName);

        UserInfoGenericV3 userInfoGeneric = cacheUserInfoGenericV3.getIfPresent(key);
        if (userInfoGeneric == null) {
            userInfoGeneric = loadUserInfoGenericV3(userName, applicationName, childAppName);
            cacheUserInfoGenericV3.put(key, userInfoGeneric);
        }
        log.debug(" userInfoGenericV3 {}", userInfoGeneric);
        return ResponseEntity.ok(userInfoGeneric);
    }

    @GetMapping(value = "/enabledAttributes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "userId", description = "userId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "operationId", description = "operationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "toDate", description = "toDate", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "fromDate", description = "fromDate", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "timeOffset", description = "timeOffset", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "localeDateFormat", description = "localeDateFormat", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY)})
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getAllEnabledAttributes(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("list; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();


        List<Map<String, Object>> resultList = getQueryBuilderData("getAllEnabledAttributes", uriInfo);
        if (resultList != null && !resultList.isEmpty()) {
            for (Map<String, Object> map : resultList) {
                if (!result.containsKey(map.get("type"))) {
                    result.put(map.get("type").toString(), new LinkedList<>());
                }
                result.get(map.get("type").toString()).add(map);
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String[]> getUserRoles(@RequestParam(name = "userName", required = false) String userName, @RequestParam(name = "applicationName", required = false) String applicationName) {
        log.trace("getUserRoles; userName={}; applicationName={}", userName, applicationName);

        //check for user present using getUserInfo, it throws exception, if not found.
        getUserInfo(userName, applicationName, null);
        List<String> roles = userDal.getUserRoles(userName, applicationName);
        if (roles == null || roles.isEmpty()) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ROLES_NOT_FOUND);
            errorInfo.add("userName", userName);
            errorInfo.add("applicationName", applicationName);
            log.warn("getUserRoles; {}", errorInfo);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        return ResponseEntity.ok(roles.toArray(new String[0]));
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = "/appUsers", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userId", description = "userId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "operationId", description = "operationId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "toDate", description = "toDate", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "fromDate", description = "fromDate", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "timeOffset", description = "timeOffset", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "localeDateFormat", description = "localeDateFormat", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY)})
    public ResponseEntity<String> getAppUsers(@RequestParam(name = "applicationName", required = false) String applicationName, HttpServletRequest servletRequest) {
        log.trace("getAppUsers; applicationName={}", applicationName);
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("getAppUsers; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        //check for application name.
        if (applicationName == null || applicationName.isEmpty()) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.APPLICATION_NOT_FOUND);
            log.warn("getAppUsers; {}", errorInfo);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }

        List<Map<String, Object>> appuUsersList = getQueryBuilderData("appUsers", uriInfo);

        if (appuUsersList == null || appuUsersList.isEmpty()) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.APPLICATION_USERS_NOT_FOUND);
            log.warn("getAppUsers; {}", errorInfo);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        Map<String, Object> tempMap = new HashMap<>();
        for (Map<String, Object> element : appuUsersList) {
            if (tempMap.containsKey(element.get("userName").toString()) && element.get("roleName") != null && !((String) element.get("roleName")).isEmpty()) {
                ((List<String>) ((Map<String, Object>) tempMap.get(element.get("userName"))).get("roleNames")).add(element.get("roleName").toString());
                element.put("roleNames", element.get("roleNames"));
                element.remove("roleName");
            } else if (element.get("roleName") != null && !((String) element.get("roleName")).isEmpty()) {
                List<String> roles = new LinkedList<String>();
                roles.add(element.get("roleName").toString());
                element.put("roleNames", roles);
                tempMap.put(element.get("userName").toString(), element);
                element.remove("roleName");
            } else {
                tempMap.put(element.get("userName").toString(), element);
            }
        }
        return ResponseEntity.ok().body(new Gson().toJson(tempMap.values()));
    }

    @GetMapping(value = "/apps", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = RBACUtil.APP_KEY_IDENTIFIER_HEADER, description = RBACUtil.APP_KEY_IDENTIFIER_HEADER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = RBACUtil.TICKET_HEADER_IDENTIFIER, description = RBACUtil.TICKET_HEADER_IDENTIFIER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<ApplicationInfo[]> getUserAuthorizedApps(@RequestParam(name = "userName", required = false) String userName, HttpServletRequest httpRequest, @RequestHeader HttpHeaders headers) {
        Integer userId = Lookup.getUserId(userName);
        if (userId == null || userId == -1) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.USER_NOT_FOUND);
            errorInfo.add("userName", userName);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        List<Application> list = applicationDal.getUserAuthorizedApps(userName);
        if (list == null || list.isEmpty()) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.NO_AUTH_APPS_FOUND);
            errorInfo.add("userName", userName);
            log.warn("getUserAuthorizedApps; {}", errorInfo);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        List<ApplicationInfo> resultList = new ArrayList<ApplicationInfo>();
        String appKeyToExclude = RBACUtil.getAppKeyFromHeader(headers);
        //find the tag using ticket or appKey from where the request for switcher originated
        AppUrlData appUrlData = null;
        if (headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER) != null && !headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER).isEmpty()) {
            appUrlData = loginService.getAppUrlDataByTicket(headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER).get(0));
        } else {
            appUrlData = loginService.getFirstAppUrlDataByUserNameAndAppKey(userName, appKeyToExclude);
        }
        List<Integer> revokedChildApplicationIds = checkForRevokedApplicationAccess(headers, userName);
        for (Application application : list) {
            if (application.getChildApplications() != null && !application.getChildApplications().isEmpty()) {
                for (ChildApplication childApp : application.getChildApplications()) {
                    if (childApp.getAppType() != null && ChildApplication.isNonSSO(childApp.getAppType()) && !deploymentUtil.getIncludeNonSSOAppsInSwitcher()) {
                        continue;
                    }
                    if (childApp.getAppType() != null && ChildApplication.isNative(childApp.getAppType())) {
                        continue;
                    }
                    if (appKeyToExclude != null && !appKeyToExclude.isEmpty() && childApp.getAppKey() != null && childApp.getAppKey().equalsIgnoreCase(appKeyToExclude)) {
                        continue;
                    }
                    if (childApp.getAppKey() != null && !childAppPermValidatorUtil.validate(childApp.getAppKey(), userName)) {
                        continue;
                    }
                    if (revokedChildApplicationIds != null && revokedChildApplicationIds.contains(childApp.getChildApplicationId())) {
                        continue;
                    }
                    ApplicationInfo applicationInfo = new ApplicationInfo();
                    if (childApp.getDescription() != null && !childApp.getDescription().isEmpty()) {
                        applicationInfo.setDescription(childApp.getDescription());
                    } else {
                        applicationInfo.setDescription(application.getDescription());
                    }
                    applicationInfo.setHomeUrl(childApp.getFirstHomeUrl());
                    if (appUrlData != null) {
                        String homeUrlByTag = childApp.getHomeUrlByTag(appUrlData.getTag());
                        if (homeUrlByTag != null && !homeUrlByTag.isEmpty()) {
                            applicationInfo.setHomeUrl(homeUrlByTag);
                        }
                    }
                    applicationInfo.setName(childApp.getChildApplicationName());
                    resultList.add(applicationInfo);
                }

            }
        }

        ApplicationInfo[] array = new ApplicationInfo[resultList.size()];
        resultList.toArray(array);
        return ResponseEntity.ok().body(array);
    }

    @GetMapping(value = "/appsDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = RBACUtil.APP_KEY_IDENTIFIER_HEADER, description = RBACUtil.APP_KEY_IDENTIFIER_HEADER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = RBACUtil.TICKET_HEADER_IDENTIFIER, description = RBACUtil.TICKET_HEADER_IDENTIFIER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<ApplicationInfoDetails[]> getUserAuthorizedAppsDetails(@RequestParam(name = "userName", required = false) String userName, HttpServletRequest httpRequest, @RequestHeader HttpHeaders headers) {
        Integer userId = Lookup.getUserId(userName);
        if (userId == null || userId == -1) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.USER_NOT_FOUND);
            errorInfo.add("userName", userName);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        List<Application> list = applicationDal.getUserAuthorizedApps(userName);
        if (list == null || list.isEmpty()) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.NO_AUTH_APPS_FOUND);
            errorInfo.add("userName", userName);
            log.warn("getUserAuthorizedAppsDetails; {}", errorInfo);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        List<ApplicationInfoDetails> resultList = new ArrayList<ApplicationInfoDetails>();
        String appKeyToExclude = RBACUtil.getAppKeyFromHeader(headers);
        //find the tag using ticket or appKey from where the request for switcher originated
        AppUrlData appUrlData = null;
        if (headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER) != null && !headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER).isEmpty()) {
            appUrlData = loginService.getAppUrlDataByTicket(headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER).get(0));
        } else {
            appUrlData = loginService.getFirstAppUrlDataByUserNameAndAppKey(userName, appKeyToExclude);
        }
//        List<Integer> revokedChildApplicationIds = checkForRevokedApplicationAccess(headers,userName);
        for (Application application : list) {
            if (application.getChildApplications() != null && !application.getChildApplications().isEmpty()) {
                for (ChildApplication childApp : application.getChildApplications()) {
                    if (childApp.getAppType() != null && ChildApplication.isNonSSO(childApp.getAppType()) && !deploymentUtil.getIncludeNonSSOAppsInSwitcher()) {
                        continue;
                    }
                    if (childApp.getAppType() != null && ChildApplication.isNative(childApp.getAppType())) {
                        continue;
                    }
                    if (appKeyToExclude != null && !appKeyToExclude.isEmpty() && childApp.getAppKey() != null && childApp.getAppKey().equalsIgnoreCase(appKeyToExclude)) {
                        continue;
                    }
                    if (childApp.getAppKey() != null && !childAppPermValidatorUtil.validate(childApp.getAppKey(), userName)) {
                        continue;
                    }
//					if (revokedChildApplicationIds != null && revokedChildApplicationIds.contains(childApp.getChildApplicationId())) {
//						 continue;
//					}
                    ApplicationInfoDetails applicationInfo = new ApplicationInfoDetails();
                    if (childApp.getDescription() != null && !childApp.getDescription().isEmpty()) {
                        applicationInfo.setDescription(childApp.getDescription());
                    } else {
                        applicationInfo.setDescription(application.getDescription());
                    }
                    applicationInfo.setHomeUrl(childApp.getFirstHomeUrl());
                    applicationInfo.addAdditionalData("appKey", childApp.getAppKey());
                    applicationInfo.addAdditionalData("parentApplicationName", application.getName());
                    if (appUrlData != null) {
                        String homeUrlByTag = childApp.getHomeUrlByTag(appUrlData.getTag());
                        if (homeUrlByTag != null && !homeUrlByTag.isEmpty()) {
                            applicationInfo.setHomeUrl(homeUrlByTag);
                        }
                    }
                    applicationInfo.setName(childApp.getChildApplicationName());
                    resultList.add(applicationInfo);
                }

            }
        }

        ApplicationInfoDetails[] array = new ApplicationInfoDetails[resultList.size()];
        resultList.toArray(array);
        return ResponseEntity.ok().body(array);
    }

    @GetMapping(value = "/appsDetailsInJson", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = RBACUtil.APP_KEY_IDENTIFIER_HEADER, description = RBACUtil.APP_KEY_IDENTIFIER_HEADER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = RBACUtil.TICKET_HEADER_IDENTIFIER, description = RBACUtil.TICKET_HEADER_IDENTIFIER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<SwitcherApplicationInfo[]> getUserAuthorizedDetails(@RequestParam(name = "userName", required = false) String userName, HttpServletRequest httpRequest, @RequestHeader HttpHeaders headers) {
        String rbacHomeUrl = null;
        Integer userId = Lookup.getUserId(userName);
        if (userId == null || userId == -1) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.USER_NOT_FOUND);
            errorInfo.add("userName", userName);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        List<Application> list = applicationDal.getUserAuthorizedApps(userName);
        if (list == null || list.isEmpty()) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.NO_AUTH_APPS_FOUND);
            errorInfo.add("userName", userName);
            log.warn("getUserAuthorizedAppsDetails; {}", errorInfo);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        List<SwitcherApplicationInfo> resultList = new ArrayList<SwitcherApplicationInfo>();
        String appKeyToExclude = RBACUtil.getAppKeyFromHeader(headers);
        //find the tag using ticket or appKey from where the request for switcher originated
        AppUrlData appUrlData = null;
        if (headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER) != null && !headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER).isEmpty()) {
            appUrlData = loginService.getAppUrlDataByTicket(headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER).get(0));
        } else {
            appUrlData = loginService.getFirstAppUrlDataByUserNameAndAppKey(userName, appKeyToExclude);
        }


        rbacHomeUrl = getRBACHomeURL(appUrlData);
        List<Integer> revokedChildApplicationIds = checkForRevokedApplicationAccess(headers, userName);
        for (Application application : list) {
            if (application.getChildApplications() != null && !application.getChildApplications().isEmpty()) {
                for (ChildApplication childApp : application.getChildApplications()) {
                    if (childApp.getAppType() != null && ChildApplication.isNonSSO(childApp.getAppType()) && !deploymentUtil.getIncludeNonSSOAppsInSwitcher()) {
                        continue;
                    }
                    if (childApp.getAppType() != null && ChildApplication.isNative(childApp.getAppType())) {
                        continue;
                    }
                    if (appKeyToExclude != null && !appKeyToExclude.isEmpty() && childApp.getAppKey() != null && childApp.getAppKey().equalsIgnoreCase(appKeyToExclude)) {
                        continue;
                    }
                    if (childApp.getAppKey() != null && !childAppPermValidatorUtil.validate(childApp.getAppKey(), userName)) {
                        continue;
                    }
                    if (revokedChildApplicationIds != null && revokedChildApplicationIds.contains(childApp.getChildApplicationId())) {
                        continue;
                    }
                    SwitcherApplicationInfo applicationInfo = new SwitcherApplicationInfo();
                    applicationInfo.setHomeUrl(childApp.getFirstHomeUrl());
                    applicationInfo.setApplicationId(childApp.getApplication().getApplicationId().toString());
                    applicationInfo.setAppKey(childApp.getAppKey());
                    String appLogoImageUrl = rbacHomeUrl + "switcherImage/" + childApp.getAppKey().toLowerCase() + ".png";
                    applicationInfo.setAppLogoImageUrl(appLogoImageUrl);
                    if (appUrlData != null) {
                        String homeUrlByTag = childApp.getHomeUrlByTag(appUrlData.getTag());
                        if (homeUrlByTag != null && !homeUrlByTag.isEmpty()) {
                            applicationInfo.setHomeUrl(homeUrlByTag);
                        }
                    }
                    if (applicationInfo.getHomeUrl() == null) {
                        applicationInfo.setHomeUrl("");

                    }
                    applicationInfo.setName(childApp.getChildApplicationName());
                    // RBAC-1859: Start
                    if (deploymentUtil.isValidateApplicationTagWithLoggedInTag()) {
                        Set<AppUrlData> appUrlDataofOthers = childApp.getAppUrlDataSet();
                        Boolean addToList = Boolean.FALSE;
                        for (AppUrlData appUrls : appUrlDataofOthers) {
                            if (appUrls.getTag() != null && !appUrls.getTag().isEmpty() && appUrlData != null && appUrlData.getTag() != null && !appUrlData.getTag().isEmpty() && appUrls.getTag().toLowerCase().contains(appUrlData.getTag().toLowerCase())) {
                                addToList = Boolean.TRUE;
                                break;
                            }
                        }
                        if (addToList) resultList.add(applicationInfo);    // RBAC-1859: END
                    } else resultList.add(applicationInfo);

                }
            }
        }
        SwitcherApplicationInfo[] array = new SwitcherApplicationInfo[resultList.size()];
        resultList.toArray(array);
        return ResponseEntity.ok().body(array);
    }

    private String getRBACHomeURL(AppUrlData appUrlData) {
        String homeURL = null;
        String rbacHomeUrl = deploymentUtil.getChangePasswordUrl();
        log.debug("rbacHomeUrl {}", rbacHomeUrl);
        if (rbacHomeUrl != null) {
            homeURL = rbacHomeUrl.split("changePassword")[0]; // to get SSO web URL
        } else {
            if (appUrlData != null && appUrlData.getTag() != null && !appUrlData.getTag().isEmpty()) {
                ChildApplication childApp = Lookup.getChildApplicationByAppKeyNew("RBAC");
                Set<AppUrlData> appUrlDataofOthers = childApp.getAppUrlDataSet();
                for (AppUrlData appUrls : appUrlDataofOthers) {
                    if (appUrls.getTag() != null && !appUrls.getTag().isEmpty() && appUrls.getTag().equalsIgnoreCase(appUrlData.getTag())) {
                        homeURL = appUrls.getHomeUrl();
                        break;
                    }
                }
                if (homeURL == null) {
                    homeURL = appUrlData.getHomeUrl();
                }
                if (homeURL != null && !homeURL.isEmpty()) {
                    rbacHomeUrl = homeURL.split("rbac")[0];
                    log.debug("rbacHomeUrl {}", rbacHomeUrl);
                    homeURL = rbacHomeUrl + "sso/rest/";
                }
            }
        }
        return homeURL;
    }

    @SuppressWarnings("rawtypes")
    @GetMapping(value = "/appsDetailsInHtml", produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = RBACUtil.APP_KEY_IDENTIFIER_HEADER, description = RBACUtil.APP_KEY_IDENTIFIER_HEADER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = RBACUtil.TICKET_HEADER_IDENTIFIER, description = RBACUtil.TICKET_HEADER_IDENTIFIER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<List<Map>> getUserAuthorizedAppsDetailsInHtml(@RequestParam(name = "userName", required = false) String userName, HttpServletRequest httpRequest, @RequestHeader HttpHeaders headers) {
        Integer userId = Lookup.getUserId(userName);
        if (userId == null || userId == -1) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.USER_NOT_FOUND);
            errorInfo.add("userName", userName);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        List<Application> list = applicationDal.getUserAuthorizedApps(userName);
        if (list == null || list.isEmpty()) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.NO_AUTH_APPS_FOUND);
            errorInfo.add("userName", userName);
            log.warn("getUserAuthorizedAppsDetails; {}", errorInfo);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        List<ApplicationInfoDetails> resultList = new ArrayList<ApplicationInfoDetails>();
        String appKeyToExclude = RBACUtil.getAppKeyFromHeader(headers);
        //find the tag using ticket or appKey from where the request for switcher originated
        AppUrlData appUrlData = null;
        if (headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER) != null && !headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER).isEmpty()) {
            appUrlData = loginService.getAppUrlDataByTicket(headers.get(RBACUtil.TICKET_HEADER_IDENTIFIER).get(0));
        } else {
            appUrlData = loginService.getFirstAppUrlDataByUserNameAndAppKey(userName, appKeyToExclude);
        }
        List<Integer> revokedChildApplicationIds = checkForRevokedApplicationAccess(headers, userName);
        for (Application application : list) {
            if (application.getChildApplications() != null && !application.getChildApplications().isEmpty()) {
                for (ChildApplication childApp : application.getChildApplications()) {
                    if (childApp.getAppType() != null && ChildApplication.isNonSSO(childApp.getAppType()) && !deploymentUtil.getIncludeNonSSOAppsInSwitcher()) {
                        continue;
                    }
                    if (childApp.getAppType() != null && ChildApplication.isNative(childApp.getAppType())) {
                        continue;
                    }
                    if (appKeyToExclude != null && !appKeyToExclude.isEmpty() && childApp.getAppKey() != null && childApp.getAppKey().equalsIgnoreCase(appKeyToExclude)) {
                        continue;
                    }
                    if (childApp.getAppKey() != null && !childAppPermValidatorUtil.validate(childApp.getAppKey(), userName)) {
                        continue;
                    }
                    if (revokedChildApplicationIds != null && revokedChildApplicationIds.contains(childApp.getChildApplicationId())) {
                        continue;
                    }
                    ApplicationInfoDetails applicationInfo = new ApplicationInfoDetails();
                    if (childApp.getDescription() != null && !childApp.getDescription().isEmpty()) {
                        applicationInfo.setDescription(childApp.getDescription());
                    } else {
                        applicationInfo.setDescription(application.getDescription());
                    }
                    applicationInfo.setHomeUrl(childApp.getFirstHomeUrl());
                    applicationInfo.addAdditionalData("appKey", childApp.getAppKey());
                    if (appUrlData != null) {
                        String homeUrlByTag = childApp.getHomeUrlByTag(appUrlData.getTag());
                        if (homeUrlByTag != null && !homeUrlByTag.isEmpty()) {
                            applicationInfo.setHomeUrl(homeUrlByTag);
                        }
                    }
                    applicationInfo.setName(childApp.getChildApplicationName());
                    // RBAC-1859: Start
                    if (deploymentUtil.isValidateApplicationTagWithLoggedInTag()) {
                        Set<AppUrlData> appUrlDataofOthers = childApp.getAppUrlDataSet();
                        Boolean addToList = Boolean.FALSE;
                        for (AppUrlData appUrls : appUrlDataofOthers) {
                            if (appUrls.getTag() != null && !appUrls.getTag().isEmpty() && appUrlData != null && appUrlData.getTag() != null && !appUrlData.getTag().isEmpty() && appUrls.getTag().toLowerCase().contains(appUrlData.getTag().toLowerCase())) {
                                addToList = Boolean.TRUE;
                                break;
                            }
                        }
                        if (addToList) resultList.add(applicationInfo);
                    } else// RBAC-1859: END
                        resultList.add(applicationInfo);

                }

            }
        }

        ApplicationInfoDetails[] array = new ApplicationInfoDetails[resultList.size()];
        resultList.toArray(array);
        String htmlString = switcherHtmlUtil.getSwitcherHtml(array);
        List<Map> htmlResultList = new ArrayList<Map>();
        Map<String, Object> htmlData = new LinkedHashMap<String, Object>();

        htmlData.put("html", htmlString);
        htmlData.put("additionalInfo", new HashMap());
        htmlResultList.add(htmlData);
        return ResponseEntity.ok().body(htmlResultList);
    }

    private String getKey(String userName, String applicationName) {
        String sb = "userName=" + userName + "; applicationName=" + applicationName;
        return sb;
    }

    private UserInfo loadUserInfo(String userName, String applicationName, String ticket) {
        String preferredLanguage = RBACUtil.DEFAULT_LOCALE_ENGLISH;
        User user = loadUser(userName);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setUserName(userName);
        userInfo.setFirstName(user.getFirstName());
        userInfo.setLastName(user.getLastName());
        userInfo.setDisplayName(getDisplayName(user));

        if (applicationName != null && !applicationName.isEmpty()) {
            userInfo.setApplicationName(applicationName);
            //userInfo.setPermissions(Sets.newTreeSet(userDal.getUserPermissions(userName, applicationName)));
            userInfo.setPermissions(userDal.getUserTargetOperations(userName, applicationName));
            if (applicationName.equalsIgnoreCase(RBACUtil.RBAC_UAM_APPLICATION_NAME)) {
                userInfo.setScopes(userDal.getUserScopes(userName, applicationName, true));
            } else {
                userInfo.setScopes(userDal.getUserScopes(userName, applicationName, false));
            }
            userInfo.setVariables(userDal.getUserVariables(userName, applicationName));
            if (ticket != null) {
                Session dbSession = sessionDal.getSessionsByUserName(userName).stream().filter(session -> session.getTicket().equals(ticket)).findAny().orElse(null);
                if (dbSession != null && dbSession.getChildApplicationName() != null) {
                    userInfo.setApplicationContextName(dbSession.getChildApplicationName());
                    String appKey = Lookup.getAppKeyByChildAppId(dbSession.getChildApplicationId());
                    log.debug("appKey {}", appKey);
                    if (appKey != null) {
                        try {
                            ApplicationCulture applicationCulture = cultureDal.getByApplicationKey(appKey);
                            if (applicationCulture != null) {
                                Boolean isValidCultureByApplication = RBACUtil.isSelectedLanguageValidForApplication(applicationCulture.getSupportedCultures(), user.getPreferredLanguage());
                                if (isValidCultureByApplication) preferredLanguage = user.getPreferredLanguage();
                                else {
                                    log.info("User Preferred Language {} is not supported for application {}. Falling back to default language {}", user.getPreferredLanguage(), dbSession.getChildApplicationName(), preferredLanguage);
                                }
                            }
                        } catch (Exception e) {
                            log.error("loadUserInfo: Exception {}. Falling back to default language {}", e.getMessage(), preferredLanguage);
                        }
                    }
                }
            }
            userInfo.setPreferredLanguage(preferredLanguage);
            userInfo.setTimezone(user.getTimeZone());

            Date lastLoginTimeInUTC = user.getLastSuccessfulLoginTime();
            if (lastLoginTimeInUTC != null) {
                Integer timeOffset = 0;
                TimeZoneMaster tm = Lookup.getTimeZoneFromTimeZoneName(userInfo.getTimezone());
                if (tm != null && tm.getTimeOffsetMinute() != null) timeOffset = tm.getTimeOffsetMinute();
                Locale locale = new Locale(preferredLanguage);
                log.debug("locale {}", locale);
                DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, locale);
                String pattern = ((SimpleDateFormat) formatter).toPattern();
                log.debug("pattern {}", pattern);
                LocalDateTime convertedToOffset = RBACUtil.convertDateUsingOffset(lastLoginTimeInUTC, timeOffset);
                DateTimeFormatter deTimeFormatter = DateTimeFormatter.ofPattern(pattern, locale);
                String dateFormatted = deTimeFormatter.format(convertedToOffset);
                userInfo.setLastSuccessfulLoginTimeDisplay(dateFormatted);
                userInfo.setDateTimeDisplayFormat(pattern);
            }

        }
        return userInfo;
    }

    private UserInfoDetails loadUserInfoDetails(String userName, String applicationName) {
        User user = loadUser(userName);
        UserInfoDetails userInfoDetails = new UserInfoDetails();
        userInfoDetails.setUserId(user.getUserId());
        userInfoDetails.setUserName(userName);
        userInfoDetails.setFirstName(user.getFirstName());
        userInfoDetails.setLastName(user.getLastName());
        userInfoDetails.setDisplayName(getDisplayName(user));

        if (applicationName != null && !applicationName.isEmpty()) {
            userInfoDetails.setApplicationName(applicationName);
            //userInfo.setPermissions(Sets.newTreeSet(userDal.getUserPermissions(userName, applicationName)));
            userInfoDetails.setPermissions(userDal.getUserTargetOperations(userName, applicationName));
            if (applicationName.equalsIgnoreCase(RBACUtil.RBAC_UAM_APPLICATION_NAME)) {
                userInfoDetails.setScopes(userDal.getUserScopes(userName, applicationName, true));
            } else {
                userInfoDetails.setScopes(userDal.getUserScopes(userName, applicationName, false));
            }
            userInfoDetails.setVariables(userDal.getUserVariables(userName, applicationName));


        }

        if (user.getGroupId() != null) {
            userInfoDetails.setGroup(groupDal.getById(user.getGroupId()).getName());
            userInfoDetails.setRoles(userDal.getUserRoles(userName, applicationName));
        }
        userInfoDetails.setLastSuccessfulLoginTime(user.getLastSuccessfulLoginTime());
        return userInfoDetails;
    }

    private UserInfoRBAC loadUserInfoRBAC(String userName, String applicationName) {
        User user = loadUser(userName);
        UserInfoRBAC userInfoRBAC = new UserInfoRBAC();
        userInfoRBAC.setUserId(user.getUserId());
        userInfoRBAC.setUserName(userName);
        userInfoRBAC.setFirstName(user.getFirstName());
        userInfoRBAC.setLastName(user.getLastName());
        userInfoRBAC.setDisplayName(getDisplayName(user));

        if (applicationName != null && !applicationName.isEmpty()) {
            userInfoRBAC.setApplicationName(applicationName);
            //userInfo.setPermissions(Sets.newTreeSet(userDal.getUserPermissions(userName, applicationName)));
            userInfoRBAC.setPermissions(userDal.getUserTargetOperations(userName, applicationName));
            if (applicationName.equalsIgnoreCase(RBACUtil.RBAC_UAM_APPLICATION_NAME)) {
                userInfoRBAC.setScopes(userDal.getUserScopes(userName, applicationName, true));
            } else {
                userInfoRBAC.setScopes(userDal.getUserScopes(userName, applicationName, false));
            }
            userInfoRBAC.setVariables(userDal.getUserVariables(userName, applicationName));
        }

        if (user.getGroupId() != null) {
            userInfoRBAC.setGroup(Lookup.getGroupName(user.getGroupId()));
            userInfoRBAC.setGroupId(user.getGroupId());
        }
        userInfoRBAC.setLastSuccessfulLoginTime(user.getLastSuccessfulLoginTime());
        return userInfoRBAC;
    }

    private UserInfoGenericV2 loadUserInfoGenericV2(String userName, String applicationName) {
        User user = loadUser(userName);
        UserInfoGenericV2 userInfoGeneric = new UserInfoGenericV2();
        userInfoGeneric.setUserId(user.getUserId());
        userInfoGeneric.setUserName(userName);
        userInfoGeneric.setFirstName(user.getFirstName());
        userInfoGeneric.setLastName(user.getLastName());
        userInfoGeneric.setDisplayName(getDisplayName(user));
        if (applicationName != null && !applicationName.isEmpty()) {
            userInfoGeneric.setApplicationName(applicationName);
            //userInfo.setPermissions(Sets.newTreeSet(userDal.getUserPermissions(userName, applicationName)));
            userInfoGeneric.setPermissions(userDal.getUserTargetOperations(userName, applicationName));
            if (applicationName.equalsIgnoreCase(RBACUtil.RBAC_UAM_APPLICATION_NAME)) {
                userInfoGeneric.setScopes(userDal.getUserScopes(userName, applicationName, true));
            } else {
                userInfoGeneric.setScopes(userDal.getUserScopes(userName, applicationName, false));
            }
            userInfoGeneric.setVariables(userDal.getUserVariables(userName, applicationName));
        }

        if (user.getGroupId() != null) {
            userInfoGeneric.setGroupId(user.getGroupId());
            userInfoGeneric.setGroup(groupDal.getById(user.getGroupId()).getName());
            userInfoGeneric.setRoles(userDal.getUserRoles(userName, applicationName));
        }
        userInfoGeneric.setLastSuccessfulLoginTime(user.getLastSuccessfulLoginTime());
        userInfoGeneric.addAdditionalData("attributes", userDal.getUserAttributes(userName));
        userInfoGeneric.addAdditionalData("inListScopes", userDal.getUserInListScopesDetails(userName, applicationName));
        Long tenantId = Lookup.getTenantIdByOrganizationId(user.getOrganizationId());
        if (tenantId != null) {
            userInfoGeneric.setTenantId(tenantId);
            userInfoGeneric.setTenantName(Lookup.getTenantNameById(tenantId));
            userInfoGeneric.setTenantLogoPath(RBACUtil.getTenantLogoUrl(tenantId));
            Tenant tenant = tenantDal.getById(tenantId);
            userInfoGeneric.setTenantType(tenant.getTenantType().getCodeValue());
            userInfoGeneric.setTenantSubType(tenant.getTenantSubType().getCodeValue());
            /* RBAC-1475 MakerChecker Start */
            userInfoGeneric.setMakerCheckerEnabledInTenant(tenant.isMakerCheckerEnabled() && deploymentUtil.getIsMakercheckerActivated());
            /* RBAC-1475 MakerChecker End */
        }
        userInfoGeneric.setSystemMultiTenant(environmentUtil.isMultiTenantEnvironment());
        return userInfoGeneric;
    }

    private UserInfoGenericV3 loadUserInfoGenericV3(String userName, String applicationName, String childAppName) {
        User user = loadUser(userName);
        String preferredLanguage = RBACUtil.DEFAULT_LOCALE_ENGLISH;
        UserInfoGenericV3 userInfoGeneric = new UserInfoGenericV3().toUserInfoDetailsV3(loadUserInfoGenericV2(userName, applicationName));
        try {
            if (childAppName == null) {
                log.debug("childAppName is null ");
                Application application = applicationDal.getByName(applicationName);
                if (application != null && !application.getChildApplications().isEmpty()) {
                    log.debug("childAppName from application {}", application.getChildApplications());
                    childAppName = application.getChildApplications().iterator().next().getChildApplicationName();
                }
                log.debug("childAppName from application {}", childAppName);
            }

            String appKey = Lookup.getAppKeyByChildAppId(Lookup.getChildApplicationIdByName(childAppName));
            log.debug("appKey {}", appKey);
            if (appKey != null) {
                try {
                    ApplicationCulture applicationCulture = cultureDal.getByApplicationKey(appKey);
                    if (applicationCulture != null) {
                        Boolean isValidCultureByApplication = RBACUtil.isSelectedLanguageValidForApplication(applicationCulture.getSupportedCultures(), user.getPreferredLanguage());
                        if (isValidCultureByApplication) preferredLanguage = user.getPreferredLanguage();
                        else {
                            log.info("User Preferred Language {} is not supported for application {}. Falling back to default language {}", user.getPreferredLanguage(), childAppName, preferredLanguage);
                        }
                    }
                } catch (Exception e) {
                    log.error("loadUserInfoGenericV3; Exception {}. Falling back to default language {}", e.getMessage(), preferredLanguage);
                }
            }
        } catch (Exception e) {
            log.error("Exception: loadUserInfoGenericV3: {}", e);

        }

        userInfoGeneric.setTimezone(user.getTimeZone());
        userInfoGeneric.setPreferredLanguage(preferredLanguage);

        Date lastLoginTimeInUTC = userInfoGeneric.getLastSuccessfulLoginTime();
        if (lastLoginTimeInUTC != null) {
            Integer timeOffset = 0;
            TimeZoneMaster tm = Lookup.getTimeZoneFromTimeZoneName(userInfoGeneric.getTimezone());
            if (tm != null && tm.getTimeOffsetMinute() != null) timeOffset = tm.getTimeOffsetMinute();

            Locale locale = new Locale(preferredLanguage);
            log.debug("locale {}", locale);
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, locale);
            String pattern = ((SimpleDateFormat) formatter).toPattern();
            log.debug("pattern {}", pattern);
            LocalDateTime convertedToOffset = RBACUtil.convertDateUsingOffset(lastLoginTimeInUTC, timeOffset);
            DateTimeFormatter deTimeFormatter = DateTimeFormatter.ofPattern(pattern, locale);
            String dateFormatted = deTimeFormatter.format(convertedToOffset);
            userInfoGeneric.setLastSuccessfulLoginTimeDisplay(dateFormatted);
            userInfoGeneric.setDateTimeDisplayFormat(pattern);
        }
        return userInfoGeneric;

    }

    private User loadUser(String userName) {
        User user = userDal.getByUserName(userName);
        if (user == null) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.USER_NOT_FOUND);
            errorInfo.add("userName", userName);
            log.warn("loadUserInfo; {}", errorInfo);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        if (!user.getIsEnabled()) {
            ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.USER_DISABLED);
            errorInfo.add("userName", userName);
            log.warn("loadUserInfo; {}", errorInfo);
            throw new WebApplicationException(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo).toString());
        }
        return user;
    }

    private List<Map<String, Object>> getQueryBuilderData(String scopeName, MultivaluedMap<String, String> uriInfo) {
        ScopeConstraint scopeConstraint = scopeConstraintDal.getByScopeName(scopeName);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        List<Map<String, Object>> queryDataList = null;
        Map<String, String> filters = optionFilter.getFilters();
        if (scopeConstraint != null) {
            String sql = scopeConstraint.getSqlQuery();
            queryDataList = externalDbDal.getCustomData(scopeConstraint.getApplicationName(), sql, filters, scopeName, null);
        }
        return queryDataList;
    }

    private List<Integer> checkForRevokedApplicationAccess(HttpHeaders headers, String userName) {
//		List<Application> listInfo = new ArrayList<Application>();
        Options options = new Options();
        if (headers.get(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY) != null && !headers.get(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY).isEmpty()) {

            OptionFilter optionFilter = new OptionFilter();
            optionFilter.addFilter(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, headers.get(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY).get(0));
            options = new Options(optionFilter);
            List<Integer> revokedApps = applicationDal.getRevokedChildApplicationIds(options);
            log.debug("revokedChildApps {}", revokedApps);
            return revokedApps;
        } else {
            Map<String, String> scopeMap = userDal.getUserScopes(userName, null, false);
            if (scopeMap != null) {
                String scopeQuery = scopeMap.get(RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS);
                if (scopeQuery != null) {
                    OptionFilter optionFilter = new OptionFilter();
                    optionFilter.addFilter(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
                    options = new Options(optionFilter);
                    List<Integer> revokedApps = applicationDal.getRevokedChildApplicationIds(options);
                    log.debug("revokedChildApps {}", revokedApps);
                    return revokedApps;
                }
            }
        }
        return null;
    }

}
