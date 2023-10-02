/*
 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

package com.esq.rbac.service.scope.scopeconstraint.service;

import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.masterattributes.domain.MasterAttributes;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import com.esq.rbac.service.scope.scopeconstraint.repository.ScopeConstraintRepository;
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import com.esq.rbac.service.scope.service.ScopeDal;
import com.esq.rbac.service.util.ExternalDataAccessRBAC;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.util.externaldatautil.ExternalDataAccessUtil;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Todo Inprogress (make repo and managed cache)
@Service
public class ScopeConstraintDalJpa implements ScopeConstraintDal {

	private static final Logger log = LoggerFactory
			.getLogger(ScopeConstraintDalJpa.class);

	private EntityManager em;

	private ScopeDal scopeDal;
	public static final String APPLICATION_RBAC = "RBAC";
	private ExternalDataAccessRBAC externalDataAccessRBAC;
	private ExternalDataAccessUtil externalDataAccessUtil;

	@Autowired
	private ScopeConstraintRepository scopeConstraintRepository;
		
	@Autowired
	public void setExternalDataAccessUtil(ExternalDataAccessUtil externalDataAccessUtil) {
		log.trace("setExternalDataAccessUtil; externalDataAccessUtil={}", externalDataAccessUtil);
		this.externalDataAccessUtil = externalDataAccessUtil;
	}

	@Autowired
	public void setExternalDataAccessRBAC(ExternalDataAccessRBAC externalDataAccessRBAC) {
		this.externalDataAccessRBAC = externalDataAccessRBAC;
	}

	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		log.trace("setEntityManager");
		this.em = em;
	}

	@Autowired
	public void setDependencies(ScopeDal scopeDal) {
		log.trace("setDependencies; scopeDal={}", scopeDal);
		this.scopeDal = scopeDal;
	}

	@Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public ScopeConstraint getById(int constraintId) {
		return em.find(ScopeConstraint.class, constraintId);
	}
	
	@Override
	public void clearCache(){
		em.getEntityManagerFactory().getCache().evict(ScopeConstraint.class);
	}
	
	@Override
	public void clearQueryCache(){
//Todo 		((JpaCache)em.getEntityManagerFactory().getCache()).clearQueryCache("getByScopeName");
//		((JpaCache)em.getEntityManagerFactory().getCache()).clearQueryCache("getDenyScope");
//		((JpaCache)em.getEntityManagerFactory().getCache()).clearQueryCache("getByScopeId");
	}

	@Override
//	@CacheEvict  /*used to clear second level cache */
	public ScopeConstraint getByScopeName(String scopeName) {
//		TypedQuery<ScopeConstraint> query = em.createNamedQuery(
//				"getByScopeName", ScopeConstraint.class);
//		query.setParameter("scopeName", scopeName.toLowerCase());
		try {
//			return query.getSingleResult();
			return scopeConstraintRepository.getByScopeName(scopeName.toLowerCase());

		} catch (NoResultException e) {
			log.error("getByScopeName; scopeName={}; NoResultException={}",
					scopeName, e);
			return null;
		} catch (Exception e) {
			log.error("getByScopeName; scopeName={}; Exception={}", scopeName,
					e);
			return null;
		}
	}

	@Override
//	@CacheEvict
	public ScopeConstraint getByScopeId(int scopeId) {
//		TypedQuery<ScopeConstraint> query = em.createNamedQuery("getByScopeId",
//				ScopeConstraint.class);
//		query.setParameter("scopeId", scopeId);
		try {
		//	return query.getSingleResult();
			return scopeConstraintRepository.getByScopeId(scopeId);
		} catch (NoResultException e) {
			log.error("getByScopeId; scopeId={}; NoResultException={}",
					scopeId, e);
			return null;
		}
	}

	@SuppressWarnings("unused")
