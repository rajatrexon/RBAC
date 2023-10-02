package com.esq.rbac.service.loginservice.embedded;

import com.esq.rbac.service.user.vo.SSOLogoutData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data@AllArgsConstructor@NoArgsConstructor
public class AppInfoResponse {

    private String service;
    private boolean renew;
    private String serviceTicket;
    private String redirectUrl;
    private String loginFormUrl;
    private String applicationName;
    private String userName;
    private List<SSOLogoutData> ssoLogoutDataList;
}
