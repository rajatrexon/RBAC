package com.esq.rbac.service.variable.util;

import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.variable.domain.Variable;
import com.esq.rbac.service.variable.variableinfo.domain.VariableInfo;
import com.esq.rbac.service.variable.variableinfov2.domain.VariableInfoV2;

public class VariableUtil {
    public static VariableInfo fromVariable(Variable variable) {

        VariableInfo variableInfo = new VariableInfo();
        variableInfo
                .setApplicationName((variable.getApplicationId() != null) ? Lookup.getApplicationName(variable
                        .getApplicationId()) : null);
        variableInfo
                .setGroupName((variable.getGroupId() != null) ? Lookup.getGroupName(variable
                        .getGroupId()) : null);
        variableInfo
                .setUserName((variable.getUserId() != null) ? Lookup.getUserName(variable
                        .getUserId()) : null);
        variableInfo.setVariableName(variable.getVariableName());
        variableInfo.setVariableValue(variable.getVariableValue());
        return variableInfo;
    }

    public static VariableInfoV2 fromVariableV2(Variable variable) {

        VariableInfoV2 variableInfo = new VariableInfoV2();
        variableInfo
                .setApplicationName((variable.getApplicationId() != null) ? Lookup.getApplicationName(variable
                        .getApplicationId()) : null);
        variableInfo
                .setGroupName((variable.getGroupId() != null) ? Lookup.getGroupName(variable
                        .getGroupId()) : null);
        variableInfo
                .setUserName((variable.getUserId() != null) ? Lookup.getUserName(variable
                        .getUserId()) : null);
        variableInfo.setAppKey((variable.getChildApplicationId() != null) ? Lookup.getAppKeyByChildAppId(variable
                .getChildApplicationId()) : null);
        variableInfo.setVariableName(variable.getVariableName());
        variableInfo.setVariableValue(variable.getVariableValue());
        return variableInfo;
    }

}
