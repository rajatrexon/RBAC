package com.esq.rbac.service.loginservice.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorAuthVO {
    private String emailId;
    private String phoneNumber;
    private String userName;
    private String userFullName;
    private String maskedChannel;
    private int maxOtpAttempts;
    private int otpTimeout;
    private int otpSessionTimeout;
    private String token;
    private String requestId;

    private String returnUrl;
    private String serviceUrl;
    private String appKey;
    private String tokenEncKey;
    private String publicKey;

    private String loginType;
    private String loginLogType;

    private String channelType;
}
