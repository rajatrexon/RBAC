package com.esq.rbac.service.loginservice.embedded;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.util.RBACUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionRegistryLogoutRequest {

    private String userName;
    private String sessionHash;
    private String service;
    private String clientIp;
    private String logoutType;
    private ChildApplication childApplication;
    private RBACUtil.LOGOUT_ACTION logoutAction;
    private String requestId;
    private Integer appType;
    private String childAppName;
    private String ticketToLogout;
    private String appKey;
    private String tag;
    private Date cutOffDate;

}
