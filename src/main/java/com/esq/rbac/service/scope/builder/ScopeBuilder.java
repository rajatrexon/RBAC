package com.esq.rbac.service.scope.builder;

import com.esq.rbac.service.application.service.ApplicationDal;
import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.codes.repository.CodeRepository;
import com.esq.rbac.service.group.service.GroupDal;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.masterattributes.domain.MasterAttributes;
import com.esq.rbac.service.masterattributes.repository.MasterAttributesRepository;
import com.esq.rbac.service.role.service.RoleDal;
import com.esq.rbac.service.scope.builder.util.ScopeGenerator;
import com.esq.rbac.service.scope.scopeBuilderdefault.domain.ScopeBuilderDefault;
import com.esq.rbac.service.scope.scopeBuilderdefault.repository.ScopeBuildDefaultRepository;
import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import com.esq.rbac.service.scope.scopeconstraint.service.ScopeConstraintDal;
import com.esq.rbac.service.tenant.service.TenantDal;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.util.externaldatautil.ExternalDataAccessScopeBuilder;
import com.esq.rbac.service.util.externaldatautil.ExternalDataAccessUtil;
import com.esq.rbac.service.variable.repository.VariableRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class ScopeBuilder {

//    public static ArrayNode tenantScopeToJson(String input){
//        try{
//            JsonNode tenantMap = new ObjectMapper().readTree(input);
//            if(tenantMap!=null){
//                JsonNode jsonRules = tenantMap.get("rules").get(0);
//                if(jsonRules!=null){
//                    return (ArrayNode) jsonRules.get("value");
//                }
//            }
//        }
//        catch (Exception e) {
//            log.error("tenantScopeToJson; Exception={};", e);
//        }
//        return null;
//    }


    private Map<String, List<Map<String, Object>>> filterMapList = new HashMap<String, List<Map<String, Object>>>();
    private Map<String, Object> lhsLabelMap = new HashMap<String, Object>();
    private Map<String, Object> rhsLabelMap = new HashMap<String, Object>();
    private Map<String, Object> rhsAttributesLabelMap = new HashMap<String, Object>();
    private final ConcurrentMap<String, ScopeConstraint> scopeConstraintMap = new ConcurrentHashMap<String, ScopeConstraint>();

    private static final String ENABLE_USER_VARIABLES_IN_SCOPE = "enable.user.variables.scope";
    private static final String ENABLE_GROUP_VARIABLES_IN_SCOPE = "enable.group.variables.scope";
    private static final String SCOPE_BUILDER_ID = "id";
    private static final String SCOPE_BUILDER_DATA_KEY = "dataKey";
    private static final String SCOPE_BUILDER_LABEL = "label";
    private static final String SCOPE_BUILDER_TYPE = "type";
    private static final String SCOPE_BUILDER_INPUT = "input";
    private static final String SCOPE_BUILDER_MULTIPLE = "multiple";
    @SuppressWarnings("unused")
    private static final String SCOPE_BUILDER_PREFIX = "prefix";
    private static final String SCOPE_BUILDER_SUFFIX = "suffix";
    private static final String SCOPE_BUILDER_OPERATORS = "operators";
    private static final String SCOPE_BUILDER_SOURCE = "source";
    private static final String SCOPE_BUILDER_LABEL_DATA = "labelData";
    private static final String SCOPE_BUILDER_RBAC_TYPE = "rbacType";
    private static final String SCOPE_BUILDER_LABEL_DATA_RHS = "rhs";
    private static final String ATTRIBUTE_USER_TYPE = "userAttribute";
    private static final String ATTRIBUTE_USER_GROUP_TYPE = "userGroupAttribute";
    private static final String ATTRIBUTE_GROUP_TYPE = "groupAttribute";

    private EntityManager em;
    @Autowired
    private GroupDal groupDal;
    @Autowired
    private UserDal userDal;
    @Autowired
    private RoleDal roleDal;
    @Autowired
    private TenantDal tenantDal;
    @Autowired
    private ExternalDataAccessUtil externalDataAccessUtil;
    @Autowired
    private ScopeConstraintDal scopeConstraintDal;

    private Configuration configuration;


    @Autowired
    public ScopeBuilder(@Qualifier("DatabaseConfigurationWithCache") Configuration configuration){
        this.configuration=configuration;
    }
    private ScopeGenerator scopeGenerator;
    @Autowired
    private ApplicationDal applicationDal;
    //dummy
    public static final boolean enableDummyData = false;

    @Autowired
    private MasterAttributesRepository masterAttributesRepository;

    @Autowired
    private CodeRepository codeRepository;
    
    @Autowired
    private ScopeBuildDefaultRepository scopeBuildDefaultRepository;

    @Autowired
    private VariableRepository variableRepository;

//    @Autowired
//    public void setExternalDataAccessUtil(
//            ExternalDataAccessUtil externalDataAccessUtil) {
//        log.trace("setExternalDataAccessUtil; externalDataAccessUtil={}",
//                externalDataAccessUtil);
//        this.externalDataAccessUtil = externalDataAccessUtil;
//    }

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager; {}", em);
        this.em = em;
    }

    @EventListener
    public void fillMasterAttributeNameLookupTable(ApplicationStartedEvent event){
        log.trace("fillMasterAttributeNameLookUpTable");
        Lookup.fillMasterAttributeNameLookupTable(getAllMasterAttributes());
    }





//    @Autowired
//    public void setApplicationDal(ApplicationDal applicationDal) {
//        log.trace("setApplicationDal; applicationDal={}", applicationDal);
//        this.applicationDal = applicationDal;
//    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    private List<MasterAttributes> getAllMasterAttributes() {
        TypedQuery<MasterAttributes> query = em.createQuery(
                "select m from MasterAttributes m", MasterAttributes.class);
        return query.getResultList();
//        System.out.println("inside the ScopeBuilder.java");
//        System.out.println("masterAttributesRepository : : "+this.masterAttributesRepository.toString());
//        return masterAttributesRepository.findAll();
    }

//    @Autowired
//    public void setGroupDal(GroupDal groupDal) {
//        log.trace("setGroupDal; groupDal={}", groupDal);
//        this.groupDal = groupDal;
//    }

//    @Autowired
//    public void setUserDal(UserDal userDal) {
//        log.trace("setUserDal; userDal={}", userDal);
//        this.userDal = userDal;
//    }

//    @Autowired
//    public void setRoleDal(RoleDal roleDal) {
//        log.trace("setRoleDal; roleDal={}", roleDal);
//        this.roleDal = roleDal;
//    }

//    @Autowired
//    public void setTenantDal(TenantDal tenantDal) {
//        log.trace("tenantDal; tenantDal={}", tenantDal);
//        this.tenantDal = tenantDal;
//    }

