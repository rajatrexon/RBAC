package com.esq.rbac.service.loginlog.service;

import com.esq.rbac.service.loginlog.domain.LoginLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class LoginLogDalProcessor {

    @Autowired
    private LoginLogService loginLogService;

    @Transactional(propagation = Propagation.REQUIRED)
    public void createLoginLog(LoginLog loginLog) {
        loginLogService.createLoginLogUseFromLoginLogProcessor(loginLog);
    }
}
