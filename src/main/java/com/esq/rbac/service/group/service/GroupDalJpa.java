package com.esq.rbac.service.group.service;

import com.esq.rbac.service.attributes.domain.AttributesData;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.calendar.service.CalendarDal;
import com.esq.rbac.service.config.CacheConfig;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.group.json.RolesInGroupJson;
import com.esq.rbac.service.group.json.UsersInGroupJson;
import com.esq.rbac.service.group.repository.GroupRepository;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.role.domain.Role;
import com.esq.rbac.service.role.service.RoleDal;
import com.esq.rbac.service.scope.builder.ScopeBuilder;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.scope.repository.ScopeRepository;
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import com.esq.rbac.service.scope.scopedefinition.repository.ScopeDefinitionRepository;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.repository.UserRepository;
import com.esq.rbac.service.user.service.UserDalJpa;
import com.esq.rbac.service.util.*;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.util.externaldatautil.HybridScopeHandler;
import com.esq.rbac.service.variable.service.VariableDal;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterables;
import com.google.gson.*;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

//@Named
@Service
@Slf4j
public class GroupDalJpa extends BaseDalJpa implements GroupDal {


    private GroupRepository groupRepository;

    @Autowired
    public void setGroupRepository(GroupRepository groupRepository) {
        log.trace("setGroupRepository; {};",groupRepository);
        this.groupRepository =groupRepository;

    }

    private CalendarDal calendarDal;

    @Autowired
    public void setCalendarDal(CalendarDal calendarDal) {
        log.trace("setCalendarDal; {};",calendarDal);
        this.calendarDal =calendarDal;

    }



    private TenantStructureGenerator tenantStructureGenerator;

    @Autowired
    public void setTenantStructureGenerator(@Lazy TenantStructureGenerator tenantStructureGenerator) {
        log.trace("setTenantStructureGenerator; {};",tenantStructureGenerator);
        this.tenantStructureGenerator =tenantStructureGenerator;

    }


    private AuditLogService auditLogDal;

    @Autowired
    public void setAuditLogService(AuditLogService auditLogDal) {
        log.trace("setAuditLogService; {};",auditLogDal);
        this.auditLogDal =auditLogDal;

    }


    private CacheService cacheService;

    @Autowired
    public void setCacheService(CacheService cacheService) {
        log.trace("setCacheService; {};",cacheService);
        this.cacheService =cacheService;

    }



    private UserDalJpa userDal;

    @Autowired
    public void setUserDalJpa(UserDalJpa userDal) {
        log.trace("setUserDalJpa; {};",userDal);
        this.userDal =userDal;

    }


    private HybridScopeHandler hybridScopeHandler;

    @Autowired
    public void setHybridScopeHandler(@Lazy HybridScopeHandler hybridScopeHandler) {
        log.trace("setHybridScopeHandler; {};",hybridScopeHandler);
        this.hybridScopeHandler =hybridScopeHandler;

    }


    private VariableDal variableDal;

    @Autowired
    public void setVariableDal(VariableDal variableDal) {
        log.trace("setVariableDal; {};",variableDal);
        this.variableDal =variableDal;

    }

    private RoleDal roleDal;

    @Autowired
    public void setRoleDal(RoleDal roleDal) {
        log.trace("setRoleDal; {};",roleDal);
        this.roleDal =roleDal;

    }

    private ScopeRepository scopeRepository;

    @Autowired
    public void setScopeRepository(ScopeRepository scopeRepository) {
        log.trace("setScopeRepository; {};",scopeRepository);
        this.scopeRepository =scopeRepository;

    }



    private UserRepository userRepository;


    @Autowired
     public void setUserRepository(UserRepository userRepository){
         log.trace("setUserRepository; {};",userRepository);
         this.userRepository =userRepository;
     }




    private ScopeDefinitionRepository scopeDefinitionRepository;


    @Autowired
    public void setScopeDefinitionRepository(ScopeDefinitionRepository scopeDefinitionRepository){
        log.trace("setScopeDefinitionRepository; {};",scopeDefinitionRepository);
        this.scopeDefinitionRepository =scopeDefinitionRepository;
    }


    GroupDalJpa(){

    }

    public static final String DUPLICATED_GROUP = "duplicatedGroup";
    public static final String DUPLICATED_GROUP_NAME = "duplicatedName";
    public static final String USER_GROUP_MAPPING_FOUND = "userGroupMappingFound";
    private static final Map<String, String> SORT_COLUMNS;
    private Map<Integer, List<Map<String, String>>> groupListChangeSet;
    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("name", "g.name");
        SORT_COLUMNS.put("roleName", "r.name");
    }

    @Autowired
    private MessagesUtil messageUtil;
    public Map<Integer, List<Map<String, String>>> getGroupListChangeSet() {
        return this.groupListChangeSet;
    }

    public void setGroupListChangeSet(Map<Integer, List<Map<String, String>>> groupListChangeSet) {
        this.groupListChangeSet = groupListChangeSet;
    }


    private static final String SEARCH_GROUPS_ROLES = "select distinct g.* from rbac.groupTable g "
            + " left outer join rbac.groupRole gr on (g.groupId = gr.groupId) "
            + " left outer join rbac.role r on (r.roleId = gr.roleId) "
            + " where ( lower(g.name) like ? or lower(g.description) like  ? "
            + " or lower(r.name) like ? ) and  g.groupId > 0 ";

    private static final String COUNT_GROUPS_ROLES = "select count(distinct g.groupId) from rbac.groupTable g "
            + " left outer join rbac.groupRole gr on (g.groupId = gr.groupId) "
            + " left outer join rbac.role r on (r.roleId = gr.roleId) "
            + " where ( lower(g.name) like ? or lower(g.description) like  ? "
            + " or lower(r.name) like ? ) and  g.groupId > 0 ";

    private static final String FIND_GROUP_IDENTICAL_SCOPE_DEFINITION = "select g.name, gr.groupId, r.roleId, r.name, sd.definition from rbac.scopeDefinition sd"
            + "	join rbac.groupTable g on(g.groupId = sd.groupId)"
            + "	join rbac.groupRole gr on(sd.groupId = gr.groupId)"
            + "	join rbac.role r on(gr.roleId = r.roleId)"
            + "	where scopeId= ?1"
            + "	and definition = (select definition from rbac.scopeDefinition where scopeId= ?3 and groupId = ?4)"
            + "	and g.groupId != ?2"
            + " and g.groupId > 0 and r.roleId > 0"
            + "	and g.groupId in (  ";

    private static final String FIND_GROUP_UNDEFINED_SCOPES = "select distinct g.name, g.groupId, r.roleId, r.name  from rbac.operation o"
            + " join rbac.operationScope os on(o.operationId = os.operationId)"
            + " join rbac.rolePermission rp on(rp.operationId = o.operationId) "
            + " join rbac.role r on(r.roleId = rp.roleId) "
            + " join rbac.groupRole gr on(gr.roleId = r.roleId) "
            + " join rbac.groupTable g on(gr.groupId = g.groupId) "
            + " where os.scopeId = ?1 "
            + " and g.groupId not in (select groupId from rbac.scopeDefinition where scopeId = ?2)"
            + " and g.groupId != ?3"
            + " and g.groupId > 0 and r.roleId > 0"
            + " and g.groupId in (";
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Group create(Group group, int userId) {
        if (group.getGroupId() != null) {
            throw new IllegalArgumentException("on create groupId must not be set");
        }
        if (group.getRestrictions() != null && group.getRestrictions().getRestrictionId() != null) {
            throw new IllegalArgumentException("on create restrictionId must not be set");
        }
        int groupDuplicate = isGroupNameDuplicate(group.getGroupId(), group.getName());
        log.trace("create; groupDuplicate={}", groupDuplicate);
        if (groupDuplicate > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_GROUP).append("; ");
            sb.append(DUPLICATED_GROUP_NAME).append("=").append(group.getName());
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_GROUP, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_GROUP_NAME, group.getName());
            log.info("create; grouperrorInfo={}", errorInfo);
            throw errorInfo;
        }
        if (group.getCalendar()!=null && group.getCalendar().getCalendarId()==null) {
            calendarDal.create(group.getCalendar(), new AuditLogInfo(userId, null));
        }
        group.setCreatedBy(userId);
        group.setCreatedOn(DateTime.now().toDate());

//        em.persist(group);
        groupRepository.save(group);

        Group enrichedGroup = tenantStructureGenerator.handleGroupCreation(group);
