package com.esq.rbac.service.user.service;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.group.domain.Group;

import com.esq.rbac.service.ivrpasswordhistory.domain.IVRPasswordHistory;
import com.esq.rbac.service.password.paswordHistory.domain.PasswordHistory;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.embedded.UserIdentity;
import com.esq.rbac.service.user.vo.SSOLogoutData;
import com.esq.rbac.service.user.vo.UserWithLogoutData;
import com.esq.rbac.service.util.dal.Options;

import java.util.List;
import java.util.Map;

public interface UserDal extends BaseDal {


    User create(User user, int createUserId, String target, String operation);

    UserWithLogoutData update(User user, int updateUserId, String clientIp);

    void evictSecondLevelCacheById(Integer userId);

    void setPassword(int userId, String password);

    void setPassword(String userName, String password);

    void overrideSHA1Password(User user, String password);

    void changePassword(String userName, String oldPassword, String newPassword);

    void changeIVRPassword(String userName, String oldPassword, String newPassword);

    void updateConsecutiveIVRLoginFailures(int userId, int consecutiveIVRLoginFailures);

    void lockIVRUser(User user, int consecutiveIVRLoginFailures);

    User getById(int userId);

    User getByIVRUserId(String ivrUserId);

    boolean checkEntityPermission(int userId, Options options);

    User getByUserName(String userName);

    User getByIdentity(String identityType, String identityId);

    /**
     * Deprecated deleteById()
     * RBAC-2730
     */
    // UserWithLogoutData deleteById(int userId, Integer loggedInUserId, String clientIp, int approveDelete, Long tenantId);

    /** RBAC-2730 */
    UserWithLogoutData softDeleteById(int userId, Integer loggedInUserId, String clientIp, int approveDelete, Long tenantId);

    List<SSOLogoutData> deleteByUserName(String userName, Integer loggedInUserId, String clientIp);

    List<String> getAllUserNames();

    List<User> getList(Options options);

    List<User> getListForDispatch(Options options);

    int getCount(Options options);

    List<String> getUserPermissions(String userName, String applicationName);

    String getUserTenantScope(String userName);

    Map<String, String> getUserScopes(String userName, String applicationName, boolean isParsingRequired);

    Map<String, List<Integer>> getUserInListScopesDetails(String userName, String applicationName);

    List<String> getUserRoles(String userName, String applicationName);

    List<PasswordHistory> getPasswordHistory(String userName, int maxEntries);

    List<PasswordHistory> getPasswordHistory(int userId, int maxEntries);

    List<IVRPasswordHistory> getIVRPasswordHistory(int userId, int maxEntries);

    Map<String, List<String>> getUserTargetOperations(String userName, String applicationName);
    Map<String,Map<String,String>> getUserVariables(String userName, String applicationName);
    Map<String, List<Map<String,String>>> getUserAttributes(String userName);

    int isUserNameDuplicate(Integer userId,String userName);

    int isIvrUserIdDuplicate(Integer userId, String ivrUserId);

    List<User> getUsersNotAssignToGroup(Options options);

    List<User> getUsersOfAnotherGroup(Options options);

    List<User> searchList(Options options);

    int getSearchCount(Options options);

    boolean isUserIdentityAssociationValid(List<UserIdentity> userIdentities, Integer userId);

    boolean isUserAuthorizedForApp(String userName, String applicationName,  String appKey);

    List<Map<String,Object>> getUserIdNames(Options options);

    List<Map<String,Object>> getUserIdNamesWithScope(Options options);

    String replaceRuntimeVariables(String scope, User user, Group group);

    List<Map<String,Object>> getCustomUserInfo(Options options);

    List<Map<String,Object>> searchCustomUserInfo(Options options);
    boolean checkTenantIdInOrgAndGroup(long organizationId, long groupId);

    boolean isUserAssociatedinDispatchContact(Integer userId);
    int updateAllUsersForOrganization(Long organizationId, Boolean isShared, int userId);
    void deleteUserOrgCalendarMapping(Long calendarId);

    /*Added By Pankaj for global user search*/
    List<Map<String,Object>> searchGlobalCustomUserInfo(String searchText,String tenantList,Integer userId,Options options);
    List<Map<String,Object>> searchGlobalCustomUserInfoCount(String searchText,String tenantList,Integer userId);

    /*added by pankaj*/
	/*RBAC / IT Request RBAC-1536
	FISRMM | Password Change Policy [FIS-RMM] */
    Integer noOfPasswordChanged(Integer userId,Integer hour);

    User getUserByMakerCheckerId(Long makerCheckerId);

    List<User> getUsersByIdIn(List<Integer> userIds);

    User getByUserNameLike(String userName);

    Boolean checkTwoFactorActiveForUserAndTenant(Long organizationId);//RBAC-1562

    User getByEmailAddress(String userName);

    int isUserEmailIdDuplicate(String emailAddress, String userName);

    List<User> getByEmailAddress(String emailAddress, String userName);

    Boolean isAzureUserMgmtEnabled();

    boolean isRevokedApplicationsForUserName(String username, ChildApplication childApplication);

    User getChannelTypesForTwoFactorAuth(User user);

    Boolean isAssertPasswordsEnabled();
}
