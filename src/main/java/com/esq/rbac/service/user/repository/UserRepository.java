package com.esq.rbac.service.user.repository;

import com.esq.rbac.service.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;


import com.esq.rbac.service.user.embedded.OrganizationHierarchyUser;
import com.esq.rbac.service.user.embedded.OrganizationInfoUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserName(String userName);

    @Query("SELECT u FROM User u WHERE LOWER(u.userName) LIKE CONCAT('%', LOWER(:userName), '%')")
    User getUserByUserNameLike(@Param("userName") String userName);

    @Query("SELECT u FROM User u WHERE u.makerCheckerId = :makerCheckerId")
    User getUserMakerCheckerId(@Param("makerCheckerId") Long makerCheckerId);

    @Query("SELECT u FROM User u JOIN u.identities i WHERE i.identityType = :identityType AND i.identityId = :identityId AND u.isStatus = 1")
    List<User> getUsersByIdentity(@Param("identityType") String identityType, @Param("identityId") String identityId);

    @Query("SELECT u.userName FROM User u WHERE u.isStatus = 1")
    List<String> getAllUserNames();

    @Modifying
    @Query("DELETE FROM User u WHERE u.userName = :userName")
    void deleteUserByUserName(@Param("userName") String userName);

//    @Query("SELECT new OrganizationHierarchyUser(u) FROM User u WHERE u.organizationId IN (SELECT org.organizationId FROM Organization org WHERE org.tenantId = :tenantId) AND u.isStatus = 1 ORDER BY u.userName")
//    List<OrganizationHierarchyUser> getOrganizationHierarchyUsersByTenant(String tenantId);

//    @Query("SELECT COUNT(u) FROM User u WHERE u.organizationId IN (SELECT org.organizationId FROM Organization org WHERE org.tenantId = :tenantId) AND u.isStatus = 1")
//    Long getOrganizationHierarchyUsersByTenantCount(String tenantId);

