package com.esq.rbac.service.scope.builder.util;

import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface ScopeGenerator extends ApplicationContextAware {

    String getFilterKeyData(String sourcePath, String dataKey, String scopeKey,
                            String userName, String additionalMap,
                            Map<String, String> scopeMap, String parentValue);
}
