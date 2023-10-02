package com.esq.rbac.service.tenant.service;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.config.CacheConfig;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.group.repository.GroupRepository;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.organization.reposotiry.OrganizationRepository;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.tenant.emaddable.TenantIdentifier;
import com.esq.rbac.service.tenant.repository.TenantRepository;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.*;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.MultivaluedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Slf4j
public class TenantDalJpa extends BaseDalJpa implements TenantDal{
    private static final String TENANT_BLANK = "blankTenantName";
    private static final String DUPLICATED_TENANT = "duplicateTenant";
    private static final String DUPLICATED_TENANT_NAME = "duplicateTenantName";

    private static final String DUPLICATED_TENANT_HOST = "duplicateTenantHost";
    private static final String UPDATE_TENANT_HOST = "updateTenantHost";
    public static final String DUPLICATE_TENANT_IDENTIFER_MAPPING = "duplicatedTenantIdentifierMapping";

    private static final Map<String, String> SORT_COLUMNS;

    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("tenantName", "t.tenantName");
        SORT_COLUMNS.put("tenantId", "t.tenantId");
    }

    EntityManager em;

    @Autowired
    public void setEntity(EntityManager em) {
        this.em = em;
    }

    TenantRepository tenantRepository;

    @Autowired
    public void setTenantRepository(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }


    AuditLogService auditLogDal;

    @Autowired
    public void setAuditLogDal(AuditLogService auditLogDal) {
        this.auditLogDal = auditLogDal;
    }


    DeploymentUtil deploymentUtil;

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        this.deploymentUtil = deploymentUtil;
    }


    private CacheService cacheService;

    @Autowired
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }


    TenantStructureGenerator tenantStructureGenerator;

    @Autowired
    public void setTenantStructureGenerator(@Lazy TenantStructureGenerator tenantStructureGenerator) {
        this.tenantStructureGenerator = tenantStructureGenerator;
    }


    GroupRepository groupRepository;

    @Autowired
    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    OrganizationRepository organizationRepository;

    @Autowired
    public void setOrganizationRepository(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }


    OrganizationMaintenanceDal organizationDal;

    @Autowired
    public void setOrganizationDal(OrganizationMaintenanceDal organizationDal) {
        this.organizationDal = organizationDal;
    }

    EnvironmentUtil environmentUtil = new EnvironmentUtil();


    UserDal userDal;

    @Autowired
    public void setUserDal(UserDal userDal) {
        this.userDal = userDal;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void evictSecondLevelCacheById(Long tenantId){
        em.getEntityManagerFactory().getCache().evict(Tenant.class, tenantId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @TenantCheck()
    public Tenant create(Tenant tenant, AuditLogInfo auditLogInfo) {
        if (tenant == null) {
            throw new IllegalArgumentException();
        }
        validateEntry(tenant);
        validateHostEntry(tenant);
        if (!isTenantIdentifierAssociationValid(tenant.getIdentifiers(), 0L)) {
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATE_TENANT_IDENTIFER_MAPPING, "tenantIdentifier already exists "
                    + "for some other tenant");
            throw errorInfo;
        }
        tenant.setCreatedBy(auditLogInfo.getLoggedInUserId().longValue());
        tenant.setCreatedOn(new Date());
        tenant.setDeleted(false);

        //adding the validation for makerCheckerEnabled and twoFactorEnabled if the value coming is null set the value
        if(tenant.isMakerCheckerEnabled() == null) {
            tenant.setMakerCheckerEnabled(false);
        }

        if(tenant.isTwoFactorAuthEnabled() == null) {
            tenant.setTwoFactorAuthEnabled(false);
        }

//        em.persist(tenant);
        tenantRepository.save(tenant);
        auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), tenant.getTenantName(),
                auditLogInfo.getTarget(), auditLogInfo.getOperation(), getObjectChangeSetLocal(null, tenant));
        cacheService.clearCache(CacheConfig.CLEAR_TENANT_CACHE);//CACHE:: CLEAR
        return tenant;
    }

    private void validateEntry(Tenant tenant) {/*Tenant with same name should not exist */
        if(tenant.getTenantName()==null){
            ErrorInfoException errorInfo = new ErrorInfoException(TENANT_BLANK);
            throw errorInfo;
        }

        Tenant tnt = getTenantByTenantName(tenant.getTenantName());
        if(tnt!=null){
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_TENANT).append("; ");
            sb.append(DUPLICATED_TENANT_NAME).append("=").append(tenant.getTenantName());
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_TENANT, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_TENANT_NAME, tenant.getTenantName());
            log.info("create; tenanterrorInfo={}", errorInfo);
            throw errorInfo;
        }
    }

    private void validateHostEntry(Tenant tenant){
//        Query query = em.createNativeQuery("select t.* from rbac.tenant t where t.tenantType=(select c.codeId from rbac.codes c where c.codeType='TENANT_TYPE' and c.codeValue='Host')"+
//                " and t.tenantSubType=(select c.codeId from rbac.codes c where c.codeType='TENANT_SUBTYPE' and c.codeValue='HostOnly')",Tenant.class);
//        Tenant resultTenant = (Tenant)query.getSingleResult();
        Tenant resultTenant = tenantRepository.tenantByTypeAndSubType();
        if(resultTenant.getTenantType().getCodeId().equals(tenant.getTenantType().getCodeId()))
        {
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_TENANT_HOST).append("; ");
            log.info("validateHostEntry; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_TENANT_HOST, sb.toString());
            log.info("create; tenanthosterrorInfo={}", errorInfo);
            throw errorInfo;
        }
    }


    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean isTenantIdentifierAssociationValid(Set<TenantIdentifier> tenantIdentifiers, Long tenantId) {
        if (tenantIdentifiers != null && !tenantIdentifiers.isEmpty()) {
            for (TenantIdentifier tenantIdentifier : tenantIdentifiers) {
//                TypedQuery<Integer> query = em.createNamedQuery("isTenantIdentifierAssociationValid", Integer.class);
//                query.setParameter(1, tenantId);
//                query.setParameter(2, tenantIdentifier.getTenantIdentifier());
//                Integer result = query.getSingleResult();
                Integer result = tenantRepository.isTenantIdentifierAssociationValid(tenantId, tenantIdentifier.getTenantIdentifier());
                if (tenantRepository.isTenantIdentifierAssociationValid(tenantId,tenantIdentifier.getTenantIdentifier()) != null) {
                    log.debug("isTenantIdentifierAssociationValid; queryResult={}", result);
                    if (result.intValue() != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @TenantCheck()
    public Tenant update(Tenant tenant, AuditLogInfo auditLogInfo) {
        cacheService.clearCache(CacheConfig.CLEAR_TENANT_CACHE);//CACHE:: CLEAR
//        Tenant dbTenant = em.find(Tenant.class, tenant.getTenantId());
        Tenant dbTenant = tenantRepository.findById(tenant.getTenantId()).orElse(null);
        if (dbTenant == null) {
            throw new IllegalArgumentException("tenant does not exist");
        }

        if(!dbTenant.getTenantName().equalsIgnoreCase(tenant.getTenantName())){//if tenant name has changed, then check for uniqueness
            Tenant tnt = getTenantByTenantName(tenant.getTenantName());
            if(tnt!=null && tnt.getTenantId() != dbTenant.getTenantId()){
                StringBuilder sb = new StringBuilder();
                sb.append(DUPLICATED_TENANT).append("; ");
                sb.append(DUPLICATED_TENANT_NAME).append("=").append(tenant.getTenantName());
                log.info("update; {}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_TENANT, sb.toString());
                errorInfo.getParameters().put(DUPLICATED_TENANT_NAME, tenant.getTenantName());
                log.info("update; tenanterrorInfo={}", errorInfo);
                throw errorInfo;
            }
        }

        //RBAC-1178
        Tenant hostTenant = Lookup.getHostTenant();
        if(hostTenant!=null && !tenant.getTenantId().equals(hostTenant.getTenantId()) && hostTenant.getTenantType().getCodeId().equals(tenant.getTenantType().getCodeId())){
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_TENANT_HOST).append("; ");
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_TENANT_HOST, sb.toString());
            log.info("update; tenanthosterrorInfo={}", errorInfo);
            throw errorInfo;
        }

        if (!isTenantIdentifierAssociationValid(tenant.getIdentifiers(), dbTenant.getTenantId())) {
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATE_TENANT_IDENTIFER_MAPPING, "tenantIdentifier already exists "
                    + "for some other tenant");
            throw errorInfo;
        }
        Tenant oldTenant = new Tenant();
        BeanUtils.copyProperties(dbTenant, oldTenant);
        oldTenant.setIdentifiers(new HashSet<TenantIdentifier>());
        if(dbTenant.getIdentifiers()!=null && !dbTenant.getIdentifiers().isEmpty()){
            for(TenantIdentifier ti: dbTenant.getIdentifiers()){
                oldTenant.getIdentifiers().add(ti);
            }
        }
        dbTenant.setAccountManager(tenant.getAccountManager());
        dbTenant.setBillDate(tenant.getBillDate());
        dbTenant.setStartDate(tenant.getStartDate());
        dbTenant.setTenantName(tenant.getTenantName());
        if(dbTenant.getTenantType().getCodeValue().equalsIgnoreCase(RBACUtil.HOST_TENANT_TYPE_CODE_VALUE) && (dbTenant.getTenantType().getCodeId()!=tenant.getTenantType().getCodeId())){
            StringBuilder sb = new StringBuilder();
            sb.append(UPDATE_TENANT_HOST).append("; ");
            log.info("update Tenant Type and Sub Type; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(UPDATE_TENANT_HOST, sb.toString());
            log.info("update Tenant Type and Sub Type; tenanthostupdateerrorInfo={}", errorInfo);
            throw errorInfo;
        }

        if (dbTenant.getIdentifiers() != null) {
            dbTenant.getIdentifiers().clear();
            if (tenant.getIdentifiers() != null) {
                dbTenant.getIdentifiers().addAll(tenant.getIdentifiers());
            }
        } else {
            dbTenant.setIdentifiers(tenant.getIdentifiers());
        }

        dbTenant.setTenantSubType(tenant.getTenantSubType());
        dbTenant.setTenantType(tenant.getTenantType());
        dbTenant.setRemarks(tenant.getRemarks());
        dbTenant.setTenantURL(tenant.getTenantURL());
        dbTenant.setUpdatedBy(auditLogInfo.getLoggedInUserId().longValue());
        dbTenant.setUpdatedOn(new Date());
        dbTenant.setDeleted(false);
        dbTenant.setTwoFactorAuthEnabled(tenant.isTwoFactorAuthEnabled()); //RBAC-1562
        dbTenant.setMakerCheckerEnabled(tenant.isMakerCheckerEnabled()); // Added By Fazia
        tenantStructureGenerator.handleTenantUpdation(dbTenant, oldTenant, auditLogInfo);
        auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), oldTenant.getTenantName(),
                auditLogInfo.getTarget(), auditLogInfo.getOperation(), getObjectChangeSetLocal(oldTenant, dbTenant));
//        Tenant retTenant = em.merge(dbTenant);
        Tenant retTenant = tenantRepository.save(dbTenant);
        if (tenant.getIdentifiers()!= null && !tenant.getIdentifiers().isEmpty()) {
            evictSecondLevelCacheById(tenant.getTenantId());
        }
        Lookup.updateTenantLookupTable(retTenant);
        return retTenant;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long tenantId, AuditLogInfo auditLogInfo) {
        cacheService.clearCache(CacheConfig.CLEAR_TENANT_CACHE);//CACHE:: CLEAR
//        Tenant dbTenant = em.find(Tenant.class, tenantId);
        Tenant dbTenant = tenantRepository.findById(tenantId).orElse(null);
        if (dbTenant != null) {
//            TypedQuery<Long> queryGroupPresence = em.createQuery("select count(1) from Group g where g.tenantId = :tenantId", Long.class);
//            queryGroupPresence.setParameter("tenantId", tenantId);
//            Long groupCount = queryGroupPresence.getSingleResult();
            Long groupCount = groupRepository.getGroupByTenantId(tenantId);
            if(groupCount!=null && groupCount > 0){
                throw new ErrorInfoException("tenantGroupAssociationFound", "Tenant can't be deleted, associated with group");
            }

//            TypedQuery<Long> queryOrganizationPresence = em.createQuery("select count(1) from Organization o where o.tenantId = :tenantId", Long.class);
//            queryOrganizationPresence.setParameter("tenantId", tenantId);
//            Long organizationCount = queryOrganizationPresence.getSingleResult();
            Long organizationCount = organizationRepository.getOrganizationByTenantId(tenantId);
            if(organizationCount!=null && organizationCount > 1){
                throw new ErrorInfoException("tenantOrganizationAssociationFound", "Tenant can't be deleted, associated with Organization");
            }

            //find the organization by tenantId
//            MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
            MultivaluedHashMap<String, String> queryParams = new MultivaluedHashMap<>();
            queryParams.add("tenantId", tenantId.toString());
            Options options = new Options(new OptionFilter(queryParams));

            //delete remaining organizations
            List<Organization> organizationsList = organizationDal.getList(options);
            if(organizationsList!=null && !organizationsList.isEmpty()){
                for(Organization org: organizationsList){
                    organizationDal.deleteById(org.getOrganizationId(), auditLogInfo.getLoggedInUserId().intValue());
                }
            }
            String updatedDeletedTenantName = dbTenant.getTenantName()+"-"+dbTenant.getTenantId();
            if(updatedDeletedTenantName.length() > 1000)
                updatedDeletedTenantName = updatedDeletedTenantName.substring(0, 1000);
//            TypedQuery<Tenant> query = em.createNamedQuery("deleteTenantById", Tenant.class);
//            query.setParameter("tenantId", tenantId);
//            query.setParameter("userId", auditLogInfo.getLoggedInUserId().longValue());
//            query.setParameter("currDateTime", DateTime.now().toDate());
//
//            query.setParameter("tenantName", updatedDeletedTenantName);
//            query.executeUpdate();
            tenantRepository.deleteTenantById(tenantId,updatedDeletedTenantName,auditLogInfo.getLoggedInUserId().longValue(),DateTime.now().toDate());

            //em.remove(dbTenant);
        }
        auditLogDal.createSyncLog(auditLogInfo.getLoggedInUserId(), dbTenant.getTenantName(),
                auditLogInfo.getTarget(), auditLogInfo.getOperation(), getObjectChangeSetLocal(dbTenant, null));
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.TENANT_LIST_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<Tenant> list(Options options) {
        if(!environmentUtil.isMultiTenantEnvironment()){
            OptionFilter filter = new OptionFilter();
            filter.addFilter("tenantType", RBACUtil.HOST_TENANT_TYPE_CODE_VALUE);
            options = new Options(filter);
        }
        Filters filters = prepareFilters(options);
        return filters.getList(em, Tenant.class, "select t from Tenant t",
                options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.TENANT_ID_LIST_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<Long> getTenantIds(Options options) {
        if(!environmentUtil.isMultiTenantEnvironment()){
            return Collections.singletonList(Lookup.getHostTenant().getTenantId());
        }
        Filters filters = prepareFilters(options);
        return filters.getList(em, Long.class, "select t.tenantId from Tenant t",
                options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public long count(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getCount(em, "select count(t) from Tenant t");
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.TENANT_BY_ID_CACHE, unless="#result == null")
    public Tenant getById(Long tenantId) {
        return em.find(Tenant.class, tenantId);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean checkEntityPermission(long tenantId, Options options) {
        Filters filters = prepareFilters(options);
        filters.addCondition(" t.tenantId = "+ tenantId + " ");
        if(filters.getCount(em, "select count(t) from Tenant t")==1){
            return true;
        }
        return false;
    }

    @Override
    @Cacheable(value = CacheConfig.TENANT_SEARCH_LIST_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<Tenant> searchList(Options options) {
        Filters filters = prepareFilters(options);
        filters.addCondition(" ( lower(t.tenantName) like :q  ) ");

        filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase()));

        return filters.getList(em, Tenant.class, "select distinct t from Tenant t ", options, SORT_COLUMNS);

    }

    @Override
    public long getSearchCount(Options options) {

        Filters filters = prepareFilters(options);
        filters.addCondition(" ( lower(t.tenantName) like :q  ) ");

        filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase()));

        return filters.getCount(em, "select count(distinct t) from Tenant t ");

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.TENANT_ID_NAMES_LIST_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<Map<String,Object>> getTenantIdNames(Options options) {
        List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
        if(!environmentUtil.isMultiTenantEnvironment()){
            OptionFilter filter = new OptionFilter();
            filter.addFilter("tenantType", RBACUtil.HOST_TENANT_TYPE_CODE_VALUE);
            options = new Options(filter);
        }
        Filters filters = prepareFilters(options);
        // add default sort by name
        OptionSort optionSort = options != null ? options
                .getOption(OptionSort.class) : null;
        if (optionSort == null) {
            optionSort = new OptionSort(new LinkedList<String>());
        }
        if(optionSort.getSortProperties().isEmpty()){
            optionSort.getSortProperties().add("tenantName");
        }
        options = new Options(optionSort, options != null ? options
                .getOption(OptionPage.class) : null, options != null ? options
                .getOption(OptionFilter.class) : null);
        List<Object[]> result = filters.getList(em, Object[].class, "select t.tenantId, t.tenantName from Tenant t", options, SORT_COLUMNS);
        if(result!=null && !result.isEmpty()){
            for(Object[] obj:result){
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("tenantId", obj[0]);
                temp.put("tenantName", obj[1].toString());
                returnObj.add(temp);
            }
        }
        return returnObj;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.TENANT_ID_NAMES_SEARCH_LIST_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public List<Map<String, Object>> searchTenantIdNames(Options options) {
        List<Map<String, Object>> returnObj = new LinkedList<Map<String, Object>>();
        if (!environmentUtil.isMultiTenantEnvironment()) {
            OptionFilter filter = new OptionFilter();
            filter.addFilter("tenantType", RBACUtil.HOST_TENANT_TYPE_CODE_VALUE);
            options = new Options(filter);
        }
        Filters filters = prepareFilters(options);
        String searchParam = SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM);
        if (searchParam != null && !searchParam.isEmpty()) {
            filters.addCondition(" ( lower(t.tenantName) like :q  ) ");
            filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils
                    .wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));
        }
        // add default sort by name
        OptionSort optionSort = options != null ? options.getOption(OptionSort.class) : null;
        if (optionSort == null) {
            optionSort = new OptionSort(new LinkedList<String>());
        }
        if (optionSort.getSortProperties().isEmpty()) {
            optionSort.getSortProperties().add("tenantName");
        }
        options = new Options(optionSort, options != null ? options.getOption(OptionPage.class) : null,
                options != null ? options.getOption(OptionFilter.class) : null);
        List<Object[]> result = filters.getList(em, Object[].class, "select t.tenantId, t.tenantName from Tenant t",
                options, SORT_COLUMNS);
        if (result != null && !result.isEmpty()) {
            for (Object[] obj : result) {
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("tenantId", obj[0]);
                temp.put("tenantName", obj[1].toString());
                returnObj.add(temp);
            }
        }
        return returnObj;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public long searchTenantIdNamesCount(Options options) {

        Filters filters = prepareFilters(options);
        String searchParam = SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM);
        if (searchParam != null && !searchParam.isEmpty()) {
            filters.addCondition(" ( lower(t.tenantName) like :q  ) ");

            filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils
                    .wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));
        }

        return filters.getCount(em, "select count(distinct t) from Tenant t ");

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.TENANT_BY_NAME_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR, unless="#result == null")
    public Tenant getTenantByTenantName(String tenantName){
//        try {
//            TypedQuery<Tenant> query = em.createNamedQuery("getTenantByTenantName", Tenant.class);
//            query.setParameter(
//                    "tName", tenantName);
//            return query.getSingleResult();
            return tenantRepository.getTenantByTenantName(tenantName).orElse(null);
//        } catch (NoResultException e) {
//            return null;
//        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Tenant getHostTenant() {
//        TypedQuery<Tenant> query = em.createNamedQuery("getHostTenant", Tenant.class);
//        query.setParameter("tenantType", RBACUtil.HOST_TENANT_TYPE_CODE_VALUE);
//        return query.getResultList().get(0);
         return tenantRepository.getHostTenant(RBACUtil.HOST_TENANT_TYPE_CODE_VALUE).orElse(null);

    }

    private Map<String, String> getObjectChangeSetLocal(Tenant oldTenant, Tenant newTenant) {
        AuditLogHelperUtil logHelperUtil =  new AuditLogHelperUtil();
        logHelperUtil.putToObjectChangeSet(OBJECTNAME, newTenant!=null?newTenant.getTenantName():oldTenant.getTenantName());
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_TENANTNAME, (newTenant!=null)?newTenant.getTenantName():null, (oldTenant!=null)?oldTenant.getTenantName(): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_TENANTTYPE, (newTenant!=null)? Lookup.getCodeValueById(newTenant.getTenantType().getCodeId()):null, (oldTenant!=null)?Lookup.getCodeValueById(oldTenant.getTenantType().getCodeId()): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_TENANTSUBTYPE, (newTenant!=null)?Lookup.getCodeValueById(newTenant.getTenantSubType().getCodeId()):null, (oldTenant!=null)?Lookup.getCodeValueById(oldTenant.getTenantSubType().getCodeId()): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_TENANTURL, (newTenant!=null)?newTenant.getTenantURL():null, (oldTenant!=null)?oldTenant.getTenantURL(): null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_TENANTREMARKS, (newTenant!=null)?newTenant.getRemarks():null, (oldTenant!=null)?oldTenant.getRemarks(): null, null, null);
        if(deploymentUtil.isEnableTwoFactorAuth() && ((oldTenant!=null && oldTenant.isTwoFactorAuthEnabled() != null && oldTenant.isTwoFactorAuthEnabled())
                || (newTenant!=null && newTenant.isTwoFactorAuthEnabled() != null && newTenant.isTwoFactorAuthEnabled())))
            logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_ENABLE2STEPVERIFICATION, (newTenant!=null)?newTenant.isTwoFactorAuthEnabled():null, (oldTenant!=null)?oldTenant.isTwoFactorAuthEnabled(): null, null, null);
        // RBAC-1983 Start
        if(deploymentUtil.getIsMakercheckerActivated() && ((oldTenant!=null && oldTenant.isMakerCheckerEnabled() != null && oldTenant.isMakerCheckerEnabled())
                || (newTenant!=null && newTenant.isMakerCheckerEnabled() != null && newTenant.isMakerCheckerEnabled())))
            logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_ENABLEMAKERCHECKER, (newTenant!=null)?newTenant.isMakerCheckerEnabled():null, (oldTenant!=null)?oldTenant.isMakerCheckerEnabled(): null, null, null);
        // RBAC-1983 end

        if( (newTenant!=null && newTenant.getIdentifiers()!=null && !newTenant.getIdentifiers().isEmpty()) ||
                (oldTenant!=null && oldTenant.getIdentifiers()!=null && !oldTenant.getIdentifiers().isEmpty()) ){
            logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_TENANTIDENTIFIERS, (newTenant!=null)?newTenant.getIdentifiers():null, (oldTenant!=null)?oldTenant.getIdentifiers(): null, null, null);
        }
        return logHelperUtil.getObjectChangeSet();
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options
                .getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null
                : optionFilter.getFilters();
        if (filters != null) {
            String userName = filters.get("userName");
            if (userName != null && userName.length() > 0) {
                String scope = userDal.getUserTenantScope(userName);
                if(scope!=null && !scope.isEmpty()){
                    filters.put(RBACUtil.TENANT_SCOPE_QUERY, scope);
                }
            }

            String tenantName = filters.get("tenantName");
            if (tenantName != null && tenantName.length() > 0) {
                result.addCondition("t.tenantName like '%"+tenantName+"%'");
//				result.addParameter("tenantName", tenantName);
            }
            String tenantScopeQuery = filters.get(RBACUtil.TENANT_SCOPE_QUERY);
            if (tenantScopeQuery != null && tenantScopeQuery.length() > 1) {
                result.addCondition("(" + tenantScopeQuery + ")");
            }

            String codeValue = filters.get("tenantType");
            if (codeValue != null && codeValue.length() > 0) {
                result.addCondition("t.tenantType.codeValue = :codeValue");
                result.addParameter("codeValue", codeValue);
            }

        }
        return result;
    }
}
