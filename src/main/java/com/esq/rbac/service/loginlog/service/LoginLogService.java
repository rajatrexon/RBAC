package com.esq.rbac.service.loginlog.service;


import com.esq.rbac.service.loginlog.domain.LoginLog;

public interface LoginLogService {
    void create(LoginLog loginLog);
    void createLoginLogUseFromLoginLogProcessor(LoginLog loginLog);
}
