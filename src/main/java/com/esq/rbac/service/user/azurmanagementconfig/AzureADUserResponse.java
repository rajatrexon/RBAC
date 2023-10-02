package com.esq.rbac.service.user.azurmanagementconfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AzureADUserResponse {
    String id;
    String displayName;
    String givenName;
    String mail;
    String mobilePhone;
    String userPrincipalName;
}
