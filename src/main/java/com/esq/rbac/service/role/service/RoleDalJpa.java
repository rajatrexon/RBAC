package com.esq.rbac.service.role.service;

import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.config.CacheConfig;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.group.repository.GroupRepository;
import com.esq.rbac.service.jointables.rolepermission.domain.RolePermission;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.role.domain.Role;
import com.esq.rbac.service.role.operationsubdomain.repository.OperationRepository;
import com.esq.rbac.service.role.repository.RoleRepository;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.scope.repository.ScopeRepository;
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import com.esq.rbac.service.scope.scopedefinition.repository.ScopeDefinitionRepository;
import com.esq.rbac.service.scope.service.ScopeDal;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.CacheService;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.TypedQuery;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Service
public class RoleDalJpa extends BaseDalJpa implements RoleDal {


    public static final String DUPLICATED_ROLE = "duplicatedRole";
    public static final String DUPLICATED_NAME = "duplicatedName";
    public static final String INVALID_APP = "invalidApp";
    public static final String INVALID_APP_OPERATION = "invalidAppOperation";
    public static final String GROUP_ROLE_MAPPING_FOUND = "groupRoleMappingFound";
    private static final Logger log = LoggerFactory.getLogger(RoleDalJpa.class);
    private static final Map<String, String> SORT_COLUMNS;
    private static final String ROLE_PERMISSION_FILTER_PREFIX = "roleScopePermissionFilter.";
    private static Map<Integer, String> filterMappings = new LinkedHashMap<Integer, String>();
    private ScopeDal scopeDal;
    private UserDal userDal;
    private CacheService cacheService;


    RoleRepository roleRepository;

    @Autowired
    public void setRoleRepository(RoleRepository role){
        this.roleRepository = role;
    }




    GroupRepository groupRepository;
    @Autowired
    public void setGroupRepository(GroupRepository groupRepository){
        this.groupRepository = groupRepository;
    }


    ScopeRepository scopeRepository;
    @Autowired
    public void setScopeRepository(ScopeRepository scopeRepository){
        this.scopeRepository = scopeRepository;
    }



    OperationRepository operationRepository;
    @Autowired
    public void setOperationRepository(OperationRepository operationRepository){
        this.operationRepository = operationRepository;
    }


    ScopeDefinitionRepository scopeDefinitionRepository;

    @Autowired
    public void setScopeDefinitionRepository(ScopeDefinitionRepository scopeDefinitionRepository){
        this.scopeDefinitionRepository = scopeDefinitionRepository;
    }


    @Autowired
    public void setScopeDal(ScopeDal scopeDal){
        this.scopeDal = scopeDal;
    }

    @Autowired
    public void setUserDal(@Lazy UserDal userDal) {
        this.userDal = userDal;
    }

