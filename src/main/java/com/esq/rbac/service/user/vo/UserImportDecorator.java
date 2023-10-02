package com.esq.rbac.service.user.vo;

import com.esq.rbac.service.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserImportDecorator {

    private User user;
    private Boolean isSuccess;
    private String importMessage;
    private String password;
    private String groupName;
    private String organizationName;
    private String roles;

    public UserImportDecorator(User user) {
        this.user = user;
    }
}
