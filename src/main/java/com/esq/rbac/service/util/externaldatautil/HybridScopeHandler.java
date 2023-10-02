package com.esq.rbac.service.util.externaldatautil;

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.group.domain.Group;
import org.springframework.context.ApplicationContextAware;

public interface HybridScopeHandler extends ApplicationContextAware {

    void handleScope(Group group, AuditLogInfo auditLogInfo);

    String getFilterKeyData(String sourcePath, String dataKey, String scopeKey, String userName, String additionalMap,
                            String parentValue);

    void setReportPortalScopeKey(String scopeKey);

    String validateAndBuildQuery(String scopeSql, String scopeJson, String scopeKey,
                                 String userName, String additionalMap);

}
