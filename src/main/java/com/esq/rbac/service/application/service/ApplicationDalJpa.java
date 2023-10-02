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
package com.esq.rbac.service.application.service;

import com.esq.rbac.service.application.applicationmaintenance.service.ApplicationMaintenanceDal;
import com.esq.rbac.service.application.childapplication.appurldata.AppUrlData;
import com.esq.rbac.service.application.childapplication.childapplicationlicense.ChildApplicationLicense;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.childapplication.repository.ChildApplicationRepository;
import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.repository.ApplicationRepo;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.config.CacheConfig;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.role.operationsubdomain.domain.Operation;
import com.esq.rbac.service.role.service.RoleDal;
import com.esq.rbac.service.role.targetsubdomain.domain.Target;
import com.esq.rbac.service.rolesinapplicationjson.RolesInApplicationJson;
import com.esq.rbac.service.util.*;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.variable.service.VariableDal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import jakarta.persistence.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.RegexValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import java.util.*;


@Service
@Slf4j
public class ApplicationDalJpa extends BaseDalJpa implements ApplicationDal {


	private static final Map<String, String> SORT_COLUMNS;
	//private static UrlValidator urlValidator = new UrlValidator();
	//to allow local urls
	private static UrlValidator urlValidator;
	public static final String HOME_URL_INVALID = "homePageUrlInvalid";
	public static final String HOME_SERVICE_URL_INVALID = "homeServiceUrlInvalid";
	public static final String GROUP_SCOPEDEFINITION_FOUND = "groupScopeDefinitionFound";
	public static final String CONTEXT_NAME_DUPLICATE = "contextNameDuplicate";
	public static final String APP_KEY_DUPLICATE = "appKeyDuplicate";


	protected EntityManager em;

	protected Class entityClass;

	@PersistenceContext(type = PersistenceContextType.EXTENDED)
	public void setEntityManager(EntityManager em) {
		log.trace("setEntityManager");
		this.em = em;
		this.entityClass = Application.class;
	}

	@Autowired
	private Validator validator;


	private VariableDal variableDal;


	@Autowired
	public void setVariableDal(VariableDal variableDal) {
		log.trace("setVariableDal; {};", variableDal);
		this.variableDal = variableDal;

	}


	private ApplicationMaintenanceDal applicationMaintenanceDal;

	@Autowired
	public void setApplicationMaintenanceDal(ApplicationMaintenanceDal applicationMaintenanceDal) {
		log.trace("setApplicationMaintenanceDal; {};", applicationMaintenanceDal);
		this.applicationMaintenanceDal = applicationMaintenanceDal;

	}


	private DeploymentUtil deploymentUtil;

