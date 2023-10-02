package com.esq.rbac.service.health.service;

import com.esq.rbac.service.health.domain.RBACHealth;

import java.util.Date;
import java.util.Map;

public interface HealthDal {
    RBACHealth createHealthInfo(RBACHealth rbacHealth);
    RBACHealth updateHealthInfo(RBACHealth rbacHealth);
    RBACHealth getHealthInfo(String appName);
    void healthManager(Map<String, Date> healthMap);
    RBACHealth[] getAllAppHealthInfo();
    RBACHealth deleteHealthInfo(RBACHealth rbacHealth);
}