//        em.merge(enrichedGroup);
        groupRepository.save(enrichedGroup);
        //setNewObjectChangeSet(group);
        Map<String, String> objectChanges = setNewObjectChangeSetLocal(enrichedGroup);
        auditLogDal.createAsyncLog(userId, enrichedGroup.getName(), "Group", "Create", objectChanges);
        return enrichedGroup;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Group update(Group group, int userId) {
        cacheService.clearCache(CacheConfig.CLEAR_GROUP_CACHE);//CACHE:: CLEAR
        if (group.getGroupId() == null) {
            throw new IllegalArgumentException("groupId missing");
        }
//        Group existingGroup = em.find(Group.class, group.getGroupId());
        Group existingGroup = groupRepository.findById(group.getGroupId()).orElse(null);
        groupRepository.findById(group.getGroupId());
        if (existingGroup == null) {
            throw new IllegalArgumentException("groupId invalid");
        }

        int groupDuplicate = isGroupNameDuplicate(group.getGroupId(), group.getName());
        log.trace("update; groupDuplicate={}", groupDuplicate);
        if (groupDuplicate > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_GROUP).append("; ");
            sb.append(DUPLICATED_GROUP_NAME).append("=").append(group.getName());
            log.info("update; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_GROUP, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_GROUP_NAME, group.getName());
            throw errorInfo;
        }
        //setObjectChangeSet(existingGroup, group);
        Map<String, String> objectChanges = setObjectChangeSetLocal(existingGroup, group);

        existingGroup.setName(group.getName());
        existingGroup.setDescription(group.getDescription());
        existingGroup.setLabels(group.getLabels());
        existingGroup.setRestrictions(group.getRestrictions());
        if (group.getCalendar()!=null && group.getCalendar().getCalendarId()==null) {
            calendarDal.create(group.getCalendar(), new AuditLogInfo(userId, null));
        }
        existingGroup.setCalendar(group.getCalendar());
        existingGroup.setAttributesData(group.getAttributesData());
        existingGroup.setTenantId(group.getTenantId());
        existingGroup.setIsTemplate(group.getIsTemplate());
        existingGroup.setUpdatedBy(userId);
        existingGroup.setUpdatedOn(DateTime.now().toDate());
        Group enrichedGroup = tenantStructureGenerator.handleGroupUpdation(existingGroup, group);

//        Group grp = em.merge(enrichedGroup);
        Group grp = groupRepository.save(enrichedGroup);
        auditLogDal.createAsyncLog(userId, group.getName(), "Group", "Update", objectChanges);

        return grp;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, allEntries = true)
    public Group updateRoles(Group group, boolean isUndefinedScopesAllowed, Integer loggedInUserId) {
        cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);
        cacheService.clearCache(CacheConfig.CLEAR_TENANT_CACHE);
        if (group.getGroupId() == null) {
            throw new IllegalArgumentException("groupId missing");
        }
//        Group existingGroup = em.find(Group.class, group.getGroupId());
        Group existingGroup = groupRepository.findById(group.getGroupId()).orElse(null);
        if (existingGroup == null) {
            throw new IllegalArgumentException("groupId invalid");
        }
        //validate for mandatory scopes undefined
        checkForIsUndefinedScopesAllowed(group, isUndefinedScopesAllowed);
        //setObjectChangeSet(existingGroup, group);

        Set<ScopeDefinition> updatedDefaultTenantScopeDefinition = checkForTenantDefaultScope(group);//RBAC-2619
        group.setScopeDefinitions(updatedDefaultTenantScopeDefinition);
        Map<String, String> objectChanges = setObjectChangeSetLocal(existingGroup, group);

        existingGroup.setRolesIds(group.getRolesIds());
        existingGroup.setScopeDefinitions(group.getScopeDefinitions());
        existingGroup.setUpdatedBy(loggedInUserId);
        existingGroup.setUpdatedOn(DateTime.now().toDate());
