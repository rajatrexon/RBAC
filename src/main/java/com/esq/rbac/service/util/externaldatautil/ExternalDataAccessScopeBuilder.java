package com.esq.rbac.service.util.externaldatautil;

import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import jakarta.ws.rs.core.UriInfo;

public interface ExternalDataAccessScopeBuilder extends ExternalDataAccess{
    String getFilters(String scopeKey, String userName, String additionalMap);

    String getFilterKeyData(String sourcePath, String dataKey,
                            String scopeKey, String userName, String additionalMap, String parentValue);

    String validateAndBuildQuery(String scopeSql, String scopeJson, String scopeKey, String userName, String additionalMap);
}
