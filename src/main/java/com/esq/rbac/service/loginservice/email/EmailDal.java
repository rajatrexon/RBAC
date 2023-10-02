package com.esq.rbac.service.loginservice.email;

import com.esq.rbac.service.loginservice.embedded.LoginRequest;
import com.esq.rbac.service.user.domain.User;

public interface EmailDal {

    void sendAlert(User user, String alertId, String alertType);

    void sendFailedLoginAlert(User user, LoginRequest request, String channelType);
}
