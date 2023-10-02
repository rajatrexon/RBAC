package com.esq.rbac.service.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import jakarta.inject.Inject;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;



//Todo need to be discussed

public class DatabaseConfigurationWithCache extends DatabaseConfiguration {

   private static final Logger log = LoggerFactory.getLogger(DatabaseConfigurationWithCache.class);

    private LoadingCache<String, Object> fieldsCache;


    @Autowired
    public DatabaseConfigurationWithCache(@Qualifier("DatabaseConfigurationWithCache")DataSource datasource, String table, String keyColumn, String valueColumn,
                                          int reloadTimeSec) {
        super(datasource, table, keyColumn, valueColumn);
        fieldsCache = CacheBuilder.newBuilder().expireAfterWrite(reloadTimeSec, TimeUnit.SECONDS)
                .build(new CacheLoader<String, Object>() {
                    public Object load(String key) {
                        return getProperty(key);
                    }
                });
    }


    protected Object resolveContainerStore(String key) {
        Object value = null;
        try {
            value = fieldsCache.get(key);
            if (value != null) {
                if (value instanceof Collection) {
                    Collection<?> collection = (Collection<?>) value;
                    value = collection.isEmpty() ? null : collection.iterator().next();
                } else if (value.getClass().isArray() && Array.getLength(value) > 0) {
                    value = Array.get(value, 0);
                }
            }
        } catch (CacheLoader.InvalidCacheLoadException e) {
            // eat this for null keys
        } catch (ExecutionException e) {
            log.error("resolveContainerStore; Exception={};", e);
        }
        return value;
    }

    public void clearAll() {
        fieldsCache.invalidateAll();
    }

}
