package com.esq.rbac.service.user.userimport.service;

import com.esq.rbac.service.user.domain.User;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserImportService {

    void importFromCSV(Reader reader, Writer writer, Integer loggedInUserId, Long currentTenantId)
            throws Exception;


    //RBAC-1892
    void exportToCSV(List<User> userList, Map<Integer, Set<String>> getRoleListByGroup, Writer writer)
            throws Exception;
}
