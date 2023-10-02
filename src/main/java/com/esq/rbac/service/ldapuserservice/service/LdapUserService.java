package com.esq.rbac.service.ldapuserservice.service;


import com.esq.rbac.service.user.domain.User;

import java.util.List;
import java.util.Map;

public interface LdapUserService {
    List<String> getAllUserNames();

    User getUserDetails(String searchParam);

    void reloadConfiguration() throws Exception;

    boolean isUserImportEnabled();

    boolean isBulkImportEnabled();

    Map<String, List<String>> testConnection(String url, String userDn, String password, String base);

    boolean checkUser(String windowsUsername, String password);

    /* RBAC-1259 START */
    void performFirstSync(Integer loggedInUserId, String clientIp) throws Exception;

    Long getMaxIdentifierValue() throws Exception;

    void performNextSync(Long dbSavedIdentifier, Integer loggedInUserId, String clientIp);

    boolean isLdapDetailsSyncEnabled();
    /* RBAC-1259 END */

    boolean isLdapEnabled();

    String getLDAPDefaultValueForIdentity();
}