//    @Query("SELECT new OrganizationInfoUser(u) FROM User u WHERE u.organizationId IN (SELECT org.organizationId FROM Organization org WHERE org.tenantId = :tenantId) AND u.isStatus = 1 ORDER BY u.userName")
//    List<OrganizationInfoUser> getOrganizationInfoUsersByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT u FROM User u WHERE u.ivrUserId = :ivrUserId AND u.isStatus = 1")
    User getUserByIVRUserId(String ivrUserId);

    @Query("SELECT u FROM User u WHERE u.userId IN :userIdIn")
    List<User> getUsersByIdIn(List<Integer> userIdIn);

    @Query(value = "SELECT u.* FROM rbac.userTable u WHERE u.organizationId IN (SELECT o.organizationId FROM rbac.organization o WHERE o.tenantId = ?1 AND o.organizationId = ?2) AND u.isStatus = 1 ORDER BY u.userName ASC OFFSET ?3 ROWS FETCH NEXT ?4 ROWS ONLY", nativeQuery = true)
    List<User> getUsersInBatchForGridViewASC(String tenantId, String organizationId, int offset, int limit);

    @Query(value = "SELECT u.* FROM rbac.userTable u WHERE u.organizationId IN (SELECT o.organizationId FROM rbac.organization o WHERE o.tenantId = ?1 AND o.organizationId = ?2) AND u.isStatus = 1 ORDER BY u.userName ASC", nativeQuery = true)
    List<User> getUsersAllForGridViewASC(String tenantId, String organizationId);


    @Query(value = "SELECT u.* FROM rbac.userTable u WHERE u.organizationId IN (SELECT o.organizationId FROM rbac.organization o WHERE o.tenantId = ?1 AND o.organizationId = ?2) AND u.isStatus = 1 ORDER BY u.userName DESC OFFSET ?3 ROWS FETCH NEXT ?4 ROWS ONLY", nativeQuery = true)
    List<User> getUsersInBatchForGridViewDESC(String tenantId, String organizationId, int offset, int limit);

    @Query(nativeQuery = true, value = "select u.* from rbac.userTable u where u.organizationId in " +
            "(select o.organizationId from rbac.organization o where o.tenantId = ?1 and o.organizationId = ?2) " +
            "AND u.isStatus = 1 order by u.userName DESC")
    List<User> getUsersAllForGridViewDESC(Long tenantId, Long organizationId);

    User getByEmailAddress(String emailAddress);

  //  List<User> getUserByEmailAddressAndUserNameNotIn(String emailAddress, String userName);

    @Query("select u from User u where u.groupId=:groupId AND u.isStatus = 1")
    List<User> getUserByGroupId(@Param("groupId") Integer groupId);

    @Query("SELECT u.userId, u.userName, u.emailAddress, u.phoneNumber, u.isEnabled, u.isLocked, u.isShared, org.organizationName " +
            "FROM User u " +
            "JOIN MakerChecker m ON m.entityId = u.userId " +
            "JOIN Organization org ON org.organizationId = u.organizationId " +
            "WHERE u.userId = :userId")
    Object getUserDetailsWithOrganizationName(@Param("userId") Integer userId);



    @Modifying
    @Query("UPDATE User u SET u.isShared = :isShared, u.updatedBy = :userId, u.updatedOn = :updatedOn WHERE u.organizationId = :organizationId")
    Integer updateAllUsersForOrganization(
            @Param("isShared") Boolean isShared,
            @Param("userId") int userId,
            @Param("updatedOn") Date updatedOn,
            @Param("organizationId") Long organizationId
    );

    @Query(value = "select u.userId from rbac.userTable u where u.orgCalendarId = ?1",nativeQuery = true)
    List<Integer> getUserIdsForOrgCalendarDeletion(Long orgCalendarId);


    @Query("update User u set u.orgCalendar = null where u.userId in :userIds")
    @Modifying
    void removeUserOrgCalendarMapping(@Param("userIds") List<Integer> userIds);



    @Query(nativeQuery = true,
            value = "select " +
                    "CASE WHEN t.targetKey IS  NULL OR t.targetKey = '' THEN t.name ELSE t.targetKey END as target, " +
                    "CASE WHEN o.operationKey IS  NULL OR o.operationKey = '' THEN o.name ELSE o.operationKey END as operation " +
                    "from rbac.userTable u " +
                    "join rbac.groupRole gr on (gr.groupId = u.groupId) " +
                    "join rbac.rolePermission rp on (rp.roleId = gr.roleId) " +
                    "join rbac.operation o on (o.operationId = rp.operationId) " +
                    "join rbac.target t on (t.targetId = o.targetId) " +
                    "join rbac.application a on (a.applicationId = t.applicationId) " +
                    "where u.userName = ?1 " +
                    "and a.name = ?2")
    List<Object[]> getUserPermissions(String userName, String applicationName);


    @Query(value = "select sd.scopeDefinition, sd.scopeAdditionalData " +
            "from User u " +
            "join ScopeDefinition sd on (sd.groupId = u.groupId) " +
            "join Scope s on (s.scopeId = sd.scopeId) " +
            "where u.userName = :userName " +
            "and s.scopeKey = :scopeKey")
    List<Object[]> getUserTenantScope(@Param("userName") String userName, @Param("scopeKey") String scopeKey);

    @Query(value = "select CASE WHEN s.scopeKey IS NULL OR s.scopeKey = '' THEN s.name ELSE s.scopeKey END as name, sd.scopeDefinition, sd.scopeAdditionalData " +
            "from User u " +
            "join ScopeDefinition sd on (sd.groupId = u.groupId) " +
            "join Scope s on (s.scopeId = sd.scopeId) " +
            "left join Application a on (a.applicationId = s.applicationId) " +
            "where u.userName = :userName " +
            "and (a.name = :applicationName or s.applicationId is null)")
    List<Object[]> getUserScopes(@Param("userName") String userName, @Param("applicationName") String applicationName);

    @Query(value = "SELECT CASE WHEN s.scopeKey IS NULL OR s.scopeKey = '' THEN s.name ELSE s.scopeKey END as name, sdd.objectId " +
            "FROM rbac.userTable u " +
            "JOIN rbac.scopeDefinitionDetail sdd ON sdd.groupId = u.groupId " +
            "JOIN rbac.scope s ON s.scopeId = sdd.scopeId " +
            "JOIN rbac.application a ON a.applicationId = s.applicationId " +
            "WHERE u.userName = :userName " +
            "AND a.name = :appName " +
            "ORDER BY s.scopeId", nativeQuery = true)
    List<Object[]> getUserInListScopesDetails(@Param("userName") String userName, @Param("appName") String applicationName);





    @Query(value = "select distinct r.name " +
            "from User u " +
            "join GroupRole gr on (gr.groupId = u.groupId) " +
            "join Role r on (gr.roleId = r.roleId) " +
            "join Application a on (a.applicationId = r.applicationId) " +
            "where u.userName = :userName " +
            "and a.name = :applicationName")
    List<String> getUserRoles(@Param("userName") String userName, @Param("applicationName") String applicationName);


    @Query(value = "select CASE WHEN t.targetKey IS NULL OR t.targetKey = '' THEN t.name ELSE t.targetKey END as targetName, " +
            "CASE WHEN o.operationKey IS NULL OR o.operationKey = '' THEN o.name ELSE o.operationKey END as operationName " +
            "from rbac.userTable u " +
            "join rbac.groupRole gr on (gr.groupId = u.groupId) " +
            "join rbac.rolePermission rp on (rp.roleId = gr.roleId) " +
            "join rbac.operation o on (o.operationId = rp.operationId) " +
            "join rbac.target t on (t.targetId = o.targetId) " +
            "join rbac.application a on (a.applicationId = t.applicationId) " +
            "where u.userName = :userName " +
            "and a.name = :applicationName",
            nativeQuery = true)
    List<Object[]> getUserTargetOperations(@Param("userName") String userName, @Param("applicationName") String applicationName);


    @Query(value = "select 'user' as attributeType, ma.attributeName, ad.valueReferenceId, ad.attributeDataValue from rbac.attributes_data ad " +
            "join rbac.master_attributes ma on (ad.attributeId=ma.attributeId) " +
            "join rbac.userTable u on (ad.userId=u.userId) " +
            "where ad.groupId is NULL and u.userName = :userName " +
            "union " +
            "select 'group' as attributeType, ma.attributeName, ad.valueReferenceId, ad.attributeDataValue from rbac.attributes_data ad " +
            "join rbac.master_attributes ma on (ad.attributeId=ma.attributeId) " +
            "join rbac.groupTable g on (ad.groupId=g.groupId) " +
            "join rbac.userTable u on (u.groupId=g.groupId) " +
            "where ad.userId is NULL and u.userName = :userName",
            nativeQuery = true)
    List<Object[]> getUserAttributes(@Param("userName") String userName);


    @Query(value = "select COUNT(1) from rbac.userIdentity ui " +
            "where ui.userId != :userId and ui.identityType = :identityType " +
            "and ui.identityId = :identityId",
            nativeQuery = true)
    int isUserIdentityAssociationValid(@Param("userId") Integer userId,
                                       @Param("identityType") String identityType,
                                       @Param("identityId") String identityId);


    @Query(value = "select COUNT(1) " +
            "from rbac.userTable u " +
            "join rbac.groupRole gr on (gr.groupId = u.groupId) " +
            "join rbac.role r on (r.roleId = gr.roleId) " +
            "join rbac.application a on (a.applicationId = r.applicationId) " +
            "where u.userName = :userName " +
            "and a.name = :appName",
            nativeQuery = true)
    int isUserAuthorizedForApp(@Param("userName") String userName,
                               @Param("appName") String appName);


    @Query("SELECT u FROM User u WHERE u.emailAddress = :emailAddress AND u.userName NOT IN (:userName) AND u.isStatus = 1")
    List<User> getUserByEmailAddressAndUserName(@Param("emailAddress") String emailAddress, @Param("userName")String userName);


    @Query(value = "SELECT count(*) FROM rbac.passwordHistory ph WHERE ph.userId = ?1 AND ph.setTime BETWEEN (SELECT DATEADD(HOUR, ?2, GETUTCDATE())) AND (SELECT GETUTCDATE())", nativeQuery = true)
    int noOfPasswordChanged(Integer userId, Integer hours);

}
