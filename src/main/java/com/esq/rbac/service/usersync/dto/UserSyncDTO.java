package com.esq.rbac.service.usersync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSyncDTO {

    private Integer userSyncId;
    private String sAMAccountName;
    private String userNameLdap;
    private String name;
    private String distinguishedName;
    private String cn;
    private String externalRecordId;
    private String userPrincipalName;
    private Integer organizationId;
    private Integer fromDataTable;
    private String objectCategory;
    private Integer userId;
    private Integer groupId;
}
