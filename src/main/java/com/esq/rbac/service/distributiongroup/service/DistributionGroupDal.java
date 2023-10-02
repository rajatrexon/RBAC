package com.esq.rbac.service.distributiongroup.service;

import com.esq.rbac.service.distributiongroup.domain.DistributionGroup;
import com.esq.rbac.service.util.dal.Options;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;

public interface DistributionGroupDal {
	
	DistributionGroup create(DistributionGroup distributionGroup, Integer loggedInUserId);
	
	DistributionGroup update(DistributionGroup distributionGroup, Integer loggedInUserId);
	
	List<DistributionGroup> getDistributionGroupList(Options options);
	
//	void deleteDistributionGroupById(Integer distId);

	void deleteDistributionGroups(MultivaluedMap<String, String> map);

	DistributionGroup getDistributionGroupByDistId(Integer distId);
	
	int getSearchCount(Options options);

	int getCount(Options options);
	
	List<DistributionGroup> getSearchGroup(Options options);
	
}
