package com.esq.rbac.service.util.externaldatautil;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExternalDataAccessUtil {

    private Map<String, Map<String, ExternalDataAccess>> externalDataAccessMap;

    public Map<String, Map<String, ExternalDataAccess>> getExternalDataAccessMap() {
        return externalDataAccessMap;
    }

    public void setExternalDataAccessMap(
            Map<String, Map<String, ExternalDataAccess>> externalDataAccessMap) {
        this.externalDataAccessMap = externalDataAccessMap;
    }

}
