package com.esq.rbac.service.loginservice.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceValidateRequest {

    private String service;
    private String serviceTicket;
    private String ipAddress;
    private String appKey;
}