	@Autowired
	public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
		log.trace("setDeploymentUtil; {};", deploymentUtil);
		this.deploymentUtil = deploymentUtil;
		urlValidator = new UrlValidator(new RegexValidator(this.deploymentUtil.getRegexForUrlAuhtorityValidation()), UrlValidator.ALLOW_LOCAL_URLS);

	}



	private RoleDal roleDal;

	@Autowired
	public void setRoleDal(@Lazy RoleDal roleDal) {
		log.trace("setRoleDal; {};", roleDal);
		this.roleDal = roleDal;

	}



	private AuditLogService auditLogDal;

	@Autowired
	public void setAuditLogService(AuditLogService auditLogDal) {
		log.trace("setAuditLogService; {};", auditLogDal);
		this.auditLogDal = auditLogDal;

	}




	private CacheService cacheService;

	@Autowired
	public void setCacheService(CacheService cacheService) {
		log.trace("setCacheService; {};", cacheService);
		this.cacheService = cacheService;

	}



	private ChildAppPermValidatorUtil childAppPermValidatorUtil;

	@Autowired
	public void childAppPermValidatorUtil(ChildAppPermValidatorUtil childAppPermValidatorUtil) {
		log.trace("setChildAppPermValidatorUtil; {};", childAppPermValidatorUtil);
		this.childAppPermValidatorUtil = childAppPermValidatorUtil;

	}



	@Autowired
	private ApplicationRepo applicationRepo;




	@Autowired
	private ChildApplicationRepository childApplicationRepository;






	static {
		SORT_COLUMNS = new TreeMap<String, String>();
		SORT_COLUMNS.put("name", "a.name");
		SORT_COLUMNS.put("childApplicationName", "c.childApplicationName");
	}
	private static final String SEARCH_APPLICATIONS = "select distinct a.* from rbac.application a "
			+ "where lower(a.name) like ? or lower(a.description) like ? ";
	private static final String COUNT_APPLICATIONS = "select count(distinct a.applicationId) from rbac.application a "
			+ "where lower(a.name) like ? or lower(a.description) like ? ";











	public void validateUrls(Application application){
		Map<String, List<String>> invalidList = new HashMap<String, List<String>>();
		if(application.getChildApplications()!=null && !application.getChildApplications().isEmpty()){
			for(ChildApplication childApplication: application.getChildApplications()){
				if(childApplication.getAppUrlDataSet()!=null && !childApplication.getAppUrlDataSet().isEmpty()){
					for(AppUrlData appUrlData:childApplication.getAppUrlDataSet()){
						if(appUrlData.getHomeUrl()!=null && !appUrlData.getHomeUrl().isEmpty()){
							if(!urlValidator.isValid(appUrlData.getHomeUrl())){
								if(!invalidList.containsKey(childApplication.getChildApplicationName())){
									invalidList.put(childApplication.getChildApplicationName(), new LinkedList<String>());
								}
								invalidList.get(childApplication.getChildApplicationName()).add("homeUrl");
							}
						}
						if(appUrlData.getServiceUrl()!=null && !appUrlData.getServiceUrl().isEmpty()){
							if(!urlValidator.isValid(appUrlData.getServiceUrl())){
								if(!invalidList.containsKey(childApplication.getChildApplicationName())){
									invalidList.put(childApplication.getChildApplicationName(), new LinkedList<String>());
								}
								invalidList.get(childApplication.getChildApplicationName()).add("serviceUrl");
							}
						}
					}
				}
			}
		}
		if(!invalidList.isEmpty()){
			ErrorInfoException errorInfo;
			try {
				errorInfo = new ErrorInfoException(HOME_SERVICE_URL_INVALID, new ObjectMapper().writeValueAsString(invalidList).toString());
				log.info("validateUrls;", errorInfo);
				throw errorInfo;
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}

	public void validateAppkeyContextName(Application application){
		Set<String> childAppNameList = new HashSet<String>();
		Set<String> childAppKeyList = new HashSet<String>();
		if(application.getChildApplications()!=null && !application.getChildApplications().isEmpty()){
			for(ChildApplication childApplication: application.getChildApplications()){
				if(childApplication.getChildApplicationName()!=null && !childApplication.getChildApplicationName().isEmpty()){
					if(!childAppNameList.add(childApplication.getChildApplicationName())){
						ErrorInfoException errorInfo = new ErrorInfoException(CONTEXT_NAME_DUPLICATE);
						log.info("validateAppkeyContextName;", errorInfo);
						throw errorInfo;
					}
				}
				if(childApplication.getAppKey()!=null && !childApplication.getAppKey().isEmpty()){
					if(!childAppKeyList.add(childApplication.getAppKey())){
						ErrorInfoException errorInfo = new ErrorInfoException(APP_KEY_DUPLICATE);
						log.info("validateAppkeyContextName;", errorInfo);
						throw errorInfo;
					}
				}
			}
		}
		if(childAppNameList!=null && !childAppNameList.isEmpty()){
//	   		TypedQuery<ChildApplication> query = em.createNamedQuery("validateChildAppName", ChildApplication.class);
//	   		query.setParameter("childApplicationNames", childAppNameList);
//	   		List<ChildApplication> childAppNameResult = query.getResultList();
			List<ChildApplication> childAppNameResult = childApplicationRepository.validateChildAppName(childAppNameList);
			if(childAppNameResult!=null && !childAppNameResult.isEmpty()){
				if(application.getChildApplications()!=null && !application.getChildApplications().isEmpty()){
					for(ChildApplication childApplication: application.getChildApplications()){
						Integer childApplicationId = childApplication.getChildApplicationId();
						if (childApplicationId != null && childApplicationId.intValue() > 0) {
							for (Iterator<ChildApplication> iterator = childAppNameResult.iterator(); iterator.hasNext();) {
								ChildApplication fetchedChildApp = (ChildApplication) iterator.next();
								if (childApplicationId.equals(fetchedChildApp.getChildApplicationId())) {

								}else{
									if (childApplication.getChildApplicationName().equalsIgnoreCase(fetchedChildApp.getChildApplicationName())) {
										ErrorInfoException errorInfo = new ErrorInfoException(CONTEXT_NAME_DUPLICATE);
										log.info("validateAppkeyContextName;", errorInfo);
										throw errorInfo;
									}
								}
							}
						}
						else {
							for (Iterator<ChildApplication> iterator = childAppNameResult.iterator(); iterator.hasNext();) {
								ChildApplication fetchedChildApp = (ChildApplication) iterator.next();
								if (childApplication.getChildApplicationName().equalsIgnoreCase(fetchedChildApp.getChildApplicationName())) {
									ErrorInfoException errorInfo = new ErrorInfoException(CONTEXT_NAME_DUPLICATE);
									log.info("validateAppkeyContextName;", errorInfo);
									throw errorInfo;
								}
							}

						}
					}
				}
			}
		}
		if(childAppKeyList!=null && !childAppKeyList.isEmpty()){
//	   		TypedQuery<ChildApplication> childAppKeyQuery = em.createNamedQuery("validateChildAppKey", ChildApplication.class);
//	   		childAppKeyQuery.setParameter("childAppKeys", childAppKeyList);
//	   		List<ChildApplication> childAppKeyResult = childAppKeyQuery.getResultList();
			List<ChildApplication> childAppKeyResult = childApplicationRepository.validateChildAppKey(childAppKeyList);
			if(childAppKeyResult!=null && !childAppKeyResult.isEmpty()){
				if(application.getChildApplications()!=null && !application.getChildApplications().isEmpty()){
					for(ChildApplication childApplication: application.getChildApplications()){
						Integer childApplicationId = childApplication.getChildApplicationId();
						if (childApplicationId != null && childApplicationId.intValue() > 0) {
							for (Iterator<ChildApplication> iterator = childAppKeyResult.iterator(); iterator.hasNext();) {
								ChildApplication fetchedChildApp = (ChildApplication) iterator.next();
								if (childApplicationId.equals(fetchedChildApp.getChildApplicationId())) {

								}else{
									if (childApplication.getAppKey().equalsIgnoreCase(fetchedChildApp.getAppKey())) {
										ErrorInfoException errorInfo = new ErrorInfoException(APP_KEY_DUPLICATE);
										log.info("validateAppkeyContextName;", errorInfo);
										throw errorInfo;
									}
								}
							}
						}
						else {
							for (Iterator<ChildApplication> iterator = childAppKeyResult.iterator(); iterator.hasNext();) {
								ChildApplication fetchedChildApp = (ChildApplication) iterator.next();
								if (childApplication.getAppKey().equalsIgnoreCase(fetchedChildApp.getAppKey())) {
									ErrorInfoException errorInfo = new ErrorInfoException(APP_KEY_DUPLICATE);
									log.info("validateAppkeyContextName;", errorInfo);
									throw errorInfo;
								}
							}
						}
					}
				}
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	private void handleLicense(Application application, Optional<Application> existingApplication, AuditLogInfo auditLogInfo){
		if(application!=null && application.getChildApplications()!=null && !application.getChildApplications().isEmpty()){
			for(ChildApplication childApplication: application.getChildApplications()){
				if(childApplication.getChangeLicense()!=null &&
						childApplication.getChangeLicense().getLicense()!=null &&
						!childApplication.getChangeLicense().getLicense().isEmpty()){
					try {
						if(existingApplication!=null && childApplication.getChildApplicationId()!=null
								&& existingApplication.get().getChildApplicationById(childApplication.getChildApplicationId())!=null
								&& existingApplication.get().getChildApplicationById(childApplication.getChildApplicationId()).getChildApplicationLicense()!=null){
							ChildApplicationLicense existingLicense = existingApplication.get().getChildApplicationById(
									childApplication.getChildApplicationId()).getChildApplicationLicense();
							existingLicense.setLicense(EnvironmentUtil.encryptLicense(childApplication.getChangeLicense().getLicense()));
							existingLicense.setAdditionalData(childApplication.getChangeLicense().getAdditionalData());
							existingLicense.setUpdatedBy(auditLogInfo.getLoggedInUserId());
							existingLicense.setUpdatedOn(DateTime.now().toDate());
							childApplication.setChildApplicationLicense(existingLicense);
						}
						else{
							ChildApplicationLicense appLicense = ChildApplicationLicense.builder()
									.license(EnvironmentUtil.encryptLicense(childApplication.getChangeLicense().getLicense()))
									.additionalData(childApplication.getChangeLicense().getAdditionalData())
									.createdBy(auditLogInfo.getLoggedInUserId())
									.createdOn(DateTime.now().toDate())
									.build();
							childApplication.setChildApplicationLicense(appLicense);
						}
					}
					catch(Exception e){
						log.error("handleLicense; Exception={};", e);
						throw new ErrorInfoException("licenseEncryptionFailed", childApplication.getAppKey());
					}
				}
				//for retaining existing licenses
				else if(existingApplication!=null && childApplication.getChildApplicationId()!=null){
					ChildApplication existingChildApp = existingApplication.get().getChildApplicationById(childApplication.getChildApplicationId());
					if(existingChildApp!=null){
						childApplication.setChildApplicationLicense(existingChildApp.getChildApplicationLicense());
					}
				}
			}
		}
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Map<String,Object> getLicenseByAppKey(String appKey){
		Map<String,Object> result = new LinkedHashMap<String,Object>();
		try{
			ChildApplication childApp = childApplicationRepository.getChildApplicationByAppKey(appKey);
			if(childApp.getChildApplicationLicense()==null ||
					childApp.getChildApplicationLicense().getLicense()==null
					|| childApp.getChildApplicationLicense().getLicense().isEmpty()){
				log.error("getLicenseByAppKey; appKey={}; noLicenseFound", appKey);
				throw new ErrorInfoException("noLicenseFound", "noLicenseFound");
			}
			try{
				result.put("licenseKey", EnvironmentUtil.decryptLicense(childApp.getChildApplicationLicense().getLicense()));
			}
			catch(Exception e){
				log.error("getLicenseByAppKey; appKey={}; Exception={};", appKey, e);
				throw new ErrorInfoException("licenseDecryptionException", "licenseDecryptionException");
			}
			result.put("childApplicationId", childApp.getChildApplicationId());
			result.put("childApplicationName", childApp.getChildApplicationName());
			result.put("appKey", childApp.getAppKey());
			if(childApp.getChildApplicationLicense().getCreatedBy()!=null){
				result.put("createdBy", childApp.getChildApplicationLicense().getCreatedBy());
			}
			if(childApp.getChildApplicationLicense().getCreatedOn()!=null){
				result.put("createdOn", childApp.getChildApplicationLicense().getCreatedOn());
			}
			if(childApp.getChildApplicationLicense().getUpdatedBy()!=null){
				result.put("updatedBy", childApp.getChildApplicationLicense().getUpdatedBy());
			}
			if(childApp.getChildApplicationLicense().getUpdatedOn()!=null){
				result.put("updatedOn", childApp.getChildApplicationLicense().getUpdatedOn());
			}
			if(childApp.getChildApplicationLicense().getAdditionalData()!=null && !childApp.getChildApplicationLicense().getAdditionalData().isEmpty()){
				result.put("additionalData", childApp.getChildApplicationLicense().getAdditionalData());
			}

		}
		catch(NoResultException nre){
			log.error("getLicenseByAppKey; appKey={}; Exception={};", appKey, nre);
			throw new ErrorInfoException("noAppFound", "noAppFound");
		}
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Application create(Application application, AuditLogInfo auditLogInfo) {
		cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);//CACHE:: CLEAR
		//added to support multiple URLs for same application (required for OB - OB Web, OB Admin are apparently same apps but separate URLs)
		//in the format--- app1==http://192.168.1.203:8779/designer ;; app2==http://192.168.1.203:8001/rbac
		//so removing following URL validation
    	/*
    	if(application.getHomeUrl()!=null && !application.getHomeUrl().isEmpty()){

    		if(!urlValidator.isValid(application.getHomeUrl())){
    			 StringBuilder sb = new StringBuilder();
                 sb.append(HOME_URL_INVALID).append("; ");
    			 ErrorInfoException errorInfo = new ErrorInfoException(HOME_URL_INVALID, sb.toString());
    	         log.info("create;", errorInfo);
    	         throw errorInfo;
    		}
    	}*/
		validateAppkeyContextName(application);
		validateUrls(application);
		handleLicense(application, null, auditLogInfo);
		application.setCreatedBy(auditLogInfo.getLoggedInUserId());
		application.setCreatedOn(DateTime.now().toDate());
//        em.persist(application);

		applicationRepo.save(application);
		auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), application.getName(),
				"Application", "Create", getObjectChangeSetLocal(null, application));
		variableDal.cleanVariablesForApplicationChanges();
		return application;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Application update(Application application, AuditLogInfo auditLogInfo) {
		cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);//CACHE:: CLEAR
		if (application.getApplicationId() == null) {
			throw new IllegalArgumentException("applicationId missing");
		}
//        Application existingApplication = em.find(Application.class, application.getApplicationId());
		Optional<Application> existingApplication = applicationRepo.findById(application.getApplicationId());
		if (existingApplication.isEmpty()) {
			throw new NoSuchElementException("applicationId invalid");
		}

		//added to support multiple URLs for same application (required for OB - OB Web, OB Admin are apparently same apps but separate URLs)
		//in the format--- app1==http://192.168.1.203:8779/designer ;; app2==http://192.168.1.203:8001/rbac
		//so removing following URL validation
    	/*
        if(application.getHomeUrl()!=null && !application.getHomeUrl().isEmpty()){
    		if(!urlValidator.isValid(application.getHomeUrl())){
    			 StringBuilder sb = new StringBuilder();
                 sb.append(HOME_URL_INVALID).append("; ");
    			 ErrorInfoException errorInfo = new ErrorInfoException(HOME_URL_INVALID, sb.toString());
    	         log.info("update;", errorInfo);
    	         throw errorInfo;
    		}
    	}
    	*/
		validateAppkeyContextName(application);
		validateUrls(application);
		handleLicense(application, existingApplication, auditLogInfo);
		//setObjectChangeSet(existingApplication, application, false);
		Map<String, String> objectChanges = getObjectChangeSetLocal(existingApplication, application);
		String existingAppName = existingApplication.get().getName();

		existingApplication.get().setDescription(application.getDescription());
		existingApplication.get().setLabels(application.getLabels());
		existingApplication.get().setName(application.getName());
       /* existingApplication.setHomeUrl(application.getHomeUrl());
        existingApplication.setServiceUrl(application.getServiceUrl());*/

		//delete maintenance for deleting child applications
		List<Integer> newChildAppIds= new ArrayList<Integer>();
		if(application.getChildApplications()!=null && !application.getChildApplications().isEmpty()){
			for(ChildApplication newChildApp : application.getChildApplications()){
				newChildAppIds.add(newChildApp.getChildApplicationId());

				//set the validator type and data as these are not available on the UI
				if(newChildApp.getChildApplicationId()!=null){
//        			ChildApplication tempChildApp = em.find(ChildApplication.class, newChildApp.getChildApplicationId());
					Optional<ChildApplication> tempChildApp = childApplicationRepository.findById(newChildApp.getChildApplicationId());
					newChildApp.setPermissionValidator(tempChildApp.get().getPermissionValidator());
					newChildApp.setPermissionValidatorData(tempChildApp.get().getPermissionValidatorData());
				}

			}
		}
		if(existingApplication.get().getChildApplications()!=null && !existingApplication.get().getChildApplications().isEmpty()){
			for(ChildApplication orgChildApp:existingApplication.get().getChildApplications()) {
				if(!newChildAppIds.contains(orgChildApp.getChildApplicationId())){
					applicationMaintenanceDal.deleteByChildApplicationId(orgChildApp.getChildApplicationId());
				}
			}
		}
		existingApplication.get().setChildApplications(application.getChildApplications());
		existingApplication.get().setSsoAllowed(application.getSsoAllowed());
		existingApplication.get().setUpdatedOn(DateTime.now().toDate());
		existingApplication.get().setUpdatedBy(auditLogInfo.getLoggedInUserId());
		auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), existingAppName,
				"Application", "Update", objectChanges);
//        Application ret = em.merge(existingApplication);
		Application ret = applicationRepo.save(existingApplication.get());

		variableDal.cleanVariablesForApplicationChanges();
		return ret;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Application updateTargetOperations(Application application) {
		cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);//CACHE:: CLEAR
		if (application.getApplicationId() == null) {
			throw new IllegalArgumentException("applicationId missing");
		}
//        Application existingApplication = em.find(Application.class, application.getApplicationId());
		Optional<Application> existingApplication = applicationRepo.findById(application.getApplicationId());
		if (existingApplication == null) {
			throw new IllegalArgumentException("applicationId invalid");
		}

		setObjectChangeSet(existingApplication.get(), application, true);

		//existingApplication.get().setTargets(application.getTargets());
		existingApplication.get().setTargets(application.getTargets());
//        Application ret = em.merge(existingApplication);
		Application ret = applicationRepo.save(existingApplication.get());

		return ret;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Application getById(int applicationId) {
		// return em.find(Application.class, applicationId);
		return applicationRepo.findById(applicationId).get();
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Application getByName(String name) {
		try {
//            TypedQuery<Application> query = em.createNamedQuery("getApplicationByName", Application.class);
//            query.setParameter("name", name);
//            return query.getSingleResult();
			return applicationRepo.getApplicationByName(name);
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteById(int applicationId, AuditLogInfo auditLogInfo) {
		cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);//CACHE:: CLEAR
		//checking whether this app has scope defined to it. If yes, the show error msg RBAC-428
//    	TypedQuery<Integer> query = em.createNamedQuery("getDefinedScopeIdsFromApplication", Integer.class);
//    	query.setParameter(1, applicationId);
//        List<Integer> scopeIds = query.getResultList();
		List<Integer> scopeIds = applicationRepo.getDefinedScopeIdsFromApplication(applicationId);
		if(scopeIds!=null && !scopeIds.isEmpty()){ //throw error since scope definition is found
			ErrorInfoException errorInfo = new ErrorInfoException(GROUP_SCOPEDEFINITION_FOUND);
			throw errorInfo;
		}
		//temporary - added for variable deletion with groupId or userId
		variableDal.deleteForCascade(null, null, applicationId);

		//Application application = em.find(Application.class, applicationId);
		Optional<Application> application = applicationRepo.findById(applicationId);
		Set<ChildApplication> childApplications = application.get().getChildApplications();
		if(childApplications!=null && !childApplications.isEmpty())
		{
			for(ChildApplication childApp : childApplications)
			{
				applicationMaintenanceDal.deleteByChildApplicationId(childApp.getChildApplicationId());
			}
		}
		auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), application.get().getName(),
				"Application", "Delete", getObjectChangeSetLocal(application, null));
//        em.remove(application);
		applicationRepo.delete(application.get());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteByName(String name, AuditLogInfo auditLogInfo) {
		cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);//CACHE:: CLEAR
		//temporary - added for variable deletion with groupId or userId
		//variableDal.deleteForCascade(null, null, applicationId);
		//Application application = em.find(Application.class, Lookup.getApplicationId(name));
		Optional<Application> application = applicationRepo.findById(Lookup.getApplicationId(name));
		if(application!=null){
//        		em.remove(em.find(Application.class, Lookup.getApplicationId(name)));
			applicationRepo.delete(applicationRepo.findById(Lookup.getApplicationId(name)).get());
			auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), application.get().getName(),
					"Application", "Delete", getObjectChangeSetLocal(application, null));
		}
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<String> getAllNames() {
//        TypedQuery<String> query = em.createNamedQuery("getAllApplicationNames", String.class);
//        return query.getResultList();

		return applicationRepo.getAllApplicationNames();

	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Application> getList(Options options) {
		Filters filters = prepareFilters(options);
		return filters.getList(em, Application.class, "select a from Application a", options, SORT_COLUMNS);

	}


	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int getCount(Options options) {
		Filters filters = prepareFilters(options);
		return filters.getCount(em, "select count(a) from Application a");
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public String getRBACContextName(){
//    	 TypedQuery<String> query = em.createNamedQuery("getRBACContextName", String.class);
//    	 query.setParameter("applicationName", RBACUtil.RBAC_UAM_APPLICATION_NAME);
//         return query.getSingleResult();
		return childApplicationRepository.getRBACContextName(RBACUtil.RBAC_UAM_APPLICATION_NAME);
	}

	private Filters prepareFilters(Options options) {

		Filters result = new Filters();
		OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
		Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
		if (filters != null) {

			String name = filters.get("name");
			if (name != null && name.length() > 0) {
				result.addCondition("a.name = :name");
				result.addParameter("name", name);
			}

			String label = filters.get("label");
			if (label != null && label.length() > 0) {
				result.addCondition(":label member of a.labels");
				result.addParameter("label", label);
			}

		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Application> searchList(Options options) {
		String q = SearchUtils
				.getSearchParam(options, SearchUtils.SEARCH_PARAM)
				.toLowerCase();
		StringBuilder sb = new StringBuilder();
		sb.append(SEARCH_APPLICATIONS);
		sb.append(SearchUtils.getOrderByParam(options, SORT_COLUMNS));
		Query query = em.createNativeQuery(sb.toString(), Application.class);
		String wildcardedq = SearchUtils.wildcarded(q);
		query.setParameter(1, wildcardedq);
		query.setParameter(2, wildcardedq);
		OptionPage optionPage = options != null ? options
				.getOption(OptionPage.class) : null;
		if (optionPage != null) {
			query.setFirstResult(optionPage.getFirstResult());
			query.setMaxResults(optionPage.getMaxResults());
		}
		List<Application> applications = query.getResultList();
		return applications;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int getSearchCount(Options options) {
		String q = SearchUtils
				.getSearchParam(options, SearchUtils.SEARCH_PARAM)
				.toLowerCase();
		StringBuilder sb = new StringBuilder();
		sb.append(COUNT_APPLICATIONS);
		Query query = em.createNativeQuery(sb.toString());
		String wildcardedq = SearchUtils.wildcarded(q);
		query.setParameter(1, wildcardedq);
		query.setParameter(2, wildcardedq);
		return ((Number) query.getSingleResult()).intValue();
	}

	private void setObjectChangeSet(Application oldApp, Application newApp, Boolean targetOperation) {
		clearObjectChangeSet ();

		putToObjectChangeSet(OBJECTCHANGES_APPID, newApp.getApplicationId().toString());
		putToObjectChangeSet(OBJECTNAME, oldApp.getName());

		if (targetOperation) {
			checkTargetPutToObjectChangeSet(newApp.getTargets(),oldApp.getTargets());
		} else {
			checkPutToObjectChangeSet(OBJECTCHANGES_APPNAME, newApp.getName(), oldApp.getName(), null, null);
			checkPutToObjectChangeSet(OBJECTCHANGES_DESCRIPTION, newApp.getDescription(), oldApp.getDescription(), null, null);
			checkPutToObjectChangeSet(OBJECTCHANGES_LABELS, newApp.getLabels(), oldApp.getLabels(), null, null);
			checkPutToObjectChangeSet(OBJECTCHANGES_SSOALLOWED, newApp.getSsoAllowed(), oldApp.getSsoAllowed(), null, null);
			//checkMapPutToObjectChangeSet(Variable.convertSetOfVariablesToMap(newApp.getVariables()), Variable.convertSetOfVariablesToMap(oldApp.getVariables()));
			// checkPutToObjectChangeSet(OBJECTCHANGES_HOMEURL, newApp.getHomeUrl(), oldApp.getHomeUrl(), null, null);
			// checkPutToObjectChangeSet(OBJECTCHANGES_SERVICEURL, newApp.getServiceUrl(), oldApp.getServiceUrl(), null, null);
		}

	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	@Cacheable(value = CacheConfig.USER_AUTH_APPS_CACHE, unless = "#result == null")
	public List<Application> getUserAuthorizedApps(String userName) {
		List<Application> resultList = new LinkedList<Application>();
		if(deploymentUtil.getShowAppDashboardInSwitcher()!=null && deploymentUtil.getShowAppDashboardInSwitcher().equals(Boolean.TRUE)){
			if(deploymentUtil.getAppDashboardApplicationId()!=null){
				Application app = getById(deploymentUtil.getAppDashboardApplicationId());
				if(app!=null && app.getChildApplications()!=null && !app.getChildApplications().isEmpty()){
					if(app!=null){
						resultList.add(app);
					}
				}
			}
		}
//		TypedQuery<Application> query = em.createNamedQuery("getUserAuthorizedApps", Application.class);
//		query.setParameter(1, userName);
//		List<Application> authList = query.getResultList();

		List<Application> authList = applicationRepo.getUserAuthorizedApps(userName);
		if(authList!=null && !authList.isEmpty()){
			resultList.addAll(authList);
		}
		return resultList;
	}

	@Override
	public List<Map<String,Object>> getApplicationIdNames(Options options) {
		Filters filters = prepareFilters(options);
		// add default sort by name
		OptionSort optionSort = options != null ? options
				.getOption(OptionSort.class) : null;
		if (optionSort == null) {
			optionSort = new OptionSort(new LinkedList<String>());
		}
		if(optionSort.getSortProperties().isEmpty()){
			optionSort.getSortProperties().add("name");
		}
		options = new Options(optionSort, options != null ? options
				.getOption(OptionPage.class) : null, options != null ? options
				.getOption(OptionFilter.class) : null);
		List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
		List<Object[]> result = filters.getList(em, Object[].class, "select a.applicationId, a.name from Application a", options, SORT_COLUMNS);
		if(result!=null && !result.isEmpty()){
			for(Object[] obj:result){
				Map<String, Object> temp = new HashMap<String, Object>();
				temp.put("applicationId", obj[0]);
				temp.put("name", obj[1].toString());
				returnObj.add(temp);
			}
		}
		return returnObj;
	}

	protected synchronized String getTargetNames(Set<Target> newTargets) {
		String name = "";
		if(newTargets!=null && !newTargets.isEmpty()){
			for (Target t : newTargets) {
				if (t.getName() != null && !t.getName().equals("")) {
					name += t.getName() + ",";
				}
			}
		}
		name = name.indexOf(",") == -1 ? "" : name.substring(0, name.length() - 1);
		return name;
	}

	protected synchronized String getTargetKeys(Set<Target> newTargets) {
		String targetKey = "";
		if(newTargets!=null && !newTargets.isEmpty()){
			for (Target t : newTargets) {
				if (t.getTargetKey() != null && !t.getTargetKey().equals("")) {
					targetKey += t.getTargetKey() + ",";
				}
			}
		}
		targetKey = targetKey.indexOf(",") == -1 ? "" : targetKey.substring(0, targetKey.length() - 1);
		return targetKey;
	}

	protected synchronized String getTargetDesc(Set<Target> newTargets) {
		String desc = "";
		if(newTargets!=null && !newTargets.isEmpty()){
			for (Target t : newTargets) {
				if (t.getDescription() != null && !t.getDescription().equals("")) {
					desc += t.getName() + "." + t.getDescription() + ",";
				}
			}
		}
		desc = desc.indexOf(",") == -1 ? "" : desc.substring(0, desc.length() - 1);
		return desc;
	}
	protected synchronized String getTargetLabel(Set<Target> newTargets) {
		String label = "";
		if(newTargets!=null && !newTargets.isEmpty()){
			for (Target t : newTargets) {
				if (t.getLabels() != null && t.getLabels().size() != 0) {
					label += t.getName() + "." + t.getLabels() + ",";
				}

			}
		}
		label = label.indexOf(",") == -1 ? "" : label.substring(0, label.length() - 1);
		return label;
	}
	protected synchronized String getOperationName(Set<Target> newTargets) {
		String op_name = "";
		if(newTargets!=null && !newTargets.isEmpty()){
			for (Target t : newTargets) {
				if(t.getOperations()!=null && !t.getOperations().isEmpty()){
					for (Operation o : t.getOperations()) {
						if (t.getName() != null && o.getName() != null
								&& !t.getName().equals("") && !o.getName().equals("")) {
							op_name += t.getName() + "." + o.getName() + ",";
						}

					}
				}
			}
		}
		op_name = op_name.indexOf(",") == -1 ? "" : op_name.substring(0,op_name.length() - 1);
		return op_name;
	}

	protected synchronized String getOperationKey(Set<Target> newTargets) {
		String operationKey = "";
		if(newTargets!=null && !newTargets.isEmpty()){
			for (Target t : newTargets) {
				if(t.getOperations()!=null && !t.getOperations().isEmpty()){
					for (Operation o : t.getOperations()) {
						if (t.getName() != null && o.getOperationKey() != null
								&& !t.getName().equals("") && !o.getOperationKey().equals("")) {
							operationKey += t.getName() + "." + o.getOperationKey() + ",";
						}

					}
				}
			}
		}
		operationKey = operationKey.indexOf(",") == -1 ? "" : operationKey.substring(0,operationKey.length() - 1);
		return operationKey;
	}

	protected synchronized String getTargetOpLabel(Set<Target> newTargets) {
		String opLabel = "";
		if(newTargets!=null && !newTargets.isEmpty()){
			for (Target t : newTargets) {
				if(t.getOperations()!=null && !t.getOperations().isEmpty()){
					for (Operation o : t.getOperations()) {
						if (t.getName() != null && o.getName() != null
								&& o.getLabels() != null && !t.getName().equals("")
								&& !o.getName().equals("") && o.getLabels().size() != 0) {
							opLabel += t.getName() + "." + o.getName() + "."
									+ o.getLabels() + ",";
						}
					}
				}
			}
		}
		opLabel = opLabel.equals("") ? "" : opLabel.substring(0,opLabel.length() - 1);
		return opLabel;
	}
	protected synchronized String getTargetOpDesc(Set<Target> newTargets) {
		String opDesc = "";
		if(newTargets!=null && !newTargets.isEmpty()){
			for (Target t : newTargets) {
				if(t.getOperations()!=null && !t.getOperations().isEmpty()){
					for (Operation o : t.getOperations()) {
						if (t.getName() != null && o.getName() != null
								&& o.getDescription() != null && !t.getName().equals("")
								&& !o.getName().equals("") && !o.getDescription().equals("")) {
							opDesc += t.getName() + "." + o.getName() + "."
									+ o.getDescription() + ",";
						}
					}
				}
			}
		}
		opDesc = opDesc.equals("") ? "" : opDesc.substring(0,opDesc.length() - 1);
		return opDesc;
	}

	protected synchronized String getScopeNames(Set<Integer> scopeIds) {
		String scopeNames = "";
		if(scopeIds!=null && !scopeIds.isEmpty()) {
			for(Integer i : scopeIds)
			{
				String tmp = Lookup.getScopeName(i);
				scopeNames+= tmp + "," ;
			}
		}
		return scopeNames.equals("") ? "" : scopeNames.substring(0,scopeNames.length() - 1);
	}

	protected synchronized String getTargetOpScopeName(Set<Target> newTargets) {
		String opScopeName = "";
		if(newTargets!=null && !newTargets.isEmpty()){
			for (Target t : newTargets) {
				if(t.getOperations()!=null && !t.getOperations().isEmpty()){
					for (Operation o : t.getOperations()) {
						if (t.getName() != null && o.getName() != null
								&& o.getScopeIds() != null && !t.getName().equals("")
								&& !o.getName().equals("") && o.getScopeIds().size() != 0) {
							opScopeName += t.getName() + "." + o.getName() + "."
									+ getScopeNames(o.getScopeIds()) + ",";
						}
					}
				}
			}
		}
		opScopeName = opScopeName.equals("") ? "" : opScopeName.substring(0,
				opScopeName.length() - 1);
		return opScopeName;
	}
	protected synchronized void checkTargetPutToObjectChangeSet(Set<Target> newTarget, Set<Target>oldTarget) {
		if(newTarget != null || oldTarget != null){
			checkPutToObjectChangeSet(OBJECTCHANGES_TARGETS_NAME, (newTarget != null  ? getTargetNames(newTarget): "") , (oldTarget != null  ? getTargetNames(oldTarget): "")  ,null,null);
			checkPutToObjectChangeSet(OBJECTCHANGES_TARGETS_KEY, (newTarget != null  ? getTargetKeys(newTarget): "") , (oldTarget != null  ? getTargetKeys(oldTarget): "")  ,null,null);
			checkPutToObjectChangeSet(OBJECTCHANGES_TARGETS_DESCRIPTION, (newTarget != null  ? getTargetDesc(newTarget): "") ,  (oldTarget != null  ? getTargetDesc(oldTarget): "") ,null,null);
			checkPutToObjectChangeSet(OBJECTCHANGES_TARGETS_LABEL,(newTarget != null ? getTargetLabel(newTarget) : "") , (oldTarget != null  ? getTargetLabel(oldTarget): "")  ,null,null);
			checkPutToObjectChangeSet(OBJECTCHANGES_OPERATIONS_NAME,(newTarget != null && newTarget.size() > 0 ? getOperationName(newTarget) : "") , (oldTarget != null   && oldTarget.size() > 0 ? getOperationName(oldTarget): "")  ,null,null);
			checkPutToObjectChangeSet(OBJECTCHANGES_OPERATIONS_KEY,(newTarget != null && newTarget.size() > 0 ? getOperationKey(newTarget) : "") , (oldTarget != null   && oldTarget.size() > 0 ? getOperationKey(oldTarget): "")  ,null,null);
			checkPutToObjectChangeSet(OBJECTCHANGES_OPERATIONS_DESCRIPTION,(newTarget != null ? getTargetOpDesc(newTarget) : "") , (oldTarget != null  ? getTargetOpDesc(oldTarget): "")  ,null,null);
			checkPutToObjectChangeSet(OBJECTCHANGES_OPERATIONS_LABEL,(newTarget != null ? getTargetOpLabel(newTarget) : "") , (oldTarget != null  ? getTargetOpLabel(oldTarget): "")  ,null,null);
			checkPutToObjectChangeSet(OBJECTCHANGES_OPERATIONS_SCOPENAMES,(newTarget != null ? getTargetOpScopeName(newTarget) : "") , (oldTarget != null  ? getTargetOpScopeName(oldTarget): "")  ,null,null);
		}
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public ChildApplication getAppDashboardChildApplication() {
		if(deploymentUtil.getAppDashboardApplicationId()!=null){
			Application app = getById(deploymentUtil.getAppDashboardApplicationId());
			if(app!=null && app.getChildApplications()!=null && !app.getChildApplications().isEmpty()){
				for(ChildApplication childApp: app.getChildApplications()){
					if(childApp.getIsDefault()){
						return childApp;
					}
				}
			}
		}
		return null;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<RolesInApplicationJson> getRolesInApplicationsData(Map<String, String> scopeMap) {
		List<RolesInApplicationJson> result = new LinkedList<RolesInApplicationJson>();
		List<String> roleIds = new LinkedList<String>();
		OptionFilter optionFilter = new OptionFilter();
		optionFilter.addFilter(RBACUtil.ROLE_SCOPE_QUERY, scopeMap.get(RBACUtil.SCOPE_KEY_ROLE_VIEW));
		List<Map<String, Object>> roleIdNames =roleDal.getRoleIdNames(new Options(optionFilter));
		if(roleIdNames!=null && !roleIdNames.isEmpty()){
			for(Map<String, Object> roleIdName:roleIdNames){
				roleIds.add(roleIdName.get("roleId").toString());
			}
		}
		optionFilter = new OptionFilter();
		optionFilter.addFilter("applicationScopeQuery", scopeMap.get("Application.View"));
		List<Map<String, Object>> applicationIdNames = getApplicationIdNames(new Options(optionFilter));
		if(applicationIdNames!=null && !applicationIdNames.isEmpty()){
			for(Map<String, Object> applicationIdName:applicationIdNames){
				result.add(new RolesInApplicationJson(Integer.parseInt(applicationIdName.get("applicationId").toString()), applicationIdName.get("name").toString()));
			}
		}
		else{
			return result;
		}
		//MultivaluedMap<String, String> multiValuedMap = new MultivaluedMapImpl();
		MultivaluedMap<String, String> multiValuedMap = new MultivaluedHashMap<>();

		if(scopeMap.get(RBACUtil.SCOPE_KEY_ROLE_VIEW)!=null){
			multiValuedMap.put(RBACUtil.ROLE_SCOPE_QUERY, Arrays.asList(new String[]{scopeMap.get(RBACUtil.SCOPE_KEY_ROLE_VIEW)}));
		}
		if(scopeMap.get("Application.View")!=null){
			multiValuedMap.put("applicationScopeQuery", Arrays.asList(new String[]{scopeMap.get("Application.View")}));
		}
		Options options = new Options(new OptionFilter((MultivaluedMap<String, String>) multiValuedMap));
		Filters filters = prepareFilters(options);
		List<Object[]> queryResponse = filters
				.getList(
						em,
						Object[].class,
						"select a.applicationId, a.name, r.roleId, r.name from Application a left join Role r on(a.applicationId=r.applicationId) "
						,options, SORT_COLUMNS);
		if(queryResponse!=null && queryResponse.size() > 0){
			for(Object[] obj:queryResponse){
				RolesInApplicationJson roleInGroupJson = new RolesInApplicationJson(Integer.parseInt(obj[0].toString()), obj[1].toString());
				int index = result.indexOf(roleInGroupJson);
				if(obj[2]!=null && index!=-1){
					if(roleIds!=null && !roleIds.isEmpty() && roleIds.contains(obj[2].toString())){
						result.get(index).addRole(Integer.parseInt(obj[2].toString()), obj[3].toString());
					}
				}
			}
		}
		return result;
	}

	@Override
	public List<Map<String,Object>> getChildApplicationNamesForScheduleMaintenence(Options options) {
		Filters filters = prepareFilters(options);
		// add default sort by name
		OptionSort optionSort = options != null ? options
				.getOption(OptionSort.class) : null;
		if (optionSort == null) {
			optionSort = new OptionSort(new LinkedList<String>());
		}
		if(optionSort.getSortProperties().isEmpty()){
			optionSort.getSortProperties().add("childApplicationName");
		}
		options = new Options(optionSort, options != null ? options
				.getOption(OptionPage.class) : null, options != null ? options
				.getOption(OptionFilter.class) : null);
		List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();

		List<String> applicationNames = new ArrayList<String>();
		applicationNames.add(RBACUtil.RBAC_UAM_APPLICATION_NAME);
		applicationNames.add(RBACUtil.APP_DASHBOARD_APPLICATION_NAME);

		filters.addCondition("c.application.applicationId NOT IN (select a.applicationId from Application a where a.name IN :applicationNames)");

		filters.addParameter("applicationNames", applicationNames);

		List<Object[]> result = filters.getList(em, Object[].class, "select c.childApplicationId, c.childApplicationName from ChildApplication c", options, SORT_COLUMNS);

		if(result!=null && !result.isEmpty()){
			for(Object[] obj:result){
				Map<String, Object> temp = new HashMap<String, Object>();
				temp.put("childApplicationId", obj[0]);
				temp.put("name", obj[1].toString());
				returnObj.add(temp);
			}
		}
		return returnObj;
	}

	private Map<String, String> getObjectChangeSetLocal(Optional<Application> oldApp,
														Application newApp) {
		AuditLogHelperUtil logHelperUtil = new AuditLogHelperUtil();
		logHelperUtil.putToObjectChangeSet(OBJECTNAME,
				oldApp != null ? oldApp.get().getName() : newApp.getName());
		if ((newApp != null && newApp.getName() != null && !newApp.getName()
				.isEmpty())
				|| (oldApp != null && oldApp.get().getName() != null && !oldApp.get()
				.getName().isEmpty())) {
			logHelperUtil.checkPutToObjectChangeSetIgnoreNulls(
					OBJECTCHANGES_APPNAME, newApp != null ? newApp.getName()
							: null, oldApp != null ? oldApp.get().getName() : null,
					null, null);
		}
		if ((newApp != null && newApp.getDescription() != null && !newApp
				.getDescription().isEmpty())
				|| (oldApp != null && oldApp.get().getDescription() != null && !oldApp.get()
				.getDescription().isEmpty())) {
			logHelperUtil
					.checkPutToObjectChangeSetIgnoreNulls(
							OBJECTCHANGES_DESCRIPTION,
							newApp != null ? newApp.getDescription() : null,
							oldApp != null ? oldApp.get().getDescription() : null,
							null, null);
		}
		if ((newApp != null && newApp.getLabels() != null && newApp.getLabels()
				.size() > 0)
				|| (oldApp != null && oldApp.get().getLabels() != null && oldApp.get()
				.getLabels().size() > 0)) {
			if (newApp != null
					&& newApp.getLabels() != null
					&& oldApp != null
					&& oldApp.get().getLabels() != null
					&& Sets.symmetricDifference(
					new HashSet<String>(oldApp.get().getLabels()),
					new HashSet<String>(newApp.getLabels())).size() == 0) {
				// log nothing, labels are same, used new HashSet as
				// Sets.symmetricDifference results are undefined when sets are
				// of different types. these can be different IndirectSet(Jpa) &
				// TreeSet(UI)
			} else {
				logHelperUtil.checkPutToObjectChangeSetIgnoreNulls(
						OBJECTCHANGES_LABELS,
						newApp != null ? newApp.getLabels() : null,
						oldApp != null ? oldApp.get().getLabels() : null, null, null);
			}
		}
		if ((newApp != null && newApp.getChildApplications() != null && newApp
				.getChildApplications().size() > 0)
				|| (oldApp != null && oldApp.get().getChildApplications() != null && oldApp.get()
				.getChildApplications().size() > 0)) {
			if (newApp != null
					&& newApp.getChildApplications() != null
					&& oldApp != null
					&& oldApp.get().getChildApplications() != null
					&& new TreeSet<ChildApplication>(
					newApp.getChildApplications()).toString().equals(
					new TreeSet<ChildApplication>(oldApp.get()
							.getChildApplications()).toString())) {
				// log nothing, childApps are same, used new TreeSet to make sure same implementation of toString is used
				// these can be different IndirectSet(Jpa) & TreeSet(UI)
			} else {
				logHelperUtil.checkPutToObjectChangeSetIgnoreNulls(
						OBJECTCHANGES_CHILD_APPS,
						newApp != null ? new TreeSet<ChildApplication>(newApp.getChildApplications()) : null,
						oldApp != null ? new TreeSet<ChildApplication>(oldApp.get().getChildApplications()) : null,
						null, null);
			}
		}

		return logHelperUtil.getObjectChangeSet();
	}

	@Override
	public Date getStatus() {
		try {
//			TypedQuery<Date> query = em.createNamedQuery("getDatabaseStatus", Date.class);
//			Date result = query.getSingleResult();
			Date result = applicationRepo.getDatabaseStatus();
			if (result != null) {
				return result;
			}

		} catch (Exception e) {
			log.error("Error in getStatus {}", e);
			throw new HttpServerErrorException(HttpStatus.NO_CONTENT, "Database connectivity lost");
		}
		throw new HttpServerErrorException(HttpStatus.NO_CONTENT, "Database connectivity lost");
	}

	@Override
	public String getApplicationNameByAppKey(String appKey) {
		String applicationName = null;
		try {
//		TypedQuery<ChildApplication> query = em.createNamedQuery("getChildApplicationByAppKey", ChildApplication.class);
//			query.setParameter("appKey", appKey);
//			ChildApplication childApp = query.getSingleResult();
			ChildApplication childApp = childApplicationRepository.getChildApplicationByAppKey(appKey);
			if(childApp != null)
			{
				Application appDetails = getById(childApp.getApplication().getApplicationId());
				applicationName = appDetails.getName();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}

		return applicationName;
	}


	private List<Integer> searchChildApplicationIds(Options options) {
		Filters filters =  new Filters();
		String searchParam = SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM);
		if (searchParam != null && !searchParam.isEmpty()) {
			filters.addCondition(" ( lower(ca.childApplicationName) like :q  ) ");
			filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils
					.wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));
			OptionSort optionSort = options != null ? options.getOption(OptionSort.class) : null;
			options = new Options(optionSort, options != null ? options.getOption(OptionPage.class) : null,
					options != null ? options.getOption(OptionFilter.class) : null);
			List<Integer> result = filters.getList(em, Integer.class, "select ca.childApplicationId from ChildApplication ca",
					options, SORT_COLUMNS);
			return result;
		}
		return null;
	}

	@Override
	public List<Integer> getRevokedChildApplicationIds(Options options) {
		Filters filters = prepareFilters(options);
		OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
		Map<String, String> filters1 = optionFilter == null ? null : optionFilter.getFilters();

		String revokeAppQuery = filters1.get(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY);
		if (revokeAppQuery != null && revokeAppQuery.length() > 1) {
			filters.addCondition("(" + revokeAppQuery + ")");
		}
		List<Integer> result = filters.getList(em, Integer.class, "select ca.childApplicationId from ChildApplication ca",
				options, SORT_COLUMNS);
		return result;
	}

	@Override
	public List<Map<String, Object>> getUserAuthorizedApplicationIdNames(String userName,Options options) {
		List<Map<String, Object>> returnObj = new LinkedList<Map<String, Object>>();
		List<Application> userAuthAppList = getUserAuthorizedApps(userName);
		List<Integer> searchChildApplicationIds = searchChildApplicationIds(options);
		if(userAuthAppList != null && !userAuthAppList.isEmpty()) {
			for(Application application: userAuthAppList){
				if(application.getChildApplications()!=null && !application.getChildApplications().isEmpty()){
					for(ChildApplication childApp:application.getChildApplications()){
//	        			if(childApp.getAppType()!=null && ChildApplication.isNonSSO(childApp.getAppType()) && !deploymentUtil.getIncludeNonSSOAppsInSwitcher()){
//	        				continue;
//	        			}
//	        			if(childApp.getAppType()!=null && ChildApplication.isNative(childApp.getAppType())){
//	        				continue;
//	        			}
						if(childApp.getAppKey()!=null && !childAppPermValidatorUtil.validate(childApp.getAppKey(), userName)){
							continue;
						}
						if(searchChildApplicationIds != null && !searchChildApplicationIds.contains(childApp.getChildApplicationId()))
							continue;

						Map<String, Object> temp = new HashMap<String, Object>();
						temp.put("childApplicationId", childApp.getChildApplicationId());
						temp.put("childApplicationName", childApp.getChildApplicationName());
						returnObj.add(temp);
					}

				}
			}
		}
		return returnObj;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Integer> getAllIds() {
//        TypedQuery<Integer> query = em.createNamedQuery("getAllApplicationIds", Integer.class);
//        return query.getResultList();
		return  applicationRepo.getAllApplicationIds();
	}

	@Override
	public List<Map<String,Object>> getApplicationIdNamesForLoggedInUser(String loggedInUserName) {
		List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
		List<Application> result = getUserAuthorizedApps(loggedInUserName);
		if(result!=null && !result.isEmpty()){
			for(Application obj:result){
				Map<String, Object> temp = new HashMap<String, Object>();
				temp.put("applicationId", obj.getApplicationId());
				temp.put("name", obj.getName());
				returnObj.add(temp);
			}
		}
		return returnObj;
	}



	@Override
	@Transactional
	public void validate(Application application) {
		Set<ConstraintViolation<Application>> violations = validator.validate(application);
		if (!violations.isEmpty()) {
			ConstraintViolation<Application> v = violations.iterator().next();
			ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
			e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
			throw e;
		}

		if (application.getChildApplications() != null) {
			for (ChildApplication childApp : application.getChildApplications()) {
				for (AppUrlData appUrlData : childApp.getAppUrlDataSet()) {
					Set<ConstraintViolation<AppUrlData>> violationsAppUrlData = validator.validate(appUrlData);
					if (!violationsAppUrlData.isEmpty()) {
						ConstraintViolation<AppUrlData> v = violationsAppUrlData.iterator().next();
						ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
						e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
						throw e;
					}
				}
			}
		}
	}





}
