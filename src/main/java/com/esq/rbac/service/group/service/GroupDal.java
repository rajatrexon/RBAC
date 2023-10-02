package com.esq.rbac.service.group.service;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.group.json.RolesInGroupJson;
import com.esq.rbac.service.group.json.UsersInGroupJson;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.util.dal.Options;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public interface GroupDal extends BaseDal {

    Group create(Group group, int userId);

    Group update(Group group, int updateUserId);

    Group updateRoles(Group group, boolean isUndefinedScopesAllowed, Integer loggedInUserId);

    Group updateScopeDefinition(Group group, Integer loggedInUserId);

    Group cloneGroup(int fromGroupId, int toGroupId, boolean isUndefinedScopesAllowed, Integer loggedInUserId);

    Group getById(int groupId);

    boolean checkEntityPermission(int groupId, Options options);

    Group getByName(String name);

    void deleteById(int groupId);

    void deleteByName(String name);

    List<String> getAllNames();

    List<Integer> getScopeIds(int groupId);

    List<Scope> getScopes(int groupId, boolean includeDefault, String loggedInUser);

    List<Group> getList(Options options);

    int getCount(Options options);

    int isGroupNameDuplicate(Integer groupId,String groupName);

    List<Group> searchList(Options options);

    int getSearchCount(Options options);

    Map<String, List<String>> getAllGroupRoleScopes(Options options);

    void cloneScopeDefinitionFromGroup(int fromGroupId,  Map<Integer, List<Integer>> fromScopeToGroupIds,  boolean isUndefinedScopesAllowed, Integer loggedInUserId);


    List<Map<String,Object>> getGroupIdNames(Options options);

    List<Map<String,Object>> getGroupIdNamesWithScope(Options options);

    List<Map<String,String>> getAllGroupWithIdenticalScopeDefinition(Options options);

    List<Map<String,String>> getAllGroupWithUndefinedScopes(Options options);

    List<UsersInGroupJson> getUsersInGroupsData(Map<String, String> scopeMap);

    List<Map<String, Object>> updateUsersInGroups(List<Map<String, Object>> userGroupList, Integer loggedInUserId);

    List<RolesInGroupJson> getRolesInGroupsData(Map<String, String> scopeMap);

    Map<String, List<String>> getGroupRoleScopeNames(int groupId);

    Map<Integer, Set<String>> getRoleNamesInGroups(Set<Integer> groupIds);

    List<Group> getTemplateGroups(Options options);

    Integer removeScopeDefByScopeKeyExceptGroups(String scopeKey, List<Integer> groupIds);

    Integer removeScopeDefByScopeKey(String scopeKey);

    Number getScopeDefCountByScopeKey(String scopeKey);

    Set<UsersInGroupJson.UserJson> getUserListByGroupId(String userName, Integer groupId, Integer userId) throws Exception;
}
