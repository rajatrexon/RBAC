package com.esq.rbac.service.loginservice.embedded;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class UserSessionData {

    private final String userName;
    private final String loginType;
    private final Date loginTime;
    private String identityId;
    
    Map<String, String> additionalAttributes;

    public UserSessionData(String userName, String loginType, Date loginTime) {
        this.userName = userName;
        this.loginType = loginType;
        this.loginTime = loginTime;
    }

    public UserSessionData() {
        this.userName = null;
        this.loginType = null;
        this.loginTime = null;
    }

    public UserSessionData identityId(String identityId) {
        this.identityId = identityId;
        return this;
    }

    public UserSessionData(String userName, String loginType, Date loginTime, String identityId) {
        this.userName = userName;
        this.loginType = loginType;
        this.loginTime = loginTime;
        this.identityId = identityId;
    }


}
