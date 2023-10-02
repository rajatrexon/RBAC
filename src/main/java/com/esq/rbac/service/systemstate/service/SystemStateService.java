package com.esq.rbac.service.systemstate.service;

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.systemstate.domain.SystemState;

public interface SystemStateService extends BaseDal {
    SystemState create(SystemState systemState, int systemStateId, String target, String operation);

    SystemState update(SystemState systemState, AuditLogInfo auditLogInfo);

    SystemState getByIdentifier(String identifier);

    void deleteById(Integer Id, AuditLogInfo auditLogInfo);
}
