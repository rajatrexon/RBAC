package com.esq.rbac.service.loginservice.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceValidateResponse {

    private String failureCode;
    private String failureMessage;
    private String username;
    private Boolean isSuccess;
}
