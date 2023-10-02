package com.esq.rbac.service.loginservice.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordPolicy {

    Boolean isPolicyViolated;
    Integer passwordChangeTimeLimitInHrs;
    Integer allowedPasswordChanges;
    Date lastPasswordSetTime;
}
