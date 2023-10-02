package com.esq.rbac.service.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Integer userId;
    private Integer groupId;
    private String firstName;
    private String lastName;
    private String userName;
    private String emailAddress;
    private String phoneNumber;
    private String homeEmailAddress;
    private String homePhoneNumber;
    private Boolean isShared;
    private Long organizationId;
}
