package com.esq.rbac.service.loginservice.util;

import com.esq.rbac.service.loginservice.embedded.LoginResponse;
import com.esq.rbac.service.loginservice.embedded.TwoFactorAuthVO;
import com.esq.rbac.service.user.domain.User;

public interface TwoFactorAlertDal {
    LoginResponse sendAlert(User user, TwoFactorAuthVO twoFactorAuthVO);
}
