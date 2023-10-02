/*
 * Copyright (c)2013,2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.service.role.service;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.role.domain.Role;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import com.esq.rbac.service.util.dal.Options;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface RoleDal extends BaseDal {

    Role create(Role role, int userId);

    Role update(Role role, int userId);


    Role updatePermissions(Role role);

    Optional<Role> getById(int roleId);

    void deleteById(int roleId);

    void deleteByName(String name);

    List<String> getAllNames();

    List<String> getAllNames(int applicationId);

    List<Integer> getScopeIds(int roleId);

    List<Role> getList(Options options);

    int getCount(Options options);

    int isRoleNameDuplicate(Integer applicationId, String name, Integer roleId);

    List<Role> searchList(Options options);

    int getSearchCount(Options options);

    List<Role> getRolesNotAssignedToAnyGroup(Options options);

    List<Role> getRolesAssignedToOtherGroups(int groupId, Options options);

    List<Map<String,Object>> getRoleIdNames(Options options);

    boolean areOperationsInApplication(Set<Integer> operationIds, Integer applicationId);

    List<Scope> getGroupRoleScopeIds(Map<String, List<Integer>> roleIdsList, boolean includeDefault, String loggedInUser);

    List<Scope> getGroupRoleScopeIds( List<Integer> roleId);

    List<ScopeDefinition> getRoleScopeDefinitions(List<Map<String, Integer>> roleGroupIdList);

    List<ScopeDefinition> getRoleScopeDefinitions(Integer roleId,Integer groupId);

	boolean getRoleTransition(List<Map<String, List<Integer>>> roleList);

	boolean getIsRoleTransitionAllowed(List<Integer> newRoleIds, List<Integer> existingRoleIds);

	void deleteScopeDefintionForRole(Integer roleId);

	boolean isScopeDefinedForRole(Map<String, Integer> roleId);

	Map<Integer,List<Integer>> getScopeDefinitionForRole(Integer roleId);

	Map<Integer, Set<String>> getRolesByGroup(); //RBAC-1892
}
