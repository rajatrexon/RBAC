package com.esq.rbac.service.distributiongroup.service;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.distributiongroup.distusergroup.repository.DistUserMapRepository;
import com.esq.rbac.service.distributiongroup.domain.DistributionGroup;
import com.esq.rbac.service.distributiongroup.repository.DistributionGroupRepository;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.util.AuditLogHelperUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DistributionGroupDalJpa extends BaseDalJpa implements DistributionGroupDal {

	private static final Logger log = LoggerFactory.getLogger(DistributionGroupDalJpa.class);




	private EntityManager em;

	@Autowired
	public void setEntityManager(EntityManager em) {
		log.trace("setEntityManager; {};", em);
		this.em =em;

	}
	private static final Map<String, String> SORT_COLUMNS;



	private AuditLogService auditLogDal;
	@Autowired
	public void setAuditLogService(AuditLogService auditLogDal) {
		log.trace("setAuditLogService; {};", auditLogDal);
		this.auditLogDal =auditLogDal;

	}


	private DistributionGroupRepository distributionGroupRepository;

	@Autowired
	public void setDistributionGroupRepository(DistributionGroupRepository distributionGroupRepository) {
		log.trace("setDistributionGroupRepository; {};",distributionGroupRepository);
		this.distributionGroupRepository =distributionGroupRepository;

	}


	private DistUserMapRepository distUserMapRepository;

	@Autowired
	public void setDistUserMapRepository(DistUserMapRepository distUserMapRepository) {
		log.trace("setDistUserMapRepository; {};",distUserMapRepository);
		this.distUserMapRepository =distUserMapRepository;

	}

	public static final String DUPLICATED_DIST_GROUP = "duplicatedDistGrp";
	public static final String DUPLICATED_DIST_GROUP_NAME = "duplicatedName";

	static {
		SORT_COLUMNS = new TreeMap<String, String>();
		SORT_COLUMNS.put("distId", "dist.distId");
		SORT_COLUMNS.put("distName", "dist.distName");
	}




	/*
	 * @Override
	 * 
	 * @Transactional(propagation = Propagation.REQUIRED) public DistributionGroup
	 * create(DistributionGroup distributionGroup) { try {
	 * 
	 * OptionFilter optionFilter = new OptionFilter();
	 * optionFilter.addFilter("distName", distributionGroup.getDistName());
	 * optionFilter.addFilter("tenantId", distributionGroup.getTenantId() + "");
	 * Options options = new Options(optionFilter); List<DistributionGroup>
	 * checkIfExists = getDistributionGroupList(options); if (checkIfExists != null
	 * && !checkIfExists.isEmpty()) { ErrorInfoException e = new
	 * ErrorInfoException("validationError"); e.getParameters().put("value",
	 * "Duplicate Distribution Group Found"); throw e;
	 * 
	 * } else em.persist(distributionGroup); return distributionGroup; } catch
	 * (Exception e) { e.printStackTrace(); throw e; } }
	 */

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public DistributionGroup create(DistributionGroup distributionGroup, Integer loggedInUserId) {
		try {
			if (distributionGroup == null) {
				throw new IllegalArgumentException();
			}

			int distDuplicate = isDistNameDuplicate(distributionGroup.getDistId(), distributionGroup.getDistName());
			if (distDuplicate > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append(DUPLICATED_DIST_GROUP).append("; ");
				sb.append(DUPLICATED_DIST_GROUP_NAME).append("=").append(distributionGroup.getDistName());
				log.info("create; {}", sb.toString());
				ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_DIST_GROUP, sb.toString());
				errorInfo.getParameters().put(DUPLICATED_DIST_GROUP_NAME, distributionGroup.getDistName());
				log.info("create; disterrorInfo={}", errorInfo);
				throw errorInfo;
			}

			distributionGroup.setCreatedBy(loggedInUserId);
			distributionGroup.setCreatedOn(new Date());
			//em.persist(distributionGroup);
			distributionGroupRepository.save(distributionGroup);
			if(loggedInUserId != null && loggedInUserId > 0) {
			Map<String, String> objectChanges = setNewObjectChangeSetLocal(distributionGroup);
			auditLogDal.createAsyncLog(loggedInUserId, distributionGroup.getDistName(), "DistributionGroup", "Create",
					objectChanges);
			}
			return distributionGroup;
		} catch (Exception e) {
			log.error("{}", e.getMessage());
			throw e;
		}
	}

	private Map<String, String> setNewObjectChangeSetLocal(DistributionGroup newGrp) {
		AuditLogHelperUtil logHelperUtil = new AuditLogHelperUtil();
		logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_DISTNAME, newGrp.getDistName());
		logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_DESCRIPTION, newGrp.getDescription());
		logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_TENANTNAME,
				Lookup.getTenantNameById(newGrp.getTenantId().longValue()));
		return logHelperUtil.getObjectChangeSet();
	}

	public int isDistNameDuplicate(Integer distId, String distName) {
		DistributionGroup distGrp = getByDistName(distName);
		if (distGrp != null) {
			if (distId != null && distId.intValue() > 0) {
				if (distId.equals(distGrp.getDistId())) {
					return 0;
				}
			}
			return 1;
		}
		return 0;
	}

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public DistributionGroup getByDistName(String distName) {
		try {
//			TypedQuery<DistributionGroup> query = em.createNamedQuery("getDistributionByName", DistributionGroup.class);
//			query.setParameter("distName", distName);
//			return query.getSingleResult();

			return distributionGroupRepository.getDistributionByName(distName);

		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public DistributionGroup update(DistributionGroup distributionGroup, Integer loggedInUserId) {
		try {

			if (distributionGroup == null) {
				throw new IllegalArgumentException();
			}
			if (distributionGroup.getDistId() == null) {
				ErrorInfoException e = new ErrorInfoException("validationError");
				e.getParameters().put("value", "Distribution Id is mandatory");
				throw e;
			}
			Integer distId = distributionGroup.getDistId();
		//	DistributionGroup existingGrp = em.find(DistributionGroup.class, distId);
			Optional<DistributionGroup> existingGrp = distributionGroupRepository.findById(distId);
			if (existingGrp.isEmpty()) {
				ErrorInfoException e = new ErrorInfoException("invalidGroupId", "distributionId invalid");
				e.getParameters().put("distId", Integer.toString(distId));
				throw e;
			}
			Map<String, String> objectChanges = setObjectChangeSetLocal(existingGrp.get(), distributionGroup);

			int distDuplicate = isDistNameDuplicate(distributionGroup.getDistId(), distributionGroup.getDistName());
			if (distDuplicate > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append(DUPLICATED_DIST_GROUP).append("; ");
				sb.append(DUPLICATED_DIST_GROUP_NAME).append("=").append(distributionGroup.getDistName());
				log.info("create; {}", sb.toString());
				ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_DIST_GROUP, sb.toString());
				errorInfo.getParameters().put(DUPLICATED_DIST_GROUP, distributionGroup.getDistName());
				log.info("create; disterrorInfo={}", errorInfo);
				throw errorInfo;
			}

			existingGrp.get().setDescription(distributionGroup.getDescription());
			existingGrp.get().setDistName(distributionGroup.getDistName());
			existingGrp.get().setTenantId(distributionGroup.getTenantId());
			existingGrp.get().setUpdatedBy(loggedInUserId);
			existingGrp.get().setUpdatedOn(new Date());
			//DistributionGroup retUser = em.merge(existingGrp);
			DistributionGroup retUser = distributionGroupRepository.save(existingGrp.get());
			if(loggedInUserId != null && loggedInUserId > 0) {
			auditLogDal.createAsyncLog(loggedInUserId, retUser.getDistName(), "DistributionGroup", "Update",
					objectChanges);
			}
			return distributionGroup;
		} catch (Exception e) {
//		e.printStackTrace();
			throw e;
		}
	}

	private Map<String, String> setObjectChangeSetLocal(DistributionGroup existingGrp, DistributionGroup newGrp) {
		AuditLogHelperUtil logHelperUtil = new AuditLogHelperUtil();
		logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_DISTID, newGrp.getDistId().toString());
		logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_DISTNAME, newGrp.getDistName(), existingGrp.getDistName(),
				null, null);
		logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_DESCRIPTION, newGrp.getDescription(),
				existingGrp.getDescription(), null, null);
		return logHelperUtil.getObjectChangeSet();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteDistributionGroups(MultivaluedMap<String, String> map) {
		try {
			if (map != null) {

				if (map.containsKey("distGroups")) {
					Integer loggedInUserId = 0;
					if (map.containsKey("userId")) {
						loggedInUserId = Integer.parseInt(map.get("userId").get(0));
					}
					String[] distgroups = map.get("distGroups").get(0).split(",");
					for (String distId : distgroups) {
					//	DistributionGroup existingGrp = em.find(DistributionGroup.class, Integer.parseInt(distId));
						Optional<DistributionGroup> existingGrp = distributionGroupRepository.findById(Integer.parseInt(distId));
						deleteDistUserMapById(Integer.parseInt(distId));
						deleteDistributionGroupById(Integer.parseInt(distId));
						if(loggedInUserId != null && loggedInUserId > 0) {
						Map<String, String> objectChanges = setNewObjectChangeSetLocal(existingGrp.get());
						auditLogDal.createAsyncLog(loggedInUserId, existingGrp.get().getDistName(), "DistributionGroup",
								"Delete", objectChanges);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ErrorInfoException(e.getMessage());
		}

	}

	private void deleteDistributionGroupById(Integer distId) {
//		TypedQuery<DistributionGroup> query = em.createNamedQuery("deleteDistributionGroupById",
//				DistributionGroup.class);
//		query.setParameter("distId", distId);
//		query.executeUpdate();
		distributionGroupRepository.deleteById(distId);
	}

	private void deleteDistUserMapById(Integer distId) {

//		TypedQuery<DistUserMap> query = em.createNamedQuery("deleteDistUserMapById", DistUserMap.class);
//		query.setParameter("distId", distId);
//		query.executeUpdate();

		distUserMapRepository.deleteById(distId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<DistributionGroup> getDistributionGroupList(Options options) {
		Filters filters = prepareFilters(options);
		return (List<DistributionGroup>) filters.getList(em, DistributionGroup.class,
				"select dist from DistributionGroup dist", options, SORT_COLUMNS);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public DistributionGroup getDistributionGroupByDistId(Integer distId) {
		try {
//			TypedQuery<DistributionGroup> query = em.createNamedQuery("getDistributionGroupByDistId",
//					DistributionGroup.class);
//			query.setParameter("distId", distId);
//			return query.getSingleResult();
			return  distributionGroupRepository.getDistributionGroupByDistId(distId);
		} catch (Exception e) {
			return null;
		}
	}

	private Filters prepareFilters(Options options) {

		Filters result = new Filters();
		OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
		Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
		if (filters != null) {
			String distId = filters.get("distId");
			if (distId != null && distId.length() > 0) {
				result.addCondition("dist.distId=:distId");
				result.addParameter("distId", Long.valueOf(distId));
			}

			String distName = filters.get("distName");
			if (distName != null && distName.length() > 0) {
				result.addCondition("dist.distName=:distName");
				result.addParameter("distName", distName);
			}

			String loggedInTenantId = filters.get("loggedInTenantId");
			if (loggedInTenantId != null && loggedInTenantId.length() > 0) {
				result.addCondition("dist.tenantId=:tenantId");
				result.addParameter("tenantId", Long.valueOf(loggedInTenantId));
			}
			
			String tenantId = filters.get("tenantId");
			if (loggedInTenantId == null && tenantId != null && tenantId.length() > 0) {
				result.addCondition("dist.tenantId=:tenantId");
				result.addParameter("tenantId", Long.valueOf(tenantId));
			}

		}
		return result;
	}

	@Override
	public int getSearchCount(Options options) {
		int count = 0;
		Filters filters = prepareFilters(options);
		filters.addCondition(" ( lower(dist.distName) like :q or lower(dist.description) like :q ) ");
		filters.addParameter(SearchUtils.SEARCH_PARAM,
				SearchUtils.wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));
		count = filters.getCount(em, "select count(dist) from DistributionGroup dist");
		return count;
	}

	@Override
	public List<DistributionGroup> getSearchGroup(Options options) {
		Filters filters = prepareFilters(options);
		filters.addCondition("  ( lower(dist.distName) like :q or lower(dist.description) like :q ) ");
		filters.addParameter(SearchUtils.SEARCH_PARAM,
				SearchUtils.wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));
		List<DistributionGroup> distGroup = filters.getList(em, DistributionGroup.class,
				"select dist from DistributionGroup dist", options, SORT_COLUMNS);
		return distGroup;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int getCount(Options options) {
		Filters filters = prepareFilters(options);
		return filters.getCount(em, "select count(dist) from DistributionGroup dist");
	}

}