//        Group grp = em.merge(existingGroup);
        Group grp = groupRepository.save(existingGroup);
        hybridScopeHandler.handleScope(grp, new AuditLogInfo(loggedInUserId, "unknownFromCode"));
        auditLogDal.createAsyncLog(loggedInUserId, group.getName(), "Group", "Update", objectChanges);
        return grp;
    }

    private Set<ScopeDefinition> checkForTenantDefaultScope(Group group) {
        Set<ScopeDefinition> scopeSet = new HashSet<ScopeDefinition>();
        if (!Lookup.getTenantIsHostById(group.getTenantId())) {
            // check only for groups other than host
            Boolean tenantScopeExistsInDefinition = false;
            if (group.getScopeDefinitions() != null && !group.getScopeDefinitions().isEmpty()) {
                for (ScopeDefinition scopeDef : group.getScopeDefinitions()) {
                    Integer scopeId = scopeDef.getScopeId();
                    if (Lookup.getScopeKey(scopeId).equalsIgnoreCase(RBACUtil.SCOPE_KEY_TENANT)) {
                        tenantScopeExistsInDefinition = true;
                        Boolean groupTenantExists = false;
                        ArrayNode jsonArray = ScopeBuilder.tenantScopeToJson(scopeDef.getScopeAdditionalData());
                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                if (group.getTenantId().equals(jsonArray.path(i).asLong())) {
                                    groupTenantExists = true;
                                    break;
                                }
                            }
                        }

                        if (!groupTenantExists) {
                            log.debug("scopeDef getScopeId {},  getScopeDefinition {}", scopeDef.getScopeId(),
                                    scopeDef.getScopeDefinition());
                            JsonParser jsonParser = new JsonParser();
                            JsonElement jsonScope = jsonParser.parse(scopeDef.getScopeAdditionalData());
                            JsonObject jsonObj = jsonScope.getAsJsonObject();
                            JsonObject rulesJson = jsonObj.get("rules").getAsJsonArray().get(0).getAsJsonObject();
                            JsonArray pluginElement = rulesJson.get("pluginValue").getAsJsonArray();

                            String field = rulesJson.get("field").getAsString();

                            String tenantIds = "";

                            JsonObject newJsonObj = new JsonObject();
                            newJsonObj.addProperty("text", Lookup.getTenantNameById(group.getTenantId()));
                            newJsonObj.addProperty("id", group.getTenantId() + "");
                            pluginElement.add(newJsonObj);
                            JsonArray valueArray = rulesJson.get("value").getAsJsonArray();

                            for (int j = 0; j < valueArray.size(); j++)
                                tenantIds += "," + valueArray.get(j).getAsLong();

                            valueArray.add(new JsonPrimitive(group.getTenantId().toString()));
                            tenantIds += "," + group.getTenantId();

                            if (tenantIds != "")
                                tenantIds = tenantIds.substring(1);

                            log.trace("valueArray {}", valueArray);
                            log.trace("pluginElement {}", pluginElement);
                            rulesJson.add("value", valueArray);
                            rulesJson.add("pluginValue", pluginElement);
                            JsonArray newRuleArr = new JsonArray();
                            newRuleArr.add(rulesJson);
                            log.trace("newRuleArr {}", newRuleArr);
                            jsonObj.add("rules", newRuleArr);
                            scopeDef.setScopeAdditionalData(jsonObj + "");
                            scopeDef.setScopeDefinition(field + " IN (" + tenantIds + ")");
                            log.trace("scopeDef Final {}", scopeDef);
                        }
                        scopeSet.add(scopeDef);
                    } else {
                        scopeSet.add(scopeDef);
                    }
                }
            }

            if (!tenantScopeExistsInDefinition) {
                // No tenant scope exists for the tenant group.
                // Create the tenant scope and add to the scope definition
                ScopeDefinition scopeDef = tenantStructureGenerator.createDefaultScopeForTenant(group.getTenantId());
                scopeDef.setGroup(group);
                scopeDef.setGroupId(group.getGroupId());
                scopeDef.setScopeId(Lookup.getScopeIdByKey(RBACUtil.SCOPE_KEY_TENANT));
                scopeSet.add(scopeDef);
            }

        } else {
            scopeSet = group.getScopeDefinitions();
        }
        return scopeSet;
    }

    public void checkForIsUndefinedScopesAllowed(Group group, boolean isUndefinedScopesAllowed) {
        log.debug("checkForIsUndefinedScopesAllowed; isUndefinedScopesAllowed={}", isUndefinedScopesAllowed);
        if(!isUndefinedScopesAllowed && group.getRolesIds()!=null && !group.getRolesIds().isEmpty()){
            //get mandatory scopes
            Set<Integer> scopeIdsDefinitionRequired = new HashSet<Integer>();
//            TypedQuery<Integer> query = em.createNamedQuery("getMandatoryScopeIdsForSelectedRoles", Integer.class);
//            query.setParameter("roleIds", group.getRolesIds());
//            List<Integer> manScopeList = query.getResultList();
            List<Integer> manScopeList = scopeRepository.getMandatoryScopeIdsForSelectedRoles(group.getRolesIds());

            if(scopeRepository.getMandatoryScopeIdsForSelectedRoles(group.getRolesIds()) !=null && !manScopeList.isEmpty()){
                scopeIdsDefinitionRequired.addAll(manScopeList);
            }

//            TypedQuery<Integer> query1 = em.createNamedQuery("getMandatoryScopeIdsForGlobalScopes", Integer.class);
//            List<Integer> manScopeGlobalList = query1.getResultList();
            List<Integer> manScopeGlobalList = scopeRepository.getMandatoryScopeIdsForGlobalScopes();
            if(manScopeGlobalList!=null && !manScopeGlobalList.isEmpty()){
                scopeIdsDefinitionRequired.addAll(manScopeGlobalList);
            }

            //proceed further only if mandatory scopes are there in selected roles
            if(scopeIdsDefinitionRequired!=null && !scopeIdsDefinitionRequired.isEmpty()) {

                //get scope ids in request
                Set<Integer> scopeIdsInRequest = new HashSet<Integer>();
                if(group.getScopeDefinitions()!=null && !group.getScopeDefinitions().isEmpty()){
                    for(ScopeDefinition scopeDefinition:group.getScopeDefinitions()){
                        scopeIdsInRequest.add(scopeDefinition.getScopeId());
                    }
                }

                //get manadtory scope ids from defined scopes
                Set<Integer> scopeIdsInRequestRequired = new HashSet<Integer>();
                if(scopeIdsInRequest!=null && !scopeIdsInRequest.isEmpty()){
//                    TypedQuery<Integer> queryForMandatoryScopes = em.createNamedQuery("getMandatoryScopeIdsForSelectedScopeIds", Integer.class);
//                    queryForMandatoryScopes.setParameter("scopeIds", scopeIdsInRequest);
//                    scopeIdsInRequestRequired =  new HashSet<Integer>(queryForMandatoryScopes.getResultList());
                    scopeIdsInRequestRequired =  new HashSet<Integer>(scopeRepository.getMandatoryScopeIdsForSelectedScopeIds(scopeIdsInRequest));
                }

                //check if any mandatory scopes are missing
                if(!scopeIdsInRequestRequired.equals(scopeIdsDefinitionRequired)){
                    Set<Integer> missingScopeDefinitions = new HashSet<Integer>(scopeIdsDefinitionRequired);
                    Set<String> missingScopeDefinitionNames = new HashSet<String>();
                    for (Integer scopeId : scopeIdsInRequestRequired) {
                        if (!missingScopeDefinitions.add(scopeId)) {
                            missingScopeDefinitions.remove(scopeId);
                        }
                    }
                    if(missingScopeDefinitions!=null && !missingScopeDefinitions.isEmpty()){
                        for(Integer scopeId : missingScopeDefinitions){
                            missingScopeDefinitionNames.add(Lookup.getScopeName(scopeId));
                        }
                        ErrorInfoException errorInfoException = new ErrorInfoException("mandatoryScopesUndefined");
                        errorInfoException.getParameters().put("missingScopeNames", missingScopeDefinitionNames.toString());
                        throw errorInfoException;
                    }
                }
            }
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Group updateScopeDefinition(Group group, Integer loggedInUserId) {
        cacheService.clearCache(CacheConfig.CLEAR_GROUP_CACHE);//CACHE:: CLEAR
        cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);
        if (group.getGroupId() == null) {
            throw new IllegalArgumentException("groupId missing");
        }
//        Group existingGroup = em.find(Group.class, group.getGroupId());
        Group existingGroup = groupRepository.findById(group.getGroupId()).orElse(null);
        if (existingGroup == null) {
            throw new IllegalArgumentException("groupId invalid");
        }
        //validate for mandatory scopes undefined
        //checkForIsUndefinedScopesAllowed(group, isUndefinedScopesAllowed);
        //setObjectChangeSet(existingGroup, group);
        //Map<String, String> objectChanges = setObjectChangeSetLocal(existingGroup, group);

        existingGroup.setRolesIds(group.getRolesIds());
        existingGroup.setScopeDefinitions(group.getScopeDefinitions());
        existingGroup.setUpdatedBy(loggedInUserId);
        existingGroup.setUpdatedOn(DateTime.now().toDate());
//        Group grp = em.merge(existingGroup);
        Group grp = groupRepository.save(existingGroup);
        //hybridScopeHandler.handleScope(grp, new AuditLogInfo(loggedInUserId, "unknown"));
        //auditLogDal.createAsyncLog(loggedInUserId, group.getName(), "Group", "Update", objectChanges);
        return grp;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Group cloneGroup(int fromGroupId, int toGroupId, boolean isUndefinedScopesAllowed, Integer loggedInUserId) {
//        Group fromGroup = em.find(Group.class, fromGroupId);
        Group fromGroup = groupRepository.findById(fromGroupId).orElse(null);
        if (fromGroup == null) {
            throw new IllegalArgumentException("fromGroupId invalid");
        }
//        Group toGroup = em.find(Group.class, toGroupId);
        Group toGroup = groupRepository.findById(toGroupId).orElse(null);
        if (toGroup == null) {
            throw new IllegalArgumentException("toGroupId invalid");
        }
        Group auditLogObj = new Group();
        auditLogObj.setDescription(toGroup.getDescription());
        auditLogObj.setName(toGroup.getName());
        auditLogObj.setLabels(toGroup.getLabels());
        auditLogObj.setGroupId(toGroup.getGroupId());
        auditLogObj.setRestrictions(toGroup.getRestrictions());
        auditLogObj.setRolesIds(fromGroup.getRolesIds());
        auditLogObj.setScopeDefinitions(fromGroup.getScopeDefinitions());
        //validate for mandatory scopes undefined
        checkForIsUndefinedScopesAllowed(auditLogObj, isUndefinedScopesAllowed);
        //setObjectChangeSet(toGroup, auditLogObj);
        Map<String, String> objectChanges = setObjectChangeSetLocal(toGroup, auditLogObj);

        toGroup.setRolesIds(fromGroup.getRolesIds());

        toGroup.setScopeDefinitions(new HashSet<ScopeDefinition>()); //RBAC-2295

        for(ScopeDefinition sd: fromGroup.getScopeDefinitions()){
            ScopeDefinition temp = new ScopeDefinition();
            temp.setScopeId(sd.getScopeId());
            temp.setGroup(toGroup);
            temp.setGroupId(toGroupId);
            temp.setScopeDefinition(sd.getScopeDefinition());
            temp.setScopeAdditionalData(sd.getScopeAdditionalData());
            toGroup.getScopeDefinitions().add(temp);
        }
        toGroup.setUpdatedBy(loggedInUserId);
        toGroup.setUpdatedOn(DateTime.now().toDate());
//        Group grp = em.merge(toGroup);
        Group grp = groupRepository.save(toGroup);
        auditLogDal.createAsyncLog(loggedInUserId, Lookup.getGroupName(toGroupId), "Group", "Update", objectChanges);
        return grp;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.GROUP_BY_GROUPID_CACHE, unless = "#result == null")
    public Group getById(int groupId) {
//        return em.find(Group.class, groupId);
        return groupRepository.findById(groupId).orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean checkEntityPermission(int groupId, Options options) {
        Filters filters = prepareFilters(options);
        filters.addCondition(" g.groupId = "+ groupId + " ");
        if(filters.getCount(em, "select count(g) from Group g")==1){
            return true;
        }
        return false;
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        if (filters != null) {

            String name = filters.get("name");
            if (name != null && name.length() > 0) {
                result.addCondition("g.name = :name");
                result.addParameter("name", name);
            }

            String label = filters.get("label");
            if (label != null && label.length() > 0) {
                result.addCondition(":label member of g.labels");
                result.addParameter("label", label);
            }

            String scopeQuery = filters.get(RBACUtil.GROUP_SCOPE_QUERY);
            if (scopeQuery != null && scopeQuery.length() > 1) {
                result.addCondition("("+scopeQuery+")");
            }

            String appRole = filters.get("appRole");
            if (appRole != null && appRole.length() > 0) {
                result.addCondition(" g.groupId in (select gr.groupId from GroupRole gr where gr.roleId in"
                        + " (select r.roleId from Role r where r.applicationId = :appRole )) ");
                result.addParameter("appRole", Integer.parseInt(appRole));
            }

            String loggedInUserName = filters.get("loggedInUserName");
            if (loggedInUserName != null && loggedInUserName.length() > 0) {
                String scope = RBACUtil.extractScopeForGroup(
                        userDal.getUserScopes(loggedInUserName, RBACUtil.RBAC_UAM_APPLICATION_NAME, true), null, false);
                if(scope!=null && !scope.isEmpty()){
                    result.addCondition(" (" + scope + ") ");
                }
            }

            String tenantId = filters.get("tenantId");
            if (tenantId != null && tenantId.length() > 0) {
                result.addCondition(" g.tenantId = :tenantId ");
                result.addParameter("tenantId", Long.valueOf(tenantId));
            }

            String tenantName = filters.get("tenantName");
            if (tenantName != null && tenantName.length() > 0) {
                result.addCondition(" g.tenantId = :tenantIdByName ");
                result.addParameter("tenantIdByName", Lookup.getTenantIdByName(tenantName));
            }

        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Group getByName(String name) {
        try {
//            TypedQuery<Group> query = em.createNamedQuery("getGroupByName", Group.class);
//            query.setParameter("name", name);
//            return query.getSingleResult();
           return groupRepository.getGroupByName(name).get(0);
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = CacheConfig.GROUP_BY_GROUPID_CACHE, allEntries = true)
    public void deleteById(int groupId) {
        //picking userIds for showing userNames in error message on UI
//        TypedQuery<Integer> query = em.createNamedQuery("getUserIdsFromUserTableByGroup", Integer.class);
//        query.setParameter(1, groupId);
//        List<Integer> userIds = query.getResultList();
        List<Integer> userIds = groupRepository.getUserIdsFromUserTableByGroup(groupId);
        if(userIds!=null && !userIds.isEmpty()){
            ErrorInfoException errorInfo = new ErrorInfoException(USER_GROUP_MAPPING_FOUND);
            throw errorInfo;
        }

        //temporary - added for variable deletion with appId or userId
        variableDal.deleteForCascade(null, groupId, null);
//        Group group = em.find(Group.class, groupId);
        Group group = groupRepository.findById(groupId).orElse(null);
//        em.remove(group);
        if(group!=null)
            groupRepository.delete(group);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = CacheConfig.GROUP_BY_GROUPID_CACHE, allEntries = true)
    public void deleteByName(String name) {
        //temporary - added for variable deletion with appId or userId
        //variableDal.deleteForCascade(null, groupId, null);
//        Query query = em.createNamedQuery("deleteGroupByName");
//        query.setParameter("name", name);
//        query.executeUpdate();
        groupRepository.deleteGroupByName(name);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<String> getAllNames() {
//        TypedQuery<String> query = em.createNamedQuery("getAllGroupNames", String.class);
//        return query.getResultList();
        return groupRepository.getAllGroupNames();
    }

    @Override
    public List<Integer> getScopeIds(int groupId) {
//        TypedQuery<Integer> query = em.createNamedQuery("getGroupScopeIds", Integer.class);
//        query.setParameter(1, groupId);
//        return query.getResultList();
        return groupRepository.getGroupScopeIds(groupId);
    }

    @Override
    public List<Scope> getScopes(int groupId, boolean includeDefault,String loggedInUser) {
//        Group group = em.find(Group.class, groupId);
        Group group = groupRepository.findById(groupId).orElse(null);
        if(group==null){
            throw new IllegalArgumentException(groupId+" not found");
        }
        Set<Integer> roleIds = group.getRolesIds();
        if(roleIds!=null && !roleIds.isEmpty()){
            Map<String, List<Integer>> roleIdsList = new HashMap<String, List<Integer>>();
            roleIdsList.put("roleIds", new LinkedList<Integer>(roleIds));
            return roleDal.getGroupRoleScopeIds(roleIdsList, includeDefault,loggedInUser);
        }
        return new LinkedList<Scope>();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Group> getList(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getList(em, Group.class, "select g from Group g", options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int getCount(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getCount(em, "select count(g) from Group g");
    }

    @Override
    public int isGroupNameDuplicate(Integer groupId, String groupName) {
        log.debug("isGroupNameDuplicate; groupId " + groupId + " , groupName " + groupName);
//        TypedQuery<Group> query = em.createNamedQuery("getGroupByName", Group.class);
//        query.setParameter("name", groupName);
//        List<Group> fetchedGroupIds = query.getResultList();
        List<Group> fetchedGroupIds = groupRepository.getGroupByName(groupName);
        if (fetchedGroupIds != null && !fetchedGroupIds.isEmpty()) {
            log.info("isGroupNameDuplicate; Found groups " + (fetchedGroupIds.size()) + " with name " + (groupName));
            if (groupId != null && groupId.intValue() > 0) {
                for (Iterator<Group> iterator = fetchedGroupIds.iterator(); iterator.hasNext();) {
                    Group fetchedGroup = (Group) iterator.next();
                    if (fetchedGroup != null && groupId.equals(fetchedGroup.getGroupId())) {
                        // updating same group
                        return 0;
                    }
                }
            }
            // duplicate
            return 1;
        }
        return 0;
    }

    @Override
    public List<Group> searchList(Options options) {
        String q = SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase();
        String inlcudeRoleSearch = SearchUtils.getSearchParam(options,
                "_inlcudeRoleSearch");
        StringBuilder sb = new StringBuilder();
        Query query = null;
        if (inlcudeRoleSearch != null
                && inlcudeRoleSearch.equalsIgnoreCase("true")) {
            sb.append(SEARCH_GROUPS_ROLES);
            sb.append(SearchUtils.getOrderByParam(options, SORT_COLUMNS));
            query = em.createNativeQuery(sb.toString(), Group.class);
            String wildcardedq = SearchUtils.wildcarded(q);
            query.setParameter(1, wildcardedq);
            query.setParameter(2, wildcardedq);
            query.setParameter(3, wildcardedq);
        } else {
            Filters filters = prepareFilters(options);


            filters.addCondition(" ( lower(g.name) like :q or lower(g.description) like :q ) ");

            filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
                    .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                    .toLowerCase()));


            return filters.getList(em, Group.class, "select distinct g from Group g", options, SORT_COLUMNS);
        }
        OptionPage optionPage = options != null ? options
                .getOption(OptionPage.class) : null;
        if (optionPage != null) {
            query.setFirstResult(optionPage.getFirstResult());
            query.setMaxResults(optionPage.getMaxResults());
        }
        List<Group> groups = query.getResultList();
        return groups;
    }

    @Override
    public int getSearchCount(Options options) {
        String q = SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase();
        String inlcudeRoleSearch = SearchUtils.getSearchParam(options,
                "_inlcudeRoleSearch");
        StringBuilder sb = new StringBuilder();
        Query query = null;
        if (inlcudeRoleSearch != null
                && inlcudeRoleSearch.equalsIgnoreCase("true")) {
            sb.append(COUNT_GROUPS_ROLES);
            query = em.createNativeQuery(sb.toString());
            String wildcardedq = SearchUtils.wildcarded(q);
            query.setParameter(1, wildcardedq);
            query.setParameter(2, wildcardedq);
            query.setParameter(3, wildcardedq);
        } else {
            Filters filters = prepareFilters(options);


            filters.addCondition(" ( lower(g.name) like :q or lower(g.description) like :q ) ");

            filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
                    .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                    .toLowerCase()));


            return filters.getCount(em, "select count(distinct g) from Group g ");
        }
        return ((Number) query.getSingleResult()).intValue();

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<String,List<String>> getAllGroupRoleScopes(Options options) {

        Filters filters = prepareFilters(options);
        //TypedQuery<Object[]> query = em.createNamedQuery("getAllGroupRoleScopes", Object[].class);
        filters.addCondition("g.groupId > 0 and r.roleId > 0");
        List<Object[]> list =  filters.getList(em, Object[].class, "select g.groupId, g.name, r.name from Group g LEFT JOIN GroupRole gr ON gr.groupId=g.groupId LEFT JOIN Role r on r.roleId=gr.roleId", options, SORT_COLUMNS);
        Map<String,List<String>> result = new LinkedHashMap<String,List<String>>();
        //List<Object[]> list = query.getResultList();
        if(list!=null && list.size()>0){
            for (Object[] pair : list) {
                if(result.containsKey(pair[1].toString()+":"+pair[0].toString())){
                    if(pair[2]!=null){
                        if(!result.get(pair[1].toString()+":"+pair[0].toString()).contains((pair[2].toString()))){
                            result.get(pair[1].toString()+":"+pair[0].toString()).add(pair[2].toString());
                        }
                    }
                }
                else{
                    List<String> roles = new ArrayList<String>();
                    if(pair[2]!=null){
                        roles.add(pair[2].toString());
                    }
                    result.put(pair[1].toString()+":"+pair[0].toString(), roles);
                }
            }
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void cloneScopeDefinitionFromGroup(int fromGroupId, Map<Integer, List<Integer>> fromScopeToGroupIds,  boolean isUndefinedScopesAllowed, Integer loggedInUserId) {
//        Group fromGroup = em.find(Group.class, fromGroupId);
        Group fromGroup = groupRepository.findById(fromGroupId).orElse(null);
        if (fromGroup == null) {
            throw new IllegalArgumentException("fromGroupId invalid");
        }

        if (fromScopeToGroupIds == null || fromScopeToGroupIds.size() == 0) {
            throw new IllegalArgumentException("fromScopeToGroupIds invalid");
        }
        Set<Integer> scopeIds = fromScopeToGroupIds.keySet();
        for (Integer fromScopeId : scopeIds) {
            ScopeDefinition scopeDefinitionToCopy = null;
            for(ScopeDefinition toCopy:fromGroup.getScopeDefinitions()){
                if(toCopy.getScopeId().equals(fromScopeId)){
                    scopeDefinitionToCopy = toCopy;
                }
            }
            List<Integer> toGroupIds = fromScopeToGroupIds.get(fromScopeId);
            if (toGroupIds != null && !toGroupIds.isEmpty()) {
                for (Integer toGroupId : toGroupIds) {
//                    Group toGroup = em.find(Group.class, toGroupId);
                    Group toGroup = groupRepository.findById(toGroupId).orElse(null);
                    if (toGroup == null) {
                        throw new IllegalArgumentException("toGroupId invalid");
                    }

                    Group auditLogObj = new Group();
                    auditLogObj.setDescription(toGroup.getDescription());
                    auditLogObj.setName(toGroup.getName());
                    auditLogObj.setLabels(toGroup.getLabels());
                    auditLogObj.setGroupId(toGroup.getGroupId());
                    auditLogObj.setRestrictions(toGroup.getRestrictions());
                    auditLogObj.setRolesIds(toGroup.getRolesIds());
                    if(auditLogObj.getScopeDefinitions() == null) {
                        auditLogObj.setScopeDefinitions(new TreeSet<ScopeDefinition>());
                    }
                    auditLogObj.getScopeDefinitions().addAll(toGroup.getScopeDefinitions());

                    //removed to allow undefining of scopes & only one scope was defined earlier.
                    //if(fromGroup.getScopeDefinitions() != null && !fromGroup.getScopeDefinitions().isEmpty())
                    //{

                    if (toGroup.getScopeDefinitions() == null) {
                        toGroup.setScopeDefinitions(new TreeSet<ScopeDefinition>());
                    }
                    boolean valueFound = false;

                    //used iterator to remove(undefine) scope definition while iterating
                    //for(ScopeDefinition sd: toGroup.getScopeDefinitions()){
                    for (Iterator<ScopeDefinition> iterator = toGroup.getScopeDefinitions().iterator(); iterator.hasNext();) {
                        ScopeDefinition sd = iterator.next();
                        if(sd.getScopeId().equals(fromScopeId)){
                            if(scopeDefinitionToCopy==null){
                                iterator.remove();
                            }
                            else{
                                sd.setScopeAdditionalData(scopeDefinitionToCopy.getScopeAdditionalData());
                                sd.setScopeDefinition(scopeDefinitionToCopy.getScopeDefinition());
                            }
                            valueFound = true;
                        }
                    }
                    if(!valueFound){
                        if(scopeDefinitionToCopy!=null){
                            ScopeDefinition temp = new ScopeDefinition();
                            temp.setScopeId(fromScopeId);
                            temp.setGroup(toGroup);
                            temp.setGroupId(toGroupId);
                            temp.setScopeDefinition(scopeDefinitionToCopy.getScopeDefinition());
                            temp.setScopeAdditionalData(scopeDefinitionToCopy.getScopeAdditionalData());
                            toGroup.getScopeDefinitions().add(temp);
                        }
                    }
                    toGroup.setUpdatedBy(loggedInUserId);
                    toGroup.setUpdatedOn(DateTime.now().toDate());
//                    em.merge(toGroup);
                    groupRepository.save(toGroup);


                    //setObjectChangeSet(toGroup, auditLogObj);
                    Map<String, String> objectChanges = setObjectChangeSetLocal(toGroup, auditLogObj);

                    groupListChangeSet  = groupListChangeSet == null? new HashMap<Integer, List<Map<String, String>>>():groupListChangeSet;
                    if(groupListChangeSet.containsKey(fromScopeId)){
                        groupListChangeSet.get(fromScopeId).add(objectChanges);
                    }else{
                        List<Map<String, String>> objectChangeSetList = new LinkedList<Map<String,String>>();
                        objectChangeSetList.add(objectChanges);
                        groupListChangeSet.put(fromScopeId, objectChangeSetList);
                    }
                    //}
                }
            }
        }
        Map<Integer, List<Map<String, String>>> groupListChangeSetMap = getGroupListChangeSet();

        Set<Integer> scopeIdsUpdated = groupListChangeSetMap.keySet();
        for (Integer scopeId : scopeIdsUpdated) {
            List<Map<String, String>> groupListChangeSet = groupListChangeSetMap.get(scopeId);
            for (Map<String, String> groupChangeSet : groupListChangeSet) {
                //auditLogger.logCreate(userId, groupChangeSet.get("name") , "Group", "Update", groupChangeSet);
                auditLogDal.createAsyncLog(loggedInUserId, groupChangeSet.get("name") , "Group", "Update", groupChangeSet);
            }
        }
        groupListChangeSetMap.clear();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.GROUP_ID_NAMES_CACHE, unless = "#result == null")
    public List<Map<String,Object>> getGroupIdNames(Options options) {
        List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
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
        List<Object[]> result = filters.getList(em, Object[].class, "select g.groupId, g.name from Group g", options, SORT_COLUMNS);
        if(result!=null && !result.isEmpty()){
            for(Object[] obj:result){
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("groupId", obj[0]);
                temp.put("name", obj[1].toString());
                returnObj.add(temp);
            }
        }
        return returnObj;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.GROUP_ID_NAMES_WITH_SCOPES_CACHE, unless = "#result == null")
    public List<Map<String,Object>> getGroupIdNamesWithScope(Options options) {
        List<Map<String,Object>> returnObj = new LinkedList<Map<String,Object>>();
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
        List<Object[]> result = filters.getList(em, Object[].class, "select g.groupId, g.name, g.tenantId from Group g", options, SORT_COLUMNS);
        if(result!=null && !result.isEmpty()){
            Map<Long, Map<String, Object>> tenantSet = new LinkedHashMap<Long, Map<String, Object>>();
            for(Object[] obj:result){
                if(obj[2]!=null){
                    Long tenantId = Long.parseLong(obj[2].toString());
                    if(!tenantSet.containsKey(tenantId)){
                        Map<String, Object> tenantMap = new LinkedHashMap<String, Object>();
                        tenantMap.put("tenantId", tenantId);
                        tenantMap.put("tenantName", tenantId);
                        tenantMap.put("groups", new LinkedList<Map<String, Object>>());
                        tenantSet.put(tenantId, tenantMap);
                    }
                    Map<String, Object> temp = new HashMap<String, Object>();
                    temp.put("groupId", obj[0]);
                    temp.put("name", obj[1].toString());
                    ((List<Map<String, Object>>)tenantSet.get(tenantId).get("groups")).add(temp);
                }
            }
            returnObj.addAll(tenantSet.values());
        }
        return returnObj;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String,String>> getAllGroupWithIdenticalScopeDefinition(Options options) {
        List<Map<String,String>> result = new ArrayList<Map<String,String>>();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filter = optionFilter == null ? null : optionFilter.getFilters();
        StringBuilder idList = new StringBuilder();
        List<Map<String,Object>> response = getGroupIdNames(options);
        for (Map<String, Object> map : response) {
            idList.append(Integer.parseInt(map.get("groupId").toString())).append(",");
        }
        Query query = em.createNativeQuery(FIND_GROUP_IDENTICAL_SCOPE_DEFINITION.concat(idList.substring(0, idList.length() - 1)).concat(")"));
        query.setParameter(1, Integer.parseInt(filter.get("scopeId")));
        query.setParameter(2, Integer.parseInt(filter.get("groupId")));
        query.setParameter(3, Integer.parseInt(filter.get("scopeId")));
        query.setParameter(4, Integer.parseInt(filter.get("groupId")));

        String userId = filter.get("userId");
        String isSelfUpdateAllowed = filter.get("isSelfUpdateAllowed");
        Integer groupId = null;
        if(isSelfUpdateAllowed.equalsIgnoreCase("true") || isSelfUpdateAllowed.equalsIgnoreCase("false")){
            if(isSelfUpdateAllowed.equalsIgnoreCase("false")){
                if(!Objects.isNull(userId)){
                    groupId = userDal.getById(Integer.parseInt(userId)).getGroupId();
                }else {
                    log.info("getAllGroupWithIdenticalScopeDefinition; userId is required if isSelfUpdateAllowed is false");
                    throw new ErrorInfoException("isSelfUpdateAllowed","userId is required if isSelfUpdateAllowed is false");
                }
            }
            List<Object[]> list =  query.getResultList();
            if(list!=null && list.size()>0){
                for (Object[] pair : list) {
                    if(groupId  != null && groupId.equals(Integer.parseInt(pair[1].toString()))){
                        continue;
                    }
                    Map<String, String> row = new HashMap<String, String>();
                    row.put("groupName", pair[0].toString());
                    row.put("groupId", pair[1].toString());
                    row.put("roleId", pair[2].toString());
                    row.put("roleName", pair[3].toString());
                    row.put("definition", pair[4].toString());
                    result.add(row);
                }
            }
        }else {
            throw new ErrorInfoException("isSelfUpdateAllowed","isSelfUpdateAllowed should be bollean i.e true or false");
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String,String>> getAllGroupWithUndefinedScopes(Options options) {
        List<Map<String,String>> result = new ArrayList<Map<String,String>>();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filter = optionFilter == null ? null : optionFilter.getFilters();
        StringBuilder idList = new StringBuilder();
        List<Map<String,Object>> response = getGroupIdNames(options);
        for (Map<String, Object> map : response) {
            idList.append(Integer.parseInt(map.get("groupId").toString())).append(",");
        }
        Query query = em.createNativeQuery(FIND_GROUP_UNDEFINED_SCOPES.concat(idList.substring(0, idList.length() - 1)).concat(")"));
        query.setParameter(1, Integer.parseInt(filter.get("scopeId")));
        query.setParameter(2, Integer.parseInt(filter.get("scopeId")));
        query.setParameter(3, Integer.parseInt(filter.get("groupId")));

        String userId = filter.get("userId");
        String isSelfUpdateAllowed = filter.get("isSelfUpdateAllowed");

        Integer groupId = null;
        if(isSelfUpdateAllowed.equals("false")){
            groupId = userDal.getById(Integer.parseInt(userId)).getGroupId();
        }
        List<Object[]> list =  query.getResultList();
        if(list!=null && list.size()>0){
            for (Object[] pair : list) {
                if(groupId  != null && groupId.equals(Integer.parseInt(pair[1].toString()))){
                    continue;
                }
                Map<String, String> row = new HashMap<String, String>();
                row.put("groupName", pair[0].toString());
                row.put("groupId", pair[1].toString());
                row.put("roleId", pair[2].toString());
                row.put("roleName", pair[3].toString());
                result.add(row);
            }
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<UsersInGroupJson> getUsersInGroupsData(Map<String, String> scopeMap) {
        List<UsersInGroupJson> result = new LinkedList<UsersInGroupJson>();
        List<String> userIds = new LinkedList<String>();
        OptionFilter optionFilter = new OptionFilter();
        optionFilter.addFilter(RBACUtil.USER_SCOPE_QUERY, scopeMap.get(RBACUtil.SCOPE_KEY_USER_VIEW));
        List<Map<String, Object>> userIdNames = userDal.getUserIdNames(new Options(optionFilter));
        if(userIdNames!=null && !userIdNames.isEmpty()){
            for(Map<String, Object> userIdName:userIdNames){
                userIds.add(userIdName.get("userId").toString());
            }
        }
        optionFilter = new OptionFilter();
        optionFilter.addFilter(RBACUtil.USER_SCOPE_QUERY, scopeMap.get(RBACUtil.SCOPE_KEY_USER_VIEW));
        List<User> userList = userDal.getUsersNotAssignToGroup(new Options(optionFilter));
        UsersInGroupJson userInGroupJsonUnAssigned = new UsersInGroupJson(-1, messageUtil.getMessage("rbacui.assignusertogroup.unassigned", null, null, null));
        result.add(userInGroupJsonUnAssigned);
        int indexUnAssignedGroup = result.indexOf(userInGroupJsonUnAssigned);
        if(userList!=null && !userList.isEmpty()){
            for(User user:userList){
                result.get(indexUnAssignedGroup).addUser(user.getUserId(), user.getUserName());
            }
        }
        optionFilter = new OptionFilter();
        optionFilter.addFilter(RBACUtil.GROUP_SCOPE_QUERY, scopeMap.get(RBACUtil.SCOPE_KEY_GROUP_VIEW));
        List<Map<String, Object>> groupIdNames = getGroupIdNames(new Options(optionFilter));
        if(groupIdNames!=null && !groupIdNames.isEmpty()){
            for(Map<String, Object> groupIdName:groupIdNames){
                result.add(new UsersInGroupJson(Integer.parseInt(groupIdName.get("groupId").toString()), groupIdName.get("name").toString()));
            }
        }
        else{
            return result;
        }
//        MultivaluedMap<String, String> multiValuedMap = new MultivaluedMapImpl();
        MultivaluedMap<String, String> multiValuedMap = new MultivaluedHashMap<>();
        if(scopeMap.get(RBACUtil.SCOPE_KEY_USER_VIEW)!=null){
            multiValuedMap.put(RBACUtil.USER_SCOPE_QUERY, Arrays.asList(new String[]{scopeMap.get(RBACUtil.SCOPE_KEY_USER_VIEW)}));
        }
        if(scopeMap.get(RBACUtil.SCOPE_KEY_GROUP_VIEW)!=null){
            multiValuedMap.put(RBACUtil.GROUP_SCOPE_QUERY, Arrays.asList(new String[]{scopeMap.get(RBACUtil.SCOPE_KEY_GROUP_VIEW)}));
        }
        Options options = new Options(new OptionFilter(multiValuedMap));
        Filters filters = prepareFilters(options);
        List<Object[]> queryResponse = filters
                .getList(
                        em,
                        Object[].class,
                        "select g.groupId, g.name, u.userId, u.userName from Group g left join User u on (u.groupId=g.groupId)",
                        options, SORT_COLUMNS);
        if(queryResponse!=null && queryResponse.size() > 0){
            for(Object[] obj:queryResponse){
                UsersInGroupJson userInGroupJson = new UsersInGroupJson(Integer.parseInt(obj[0].toString()), obj[1].toString());
                int index = result.indexOf(userInGroupJson);
                if(index==-1){
                    result.add(userInGroupJson);
                    index = result.indexOf(userInGroupJson);
                }
                if(obj[2]!=null && index!=-1){
                    if(userIds!=null && !userIds.isEmpty() && userIds.contains(obj[2].toString())){
                        result.get(index).addUser(Integer.parseInt(obj[2].toString()), obj[3].toString());
                    }
                }
            }
        }

        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Map<String, Object>> updateUsersInGroups(
            List<Map<String, Object>> userGroupList, Integer loggedInUserId) {
        cacheService.clearCache(CacheConfig.CLEAR_GROUP_CACHE);//CACHE:: CLEAR
        cacheService.clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);
        Set<String> notAssignedUserNames = new HashSet<String>();
        if (userGroupList != null && !userGroupList.isEmpty()) {
            Map<String,String> oldNewUserGroupIds = new LinkedHashMap<String,String>();
            for (Map<String, Object> userGroupEntry : userGroupList) {
                Integer groupIdToSet = Integer.parseInt(userGroupEntry.get(
                        "groupId").toString());
                if (groupIdToSet != null) {
                    if (groupIdToSet == -1) {
                        groupIdToSet = null;
                    } else {
//                        Group existingGroup = em
//                                .find(Group.class, groupIdToSet);
                        Group existingGroup = groupRepository.findById(groupIdToSet).orElse(null);
                        if (existingGroup == null) {
                            throw new IllegalArgumentException(
                                    "groupId invalid");
                        }
                    }
                    Integer userIdToUpdate = Integer.parseInt(userGroupEntry
                            .get("userId").toString());
                    if (userIdToUpdate != null) {
//                        User existingUser = em.find(User.class, userIdToUpdate);
                        User existingUser = userRepository.findById(userIdToUpdate).orElse(null);
                        if (existingUser == null) {
                            throw new IllegalArgumentException("userId invalid");
                        }
                        String userName = Lookup.getUserName(existingUser.getUserId());
                        if(groupIdToSet!=null){
                            oldNewUserGroupIds.put(userName+":new", Lookup.getGroupName(groupIdToSet));
                        }
                        else{
                            oldNewUserGroupIds.put(userName+":new", "");
                        }

                        if(existingUser.getGroupId()!=null){
                            oldNewUserGroupIds.put(userName+":old", Lookup.getGroupName(existingUser.getGroupId()));
                        }
                        else{
                            oldNewUserGroupIds.put(userName+":old", "");
                        }

                        if(groupIdToSet!=null){

                            // Call for Tenant Id check in User and Group starts here
                            boolean allowFlag = userDal.checkTenantIdInOrgAndGroup(existingUser.getOrganizationId(), groupIdToSet);
                            // Call for Tenant Id check in User and Group ends here
                            if(allowFlag){
                                existingUser.setGroupId(groupIdToSet);
//                                em.merge(existingUser);
                                userRepository.save(existingUser);
                            }
                            else{
                                notAssignedUserNames.add(existingUser.getUserName())	;
                            }

                        }else{
                            existingUser.setGroupId(groupIdToSet);
//                            em.merge(existingUser);
                            userRepository.save(existingUser);
                        }
                    }
                }
            }
            if(notAssignedUserNames!=null && !notAssignedUserNames.isEmpty()){
                ErrorInfoException errorInfo = new ErrorInfoException("userNameAssign",notAssignedUserNames.toString());
                errorInfo.getParameters().put("userNames", notAssignedUserNames.toString());
                log.info("updateUsersInGroups; userassignerrorInfo={}", errorInfo);
                throw errorInfo;
            }


            log.debug("updateUsersInGroups; oldNewUserGroupIds={};", oldNewUserGroupIds);
            if(oldNewUserGroupIds!=null && !oldNewUserGroupIds.isEmpty()){
                oldNewUserGroupIds.put("name", "AssignGroup");
                try{
					/*auditLogDal.create(AuditLogUtil.createAuditLog(loggedInUserId, "User", "Assign Group",
						 oldNewUserGroupIds));*/
                    //Optimized Audit Logger on a separate thread
                    auditLogDal.createAsyncLog(loggedInUserId, null, "User", "Assign Group", oldNewUserGroupIds);
                }
                catch(Exception e){
                    log.error("updateUsersInGroups; Exception={};", e);
                }
            }
        }
        return userGroupList;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<RolesInGroupJson> getRolesInGroupsData(Map<String, String> scopeMap) {
        List<RolesInGroupJson> result = new LinkedList<RolesInGroupJson>();
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
        optionFilter.addFilter(RBACUtil.ROLE_SCOPE_QUERY, scopeMap.get(RBACUtil.SCOPE_KEY_ROLE_VIEW));
        List<Role> roleList = roleDal.getRolesNotAssignedToAnyGroup(new Options(optionFilter));
        RolesInGroupJson roleInGroupJsonUnAssigned = new RolesInGroupJson(-1, messageUtil.getMessage("rbacui.assignroletogroup.unassigned", null, null, null));
        result.add(roleInGroupJsonUnAssigned);
        int indexUnAssignedGroup = result.indexOf(roleInGroupJsonUnAssigned);
        if(roleList!=null && !roleList.isEmpty()){
            for(Role role:roleList){
                result.get(indexUnAssignedGroup).addRole(role.getRoleId(), role.getName(), Lookup.getApplicationName(role.getApplicationId()));
            }
        }
        optionFilter = new OptionFilter();
        optionFilter.addFilter(RBACUtil.GROUP_SCOPE_QUERY, scopeMap.get(RBACUtil.SCOPE_KEY_GROUP_VIEW));
        List<Map<String, Object>> groupIdNames = getGroupIdNames(new Options(optionFilter));
        if(groupIdNames!=null && !groupIdNames.isEmpty()){
            for(Map<String, Object> groupIdName:groupIdNames){
                result.add(new RolesInGroupJson(Integer.parseInt(groupIdName.get("groupId").toString()), groupIdName.get("name").toString()));
            }
        }
        else{
            return result;
        }
//        MultivaluedMap<String, String> multiValuedMap = new MultivaluedMapImpl();
        MultivaluedMap<String, String> multiValuedMap = new MultivaluedHashMap<>();
        if(scopeMap.get(RBACUtil.SCOPE_KEY_ROLE_VIEW)!=null){
            multiValuedMap.put(RBACUtil.ROLE_SCOPE_QUERY, Arrays.asList(new String[]{scopeMap.get(RBACUtil.SCOPE_KEY_ROLE_VIEW)}));
        }
        if(scopeMap.get(RBACUtil.SCOPE_KEY_GROUP_VIEW)!=null){
            multiValuedMap.put(RBACUtil.GROUP_SCOPE_QUERY, Arrays.asList(new String[]{scopeMap.get(RBACUtil.SCOPE_KEY_GROUP_VIEW)}));
        }
        Options options = new Options(new OptionFilter(multiValuedMap));
        Filters filters = prepareFilters(options);
        List<Object[]> queryResponse = filters
                .getList(
                        em,
                        Object[].class,
                        "select g.groupId, g.name, gr.roleId, r.name ,r.applicationId from Group g left join GroupRole gr on(g.groupId=gr.groupId) left join Role r on(gr.roleId=r.roleId)"
                        ,options, SORT_COLUMNS);
        if(queryResponse!=null && queryResponse.size() > 0){
            for(Object[] obj:queryResponse){
                RolesInGroupJson roleInGroupJson = new RolesInGroupJson(Integer.parseInt(obj[0].toString()), obj[1].toString());
                int index = result.indexOf(roleInGroupJson);
                if(index==-1){
                    result.add(roleInGroupJson);
                    index = result.indexOf(roleInGroupJson);
                }
                if(obj[2]!=null && index!=-1){
                    if(roleIds!=null && !roleIds.isEmpty() && roleIds.contains(obj[2].toString())){
                        result.get(index).addRole(Integer.parseInt(obj[2].toString()), obj[3].toString(),Lookup.getApplicationName(Integer.parseInt(obj[4].toString())));
                    }
                }
            }
        }

        return result;

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<String, List<String>> getGroupRoleScopeNames(int groupId) {
//        Group group = em.find(Group.class, groupId);
        Group group = groupRepository.findById(groupId).orElse(null);
        if(group==null)
            throw new ErrorInfoException("No Group Role Scopes Names Found For the id "+groupId);
        Set<Integer> roleIds = group.getRolesIds();
        Set<ScopeDefinition> scopeDefinitions  =group.getScopeDefinitions();
        Map<String, List<String>> groupRoleScopeList = new HashMap<String, List<String>>();
        List<String> roleList = new ArrayList<String>();
        List<String> scopeList = new ArrayList<String>();

        if(roleIds!=null){
            for(Integer roleId : roleIds){
                String roleName = Lookup.getRoleName(roleId);
                roleList.add(roleName);
            }
        }
        if(scopeDefinitions!=null){
            for(ScopeDefinition scopeDefinition : scopeDefinitions){
                Integer scopeId = scopeDefinition.getScopeId();
                String scopeName = Lookup.getScopeName(scopeId);
                scopeList.add(scopeName);
            }
        }

        groupRoleScopeList.put("roles",roleList);
        groupRoleScopeList.put("scopes",scopeList);

        return groupRoleScopeList;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<Integer, Set<String>> getRoleNamesInGroups(Set<Integer> groupIds){
        Map<Integer, Set<String>> result = new LinkedHashMap<Integer, Set<String>>();
        if(groupIds!=null && !groupIds.isEmpty()){
            //TypedQuery<Object[]> dbResultList = em.createNamedQuery("getRoleNamesInGroups", Object[].class);
            //dbResultList.setParameter("groupIds", groupIds);
            //List<Object[]> resultList = dbResultList.getResultList();
            //to prevent stack overflow error for eclipselink join clause
            for (List<Integer> groupIdsSubSet : Iterables.partition(groupIds, 500)) {
                TypedQuery<Object[]> list = em.createQuery("select gt.groupId, r.name from Group gt "
                        + " join GroupRole gr on (gr.groupId=gt.groupId) join Role r on (gr.roleId = r.roleId) "
                        + " where  gt.groupId IN ( "+ StringUtils.join(groupIdsSubSet,",") + ") " ,Object[].class);
                List<Object[]> objectList = list.getResultList();

                if(objectList!=null && !objectList.isEmpty()){
                    for(Object[] obj:objectList){
                        Integer groupId = null;
                        try{
                            if(obj[0]!=null){
                                groupId = Integer.parseInt(obj[0].toString());
                                if(!result.containsKey(groupId)){
                                    result.put(groupId, new TreeSet<String>());
                                }
                                result.get(groupId).add(obj[1].toString());
                            }
                        }
                        catch(ArrayIndexOutOfBoundsException e){
                            //ignored
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<Group> getTemplateGroups(Options options) {
        Filters filters = prepareFilters(options);
        filters.addCondition(" g.isTemplate = TRUE ");
        return filters.getList(em, Group.class, "select g from Group g",
                options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Integer removeScopeDefByScopeKeyExceptGroups(String scopeKey, List<Integer> groupIds) {
        cacheService.clearCache(CacheConfig.CLEAR_GROUP_CACHE);//CACHE:: CLEAR
        if(groupIds!=null && !groupIds.isEmpty()){
            Query query = em.createQuery("delete from ScopeDefinition sd where sd.scopeId in "+
                    "  (select s.scopeId from Scope s where s.scopeKey=:scopeKey) and sd.groupId not in ( "+StringUtils.join(groupIds,",") + ") ");
            query.setParameter("scopeKey", scopeKey);
            //query.setParameter("groupIds", groupIds);
            int result = query.executeUpdate();
            em.getEntityManagerFactory().getCache().evict(ScopeDefinition.class);
            return result;
        }
        return 0;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Integer removeScopeDefByScopeKey(String scopeKey) {
        cacheService.clearCache(CacheConfig.CLEAR_GROUP_CACHE);//CACHE:: CLEAR
        Query query = em.createQuery("delete from ScopeDefinition sd where sd.scopeId in "+
                "  (select s.scopeId from Scope s where s.scopeKey=:scopeKey) ");
        query.setParameter("scopeKey", scopeKey);
        int result = query.executeUpdate();
        em.getEntityManagerFactory().getCache().evict(ScopeDefinition.class);
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Number getScopeDefCountByScopeKey(String scopeKey) {
//        TypedQuery<Number> query = em.createNamedQuery("getScopeDefCountByScopeKey", Number.class);
//        query.setParameter("scopeId", ScopeLookup.getScopeIdByKey(scopeKey));
//        return query.getSingleResult();
        return scopeDefinitionRepository.getScopeDefCountByScopeKey(Lookup.getScopeIdByKey(scopeKey));
    }

    @Override
    @Cacheable(value = CacheConfig.USERS_IN_GROUP_CACHE, unless = "#result == null")
    public Set<UsersInGroupJson.UserJson> getUserListByGroupId(String userName, Integer groupId, Integer userId) throws Exception {
        if(groupId == null || groupId <= 0) {
            User user = null;
            if(userName != null && !userName.isEmpty())
                user = userDal.getByUserName(userName);
            else if(userId != null && userId > 0)
                user = userDal.getById(userId);

            if(user != null && user.getGroupId() > 0)
                groupId = user.getGroupId();
        }

        if(groupId == null || groupId <= 0)
            throw new Exception("No Group Id found in the request");

        List<User> userList = getUsersInGroup(groupId);
        Set<UsersInGroupJson.UserJson> userJsonSet = new HashSet<UsersInGroupJson.UserJson>();
        if(userList != null && !userList.isEmpty()) {
            for(User e: userList)
                userJsonSet.add(new UsersInGroupJson.UserJson(e.getUserId(),e.getUserName()));
        }
        log.debug("{} users in groupId {}", userJsonSet != null ? userJsonSet.size():0,groupId);
        return userJsonSet;
    }

    public List<User> getUsersInGroup(Integer groupId) {
        try {
//            TypedQuery<User> query = em.createNamedQuery("getUserByGroupId", User.class);
//            query.setParameter("groupId", groupId);
//            return query.getResultList();
            return userRepository.getUserByGroupId(groupId);
        } catch (NoResultException e) {
            return null;
        }
    }



    private Map<String, String> setNewObjectChangeSetLocal(Group newGroup) {
        //clearObjectChangeSet();
        AuditLogHelperUtil logHelperUtil =  new AuditLogHelperUtil();
        logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_GROUPID, newGroup.getGroupId().toString());
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_GROUPNAME, newGroup.getName(), "", null, null);

        if(newGroup.getAttributesData() != null && newGroup.getAttributesData().size() > 0){
            for(AttributesData newAD: newGroup.getAttributesData()){
                logHelperUtil.checkPutToObjectChangeSet(Lookup.getMasterAttributeNameById(newAD.getAttributeId()), newAD.getAttributeDataValue(), null, null, null);
            }
            //checkPutToObjectChangeSet(OBJECTCHANGES_GROUP_ATTRIBUTES, newAttrData, oldAttrData, null, null);
        }
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_GROUP_ISTEMPLATE, newGroup.getIsTemplate(), "", null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_DESCRIPTION, newGroup.getDescription(), "", null, null);
        if (newGroup.getTenantId() != null) {
            logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_TENANT,
                    Lookup.getTenantNameById(newGroup.getTenantId()), "", null,
                    null);
        }
        if(newGroup.getScopeDefinitions()!=null && !newGroup.getScopeDefinitions().isEmpty()){
            logHelperUtil.checkScopeNamePutToObjectChangeSet(newGroup.getScopeDefinitions(), null);
        }
        if(newGroup.getLabels() != null && newGroup.getLabels().size() > 0)
            logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_LABELS, newGroup.getLabels(), "", null, null);

        return logHelperUtil.getObjectChangeSet();
    }

    private Map<String, String> setObjectChangeSetLocal(Group oldGroup, Group newGroup) {
        //clearObjectChangeSet();
        AuditLogHelperUtil logHelperUtil =  new AuditLogHelperUtil();

        logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_GROUPID, newGroup.getGroupId().toString());
        logHelperUtil.putToObjectChangeSet(OBJECTNAME, oldGroup.getName());
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_GROUPNAME, newGroup.getName(), oldGroup.getName(), null, null);

        if((newGroup.getAttributesData() != null && newGroup.getAttributesData().size() > 0) && (oldGroup.getAttributesData() != null && oldGroup.getAttributesData().size() > 0)){
            for(AttributesData newAD: newGroup.getAttributesData()){
                for(AttributesData oldAD: oldGroup.getAttributesData()){
                    if(oldAD.getAttributeDataId() == newAD.getAttributeDataId()){
                        logHelperUtil.checkPutToObjectChangeSet(Lookup.getMasterAttributeNameById(newAD.getAttributeId()), newAD.getAttributeDataValue(), oldAD.getAttributeDataValue(), null, null);
                        break;
                    }
                }
            }
        }
        if((newGroup.getAttributesData() != null && newGroup.getAttributesData().size() > 0)){
            for(AttributesData newAD: newGroup.getAttributesData()){
                if(oldGroup.getAttributesData() != null && !oldGroup.getAttributesData().contains(newAD))
                    logHelperUtil.checkPutToObjectChangeSet(Lookup.getMasterAttributeNameById(newAD.getAttributeId()), newAD.getAttributeDataValue(), null, null, null);
            }
        }
        if((oldGroup.getAttributesData() != null && oldGroup.getAttributesData().size() > 0)){
            for(AttributesData oldAD: oldGroup.getAttributesData()){
                if(newGroup.getAttributesData() != null && !newGroup.getAttributesData().contains(oldAD))
                    logHelperUtil.checkPutToObjectChangeSet(Lookup.getMasterAttributeNameById(oldAD.getAttributeId()), null, oldAD.getAttributeDataValue(), null, null);
            }
        }

        if(newGroup.getIsTemplate()!=oldGroup.getIsTemplate()){
            logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_GROUP_ISTEMPLATE, newGroup.getIsTemplate(), oldGroup.getIsTemplate(), null, null);
        }
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_DESCRIPTION, newGroup.getDescription(), oldGroup.getDescription(), null, null);
        logHelperUtil.checkPutToObjectChangeSet(
                OBJECTCHANGES_TENANT,
                newGroup.getTenantId() != null ? Lookup
                        .getTenantNameById(newGroup.getTenantId()) : null,
                oldGroup.getTenantId() != null ? Lookup
                        .getTenantNameById(oldGroup.getTenantId()) : null,
                null, null);
        if(newGroup.getLabels().size() > 0 || oldGroup.getLabels().size() > 0 )
            logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_LABELS, newGroup.getLabels(), oldGroup.getLabels(), null, null);
        // checkPutToObjectChangeSet(OBJECTCHANGES_SCOPENAME, newGroup.getScopeDefinitions().values(), oldGroup.getScopeDefinitions(), null, null);
        logHelperUtil.checkScopeNamePutToObjectChangeSet(newGroup.getScopeDefinitions(), oldGroup.getScopeDefinitions());
        logHelperUtil.checkRoleIdsPutToObjectChangeSet(newGroup.getRolesIds(), oldGroup.getRolesIds());
        //checkMapPutToObjectChangeSet(Variable.convertSetOfVariablesToMap(newGroup.getVariables()), Variable.convertSetOfVariablesToMap(oldGroup.getVariables()));
        logHelperUtil.checkRestrictionPutToObjectChangeSet(newGroup.getRestrictions(), oldGroup.getRestrictions());
        return logHelperUtil.getObjectChangeSet();
    }
}
