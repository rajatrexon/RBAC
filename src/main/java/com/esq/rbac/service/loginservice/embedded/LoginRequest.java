package com.esq.rbac.service.loginservice.embedded;

import com.esq.rbac.service.util.SpecialCharValidator;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    private String service;
    @Size(min=1, max = 254)
    @SpecialCharValidator
    private String userName;
    private String password;
    private String newPassword;
    private String clientIP;
    private String sessionHash;
    private Map<String, String> headerMap;
    private String deviceType;
    private String appKey;
    private String passwordEncryptionKey;
    //RBAC-1747 Start
    private String captchaText;
    private String captchaUUID;
    //RBAC-1747 End
    private Map<String, String> parameters;

    //RBAC-1562 Starts
    private Boolean isInvalidToken = false;
    private String loginType;
    private String loginLogType;
    //RBAC-1562 Ends

    private String strCurrentDateTime;

    private String selectType;

    private String audioCaptchaUUID;
}
