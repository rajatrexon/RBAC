//package com.esq.rbac.service.scope.context;
//
//import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
//import com.esq.rbac.service.group.domain.Group;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//
//@Component
//public interface HybridScopeHandler extends ApplicationContextAware {
//    void handleScope(Group group, AuditLogInfo auditLogInfo);
//
//    String getFilterKeyData(String sourcePath, String dataKey, String scopeKey, String userName, String additionalMap,
//                            String parentValue);
//
//    void setReportPortalScopeKey(String scopeKey);
//
//    String validateAndBuildQuery(String scopeSql, String scopeJson, String scopeKey,
//                                 String userName, String additionalMap);
//
//}