//	@CacheEvict
	private ScopeConstraint getDenyScope(int applicationId) {
//		TypedQuery<ScopeConstraint> query = em.createNamedQuery("getDenyScope",
//				ScopeConstraint.class);
//		query.setParameter("applicationId", applicationId);
		try {
//			return query.getSingleResult();
			return scopeConstraintRepository.getDenyScope(applicationId);
		} catch (NoResultException e) {
			log.info("getDenyScope; applicationId={}; NoResultException={}",
					applicationId, e);
			return null;
		}
	}

	private Filters prepareFilters(Options options) {

		Filters result = new Filters();
		OptionFilter optionFilter = options == null ? null : options
				.getOption(OptionFilter.class);
		Map<String, String> filters = optionFilter == null ? null
				: optionFilter.getFilters();
		if (filters != null) {

			String scopeId = filters.get("scopeId");
			if (scopeId != null && scopeId.length() > 0) {
				result.addCondition("s.scopeId = :scopeId");
				result.addParameter("scopeId", Integer.valueOf(scopeId));
			}

			String scopeName = filters.get("scopeName");
			if (scopeName != null && scopeName.length() > 0) {
				result.addCondition("s.scopeName = :scopeName");
				result.addParameter("scopeName", scopeName);
			}
		}
		return result;
	}

	//just used for logging purpose as there is no PROC exposed by OB/IMS
	@Override
	public void executeExternaUpdate(Group group) {

		Set<ScopeDefinition> scopeDefinitions = group.getScopeDefinitions();
		log.info("executeExternaUpdate; scopeDefinitionKeys={}",
				scopeDefinitions);

		for (ScopeDefinition sd : scopeDefinitions) {
			Integer scopeId = sd.getScopeId();
			ScopeConstraint scopeConstraint = getByScopeId(scopeId);
			Scope scope = scopeDal.getById(scopeId);
			///String definition = scopeDefinitions.get(scopeId).getScopeDefinition();
			String definition = sd.getScopeDefinition();

			if (scopeConstraint != null) {
				if (scopeConstraint.getComparators()!=null && scopeConstraint.getComparators().contains("INLIST")) {
					if (definition != null) {
						definition = definition.replace("INLIST[", "");
						definition = definition.replace("]", "");
						definition = definition.replace("'", "");
						String[] defList = definition.split(",");
						boolean booleanAll = false;
						List<Integer> arguments = new ArrayList<Integer>();
						if(defList!=null){
							for (String keyList : defList) {
								if (keyList!=null && keyList.substring(0, keyList.indexOf(";"))
										.equals("-1")) {
									booleanAll = true;
									break;
								}
								arguments.add(Integer.parseInt(keyList.substring(0,
										keyList.indexOf(";"))));
							}
						}
						for (Object[] objectArray : getTargetDetailsByGroupScopeId(
								group.getGroupId(), scopeId)) {
							log.info(
									"executeExternaUpdate; executeProc OBPROC with parameters groupId={}, scopeName={}, selectedIds={}, booleanAll={} scopeDetails={}",
									group.getGroupId(), scope.getName(),
									arguments, booleanAll,
									ScopeDetails.toScopeDetails(objectArray));
							/*try {
								externalDbDal.executeProc(
										scopeConstraint.getApplicationName(),
										"OBPROC", arguments);
							} catch (Exception e) {
								// log.error("executeExternaUpdate; Exception={}",
								// e);
							}*/
						}

					}
				}
				else if (scopeConstraint.getComparators()!=null && scopeConstraint.getComparators().contains("NEGATION")) {
					List<Object> arguments = new ArrayList<Object>();
					boolean booleanNegation = false;
					if (definition != null && definition.contains("![")) {
						booleanNegation = true;
						definition = definition.replace("![", "");
						definition = definition.replace("]", "");
						arguments.add(true);
					} else {
						arguments.add(false);
					}

					for (Object[] objectArray : getTargetDetailsByGroupScopeId(
							group.getGroupId(), scopeId)) {
						log.info(
								"executeExternaUpdate; executeProc HDPROC with parameters groupId={}, scopeName={}, scopeDef={}, booleanNegation={} scopeDetails={}",
								group.getGroupId(), scope.getName(), definition,
								booleanNegation,
								ScopeDetails.toScopeDetails(objectArray));
						/*try {
							externalDbDal.executeProc(
									scopeConstraint.getApplicationName(), "HDPROC",
									arguments);
						} catch (Exception e) {
							// log.error("executeExternaUpdate; Exception={}",
							// e);
						}*/
					}

				}
			} 
		}
	}

	private List<Object[]> getTargetDetailsByGroupScopeId(Integer groupId,
			Integer scopeId) {
		Query query = em.createNamedQuery("getTargetDetailsByGroupScopeId",
				ScopeDetails.class);
		query.setParameter(1, groupId);
		query.setParameter(2, scopeId);
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		return resultList;
	}
	
	private List<ScopeConstraint> getDefaultScopeTypeByAppId(Integer applicationId){
		TypedQuery<ScopeConstraint> query = em.createQuery("select sc from ScopeConstraint sc where sc.applicationId = :applicationId and sc.scopeName='DEFAULTSCOPE' ", ScopeConstraint.class);
		query.setParameter("applicationId", applicationId);
		return query.getResultList();
	}

	@Override
	public List<ScopeConstraint> getConstraints(Options options) {
		/*OptionFilter optionFilter = options == null ? null : options
				.getOption(OptionFilter.class);
		Map<String, String> filtersMap = optionFilter == null ? null
				: optionFilter.getFilters();
		if (filtersMap != null) {
			String scopeId = filtersMap.get("scopeId");
			if (scopeId != null && scopeId.length() > 0) {
				Integer appId = scopeDal.getById(Integer.valueOf(scopeId))
						.getApplicationId();
				ScopeConstraint scopeConstraint = getDenyScope(appId);
				if (scopeConstraint != null) {
					scopeConstraint.setSqlQuery("");
					List<ScopeConstraint> scopeConstraints = new ArrayList<ScopeConstraint>();
					scopeConstraints.add(scopeConstraint);
					return scopeConstraints;
				}

			}
		}*/
		Filters filters = prepareFilters(options);
		
		//check for overridden scope type
		List<ScopeConstraint> scopeConstraintList =  filters.getList(em, ScopeConstraint.class,
				"select s from ScopeConstraint s", options);
		if(scopeConstraintList!=null && !scopeConstraintList.isEmpty()){
			return scopeConstraintList;
		}
		
		//check for default scope for app
		OptionFilter optionFilter = options == null ? null : options
				.getOption(OptionFilter.class);
		Map<String, String> filtersMap = optionFilter == null ? null
				: optionFilter.getFilters();
		if (filtersMap != null) {
			String scopeId = filtersMap.get("scopeId");
			if (scopeId != null && scopeId.length() > 0) {
				Integer applicationId = scopeDal.getById(Integer.valueOf(scopeId))
						.getApplicationId();
				if(applicationId!=null){
					List<ScopeConstraint> newScopeConstraintList = getDefaultScopeTypeByAppId(applicationId);
					if(newScopeConstraintList!=null && !newScopeConstraintList.isEmpty()){
						return newScopeConstraintList;
					}
					log.info("getConstraints; No scope type found for scopeId={}", scopeId);
				}
			}
		}
		return null;
	}

	private static class ScopeDetails {
		int groupId;
		String groupName;
		int roleId;
		String roleName;
		String target;
		String operation;
		String scopeRBACDb;

		public static ScopeDetails toScopeDetails(Object[] objectArray) {
			ScopeDetails scopeDetails = new ScopeDetails();
			scopeDetails.groupId = (Integer) objectArray[0];
			scopeDetails.groupName = (String) objectArray[1];
			scopeDetails.roleId = (Integer) objectArray[2];
			scopeDetails.roleName = (String) objectArray[3];
			scopeDetails.target = (String) objectArray[4];
			scopeDetails.operation = (String) objectArray[5];
			scopeDetails.scopeRBACDb = (String) objectArray[6];
			return scopeDetails;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("ScopeDetails{groupId=").append(groupId);
			sb.append("; groupName=").append(groupName);
			sb.append("; roleId=").append(roleId);
			sb.append("; roleName=").append(roleName);
			sb.append("; target=").append(target);
			sb.append("; operation=").append(operation);
			sb.append("; scopeRBACDb=").append(scopeRBACDb);
			sb.append("}");
			return sb.toString();
		}
	}

	@Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public String getAttributeDataByScopeConstraintId(
			Integer scopeConstraintId, Options options, Integer userId, HttpServletRequest servletRequest) {
		ScopeConstraint scopeConstraint = getById(scopeConstraintId);

		if (scopeConstraint != null) {
			log.trace("getAttributeDataByScopeConstraintId; scopeName={}",
					scopeConstraint.getScopeName());
			if(scopeConstraint.getSourceType().equals(APPLICATION_RBAC))
				return externalDataAccessRBAC.list(scopeConstraint, servletRequest, userId, options);
			else
				return externalDataAccessUtil.getExternalDataAccessMap().get(scopeConstraint.getSourceType()).get(scopeConstraint.getApplicationName()).list(scopeConstraint, servletRequest, userId);
		}
		return null;
	}
	
	@Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public String getAttributeDataByAttributeId(
			Integer attributeId, Options options, Integer userId,HttpServletRequest servletRequest) {
		TypedQuery<MasterAttributes> masterAttributeQuery = em.createQuery("select m from MasterAttributes m  where m.attributeId=:attributeId", MasterAttributes.class);
		masterAttributeQuery.setParameter("attributeId", attributeId);
		MasterAttributes masterAttribute = masterAttributeQuery.getSingleResult();
		return getAttributeDataByScopeConstraintId(masterAttribute.getScopeConstraintId(), options, userId, servletRequest);
	}

	@Override
	public List<ScopeConstraint> getScopeConstraintsForQueryBuilder(
			String scopeKey) {
		TypedQuery<ScopeConstraint> queryScopeConstraint = em
				.createQuery(
						"select distinct sc from ScopeConstraint sc " +
								"where ( sc.applicationId in (select s.applicationId from Scope s where s.scopeKey = :scopeKey) " +
										"or sc.scopeId in (select s.scopeId from Scope s where s.scopeKey = :scopeKey) ) " +
										"and sc.comparators = 'QUERYBUILDER' " +
										"and sc.sourceType is not null ",
						ScopeConstraint.class);
		queryScopeConstraint.setParameter("scopeKey", scopeKey);
		return queryScopeConstraint.getResultList();
	}

}
