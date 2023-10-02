package com.esq.rbac.service.basedal;

import com.esq.rbac.service.restriction.domain.Restriction;

import java.util.Map;
import java.util.Set;

public interface BaseDal {
    /* RBAC-1475 MakerChecker End */
    Map<String, String> getObjectChangeSet();

    void clearObjectChangeSet();

    void putToObjectChangeSet(String key, String value);

    void putToObjectChangeSet(String objChangeStr, String objChangeParam, Object oldVal, String oldValOut, Object newVal, String newValOut);

    @SuppressWarnings({"rawtypes"})
    void checkPutToObjectChangeSet(String objChangeStr, Object newVal, Object oldVal, String newValOut, String oldValOut);

    String getFullDays(String days);

    void setHour(int count, String minute);

    String getHours(String hours);

    void checkMapPutToObjectChangeSet(Map<String, String> newMap, Map<String, String> oldMap);

    public  void checkRestrictionPutToObjectChangeSet(Restriction newRestriction, Restriction oldRestriction);

    String getRoleNames(Set<Integer> roleIds);

    public void checkRoleIdsPutToObjectChangeSet(Set<Integer> newRoleIds, Set<Integer> oldRoleIds);

    String getTargetOperationName(Integer operationId);

    void checkOperationIdsPutToObjectChangeSet(Set<Integer> newOperationIds, Set<Integer> oldOperationIds);
}
