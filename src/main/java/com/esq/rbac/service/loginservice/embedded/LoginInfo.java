package com.esq.rbac.service.loginservice.embedded;

import com.esq.rbac.service.loginlog.domain.LoginType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginInfo {

    private String service;

    private LoginType[] loginTypes;

    private Set<String> childAppName;

    //RBAC-1747 Start
    private String captchaUUID;
    private Integer enableCaptchaCount;
    private Integer captchaTimeoutInMinutes;
    //RBAC-1747 End
    private Integer audioCaptchaTimeoutInSeconds;
    // RBAC-1732
    private String overrideLanguageCode;

    private String audioCaptchaUUID;

    public Set<String> getChildAppName() {
        return childAppName;
    }

    public void setChildApp(Set<String> childAppName) {
        this.childAppName = childAppName;
    }

    public LoginType[] getLoginTypes() {
        return loginTypes;
    }

    public void setLoginTypes(LoginType[] loginTypes) {
        this.loginTypes = loginTypes;
    }
}