//    @Autowired
//    public void setScopeConstraintDal(ScopeConstraintDal scopeConstraintDal) {
//        log.trace("setScopeConstraintDal; scopeConstraintDal={}",
//                scopeConstraintDal);
//        this.scopeConstraintDal = scopeConstraintDal;
//    }

    @Autowired
    public void setScopeGenerator(ScopeGenerator scopeGenerator) {
        log.trace("setScopeGenerator; scopeGenerator={}", scopeGenerator);
        this.scopeGenerator = scopeGenerator;
    }

    public void setPropertyConfiguration(Configuration configuration) {
        log.trace("setPropertyConfiguration; configuration={};", configuration);
        this.configuration = configuration;
    }

    private synchronized void addDefaultScopeBuilderEntries(
            Map<String, Object> filterMap, String label) {
        filterMap.put(SCOPE_BUILDER_TYPE, "string");
        filterMap.put(SCOPE_BUILDER_INPUT, "select");
        filterMap.put(SCOPE_BUILDER_MULTIPLE, "true");
        filterMap.put(SCOPE_BUILDER_SUFFIX, ") ");
        filterMap.put(SCOPE_BUILDER_OPERATORS, new String[]{"in", "not_in"});
        filterMap.put(SCOPE_BUILDER_LABEL, label);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Code> getScopedCodes() {
//        TypedQuery<Code> codeQuery = em.createQuery(
//                "select c from Code c where c.scopeData is not null",
//                Code.class);
//        return codeQuery.getResultList();
        return codeRepository.getAllCodesWithScope();
    }

    @SuppressWarnings("unchecked")
    private synchronized void buildScopeFilterForCode(
            Map<String, Object> filterMap, Code code,
            String codeType) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> filterMapDatabase = null;
        try {
            filterMapDatabase = mapper.readValue(code.getScopeData(),
                    Map.class);
        } catch (Exception e) {
            log.error("buildScopeFilterForCode; Exception={}", e);
        }
        filterMap.putAll(filterMapDatabase);
        filterMap.put(SCOPE_BUILDER_DATA_KEY, code.getCodeId());
        filterMap.put(SCOPE_BUILDER_SOURCE, "/codes");
        filterMap.put(SCOPE_BUILDER_RBAC_TYPE, codeType);
        lhsLabelMap.put(filterMap.get(SCOPE_BUILDER_ID).toString(),
                filterMapDatabase.get(SCOPE_BUILDER_LABEL_DATA));
        rhsAttributesLabelMap.put(code.getCodeId().toString(),
                filterMapDatabase.get(SCOPE_BUILDER_LABEL_DATA_RHS));
        filterMap.remove(SCOPE_BUILDER_LABEL_DATA);
        filterMap.remove(SCOPE_BUILDER_LABEL_DATA_RHS);

    }

    @SuppressWarnings("unchecked")
    private synchronized void buildScopeFilterForAttribute(
            Map<String, Object> filterMap, MasterAttributes attribute,
            String attributeType) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> filterMapDatabase = null;
        try {
            filterMapDatabase = mapper.readValue(attribute.getScopeData(),
                    Map.class);
        } catch (Exception e) {
            log.error("buildScopeFilterForAttribute; Exception={}", e);
        }
        filterMap.putAll(filterMapDatabase);
        filterMap.put(SCOPE_BUILDER_DATA_KEY, attribute.getAttributeId());
        filterMap.put(SCOPE_BUILDER_SOURCE, "/attributes");
        filterMap.put(SCOPE_BUILDER_RBAC_TYPE, attributeType);
        lhsLabelMap.put(filterMap.get(SCOPE_BUILDER_ID).toString(),
                filterMapDatabase.get(SCOPE_BUILDER_LABEL_DATA));
        rhsAttributesLabelMap.put(attribute.getAttributeId().toString(),
                filterMapDatabase.get(SCOPE_BUILDER_LABEL_DATA_RHS));
        filterMap.remove(SCOPE_BUILDER_LABEL_DATA);
        filterMap.remove(SCOPE_BUILDER_LABEL_DATA_RHS);

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private synchronized void populateDefaultRhsLabels(
            ScopeBuilderDefault scopeBuilderDefault, String scopeKey) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (scopeBuilderDefault.getRhsJson() != null
                    && !scopeBuilderDefault.getRhsJson().isEmpty()) {
                if (rhsLabelMap.containsKey(scopeKey)) {
                    Map<String, Object> innerJson = mapper.readValue(
                            scopeBuilderDefault.getRhsJson(), Map.class);
                    if (innerJson != null && !innerJson.isEmpty()) {
                        for (String key : innerJson.keySet()) {
                            ((Map) rhsLabelMap.get(scopeKey)).put(key,
                                    innerJson.get(key));
                        }
                    }
                } else {
                    rhsLabelMap.put(scopeKey, mapper.readValue(
                            scopeBuilderDefault.getRhsJson(), Map.class));
                }
            }
        } catch (Exception e) {
            log.error("populateDefaultRhsLabels; Exception={}", e);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void refreshScopeBuilder() {
        filterMapList.clear();
        lhsLabelMap.clear();
        rhsLabelMap.clear();
        rhsAttributesLabelMap.clear();

        List<Map<String, Object>> filterMapListUserView = new LinkedList<Map<String, Object>>();
        List<Map<String, Object>> filterMapListGroupView = new LinkedList<Map<String, Object>>();
        List<Map<String, Object>> filterMapListRoleView = new LinkedList<Map<String, Object>>();
        Map<String, List<Map<String, Object>>> filterMapListGlobalScopes = new LinkedHashMap<String, List<Map<String, Object>>>();

        Map<String, Object> filterMap = null;

        // add queries for user & group fields
        filterMap = new HashMap<String, Object>();

//        TypedQuery<ScopeBuilderDefault> query = em.createNamedQuery(
//                "getScopeBuilderDefaults", ScopeBuilderDefault.class);
//        List<ScopeBuilderDefault> scopeBuilderDefaultsList = query
//                .getResultList();
        List<ScopeBuilderDefault> scopeBuilderDefaultsList = scopeBuildDefaultRepository.getScopeBuilderDefaults();
        if (scopeBuilderDefaultsList != null
                && !scopeBuilderDefaultsList.isEmpty()) {
            for (ScopeBuilderDefault scopeBuilderDefault : scopeBuilderDefaultsList) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> filterMapDatabase = null;
                try {
                    filterMapDatabase = mapper.readValue(
                            scopeBuilderDefault.getLhsJson(), Map.class);
                } catch (Exception e) {
                    log.error("refreshScopeBuilder; Exception={}", e);
                }
                filterMap = new HashMap<String, Object>();
                filterMap.putAll(filterMapDatabase);
                if (scopeBuilderDefault.getScopeKey().equalsIgnoreCase(
                        RBACUtil.SCOPE_KEY_USER_VIEW)) {
                    filterMapListUserView.add(filterMap);
                    populateDefaultRhsLabels(scopeBuilderDefault,
                            RBACUtil.SCOPE_KEY_USER_VIEW);
                } else if (scopeBuilderDefault.getScopeKey().equalsIgnoreCase(
                        RBACUtil.SCOPE_KEY_GROUP_VIEW)) {
                    filterMapListGroupView.add(filterMap);
                    populateDefaultRhsLabels(scopeBuilderDefault,
                            RBACUtil.SCOPE_KEY_GROUP_VIEW);
                } else if (scopeBuilderDefault.getScopeKey().equalsIgnoreCase(
                        RBACUtil.SCOPE_KEY_ROLE_VIEW)) {
                    filterMapListRoleView.add(filterMap);
                    populateDefaultRhsLabels(scopeBuilderDefault,
                            RBACUtil.SCOPE_KEY_ROLE_VIEW);
                } else if (scopeBuilderDefault.getScopeKey().equalsIgnoreCase(
                        RBACUtil.SCOPE_KEY_TENANT)) {
                    if (!filterMapListGlobalScopes.containsKey(RBACUtil.SCOPE_KEY_TENANT)) {
                        filterMapListGlobalScopes.put(RBACUtil.SCOPE_KEY_TENANT, new LinkedList<Map<String, Object>>());
                    }
                    filterMapListGlobalScopes.get(RBACUtil.SCOPE_KEY_TENANT).add(filterMap);
                    populateDefaultRhsLabels(scopeBuilderDefault,
                            RBACUtil.SCOPE_KEY_TENANT);
                } else if (scopeBuilderDefault.getScopeKey().equalsIgnoreCase(
                        RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS)) {
                    if (!filterMapListGlobalScopes.containsKey(RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS)) {
                        filterMapListGlobalScopes.put(RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS, new LinkedList<Map<String, Object>>());
                    }
                    filterMapListGlobalScopes.get(RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS).add(filterMap);
                    populateDefaultRhsLabels(scopeBuilderDefault,
                            RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS);
                }
                lhsLabelMap.put(filterMap.get(SCOPE_BUILDER_ID).toString(),
                        filterMapDatabase.get(SCOPE_BUILDER_LABEL_DATA));
                filterMap.remove(SCOPE_BUILDER_LABEL_DATA);
            }
        }

        // add queries for attributes
//        TypedQuery<MasterAttributes> query1 = em
//                .createQuery(
//                        "select m from MasterAttributes m where m.isEnabled = TRUE",
//                        MasterAttributes.class);
//        List<MasterAttributes> attributesList = query1.getResultList();
        List<MasterAttributes> attributesList = masterAttributesRepository.getAllEnabledMasterAttributes();

        if (attributesList != null && !attributesList.isEmpty()) {
            for (MasterAttributes attribute : attributesList) {
                if (attribute.getIsUser() && attribute.getIsUserScopeable()) {
                    filterMap = new HashMap<String, Object>();
                    filterMap
                            .put(SCOPE_BUILDER_ID,
                                    " u.userId IN ( select a.user.userId from AttributesData a where a.attributeId= "
                                            + attribute.getAttributeId()
                                            + " and a.attributeDataValue ");
                    filterMap.put(SCOPE_BUILDER_SUFFIX, ") ");
                    buildScopeFilterForAttribute(filterMap, attribute,
                            ATTRIBUTE_USER_TYPE);
                    filterMapListUserView.add(filterMap);
                }
                if (attribute.getIsGroup() && attribute.getIsGroupScopeable()) {
                    filterMap = new HashMap<String, Object>();
                    filterMap
                            .put(SCOPE_BUILDER_ID,
                                    "u.groupId IN ( select g.groupId from Group g where g.attributesData IN ( select a from AttributesData a where a.attributeId= "
                                            + attribute.getAttributeId()
                                            + " and a.attributeDataValue ");

                    filterMap.put(SCOPE_BUILDER_SUFFIX, " ) ) ");
                    buildScopeFilterForAttribute(filterMap, attribute,
                            ATTRIBUTE_USER_GROUP_TYPE);
                    filterMapListUserView.add(filterMap);

                    filterMap = new HashMap<String, Object>();
                    filterMap
                            .put(SCOPE_BUILDER_ID,
                                    "g.groupId IN ( select a.group.groupId from AttributesData a where a.attributeId= "
                                            + attribute.getAttributeId()
                                            + " and a.attributeDataValue ");
                    filterMap.put(SCOPE_BUILDER_SUFFIX, ") ");
                    buildScopeFilterForAttribute(filterMap, attribute,
                            ATTRIBUTE_GROUP_TYPE);
                    filterMapListGroupView.add(filterMap);
                }
                if (attribute.getScopeData() != null && !attribute.getScopeData().isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> scopeDataMap = mapper.readValue(attribute.getScopeData(),
                                Map.class);
                        if (scopeDataMap != null && !scopeDataMap.isEmpty()) {
                            List<String> globalScopeListForAttr = (List<String>) scopeDataMap.get("globalScopes");
                            if (globalScopeListForAttr != null && !globalScopeListForAttr.isEmpty()) {
                                for (String scopeKeyInGlobal : globalScopeListForAttr) {
                                    filterMap = new HashMap<String, Object>();
                                    filterMap
                                            .put(SCOPE_BUILDER_ID,
                                                    scopeDataMap.get(SCOPE_BUILDER_ID));

                                    //filterMap.put(SCOPE_BUILDER_SUFFIX, " ) ) ");
                                    buildScopeFilterForAttribute(filterMap, attribute,
                                            attribute.getAttributeName());
                                    if (!filterMapListGlobalScopes.containsKey(scopeKeyInGlobal)) {
                                        filterMapListGlobalScopes.put(scopeKeyInGlobal, new LinkedList<Map<String, Object>>());
                                    }
                                    filterMapListGlobalScopes.get(scopeKeyInGlobal).add(filterMap);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("refreshScopeBuilder; Exception={}", e);
                    }

                }


            }
        }

        // add scope data for codes
        List<Code> codesList = getScopedCodes();
        if (codesList != null && !codesList.isEmpty()) {
            for (Code code : codesList) {
                if (code.getScopeData() != null
                        && !code.getScopeData().isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> scopeDataMap = mapper.readValue(
                                code.getScopeData(), Map.class);
                        if (scopeDataMap != null && !scopeDataMap.isEmpty()) {
                            List<String> globalScopeListForAttr = (List<String>) scopeDataMap
                                    .get("globalScopes");
                            if (globalScopeListForAttr != null
                                    && !globalScopeListForAttr.isEmpty()) {
                                for (String scopeKeyInGlobal : globalScopeListForAttr) {
                                    filterMap = new HashMap<String, Object>();
                                    filterMap.put(SCOPE_BUILDER_ID,
                                            scopeDataMap.get(SCOPE_BUILDER_ID));

                                    // filterMap.put(SCOPE_BUILDER_SUFFIX,
                                    // " ) ) ");
                                    buildScopeFilterForCode(filterMap, code,
                                            code.getCodeType());
                                    if (filterMapListGlobalScopes
                                            .containsKey(scopeKeyInGlobal) == false) {
                                        filterMapListGlobalScopes
                                                .put(scopeKeyInGlobal,
                                                        new LinkedList<Map<String, Object>>());
                                    }
                                    filterMapListGlobalScopes.get(
                                            scopeKeyInGlobal).add(filterMap);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("refreshScopeBuilder; Exception={}", e);
                    }

                }
            }
        }

        if (configuration.getString(ENABLE_USER_VARIABLES_IN_SCOPE,
                "false").equalsIgnoreCase("true")) {
            // add queries for variables
//            TypedQuery<String> queryUserVariables = em
//                    .createQuery(
//                            "select distinct(v.variableName) from Variable v where v.userId is NOT NULL",
//                            String.class);
//            List<String> userVariables = queryUserVariables.getResultList();
            List<String> userVariables = variableRepository.getVariableNameWithUserIdNotNull();

            if (userVariables != null && !userVariables.isEmpty()) {
                for (String variableName : userVariables) {
                    filterMap = new HashMap<String, Object>();
                    filterMap.put(SCOPE_BUILDER_ID,
                            "u.userId IN (select v.userId from Variable v where v.variableName ='"
                                    + variableName + "' and v.variableValue  ");
                    addDefaultScopeBuilderEntries(filterMap, "User's Variable "
                            + variableName);
                    filterMap.put(SCOPE_BUILDER_DATA_KEY, variableName);
                    filterMap.put(SCOPE_BUILDER_SOURCE, "/variables");
                    filterMapListUserView.add(filterMap);
                }
            }
        }
        if (configuration.getString(ENABLE_GROUP_VARIABLES_IN_SCOPE,
                "false").equalsIgnoreCase("true")) {
//            TypedQuery<String> queryGroupVariables = em
//                    .createQuery(
//                            "select distinct(v.variableName) from Variable v where v.groupId is NOT NULL",
//                            String.class);
//            List<String> groupVariables = queryGroupVariables.getResultList();
            List<String> groupVariables = variableRepository.getVariableNameWithGroupIdNotNull();
            if (groupVariables != null && !groupVariables.isEmpty()) {
                for (String variableName : groupVariables) {
                    filterMap = new HashMap<String, Object>();
                    filterMap
                            .put(SCOPE_BUILDER_ID,
                                    "u.groupId IN ( select g.groupId from Group g where g.groupId IN (select v.groupId from Variable v where v.variableName ='"
                                            + variableName
                                            + "' and v.variableValue  ");
                    addDefaultScopeBuilderEntries(filterMap,
                            "User Group's Variable " + variableName);
                    filterMap.put(SCOPE_BUILDER_DATA_KEY, variableName);
                    filterMap.put(SCOPE_BUILDER_SUFFIX, " ) ) ");
                    filterMap.put(SCOPE_BUILDER_SOURCE, "/variables");
                    filterMapListUserView.add(filterMap);
                }
            }
        }

        filterMapList.put(RBACUtil.SCOPE_KEY_USER_VIEW, filterMapListUserView);
        filterMapList.put(RBACUtil.SCOPE_KEY_GROUP_VIEW, filterMapListGroupView);
        filterMapList.put(RBACUtil.SCOPE_KEY_ROLE_VIEW, filterMapListRoleView);
        if (!filterMapListGlobalScopes.isEmpty()) {
            for (String scopeKey : filterMapListGlobalScopes.keySet()) {
                filterMapList.put(scopeKey, filterMapListGlobalScopes.get(scopeKey));
            }
        }
    }

    public String validateScopeBuilderOutput(String data, String scopeKey,
                                             String userName, String additionalMap) {
        ScopeConstraint scopeConstraint = getScopeConstraintByScopeKey(scopeKey);
        if (scopeConstraint != null) {
            if (scopeConstraint.getApplicationId() != null && scopeConstraint.getApplicationId() == RBACUtil.RBAC_APPLICATION_ID) {
                if (RBACUtil.SCOPE_KEY_USER_VIEW.equalsIgnoreCase(scopeKey)
                        || RBACUtil.SCOPE_KEY_GROUP_VIEW.equalsIgnoreCase(scopeKey)
                        || RBACUtil.SCOPE_KEY_ROLE_VIEW.equalsIgnoreCase(scopeKey)
                        || RBACUtil.SCOPE_KEY_TENANT.equalsIgnoreCase(scopeKey)
                        || RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS.equalsIgnoreCase(scopeKey)) {
                    if (data != null && !data.isEmpty()) {
                        try {
                            JsonNode jsonObject = new ObjectMapper().readTree(data);
                            String sql = jsonObject.path("sql").asText().toString();
                            if (sql != null && !sql.isEmpty()) {
                                String scope = sql;
                                User user = userDal.getByUserName(userName);
                                if (user != null) {
                                    scope = userDal.replaceRuntimeVariables(sql,
                                            userDal.getByUserName(userName),
                                            groupDal.getById(user.getGroupId()));
                                }
                                MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
                                try {
                                    if (RBACUtil.SCOPE_KEY_USER_VIEW.equalsIgnoreCase(scopeKey)) {
                                        queryParams.put(RBACUtil.USER_SCOPE_QUERY,
                                                Arrays.asList(new String[]{scope}));
                                        Options options = buildOptions(queryParams);
                                        userDal.getUserIdNames(options);
                                    } else if (RBACUtil.SCOPE_KEY_GROUP_VIEW
                                            .equalsIgnoreCase(scopeKey)) {
                                        queryParams.put(RBACUtil.GROUP_SCOPE_QUERY,
                                                Arrays.asList(new String[]{scope}));
                                        Options options = buildOptions(queryParams);
                                        groupDal.getGroupIdNames(options);
                                    } else if (RBACUtil.SCOPE_KEY_ROLE_VIEW
                                            .equalsIgnoreCase(scopeKey)) {
                                        queryParams.put(RBACUtil.ROLE_SCOPE_QUERY,
                                                Arrays.asList(new String[]{scope}));
                                        Options options = buildOptions(queryParams);
                                        roleDal.getRoleIdNames(options);
                                    } else if (RBACUtil.SCOPE_KEY_TENANT
                                            .equalsIgnoreCase(scopeKey)) {
                                        queryParams.put(RBACUtil.TENANT_SCOPE_QUERY,
                                                Arrays.asList(new String[]{scope}));
                                        Options options = buildOptions(queryParams);
                                        tenantDal.getTenantIdNames(options);
                                    } else if (RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS
                                            .equalsIgnoreCase(scopeKey)) {
                                        queryParams.put(RBACUtil.REVOKE_APP_ACCESS_SCOPE_QUERY,
                                                Arrays.asList(new String[]{scope}));
                                        Options options = buildOptions(queryParams);
                                        applicationDal.getApplicationIdNames(options);
                                    }
                                } catch (Exception e) {
                                    log.error(
                                            "validateScopeBuilderOutput; scopeKey={}, Exception={}",
                                            scopeKey, e);
                                    return "{\"isValid\":1}";
                                }
                                return "{\"isValid\":0,\"parsedScope\":" + data + "}";
                            }
                        } catch (Exception e) {
                            log.error(
                                    "validateScopeBuilderOutput; scopeKey={}, Exception={}",
                                    scopeKey, e);
                            return "{\"isValid\":1}";
                        }

                    }
                }
            } else if (scopeConstraint.getApplicationId() != null && scopeConstraint.getApplicationId() != RBACUtil.RBAC_APPLICATION_ID) {
                if (data != null && !data.isEmpty()) {
                    if (enableDummyData) {
                        return "{\"isValid\":0,\"parsedScope\":" + data + "}";
                    }
                    try {

                        JsonNode jsonObject = new ObjectMapper().readTree(data);
                        String scopeSql = jsonObject.path("sql").asText();
                        String scopeJson = jsonObject.path("sqlBuilderRules").asText() != "" ? jsonObject.path("sqlBuilderRules").asText() : jsonObject.path("sqlBuilderRules").toString();
                        String sql = ((ExternalDataAccessScopeBuilder) externalDataAccessUtil
                                .getExternalDataAccessMap()
                                .get(scopeConstraint.getSourceType())
                                .get(scopeConstraint.getApplicationName())).validateAndBuildQuery(scopeSql, scopeJson, scopeKey, userName, additionalMap);
                        try {

                            JsonNode tempIsValidCheck = new ObjectMapper().readTree(sql);
                            try {
                                int isValid = tempIsValidCheck.path("isValid").asInt();
                                String errorMessage = "";
                                try {
                                    errorMessage = tempIsValidCheck.path("errorMessage").asText();
                                } catch (Exception e) {
                                    //ignore
                                }
                                if (isValid == 1) {
                                    return "{\"isValid\":1, \"errorMessage\":\"" + errorMessage + "\"}";
                                }
                            } catch (Exception e) {
                                //ignore
                            }
                            try {
                                String isValid = tempIsValidCheck.path("isValid").asText();
                                String errorMessage = "";
                                try {
                                    errorMessage = tempIsValidCheck.path("errorMessage").asText();
                                } catch (Exception e) {
                                    //ignore
                                }
                                if ("1".equalsIgnoreCase(isValid)) {
                                    return "{\"isValid\":1, \"errorMessage\":\"" + errorMessage + "\"}";
                                }
                            } catch (Exception e) {
                                //ignore
                            }
                        } catch (Exception e) {
                            //ignore
                        }
                        if (sql != null) {
                            ((ObjectNode) jsonObject).put("sql", sql);
                            return "{\"isValid\":0,\"parsedScope\":" + jsonObject.toString() + "}";
                        }

                    } catch (Exception e) {
                        log.error(
                                "validateScopeBuilderOutput; scopeKey={}, Exception={}",
                                scopeKey, e);
                        return "{\"isValid\":1}";
                    }
                }
            } else {
                log.error("validateScopeBuilderOutput; Check Scope Constraint Entries;");
            }
        } else {
            log.error("validateScopeBuilderOutput; Check Scope Constraint Entries;");
        }
        return "{\"isValid\":0,\"parsedScope\":" + data + "}";
    }

    private static String getLangCode(String additionalMap) {
        String langCode = "default";
        try {
            JsonNode additionalMapJson = new ObjectMapper().readTree(additionalMap);
            if (additionalMapJson != null && additionalMapJson.size() > 0) {
                if (additionalMapJson.path("locale").asText() != null) {
                    langCode = additionalMapJson.path("locale").asText();
                }
            }
        } catch (Exception e) {
            log.error("getLangCode; Exception={}", e);
        }
        return langCode;
    }

    public static String getRbacType(String additionalMap) {
        String rbacType = null;
        try {
            JsonNode additionalMapJson = new ObjectMapper().readTree(additionalMap);
            if (additionalMapJson != null && additionalMapJson.size() > 0) {
                if (additionalMapJson.path("rbacType").asText() != null) {
                    rbacType = additionalMapJson.path("rbacType").asText();
                }
            }
        } catch (Exception e) {
            log.error("getRbacType; Exception={}", e);
        }
        return rbacType;
    }

    public String getFilters(String scopeKey, String userName,
                             String additionalMap) {
        String langCode = getLangCode(additionalMap);
        ScopeConstraint scopeConstraint = getScopeConstraintByScopeKey(scopeKey);
        if (scopeConstraint != null) {
            if (scopeConstraint.getApplicationId() != null && scopeConstraint.getApplicationId() != RBACUtil.RBAC_APPLICATION_ID) {
                //dummy
                //insert into rbac.scopeConstraint (applicationName, applicationId, scopeName, scopeId, sqlQuery, comparators, sourceType) values ('DISPATCHER', 200, 'Source', 6, '', 'QUERYBUILDER', 'REST')
                if (enableDummyData) {
                    return "[{\"subFields\":null,\"dataKey\":\"FieldDefId = 387\",\"id\":\"AssignedParty\",\"input\":\"select\",\"label\":\"Assigned Party\",\"multiple\":true,\"operators\":[\"equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"string\"},{\"subFields\":null,\"dataKey\":\"Performance\",\"id\":\"Performance\",\"input\":\"select\",\"label\":\"Performance\",\"multiple\":true,\"operators\":[\"equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"string\"},{\"subFields\":null,\"dataKey\":\"FieldDefId = 358\",\"id\":\"CreateTime\",\"input\":\"text\",\"label\":\"Create Time\",\"multiple\":false,\"operators\":[\"equal\",\"greater_or_equal\",\"less_or_equal\",\"is_null\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"datetime\"},{\"subFields\":null,\"dataKey\":\"FieldDefId = 354\",\"id\":\"IncidentId\",\"input\":\"text\",\"label\":\"Incident Id\",\"multiple\":false,\"operators\":[\"equal\",\"greater_or_equal\",\"less_or_equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"integer\"},{\"subFields\":null,\"dataKey\":\"FieldDefId = 390\",\"id\":\"IncidentType\",\"input\":\"select\",\"label\":\"Incident Type\",\"multiple\":true,\"operators\":[\"equal\",\"greater_or_equal\",\"less_or_equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"integer\"},{\"subFields\":null,\"dataKey\":\"FieldDefId = 393\",\"id\":\"IsLinked\",\"input\":\"select\",\"label\":\"Is Linked\",\"multiple\":true,\"operators\":[\"equal\",\"greater_or_equal\",\"less_or_equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"integer\"},{\"subFields\":null,\"dataKey\":\"FieldDefId = 392\",\"id\":\"ParentIncidentId\",\"input\":\"text\",\"label\":\"Parent Incident Id\",\"multiple\":false,\"operators\":[\"equal\",\"greater_or_equal\",\"less_or_equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"integer\"},{\"subFields\":null,\"dataKey\":\"FieldDefId = 362\",\"id\":\"Priority\",\"input\":\"select\",\"label\":\"Priority\",\"multiple\":true,\"operators\":[\"equal\",\"greater_or_equal\",\"less_or_equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"integer\"},{\"subFields\":[{\"subFields\":[{\"subFields\":null,\"dataKey\":\"FieldDefId = 299\",\"id\":\"SubSubStatusCode\",\"input\":\"select\",\"label\":\"SubSub Status\",\"multiple\":true,\"operators\":[\"equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"string\"}],\"dataKey\":\"FieldDefId = 242\",\"id\":\"SubStatusCode\",\"mandatory\":true,\"input\":\"select\",\"label\":\"SubStatus Status\",\"multiple\":true,\"operators\":[\"equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"string\"}],\"dataKey\":\"FieldDefId = 241\",\"id\":\"StatusCode\",\"input\":\"select\",\"label\":\"Status\",\"multiple\":true,\"operators\":[\"equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"string\"},{\"subFields\":null,\"dataKey\":\"FieldDefId = 391\",\"id\":\"TargetApps\",\"input\":\"select\",\"label\":\"Target Application\",\"multiple\":false,\"operators\":[\"equal\",\"is_null\",\"is_not_null\",\"in\",\"not_in\"],\"prefix\":\"\",\"suffix\":\"\",\"type\":\"string\"}]";
                }
                return ((ExternalDataAccessScopeBuilder) externalDataAccessUtil
                        .getExternalDataAccessMap()
                        .get(scopeConstraint.getSourceType())
                        .get(scopeConstraint.getApplicationName())).getFilters(
                        scopeKey, userName, additionalMap);
            } else if (scopeConstraint.getApplicationId() != null && scopeConstraint.getApplicationId() == RBACUtil.RBAC_APPLICATION_ID) {
                if (RBACUtil.SCOPE_KEY_USER_VIEW.equalsIgnoreCase(scopeKey)) {
                    return new Gson().toJson(populateRuntimeFilterData(
                            RBACUtil.SCOPE_KEY_USER_VIEW, langCode));
                } else if (RBACUtil.SCOPE_KEY_GROUP_VIEW.equalsIgnoreCase(scopeKey)) {
                    return new Gson().toJson(populateRuntimeFilterData(
                            RBACUtil.SCOPE_KEY_GROUP_VIEW, langCode));
                } else if (RBACUtil.SCOPE_KEY_ROLE_VIEW.equalsIgnoreCase(scopeKey)) {
                    return new Gson().toJson(populateRuntimeFilterData(
                            RBACUtil.SCOPE_KEY_ROLE_VIEW, langCode));
                } else if (RBACUtil.SCOPE_KEY_TENANT.equalsIgnoreCase(scopeKey)) {
                    return new Gson().toJson(populateRuntimeFilterData(
                            RBACUtil.SCOPE_KEY_TENANT, langCode));
                } else if (RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS.equalsIgnoreCase(scopeKey)) {
                    return new Gson().toJson(populateRuntimeFilterData(
                            RBACUtil.SCOPE_KEY_REVOKE_APPLICATION_ACCESS, langCode));
                }
                return new Gson().toJson(populateRuntimeFilterData(
                        scopeKey, langCode));
            } else {
                log.error("getFilters; Check Scope Constraint Entries;");
            }
        } else {
            log.error("getFilters; Check Scope Constraint Entries;");
        }
        return new Gson().toJson(filterMapList);
    }

    private ScopeConstraint getScopeConstraintByScopeKey(String scopeKey) {
        if (scopeConstraintMap.containsKey(scopeKey)) {
            return scopeConstraintMap.get(scopeKey);
        }
        List<ScopeConstraint> scopeConstraintList = scopeConstraintDal.getScopeConstraintsForQueryBuilder(scopeKey);
        //if only one entry is there, it has to be DEFAULTSCOPE or some entry with sourceType defined
        if (scopeConstraintList != null && !scopeConstraintList.isEmpty() && scopeConstraintList.size() == 1) {
            scopeConstraintMap.put(scopeKey, scopeConstraintList.get(0));
            return scopeConstraintList.get(0);
        }
        //if more than one entry is there, it has to be some entry with a scopeId
        else if (scopeConstraintList != null && !scopeConstraintList.isEmpty() && scopeConstraintList.size() > 1) {
            for (ScopeConstraint sc : scopeConstraintList) {
                if (sc.getScopeId() != null) {
                    scopeConstraintMap.put(Lookup.getScopeKey(sc.getScopeId()), sc);
                    //return sc;
                } else {
                    scopeConstraintMap.put(scopeKey, sc);
                }
            }
        }
        if (scopeConstraintMap.containsKey(scopeKey)) {
            return scopeConstraintMap.get(scopeKey);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private List<Map<String, Object>> populateRuntimeFilterData(
            String scopeKey, String langCode) {
        List<Map<String, Object>> userFilterList = filterMapList.get(scopeKey);
        for (Map<String, Object> filterMap : userFilterList) {
            if (filterMap.get(SCOPE_BUILDER_LABEL) == null) {
                filterMap.put(SCOPE_BUILDER_LABEL, ((Map) ((Map) ((Map) lhsLabelMap
                        .get(filterMap.get(SCOPE_BUILDER_ID))).get(scopeKey))
                        .get(filterMap.get(SCOPE_BUILDER_RBAC_TYPE))).get(langCode)
                        .toString());
            }
        }
        return userFilterList;
    }

    private Options buildOptions(MultivaluedMap<String, String> queryParams) {
        OptionPage optionPage = new OptionPage(queryParams, 0,
                Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(queryParams);
        OptionFilter optionFilter = new OptionFilter(queryParams);
        Options options = new Options(optionPage, optionSort, optionFilter);
        return options;
    }

    private Options buildOptions(MultivaluedMap<String, String> queryParams, String additionalMap) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference<HashMap<String, Object>>() {
        };
        HashMap<String, Object> otherFilters = mapper.readValue(additionalMap, typeRef);
        if (otherFilters != null && !otherFilters.isEmpty()) {
            for (String param : otherFilters.keySet()) {
                queryParams.putSingle(param, otherFilters.get(param).toString());
            }
        }
        OptionPage optionPage = new OptionPage(queryParams, 0,
                Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(queryParams);
        OptionFilter optionFilter = new OptionFilter(queryParams);
        Options options = new Options(optionPage, optionSort, optionFilter);
        return options;
    }

    @SuppressWarnings("rawtypes")
    public String getCurrentLabel(String scopeKey, String dataKey,
                                  String additionalMap) {
        String result = null;
        try {
            result = ((Map) ((Map) ((Map) rhsAttributesLabelMap.get(dataKey))
                    .get(scopeKey)).get(getRbacType(additionalMap))).get(
                    getLangCode(additionalMap)).toString();
        } catch (Exception e) {
            log.info(
                    "getCurrentLabel; label not found for scopeKey={}; dataKey={}; rbacType={}",
                    scopeKey, dataKey, getRbacType(additionalMap));
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    public String getFilterKeyData(String sourcePath, String dataKey,
                                   String scopeKey, String userName, String additionalMap,
                                   Map<String, String> scopeMap, String parentValue) throws Exception {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        ScopeConstraint scopeConstraint = getScopeConstraintByScopeKey(scopeKey);

        if (scopeConstraint != null) {
            if (scopeConstraint.getSourceType().equals("DYNAMIC")) {
                return scopeGenerator.getFilterKeyData(sourcePath, dataKey, scopeKey, userName, additionalMap, scopeMap, parentValue);
            }
            if (scopeConstraint.getApplicationId() != null && scopeConstraint.getApplicationId() != RBACUtil.RBAC_APPLICATION_ID) {

                // dummy
                if (enableDummyData) {
                    if (dataKey != null && dataKey.equalsIgnoreCase("FieldDefId = 387")) {
                        return "{\"Branch-111\":\"Branch-111\",\"Branch-128\":\"Branch-128\",\"Branch-291\":\"Branch-291\",\"Branch-310\":\"Branch-310\",\"Branch-402\":\"Branch-402\",\"Branch-404\":\"Branch-404\",\"Branch-407\":\"Branch-407\",\"Branch-408\":\"Branch-408\",\"Branch-413\":\"Branch-413\",\"Branch-414\":\"Branch-414\",\"Branch-415\":\"Branch-415\",\"Branch-503\":\"Branch-503\",\"Branch-510\":\"Branch-510\",\"Branch-511\":\"Branch-511\",\"Branch-514\":\"Branch-514\",\"Branch-516\":\"Branch-516\",\"Branch-605\":\"Branch-605\",\"Branch-612\":\"Branch-612\",\"Branch-613\":\"Branch-613\",\"Branch-618\":\"Branch-618\",\"Branch-619\":\"Branch-619\",\"Cash-Center-952\":\"Cash-Center-952\",\"Cash-Center-955\":\"Cash-Center-955\",\"Cash-Center-957\":\"Cash-Center-957\",\"Cash-Center-970\":\"Cash-Center-970\",\"Cash-Center-971\":\"Cash-Center-971\",\"Cash-Center-972\":\"Cash-Center-972\",\"Cash-Center-973\":\"Cash-Center-973\",\"Cash-Center-974\":\"Cash-Center-974\",\"Cash-Center-975\":\"Cash-Center-975\",\"Cash-Center-976\":\"Cash-Center-976\",\"Cash-Center-977\":\"Cash-Center-977\",\"Deta-Sad-Central\":\"Deta-Sad-Central\",\"Deta-Sad-Eastern\":\"Deta-Sad-Eastern\",\"Deta-Sad-Northern\":\"Deta-Sad-Northern\",\"Deta-Sad-Qassim\":\"Deta-Sad-Qassim\",\"Deta-Sad-Southern\":\"Deta-Sad-Southern\",\"Deta-Sad-Western\":\"Deta-Sad-Western\",\"SkyBand-Central\":\"SkyBand-Central\",\"SkyBand-Eastern\":\"SkyBand-Eastern\",\"SkyBand-Northern\":\"SkyBand-Northern\",\"SkyBand-Qassim\":\"SkyBand-Qassim\",\"SkyBand-Southern\":\"SkyBand-Southern\",\"SkyBand-Western\":\"SkyBand-Western\",\"STC-Central\":\"STC-Central\",\"STC-Eastern\":\"STC-Eastern\",\"STC-Northern\":\"STC-Northern\",\"STC-Qassim\":\"STC-Qassim\",\"STC-Southern\":\"STC-Southern\",\"STC-Western\":\"STC-Western\",\"Al-Majal-Central\":\"Al-Majal-Central\",\"Al-Majal-Eastern\":\"Al-Majal-Eastern\",\"Al-Majal-Northern\":\"Al-Majal-Northern\",\"Al-Majal-Qassim\":\"Al-Majal-Qassim\",\"Al-Majal-Southern\":\"Al-Majal-Southern\",\"Al-Majal-Western\":\"Al-Majal-Western\",\"Amnco-Central\":\"Amnco-Central\",\"Amnco-Eastern\":\"Amnco-Eastern\",\"Amnco-Northern\":\"Amnco-Northern\",\"Amnco-Qassim\":\"Amnco-Qassim\",\"Amnco-Southern\":\"Amnco-Southern\",\"Amnco-Western\":\"Amnco-Western\",\"Hemaia-Central\":\"Hemaia-Central\",\"Hemaia-Eastern\":\"Hemaia-Eastern\",\"Hemaia-Northern\":\"Hemaia-Northern\",\"Hemaia-Qassim\":\"Hemaia-Qassim\",\"Hemaia-Southern\":\"Hemaia-Southern\",\"Hemaia-Western\":\"Hemaia-Western\",\"Diebold-Central\":\"Diebold-Central\",\"Diebold-Eastern\":\"Diebold-Eastern\",\"Diebold-Northern\":\"Diebold-Northern\",\"Diebold-Qassim\":\"Diebold-Qassim\",\"Diebold-Southern\":\"Diebold-Southern\",\"Diebold-Western\":\"Diebold-Western\",\"NCR-Central\":\"NCR-Central\",\"NCR-Eastern\":\"NCR-Eastern\",\"NCR-Northern\":\"NCR-Northern\",\"NCR-Qassim\":\"NCR-Qassim\",\"NCR-Southern\":\"NCR-Southern\",\"NCR-Western\":\"NCR-Western\",\"Administration-Services\":\"Administration-Services\",\"ATM-Business\":\"ATM-Business\",\"ATM-Help-Desk\":\"ATM-Help-Desk\",\"ATM-Installations\":\"ATM-Installations\",\"ATM-Security\":\"ATM-Security\",\"Branch-ATM-Support-Central\":\"Branch-ATM-Support-Central\",\"Branch-ATM-Support-Eastern\":\"Branch-ATM-Support-Eastern\",\"Branch-ATM-Support-Northern\":\"Branch-ATM-Support-Northern\",\"Branch-ATM-Support-Qassim\":\"Branch-ATM-Support-Qassim\",\"Branch-ATM-Support-Southern\":\"Branch-ATM-Support-Southern\",\"Branch-ATM-Support-Western\":\"Branch-ATM-Support-Western\",\"Branch-Services-Department\":\"Branch-Services-Department\",\"Call-Center\":\"Call-Center\",\"CIT-Management\":\"CIT-Management\",\"Claim-Department\":\"Claim-Department\",\"IST-Switch\":\"IST-Switch\",\"Network-Operations-Department\":\"Network-Operations-Department\",\"Operation-Risk\":\"Operation-Risk\",\"Problem-Management-Department\":\"Problem-Management-Department\",\"Regional-Coordinators-Central\":\"Regional-Coordinators-Central\",\"Regional-Coordinators-Eastern\":\"Regional-Coordinators-Eastern\",\"Regional-Coordinators-Northern\":\"Regional-Coordinators-Northern\",\"Regional-Coordinators-Qassim\":\"Regional-Coordinators-Qassim\",\"Regional-Coordinators-Southern\":\"Regional-Coordinators-Southern\",\"Regional-Coordinators-Western\":\"Regional-Coordinators-Western\",\"Settlement-Department\":\"Settlement-Department\",\"Unnamed party\":\"Unnamed party\",\"Cash-Center-111\":\"Cash-Center-111\",\"Cash-Center-128\":\"Cash-Center-128\",\"Cash-Center-291\":\"Cash-Center-291\",\"Cash-Center-310\":\"Cash-Center-310\",\"Cash-Center-402\":\"Cash-Center-402\",\"Cash-Center-404\":\"Cash-Center-404\",\"Cash-Center-407\":\"Cash-Center-407\",\"Cash-Center-408\":\"Cash-Center-408\",\"Cash-Center-413\":\"Cash-Center-413\",\"Cash-Center-414\":\"Cash-Center-414\",\"Cash-Center-415\":\"Cash-Center-415\",\"Cash-Center-503\":\"Cash-Center-503\",\"Cash-Center-510\":\"Cash-Center-510\",\"Cash-Center-511\":\"Cash-Center-511\",\"Cash-Center-514\":\"Cash-Center-514\",\"Cash-Center-516\":\"Cash-Center-516\",\"Cash-Center-605\":\"Cash-Center-605\",\"Cash-Center-612\":\"Cash-Center-612\",\"Cash-Center-613\":\"Cash-Center-613\",\"Cash-Center-618\":\"Cash-Center-618\",\"Cash-Center-619\":\"Cash-Center-619\",\"Branch-101\":\"Branch-101\",\"Branch-102\":\"Branch-102\",\"Branch-103\":\"Branch-103\",\"Branch-104\":\"Branch-104\",\"Branch-105\":\"Branch-105\",\"Branch-106\":\"Branch-106\",\"Branch-107\":\"Branch-107\",\"Branch-114\":\"Branch-114\",\"Branch-115\":\"Branch-115\",\"Branch-117\":\"Branch-117\",\"Branch-118\":\"Branch-118\",\"Branch-120\":\"Branch-120\",\"Branch-121\":\"Branch-121\",\"Branch-122\":\"Branch-122\",\"Branch-123\":\"Branch-123\",\"Branch-124\":\"Branch-124\",\"Branch-125\":\"Branch-125\",\"Branch-130\":\"Branch-130\",\"Branch-133\":\"Branch-133\",\"Branch-135\":\"Branch-135\",\"Branch-136\":\"Branch-136\",\"Branch-137\":\"Branch-137\",\"Branch-138\":\"Branch-138\",\"Branch-139\":\"Branch-139\",\"Branch-142\":\"Branch-142\",\"Branch-145\":\"Branch-145\",\"Branch-147\":\"Branch-147\",\"Branch-148\":\"Branch-148\",\"Branch-149\":\"Branch-149\",\"Branch-150\":\"Branch-150\",\"Branch-152\":\"Branch-152\",\"Branch-153\":\"Branch-153\",\"Branch-155\":\"Branch-155\",\"Branch-157\":\"Branch-157\",\"Branch-165\":\"Branch-165\",\"Branch-167\":\"Branch-167\",\"Branch-168\":\"Branch-168\",\"Branch-169\":\"Branch-169\",\"Branch-170\":\"Branch-170\",\"Branch-176\":\"Branch-176\",\"Branch-177\":\"Branch-177\",\"Branch-178\":\"Branch-178\",\"Branch-179\":\"Branch-179\",\"Branch-180\":\"Branch-180\",\"Branch-182\":\"Branch-182\",\"Branch-183\":\"Branch-183\",\"Branch-184\":\"Branch-184\",\"Branch-185\":\"Branch-185\",\"Branch-186\":\"Branch-186\",\"Branch-187\":\"Branch-187\",\"Branch-188\":\"Branch-188\",\"Branch-189\":\"Branch-189\",\"Branch-190\":\"Branch-190\",\"Branch-201\":\"Branch-201\",\"Branch-202\":\"Branch-202\",\"Branch-203\":\"Branch-203\",\"Branch-204\":\"Branch-204\",\"Branch-205\":\"Branch-205\",\"Branch-206\":\"Branch-206\",\"Branch-207\":\"Branch-207\",\"Branch-209\":\"Branch-209\",\"Branch-211\":\"Branch-211\",\"Branch-212\":\"Branch-212\",\"Branch-215\":\"Branch-215\",\"Branch-216\":\"Branch-216\",\"Branch-217\":\"Branch-217\",\"Branch-218\":\"Branch-218\",\"Branch-219\":\"Branch-219\",\"Branch-221\":\"Branch-221\",\"Branch-222\":\"Branch-222\",\"Branch-223\":\"Branch-223\",\"Branch-224\":\"Branch-224\",\"Branch-226\":\"Branch-226\",\"Branch-228\":\"Branch-228\",\"Branch-229\":\"Branch-229\",\"Branch-230\":\"Branch-230\",\"Branch-235\":\"Branch-235\",\"Branch-236\":\"Branch-236\",\"Branch-238\":\"Branch-238\",\"Branch-239\":\"Branch-239\",\"Branch-240\":\"Branch-240\",\"Branch-241\":\"Branch-241\",\"Branch-242\":\"Branch-242\",\"Branch-244\":\"Branch-244\",\"Branch-245\":\"Branch-245\",\"Branch-246\":\"Branch-246\",\"Branch-247\":\"Branch-247\",\"Branch-248\":\"Branch-248\",\"Branch-255\":\"Branch-255\",\"Branch-257\":\"Branch-257\",\"Branch-258\":\"Branch-258\",\"Branch-259\":\"Branch-259\",\"Branch-260\":\"Branch-260\",\"Branch-261\":\"Branch-261\",\"Branch-262\":\"Branch-262\",\"Branch-264\":\"Branch-264\",\"Branch-265\":\"Branch-265\",\"Branch-266\":\"Branch-266\",\"Branch-267\":\"Branch-267\",\"Branch-268\":\"Branch-268\",\"Branch-269\":\"Branch-269\",\"Branch-270\":\"Branch-270\",\"Branch-271\":\"Branch-271\",\"Branch-290\":\"Branch-290\",\"Branch-297\":\"Branch-297\",\"Branch-299\":\"Branch-299\",\"Branch-301\":\"Branch-301\",\"Branch-302\":\"Branch-302\",\"Branch-304\":\"Branch-304\",\"Branch-305\":\"Branch-305\",\"Branch-307\":\"Branch-307\",\"Branch-308\":\"Branch-308\",\"Branch-311\":\"Branch-311\",\"Branch-312\":\"Branch-312\",\"Branch-313\":\"Branch-313\",\"Branch-314\":\"Branch-314\",\"Branch-315\":\"Branch-315\",\"Branch-317\":\"Branch-317\",\"Branch-318\":\"Branch-318\",\"Branch-320\":\"Branch-320\",\"Branch-321\":\"Branch-321\",\"Branch-322\":\"Branch-322\",\"Branch-323\":\"Branch-323\",\"Branch-324\":\"Branch-324\",\"Branch-327\":\"Branch-327\",\"Branch-328\":\"Branch-328\",\"Branch-329\":\"Branch-329\",\"Branch-330\":\"Branch-330\",\"Branch-331\":\"Branch-331\",\"Branch-332\":\"Branch-332\",\"Branch-333\":\"Branch-333\",\"Branch-334\":\"Branch-334\",\"Branch-336\":\"Branch-336\",\"Branch-337\":\"Branch-337\",\"Branch-340\":\"Branch-340\",\"Branch-342\":\"Branch-342\",\"Branch-343\":\"Branch-343\",\"Branch-344\":\"Branch-344\",\"Branch-345\":\"Branch-345\",\"Branch-346\":\"Branch-346\",\"Branch-347\":\"Branch-347\",\"Branch-348\":\"Branch-348\",\"Branch-349\":\"Branch-349\",\"Branch-350\":\"Branch-350\",\"Branch-351\":\"Branch-351\",\"Branch-352\":\"Branch-352\",\"Branch-353\":\"Branch-353\",\"Branch-354\":\"Branch-354\",\"Branch-355\":\"Branch-355\",\"Branch-357\":\"Branch-357\",\"Branch-359\":\"Branch-359\",\"Branch-401\":\"Branch-401\",\"Branch-403\":\"Branch-403\",\"Branch-405\":\"Branch-405\",\"Branch-406\":\"Branch-406\",\"Branch-409\":\"Branch-409\",\"Branch-410\":\"Branch-410\",\"Branch-411\":\"Branch-411\",\"Branch-416\":\"Branch-416\",\"Branch-421\":\"Branch-421\",\"Branch-500\":\"Branch-500\",\"Branch-501\":\"Branch-501\",\"Branch-504\":\"Branch-504\",\"Branch-505\":\"Branch-505\",\"Branch-506\":\"Branch-506\",\"Branch-507\":\"Branch-507\",\"Branch-508\":\"Branch-508\",\"Branch-509\":\"Branch-509\",\"Branch-513\":\"Branch-513\",\"Branch-515\":\"Branch-515\",\"Branch-517\":\"Branch-517\",\"Branch-518\":\"Branch-518\",\"Branch-520\":\"Branch-520\",\"Branch-521\":\"Branch-521\",\"Branch-523\":\"Branch-523\",\"Branch-525\":\"Branch-525\",\"Branch-602\":\"Branch-602\",\"Branch-603\":\"Branch-603\",\"Branch-604\":\"Branch-604\",\"Branch-606\":\"Branch-606\",\"Branch-607\":\"Branch-607\",\"Branch-608\":\"Branch-608\",\"Branch-609\":\"Branch-609\",\"Branch-610\":\"Branch-610\",\"Branch-611\":\"Branch-611\",\"Branch-614\":\"Branch-614\",\"Branch-615\":\"Branch-615\",\"Branch-616\":\"Branch-616\",\"Branch-617\":\"Branch-617\",\"Branch-621\":\"Branch-621\",\"Branch-622\":\"Branch-622\",\"Branch-624\":\"Branch-624\",\"NIBANK1\":\"NIBANK1\",\"CurrentUser.AssignedParty\":\"dispatchParty\"}";
                    }
                    if (dataKey != null && dataKey.equalsIgnoreCase("FieldDefId = 241")) {
                        return "{\"01\":\"OPEN\", \"02\":\"WORK\", \"03\": \"HOLD\", \"04\": \"RESOLVED\", \"05\":\"CLOSE\"}";
                    }
                    if (dataKey != null && dataKey.equalsIgnoreCase("FieldDefId = 242")) {
                        if (parentValue.equalsIgnoreCase("01")) {
                            return "{\"10\":\"SUB-OPEN\", \"11\":\"SUB-WORK\"}";
                        } else if (parentValue.equalsIgnoreCase("05")) {
                            return "{\"10\":\"SUB-CLOSE\", \"11\":\"SUB-REWORK\", \"12\":\"SUB-REOPEN\"}";
                        }
                        return "{\"10\":\"SUB-OPEN\", \"11\":\"SUB-WORK\", \"12\": \"SUB-HOLD\", \"13\": \"SUB-RESOLVED\", \"14\":\"SUB-CLOSE\"}";
                    }
                    if (dataKey != null && dataKey.equalsIgnoreCase("FieldDefId = 299")) {
                        return "{\"20\":\"SUB-SUB-OPEN\", \"21\":\"SUB-SUB-WORK\", \"22\": \"SUB-SUB-HOLD\", \"23\": \"SUB-SUB-RESOLVED\", \"24\":\"SUB-SUB-CLOSE\"}";
                    }
                    if (dataKey != null && dataKey.equalsIgnoreCase("FieldDefId = 390")) {
                        return "[{\"varOwner\":\"IMS\",\"values\":{\"Branch-617\":\"Branch-617\",\"Branch-621\":\"Branch-621\",\"Branch-622\":\"Branch-622\"}},{\"varOwner\":\"RBAC\",\"values\":{\"CurrentUser.AssignedParty\":\"dispatchParty\"}}]";
                    }
                    if (dataKey != null && dataKey.equalsIgnoreCase("Performance")) {
                        return "{\"01\":\"DISK\", \"02\":\"NETWORK\", \"03\": \"CPU\"}";
                    }
                }
                return ((ExternalDataAccessScopeBuilder) externalDataAccessUtil
                        .getExternalDataAccessMap()
                        .get(scopeConstraint.getSourceType())
                        .get(scopeConstraint.getApplicationName()))
                        .getFilterKeyData(sourcePath, dataKey, scopeKey, userName,
                                additionalMap, parentValue);
            }
        }
        if ("group".equalsIgnoreCase(sourcePath)) {
            String scopeQuery = null;
            if (scopeMap != null && !scopeMap.isEmpty()) {
                scopeQuery = RBACUtil.encodeForScopeQuery(scopeMap
                        .get(RBACUtil.SCOPE_KEY_GROUP_VIEW));
            }
            MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
            queryParams.put(RBACUtil.GROUP_SCOPE_QUERY,
                    Arrays.asList(new String[]{scopeQuery}));
            Options options = buildOptions(queryParams);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            list = groupDal.getGroupIdNames(options);
            for (Map<String, Object> tempMap : list) {
                filterMap.put(tempMap.get("groupId").toString(),
                        tempMap.get("name"));
            }
            if (rhsLabelMap.get(scopeKey) != null
                    && (Map) ((Map) rhsLabelMap.get(scopeKey))
                    .get(getRbacType(additionalMap)) != null) {
                filterMap.put(
                        "$$currentuser.group.id$$",
                        ((Map) ((Map) rhsLabelMap.get(scopeKey))
                                .get(getRbacType(additionalMap))).get(
                                getLangCode(additionalMap)).toString());
            }
        } else if ("role".equalsIgnoreCase(sourcePath)) {
            String scopeQuery = null;
            if (scopeMap != null && !scopeMap.isEmpty()) {
                scopeQuery = RBACUtil.encodeForScopeQuery(scopeMap
                        .get(RBACUtil.SCOPE_KEY_ROLE_VIEW));
            }
            MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
            queryParams.put(RBACUtil.ROLE_SCOPE_QUERY,
                    Arrays.asList(new String[]{scopeQuery}));
            Options options = buildOptions(queryParams);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            list = roleDal.getRoleIdNames(options);
            for (Map<String, Object> tempMap : list) {
                filterMap.put(tempMap.get("roleId").toString(),
                        tempMap.get("roleName"));
            }
        } else if ("tenant".equalsIgnoreCase(sourcePath)) {
            String scopeQuery = null;
            if (scopeMap != null && !scopeMap.isEmpty()) {
                scopeQuery = RBACUtil.encodeForScopeQuery(scopeMap
                        .get(RBACUtil.SCOPE_KEY_TENANT));
            }
            MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
            queryParams.put(RBACUtil.TENANT_SCOPE_QUERY,
                    Arrays.asList(new String[]{scopeQuery}));
            Options options = buildOptions(queryParams, additionalMap);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            list = tenantDal.searchTenantIdNames(options);
            Map<String, Object> filterMapForTenants = new HashMap<String, Object>();
            for (Map<String, Object> tempMap : list) {
                filterMapForTenants.put(tempMap.get("tenantId").toString(),
                        tempMap.get("tenantName"));
            }
            filterMap.put("totalCount", tenantDal.searchTenantIdNamesCount(options));
            filterMap.put("values", filterMapForTenants);
        } else if ("childApplication".equalsIgnoreCase(sourcePath)) {
            MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
            Options options = buildOptions(queryParams, additionalMap);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            list = applicationDal.getUserAuthorizedApplicationIdNames(userName, options);
            Map<String, Object> filterMapForTenants = new HashMap<String, Object>();
            for (Map<String, Object> tempMap : list) {
                filterMapForTenants.put(tempMap.get("childApplicationId").toString(),
                        tempMap.get("childApplicationName"));
            }
            filterMap.put("totalCount", list != null ? list.size() : 0);
            filterMap.put("values", filterMapForTenants);
        } else if ("attributes".equalsIgnoreCase(sourcePath)) {
            if (dataKey != null && !dataKey.isEmpty()) {
                String groupScopeQuery = null;
                String userScopeQuery = null;
                if (scopeMap != null && !scopeMap.isEmpty()) {
                    groupScopeQuery = RBACUtil.encodeForScopeQuery(scopeMap
                            .get(RBACUtil.SCOPE_KEY_GROUP_VIEW));
                    userScopeQuery = RBACUtil.encodeForScopeQuery(scopeMap
                            .get(RBACUtil.SCOPE_KEY_USER_VIEW));
                }
                MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
                queryParams.put(RBACUtil.USER_SCOPE_QUERY,
                        Arrays.asList(new String[]{userScopeQuery}));
                queryParams.put(RBACUtil.GROUP_SCOPE_QUERY,
                        Arrays.asList(new String[]{groupScopeQuery}));
                Options options = buildOptions(queryParams);

                String attributesArray = scopeConstraintDal
                        .getAttributeDataByAttributeId(
                                Integer.parseInt(dataKey), options, null, null);
                if (attributesArray != null && !attributesArray.isEmpty()) {
                    try {
                        ArrayNode jsonArray = (ArrayNode) new ObjectMapper().readTree(attributesArray);
                        for (int z = 0; z < jsonArray.size(); z++) {
                            filterMap.put(jsonArray.get(z).get("name").toString(), jsonArray.get(z).get("name"));
                        }
                        if (scopeKey.equalsIgnoreCase(RBACUtil.SCOPE_KEY_USER_VIEW)) {
                            String templabel = getCurrentLabel(scopeKey,
                                    dataKey, additionalMap);
                            if (templabel != null && getRbacType(additionalMap).equalsIgnoreCase(ATTRIBUTE_USER_TYPE)) {
                                filterMap
                                        .put("$$currentuser."
                                                + Lookup.getMasterAttributeNameById(Integer
                                                .parseInt(dataKey))
                                                + "$$", templabel);
                            }
                            templabel = getCurrentLabel(scopeKey, dataKey,
                                    additionalMap);
                            if (templabel != null && getRbacType(additionalMap).equalsIgnoreCase(ATTRIBUTE_USER_GROUP_TYPE)) {
                                filterMap
                                        .put("$$currentuser.group."
                                                        + Lookup.getMasterAttributeNameById(Integer
                                                        .parseInt(dataKey))
                                                        + "$$",
                                                templabel);
                            }
                        } else if (scopeKey.equalsIgnoreCase(RBACUtil.SCOPE_KEY_GROUP_VIEW)) {
                            String templabel = getCurrentLabel(scopeKey,
                                    dataKey, additionalMap);
                            if (templabel != null) {
                                filterMap
                                        .put("$$currentuser.group."
                                                        + Lookup.getMasterAttributeNameById(Integer
                                                        .parseInt(dataKey))
                                                        + "$$",
                                                templabel);
                            }
                        } else {
                            String templabel = getCurrentLabel(scopeKey,
                                    dataKey, additionalMap);
                            if (templabel != null) {
                                filterMap
                                        .put("$$currentuser."
                                                        + Lookup.getMasterAttributeNameById(Integer
                                                        .parseInt(dataKey))
                                                        + "$$",
                                                templabel);
                            }
                        }
                    } catch (Exception e) {
                        log.error("getFilterKeyData; dataKey={}; additionalMap={}; Exception={}",
                                dataKey, additionalMap, e);
                    }

                }
            }
        } else if ("variables".equalsIgnoreCase(sourcePath)) {
            if (dataKey != null && !dataKey.isEmpty()) {
                String scopeQuery = null;
                if (scopeMap != null && !scopeMap.isEmpty()) {
                    scopeQuery = RBACUtil.encodeForScopeQuery(scopeMap
                            .get(RBACUtil.SCOPE_KEY_USER_VIEW));
                }
                MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
                queryParams.put(RBACUtil.USER_SCOPE_QUERY,
                        Arrays.asList(new String[]{scopeQuery}));
                Options options = buildOptions(queryParams);
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                list = userDal.getUserIdNames(options);
                if (list != null && !list.isEmpty()) {
                    String additionalQuery = "";
                    if (scopeQuery != null && !scopeQuery.isEmpty() && scopeQuery.length() > 1) {
                        additionalQuery = additionalQuery + " and  (v.userId IN (select u.userId from User u where " + "(" + scopeQuery + ")" + ") "
                                + "or v.groupId IN (select u.groupId from User u where u.userId IN (select u.userId from User u where " + "(" + scopeQuery + ")" + ") ) )";
                    }
                    TypedQuery<String> queryUserVariables = em
                            .createQuery(
                                    "select distinct(v.variableValue) from Variable v where v.variableName = :variableName" + additionalQuery,
                                    String.class);
                    queryUserVariables.setParameter("variableName", dataKey);
                    List<String> userVariables = queryUserVariables
                            .getResultList();
                    for (String variableValue : userVariables) {
                        filterMap.put(variableValue, variableValue);
                    }
                    filterMap.put("$$currentuser.variable." + dataKey + "$$",
                            "Current User's  " + dataKey);

                }
            }
        } else {
            log.error("getFilterKeyData; Check Scope Constraint Entries;");
        }
        return new Gson().toJson(filterMap);
    }

    public static ArrayNode tenantScopeToJson(String input) {
        try {
            JsonNode tenantMap = new ObjectMapper().readTree(input);
            if (tenantMap != null) {
                JsonNode jsonRules = tenantMap.get("rules").get(0);
                if (jsonRules != null) {
                    return (ArrayNode) jsonRules.get("value");
                }
            }
        } catch (Exception e) {
            log.error("tenantScopeToJson; Exception={};", e);
        }
        return null;
    }
}
