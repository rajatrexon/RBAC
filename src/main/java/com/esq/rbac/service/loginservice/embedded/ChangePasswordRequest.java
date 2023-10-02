package com.esq.rbac.service.loginservice.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    private String userName;
    private String oldPassword;
    private String newPassword;
    private String service;
    private String clientIP;
    private Map<String, String> parameters;
    private String sessionHash;
    private String appKey;
    private String requestId;
    private String passwordEncryptionKey;

}
