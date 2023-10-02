package com.esq.rbac.service.usersync.repository;

import com.esq.rbac.service.userexternalrecord.domain.UserExternalRecord;
import com.esq.rbac.service.usersync.domain.UserSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface UserSyncRepository extends JpaRepository<UserSync, Integer> {
    @Modifying
    @Query("UPDATE UserSync t SET t.status = :status, t.updatedBy = :updatedBy, t.updatedOn = :currDateTime WHERE t.userSyncId = :userSyncId")
    void discardUserSyncById(@Param("userSyncId") Integer userSyncId, @Param("status") Integer status,
                             @Param("updatedBy") Integer updatedBy, @Param("currDateTime") Date currDateTime);

    @Modifying
    @Query(value = "UPDATE userSync SET userId = ?1 WHERE userSyncId = ?2", nativeQuery = true)
    void updateUserIdInUserSync(String userId, Integer userSyncId);

    @Modifying
    @Query("DELETE FROM UserSync t WHERE t.externalRecordId = :externalRecordId")
    void forceDeleteUserSyncByExternalRecordId(@Param("externalRecordId") String externalRecordId);

    UserSync findByExternalRecordId(String externalRecordId);
}
