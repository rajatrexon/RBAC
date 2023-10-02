package com.esq.rbac.service.distributiongroup.distusergroup.service;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.distributiongroup.distusergroup.domain.DistUserMap;
import com.esq.rbac.service.distributiongroup.distusergroup.repository.DistUserMapRepository;
import com.esq.rbac.service.distributiongroup.domain.DistributionGroup;
import com.esq.rbac.service.distributiongroup.repository.DistributionGroupRepository;
import com.esq.rbac.service.distributiongroup.service.DistributionGroupDal;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.AuditLogHelperUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DistUserMapDalJpa extends BaseDalJpa implements DistUserMapDal {

	private static final Logger log = LoggerFactory.getLogger(DistUserMapDalJpa.class);


	@Autowired
	private EntityManager em;

	@Autowired
	private UserDal userDal;


	@Autowired
	private DistributionGroupDal distributionGroupDal;

	@Autowired
	DistributionGroupRepository distributionGroupRepository;
	private static final Map<String, String> SORT_COLUMNS;

	@Autowired
	private AuditLogService auditLogDal;

	static {
		SORT_COLUMNS = new TreeMap<String, String>();
		SORT_COLUMNS.put("distId", "distUser.distId");
		SORT_COLUMNS.put("userId", "distUser.userId");
	}



@Autowired
private DistUserMapRepository distUserMapRepository;





	@Deprecated
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<User> create(DistUserMap distUserMap, Options options, Integer loggedInUserId) {
		try {
			assignUsers(distUserMap, loggedInUserId);
			return getUserInDistributionGroup(options);
		} catch (Exception e) {
			throw e;
		}
	}

	private boolean checkIfUserandDistSameTenant(User user, DistributionGroup distMaster) {
		Boolean isSame = Boolean.FALSE;
		if (user.getIsShared() != null && user.getIsShared()) {
			isSame = Boolean.TRUE;
		}else {
			Long userTenant = Lookup.getTenantIdByOrganizationId(user.getOrganizationId());
			if (userTenant != null && userTenant.intValue() == distMaster.getTenantId())
				isSame = Boolean.TRUE;
		}
		return isSame;
	}

	private DistUserMap getMappingByDistIdAndUserId(Integer distId, Integer userId) {
		try {
//			TypedQuery<DistUserMap> query = em.createNamedQuery("getByUserIdandDistId", DistUserMap.class);
//			query.setParameter("distId", distId);
//			query.setParameter("userId", userId);
//			DistUserMap mapping = query.getSingleResult();

			DistUserMap mapping = distUserMapRepository.getByUserIdandDistId(distId,userId);
			return mapping;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	@Deprecated
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteUsersFromDistGroup(MultivaluedMap<String, String> map) {
		/*
		 * if (id != null) { TypedQuery<DistUserMap> query =
		 * em.createNamedQuery("deleteDistUserMapById", DistUserMap.class);
		 * query.setParameter("id", id); query.executeUpdate(); }
		 */

		try {

			if (map.containsKey("distId")) {

				int distId = Integer.parseInt(map.getFirst("distId"));

				if (map.containsKey("userIds")) {

					String userIdArr[] = map.get("userIds").get(0).split(",");
					Set<Integer> userIds = new HashSet<>();

					for (String userId : userIdArr) {

						userIds.add(Integer.parseInt(userId));
					}

//					TypedQuery<DistUserMap> query = em.createNamedQuery("deleteUsersFromDistributionGroup",
//							DistUserMap.class);
//					query.setParameter("distId", distId);
//					query.setParameter("userIds", userIds);
//					query.executeUpdate();

					distUserMapRepository.deleteUsersFromDistributionGroup(distId,userIds);

				}

			}

		} catch (Exception e) {

			throw new ErrorInfoException(e.getMessage());
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<User> getUserInDistributionGroup(Options options) {
		OptionFilter of = (OptionFilter) options.getOptions().get(1);
		Filters filters = new Filters();
		filters.addParameter("distId", Integer.valueOf(of.getFilter("distId")));
		filters.addCondition(
				"(" + "u.userId In (select dum.userId from DistUserMap dum where dum.distId= :distId)" + ")");

		return filters.getList(em, User.class, "select distinct u from User u ", options, SORT_COLUMNS);
	}
	  

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<DistributionGroup> getDistributionGroup(Options options) {
		Filters filters = prepareDistributionsFilters(options);
		if (filters != null && !filters.getParameters().isEmpty())
			return (List<DistributionGroup>) filters.getList(em, DistributionGroup.class,
					"select distGr from DistributionGroup distGr", options, SORT_COLUMNS);
		else
			return null;
	}

	private Filters prepareDistributionsFilters(Options options) {
		Filters result = new Filters();
		OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
		Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
		if (filters != null) {
			String userId = filters.get("userId");
			if (userId != null && userId.length() > 0) {
				result.addCondition("distGr.distId in (select dm.distId from DistUserMap dm where dm.userId=:userId)");
				result.addParameter("userId", Integer.valueOf(userId));
			}

			String userName = filters.get("userName");
			if (userName != null && userName.length() > 0) {
				result.addCondition(
						"distGr.distId IN (select dm.distId from DistUserMap dm where dm.userId IN (select u.userId from User u where u.userName=:userName))");
				result.addParameter("userName", userName);
			}

			String firstName = filters.get("firstName");
			if (firstName != null && firstName.length() > 0) {
				result.addCondition(
						"distGr.distId IN (select dm.distId from DistUserMap dm where dm.userId IN (select u.userId from User u where u.firstName=:firstName))");
				result.addParameter("firstName", firstName);
			}
		}

		return result;
	}

	@Override
	public List<User> getAssignedUserListBySearch(Options options) {

		OptionFilter of = (OptionFilter) options.getOptions().get(1);
		Filters filters = new Filters();
		filters.addParameter("distId", Integer.valueOf(of.getFilter("distId")));
		filters.addParameter(SearchUtils.SEARCH_PARAM,
				SearchUtils.wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));

		filters.addCondition("(" + "(lower(u.userName) like :q or lower(u.emailAddress) like :q) "
				+ "and u.userId In (select dum.userId from DistUserMap dum where dum.distId= :distId)" + ")");

		return filters.getList(em, User.class, "select distinct u from User u ", options, SORT_COLUMNS);

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void unassignUsers(DistUserMap distUserMap, Integer loggedInUserId) {
		try {
			DistributionGroup distMaster = distributionGroupDal.getDistributionGroupByDistId(distUserMap.getDistId());
			if (distUserMap != null && distUserMap.getDistId() != null) {
				if (distUserMap.getUserIdSet() != null && !distUserMap.getUserIdSet().isEmpty()) {
//					TypedQuery<DistUserMap> query = em.createNamedQuery("deleteUsersFromDistributionGroup",
//							DistUserMap.class);
//					query.setParameter("distId", distUserMap.getDistId());
//					query.setParameter("userIds", distUserMap.getUserIdSet());
//					query.executeUpdate();
					distUserMapRepository.deleteUsersFromDistributionGroup(distUserMap.getDistId(),distUserMap.getUserIdSet());

				}

				String userList = "";
				for (Integer userId : distUserMap.getUserIdSet()) {
					User user = userDal.getById(userId);
					if (user != null) {
						userList += ", " + user.getUserName();
					}
				}
				if (userList != "") {
					userList = userList.substring(1);

					Map<String, String> objectChanges = setNewObjectChangeSetLocal(distMaster,
							OBJECTCHANGES_UNASSIGNED_USERS, userList);
					auditLogDal.createAsyncLog(loggedInUserId, distMaster.getDistName(), "DistributionGroup", "UserMap",
							objectChanges);
				}

			}
		} catch (Exception e) {
			throw new ErrorInfoException(e.getMessage());
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void assignUsers(DistUserMap distUserMap, Integer loggedInUserId) {
		try {
			String userList = "";
			Set<Integer> userIds = distUserMap.getUserIdSet();
			DistributionGroup distMaster = distributionGroupDal.getDistributionGroupByDistId(distUserMap.getDistId());
			for (Integer userId : userIds) {
				DistUserMap checkIfdistUserMap = getMappingByDistIdAndUserId(distUserMap.getDistId(), userId);
				if (checkIfdistUserMap == null) {
					User user = userDal.getById(userId);
					DistUserMap distUserMapDb = new DistUserMap();
					if (user != null) {
						if (checkIfUserandDistSameTenant(user, distMaster)) {
							userList += ", " + user.getUserName();
							distUserMapDb.setDistId(distUserMap.getDistId());
							distUserMapDb.setUserId(userId);
							distUserMapDb.setCreatedOn(new Date());
							distUserMapDb.setCreatedBy(loggedInUserId);
//							em.persist(distUserMapDb);
							distUserMapRepository.save(distUserMapDb);
						}
					}
				}
			}
			if (userList != "") {
				userList = userList.substring(1);

				Map<String, String> objectChanges = setNewObjectChangeSetLocal(distMaster, OBJECTCHANGES_ASSIGNED_USERS,
						userList);
				auditLogDal.createAsyncLog(loggedInUserId, distMaster.getDistName(), "DistributionGroup", "UserMap",
						objectChanges);
			}

		} catch (Exception e) {
			throw e;
		}
	}

	private Map<String, String> setNewObjectChangeSetLocal(DistributionGroup distMaster,
			String objectchangesAssignedUsers, String userList) {
		AuditLogHelperUtil logHelperUtil = new AuditLogHelperUtil();
		logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_DISTNAME, distMaster.getDistName());
		logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_TENANTNAME, Lookup.getTenantNameById(distMaster.getTenantId().longValue()));
		logHelperUtil.putToObjectChangeSet(objectchangesAssignedUsers, userList);
		return logHelperUtil.getObjectChangeSet();
	}

	@Override
	public int getAssignedSearchCount(Options options) {
		OptionFilter of = (OptionFilter) options.getOptions().get(1);
		Filters filters = new Filters();
		filters.addParameter("distId", Integer.valueOf(of.getFilter("distId")));
		filters.addParameter(SearchUtils.SEARCH_PARAM,
				SearchUtils.wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));

		filters.addCondition("(" + "(lower(u.userName) like :q or lower(u.emailAddress) like :q) "
				+ "and u.userId In (select dum.userId from DistUserMap dum where dum.distId= :distId)" + ")");

		return filters.getCount(em, "select count(distinct u) from User u ");
	}

	@Override
	public int getAssignedCount(Options options) {
		OptionFilter of = (OptionFilter) options.getOptions().get(1);
		Filters filters = new Filters();
		filters.addParameter("distId", Integer.valueOf(of.getFilter("distId")));
		filters.addCondition(
				"(" + "u.userId In (select dum.userId from DistUserMap dum where dum.distId= :distId)" + ")");
		return filters.getCount(em, "select count(u) from User u");
	}

	@Override
	public List<User> getUnAssignedUserListBySearch(Options options) {
		OptionFilter of = (OptionFilter) options.getOptions().get(1);
		Filters filters = new Filters();
		Integer distId = Integer.valueOf(of.getFilter("distId"));
//		DistributionGroup distMaster = em.find(DistributionGroup.class, distId);
		Optional<DistributionGroup> distMaster = distributionGroupRepository.findById(distId);
		filters.addParameter("distId", distId);
		filters.addParameter("tenantId", distMaster.get().getTenantId());
		filters.addParameter(SearchUtils.SEARCH_PARAM,
				SearchUtils.wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));

		filters.addCondition("(" + "(lower(u.userName) like :q or lower(u.emailAddress) like :q) "
				+ "and u.userId NOT IN (select dum.userId from DistUserMap dum where dum.distId= :distId)"
				+ "and (u.organizationId is Null or u.organizationId In (select o.organizationId from Organization o where o.tenantId= :tenantId)"
				+ "or u.userId IN (select ur.userId from User ur where ur.isShared = 1 and ur.userId NOT IN (select dm.userId from DistUserMap dm where dm.distId= :distId)))"
				+ ")");

		return filters.getList(em, User.class, "select distinct u from User u", options, SORT_COLUMNS);

	}

	@Override
	public List<User> getUserNotInDistributionGroup(Options options) {
		OptionFilter of = (OptionFilter) options.getOptions().get(1);
		Filters filters = new Filters();
		Integer distId = Integer.valueOf(of.getFilter("distId"));
	//	DistributionGroup distMaster = em.find(DistributionGroup.class, distId);
		Optional<DistributionGroup> distMaster = distributionGroupRepository.findById(distId);
		System.out.println("==================>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+distMaster);
		filters.addParameter("distId", distId);
		filters.addParameter("tenantId", distMaster.get().getTenantId());
		filters.addCondition("(" + " u.userId NOT IN (select dum.userId from DistUserMap dum where dum.distId= :distId)"
				+ "and (u.organizationId is Null or u.organizationId In (select o.organizationId from Organization o where o.tenantId= :tenantId)"
				+ "or u.userId IN (select ur.userId from User ur where ur.isShared = 1 and ur.userId NOT IN (select dm.userId from DistUserMap dm where dm.distId= :distId)))"
				+ ")");

		return filters.getList(em, User.class, "select distinct u from User u ", options, SORT_COLUMNS);
	}

	@Override
	public int getUnAssignedSearchCount(Options options) {
		OptionFilter of = (OptionFilter) options.getOptions().get(1);
		Filters filters = new Filters();
		Integer distId = Integer.valueOf(of.getFilter("distId"));
	//	DistributionGroup distMaster = em.find(DistributionGroup.class, distId);
		Optional<DistributionGroup> distMaster = distributionGroupRepository.findById(distId);
		filters.addParameter("distId", distId);
		filters.addParameter("tenantId", distMaster.get().getTenantId());
		filters.addParameter(SearchUtils.SEARCH_PARAM,
				SearchUtils.wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));

		filters.addCondition("(" + "(lower(u.userName) like :q or lower(u.emailAddress) like :q) "
				+ "and u.userId NOT IN (select dum.userId from DistUserMap dum where dum.distId= :distId)"
				+ "and (u.organizationId is Null or u.organizationId In (select o.organizationId from Organization o where o.tenantId= :tenantId)"
				+ "or u.userId IN (select ur.userId from User ur where ur.isShared = 1 and ur.userId NOT IN (select dm.userId from DistUserMap dm where dm.distId= :distId)))"
				+ ")");

		return filters.getCount(em, "select count(distinct u) from User u ");
	}

	@Override
	public int getUnAssignedCount(Options options) {
		OptionFilter of = (OptionFilter) options.getOptions().get(1);
		Filters filters = new Filters();
		Integer distId = Integer.valueOf(of.getFilter("distId"));
	//	DistributionGroup distMaster = em.find(DistributionGroup.class, distId);
		Optional<DistributionGroup> distMaster = distributionGroupRepository.findById(distId);
		filters.addParameter("distId", distId);
		filters.addParameter("tenantId", distMaster.get().getTenantId());
		filters.addCondition("(" + " u.userId NOT IN (select dum.userId from DistUserMap dum where dum.distId= :distId)"
				+ "and (u.organizationId is Null or u.organizationId In (select o.organizationId from Organization o where o.tenantId= :tenantId)"
				+ "or u.userId IN (select ur.userId from User ur where ur.isShared = 1 and ur.userId NOT IN (select dm.userId from DistUserMap dm where dm.distId= :distId)))"
				+ ")");
		return filters.getCount(em, "select count(u) from User u");
	}

}
