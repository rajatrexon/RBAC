package com.esq.rbac.service.loginservice.rest;

import com.esq.rbac.service.application.applicationmaintenance.util.ApplicationDownInfo;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.loginlog.domain.LoginType;
import com.esq.rbac.service.loginservice.embedded.*;
import com.esq.rbac.service.loginservice.service.LoginService;
import com.esq.rbac.service.loginservice.util.TwoFactorAlertDal;
import com.esq.rbac.service.lookup.ApplicationMaintenanceCache;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.sessionregistry.registry.SessionRegistry;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.util.RBACUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/login")
@Slf4j
public class LoginRest {

    private final LoginService loginService;
    //RBAC-1562 Starts
    private final TwoFactorAlertDal twoFactorAlertDal;
    private SessionRegistry sessionRegistry;
    private final Validator validator;

    public LoginRest(LoginService loginService, Validator validator, TwoFactorAlertDal twoFactorAlertDal) {
        this.loginService = loginService;
        this.validator = validator;
        this.twoFactorAlertDal = twoFactorAlertDal;
    }


    //RBAC-1562 Ends


    @PostMapping(value = "/appInfo", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<AppInfoResponse> getAppInfo(@RequestBody AppInfoRequest request) throws Exception {
        log.trace("getAppInfo; {}", request);
        return ResponseEntity.ok().body(loginService.getAppInfo(request));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws Exception {
        log.trace("login; {}", request);
        validate(request);
        isAppUnderMaintenance(request);
        return ResponseEntity.ok().body(loginService.login(request));
    }


    @PostMapping(value = "/loginWindows", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginResponse> loginWindows(@RequestBody LoginRequest request) throws Exception {
        log.trace("loginWindows; {}", request);
        validate(request);
        isAppUnderMaintenance(request);
        return ResponseEntity.ok().body(loginService.loginWindows(request));
    }


    @PostMapping(value = "/loginAuto", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginResponse> loginAuto(@RequestBody LoginRequest request) throws Exception {
        log.trace("loginAuto; {}", request);
        validate(request);
        isAppUnderMaintenance(request);
        return ResponseEntity.ok().body(loginService.loginAuto(request));
    }

    @PostMapping(value = "/loginGenericHeaderAuth", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginResponse> loginGenericHeaderAuth(@RequestBody LoginRequest request) throws Exception {
        log.trace("loginGenericHeaderAuth; {}", request);
        validate(request);
        isAppUnderMaintenance(request);
        return ResponseEntity.ok().body(loginService.genericHeaderLoginService(request));
    }


    @PostMapping(value = "/loginSiteMinder", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginResponse> loginSiteMinder(@RequestBody LoginRequest request) throws Exception {
        log.trace("loginSiteMinder; {}", request);
        validate(request);
        isAppUnderMaintenance(request);
        return ResponseEntity.ok().body(loginService.loginSiteMinder(request));
    }

    @PostMapping(value = "/loginForgeRock", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginResponse> loginForgeRock(@RequestBody LoginRequest request) throws Exception {
        log.trace("loginForgeRock; {}", request);
        validate(request);
        isAppUnderMaintenance(request);
        return ResponseEntity.ok().body(loginService.loginForgeRock(request));
    }


    @PostMapping(value = "/serviceValidate", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<ServiceValidateResponse> serviceValidate(@RequestBody ServiceValidateRequest request) throws Exception {
        log.trace("serviceValidate; {}", request);
        return ResponseEntity.ok().body(loginService.serviceValidate(request));
    }

    @PostMapping(value = "/validateLogoutRequest", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> validateLogoutRequest(@RequestBody LogoutRequest request) throws Exception {
        log.trace("validateLogoutRequest; {}", request);
        return ResponseEntity.ok().body(loginService.validateLogoutRequest(request));
    }

    @GetMapping(value = "/loginTypes", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginType[]> getLoginTypes() throws Exception {
        log.trace("getLoginTypes;");
        return ResponseEntity.ok().body(loginService.getLoginTypes());
    }


    @GetMapping(value = "/loginInfoWithChildAppName", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginInfo> getloginTypesWithChildAppName() throws Exception {
        log.trace("getLoginTypes;");
//        LoginType[] loginType = getLoginTypes();
        LoginType[] loginType = loginService.getLoginTypes();
        Set<String> childAppName = Lookup.getChildAppNameList();
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setChildApp(childAppName);
        loginInfo.setLoginTypes(loginType);
        return ResponseEntity.ok().body(loginInfo);
    }


    @PostMapping(value = "/changePasswordWindows", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public void changePasswordWindows(@RequestBody ChangePasswordRequest request) throws Exception {
        log.trace("changePasswordWindows; {}", request);
        loginService.changePasswordWindows(request);
    }


    @GetMapping(value = "/changePasswordsAllowed", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<String[]> getChangePasswordsAllowed() {
        log.trace("getChangePasswordsAllowed;");
        return ResponseEntity.ok().body(loginService.getChangePasswordsAllowed());
    }


    @GetMapping(value = "/homeUrlByServiceUrl", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<String> getHomeUrlByServiceUrl(@RequestParam("serviceUrl") String serviceUrl) {
        log.trace("getHomeUrlByServiceUrl;");
        String homeUrl = loginService.getHomeUrlByServiceUrl(serviceUrl);
        if (homeUrl == null || homeUrl.isEmpty()) {
            return ResponseEntity.ok().body("");
        }
        return ResponseEntity.ok().body(homeUrl);
    }


    @GetMapping(value = "/homeUrlByApplicationNameAndHost", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<String> getHomeUrlByApplicationNameAndHost(@RequestParam("childApplicationName") String applicationName, @RequestParam("url") String url) {
        log.trace("getHomeUrlByApplicationNameAndHost; applicationName={}; url={};", applicationName, url);
        String homeUrl = Lookup.getHomeUrlByApplicationNameAndHost(applicationName, url);
        if (homeUrl == null || homeUrl.isEmpty()) {
            return ResponseEntity.ok().body("");
        }
        return ResponseEntity.ok().body(homeUrl);
    }


    @GetMapping(value = "/childApplicationByServiceUrl", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<ChildApplication> getChildApplicationByServiceUrlOrAppKey(@RequestParam("serviceUrl") String serviceUrl, @RequestParam(RBACUtil.APP_KEY_IDENTIFIER_PARAM) String appKey) {
        log.trace("getChildApplicationByServiceUrlOrAppKey; serviceUrl={}; appKey={};", serviceUrl, appKey);
        return ResponseEntity.ok().body(loginService.getChildApplicationByServiceUrlOrAppKey(serviceUrl, appKey));
    }


    @GetMapping(value = "/childApplicationByServiceUrlAndAppKey", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<ChildApplication> getChildApplicationByServiceUrlAndAppKey(@RequestParam("serviceUrl") String serviceUrl, @RequestParam(RBACUtil.APP_KEY_IDENTIFIER_PARAM) String appKey) {
        log.trace("getChildApplicationByServiceUrlOrAppKey; serviceUrl={}; appKey={};", serviceUrl, appKey);
        return ResponseEntity.ok().body(loginService.getChildApplicationByServiceUrlAndAppKey(serviceUrl, appKey));
    }


    @GetMapping(value = "/isApplicationDown", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<ApplicationDownInfo> isApplicationDown(@RequestParam("childApplicationName") String childApplicationName) {
        log.trace("isApplicationDown;");
        ApplicationDownInfo appDownInfo = loginService.isApplicationDown(childApplicationName);
    	/*if(appDownInfo==null){

    	}*/
        return ResponseEntity.ok().body(appDownInfo);
    }

    private void isAppUnderMaintenance(LoginRequest request) {
        String childApplicationName = null;
        if (request.getAppKey() != null && !request.getAppKey().isEmpty()) {
            ChildApplication childApp = Lookup.getChildApplicationByAppKeyNew(request.getAppKey());
            childApplicationName = childApp != null ? childApp.getChildApplicationName() : null;
        } else {
            ChildApplication childApp = Lookup.getChildApplicationByServiceUrlNew(request.getService());
            childApplicationName = childApp != null ? childApp.getChildApplicationName() : null;
        }
        if (childApplicationName != null && !childApplicationName.isEmpty()) {
            ApplicationDownInfo appDownInfo = ApplicationMaintenanceCache.isApplicationDown(childApplicationName);
            if (appDownInfo != null) {
                log.info("isAppUnderMaintenance; user={}; {} is under maintenance from {} till {}; user={}", request.getUserName(), childApplicationName, appDownInfo.getFromDate(), appDownInfo.getToDate());
                ErrorInfoException errorInfoException = new ErrorInfoException(LoginResponse.APP_UNDER_MAINTENANCE);
                errorInfoException.getParameters().put("applicationName", appDownInfo.getChildApplicationName());
                //UriComponentsBuilder uriBuilderForMaintenance = UriComponentsBuilder.fromUriString(deploymentUtil.getMaintenancePage());
                String param = /*EsqSymetricCipher.encryptPassword(appDownInfo.getChildApplicationName());*/appDownInfo.getChildApplicationName();
                //uriBuilderForMaintenance.queryParam("a", param);
                errorInfoException.getParameters().put("a", param);
                //uriBuilderForMaintenance.queryParam("homeUrl", Lookup.getHomeUrlByServiceUrlNew(request.getService()));
                errorInfoException.getParameters().put("homeUrl", Lookup.getHomeUrlByServiceUrlNew(request.getService()));
                //errorInfoException.getParameters().put("redirectUrl", uriBuilderForMaintenance.build().toString());
                throw errorInfoException;
            }
        }
    }


    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public void logout(@RequestBody LogoutRequest request) throws Exception {
        log.trace("logout; {}", request);
        loginService.logout(request);
    }


    @GetMapping(value = "/userSessionData", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<UserSessionData> getUserSessionData(@RequestParam("sessionHash") String sessionHash, @RequestParam("serviceUrl") String serviceUrl, @RequestParam(RBACUtil.APP_KEY_IDENTIFIER_PARAM) String appKey) {
        log.debug("getUserSessionData; sessionHash={}; serviceUrl={}; appKey={};", sessionHash, serviceUrl, appKey);
        return ResponseEntity.ok().body(sessionRegistry.getUserSessionData(sessionHash, serviceUrl, appKey));
    }

    @Deprecated
    @PostMapping(value = "/sessionRegistryLogout", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Operation(hidden = true, description = "sessionRegistryLogout")
    public ResponseEntity<LogoutResponse> sessionRegistryLogout(@RequestBody SessionRegistryLogoutRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        String loggedInUserName = headers.get("loggedInUserName") != null ? headers.get("loggedInUserName").get(0) : null;
        String appKey = RBACUtil.getAppKeyFromHeader(headers);
        if (appKey != null) {
            request.setAppKey(appKey);
        }
        //this handling is required to support apps that are still sending logout calls to app layer.
        //RBAC SSO will send this param as false when deploymentUtil.logoutViaWebLayer is set to true
        log.warn("sessionRegistryLogout; This call should now be send to the sso layer; appKey={}; tag={};", request.getAppKey(), request.getTag());
        return ResponseEntity.ok().body(loginService.sessionRegistryLogout(request, loggedInUserName, Boolean.TRUE));
    }


    @PostMapping(value = "/sessionRegistryLogoutApp", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = RBACUtil.APP_KEY_IDENTIFIER_HEADER, description = RBACUtil.APP_KEY_IDENTIFIER_HEADER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<LogoutResponse> sessionRegistryLogoutApp(@RequestParam SessionRegistryLogoutRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        String loggedInUserName = headers.get("loggedInUserName") != null ? headers.get("loggedInUserName").get(0) : null;
        String appKey = RBACUtil.getAppKeyFromHeader(headers);
        if (appKey != null) {
            request.setAppKey(appKey);
        }
        return ResponseEntity.ok().body(loginService.sessionRegistryLogout(request, loggedInUserName, null));
    }


    @PostMapping(value = "/sessionRegistryLogoutWeb", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "loggedInUserName", description = "loggedInUserName", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = RBACUtil.APP_KEY_IDENTIFIER_HEADER, description = RBACUtil.APP_KEY_IDENTIFIER_HEADER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<LogoutResponse> sessionRegistryLogoutWeb(@RequestBody SessionRegistryLogoutRequest request, @RequestHeader HttpHeaders headers) throws Exception {
        String loggedInUserName = headers.get("loggedInUserName") != null ? headers.get("loggedInUserName").get(0) : null;
        String appKey = RBACUtil.getAppKeyFromHeader(headers);
        if (appKey != null) {
            request.setAppKey(appKey);
        }
        return ResponseEntity.ok().body(loginService.sessionRegistryLogout(request, loggedInUserName, null));
    }


    @GetMapping(value = "/isLogoutRequestDone/{requestId}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public boolean isLogoutRequestDone(@RequestParam("requestId") String requestId) {
        return sessionRegistry.isLogoutRequestDone(requestId);
    }

    @GetMapping(value = "/isChangePasswordValid", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> isChangePasswordValid(@RequestParam("sessionHash") String sessionHash) {
        return ResponseEntity.ok().body(loginService.isChangePasswordValid(sessionHash));
    }

    //RBAC-1562 Starts
    @PostMapping(value = "/twoFactorAuthRequest", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginResponse> twoFactorAuthRequest(@RequestBody LoginRequest loginRequest) throws Exception {
        log.trace("twoFactorAuthRequest; {}", loginRequest);
        return ResponseEntity.ok().body(loginService.twoFactorAuthRequest(loginRequest));
    }


    @PostMapping(value = "/getAuthChannelDetails", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<TwoFactorAuthVO> getAuthChannelDetails(@RequestParam("userName") String userName) throws Exception {
        log.trace("userName; {}", userName);
        return ResponseEntity.ok().body(loginService.getAuthChanelDetails(userName));
    }


    @PostMapping(value = "/sendOTPAlert", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginResponse> sendOTPAlert(@RequestBody TwoFactorAuthVO twoFactorAuthVO) throws Exception {
        log.trace("twoFactorAuth; {}", twoFactorAuthVO);
//        return loginService.sendOTPAlert(twoFactorAuthVO);
        LoginResponse loginResponse = new LoginResponse();
        try {
            User user = loginService.getByUserName(twoFactorAuthVO.getUserName());
            loginResponse = twoFactorAlertDal.sendAlert(user, twoFactorAuthVO);

        } catch (Exception e) {
            log.error("LoginServiceImpl; sendOTPAlert; {}", e.getMessage());
            loginResponse.setResultCode(LoginResponse.OTP_SENDING_FAILED);
        }
        return ResponseEntity.ok().body(loginResponse);
    }

    //RBAC-1562 Ends

    /*Added By Pankaj for BreakFix BRKFIX-450 Un-validated redirect and forwardsBRKFIX-451
	Un-validated redirect and forwards */

    @GetMapping(value = "/changePasswordURLExits", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> isChangePasswordReturnURLExits(@RequestParam("returnUrl") String returnUrl) {
        log.trace("isChangePasswordReturnURLExits;");
        return ResponseEntity.ok().body(loginService.isChangePasswordReturnURLExits(returnUrl));
    }

    //Azure login redirect request
    @PostMapping(value = "/azureLogin", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginResponse> loginAzureActiveDirectory(@RequestBody LoginRequest request) {
        log.trace("loginAzureActiveDirectory; {}", request);
        validate(request);
//         isAppUnderMaintenance(request);
        return ResponseEntity.ok().body(loginService.loginAzureActiveDirectory(request));
    }

    //Azure validate username request
    @PostMapping(value = "/validateUserName", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginResponse> validateUsername(@RequestBody LoginRequest request) {
        log.trace("loginAzureActiveDirectoryValidateUserName; {}", request);
//         isAppUnderMaintenance(request);
        return ResponseEntity.ok().body(loginService.validateUserName(request));
    }


    @GetMapping(value = "/userSessionDataCount", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<Integer> userSessionDataCount(@RequestParam(RBACUtil.TICKET_HEADER_IDENTIFIER) String ticket, @RequestParam("userName") String userName) {
        log.trace("userSessionDataCount; ticket={}, userName={}", ticket, userName);
        return ResponseEntity.ok().body(sessionRegistry.userSessionDataCount(ticket, userName));
    }

    private void validate(LoginRequest loginRequest) {
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);
        if (violations.size() > 0) {
            log.warn("validate; violations={}", violations);

            ConstraintViolation<LoginRequest> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }
}
