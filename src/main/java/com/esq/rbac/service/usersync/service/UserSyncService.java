package com.esq.rbac.service.usersync.service;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.userexternalrecord.domain.UserExternalRecord;
import com.esq.rbac.service.usersync.domain.UserSync;
import com.esq.rbac.service.util.dal.Options;

import java.util.List;

public interface UserSyncService extends BaseDal {

   UserSync create(UserSync userSync, AuditLogInfo auditLogInfo);

   UserSync update(UserSync userSync, AuditLogInfo auditLogInfo);

   UserSync getById(int userSyncId);

    void deleteById(Integer userSyncId, AuditLogInfo auditLogInfo);

    void forceDeleteById(Integer userSyncId, AuditLogInfo auditLogInfo);

    List<UserSync> list(Options options);

    int getCount(Options options);

    List<UserSync> searchList(Options options);

    int getSearchCount(Options options);

    void createUserExternalRecord(String externalRecordId, int userId);

    Integer findUserIdByExternalRecordId(String externalRecordId);

    UserSync getByExternalRecordId(String externalRecordId);

    /*UserSync updateStatus(UserSync userSync);*/

    UserExternalRecord findExternalRecordByUserId(Integer userId);

    UserSync getByUserId(Integer userId);

    void deleteUserFromUserSync(Integer userId, Integer loggedInUserId, String clientIp, Long makerCheckerId, Integer userStatus);

    void updateExistingConflicts(Integer loggedInUserId, String clientIp);
}
