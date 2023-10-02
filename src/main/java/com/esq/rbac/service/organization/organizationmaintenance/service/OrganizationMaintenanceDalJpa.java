package com.esq.rbac.service.organization.organizationmaintenance.service;

import java.util.*;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.calendar.service.CalendarDal;
import com.esq.rbac.service.config.CacheConfig;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.embedded.OrganizationGrid;
import com.esq.rbac.service.organization.reposotiry.OrganizationRepository;
import com.esq.rbac.service.organization.vo.OrganizationHierarchy;
import com.esq.rbac.service.organization.vo.OrganizationInfo;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.embedded.OrganizationHierarchyUser;
import com.esq.rbac.service.user.embedded.OrganizationInfoUser;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.*;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class OrganizationMaintenanceDalJpa extends BaseDalJpa implements
        OrganizationMaintenanceDal {

    private static final Map<String, String> SORT_COLUMNS;
    private static final String ORGANIZATION_BLANK = "blankOrganizationName";
    private static final String DUPLICATED_ORGANIZATION = "duplicateOrganization";
    private static final String DUPLICATED_ORGANIZATION_NAME = "duplicateOrganizationName";


    private AuditLogService auditLogDal;

    @Autowired
    public void setAuditLogService(AuditLogService auditLogDal){
        this.auditLogDal = auditLogDal;
    }


    private UserDal userDal;

    @Autowired
    public void setUserDal(UserDal userDal){
        this.userDal = userDal;
    }




    private CalendarDal calendarDal;

    @Autowired
    public void setCalendarDal(CalendarDal calendarDal){
        this.calendarDal = calendarDal;
    }

    private DeploymentUtil deploymentUtil;

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil){
        this.deploymentUtil = deploymentUtil;
    }

    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        //  SORT_COLUMNS.put("isEnabled", "u.isEnabled");
        SORT_COLUMNS.put("organizationName", "org.organizationName");
        SORT_COLUMNS.put("parentOrganizationId", "org.parentOrganizationId");
        SORT_COLUMNS.put("organizationId", "org.organizationId");
    }


    private OrganizationRepository organizationRepository;

    @Autowired
    public void setOrganizationRepository(OrganizationRepository organizationRepository){
        this.organizationRepository = organizationRepository;
    }

    private CacheService cacheService;
    @Autowired
    public void setCacheService(CacheService cacheService){
        this.cacheService = cacheService;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Organization create(Organization organizationMaintenance, int userId, String target, String operation) {
        cacheService.clearCache(CacheConfig.CLEAR_ALL_ORG_CACHE);//CACHE:: CLEAR
        if (organizationMaintenance == null) {
            throw new IllegalArgumentException();
        }
        validateEntry(organizationMaintenance);
        organizationMaintenance.setCreatedBy(userId);
        organizationMaintenance.setCreatedOn(DateTime.now().toDate());
        organizationMaintenance.setIsDeleted(false);
        String timezone = (organizationMaintenance.getOrganizationTimeZone() != null
                && !organizationMaintenance.getOrganizationTimeZone().isEmpty()
                && Lookup.isTimeZoneValid(organizationMaintenance.getOrganizationTimeZone()))
                ? organizationMaintenance.getOrganizationTimeZone()
                : null;
        organizationMaintenance.setOrganizationTimeZone(timezone);
        //OrganizationMaintenance.setIsExpired(false);
       // em.persist(organizationMaintenance);
        organizationRepository.save(organizationMaintenance);
        Map<String, String> objectChanges = setNewObjectChangeSetLocal(null, organizationMaintenance);
        auditLogDal.createSyncLog(userId, organizationMaintenance.getOrganizationName(),target, operation, objectChanges);
        return organizationMaintenance;
    }
    private Map<String, String> setNewObjectChangeSetLocal(Organization oldOrganization, Organization newOrganization) {
        //clearObjectChangeSet();
        AuditLogHelperUtil logHelperUtil =  new AuditLogHelperUtil();
        logHelperUtil.putToObjectChangeSet(OBJECTNAME, newOrganization!=null?newOrganization.getOrganizationName():oldOrganization.getOrganizationName());
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_ORGANIZATIONNAME, newOrganization.getOrganizationName(), (oldOrganization!=null)?oldOrganization.getOrganizationName(): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_ORGANIZATIONFULLNAME, newOrganization.getOrganizationFullName(), (oldOrganization!=null)?oldOrganization.getOrganizationFullName(): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_ORGANIZATIONREMARKS, newOrganization.getRemarks(), (oldOrganization!=null)?oldOrganization.getRemarks(): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_ORGANIZATIONTYPE, Lookup.getCodeValueById(newOrganization.getOrganizationType().getCodeId()), (oldOrganization!=null)?Lookup.getCodeValueById(oldOrganization.getOrganizationType().getCodeId()) : null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_ORGANIZATIONSUBTYPE, Lookup.getCodeValueById(newOrganization.getOrganizationSubType().getCodeId()), (oldOrganization!=null)?Lookup.getCodeValueById(oldOrganization.getOrganizationSubType().getCodeId()) : null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_ORGANIZATIONPARENTORGANIZATIONID, Lookup.getOrganizationNameById(newOrganization.getParentOrganizationId()), (oldOrganization!=null)?Lookup.getOrganizationNameById(newOrganization.getParentOrganizationId()): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_ORGANIZATIONURL, newOrganization.getOrganizationURL(), (oldOrganization!=null)?oldOrganization.getOrganizationURL(): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_ORGANIZATIONTENANTNAME, Lookup.getTenantNameById(newOrganization.getTenantId()), (oldOrganization!=null)?Lookup.getTenantNameById(newOrganization.getTenantId()): null, null, null);

        return logHelperUtil.getObjectChangeSet();
    }
    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.ORG_BY_NAME_CACHE, unless="#result == null")
    public Organization getOrganizationByOrganizationName(String orgName, Long tenantId){
        try {
           return organizationRepository.getOrganizationByTenant(orgName,tenantId);

        } catch (NoResultException e) {
            return null;
        }
    }
    private void validateEntry(Organization organizationMaintenance) {
        if(organizationMaintenance.getOrganizationName()==null){
            ErrorInfoException errorInfo = new ErrorInfoException(ORGANIZATION_BLANK);
            throw errorInfo;
        }

        Organization org = getOrganizationByOrganizationName(organizationMaintenance.getOrganizationName(), organizationMaintenance.getTenantId());
        if(org!=null){
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_ORGANIZATION).append("; ");
            sb.append(DUPLICATED_ORGANIZATION_NAME).append("=").append(organizationMaintenance.getOrganizationName());
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_ORGANIZATION, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_ORGANIZATION_NAME, organizationMaintenance.getOrganizationName());
            log.info("create; organizationerrorInfo={}", errorInfo);
            throw errorInfo;
        }

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.ORG_CUSTOM_INFO_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<Map<String,Object>> getCustomOrganizationInfo(Options options) {
        Filters filters = prepareFilters(options);
        List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
        List<Object[]> result = filters
                .getList(
                        em,
                        Object[].class,
                        "select org.organizationId, org.organizationName, org.organizationFullName, org.organizationType, org.organizationSubType, org.parentOrganizationId, org.organizationURL , (SELECT org1.organizationName FROM Organization org1 WHERE org1.organizationId = org.parentOrganizationId) from Organization org",
                        options, SORT_COLUMNS);
        if(result!=null && !result.isEmpty()){
            for(Object[] obj:result){
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("organizationId", obj[0]);
                temp.put("organizationName", obj[1]);
                temp.put("organizationFullName", obj[2]);
                temp.put("organizationType", obj[3]);
                temp.put("organizationSubType", obj[4]);
                temp.put("parentOrganizationId", obj[5]);
                temp.put("organizationURL", obj[6]);
                temp.put("parentOrganizationName", obj[7]);
                returnObj.add(temp);
            }

        }
        return returnObj;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String,Object>> searchCustomOrganizationInfo(Options options) {
        Filters filters = prepareFilters(options);
        filters.addCondition("(" + "lower(org.organizationName) like :q  "
                + ")");

        filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase()));
        List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
        List<Object[]> result = filters
                .getList(
                        em,
                        Object[].class,
                        "select org.organizationId, org.organizationName, org.organizationFullName, org.organizationType, org.organizationSubType, org.parentOrganizationId, org.organizationURL from Organization org",
                        options, SORT_COLUMNS);

        if(result!=null && !result.isEmpty()){
            for(Object[] obj:result){
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("organizationId", obj[0]);
                temp.put("organizationName", obj[1]);
                temp.put("organizationFullName", obj[2]);
                temp.put("organizationType", obj[3]);
                temp.put("organizationSubType", obj[4]);
                temp.put("parentOrganizationId", obj[5]);
                temp.put("organizationURL", obj[6]);

                returnObj.add(temp);
            }

        }
        return returnObj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Organization update(
            Organization organizationMaintenance, int userId, String target, String operation) {
        cacheService.clearCache(CacheConfig.CLEAR_ALL_ORG_CACHE);//CACHE:: CLEAR
        if (organizationMaintenance.getOrganizationId() == null) {
            throw new IllegalArgumentException("organizationId missing");
        }
        Organization existingOrganizationMaintenance = em.find(
                Organization.class,
                organizationMaintenance.getOrganizationId());
        if (existingOrganizationMaintenance == null) {
            throw new IllegalArgumentException("organizationId invalid");
        }
        if((!existingOrganizationMaintenance.getOrganizationName().equalsIgnoreCase(organizationMaintenance.getOrganizationName())) ||
                (existingOrganizationMaintenance.getTenantId().compareTo(organizationMaintenance.getTenantId())!=0)){//if org name or tenantid has changed
            Organization org = getOrganizationByOrganizationName(organizationMaintenance.getOrganizationName(), organizationMaintenance.getTenantId());
            if(org!=null && org.getOrganizationId() != existingOrganizationMaintenance.getOrganizationId()){
                StringBuilder sb = new StringBuilder();
                sb.append(DUPLICATED_ORGANIZATION).append("; ");
                sb.append(DUPLICATED_ORGANIZATION_NAME).append("=").append(organizationMaintenance.getOrganizationName());
                log.info("update; {}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_ORGANIZATION, sb.toString());
                errorInfo.getParameters().put(DUPLICATED_ORGANIZATION_NAME, organizationMaintenance.getOrganizationName());
                log.info("update; organizationerrorInfo={}", errorInfo);
                throw errorInfo;
            }
        }

		/*validateExistingEntry(OrganizationMaintenance);
		setObjectChangeSet(existingOrganizationMaintenance,
				OrganizationMaintenance);*/
        Map<String, String> objectChanges = setNewObjectChangeSetLocal(existingOrganizationMaintenance, organizationMaintenance);

        existingOrganizationMaintenance.setCreatedBy(userId);
        existingOrganizationMaintenance.setCreatedOn(DateTime.now().toDate());
        existingOrganizationMaintenance.setOrganizationName(organizationMaintenance.getOrganizationName());
        existingOrganizationMaintenance.setOrganizationFullName(organizationMaintenance.getOrganizationFullName());
        existingOrganizationMaintenance.setOrganizationType(organizationMaintenance.getOrganizationType());
        existingOrganizationMaintenance.setOrganizationSubType(organizationMaintenance.getOrganizationSubType());
        existingOrganizationMaintenance.setParentOrganizationId(organizationMaintenance.getParentOrganizationId());
        existingOrganizationMaintenance.setOrganizationURL(organizationMaintenance.getOrganizationURL());
        existingOrganizationMaintenance.setRemarks(organizationMaintenance.getRemarks());
        existingOrganizationMaintenance.setTenantId(organizationMaintenance.getTenantId());
        existingOrganizationMaintenance.setUpdatedBy(userId);
        existingOrganizationMaintenance.setUpdatedOn(DateTime.now().toDate());
        existingOrganizationMaintenance.setIsDeleted(false);
        existingOrganizationMaintenance.setIsShared(organizationMaintenance.getIsShared());
        String timezone = (organizationMaintenance.getOrganizationTimeZone() != null
                && !organizationMaintenance.getOrganizationTimeZone().isEmpty()
                && Lookup.isTimeZoneValid(organizationMaintenance.getOrganizationTimeZone()))
                ? organizationMaintenance.getOrganizationTimeZone()
                : null;
        existingOrganizationMaintenance.setOrganizationTimeZone(timezone);
        Organization returnObj = em.merge(existingOrganizationMaintenance);
        int updatedUsers = userDal.updateAllUsersForOrganization(organizationMaintenance.getOrganizationId(), organizationMaintenance.getIsShared(), userId);
        log.info("updatedUsers; {}", updatedUsers);
        auditLogDal.createSyncLog(userId, organizationMaintenance.getOrganizationName(),target, operation, objectChanges);
        return returnObj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(long organizationId, int userId) {
        cacheService.clearCache(CacheConfig.CLEAR_ALL_ORG_CACHE);//CACHE:: CLEAR
        Organization organizationMaintenance = em.find(
                Organization.class, organizationId);
        if (organizationMaintenance == null) {
            throw new IllegalArgumentException("Invalid OrganizationId");
        }

        Long userCount =organizationRepository.countByOrganizationId(organizationId);
        if(userCount!=null && userCount > 0){
            throw new ErrorInfoException("organizationUserAssociationFound", "Organizataion can't be deleted, associated with user");
        }
        Integer updateCount = organizationRepository.deleteByOrganizationId(organizationId);

        log.info("deleteById: {} organizationCalendar mapping deleted Organization ID = {}", updateCount, organizationId);
        organizationRepository.deleteOrganizationById(organizationId,userId,DateTime.now().toDate());
        auditLogDal.createSyncLog(userId, organizationMaintenance.getOrganizationName(),"Organization", "Delete", setNewObjectChangeSetLocal(null, organizationMaintenance));
    }
	/*
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteByChildOrganizationId(int childOrganizationId) {
		Query query = em.createQuery("delete from OrganizationMaintenance a where a.childOrganizationId= :childOrganizationId");
		query.setParameter("childOrganizationId", childOrganizationId);
		int result = query.executeUpdate();
		log.info("deleteByChildOrganizationId; result={}",result);
	}*/

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options
                .getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null
                : optionFilter.getFilters();
        if (filters != null) {

            String filterOrganizationId = filters.get("filterOrganizationId");
            if (filterOrganizationId != null && filterOrganizationId.length() > 0) {
                result.addCondition(" org.organizationId = :filterOrganizationId ");
                result.addParameter("filterOrganizationId",
                        Long.valueOf(filterOrganizationId));
            }

            String organizationId = filters.get("organizationId");
            if (organizationId != null && organizationId.length() > 0) {
                result.addCondition(" org.organizationId = :organizationId or org.parentOrganizationId = :organizationId");
                result.addParameter("organizationId",
                        Long.valueOf(organizationId));
            }
            String organizationName = filters.get("organizationName");
            if (organizationName != null && organizationName.length() > 0) {
                result.addCondition("org.organizationName = :organizationName");
                result.addParameter("organizationName", organizationName);
            }

            String tenantId = filters.get("tenantId");
            if (tenantId != null && tenantId.length() > 0) {
                result.addCondition("org.tenantId = :tenantId");
                result.addParameter("tenantId", Long.valueOf(tenantId));
            }

            String tenantName = filters.get("tenantName");
            if (tenantName != null && tenantName.length() > 0) {
                result.addCondition(" org.tenantId = :tenantIdByName ");
                result.addParameter("tenantIdByName", Lookup.getTenantIdByName(tenantName));
            }

            String organizationScopeQuery = filters.get(RBACUtil.ORGANIZATION_SCOPE_QUERY);
            if (organizationScopeQuery != null && organizationScopeQuery.length() > 1) {
                result.addCondition(" (" + organizationScopeQuery + ") ");
            }

            String organizationTypeCode = filters.get("organizationTypeCode");
            if (organizationTypeCode != null
                    && organizationTypeCode.length() > 0) {
                result.addCondition("org.organizationType.codeId = :organizationTypeCode");
                result.addParameter("organizationTypeCode",
                        Long.valueOf(organizationTypeCode));
            }

            String organizationSubTypeCode = filters.get("organizationSubTypeCode");
            if (organizationSubTypeCode != null && organizationSubTypeCode.length() > 0) {
                result.addCondition("org.organizationSubType.codeId = :organizationSubTypeCode");
                result.addParameter("organizationSubTypeCode", Long.valueOf(organizationSubTypeCode));
            }

            String isShared = filters.get("isShared");
            if (isShared != null && isShared.length() > 0) {
                boolean isSharedBool = Boolean.valueOf(isShared);
                result.addCondition(" org.isShared = :isSharedBool ");
                result.addParameter("isSharedBool", isSharedBool);
            }

            String userName = filters.get("userName");
            if (userName != null && userName.length() > 0) {
                String scope = RBACUtil.extractScopeForOrganization(
                        userDal.getUserScopes(userName, RBACUtil.RBAC_UAM_APPLICATION_NAME, true), null, false);
                if(scope!=null && !scope.isEmpty()){
                    result.addCondition(" (" + scope + ") ");
                }
            }

            String loggedInUserName = filters.get("loggedInUserName");
            if (loggedInUserName != null && loggedInUserName.length() > 0) {
                String scope = RBACUtil.extractScopeForOrganization(
                        userDal.getUserScopes(loggedInUserName, RBACUtil.RBAC_UAM_APPLICATION_NAME, true), null, false);
                if(scope!=null && !scope.isEmpty()){
                    result.addCondition(" (" + scope + ") ");
                }
            }

			/*String childOrganizationId = filters.get("childOrganizationId");
			if (childOrganizationId != null && !childOrganizationId.equals("null") && childOrganizationId.length() > 0) {
				result.addCondition("a.childOrganizationId = :childOrganizationId");
				result.addParameter("childOrganizationId",
						Integer.valueOf(childOrganizationId));
			}

			String fromDate = filters.get("fromDate");
			if (fromDate != null && fromDate.length() > 0) {
				result.addCondition("a.fromDate = :fromDate");
				result.addParameter("fromDate", fromDate);
			}

			String toDate = filters.get("toDate");
			if (toDate != null && toDate.length() > 0) {
				result.addCondition("a.toDate = :toDate");
				result.addParameter("toDate", toDate);
			}

			String message = filters.get("message");
			if (message != null && message.length() > 0) {
				result.addCondition("a.message = :message");
				result.addParameter("message", message);
			}

			String isEnabled = filters.get("isEnabled");
			if (isEnabled != null && isEnabled.length() > 0) {
				result.addCondition("a.isEnabled = :isEnabled");
				result.addParameter("isEnabled", Boolean.valueOf(isEnabled));
			}

			String isExpired = filters.get("isExpired");
			if (isExpired != null && isExpired.length() > 0) {
				result.addCondition("a.isExpired = :isExpired");
				result.addParameter("isExpired", Boolean.valueOf(isExpired));
			}*/

        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.ALL_ORG_LIST_CACHE,keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless = "#result == null")
    public List<Organization> getList(Options options) {
        Filters filters = prepareFilters(options);
        return filters
                .getList(em, Organization.class,
                        " select org from Organization org ", options,
                        SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.ORG_HIERARCHY_D3_VIEW_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public Map<Long, Set<OrganizationHierarchy>> getOrganizationHierarchy(Options options) {
        List<Organization> organizationList = getList(options);
        Map<Long, Set<OrganizationHierarchy>> lookupCache = new TreeMap<Long, Set<OrganizationHierarchy>>();
        Map<Long, OrganizationHierarchy> temporaryMap = new LinkedHashMap<Long, OrganizationHierarchy>();
        Map<Long, List<Map<String, Object>>> selectBoxMap = new LinkedHashMap<Long, List<Map<String, Object>>>();
        if(organizationList!=null && !organizationList.isEmpty()){
            for(Organization org:organizationList){
                OrganizationHierarchy orgHierarchy = new OrganizationHierarchy(org);
                temporaryMap.put(org.getOrganizationId(), orgHierarchy);
                if(selectBoxMap.get(org.getTenantId())==null){
                    selectBoxMap.put(org.getTenantId(), new LinkedList<Map<String,Object>>());
                }
                Map<String, Object> tempMap = new HashMap<String, Object>();
                tempMap.put("id", orgHierarchy.getId());
                tempMap.put("text", orgHierarchy.getOrganizationName());
                selectBoxMap.get(org.getTenantId()).add(tempMap);
            }
            for(Organization org:organizationList){
                //for case where we are fetching tree from a sub-node which is non parent -> temporaryMap.get(org.getParentOrganizationId())!=null
                if(org.getParentOrganizationId()!=null && temporaryMap.get(org.getParentOrganizationId())!=null){
                    temporaryMap.get(org.getParentOrganizationId()).getChildren().add(temporaryMap.get(org.getOrganizationId()));
                }
                else{
                    if(lookupCache.get(org.getTenantId())==null){
                        lookupCache.put(org.getTenantId(), new TreeSet<OrganizationHierarchy>());
                    }
                    lookupCache.get(org.getTenantId()).add(temporaryMap.get(org.getOrganizationId()));
                }
            }
        }
        if(lookupCache!=null && lookupCache.size() > 0){
            for(Long tenantId: lookupCache.keySet()){
                //get users for each tenant
                List<OrganizationHierarchyUser> usersList = organizationRepository.findOrganizationHierarchyUsersByTenantId(tenantId);
                if(usersList!=null && !usersList.isEmpty()){
                    for(OrganizationHierarchyUser orgUser: usersList){
                        if(temporaryMap.get(orgUser.getOrganizationId()) != null){
                            String groupName = Lookup.getGroupName(orgUser.getGroupId());
                            orgUser.setGroupName((groupName!=null && !groupName.isEmpty())?groupName:null);
                            temporaryMap.get(orgUser.getOrganizationId()).getChildren().add(orgUser);
                        }
                        if(selectBoxMap.get(tenantId)==null){
                            selectBoxMap.put(tenantId, new LinkedList<Map<String,Object>>());
                        }
                        Map<String, Object> tempMap = new HashMap<String, Object>();
                        tempMap.put("id", orgUser.getId());
                        tempMap.put("text", orgUser.getUserName());
                        selectBoxMap.get(tenantId).add(tempMap);
                    }
                }
                for(OrganizationHierarchy orgHierarchy:lookupCache.get(tenantId)){
                    orgHierarchy.getSelectBoxData().addAll(selectBoxMap.get(orgHierarchy.getTenantId()));
                }
            }
        }
        return lookupCache;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String, Object>> getOrganizationInfo(Options options) {
        List<Organization> organizationList = getList(options);
        Map<Long, Set<OrganizationInfo>> lookupCache = new TreeMap<Long, Set<OrganizationInfo>>();
        Map<Long, OrganizationInfo> temporaryMap = new LinkedHashMap<Long, OrganizationInfo>();
        List<Map<String, Object>> resultMap = new LinkedList<Map<String,Object>>();
        if(organizationList!=null && !organizationList.isEmpty()){
            for(Organization org:organizationList){
                OrganizationInfo orgInfo = new OrganizationInfo(org);
                temporaryMap.put(org.getOrganizationId(), orgInfo);
            }
            for(Organization org:organizationList){
                //for case where we are fetching tree from a sub-node which is non parent -> temporaryMap.get(org.getParentOrganizationId())!=null
                if(org.getParentOrganizationId()!=null && temporaryMap.get(org.getParentOrganizationId())!=null){
                    temporaryMap.get(org.getParentOrganizationId()).getOrganizations().add(temporaryMap.get(org.getOrganizationId()));
                }
                else{
                    if(lookupCache.get(org.getTenantId())==null){
                        lookupCache.put(org.getTenantId(), new TreeSet<OrganizationInfo>());
                    }
                    lookupCache.get(org.getTenantId()).add(temporaryMap.get(org.getOrganizationId()));
                }
            }
        }
        if(lookupCache!=null && lookupCache.size() > 0){
            for(Long tenantId: lookupCache.keySet()){
                //get users for each tenant

                List<OrganizationInfoUser> usersList = organizationRepository.findOrganizationInfoUsersByTenantId(tenantId);

                if(usersList!=null && !usersList.isEmpty()){
                    for(OrganizationInfoUser orgUser: usersList){
                        if(temporaryMap.get(orgUser.getOrganizationId()) != null){
                            String groupName = Lookup.getGroupName(orgUser.getGroupId());
                            orgUser.setGroupName((groupName!=null && !groupName.isEmpty())?groupName:null);
                            temporaryMap.get(orgUser.getOrganizationId()).getUsers().add(orgUser);
                        }
                    }
                }
                Map<String, Object> resultObj = new LinkedHashMap<String, Object>();
                resultObj.put("tenantId", tenantId);
                resultObj.put("tenantName", Lookup.getTenantNameById(tenantId));
                resultObj.put("organizations", lookupCache.get(tenantId));
                resultMap.add(resultObj);
            }
        }
        return resultMap;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int getCount(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getCount(em,
                "select count(org) from Organization org");
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Organization getById(long organizationId) {

        Organization org =organizationRepository.findById(organizationId).get();
        org = getOrganizationTimezone(org);
        return org;
    }

    private Organization getOrganizationTimezone(Organization org) {
        if (org != null && (org.getOrganizationTimeZone() == null || org.getOrganizationTimeZone().isEmpty())) {
            // Get OrganizationTimeZone
            String getTimezone = calendarDal.getTimezoneFromOrganization(org.getOrganizationId(),org.getTenantId());
            org.setOrganizationTimeZone(getTimezone);
        }
        return org;
    }



    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String,Object>> getOrganizationIdNames(Options options) {
        Filters filters = prepareFilters(options);
        // add default sort by name
        OptionSort optionSort = options != null ? options
                .getOption(OptionSort.class) : null;
        if (optionSort == null) {
            optionSort = new OptionSort(new LinkedList<String>());
        }
        if(optionSort.getSortProperties().isEmpty()){
            optionSort.getSortProperties().add("organizationName");
        }
        options = new Options(optionSort, options != null ? options
                .getOption(OptionPage.class) : null, options != null ? options
                .getOption(OptionFilter.class) : null);
        List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
        List<Object[]> result = filters.getList(em, Object[].class, "select org.organizationId, org.organizationName from Organization org", options, SORT_COLUMNS);
        if(result!=null && !result.isEmpty()){
            for(Object[] obj:result){
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("organizationId", obj[0]);
                temp.put("organizationName", obj[1].toString());
                returnObj.add(temp);
            }
        }
        return returnObj;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String, Object>> getOrganizationIdNamesDetails(Options options) {
        Filters filters = prepareFilters(options);
        // add default sort by name
        OptionSort optionSort = options != null ? options.getOption(OptionSort.class) : null;
        if (optionSort == null) {
            optionSort = new OptionSort(new LinkedList<String>());
        }
        if (optionSort.getSortProperties().isEmpty()) {
            optionSort.getSortProperties().add("organizationName");
        }
        options = new Options(optionSort, options != null ? options.getOption(OptionPage.class) : null,
                options != null ? options.getOption(OptionFilter.class) : null);
        List<Map<String, Object>> returnObj = new LinkedList<Map<String, Object>>();
        List<Object[]> result = filters.getList(em, Object[].class,
                "select org.organizationId, org.organizationName, org.parentOrganizationId, org.tenantId from Organization org",
                options, SORT_COLUMNS);
        if (result != null && !result.isEmpty()) {
            for (Object[] obj : result) {
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("organizationId", obj[0]);
                temp.put("organizationName", obj[1].toString());
                temp.put("parentOrganizationId", obj[2]);
                temp.put("tenantId", obj[3]);
                returnObj.add(temp);
            }
        }
        return returnObj;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String,Object>> getOrganizationIdNamesWithScope(Options options) {
        Filters filters = prepareFilters(options);
        // add default sort by name
        OptionSort optionSort = options != null ? options
                .getOption(OptionSort.class) : null;
        if (optionSort == null) {
            optionSort = new OptionSort(new LinkedList<String>());
        }
        if(optionSort.getSortProperties().isEmpty()){
            optionSort.getSortProperties().add("organizationName");
        }
        options = new Options(optionSort, options != null ? options
                .getOption(OptionPage.class) : null, options != null ? options
                .getOption(OptionFilter.class) : null);
        List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
        List<Object[]> result = filters.getList(em, Object[].class, "select org.organizationId, org.organizationName, org.tenantId from Organization org", options, SORT_COLUMNS);
        if(result!=null && !result.isEmpty()){
            Map<Long, Map<String, Object>> tenantSet = new LinkedHashMap<Long, Map<String, Object>>();
            for(Object[] obj:result){
                if(obj[2]!=null){
                    Long tenantId = Long.parseLong(obj[2].toString());
                    if(!tenantSet.containsKey(tenantId)){
                        Map<String, Object> tenantMap = new LinkedHashMap<String, Object>();
                        tenantMap.put("tenantId", tenantId);
                        tenantMap.put("tenantName", tenantId);
                        tenantMap.put("organizations", new LinkedList<Map<String, Object>>());
                        tenantSet.put(tenantId, tenantMap);
                    }
                    Map<String, Object> temp = new HashMap<String, Object>();
                    temp.put("organizationId", obj[0]);
                    temp.put("organizationName", obj[1].toString());
                    ((List<Map<String, Object>>)tenantSet.get(tenantId).get("organizations")).add(temp);
                }
            }
            returnObj.addAll(tenantSet.values());
        }
        return returnObj;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.ORG_LIST_BY_TENANT_CACHE,keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<Map<String,Object>> getOrganizationByTenantId(Options options) {
        Filters filters = prepareFilters(options);
        // add default sort by name
        OptionSort optionSort = options != null ? options
                .getOption(OptionSort.class) : null;
        if (optionSort == null) {
            optionSort = new OptionSort(new LinkedList<String>());
        }
        if(optionSort.getSortProperties().isEmpty()){
            optionSort.getSortProperties().add("organizationName");
        }
        options = new Options(optionSort, options != null ? options
                .getOption(OptionPage.class) : null, options != null ? options
                .getOption(OptionFilter.class) : null);
        List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
        List<Object[]> result = filters.getList(em, Object[].class, "select org.organizationId, org.organizationName,org.isShared from Organization org", options, SORT_COLUMNS);
        if(result!=null && !result.isEmpty()){
            for(Object[] obj:result){
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("organizationId", obj[0]);
                temp.put("organizationName", obj[1].toString());
                temp.put("isShared", (obj[2]!=null)?obj[2]:false);
                returnObj.add(temp);
            }
        }
        return returnObj;
    }


    /******* RBAC-1656 Start ******/
    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.ORG_HIERARCHY_GRID_VIEW_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<OrganizationGrid> getOrganizationHierarchyGridView(Options options) {
        String type = options.getOption(OptionFilter.class).getFilter("type");
        if (type != null && !type.equalsIgnoreCase("user")) {
//			Filters filters = prepareFilters(options);

            Long orgId = Long.parseLong(options.getOption(OptionFilter.class).getFilter("itemId").substring(1));
            Long tenantId = Long.parseLong(options.getOption(OptionFilter.class).getFilter("tenantId"));

            String showAll = options.getOption(OptionFilter.class).getFilter("showAll");

            String sortOrder = options.getOption(OptionFilter.class).getFilter("sortOrder");
            Long startNo = Long.parseLong(options.getOption(OptionFilter.class).getFilter("startNo"));
            if(sortOrder == null || sortOrder.isEmpty())
                sortOrder = "ASC";

//
//			filters.addCondition("org.tenantId = :tenantId and org.parentOrganizationId = :orgId");
//			filters.addParameter("orgId", orgId);
//			filters.addParameter("tenantId", tenantId);
//			List<OrganizationHierarchy> organizationList = filters.getList(em, OrganizationHierarchy.class,
//					"SELECT new com.esq.rbac.model.rest.OrganizationHierarchy(org) from Organization org", options,
//					SORT_COLUMNS);// query1.getResultList();

            String queryKey = "getOrganizationInBatchForGridViewASC";
            String queryKeyUser = "getUsersInBatchForGridViewASC";
            if(sortOrder.equalsIgnoreCase("desc")) {
                queryKey = "getOrganizationInBatchForGridViewDESC";
                queryKeyUser = "getUsersInBatchForGridViewDESC";
            }

            if(showAll != null && !showAll.isEmpty() && showAll.equalsIgnoreCase("showAll"))
            {
                queryKey = "getOrganizationAllForGridViewASC";
                queryKeyUser = "getUsersAllForGridViewASC";
                if(sortOrder.equalsIgnoreCase("desc")) {
                    queryKey = "getOrganizationAllForGridViewDESC";
                    queryKeyUser = "getUsersAllForGridViewDESC";
                }

            }
            List<Organization> organizationList =organizationRepository.getOrganizationInBatchForGridViewASC(tenantId,orgId, Math.toIntExact(startNo),deploymentUtil.getBatchSizeForGridData()/2);

            List<OrganizationGrid> lookupCache = new ArrayList<OrganizationGrid>();
            if (organizationList != null && !organizationList.isEmpty()) {
                for (Organization org : organizationList) {
                    OrganizationHierarchy gridView = new OrganizationHierarchy(org);
                    if(gridView.getTenantId() == null)
                        gridView.setTenantId(Lookup.getTenantIdByOrganizationId(gridView.getOrganizationId()));
                    OrganizationGrid orgGrid = new OrganizationGrid();
                    orgGrid.setItemId("o" + gridView.getOrganizationId());
                    orgGrid.setLabel(gridView.getOrganizationName());
                    orgGrid.setType(gridView.getOrganizationType().getName().toLowerCase());
                    orgGrid.setTenantId(gridView.getTenantId());
                    orgGrid.setParentOrgId(gridView.getParentOrganizationId());
                    orgGrid.setDetails(gridView);
                    lookupCache.add(orgGrid);

                }
            }

            // get users for each tenant

//			Filters filtersUsers = new Filters();

//			filtersUsers.addCondition("u.organizationId in ("
//					+ "	            	select org.organizationId from Organization org where org.tenantId = :tenantId and org.organizationId = :orgId"
//					+ "	            ) order by u.userName");
//
//			filtersUsers.addParameter("tenantId", tenantId);
//			filtersUsers.addParameter("orgId", orgId);
//			List<OrganizationHierarchyUser> usersList = filtersUsers.getList(em, OrganizationHierarchyUser.class,
//					"SELECT new com.esq.rbac.model.rest.OrganizationHierarchyUser(u) from User u", options);

           List<User> usersList=organizationRepository.findUsersByOrganizationIdAndStatus(tenantId,orgId, Math.toIntExact(startNo), deploymentUtil.getBatchSizeForGridData()/2);
            if (usersList != null && !usersList.isEmpty()) {
                for (User u : usersList) {
                    OrganizationGrid orgGrid = new OrganizationGrid();
                    orgGrid.setItemId("u" + u.getUserId());
                    orgGrid.setLabel(u.getUserName());
                    orgGrid.setType("user");
                    orgGrid.setTenantId(Lookup.getTenantIdByOrganizationId(u.getOrganizationId()));
                    orgGrid.setParentOrgId(u.getOrganizationId());
                    orgGrid.setDetails(u);
                    lookupCache.add(orgGrid);

                }
            }

//			lookupCache.put(orgId, selectBox);

            return lookupCache;
        }

        return null;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.ORG_SEARCH_BOX_DATA_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public Map<String, List<Map<String, Object>>> getSearchBoxData(Options options) {
       List<OrganizationHierarchy> organizationList =  organizationRepository.getOrganizationForGridView(Long.parseLong(options.getOption(OptionFilter.class).getFilter("tenantId")));

        Map<String, List<Map<String, Object>>> lookupCache = new TreeMap<String, List<Map<String, Object>>>();
        List<Map<String, Object>> selectBox = new LinkedList<Map<String, Object>>();

        if (organizationList != null && !organizationList.isEmpty()) {
            for (Organization org : organizationList) {
                OrganizationHierarchy orgView = new OrganizationHierarchy(org);
                Map<String, Object> temporaryMap = new HashMap<String, Object>();
                temporaryMap.put("id", "o" + orgView.getOrganizationId());
                temporaryMap.put("text", orgView.getOrganizationName());
                selectBox.add(temporaryMap);

            }
        }

        // get users for each tenant
        List<OrganizationHierarchyUser> usersList = organizationRepository.findOrganizationHierarchyUsersByTenantId(Long.parseLong(options.getOption(OptionFilter.class).getFilter("tenantId")));

        if (usersList != null && !usersList.isEmpty()) {
            for (OrganizationHierarchyUser u : usersList) {
                Map<String, Object> temporaryMap = new HashMap<String, Object>();
                temporaryMap.put("id", "u" + u.getUserId());
                temporaryMap.put("text", u.getUserName());
                selectBox.add(temporaryMap);
            }
        }
        lookupCache.put("selectBox", selectBox);
        return lookupCache;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<String, Integer> getNodesCount(Options options) {
        Map<String, Integer> responseMap = new HashMap<String, Integer>();
        Filters filters = prepareFilters(options);
        int groupsCount = filters.getCount(em, "select count(org) from Organization org");

        Long userCount = organizationRepository.countUsersByTenantId(String.valueOf(Integer.parseInt(options.getOption(OptionFilter.class).getFilter("tenantId"))));
//			int userCount =  filters.getCount(em, "select count(org) from User org");

        responseMap.put("nodeCount", (int) (long) userCount + groupsCount);
        responseMap.put("configureDataLimit", deploymentUtil.getNodeLimitForOrgGridView());
        return responseMap;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public OrganizationGrid getTenantOrganizationGrid(Options options) {

        Filters filters = prepareFilters(options);
        filters.addCondition("org.tenantId = :tenantId and org.parentOrganizationId IS NULL");
        filters.addParameter("tenantId", Long.parseLong(options.getOption(OptionFilter.class).getFilter("tenantId")));
        List<Organization> organizationList = filters.getList(em, Organization.class,
                "SELECT org from com.esq.rbac.service.organization.domain.Organization org", options, SORT_COLUMNS);// query1.getResultList();

        for (Organization org : organizationList) {
            OrganizationHierarchy gridView = new OrganizationHierarchy(org);
            OrganizationGrid orgGrid = new OrganizationGrid();
            orgGrid.setItemId("o" + gridView.getOrganizationId());
            orgGrid.setLabel(gridView.getOrganizationName());
            orgGrid.setType(gridView.getOrganizationType().getName().toLowerCase());
            orgGrid.setTenantId(gridView.getTenantId());
            orgGrid.setParentOrgId(gridView.getParentOrganizationId());
            orgGrid.setDetails(gridView);
            return orgGrid;
        }

        return null;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<String, OrganizationGrid> getSearchData(Options options) {
        if (options != null && options.getOption(OptionFilter.class).getFilter("searchId") != null) {
            String searchValue = options.getOption(OptionFilter.class).getFilter("searchId");
            String searchType = searchValue.substring(0, 1);
            Long searchId = Long.parseLong(searchValue.substring(1));

            Map<String, OrganizationGrid> lookupCache = new TreeMap<String, OrganizationGrid>();

            Map<String, OrganizationGrid> orgGridData = getOrgDetails(searchId, searchType, options, lookupCache);

            return orgGridData;
        } else {
            return null;
        }

    }

    Map<String, OrganizationGrid> getOrgDetails(Long searchId, String searchType, Options options,
                                                Map<String, OrganizationGrid> lookupCache) {
        OrganizationGrid orgGrid = new OrganizationGrid();
        List<OrganizationHierarchy> organizationList = new LinkedList<OrganizationHierarchy>();
        List<OrganizationHierarchyUser> usersList = new LinkedList<OrganizationHierarchyUser>();

        if (searchType.equalsIgnoreCase("o")) {
            Filters filters = prepareFilters(options);

            filters.addCondition("org.tenantId = :tenantId and org.organizationId = :searchId");
            filters.addParameter("searchId", searchId);
            filters.addParameter("tenantId",
                    Long.parseLong(options.getOption(OptionFilter.class).getFilter("tenantId")));
            organizationList = filters.getList(em, OrganizationHierarchy.class,
                    "SELECT new com.esq.rbac.model.rest.OrganizationHierarchy(org) from Organization org", options,
                    SORT_COLUMNS);// query1.getResultList();
        } else if (searchType.equalsIgnoreCase("u")) {
            Filters filtersUsers = new Filters();

            filtersUsers.addCondition(
                    "u.userId = :searchId and u.organizationId in (select org.organizationId from Organization org"
                            + " where org.tenantId = :tenantId" + ") order by u.userName");
            filtersUsers.addParameter("tenantId",
                    Long.parseLong(options.getOption(OptionFilter.class).getFilter("tenantId")));
            filtersUsers.addParameter("searchId", searchId);
            usersList = filtersUsers.getList(em, OrganizationHierarchyUser.class,
                    "SELECT new com.esq.rbac.model.rest.OrganizationHierarchyUser(u) from User u", options);
        }

        if (!organizationList.isEmpty()) {
            for (OrganizationHierarchy org : organizationList) {
                OrganizationHierarchy gridView = new OrganizationHierarchy(org);
                orgGrid.setItemId("o" + gridView.getOrganizationId());
                orgGrid.setLabel(gridView.getOrganizationName());
                orgGrid.setType(gridView.getOrganizationType().getName().toLowerCase());
                orgGrid.setTenantId(gridView.getTenantId());
                orgGrid.setParentOrgId(gridView.getParentOrganizationId());
                orgGrid.setDetails(gridView);
            }
        } else if (!usersList.isEmpty()) {
            for (OrganizationHierarchyUser user : usersList) {
                OrganizationHierarchyUser userView = new OrganizationHierarchyUser(user);
                orgGrid.setItemId("u" + userView.getUserId());
                orgGrid.setLabel(userView.getUserName());
                orgGrid.setType("user");
                orgGrid.setTenantId(Long.parseLong(options.getOption(OptionFilter.class).getFilter("tenantId")));
                orgGrid.setParentOrgId(userView.getOrganizationId());
                orgGrid.setDetails(userView);
            }
        }

        // for Host
        if (orgGrid.getParentOrgId() == null) {
            lookupCache.put("0", orgGrid);
            return lookupCache;
        } else {
            lookupCache.put(String.valueOf(orgGrid.getParentOrgId()), orgGrid);
            getOrgDetails(orgGrid.getParentOrgId(), "o", options, lookupCache);
        }
        return lookupCache;

    }

    @Override
    public Integer getBatchSizeForData() {
        return deploymentUtil.getBatchSizeForGridData();
    }

    /******* RBAC-1656 End ******/

}
