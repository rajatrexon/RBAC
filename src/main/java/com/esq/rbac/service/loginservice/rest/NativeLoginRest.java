package com.esq.rbac.service.loginservice.rest;


import com.esq.rbac.service.loginservice.embedded.*;
import com.esq.rbac.service.loginservice.service.LoginService;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.MessagesUtil;
import com.esq.rbac.service.util.RBACUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Locale;

@RestController
@RequestMapping("/native")
@Tag(name="/native")
@Slf4j
public class NativeLoginRest {

    private final LoginService loginService;
    private final MessagesUtil messageUtil;
    private final DeploymentUtil deploymentUtil;

    public NativeLoginRest(LoginService loginService,MessagesUtil messagesUtil,DeploymentUtil deploymentUtil){
        log.trace("setLoginService; {}", loginService);
        this.loginService = loginService;
        this.deploymentUtil = deploymentUtil;
        this.messageUtil = messagesUtil;
    }

    @PostMapping(value = "/login",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = RBACUtil.LOCALE_IDENTIFIER, description = RBACUtil.LOCALE_IDENTIFIER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public LoginResponse loginNative(
           @RequestBody LoginRequest loginRequest,
            @Context HttpServletRequest httpRequest) throws Exception {
        //loginRequest.setClientIP(RBACUtil.getRemoteAddress(httpRequest));
        //loginRequest.setHeaderMap(RBACUtil.getHeaderMap(httpRequest));
        loginRequest.setDeviceType(RBACUtil.DEVICE_NATIVE);
        log.trace("loginNative; loginRequest={}", loginRequest);
        LoginResponse loginResponse = loginService.loginNative(loginRequest);
        loginResponse.setResultMessage(messageUtil.getMessage("login."+loginResponse.getResultCode(), null, getLocaleFromRequest(httpRequest), loginResponse.getResultCode()));
        return loginResponse;
    }


    @PostMapping(value = "/loginLDAP",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = RBACUtil.LOCALE_IDENTIFIER, description = RBACUtil.LOCALE_IDENTIFIER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public LoginResponse loginLDAP(
            LoginRequest loginRequest,
            @Context HttpServletRequest httpRequest) throws Exception {
        //loginRequest.setClientIP(RBACUtil.getRemoteAddress(httpRequest));
        //loginRequest.setHeaderMap(RBACUtil.getHeaderMap(httpRequest));
        loginRequest.setDeviceType(RBACUtil.DEVICE_NATIVE);
        log.trace("loginNativeLDAP; loginRequest={}", loginRequest);
        LoginResponse loginResponse = loginService.loginNativeLDAP(loginRequest);
        log.info("loginResponse ={} ",loginResponse);
        loginResponse.setResultMessage(messageUtil.getMessage("login."+loginResponse.getResultCode(), null, getLocaleFromRequest(httpRequest), loginResponse.getResultCode()));
        return loginResponse;
    }

    @PostMapping(value = "/logout",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = RBACUtil.LOCALE_IDENTIFIER, description = RBACUtil.LOCALE_IDENTIFIER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public LogoutResponse logoutNative(@RequestBody LogoutRequest request,
                                       @Context HttpServletRequest httpRequest) throws Exception {
        //request.setClientIP(RBACUtil.getRemoteAddress(httpRequest));
        log.trace("logoutNative; request={}", request);
        LogoutResponse response = loginService.logoutNative(request);
        if(response.getSessionHashChildApplicationNames()!=null && !response.getSessionHashChildApplicationNames().isEmpty())
        {
            response.getSessionHashChildApplicationNames().clear();
        }
        response.setResultMessage(messageUtil.getMessage("logout."+response.getResultCode(), null, getLocaleFromRequest(httpRequest), response.getResultCode()));
        return response;
    }

    @PostMapping(value = "/validateTicket",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = RBACUtil.LOCALE_IDENTIFIER, description = RBACUtil.LOCALE_IDENTIFIER, required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public ServiceValidateResponse validateNativeTicket(ServiceValidateRequest request,
                                                        @Context HttpServletRequest httpRequest) throws Exception {
        //request.setIpAddress(RBACUtil.getRemoteAddress(httpRequest));
        log.trace("validateNativeTicket; request={}", request);
        ServiceValidateResponse response = loginService.validateNativeTicket(request);
        if(response.getIsSuccess().equals(Boolean.FALSE)){
            response.setFailureMessage(messageUtil.getMessage("validate."+response.getFailureCode(), null, getLocaleFromRequest(httpRequest), response.getFailureCode()));
        }
        return response;
    }

    public Locale getLocaleFromRequest(HttpServletRequest httpRequest){
        if(httpRequest.getHeader(RBACUtil.LOCALE_IDENTIFIER)!=null && !httpRequest.getHeader(RBACUtil.LOCALE_IDENTIFIER).isEmpty()){
            return RBACUtil.getLocaleFromString(httpRequest.getHeader(RBACUtil.LOCALE_IDENTIFIER));
        }
        if(deploymentUtil.getDefaultLocaleForRestMessages()!=null && !deploymentUtil.getDefaultLocaleForRestMessages().isEmpty()){
            return RBACUtil.getLocaleFromString(deploymentUtil.getDefaultLocaleForRestMessages());
        }
        return  RBACUtil.getLocaleFromString(RBACUtil.DEFAULT_LOCALE_STRING);
    }
}
