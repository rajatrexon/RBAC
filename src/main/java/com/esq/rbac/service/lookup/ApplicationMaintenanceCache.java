package com.esq.rbac.service.lookup;

import com.esq.rbac.service.application.applicationmaintenance.util.ApplicationDownInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ApplicationMaintenanceCache {

    private static final int CACHE_MAX_SIZE = 1000;

    private static Cache<String, ApplicationDownInfo> applicationStateCache = CacheBuilder
            .newBuilder().maximumSize(CACHE_MAX_SIZE).recordStats().build();

    public static ApplicationDownInfo isApplicationDown(String applicationName) {
        try {
            if(applicationName!=null){
                ApplicationDownInfo returnValue = applicationStateCache
                        .getIfPresent(applicationName.toLowerCase());
                if (returnValue != null) {
                    return returnValue;
                }
            }
        }catch (Exception e) {
            log.error("isApplicationDown; Exception={}", e);
        }
        return null;
    }

    public static synchronized void replaceCache(
            Map<String, ApplicationDownInfo> map) {
        log.info("replaceCache;  map={}", map);
        applicationStateCache.invalidateAll();
        applicationStateCache.putAll(map);
    }
}
