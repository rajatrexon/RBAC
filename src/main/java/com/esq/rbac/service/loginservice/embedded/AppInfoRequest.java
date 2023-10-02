package com.esq.rbac.service.loginservice.embedded;

import com.esq.rbac.service.util.RBACUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor@NoArgsConstructor
public class AppInfoRequest {
    private String service;
    private boolean renew;
    private String ssoContextPath;
    private String clientIP;
    private String sessionHash;
    private Map<String, String> headerMap;
    private String deviceType;
    private String appKey;

    public AppInfoRequest(HttpServletRequest httpRequest) {
        this.headerMap = RBACUtil.getHeaderMap(httpRequest);
        this.deviceType = RBACUtil.getDeviceType(httpRequest, RBACUtil.DEVICE_NORMAL);
    }
}