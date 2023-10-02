package com.esq.rbac.service.distributiongroup.distusergroup.service;

import com.esq.rbac.service.distributiongroup.distusergroup.domain.DistUserMap;
import com.esq.rbac.service.distributiongroup.domain.DistributionGroup;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.util.dal.Options;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;

public interface DistUserMapDal {
	
	List<User> create(DistUserMap distUserMap, Options options, Integer loggedInUserId);
	
	List<User> getUserInDistributionGroup(Options options);
	
	List<DistributionGroup> getDistributionGroup(Options options);
	
//	void deleteDistUserMapById(Integer id);

//	List<User> assignUserInDistributionGroup(Integer distId);

	void deleteUsersFromDistGroup(MultivaluedMap<String, String> map);
	
	List<User> getAssignedUserListBySearch(Options options);

	void unassignUsers(DistUserMap distUserMap, Integer loggedInUserId);

	void assignUsers(DistUserMap distUserMap, Integer loggedInUserId);

	int getAssignedSearchCount(Options options);

	int getAssignedCount(Options options);

	List<User> getUnAssignedUserListBySearch(Options options);

	List<User> getUserNotInDistributionGroup(Options options);

	int getUnAssignedSearchCount(Options options);

	int getUnAssignedCount(Options options);
	
}
