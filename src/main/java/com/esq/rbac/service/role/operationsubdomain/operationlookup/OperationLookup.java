package com.esq.rbac.service.role.operationsubdomain.operationlookup;

import java.util.TreeMap;

public class OperationLookup {

    private static TreeMap<Integer, String> operationNamesLookupTable = new TreeMap<Integer, String>();

    private static TreeMap<Integer, String> operationTargetLookupTable = new TreeMap<Integer, String>();



    public static String getTargetOperationName(Integer operationId){
        String operationName = null;
        if (operationId != null) {
            operationName = operationNamesLookupTable.get(operationId);
        }
        return (operationName!=null)?operationTargetLookupTable.get(operationId)+"."+operationName:null;
    }
}