    @Autowired
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("name", "r.name");
    }


    public void setPropertyConfiguration(Configuration configuration) {
        /*
         * log.trace("setPropertyConfiguration; configuration={};", configuration);
         * Iterator<String> keysItr = configuration.getKeys(); while(keysItr.hasNext()){
         * String key = keysItr.next(); if (key.startsWith(PROP_LDAP_USER_MAPPING)) {
         * userFieldMappings.put(key.substring(9, key.length()), configuration
         * .getString(key)); } } PROP_LDAP_USERNAME_MAPPING =
         * configuration.getString(PROP_LDAP_USER_MAPPING+"userName"); String[]
         * testFields =
         * configuration.getString(PROP_LDAP_TEST_MAPPING).toLowerCase().split(",");
         * ldapTestFields = Arrays.asList(testFields);
         */
        Properties properties = new Properties();
        String propsUrl = System.getProperty("defaultProps");
        try {
            properties.load(new FileInputStream(new File(propsUrl)));
            for (Object key : properties.keySet()) {
                if (((String) key).startsWith(ROLE_PERMISSION_FILTER_PREFIX)) {

                    filterMappings.put(Integer.parseInt(((String) key).substring(26, ((String) key).length())),
                            properties.getProperty((String) key));
                }
            }

            log.info("mappings ={}", filterMappings);
        } catch (Exception e) {
            log.error("static; Exception={}", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Role create(Role role, int userId) {
        if (isRoleNameDuplicate(role.getApplicationId(), role.getName(), role.getRoleId()) == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_ROLE).append("; ");
            sb.append(DUPLICATED_NAME).append("=").append(role.getName());
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_ROLE, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_NAME, role.getName());
            throw errorInfo;
        }
        role.setCreatedBy(userId);
//        role.setCreatedOn(DateTime.now().toDate());
        role.setCreatedOn(new Date());
//        em.persist(role);
//        roleRepository.save(role);
         return roleRepository.save(role);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Role update(Role role, int userId) {
        cacheService.clearCache(CacheConfig.CLEAR_ALL_CACHE);//CACHE:: CLEAR
        if (role.getRoleId() == null) {
            throw new IllegalArgumentException("roleId missing");
        }
//        Role existingRole = em.find(Role.class, role.getRoleId());
        Role existingRole = roleRepository.findById(role.getRoleId()).orElse(null);
        if (existingRole == null) {
            throw new IllegalArgumentException("roleId invalid");
        }

        setObjectChangeSet(existingRole, role, false, false, null);

        if (isRoleNameDuplicate(role.getApplicationId(), role.getName(), role.getRoleId()) == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_ROLE).append("; ");
            sb.append(DUPLICATED_NAME).append("=").append(role.getName());
            log.info("update; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_ROLE, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_NAME, role.getName());
            throw errorInfo;
        }

        existingRole.setDescription(role.getDescription());
        existingRole.setLabels(role.getLabels());
        existingRole.setName(role.getName());
        existingRole.setUpdatedBy(userId);
//        existingRole.setUpdatedOn(DateTime.now().toDate());
        existingRole.setUpdatedOn(new Date());
//        Role rl = em.merge(existingRole);
        Role rl = roleRepository.save(existingRole);
        return rl;
    }



    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int isRoleNameDuplicate(Integer applicationId, String name, Integer roleId) {

//        TypedQuery<Role> query = em.createNamedQuery("getRoleByNameAndApplicationId", Role.class);
//        query.setParameter("name", name.toLowerCase());
//        query.setParameter("applicationId", applicationId);
//        List<Role> fetchedRoleIds = query.getResultList();

        List<Role> fetchedRoleIds = roleRepository.findByNameAndApplicationId(name, applicationId);
        if (fetchedRoleIds != null && !fetchedRoleIds.isEmpty()) {
            if (roleId != null && roleId.intValue() > 0) {
                for (Iterator<Role> iterator = fetchedRoleIds.iterator(); iterator.hasNext();) {
                    Role fetchedRole = (Role) iterator.next();
                    if (fetchedRole != null && roleId.equals(fetchedRole.getRoleId())) {
                        // updating same role
                        return 0;
                    }
                }
            }
            // duplicate
            return 1;
        }
        return 0;
    }




    private void setObjectChangeSet(Role oldRole, Role newRole, boolean permissions, boolean isDefinitionDeleted ,Map<Integer,List<Integer>> groupScopeMap) {
        clearObjectChangeSet();

        putToObjectChangeSet(OBJECTCHANGES_ROLEID, newRole.getRoleId().toString());
        putToObjectChangeSet(OBJECTNAME, oldRole.getName());

        checkPutToObjectChangeSet(OBJECTCHANGES_ROLENAME, newRole.getName(), oldRole.getName(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_DESCRIPTION, newRole.getDescription(), oldRole.getDescription(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_LABELS, (!newRole.getLabels().isEmpty()?newRole.getLabels():""), (!oldRole.getLabels().isEmpty()?oldRole.getLabels():""), null, null);
        if (permissions) {
            checkOperationIdsPutToObjectChangeSet(newRole.getOperationIds(), oldRole.getOperationIds());
        }
        if(isDefinitionDeleted){

            checkRoleScopePutToObjectChangeSet(groupScopeMap);
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Role updatePermissions(Role role) {
        cacheService.clearCache(CacheConfig.CLEAR_ALL_CACHE);//CACHE:: CLEAR
        if (role.getRoleId() == null) {
            throw new IllegalArgumentException("roleId missing");
        }
//        Role existingRole = em.find(Role.class, role.getRoleId());

        Optional<Role> existingRole = roleRepository.findById(role.getRoleId());

        if (existingRole.isEmpty()) {
            throw new IllegalArgumentException("roleId invalid");
        }

       List<RolePermission> permissions = getRoleList(role.getRoleId());
        Set<Integer> getPermits = new HashSet<>();
        for(RolePermission operations : permissions){
            getPermits.add(operations.getOperationId());
        }

        Role tempRole = new Role();
        tempRole.setApplicationId(existingRole.get().getApplicationId());
        tempRole.setCreatedBy(existingRole.get().getCreatedBy());
        tempRole.setCreatedOn(existingRole.get().getCreatedOn());
        tempRole.setDescription(existingRole.get().getDescription());
        tempRole.setLabels(existingRole.get().getLabels());
        tempRole.setName(existingRole.get().getName());
        tempRole.setOperationIds(getPermits);
        tempRole.setRoleId(existingRole.get().getRoleId());
        tempRole.setUpdatedBy(existingRole.get().getUpdatedBy());
        tempRole.setUpdatedOn(existingRole.get().getUpdatedOn());


        if (!role.getApplicationId().equals(existingRole.get().getApplicationId())) {
            ErrorInfoException errorInfo = new ErrorInfoException(INVALID_APP);
            throw errorInfo;
        }
        if (getPermits != null && !getPermits.isEmpty()) {
            if (!areOperationsInApplication(getPermits, existingRole.get().getApplicationId())) {
                ErrorInfoException errorInfo = new ErrorInfoException(INVALID_APP_OPERATION);
                throw errorInfo;
            }
        }
        existingRole.get().setOperationIds(getPermits);
//        Role rl = em.merge(existingRole);

        Role existingRole1 = existingRole.orElse(null);
        Role rl = roleRepository.save(existingRole1);

        Map<Integer,List<Integer>> groupScopeMap = getScopeDefinitionForRole(role.getRoleId());

        int count =0;
        boolean isDefinitionDeleted = true;

        for (Map.Entry<Integer,List<Integer>> entry : groupScopeMap.entrySet()) {
            if(entry.getValue().isEmpty()){
                count++;
            }
        }

        if(count == groupScopeMap.size()){
            isDefinitionDeleted = false;
            groupScopeMap = null;
        }

        setObjectChangeSet(tempRole, role, true, isDefinitionDeleted, groupScopeMap);

        deleteScopeDefintionForRole(role.getRoleId());
        em.getEntityManagerFactory().getCache().evict(Group.class);
        em.getEntityManagerFactory().getCache().evict(ScopeDefinition.class);

        return rl;
    }


    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Role> getById(int roleId) {

//        return em.find(Role.class, roleId);
        return roleRepository.findById(roleId);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(int roleId) {
        //picking groupIds for future cascade deletion/showing group name in error message on UI
//        TypedQuery<Integer> query = em.createNamedQuery("getGroupIdsFromGroupRole", Integer.class);
//        query.setParameter(1, roleId);
//        List<Integer> groupIds = query.getResultList();

        List<Integer> groupIds = groupRepository.getGroupIdsFromGroupRole(roleId);

        if(groupIds!=null && !groupIds.isEmpty()){
            ErrorInfoException errorInfo = new ErrorInfoException(GROUP_ROLE_MAPPING_FOUND);
            throw errorInfo;
        }
//        Role role = em.find(Role.class, roleId);
//        em.remove(role);

        roleRepository.deleteById(roleId);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteByName(String name) {
//        Query query = em.createNamedQuery("deleteRoleByName");
//        query.setParameter("name", name);
//        query.executeUpdate();

        roleRepository.deleteRoleByName(name);


    }



    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<String> getAllNames() {
//        TypedQuery<String> query = em.createNamedQuery("getAllRoleNames", String.class);
//        return query.getResultList();

        return  roleRepository.getAllRoleNames();

    }

  @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<String> getAllNames(int applicationId) {
//        TypedQuery<String> query = em.createNamedQuery("getAllRoleNamesByApplicationId", String.class);
//        query.setParameter("applicationId", applicationId);
//        return query.getResultList();

        return roleRepository.getAllRoleNamesByApplicationId(applicationId);
    }


    @Override
    public List<Integer> getScopeIds(int roleId) {
//        TypedQuery<Integer> query = em.createNamedQuery("getRoleScopeIds", Integer.class);
//        query.setParameter(1, roleId);
//        return query.getResultList();

        return roleRepository.getRoleScopeIds(roleId);
    }


    @Override
    public List<Scope> getGroupRoleScopeIds( List<Integer> roleIds) {
//        TypedQuery<Scope> query = em.createNamedQuery("getGroupRoleScopeIds", Scope.class);
//        query.setParameter("roleIds", roleIds);
//        return query.getResultList();

        return groupRepository.getGroupRoleScopeIds(roleIds);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Role> getList(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getList(em, Role.class, "select r from Role r", options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int getCount(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getCount(em, "select count(r) from Role r");
    }



    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Role> getRolesNotAssignedToAnyGroup(Options options) {
        /*TypedQuery<Role> query = em.createNamedQuery("getRolesNotAssignedToAnyGroup", Role.class);
        return query.getResultList();*/
        Filters filters = prepareFilters(options);
        filters.addCondition("r.roleId not in (select distinct gr.roleId from GroupRole gr)");
        return filters
                .getList(
                        em,
                        Role.class,
                        "select distinct r from Role r",
                        options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Role> getRolesAssignedToOtherGroups(int groupId, Options options) {
        /*TypedQuery<Role> query = em.createNamedQuery("getRolesAssignedToOtherGroups", Role.class);
        query.setParameter(1, groupId);
        return query.getResultList();*/
        Filters filters = prepareFilters(options);
        filters.addParameter("groupId", groupId);
        filters.addCondition("r.roleId in (select distinct gr.roleId from GroupRole gr where gr.groupId != :groupId) and r.roleId not in (select distinct gr.roleId from GroupRole gr where gr.groupId = :groupId)");
        return filters
                .getList(
                        em,
                        Role.class,
                        "select distinct r from Role r",
                        options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean areOperationsInApplication(Set<Integer> operationIds, Integer applicationId) {
//        TypedQuery<Long> query = em.createNamedQuery("areOperationsInApplication", Long.class);
//        query.setParameter("applicationId", applicationId);
//        query.setParameter("operationIds", operationIds);
//        Long result = query.getSingleResult();

        Long result = operationRepository.areOperationsInApplication(applicationId,operationIds);
        if (result != null) {
            log.debug("areOperationsInApplication; queryResult={}", result);
            if (result.intValue() == operationIds.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String,Object>> getRoleIdNames(Options options) {
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
        List<Object[]> result = filters.getList(em, Object[].class, "select r.roleId, r.name from Role r", options, SORT_COLUMNS);
        if(result!=null && !result.isEmpty()){
            for(Object[] obj:result){
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("roleId", obj[0]);
                temp.put("roleName", obj[1].toString());
                returnObj.add(temp);
            }
        }
        return returnObj;
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        if (filters != null) {

            String name = filters.get("name");
            if (name != null && name.length() > 0) {
                result.addCondition("r.name = :name");
                result.addParameter("name", name);
            }

            String label = filters.get("label");
            if (label != null && label.length() > 0) {
                result.addCondition(":label member of r.labels");
                result.addParameter("label", label);
            }

            String applicationId = filters.get("applicationId");
            if (applicationId != null && applicationId.length() > 0) {
                result.addCondition("r.applicationId=:applicationId");
                result.addParameter("applicationId", Integer.valueOf(applicationId));
            }

            String scopeQuery = filters.get(RBACUtil.ROLE_SCOPE_QUERY);
            if (scopeQuery != null && scopeQuery.length() > 1) {
                result.addCondition("("+scopeQuery+")");
            }

        }
        return result;
    }

    //@SuppressWarnings("unchecked")
    @Override
    public List<Role> searchList(Options options) {

        String applicationId = SearchUtils.getSearchParam(options, "applicationId");

        Filters filters = prepareFilters(options);

        filters.addCondition(" ( lower(r.name) like :q or lower(r.description) like :q ) and r.applicationId = :applicationId");

        filters.addParameter(
                SearchUtils.SEARCH_PARAM,
                SearchUtils.wildcarded(SearchUtils.getSearchParam(options,
                        SearchUtils.SEARCH_PARAM).toLowerCase()));

        filters.addParameter("applicationId", Integer.parseInt(applicationId));

        return filters.getList(em, Role.class, "select distinct r from Role r",
                options, SORT_COLUMNS);

        /* String q = SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase();


        String appId = SearchUtils.getSearchParam(options, "applicationId");
        StringBuilder sb = new StringBuilder();
        sb.append(SEARCH_ROLES);
        sb.append(SearchUtils.getOrderByParam(options, SORT_COLUMNS));
        Query query = em.createQuery(sb.toString(), Role.class);
        String wildcardedq = SearchUtils.wildcarded(q);
        query.setParameter(1, wildcardedq);
        query.setParameter(2, wildcardedq);
        query.setParameter(3, Integer.parseInt(appId));
        OptionPage optionPage = options != null ? options
                .getOption(OptionPage.class) : null;
        if (optionPage != null) {
            query.setFirstResult(optionPage.getFirstResult());
            query.setMaxResults(optionPage.getMaxResults());
        }
        List<Role> roles = query.getResultList();
        return roles; */
    }

    @Override
    public int getSearchCount(Options options) {

        String applicationId = SearchUtils.getSearchParam(options, "applicationId");

        Filters filters = prepareFilters(options);

        filters.addCondition(" ( lower(r.name) like :q or lower(r.description) like :q ) and r.applicationId = :applicationId");

        filters.addParameter(
                SearchUtils.SEARCH_PARAM,
                SearchUtils.wildcarded(SearchUtils.getSearchParam(options,
                        SearchUtils.SEARCH_PARAM).toLowerCase()));

        filters.addParameter("applicationId", Integer.parseInt(applicationId));

        return filters.getCount(em, "select count(distinct r) from Role r");

        /*String q = SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase();
        String appId = SearchUtils.getSearchParam(options, "applicationId");
        StringBuilder sb = new StringBuilder();
        sb.append(COUNT_ROLES);
        Query query = em.createQuery(sb.toString());
        String wildcardedq = SearchUtils.wildcarded(q);
        query.setParameter(1, wildcardedq);
        query.setParameter(2, wildcardedq);
        query.setParameter(3, Integer.parseInt(appId));
        return ((Number) query.getSingleResult()).intValue();*/
    }



    @Override
    public List<Scope> getGroupRoleScopeIds(Map<String, List<Integer>> roleIdsList, boolean includeDefault,String loggedInUser) {
        List<Scope> result = null;
        List<Integer> roleIds = new ArrayList<Integer>();
        if (roleIdsList != null && !roleIdsList.isEmpty()) {
            roleIds = roleIdsList.get("roleIds");
        }

        if(!roleIds.isEmpty())
        {
            result = getGroupRoleScopeIds(roleIds);
        }
        if(includeDefault){
            if(result==null){
                result = new LinkedList<Scope>();
            }
            result.addAll(scopeDal.getListGlobal());
        }

        User user = userDal.getByUserName(loggedInUser);
        Long tenantId = Lookup.getTenantIdByOrganizationId(user.getOrganizationId());
        log.info("{} is Host? = {}",loggedInUser, Lookup.getTenantIsHostById(tenantId));
        if(!Lookup.getTenantIsHostById(tenantId) && filterMappings != null && !filterMappings.isEmpty() && !roleIds.isEmpty()) {

            String filterredRoleIds = "";
            String finalQuery = "";
            String query = " UNION " +" select distinct s.scopeId from Scope s" +
                    " join OperationScope os on (os.scopeId = s.scopeId)" +
                    " join RolePermission rp on (rp.operationId = os.operationId)" +
                    " join GroupRole gr on(rp.roleId=gr.roleId) ";

            for (Integer roleId: roleIds) {
                if(filterMappings.containsKey(roleId))
                {
                    finalQuery += query + " where gr.roleId = "+roleId+" and "+filterMappings.get(roleId);
                }
                else
                    filterredRoleIds += ","+roleId;
            }

            if(filterredRoleIds != null && !filterredRoleIds.isEmpty()){
                filterredRoleIds = filterredRoleIds.substring(1);
                finalQuery  += query + " where gr.roleId IN ("+filterredRoleIds+")";
            }

            List<Integer> scopeIds = new ArrayList<Integer>();
            if(finalQuery != null && !finalQuery.isEmpty()) {
                finalQuery = finalQuery.substring(6);
                Filters filters = new Filters();
                scopeIds = filters.getList(em, Integer.class,finalQuery, null,null);
            }

            List<Scope> resultFiltered = new ArrayList<Scope>();
            for(Scope res: result) {
                if(scopeIds.contains(res.getScopeId()))
                    res.setDisplayState("block");
                else
                    res.setDisplayState("none");
                resultFiltered.add(res);
            }

            if(resultFiltered != null && !resultFiltered.isEmpty()) {
                result.clear();
                result.addAll(resultFiltered);
            }
        }
        return result;
    }

    @Override
    public List<ScopeDefinition> getRoleScopeDefinitions(List<Map<String, Integer>> roleGroupIdList) {
        // TODO Auto-generated method stub
        Integer roleId, groupId;
        List<ScopeDefinition> result = null;
        if(roleGroupIdList!=null && !roleGroupIdList.isEmpty()){
            for(Map<String, Integer> obj: roleGroupIdList){
                roleId = obj.get("roleId");
                groupId = obj.get("groupId");
                if(roleId!=null && groupId !=null)
                {
                    result = getRoleScopeDefinitions(roleId, groupId);
                }
            }
        }
        return result;
    }

    @Override
    public List<ScopeDefinition> getRoleScopeDefinitions( Integer roleId,Integer groupId) {
//        TypedQuery<ScopeDefinition> query = em.createNamedQuery("getRoleScopeDefinitions", ScopeDefinition.class);
//        query.setParameter("roleId", roleId);
//        query.setParameter("groupId", groupId);
//        return query.getResultList();

        return scopeDefinitionRepository.getRoleScopeDefinitions(roleId,groupId);
    }

    @Override
    public boolean getRoleTransition(List<Map<String, List<Integer>>> roleList) {
        // TODO Auto-generated method stub
        List<Integer> newRoleIds = null;
        List<Integer> existingRoleIds = null;
        boolean result = true ;
        if(roleList!=null && !roleList.isEmpty()){
            for(Map<String, List<Integer>> obj: roleList){
                newRoleIds = obj.get("newRoleIds");
                existingRoleIds = obj.get("existingRoleIds");
                if(newRoleIds!=null && existingRoleIds !=null && !newRoleIds.isEmpty() && !existingRoleIds.isEmpty())
                {
                    result = getIsRoleTransitionAllowed(newRoleIds,existingRoleIds);
                }
            }
        }
        return result;
    }

    @Override
    public boolean getIsRoleTransitionAllowed(List<Integer> newRoleIds,List<Integer> existingRoleIds) {
        // TODO Auto-generated method stub
//        TypedQuery<Scope> query = em.createNamedQuery("getIsRoleTransitionAllowed", Scope.class);
//        query.setParameter("newRoleIds", newRoleIds);
//        query.setParameter("existingRoleIds",existingRoleIds);
//        List<Scope> result =  query.getResultList();
        List<Scope> result = scopeRepository.getIsRoleTransitionAllowed(newRoleIds,existingRoleIds);
         if(result.isEmpty()|| result==null)
        {
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void deleteScopeDefintionForRole(Integer roleId) {
        // TODO Auto-generated method stub
//        TypedQuery<Integer> query = em.createNamedQuery("getGroupIdsFromGroupRole", Integer.class);
//        query.setParameter(1, roleId);
//        List<Integer> groupIds = query.getResultList();
        List<Integer> groupIds = groupRepository.getGroupIdsFromGroupRole(roleId);
        if(groupIds!=null && !groupIds.isEmpty()){
            for(int groupId:groupIds){
//                Query query1 = em.createNamedQuery("deleteScopeDefinitonForRole");
//                query1.setParameter("groupId", groupId);
//                query1.executeUpdate();
                scopeDefinitionRepository.deleteScopeDefinitonForRole(groupId);
            }
        }
    }

    @Override
    public boolean isScopeDefinedForRole(Map<String,Integer> roleIdMap) {
        // TODO Auto-generated method stub
        boolean result = false;
        Long count;
        int roleId = roleIdMap.get("roleId");
//        TypedQuery<Integer> query = em.createNamedQuery("getGroupIdsFromGroupRole", Integer.class);
//        query.setParameter(1, roleId);
//        List<Integer> groupIds = query.getResultList();
        List<Integer> groupIds = groupRepository.getGroupIdsFromGroupRole(roleId);
        if(groupIds!=null && !groupIds.isEmpty()){
            for(int groupId:groupIds){
//                TypedQuery<Long> query1 = em.createNamedQuery("getDefinedScopeCountforGroup",Long.class);
//                query1.setParameter("groupId", groupId);
//                count = query1.getSingleResult();
                count = scopeDefinitionRepository.getDefinedScopeCountForGroup(groupId);
                if(count > 0){
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public Map<Integer,List<Integer>> getScopeDefinitionForRole(Integer roleId) {
        // TODO Auto-generated method stub

        Map<Integer,List<Integer>> groupScopeMap = new HashMap<Integer,List<Integer>>();
//        TypedQuery<Integer> query = em.createNamedQuery("getGroupIdsFromGroupRole", Integer.class);
//        query.setParameter(1, roleId);
//        List<Integer> groupIds = query.getResultList();
        List<Integer> groupIds = groupRepository.getGroupIdsFromGroupRole(roleId);
        if(groupIds!=null && !groupIds.isEmpty()){
            for(int groupId:groupIds){
//                TypedQuery<Integer> query1 = em.createNamedQuery("getScopeDefinitionForRole",Integer.class);
//                query1.setParameter("groupId", groupId);
                List<Integer> result =new ArrayList<Integer>();
//                result =  query1.getResultList();
                result = scopeDefinitionRepository.getScopeDefinitionForRole(groupId);
                groupScopeMap.put(groupId, result);
            }
        }

        log.info("getScopeDefinitionForRole;groupScopeMap={};", groupScopeMap);

        return groupScopeMap;
    }


    //RBAC-1892
    @Override
    public Map<Integer, Set<String>> getRolesByGroup() {

        Map<Integer,Set<String>> groupRoleMap = new HashMap<Integer, Set<String>>();

//        TypedQuery<Object[]> getRolesByGroupQuery = em.createNamedQuery("getRolesByGroup", Object[].class);
//        List<Object[]> result = getRolesByGroupQuery.getResultList();
        List<Object[]> result = roleRepository.getRolesByGroup();
        for (Object[] pair : result) {
            Set<String> roleList = new HashSet<String>();
            Integer groupId = (Integer) pair[2];
            if(groupRoleMap != null && groupRoleMap.containsKey(groupId))
                roleList = groupRoleMap.get(groupId);
            roleList.add((String)pair[1]);
            groupRoleMap.put(groupId,roleList);

        }
        return groupRoleMap;
    }

    public List<RolePermission> getRoleList(Integer roleId){
        TypedQuery<RolePermission> query = em.createQuery("from RolePermission where roleId = :role", RolePermission.class);
        query.setParameter("role", roleId);
        List<RolePermission> permissions = query.getResultList();
        return permissions;
    }
}
