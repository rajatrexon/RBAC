package com.esq.rbac.service.contact.objectrole.rest;

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.base.error.RestErrorMessages;
import com.esq.rbac.service.base.exception.RestException;
import com.esq.rbac.service.base.rest.BaseRest;
import com.esq.rbac.service.base.vo.Count;
import com.esq.rbac.service.calendar.service.CalendarDal;
import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.codes.service.CodeDal;
import com.esq.rbac.service.contact.contactrole.queries.ContactRoleQueries;
import com.esq.rbac.service.contact.contactrole.repository.ContactRoleRepository;
import com.esq.rbac.service.contact.domain.Contact;
import com.esq.rbac.service.contact.embedded.ContactAvailableTime;
import com.esq.rbac.service.contact.embedded.ContactWorkEndTime;
import com.esq.rbac.service.contact.helpers.ContactUserRest;
import com.esq.rbac.service.contact.mappingtype.domain.MappingType;
import com.esq.rbac.service.contact.messagetemplate.domain.MessageTemplate;
import com.esq.rbac.service.contact.messagetemplate.repository.TemplateRepository;
import com.esq.rbac.service.contact.objectrole.domain.ObjectRole;
import com.esq.rbac.service.contact.objectrole.repository.ObjectRoleRepository;
import com.esq.rbac.service.contact.repository.ContactRepository;
import com.esq.rbac.service.contact.schedule.domain.Schedule;
import com.esq.rbac.service.contact.schedule.embedded.ScheduleRule;
import com.esq.rbac.service.contact.schedule.helpers.ScheduleEvaluator;
import com.esq.rbac.service.contact.sla.domain.SLA;
import com.esq.rbac.service.contact.sla.repository.SLARepository;
import com.esq.rbac.service.culture.service.CultureDal;
import com.esq.rbac.service.exception.ErrorInfo;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.externaldb.service.ExternalDbDal;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import com.esq.rbac.service.scope.scopeconstraint.service.ScopeConstraintDal;
import com.esq.rbac.service.session.constraintdata.domain.ConstraintData;
import com.esq.rbac.service.targetoperations.TargetOperations;
import com.esq.rbac.service.timezonemaster.service.TimeZoneMasterDal;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.embedded.CloneObjectRole;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.CacheManagerUtil;
import com.esq.rbac.service.util.ContactAuditUtil;
import com.esq.rbac.service.util.ContactDispatcherUtil;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Slf4j
@RestController
@RequestMapping("/object")
//@ManagedResource(objectName = "com.esq.dispatcher.contacts:type=REST,name=ObjectRest")
@SuppressWarnings("unused")
public class ObjectRoleRest extends BaseRest<ObjectRole> {

    private static final String ID = "id";
    private static final String OBJECT_ID = "objectId";
    private static final String CONTACT_ROLE_ID = "contactRoleId";
    private static final String CONTACT_ID = "contactId";
    private static final String CREATED_TIME = "createdTime";
    private static final String UPDATED_TIME = "updatedTime";
    private static final String FIND_CONTACT_ROLE_ID_BY_NAME = "findContactRoleIdByName";
    private static final String FIND_ADDRESS_TYPE_ID_BY_NAME = "findAddressTypeIdByName";
    private static final String NAME = "name";
    private static final String FIND_MAP_ID_BY_MAPFROM_MAPTO = "findMapIdByMapFromMapTo";
    private static final String LISTOF_CONTACT_ROLES = "listContactRolesInObjectRole";
    private static final String MAPFROM = "mapFrom";
    private static final String MAPTO = "mapTo";
    private static final Set<String> FILTER_COLUMNS;
    private static final Set<String> ORDER_COLUMNS;
    private static final Set<String> SEARCH_COLUMNS;
    private static final String DEFAULT_ADD_BUTTTON_ENABLE = "true";
    private static final String DEFAULT_ADD_POLICY_TO_ROLE_ENABLE = "true";
    private static final String DEFAULT_ADD_PARTY_TO_ROLE_ENABLE = "true";
    private static final String PROPERTY_ADD_BUTTTON = "actionAddObject.button.enabled";
    private static final String PROPERTY_ADD_POLICY_TO_ROLE = "actionAddObjectPolicyService.button.enabled";
    private static final String PROPERTY_ADD_PARTY_TO_ROLE = "actionAddObjectPartyIdentifierService.button.enabled";
    private static final String PROPERTY_ADD_PARTY_TO_ROLE_MAP_FROM = "actionAddObjectPartyIdentifierService.mapFrom";
    private static final String PARAM_IS_OBJECT_CONTACT = "objectContact";
    private static final String PARAM_APP_KEY = "appKey";

    static {
        FILTER_COLUMNS = new HashSet<String>(Arrays.asList(OBJECT_ID, CONTACT_ROLE_ID));

        ORDER_COLUMNS = new HashSet<String>(Arrays.asList(OBJECT_ID, CONTACT_ROLE_ID, PARAM_TENANT_ID));

        SEARCH_COLUMNS = new HashSet<String>(List.of(OBJECT_ID));

    }

    private EntityManagerFactory entityManagerFactory = null;
    private ContactRepository contactRepository = null;
    private ContactRoleRepository contactRoleRepository = null;
    private SLARepository slaRepository = null;
    private ObjectRoleRepository objectContactRepository = null;
    private Configuration configuration;
    private UserDal userDal;
    private DeploymentUtil deploymentUtil;
    private ContactDispatcherUtil contactDispatcherUtil;
    private CodeDal codeDal;
    private ContactUserRest userRest;
    private CalendarDal calendarDal;
    private CacheManagerUtil cacheManager;
    private Pattern orgNameContactMappingRegex = Pattern.compile("(?i)%(.*)%"); // ^(%branch%|%region%|%city%)$
    private ScopeConstraintDal scopeConstraintDal;
    private ExternalDbDal externalDbDal;
    private OrganizationMaintenanceDal orgDal;
    private CultureDal cultureDal;
    private TimeZoneMasterDal timeZoneMasterDal;

    @Autowired
    public ObjectRoleRest(ObjectRoleRepository objectContactRepository) {
        super(ObjectRole.class, objectContactRepository);
        this.objectContactRepository = objectContactRepository;
    }

    //to avoid null pointer exceptions for null input
    private static boolean isPatternMatched(Pattern pattern, String input) {
        if (input == null) {
            return false;
        }
        return pattern.matcher(input).matches();
    }

    private static void replacePercentageChannelData(Contact contact, String channel) {
        if (contact.getChannel() != null) {
            Code newCode = Code.copyOf(contact.getChannel());
            newCode.setCodeValue(channel);
            newCode.setName(channel);
            newCode.setCodeId(null);
            contact.setChannel(newCode);
        } else {
            log.error("copyChannel; Channel is null; contact={}; Exception={};", contact);
        }
        if (contact.getTemplate() != null) {
            try {
                MessageTemplate newTemplate = MessageTemplate.copyOf(contact.getTemplate());
                newTemplate.setJsonDefinition(TemplateRepository.getJsonDefinitionByChannel(newTemplate.getJsonDefinition(), channel.toLowerCase()));
                contact.setTemplate(newTemplate);
            } catch (Exception e) {
                log.error("copyChannel; Error while getting channel template from generic template; contact={}; Exception={};", contact, e);
            }
        } else {
            log.error("copyChannel; Template is null; contact={}; Exception={};", contact);
        }

    }

    public static Map<String, String> jsonToMap(String additionalFilter) throws IOException {
        log.debug("jsonToMap;additionalFilter={}", additionalFilter);
        ObjectMapper objMapper = new ObjectMapper();
        HashMap<String, String> map = objMapper.readValue(additionalFilter, new TypeReference<HashMap<String, String>>() {
        });

        Map<String, String> resultMap = new HashMap<>();
        if (map == null || map.isEmpty()) {
            return resultMap;
        }
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            String newKey = key.toLowerCase();
            resultMap.put(newKey, map.get(key));
        }
        /*
        JsonNode jObject = new ObjectMapper().readTree(additionalFilter);
        Iterator<?> keys = jObject.elements();
        while( keys.hasNext() ){
            String key = (String)keys.next();
            String value = jObject.path(key).asText();
            map.put(key.toLowerCase(), value);
        }*/
        return resultMap;
    }

    @Autowired
    public void setTimeZoneMasterDal(TimeZoneMasterDal timeZoneMasterDal) {
        this.timeZoneMasterDal = timeZoneMasterDal;
    }

    @Autowired
    public void setCultureDal(CultureDal cultureDal) {
        this.cultureDal = cultureDal;
    }

    @Autowired
    public void setScopeConstraintDal(ScopeConstraintDal scopeConstraintDal, ExternalDbDal externalDbDal) {
        log.trace("setScopeConstraintDal; externalDbDal={}", externalDbDal);
        this.scopeConstraintDal = scopeConstraintDal;
        this.externalDbDal = externalDbDal;
    }

    @Autowired
    public void setUserRest(ContactUserRest userRest) {
        log.trace("setUserRest;");
        this.userRest = userRest;
    }

    @Autowired
    public void setUserDal(UserDal userDal) {
        log.trace("setUserDal; userDal={};", userDal);
        this.userDal = userDal;
    }

    @Autowired
    public void setOrgDal(OrganizationMaintenanceDal orgDal) {
        log.trace("setUserRest;");
        this.orgDal = orgDal;
    }

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        log.trace("setDeploymentUtil; deploymentUtil={};", deploymentUtil);
        this.deploymentUtil = deploymentUtil;
    }

    @Autowired
    public void setContactDispatcherUtil(ContactDispatcherUtil contactDispatcherUtil) {
        this.contactDispatcherUtil = contactDispatcherUtil;
    }

    @Autowired
    public void setCodeDal(CodeDal codeDal) {
        this.codeDal = codeDal;
        OptionFilter optionFilter = new OptionFilter();
        optionFilter.addFilter("codeType", "DISPATCH_CONTACT_VARIABLE");
        List<Code> codeList = codeDal.list(new Options(optionFilter));
        if (codeList != null && !codeList.isEmpty()) {
            List<String> patterns = new LinkedList<String>();
            for (int i = 0; i < codeList.size(); i++) {
                if (codeList.get(i).getCodeValue() != null && !codeList.get(i).getCodeValue().isEmpty()) {
                    patterns.add(codeList.get(i).getCodeValue().toLowerCase());
                }
            }
            optionFilter = new OptionFilter();
            optionFilter.addFilter("codeType", "DISPATCH_CONTACT_PERC_CHANNEL_VARIABLE");
            codeList = codeDal.list(new Options(optionFilter));
            if (codeList != null && !codeList.isEmpty()) {
                for (int i = 0; i < codeList.size(); i++) {
                    if (codeList.get(i).getCodeValue() != null && !codeList.get(i).getCodeValue().isEmpty()) {
                        patterns.add(codeList.get(i).getCodeValue().toLowerCase());
                    }
                }
            }
            try {
                orgNameContactMappingRegex = Pattern.compile("(?i)^(" + StringUtils.join(patterns, "|") + ")$");
            } catch (PatternSyntaxException p) {
                //thrown this to fail tanuki wrapper
                throw new BeanCreationException("ObjectRoleRest.setCodeDal", p.getMessage());
            }
            log.info("setCodeDal; orgNameContactMappingRegex={};", orgNameContactMappingRegex);
        }
    }

    @Resource(name = "propertyConfig")
    public void setConfiguration(Configuration configuration) {
        log.trace("setConfiguration;");
        this.configuration = configuration;
    }

    public String getTimeZone(String timeZone) {
        String offSet = cultureDal.getOffsetFromTimeZone(timeZone);
        ZoneId z = ZoneId.of(offSet);
        return TimeZone.getTimeZone(z).getDisplayName();
    }

    @Autowired
    public void setCacheManager(CacheManagerUtil cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public ResponseEntity<ObjectRole> readById(@PathVariable("id") long id) {
        return super.readById(id);
    }

    @GetMapping(value = "/usedActionRule", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Object[]> actionRulesUsedInDispatchMap(HttpServletRequest servletRequest) {
        List<Object> list = null;
        Object[] array = null;
        log.info("actionRulesUsedInDispatchMap;  uriInfo = {}", servletRequest);
        try {
            // MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            Map<String, String[]> parameterMap = servletRequest.getParameterMap();
            MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();

            parameterMap.forEach((key, values) -> queryParams.addAll(key, Arrays.asList(values)));

            log.info("actionRulesUsedInDispatchMap;  queryParams = {}", queryParams);
            String isObjectContact = getParameterSingle(queryParams, PARAM_IS_OBJECT_CONTACT, null);
            if (isObjectContact.equalsIgnoreCase("true")) {
                // Handling for object mapping screen
            }
            list = objectContactRepository.readActionRulesInObjectRole();
            array = new Object[list.size()];
            array = list.toArray(array);
        } catch (Exception e) {
            log.error("actionRulesUsedInDispatchMap; exception={}", e);
        }
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).header("Expires", new Date().toString()).body(array);
    }

    @GetMapping(value = "/objectRole", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Object> readByObjectKey(HttpServletRequest request) {

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        log.debug("readByObjectKey;  uriInfo = {}", uriInfo);
        ObjectRole objectRole = null;
        try {
            //MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            String actionRuleId = uriInfo.getFirst("actionRuleId");
            long tenantId = Long.parseLong(uriInfo.getFirst("tenantId"));
            log.debug("readByObjectKey; actionRuleId={},tenantId={}", actionRuleId, tenantId);
            objectRole = objectContactRepository.readObjectRole(actionRuleId, tenantId);
        } catch (NoResultException nre) {
            log.debug("readByObjectKey; NoResultException={}", nre.getMessage());
        } catch (Exception e) {
            log.error("readByObjectKey; exception={}", e);
        }
        if (objectRole == null) {
            return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).header("Expires", new Date().toString()).body(new HashMap<String, String>());
        }
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).header("Expires", new Date().toString()).body(objectRole);
    }

    @GetMapping(value = "/mapId", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public int getMapID(HttpServletRequest request) {

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        log.info("getMapID;  uriInfo = {}", uriInfo);
        int id;
        try {
            //MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            log.info("getMapID;  queryParams = {}", uriInfo);
            String from = uriInfo.getFirst("mapFrom");
            String to = uriInfo.getFirst("mapTo");
            id = mapTypeId(from, to);

        } catch (Exception e) {
            throw new RestException(RestErrorMessages.COUNT_FAILED, "Count resource");
        }

        return id;

    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public ResponseEntity<Object[]> list(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) {

        log.debug("list");
        log.debug("uriInfo = {}, headers = {}", request, headers);
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> queryParams.addAll(key, Arrays.asList(values)));
        try {
            //MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            boolean hasFilter = false;
            for (String filterColumn : FILTER_COLUMNS) {
                if (queryParams.containsKey(filterColumn)) {
                    hasFilter = true;
                    break;
                }
            }
            String asc = getParameterSingle(queryParams, PARAM_ASCENDING, null);
            String desc = getParameterSingle(queryParams, PARAM_DESCENDING, null);
            String isObjectContact = getParameterSingle(queryParams, PARAM_IS_OBJECT_CONTACT, null);
            String appKey = getParameterSingle(queryParams, PARAM_APP_KEY, null);
            int first = getParameterSingle(queryParams, PARAM_FIRST, PARAM_FIRST_DEFAULT);
            int max = getParameterSingle(queryParams, PARAM_MAX, PARAM_MAX_DEFAULT);
            String tenantScope = getTenantScope(queryParams, headers);
            boolean isTenantVisible = false;
            if (queryParams.containsKey(IS_TENANT_VISIBLE) && queryParams.containsKey(PARAM_Q)) {
                isTenantVisible = getParameterSingle(queryParams, IS_TENANT_VISIBLE, null).equalsIgnoreCase("true");
            }
            log.debug("list; asc={}; desc={}; first={}; max={} ,tenantScope ={}, isTenantVisible = {}", asc, desc, first, max, tenantScope, isTenantVisible);

            if (queryParams.containsKey(PARAM_Q)) {
                String q = queryParams.getFirst(PARAM_Q);
                log.debug("list; q={}", q);
                ObjectRoleRepository objectRoleRepository = (ObjectRoleRepository) repository;
                List<ObjectRole> result;
                if (tenantScope == null || tenantScope.trim().equals("") || tenantScope.trim().equals("[]")) {
                    result = objectRoleRepository.fullTextSearch(q, asc, desc, first, max, isObjectContact, isTenantVisible);
                } else {
                    result = objectRoleRepository.fullTextSearch(q, asc, desc, first, max, isObjectContact, isTenantVisible, getTenantData(tenantScope));
                }
                ObjectRole[] a = new ObjectRole[result.size()];
                a = result.toArray(a);
                return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).header("Expires", new Date().toString()).body(a);

            } else if (!hasFilter) {
                ObjectRoleRepository objectRoleRepository = (ObjectRoleRepository) repository;
                List<ObjectRole> result;
                if (tenantScope == null || tenantScope.trim().equals("") || tenantScope.trim().equals("[]")) {
                    result = objectRoleRepository.list(asc, desc, first, max, isObjectContact, appKey);
                } else {
                    result = objectRoleRepository.list(asc, desc, first, max, isObjectContact, appKey, getTenantData(tenantScope));
                }
                ObjectRole[] a = new ObjectRole[result.size()];
                a = result.toArray(a);
                return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).header("Expires", new Date().toString()).body(a);
            }
        } catch (RestException e) {
            logException("list", e);
            throw new RestException(RestErrorMessages.LIST_FAILED, "List resource");
        } catch (Exception e) {
            logException("list", e);
            new ErrorInfo(ErrorInfo.INTERNAL_ERROR);
            return null;
        }
        return super.list(request, headers);
    }

    @GetMapping(value = "/count", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public ResponseEntity<Count> count(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) {
        try {
            //MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            Map<String, String[]> parameterMap = request.getParameterMap();
            MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

            parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
            String appKey = null;//getParameterSingle(queryParams, PARAM_APP_KEY, null);
            String isObjectContact = null;
            if (uriInfo != null && uriInfo.containsKey(PARAM_APP_KEY)) {
                if (uriInfo.get(PARAM_APP_KEY).size() > 1) {
                    log.warn("Only one value allowed for parameter " + PARAM_APP_KEY);
                    throw new RestException(RestErrorMessages.PARAMETER_ONLY_ONE, "Only one value allowed for parameter: {0}", PARAM_APP_KEY);
                }
                appKey = uriInfo.getFirst(PARAM_APP_KEY);
                //appKey = queryParams.get(PARAM_APP_KEY)
            }

            if (uriInfo != null && uriInfo.containsKey(PARAM_IS_OBJECT_CONTACT)) {
                if (uriInfo.get(PARAM_IS_OBJECT_CONTACT).size() > 1) {
                    log.warn("Only one value allowed for parameter " + PARAM_IS_OBJECT_CONTACT);
                    throw new RestException(RestErrorMessages.PARAMETER_ONLY_ONE, "Only one value allowed for parameter: {0}", PARAM_IS_OBJECT_CONTACT);
                }
                isObjectContact = uriInfo.getFirst(PARAM_IS_OBJECT_CONTACT);
                //appKey = queryParams.get(PARAM_APP_KEY)
            }
            //  String isObjectContact =getParameterSingle(queryParams, PARAM_IS_OBJECT_CONTACT,null);


            if (uriInfo.containsKey(PARAM_Q)) {
                String q = uriInfo.getFirst(PARAM_Q);
                String tenantScope = getTenantScope(uriInfo, headers);
                ObjectRoleRepository objectRoleRepository = (ObjectRoleRepository) repository;
                int result = 0;
                if (tenantScope == null || tenantScope.trim().equals("") || tenantScope.trim().equals("[]")) {
                    result = objectRoleRepository.fullTextCount(q, isObjectContact, appKey);
                } else {
                    result = objectRoleRepository.fullTextCount(q, isObjectContact, appKey, getTenantData(tenantScope));
                }
                Count count = new Count(result);
                return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).header("Expires", new Date().toString()).body(count);
            }
        } catch (Exception e) {
            logException("count", e);
            throw new RestException(RestErrorMessages.COUNT_FAILED, "Count resource");
        }

        return super.count(request, headers);
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<ObjectRole> create(@RequestBody ObjectRole objectContact, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        // To check unique object mapping Need to discuss about the combinations of object id with service then we will add the
        // unique check in the object mapping
        String tenantId = Long.toString(objectContact.getTenantId());
        MultivaluedMap<String, String> multiValuedMap = new MultivaluedHashMap<>();
        multiValuedMap.put("tenantIds", List.of(tenantId));
        ScopeConstraint scopeConstraint;
        int count = 0;
        if (!objectContact.getAppKey().equalsIgnoreCase("CMS")) {
            try {
                scopeConstraint = scopeConstraintDal.getByScopeName("ActionRule");
                if (scopeConstraint != null) {
                    ConstraintData[] constraintDataArray = externalDbDal.getData(scopeConstraint.getApplicationName(), scopeConstraint.getSqlQuery(), multiValuedMap);
                    for (int i = 0; i < constraintDataArray.length; i++) {
                        if (objectContact.getObjectKey().equals(constraintDataArray[i].getId())) {
                            count = 1;
                            break;
                        }
                    }
                    if (count != 1) {
                        log.error("More than one ActionRule returned");
                    }
                }
            } catch (NoResultException e) {
                log.debug("create; NoResultException= {}", e);
            } catch (Exception e) {
                log.error("create; exception={}; message={}", e.getClass().getName(), e.getMessage());
                log.error("create; exception {}", e);
                throw new ErrorInfoException("ActionRule not found in SSTOB database");
            }
        }
        int result = 0;
        try {
            result = objectContactRepository.objectNameSearch(objectContact.getObjectId().trim(), objectContact.getTenantId());
        } catch (Exception e1) {
            log.warn("create;exception={}", e1);
        }
        if (result != 0) {
            log.error("create;Failed to create object, ({ }) already exist", objectContact.getObjectId().trim());
            logException("create;exception={}", new RestException(RestErrorMessages.CREATE_OBJECT_FAILED, "Failed to create resource"));
            throw new RestException(RestErrorMessages.CREATE_OBJECT_FAILED, "Failed to create resource", objectContact.getObjectId());
        }
        ResponseEntity<ObjectRole> response = super.create(objectContact);
        ObjectRole createdObjectRole = super.readById(objectContact.getId()).getBody();
        log.debug("create; response={}", response);
        try {
            userRest.createAuditLog(TargetOperations.OBJECTCONTACT_TARGET_NAME, TargetOperations.CREATE_OPERATION, ContactAuditUtil.convertToJSON(createdObjectRole, TargetOperations.CREATE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("create;exception={}", e);
        }
        return response;
    }

    @DeleteMapping(value = "/deleteImportedActionRuleMapping", produces = MediaType.APPLICATION_JSON)
    public String deleteImportedActionRuleMapping(@RequestHeader org.springframework.http.HttpHeaders headers, @QueryParam("tenantName") String tenantName) throws IOException {
        log.info("deleteImportedActionRuleMapping; tenantName={}", tenantName);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        try {
            return cacheManager.deleteAndrefreshCacheForActionRuleMapping(tenantName, new AuditLogInfo(userId, headers.get("clientIp").get(0), "tenantName", "Tenant", "Delete"));
        } catch (ErrorInfoException e) {
            log.warn("deleteImportedActionRuleMapping;exception={}", e);
            throw e;
        }
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<ObjectRole> update(@PathVariable("id") long id, ObjectRole objectContact, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        ResponseEntity<ObjectRole> response = null;
        ObjectRole savedObjectRole = super.readById(id).getBody();
        int result = 0;
        if (!savedObjectRole.getObjectId().trim().equalsIgnoreCase(objectContact.getObjectId().trim())) {
            try {
                result = objectContactRepository.objectNameSearch(objectContact.getObjectId().trim(), objectContact.getTenantId());
            } catch (Exception e1) {
                log.warn("update;exception={}", e1);
            }
            if (result != 0) {
                log.error("update;Failed to update object, ({ }) already exist", objectContact.getObjectId().trim());
                logException("update;exception={}", new RestException(RestErrorMessages.UPDATE_OBJECT_FAILED, "Failed to update resource"));
                throw new RestException(RestErrorMessages.UPDATE_OBJECT_FAILED, "Failed to update resource", objectContact.getObjectId().trim());
            }
        }
        response = super.update(id, objectContact);
        ObjectRole newObjectRole = super.readById(id).getBody();
        try {
            userRest.createAuditLog(TargetOperations.OBJECTCONTACT_TARGET_NAME, TargetOperations.UPDATE_OPERATION, ContactAuditUtil.compareObject(savedObjectRole, newObjectRole), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("update;exception={}", e);
        }
        return response;
    }

    @PutMapping(value = "/actionRuleUpdate", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<ObjectRole> updateObjectId(HttpServletRequest servletRequest, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        ResponseEntity<ObjectRole> response = null;
        ObjectRole updateObjectRole = null;
        ObjectRole savedObjectRole = null;
        try {
            Map<String, String[]> parameterMap = servletRequest.getParameterMap();
            MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
            parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
            MultivaluedMap<String, String> queryParams = uriInfo;
            log.info("updateObjectId;  queryParams = {}", queryParams);
            long tenantId = Long.parseLong(queryParams.getFirst("tenantId"));
            String actionRuleId = queryParams.getFirst("actionRuleId");
            String actionRule = queryParams.getFirst("actionRule");
            savedObjectRole = objectContactRepository.readObjectRole(actionRuleId, tenantId);
            updateObjectRole = savedObjectRole;
            if (updateObjectRole != null) {
                updateObjectRole.setObjectId(actionRule);
                response = super.update(updateObjectRole.getId(), updateObjectRole);
                ObjectRole newObjectRole = super.readById(updateObjectRole.getId()).getBody();
            }
            return response;
        } catch (Exception e) {
            log.warn("updateObjectId;exception={}", e);
        }
        return response;
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") long id, @RequestHeader org.springframework.http.HttpHeaders headers) {

        ResponseEntity<ObjectRole> res = super.readById(id);
        ObjectRole objectContact = res.getBody();
        synchronized (this) {
            super.deleteById(id);
        }
        try {
            userRest.createAuditLog(TargetOperations.OBJECTCONTACT_TARGET_NAME, TargetOperations.DELETE_OPERATION, ContactAuditUtil.convertToJSON(objectContact, TargetOperations.DELETE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("deleteById;exception={}", e);
        }
    }

    @DeleteMapping("/actionRuleDelete")
    public void deleteByObjecId(HttpServletRequest servletRequest, @RequestHeader org.springframework.http.HttpHeaders headers) {
        ObjectRole savedObjectRole = null;
        try {
            Map<String, String[]> parameterMap = servletRequest.getParameterMap();
            MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
            parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
            MultivaluedMap<String, String> queryParams = uriInfo;
            log.info("deleteByObjecId;  queryParams = {}", queryParams);
            long tenantId = Long.parseLong(queryParams.getFirst("tenantId"));
            String actionRuleId = queryParams.getFirst("actionRuleId");
            savedObjectRole = objectContactRepository.readObjectRole(actionRuleId, tenantId);
            if (savedObjectRole != null) {
                super.deleteById(savedObjectRole.getId());
            }
        } catch (Exception e) {
            log.warn("deleteByObjecId;exception={}", e);
        }
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = "/contacts", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Object[]> queryContacts(@QueryParam("o") String objectId, @QueryParam("r") @DefaultValue("0") long contactRoleId, @QueryParam("rn") String contactRoleName, @QueryParam("t") String timestamp, @QueryParam("a") @DefaultValue("0") Long addressType, @QueryParam("an") String addressTypeName) {

        log.debug("queryContacts; {o:'{}', r:'{}', rn:'{}', t:'{}', a:'{}', an:'{}'}", objectId, contactRoleId, contactRoleName, timestamp, addressType, addressTypeName);
        if (contactRoleName != null && contactRoleName.length() > 0) {
            Long id = mapContactRole(contactRoleName);
            if (id != null) {
                contactRoleId = id;
            }
        }
        Calendar t = null;
        if (timestamp != null) {
            try {
                t = ScheduleEvaluator.parseTimestampToUtcCalendar(timestamp);
            } catch (Exception e) {
                log.warn("queryContracts; Failed to parse timestamp {}: {}", timestamp, e);
            }
        }
        if (addressTypeName != null && addressTypeName.length() > 0) {
            Long id = mapAddressType(addressTypeName);
            if (id != null) {
                addressType = id;
            }
        }

        List<ObjectRole> list = repository.getQuery().filter("objectId", objectId).filter("contactRoleId", contactRoleId).list();

        if (list.size() > 1) {
            log.error("query; expected 0 or 1 ObjectContact intances, found={}", list.size());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        }
        if (list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        ObjectRole instance = list.get(0);
        List<Contact> contactList = new LinkedList<Contact>();
        for (Contact contact : instance.getContactList()) {
            if (ScheduleEvaluator.evaluate(getSchedule(contact), t) != 0) {
                if (addressType == null || contact.getAddressTypeId() == addressType) {
                    contactList.add(contact);
                }
            }
        }
        Contact[] contactArray = new Contact[contactList.size()];
        contactList.toArray(contactArray);

        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).body(contactArray);
    }

    @GetMapping(value = "/contacts2", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Contact[]> queryContacts2(@QueryParam("objectId") String objectId, @QueryParam("contactRole") String contactRole, @QueryParam("addressType") String addressType, @QueryParam("timestamp") String timestamp, @QueryParam("tenantId") String tenantId) throws Exception {

        log.debug("queryContacts2; objectId={}", objectId);
        log.debug("queryContacts2; contactRole={}", contactRole);
        log.debug("queryContacts2; addressType={}", addressType);
        log.debug("queryContacts2; timestamp={}", timestamp);
        log.debug("queryContacts2; tenantId={}", tenantId);

        List<Contact> contactList = null;
        if (tenantId == null || tenantId.trim().equals("") || tenantId.trim().isEmpty()) {
            contactList = contactRepository.queryContacts(objectId, contactRole, addressType);
        } else {
            contactList = contactRepository.queryContacts(objectId, contactRole, addressType, Long.parseLong(tenantId));
        }

        Calendar t = null;
        if (timestamp != null) {
            try {
                t = ScheduleEvaluator.parseTimestampToUtcCalendar(timestamp);
            } catch (Exception e) {
                log.warn("queryContacts2; Failed to parse timestamp {}: {}", timestamp, e.getMessage());
            }
        }

        List<Contact> filteredList = new LinkedList<Contact>();
        for (Contact contact : contactList) {
            if (ScheduleEvaluator.evaluate(getSchedule(contact), t) != 0) {
                filteredList.add(contact);
            }
        }
        contactList = filteredList;

        Contact[] contactArray = new Contact[contactList.size()];
        contactList.toArray(contactArray);
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).body(contactArray);
    }

    @GetMapping(value = "/dispatchMap", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Map<String, Object>> queryDispatchMap(@QueryParam("tenantId") long tenantId, @QueryParam("actionRule") String actionRule, @QueryParam("lifecycle") String lifecycle, @QueryParam("channel") String channel, @QueryParam("atmSchedule") String atmSchedule, @QueryParam("timestamp") String timestamp) throws Exception {
        log.debug("queryDispatchMap; Tenant={} ; actionRule ={},lifecycle={},atmSchedule={},channel={},timestamp={}", tenantId, actionRule, lifecycle, atmSchedule, channel, timestamp);
        String response = null;
        List<Object[]> objectContactList = null;
        List<Contact> dispatchContactList = null;
        if (channel != null && !channel.isEmpty()) {
            dispatchContactList = contactRepository.queryDispatchMapWithChannel(tenantId, actionRule, lifecycle, atmSchedule, channel);
        } else if (lifecycle != null && !lifecycle.isEmpty() && atmSchedule != null && !atmSchedule.isEmpty()) {
            dispatchContactList = contactRepository.queryDispatchMap(tenantId, actionRule, lifecycle, atmSchedule);
        } else {
            //To do need to provide all contacts
        }

        //dummy code for fetch user by branch starts
		/* MultivaluedMap<String, String> queryMap = new MultivaluedMapImpl();
     	 queryMap.putSingle("organizationName", branchName);
     	 queryMap.putSingle("tenantId", String.valueOf(tenantId)); //optional
     	 OptionFilter optionFilter = new OptionFilter(queryMap);
     	 Options options = new Options(optionFilter);
     	 List<User> users = userDal.getList(options);
		 //for ur contact u can do: contact.setUser(user);*/
        //dummy code for fetch user by branch ends

        Calendar t = null;
        if (timestamp != null) {
            try {
                t = ScheduleEvaluator.parseTimestampToUtcCalendar(timestamp);
            } catch (Exception e) {
                log.warn("queryDispatchMap; Failed to parse timestamp {}: {}", timestamp, e.getMessage());
            }
        }

        log.debug("queryDispatchMap;before evaluate dispatchContactList size ={}", dispatchContactList.size());
        List<Contact> filteredList = new LinkedList<Contact>();
        for (Contact contact : dispatchContactList) {
            if (ScheduleEvaluator.evaluate(getSchedule(contact), t) != 0) {
                filteredList.add(contact);
            }
        }
        dispatchContactList = filteredList;
        log.debug("queryDispatchMap;after evaluate dispatchContactList size ={}", dispatchContactList.size());
        Map<String, Object> jsonMap = prepareJsonForContacts(dispatchContactList);
        log.debug("queryDispatchMap;jsonMap={}", jsonMap);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(jsonMap);
    }

    private Map<String, Object> prepareJsonForContacts(List<Contact> dispatchContactList) {
        log.info("prepareJsonForContacts;dispatchContactList={}", dispatchContactList.size());
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        if (dispatchContactList != null && !dispatchContactList.isEmpty()) {
            List<Object> jsonContactList = new ArrayList<Object>();
            for (Contact contact : dispatchContactList) {
                if (!jsonMap.containsKey("partyName")) {
                    jsonMap.put("partyName", contact.getName());
                }
                Map<String, Object> temp = new HashMap<String, Object>();
                Map<String, Object> userTemp = new HashMap<String, Object>();
                userTemp.put("firstName", contact.getUser().getFirstName());
                userTemp.put("lastName", contact.getUser().getLastName());
                userTemp.put("address", contact.getAddress());
                userTemp.put("CC", contact.isContactCC());
                if (contact.getAddressTypeId() != null) {
                    userTemp.put("addressTypeId", (contact.getAddressTypeId()));
                }
                temp.put("user", userTemp);
                temp.put("lifeCycle", contact.getLifecycle().getName());
                temp.put("contactType", contact.getType().getName());
                temp.put("level", contact.getLevel().getName());
                temp.put("contactChannel", contact.getChannel().getName());
                temp.put("atmSchedule", contact.getAtmSchedule().getName());
                Map<String, Object> duriationTemp = new HashMap<String, Object>();
                duriationTemp.put("name", contact.getDuration().getName());
                duriationTemp.put("baseValueMinutes", contact.getDuration().getBaseValueMinutes());
                duriationTemp.put("graceValueMinutes", contact.getDuration().getGraceValueMinutes());
                temp.put("duration", duriationTemp);
                temp.put("template", contact.getTemplate());
                jsonContactList.add(temp);
            }
            jsonMap.put("partyDetails", jsonContactList);
        }
        return jsonMap;
    }

    private MultivaluedMap<String, String> getQueryMapForUserSearch(String address, String branchOrUserFromFilter, long tenantId) {
        MultivaluedMap<String, String> queryMap = new MultivaluedHashMap<>();
        if (isPatternMatched(deploymentUtil.getUserMatchingRegexForDispatch(), address)) {
            queryMap.putSingle("userName", branchOrUserFromFilter);
        } else {
            queryMap.putSingle("organizationName", branchOrUserFromFilter);
        }
        queryMap.putSingle("tenantId", String.valueOf(tenantId));
        queryMap.putSingle("variableName", deploymentUtil.getVariableForUserIVRDispatchSeq());
        return queryMap;
    }

    private void getContactForSMSChannel(Contact contact, User user, List<Contact> dispatchContactList) {
        String contactSMSAddress = ContactDispatcherUtil.getAddressForChannel("SMS", user);
        if (contactSMSAddress != null && !contactSMSAddress.isEmpty() && !contactSMSAddress.equals("")) {
            Contact tempSMSContact = new Contact();
            tempSMSContact.setAtmSchedule(contact.getAtmSchedule());
            tempSMSContact.setChannel(contact.getChannel());
            tempSMSContact.setContactMapping(contact.getContactMapping());
            tempSMSContact.setCreatedTime(contact.getCreatedTime());
            tempSMSContact.setUpdatedTime(contact.getUpdatedTime());
            tempSMSContact.setDuration(contact.getDuration());
            tempSMSContact.setLevel(contact.getLevel());
            tempSMSContact.setLifecycle(contact.getLifecycle());
            tempSMSContact.setObjectRole(contact.getObjectRole());
            tempSMSContact.setSeqNum(contact.getSeqNum());
            tempSMSContact.setName(contact.getName());
            tempSMSContact.setTemplate(contact.getTemplate());
            tempSMSContact.setType(contact.getType());
            tempSMSContact.setWaitForNextContact(contact.isWaitForNextContact());
            tempSMSContact.setAddress(contactSMSAddress);
            // Added for CC
            tempSMSContact.setContactCC(contact.isContactCC());
            tempSMSContact.setUser(user);
            log.trace("getContactForSMSChannel;tempSMSContact={}", tempSMSContact);
            dispatchContactList.add(tempSMSContact);
        }
    }

    private void getContactForEmailChannel(Contact contact, User user, List<Contact> dispatchContactList) {
        String contactEmailAddress = ContactDispatcherUtil.getAddressForChannel("Email", user);
        if (contactEmailAddress != null && !contactEmailAddress.isEmpty() && !contactEmailAddress.equals("")) {
            Contact tempEmailContact = new Contact();
            if (contact.getAddressTypeId() != null) {
                tempEmailContact.setAddressTypeId(contact.getAddressTypeId());
            }
            tempEmailContact.setAtmSchedule(contact.getAtmSchedule());
            tempEmailContact.setChannel(contact.getChannel());
            tempEmailContact.setContactMapping(contact.getContactMapping());
            tempEmailContact.setCreatedTime(contact.getCreatedTime());
            tempEmailContact.setUpdatedTime(contact.getUpdatedTime());
            tempEmailContact.setDuration(contact.getDuration());
            tempEmailContact.setLevel(contact.getLevel());
            tempEmailContact.setLifecycle(contact.getLifecycle());
            tempEmailContact.setObjectRole(contact.getObjectRole());
            tempEmailContact.setSeqNum(contact.getSeqNum());
            tempEmailContact.setName(contact.getName());
            tempEmailContact.setTemplate(contact.getTemplate());
            tempEmailContact.setType(contact.getType());
            tempEmailContact.setWaitForNextContact(contact.isWaitForNextContact());
            tempEmailContact.setAddress(contactEmailAddress);
            // Added for CC address
            tempEmailContact.setContactCC(contact.isContactCC());
            tempEmailContact.setUser(user);
            log.trace("getContactForEmailChannel;tempEmailContact={}", tempEmailContact);
            dispatchContactList.add(tempEmailContact);
        }
    }

    private void getContactForVoiceChannel(Contact contact, User user, List<Contact> dispatchContactList) {
        String contactVoiceAddress = ContactDispatcherUtil.getAddressForChannel("Voice", user);
        if (contactVoiceAddress != null && !contactVoiceAddress.isEmpty() && !contactVoiceAddress.equals("")) {
            Contact tempVoiceContact = new Contact();
            tempVoiceContact.setAtmSchedule(contact.getAtmSchedule());
            tempVoiceContact.setChannel(contact.getChannel());
            tempVoiceContact.setContactMapping(contact.getContactMapping());
            tempVoiceContact.setCreatedTime(contact.getCreatedTime());
            tempVoiceContact.setUpdatedTime(contact.getUpdatedTime());
            tempVoiceContact.setDuration(contact.getDuration());
            tempVoiceContact.setLevel(contact.getLevel());
            tempVoiceContact.setLifecycle(contact.getLifecycle());
            tempVoiceContact.setObjectRole(contact.getObjectRole());
            tempVoiceContact.setSeqNum(contact.getSeqNum());
            tempVoiceContact.setName(contact.getName());
            tempVoiceContact.setTemplate(contact.getTemplate());
            tempVoiceContact.setType(contact.getType());
            tempVoiceContact.setWaitForNextContact(contact.isWaitForNextContact());

            tempVoiceContact.setAddress(contactVoiceAddress);
            // Added for CC
            tempVoiceContact.setContactCC(contact.isContactCC());
            tempVoiceContact.setUser(user);
            log.trace("getContactForVoiceChannel;tempVoiceContact={}", tempVoiceContact);
            dispatchContactList.add(tempVoiceContact);
        }
    }

    private void getContactForOtherChannel(Contact contact, User user, List<Contact> dispatchContactList) {
        Contact tempContact = new Contact();
        tempContact.setAtmSchedule(contact.getAtmSchedule());
        tempContact.setChannel(contact.getChannel());
        tempContact.setContactMapping(contact.getContactMapping());
        tempContact.setCreatedTime(contact.getCreatedTime());
        tempContact.setUpdatedTime(contact.getUpdatedTime());
        tempContact.setDuration(contact.getDuration());
        tempContact.setLevel(contact.getLevel());
        tempContact.setLifecycle(contact.getLifecycle());
        tempContact.setObjectRole(contact.getObjectRole());
        tempContact.setSeqNum(contact.getSeqNum());
        tempContact.setName(contact.getName());
        tempContact.setTemplate(contact.getTemplate());
        tempContact.setType(contact.getType());
        tempContact.setWaitForNextContact(contact.isWaitForNextContact());
        tempContact.setAddress("");
        tempContact.setContactCC(contact.isContactCC());
        tempContact.setUser(user);
        log.trace("getContactForOtherChannel;tempContact={}", tempContact);
        dispatchContactList.add(tempContact);
    }

    @SuppressWarnings("static-access")
    @GetMapping(value = "/dispatchMapWithNextAvailableUser", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Map<String, Object>> queryDispatchMapWithNextAvailableUser(@QueryParam("tenantId") long tenantId, @QueryParam("actionRule") String actionRule, @QueryParam("lifecycle") String lifecycle, @QueryParam("channel") String channel, @QueryParam("atmSchedule") String atmSchedule, @QueryParam("additionalFilter") String additionalFilter, @QueryParam("timestamp") String timestamp) throws Exception {
        log.debug("queryDispatchMapWithNextAvailableUser; Tenant={} ; actionRule ={},lifecycle={},atmSchedule={},channel={},additionalFilter={},timestamp={}", tenantId, actionRule, lifecycle, atmSchedule, channel, additionalFilter, timestamp);
        String response = null;
        List<Contact> dispatchContactList = null;
        if (channel != null && !channel.isEmpty() && atmSchedule != null && !atmSchedule.isEmpty()) {
            dispatchContactList = contactRepository.queryDispatchMapWithChannel(tenantId, actionRule, lifecycle, atmSchedule, channel);
        } else if (atmSchedule != null && !atmSchedule.isEmpty()) {
            dispatchContactList = contactRepository.queryDispatchMap(tenantId, actionRule, lifecycle, atmSchedule);
        } else {
            dispatchContactList = contactRepository.queryDispatchMapWithoutAtmSched(tenantId, actionRule, lifecycle);
        }
        log.debug("queryDispatchMapWithNextAvailableUser;dispatchContactList before branch check size ={}", dispatchContactList.size());
        List<ContactAvailableTime> result = prepareDispatchMapContacts(dispatchContactList, additionalFilter, channel, tenantId, timestamp);
        Map<String, Object> jsonMap = prepareJsonForContactsWithAvalableTime(result);
        log.debug("queryDispatchMapWithNextAvailableUser;jsonMap={}", jsonMap);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(jsonMap);
    }

    private List<ContactAvailableTime> prepareDispatchMapContacts(List<Contact> dispatchContactList, String additionalFilter, String channel, long tenantId, String timestamp) throws IOException {
        List<Contact> dispatchContactListForBranch = new ArrayList<Contact>(dispatchContactList);

        for (Contact contact : dispatchContactListForBranch) {
            String branchOrUserFromFilter = null;
            boolean branchUserFound = false;
            Long tenanatIdForSharedUser = 0L;
            Boolean isUserShared = false;
            Boolean isOrgShared = false;
            //RBAC-1468 starts


            if (additionalFilter != null && !additionalFilter.isEmpty()) {
                Map<String, String> additionalFilterMap = jsonToMap(additionalFilter);
                if (contact.getAddress() != null && isPatternMatched(orgNameContactMappingRegex, contact.getAddress())) {
                    branchOrUserFromFilter = additionalFilterMap.get(contact.getAddress().replaceAll("%", "").toLowerCase());
                    //null check & user/org name switch has to be done here, otherwise it will include empty/not-found filters, because of underlying
                    //problem in userDal
                    int userIdFromFilter = 0;

                    try {
                        userIdFromFilter = Integer.parseInt(branchOrUserFromFilter);
                        User userFromId = userDal.getById(userIdFromFilter);
                        if (userFromId != null && userFromId.getIsShared()) {
                            Long organisationId = userFromId.getOrganizationId();
                            Organization org = orgDal.getById(organisationId);
                            tenanatIdForSharedUser = (org != null) ? org.getTenantId() : 100L;
                            isUserShared = userFromId.getIsShared();
                        }
                    } catch (Exception e) {
                        log.info("Not a long value");
                    }
                    try {
                        Organization org = orgDal.getOrganizationByOrganizationName(branchOrUserFromFilter, tenantId);
                        if (org == null) {
                            org = orgDal.getOrganizationByOrganizationName(branchOrUserFromFilter, 100L);
                            isOrgShared = (org != null) ? org.getIsShared() : false;
                        }
                    } catch (Exception e) {
                        log.info("#Not a shared organisation");
                    }
                    //RBAC-1468 end
                    if (branchOrUserFromFilter != null && !branchOrUserFromFilter.isEmpty() && isPatternMatched(deploymentUtil.getUserMatchingRegexForDispatch(), contact.getAddress())) {
                        String branchOrUserFromFilterCopy = null;
                        try {
                            branchOrUserFromFilterCopy = branchOrUserFromFilter;
                            branchOrUserFromFilter = null;
                            branchOrUserFromFilter = Lookup.getUserNameWithNull(Integer.valueOf(branchOrUserFromFilterCopy));
                        } catch (Exception e) {
                            log.error("queryDispatchMapWithNextAvailableUser; assuming branchOrUserFromFilter as null, originalValue={}; " + "additionalFilterMap= {}; exception= {}; ", branchOrUserFromFilterCopy, additionalFilterMap, e);
                        }
                    }
                }
            }
            if (channel != null && !channel.isEmpty() && isPatternMatched(orgNameContactMappingRegex, contact.getAddress()) && branchOrUserFromFilter != null && !branchOrUserFromFilter.trim().isEmpty()) {
                branchUserFound = true;
                dispatchContactList.remove(contact);
                log.info("queryDispatchMapWithNextAvailableUser;branch found for contact = {}; channel={} ", contact, channel);
//			    MultivaluedMap<String, String> queryMap = getQueryMapForUserSearch(contact.getAddress(), branchOrUserFromFilter, tenantId);
                MultivaluedMap<String, String> queryMap = null;
                if (isUserShared)
                    queryMap = getQueryMapForUserSearch(contact.getAddress(), branchOrUserFromFilter, tenanatIdForSharedUser);
                else if (isOrgShared)
                    queryMap = getQueryMapForUserSearchForSharedOrg(contact.getAddress(), branchOrUserFromFilter, 100); //use 100 because if the org is shared the tenant id is always 100;
                else queryMap = getQueryMapForUserSearch(contact.getAddress(), branchOrUserFromFilter, tenantId);
                OptionFilter optionFilter = new OptionFilter(queryMap);
                OptionSort optionSort = new OptionSort(List.of("variableValue"));
                Options options = new Options(optionFilter, optionSort);
                List<User> branchUserList = null;
                try {
                    branchUserList = userDal.getListForDispatch(options);
                } catch (Exception e) {
                    //handled case when the variableValue might not be int, so revert back to original call & log error
                    log.error("queryDispatchMapWithNextAvailableUser; getListForDispatch failed; trying with getList; Exception={}", e);
                    options = new Options(optionFilter);
                    branchUserList = userDal.getList(options);
                }
                log.debug("queryDispatchMapWithNextAvailableUser;branchUserList size = {}", branchUserList.size());
                for (User user : branchUserList) {
                    Contact tempContact = new Contact();
                    if (contact.getAddressTypeId() != null) {
                        tempContact.setAddressTypeId(contact.getAddressTypeId());
                    }
                    tempContact.setAtmSchedule(contact.getAtmSchedule());
                    tempContact.setChannel(contact.getChannel());
                    tempContact.setContactMapping(contact.getContactMapping());
                    tempContact.setCreatedTime(contact.getCreatedTime());
                    tempContact.setUpdatedTime(contact.getUpdatedTime());
                    tempContact.setDuration(contact.getDuration());
                    tempContact.setLevel(contact.getLevel());
                    tempContact.setLifecycle(contact.getLifecycle());
                    tempContact.setObjectRole(contact.getObjectRole());
                    tempContact.setSeqNum(contact.getSeqNum());
                    tempContact.setName(contact.getName());
                    //tempContact.setParty(contact.getParty());
                    //tempContact.setAddressTypeId(contact.getAddressTypeId());
                    tempContact.setTemplate(contact.getTemplate());
                    tempContact.setType(contact.getType());
                    tempContact.setWaitForNextContact(contact.isWaitForNextContact());
                    String contactAddress = ContactDispatcherUtil.getAddressForChannel(channel, user);
                    tempContact.setAddress(contactAddress);
                    // Added for CC address
                    tempContact.setContactCC(contact.isContactCC());
                    tempContact.setUser(user);
                    log.trace("queryDispatchMapWithNextAvailableUser;tempContact={}", tempContact);
                    dispatchContactList.add(tempContact);
                }
            } else if (isPatternMatched(orgNameContactMappingRegex, contact.getAddress()) && branchOrUserFromFilter != null && !branchOrUserFromFilter.trim().isEmpty()) {
                branchUserFound = true;
                dispatchContactList.remove(contact);
                log.info("queryDispatchMapWithNextAvailableUser;branch found for contact = {}", contact);
//			     MultivaluedMap<String, String> queryMap = getQueryMapForUserSearch(contact.getAddress(), branchOrUserFromFilter, tenantId);
                MultivaluedMap<String, String> queryMap = null;
                if (isUserShared)
                    queryMap = getQueryMapForUserSearch(contact.getAddress(), branchOrUserFromFilter, tenanatIdForSharedUser);
                else if (isOrgShared)
                    queryMap = getQueryMapForUserSearchForSharedOrg(contact.getAddress(), branchOrUserFromFilter, 100); //use 100 because if the org is shared the tenant id is always 100;
                else queryMap = getQueryMapForUserSearch(contact.getAddress(), branchOrUserFromFilter, tenantId);
                OptionFilter optionFilter = new OptionFilter(queryMap);
                OptionSort optionSort = new OptionSort(List.of("variableValue"));
                Options options = new Options(optionFilter, optionSort);
                List<User> branchUserList = null;
                try {
                    branchUserList = userDal.getListForDispatch(options);
                } catch (Exception e) {
                    //handled case when the variableValue might not be int, so revert back to original call & log error
                    log.error("queryDispatchMapWithNextAvailableUser; getListForDispatch failed; trying with getList; Exception={}", e);
                    options = new Options(optionFilter);
                    branchUserList = userDal.getList(options);
                }
                log.debug("queryDispatchMapWithNextAvailableUser;branchUserList size = {}", branchUserList.size());
                String contactChannel = contact.getChannel().getCodeValue();
                log.debug("Channel = {}", contactChannel);
                for (User user : branchUserList) {
					/*
					 * if channel is dynamic, then populate the dispatchContactList with user
						based on channel received in additionalFilter
					 */
                    boolean otherChannelUser = false; //for EDI in %CHANNEL%
                    if (contactChannel.equalsIgnoreCase(deploymentUtil.getPercentageChannelVariable())) {
                        Map<String, String> additionalFilterMap = jsonToMap(additionalFilter);
                        String channelFoundInFilter = additionalFilterMap.get(contact.getAddress().replaceAll("%", "").toLowerCase() + deploymentUtil.getPercentageChannelVariableSuffix().toLowerCase());
                        if (channelFoundInFilter != null && !channelFoundInFilter.isEmpty()) {
                            if (channelFoundInFilter.equalsIgnoreCase("SMS")) {
                                replacePercentageChannelData(contact, "SMS");
                                contactChannel = "SMS";
                            } else if (channelFoundInFilter.equalsIgnoreCase("Email")) {
                                replacePercentageChannelData(contact, "Email");
                                contactChannel = "Email";
                            } else if (channelFoundInFilter.equalsIgnoreCase("Voice")) {
                                replacePercentageChannelData(contact, "Voice");
                                contactChannel = "Voice";
                            } else {
                                replacePercentageChannelData(contact, channelFoundInFilter);
                                contactChannel = channelFoundInFilter;
                                otherChannelUser = true;
                            }
                        } else {
                            log.error("queryDispatchMapWithNextAvailableUser; Channel not found for %CHANNEL% contact={}; additionalFilterMap={};", contact, additionalFilterMap);
                        }
                    }
                    if (contactChannel.equalsIgnoreCase("SMS")) { //if channel is SMS, then populate the dispatchContactList with user having SMS
                        getContactForSMSChannel(contact, user, dispatchContactList);
                    } else if (contactChannel.equalsIgnoreCase("Email")) { //if channel is Email, then populate the dispatchContactList with user having Email
                        getContactForEmailChannel(contact, user, dispatchContactList);
                    } else if (contactChannel.equalsIgnoreCase("Voice")) { //if channel is Voice, then populate the dispatchContactList with user having Voice
                        getContactForVoiceChannel(contact, user, dispatchContactList);
                    } else if (otherChannelUser) {
                        getContactForOtherChannel(contact, user, dispatchContactList);
                    }
                }
            }
            if (!branchUserFound && contact.getAddress() != null && isPatternMatched(orgNameContactMappingRegex, contact.getAddress())) {
                dispatchContactList.remove(contact);
            }
        }
        log.debug("queryDispatchMapWithNextAvailableUser;after branch check dispatchContactList size ={}", dispatchContactList.size());


        Calendar t = null;
        if (timestamp != null) {
            try {
                t = ScheduleEvaluator.parseTimestampToUtcCalendar(timestamp);
            } catch (Exception e) {
                log.warn("queryDispatchMapWithNextAvailableUser; Failed to parse timestamp {}: {}", timestamp, e.getMessage());
            }
        }
        List<ContactAvailableTime> result = new LinkedList<ContactAvailableTime>();
        for (Contact contact : dispatchContactList) {
            Schedule schedule = getSchedule(contact);
            if (schedule != null) {
                Calendar availableTime = ScheduleEvaluator.findAvailableTime(schedule, t);
                if (availableTime == null) {
                    if (t == null) {
                        t = Calendar.getInstance(TimeZone.getTimeZone(schedule.getTimeZone()));
                    }
                    Calendar endOfDay = Calendar.getInstance(t.getTimeZone());
                    endOfDay.setTimeInMillis(t.getTimeInMillis());
                    endOfDay.add(Calendar.DAY_OF_MONTH, 1);
                    availableTime = ScheduleEvaluator.findAvailableTimeNextSchedule(getSchedule(contact, true), endOfDay);
                }
                if (ScheduleEvaluator.evaluate(schedule, t) != 0) {
                    result.add(new ContactAvailableTime(availableTime != null ? availableTime.getTime() : null, contact, true));
                } else {
                    result.add(new ContactAvailableTime(availableTime != null ? availableTime.getTime() : null, contact, false));
                }
            } else {
                result.add(new ContactAvailableTime(null, contact, false));
            }

        }
        return result;
    }

    @SuppressWarnings("static-access")
    @GetMapping(value = "/checkUserAvailabilityForDispatch", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Map<String, Object>> checkUserAvailabilityForDispatch(@QueryParam("userId") Integer userId, @QueryParam("timestamp") String timestamp, @QueryParam("timezone") String timezone) throws Exception {
        log.debug("checkUserAvailabilityForDispatch; userId ={},timezone={},timestamp={}", userId, timestamp);


        Map<String, Object> jsonMap = new HashMap<String, Object>();
        String username = null;
        String firstName = null;
        String lastName = null;
        Date nextAvailableTime = null;
        Boolean isAvailable = Boolean.FALSE;

        if (userId != null && userId > 0) {
            User userDet = userDal.getById(userId);
            if (userDet != null) {
                username = userDet.getUserName();
                firstName = userDet.getFirstName();
                lastName = userDet.getLastName();
                Contact contact = new Contact();
                contact.setUser(userDet);
                Schedule schedule = getSchedule(contact);

                if (schedule != null) {
                    Calendar t = null;
                    if (timestamp != null) {
                        try {
                            t = ScheduleEvaluator.parseTimestampToUtcCalendar2(timestamp, timezone, schedule);
                        } catch (Exception e) {
                            log.warn("checkUserAvailabilityForDispatch; Failed to parse timestamp {}: {}", timestamp, e.getMessage());
                        }
                    }
                    Calendar availableTime = ScheduleEvaluator.findAvailableTime(schedule, t);
                    if (availableTime == null) {
                        if (t == null) {
                            t = Calendar.getInstance(TimeZone.getTimeZone(schedule.getTimeZone()));
                        }
                        Calendar endOfDay = Calendar.getInstance(t.getTimeZone());
                        endOfDay.setTimeInMillis(t.getTimeInMillis());
                        endOfDay.add(Calendar.DAY_OF_MONTH, 1);
                        availableTime = ScheduleEvaluator.findAvailableTimeNextSchedule(getSchedule(contact, true), endOfDay);
                    }
                    if (ScheduleEvaluator.evaluate(schedule, t) != 0) {
//	        			nextAvailableTime = availableTime!=null?availableTime.getTime():null;
                        isAvailable = Boolean.TRUE;
                    } else {
                        nextAvailableTime = availableTime != null ? availableTime.getTime() : null;
                        isAvailable = Boolean.FALSE;
                    }
                }
            }
        }

        jsonMap.put("username", username);
        jsonMap.put("firstName", firstName);
        jsonMap.put("lastName", lastName);
        jsonMap.put("nextAvailableTime", nextAvailableTime);
        jsonMap.put("isAvailable", isAvailable);
        jsonMap.put("userId", userId);
        log.debug("checkUserAvailabilityForDispatch;jsonMap={}", jsonMap);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(jsonMap);
    }
    // RBAC-2130 end

    //RBAC-2130 start
    @SuppressWarnings("static-access")
    @GetMapping(value = "/dispatchMapWithNextAvailableUserBySeq", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Map<String, Object>> queryDispatchMapWithNextAvailableUserV2(@QueryParam("tenantId") long tenantId, @QueryParam("actionRule") String actionRule, @QueryParam("lifecycle") String lifecycle, @QueryParam("channel") String channel, @QueryParam("atmSchedule") String atmSchedule, @QueryParam("additionalFilter") String additionalFilter, @QueryParam("timestamp") String timestamp, @QueryParam("includeCC") Boolean includeCC) throws Exception {
        log.debug("dispatchMapWithNextAvailableUserBySeq; Tenant={} ; actionRule ={},lifecycle={},atmSchedule={},channel={},additionalFilter={},timestamp={},includeCC={}", tenantId, actionRule, lifecycle, atmSchedule, channel, additionalFilter, timestamp, includeCC);
        String response = null;
        List<Contact> dispatchContactList = null;
        if (channel != null && !channel.isEmpty()) {
            dispatchContactList = contactRepository.queryDispatchMapWithChannelBySeq(tenantId, actionRule, lifecycle, atmSchedule, channel, includeCC);
        } else {
            dispatchContactList = contactRepository.queryDispatchMapBySeq(tenantId, actionRule, lifecycle, atmSchedule, includeCC);
        }
        log.debug("dispatchMapWithNextAvailableUserBySeq;dispatchContactList before branch check size ={}", dispatchContactList.size());
        List<ContactAvailableTime> result = prepareDispatchMapContacts(dispatchContactList, additionalFilter, channel, tenantId, timestamp);
        Map<String, Object> jsonMap = prepareJsonForContactsWithAvalableTimeBySeq(result);
        log.debug("queryDispatchMapWithNextAvailableUser;jsonMap={}", jsonMap);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(jsonMap);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> prepareJsonForContactsWithAvalableTimeBySeq(List<ContactAvailableTime> dispatchContactAvalableTimeList) {
        log.info("prepareJsonForContactsWithAvalableTime;dispatchContactAvalableTimeList={}", dispatchContactAvalableTimeList.size());
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        if (dispatchContactAvalableTimeList != null && !dispatchContactAvalableTimeList.isEmpty()) {
            List<Object> jsonPartyDetailsList = new ArrayList<Object>();
            Map<String, Integer> contactMappingUserList1 = new HashMap<String, Integer>();
            int i = -1;
            jsonMap.put("userDetails", jsonPartyDetailsList);
            for (ContactAvailableTime contactAvailableTime : dispatchContactAvalableTimeList) {
                try {
                    Contact contact = contactAvailableTime.getContact();
                    List<Object> templateList = new ArrayList<Object>();
                    Map<String, Object> temp = new HashMap<String, Object>();
                    Map<String, Object> contactDetailObj = new HashMap<String, Object>();
                    contactDetailObj.put("contactChannel", contact.getChannel().getName());
                    contactDetailObj.put("address", (contact.getAddress() != null && contact.getAddress().length() > 0 ? contact.getAddress() : null));
                    contactDetailObj.put("template", contact.getTemplate());
                    boolean isUserPresent = (contact.getUser() != null && contact.getUser().getUserName() != null); //handling to check if the user is present or not. User details will not be there in case of NCR-EDI or DECAL
                    if (isUserPresent) {
                        if ((!contactMappingUserList1.containsKey(contact.getUser().getUserName()))) {
                            temp.put("contactMapping", null);
                            temp.put("lifeCycle", contact.getLifecycle().getName());
                            temp.put("contactType", contact.getType().getName());
                            temp.put("level", contact.getLevel().getName());
                            temp.put("atmSchedule", contact.getAtmSchedule().getName());
                            Map<String, Object> duriationTemp = new HashMap<String, Object>();
                            duriationTemp.put("baseValueMinutes", contact.getDuration().getBaseValueMinutes());
                            temp.put("duration", duriationTemp);
                            temp.put("template", null);
                            temp.put("waitForNextContact", contact.isWaitForNextContact());
                            Map<String, Object> userTemp = new HashMap<String, Object>();
                            userTemp.put("firstName", (isUserPresent ? contact.getUser().getFirstName() : null));
                            userTemp.put("lastName", (isUserPresent ? contact.getUser().getLastName() : null));
                            userTemp.put("userName", (isUserPresent ? contact.getUser().getUserName() : null));
                            userTemp.put("userId", (isUserPresent ? contact.getUser().getUserId() : null));

                            userTemp.put("CC", contact.isContactCC());
                            if (contact.getAddressTypeId() != null) {
                                userTemp.put("addressTypeId", (contact.getAddressTypeId()));
                            }
                            com.esq.rbac.service.calendar.domain.Calendar orgCalendar = isUserPresent ? contact.getUser().getOrgCalendar() : null;
                            if (isUserPresent && orgCalendar == null) {
                                Long defaultCalendar = Lookup.getDefaultWorkCalendarIdByOrganization(contact.getUser().getOrganizationId());
                                if (defaultCalendar != null) {
                                    orgCalendar = calendarDal.getById(defaultCalendar);
                                }
                            }
                            userTemp.put("timeZone", (isUserPresent && orgCalendar != null ? orgCalendar.getTimeZone() : null));
                            userTemp.put("orgName", (isUserPresent ? Lookup.getOrganizationNameById(contact.getUser().getOrganizationId()) : null));
                            userTemp.put("orgId", contact.getUser() != null && contact.getUser().getOrganizationId() != null ? contact.getUser().getOrganizationId() : "");
                            userTemp.put("securityGroupName", (isUserPresent ? Lookup.getGroupName(contact.getUser().getGroupId()) : null));
                            userTemp.put("isAvailable", contactAvailableTime.isAvailable());
                            userTemp.put("nextAvailableTime", (!contactAvailableTime.isAvailable() ? contactAvailableTime.getAvailableTime() : ""));
                            userTemp.put("sequenceNo", contact.getSeqNum());
                            templateList.add(contactDetailObj);
                            userTemp.put("contactDetails", templateList);
                            jsonPartyDetailsList.add(temp);
                            temp.put("user", userTemp);
                            i++;
                            contactMappingUserList1.put(contact.getUser().getUserName(), i);
                        } else {
                            List partydetailsList = (List) jsonMap.get("userDetails");
                            int j = i;
                            if (contactMappingUserList1.containsKey(contact.getUser().getUserName())) {
                                j = contactMappingUserList1.get(contact.getUser().getUserName());
                            }
                            Map userDetailsMap = (Map) partydetailsList.get(j);
                            Map<String, Object> usersList = (Map<String, Object>) userDetailsMap.get("user");
                            List templateListEx = (List) usersList.get("contactDetails");
                            templateListEx.add(contactDetailObj);
                        }
                    }
                } catch (Exception e) {
                    log.error("prepareJsonForContactsWithAvalableTimeBySeq; {} ", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return jsonMap;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> prepareJsonForContactsWithAvalableTime(List<ContactAvailableTime> dispatchContactAvalableTimeList) {
        log.info("prepareJsonForContactsWithAvalableTime;dispatchContactAvalableTimeList={}", dispatchContactAvalableTimeList.size());
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        if (dispatchContactAvalableTimeList != null && !dispatchContactAvalableTimeList.isEmpty()) {
            List<Object> jsonPartyDetailsList = new ArrayList<Object>();
            Map<String, Integer> contactMappingUserList = new HashMap<String, Integer>();
            int i = -1;
            jsonMap.put("partyDetails", jsonPartyDetailsList);
            for (ContactAvailableTime contactAvailableTime : dispatchContactAvalableTimeList) {
                try {
                    Contact contact = contactAvailableTime.getContact();
                    List<Map<String, Object>> addressList = new ArrayList<Map<String, Object>>();
                    Map<String, Object> temp = new HashMap<String, Object>();
                    Map<String, Object> userTemp = new HashMap<String, Object>();
                    boolean isUserPresent = (contact.getUser() != null); //handling to check if the user is present or not. User details will not be there in case of NCR-EDI or DECAL
                    userTemp.put("firstName", (isUserPresent ? contact.getUser().getFirstName() : null));
                    userTemp.put("lastName", (isUserPresent ? contact.getUser().getLastName() : null));
                    userTemp.put("userName", (isUserPresent ? contact.getUser().getUserName() : null));
                    userTemp.put("userId", (isUserPresent ? contact.getUser().getUserId() : null));
                    userTemp.put("address", (contact.getAddress() != null && contact.getAddress().length() > 0 ? contact.getAddress() : null));
                    userTemp.put("CC", contact.isContactCC());
                    if (contact.getAddressTypeId() != null) {
                        userTemp.put("addressTypeId", (contact.getAddressTypeId()));
                    }
                    String timezone = null;
//					com.esq.rbac.model.Calendar orgCalendar=isUserPresent?contact.getUser().getOrgCalendar():null;
//				     if (isUserPresent && orgCalendar == null) {
//				      Long defaultCalendar = Lookup
//				        .getDefaultWorkCalendarIdByOrganization(contact.getUser().getOrganizationId());
//				      if (defaultCalendar != null) {
//				       orgCalendar = calendarDal.getById(defaultCalendar);
//				      }
//				     }
                    if (isUserPresent) {
                        User user = userDal.getById(contact.getUser().getUserId());
                        timezone = user.getTimeZone();
                    }
                    //userTemp.put("timeZone", (isUserPresent && orgCalendar!=null?orgCalendar.getTimeZone():null));
                    userTemp.put("timeZone", timezone);
                    userTemp.put("orgName", (isUserPresent ? Lookup.getOrganizationNameById(contact.getUser().getOrganizationId()) : null));
                    userTemp.put("orgId", contact.getUser() != null && contact.getUser().getOrganizationId() != null ? contact.getUser().getOrganizationId() : "");
                    userTemp.put("securityGroupName", (isUserPresent ? Lookup.getGroupName(contact.getUser().getGroupId()) : null));
                    userTemp.put("isAvailable", contactAvailableTime.isAvailable());
                    userTemp.put("nextAvailableTime", (!contactAvailableTime.isAvailable() ? contactAvailableTime.getAvailableTime() : ""));
                    userTemp.put("sequenceNo", contact.getSeqNum());
                    userTemp.put("template", contact.getTemplate());
                    if ((!contactMappingUserList.containsKey(contact.getContactMapping() + contact.getChannel().getName() + contact.getTemplate().getId()))) {
                        temp.put("contactMapping", contact.getContactMapping());
                        temp.put("lifeCycle", contact.getLifecycle().getName());
                        temp.put("contactType", contact.getType().getName());
                        temp.put("level", contact.getLevel().getName());
                        temp.put("contactChannel", contact.getChannel().getName());
                        temp.put("atmSchedule", contact.getAtmSchedule().getName());
                        Map<String, Object> duriationTemp = new HashMap<String, Object>();
                        duriationTemp.put("baseValueMinutes", contact.getDuration().getBaseValueMinutes());
                        temp.put("duration", duriationTemp);
                        temp.put("template", contact.getTemplate());
                        temp.put("waitForNextContact", contact.isWaitForNextContact());
                        jsonPartyDetailsList.add(temp);
                        addressList.add(userTemp);
                        temp.put("users", addressList);
                        i++;
                        contactMappingUserList.put(contact.getContactMapping() + contact.getChannel().getName() + contact.getTemplate().getId(), i);
                    } else {
                        List partydetailsList = (List) jsonMap.get("partyDetails");
                        int j = i;
                        if (contactMappingUserList.containsKey(contact.getContactMapping() + contact.getChannel().getName() + contact.getTemplate().getId())) {
                            j = contactMappingUserList.get(contact.getContactMapping() + contact.getChannel().getName() + contact.getTemplate().getId());
                        }
                        Map userDetailsMap = (Map) partydetailsList.get(j);
                        List usersList = (List) userDetailsMap.get("users");
                        usersList.add(userTemp);
                    }
                } catch (Exception e) {
                    log.error("prepareJsonForContactsWithAvalableTimeBySeq; {} ", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return jsonMap;
    }


    @GetMapping(value = "/actionRuleContacts", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Map<String, List<Map<String, Object>>>> queryActionRuleContacts(@QueryParam("tenantId") long tenantId, @QueryParam("actionRule") List<String> actionruleNameList, @QueryParam("DynamicContactsInfo") String dynamicContactsInfo) throws Exception {
        log.debug("queryActionRuleContacts; Tenant={} ; actionruleNameList ={}", tenantId, actionruleNameList);
        log.info("queryActionRuleContacts; dynamicContactsInfo ={}", dynamicContactsInfo);
        List<Contact> objectContactList = contactRepository.queryActionRuleContacts(tenantId, actionruleNameList);
        Contact[] resultArray = new Contact[objectContactList.size()];
        objectContactList.toArray(resultArray);
        Map<String, List<Map<String, Object>>> jsonMap = prepareJsonForQueryActionRuleContacts(objectContactList, dynamicContactsInfo, tenantId);
        log.info("queryActionRuleContacts;jsonMap={}", jsonMap);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(jsonMap);
    }

    private Map<String, List<Map<String, Object>>> prepareJsonForQueryActionRuleContacts(List<Contact> dispatchActionRuleContactsList, String dynamicContactsInfo, long tenantId) throws IOException {
        log.info("prepareJsonForQueryActionRuleContacts;dispatchActionRuleContactsList size={}", dispatchActionRuleContactsList.size());
        Map<String, List<Map<String, Object>>> jsonMap = new HashMap<String, List<Map<String, Object>>>();
        Map<String, String> dynamicContactsInfoMap = null;
        if (dynamicContactsInfo != null && !dynamicContactsInfo.isEmpty() && !dynamicContactsInfo.equalsIgnoreCase("{DynamicContactsInfo}")) {
            dynamicContactsInfoMap = jsonToMap(dynamicContactsInfo);
        }
        if (dispatchActionRuleContactsList != null && !dispatchActionRuleContactsList.isEmpty()) {
            for (Contact contact : dispatchActionRuleContactsList) {
                List<Map<String, Object>> addressList = new ArrayList<Map<String, Object>>();
                Map<String, Object> temp = new HashMap<String, Object>();
                Map<String, Object> userTemp = new HashMap<String, Object>();

                String branchOrUserFromFilter = null;
                String channel = contact.getChannel().getName();
                String originalAddress = contact.getAddress();
                String userAddress = contact.getAddress();
                Long tenanatIdForSharedUser = 0L;
                Boolean isUserShared = false;
                Boolean isOrgShared = false;
                boolean showBranchName = deploymentUtil.isShowBranchNameforPercentageBranch();
                log.debug("pattern channel = {}, match = {}", channel, isPatternMatched(orgNameContactMappingRegex, channel));
                if (dynamicContactsInfoMap != null && channel != null && !channel.isEmpty()) {
                    if (channel.equalsIgnoreCase(deploymentUtil.getPercentageChannelVariable())) // check for %channel%
                    {
                        String addressForChannel = (userAddress.replaceAll("%", "") + deploymentUtil.getPercentageChannelVariableSuffix()).toLowerCase();
                        channel = dynamicContactsInfoMap.get(addressForChannel);
                    }
                    //else if (isPatternMatched(orgNameContactMappingRegex, channel))
                    //channel = dynamicContactsInfoMap.get(channel.replaceAll("%", "").toLowerCase());
                }
                log.info("prepareJsonForQueryActionRuleContacts;channel={}", channel);
                if (dynamicContactsInfoMap != null && isPatternMatched(orgNameContactMappingRegex, userAddress)) {
                    branchOrUserFromFilter = dynamicContactsInfoMap.get(userAddress.replaceAll("%", "").toLowerCase());
                    //null check & user/org name switch has to be done here, otherwise it will include empty/not-found filters, because of underlying
                    //problem in userDal
                    //RBAC-1468
                    int userIdFromFilter = 0;

                    try {
                        userIdFromFilter = Integer.parseInt(branchOrUserFromFilter);
                        User userFromId = userDal.getById(userIdFromFilter);
                        if (userFromId != null && userFromId.getIsShared()) {
                            Long organisationId = userFromId.getOrganizationId();
                            Organization org = orgDal.getById(organisationId);
                            tenanatIdForSharedUser = (org != null) ? org.getTenantId() : 100L;
                            isUserShared = userFromId.getIsShared();
                        }
                    } catch (Exception e) {
                        log.info("Not a long value");
                    }
                    //RBAC-1468 end
                    if (branchOrUserFromFilter != null && !branchOrUserFromFilter.isEmpty() && isPatternMatched(deploymentUtil.getUserMatchingRegexForDispatch(), userAddress)) {
                        String branchOrUserFromFilterCopy = null;
                        try {
                            branchOrUserFromFilterCopy = branchOrUserFromFilter;
                            branchOrUserFromFilter = null;
                            branchOrUserFromFilter = Lookup.getUserNameWithNull(Integer.valueOf(branchOrUserFromFilterCopy));
                        } catch (Exception e) {
                            log.error("prepareJsonForQueryActionRuleContacts; assuming branchOrUserFromFilter as null, originalValue={}; " + "dynamicContactsInfoMap= {}; exception= {}; ", branchOrUserFromFilterCopy, dynamicContactsInfoMap, e);
                        }
                    } else userAddress = "No Data Available";
                }

                if (channel != null && !channel.isEmpty() && branchOrUserFromFilter != null && !branchOrUserFromFilter.isEmpty()) {
                    log.debug("prepareJsonForQueryActionRuleContacts;branch found for contact = {}; channel={}, showBranchName={},originalAddress = {} ", contact.getObjectRole(), channel, showBranchName, originalAddress);
                    if (originalAddress.equalsIgnoreCase("%branch%") && showBranchName) {
                        userAddress = branchOrUserFromFilter;
                    } else {
                        try {
                            Organization org = orgDal.getOrganizationByOrganizationName(branchOrUserFromFilter, tenantId);
                            if (org == null) {
                                org = orgDal.getOrganizationByOrganizationName(branchOrUserFromFilter, 100L);
                                isOrgShared = (org != null) ? org.getIsShared() : false;
                            }
                        } catch (Exception e) {
                            log.info("#Not a shared organisation");
                        }
                        MultivaluedMap<String, String> queryMap = null;
                        if (isUserShared)
                            queryMap = getQueryMapForUserSearch(userAddress, branchOrUserFromFilter, tenanatIdForSharedUser);
                        else if (isOrgShared)
                            queryMap = getQueryMapForUserSearchForSharedOrg(userAddress, branchOrUserFromFilter, 100); //use 100 because if the org is shared the tenant id is always 100;
                        else queryMap = getQueryMapForUserSearch(userAddress, branchOrUserFromFilter, tenantId);
                        OptionFilter optionFilter = new OptionFilter(queryMap);
                        OptionSort optionSort = new OptionSort(List.of("variableValue"));
                        Options options = new Options(optionFilter, optionSort);
                        List<User> branchUserList = null;
                        try {
                            branchUserList = userDal.getListForDispatch(options);
                        } catch (Exception e) {
                            //handled case when the variableValue might not be int, so revert back to original call & log error
                            log.error("prepareJsonForQueryActionRuleContacts; getListForDispatch failed; trying with getList; Exception={}", e);
                            options = new Options(optionFilter);
                            branchUserList = userDal.getList(options);
                        }
                        log.debug("prepareJsonForQueryActionRuleContacts;branchUserList size = {}", branchUserList.size());
                        userAddress = "";
                        for (User user : branchUserList) {
                            String contactAddress = ContactDispatcherUtil.getAddressForChannel(channel, user);
                            userAddress += ", " + contactAddress;
                        }
                        if (userAddress != "") userAddress = userAddress.substring(2); // ignoring ,
                    }
                }
                log.info("prepareJsonForQueryActionRuleContacts;address = {}", userAddress);
                userTemp.put("firstName", contact.getUser() != null ? contact.getUser().getFirstName() : "");
                userTemp.put("lastName", contact.getUser() != null ? contact.getUser().getLastName() : "");
                userTemp.put("address", userAddress);
                userTemp.put("CC", contact.isContactCC());
                if (contact.getAddressTypeId() != null) {
                    userTemp.put("addressTypeId", (contact.getAddressTypeId()));
                }
                temp.put("user", userTemp);
                temp.put("lifeCycle", contact.getLifecycle().getName());
                temp.put("contactType", contact.getType().getName());
                temp.put("level", contact.getLevel().getName());
                temp.put("contactChannel", channel);
                temp.put("atmSchedule", contact.getAtmSchedule().getName());
                Map<String, Object> duriationTemp = new HashMap<String, Object>();
                duriationTemp.put("name", contact.getDuration().getName());
                duriationTemp.put("baseValueMinutes", contact.getDuration().getBaseValueMinutes());
                duriationTemp.put("graceValueMinutes", contact.getDuration().getGraceValueMinutes());
                temp.put("duration", duriationTemp);
                temp.put("template", contact.getTemplate());
                if (jsonMap.containsKey(contact.getObjectRole().getObjectId())) {
                    jsonMap.get(contact.getObjectRole().getObjectId()).add(temp);
                } else {
                    addressList.add(temp);
                    jsonMap.put(contact.getObjectRole().getObjectId(), addressList);
                }
            }
        }
        return jsonMap;
    }

    private MultivaluedMap<String, String> getQueryMapForUserSearchForSharedOrg(String address, String branchOrUserFromFilter, int tenantId) {
        MultivaluedMap<String, String> queryMap = new MultivaluedHashMap<>();
        queryMap.putSingle("organizationName", branchOrUserFromFilter);
        queryMap.putSingle("tenantId", String.valueOf(tenantId));
        queryMap.putSingle("isShared", String.valueOf(true));
        queryMap.putSingle("variableName", deploymentUtil.getVariableForUserIVRDispatchSeq());
        return queryMap;
    }

    @GetMapping(value = "/contactRoleName", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<String[]> queryContactsRoleName(@QueryParam("objectId") String objectId, @QueryParam("tenantId") String tenantId) throws Exception {
        log.debug("queryContactsRoleName; objectId={} ,tenantId={}", objectId, tenantId);
        List<String> contactRoleList;
        if (tenantId == null || tenantId.trim().equals("") || tenantId.trim().isEmpty()) {
            contactRoleList = contactRoleRepository.queryContactRoleName(objectId);
        } else {
            contactRoleList = contactRoleRepository.queryContactRoleName(objectId, Long.parseLong(tenantId));
        }
        String[] contactRoleArray = new String[contactRoleList.size()];
        contactRoleList.toArray(contactRoleArray);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(contactRoleArray);
    }

    @GetMapping(value = "/contactsAvailableTime", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<ContactAvailableTime[]> queryContactsAvailableTime(@QueryParam("objectId") String objectId, @QueryParam("contactRole") String contactRole, @QueryParam("addressType") String addressType, @QueryParam("timestamp") String timestamp, @QueryParam("tenantId") String tenantId) throws Exception {

        log.debug("queryContactsAvailableTime; objectId={}; contactRole={}; addressType={}; timestamp={} ,tenantId={}", objectId, contactRole, addressType, timestamp, tenantId);

        List<Contact> contactList = null;
        if (tenantId == null || tenantId.trim().equals("") || tenantId.trim().isEmpty()) {
            contactList = contactRepository.queryContacts(objectId, contactRole, addressType);
        } else {
            contactList = contactRepository.queryContacts(objectId, contactRole, addressType, Long.parseLong(tenantId));
        }

        Calendar t = null;
        if (timestamp != null) {
            try {
                t = ScheduleEvaluator.parseTimestampToUtcCalendar(timestamp);
            } catch (Exception e) {
                log.warn("queryContactsAvailableTime; Failed to parse timestamp {}: {}", timestamp, e);
            }
        }

        List<ContactAvailableTime> result = new LinkedList<ContactAvailableTime>();
        for (Contact contact : contactList) {
            Calendar availableTime = ScheduleEvaluator.findAvailableTime(getSchedule(contact), t);
            result.add(new ContactAvailableTime(availableTime.getTime(), contact));
        }

        ContactAvailableTime[] resultArray = new ContactAvailableTime[result.size()];
        result.toArray(resultArray);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(resultArray);
    }

    @GetMapping(value = "/contactsWorkEndTime", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<ContactWorkEndTime[]> queryContactsWorkEndTime(@QueryParam("objectId") String objectId, @QueryParam("contactRole") String contactRole, @QueryParam("addressType") String addressType, @QueryParam("timestamp") String timestamp, @QueryParam("workMinutes") int workMinutes, @QueryParam("tenantId") String tenantId) throws Exception {

        log.debug("queryContactsWorkEndTime; objectId={}; contactRole={}; addressType={}; timestamp={}; workMinutes={},tenantId={}", objectId, contactRole, addressType, timestamp, workMinutes, tenantId);

        List<Contact> contactList = null;
        if (tenantId == null || tenantId.trim().equals("") || tenantId.trim().isEmpty()) {
            contactList = contactRepository.queryContacts(objectId, contactRole, addressType);
        } else {
            contactList = contactRepository.queryContacts(objectId, contactRole, addressType, Long.parseLong(tenantId));
        }

        Calendar t = null;
        if (timestamp != null) {
            try {
                t = ScheduleEvaluator.parseTimestampToUtcCalendar(timestamp);
            } catch (Exception e) {
                log.warn("queryContactsWorkEndTime; Failed to parse timestamp {}: {}", timestamp, e);
            }
        }
        if (t == null) {
            t = Calendar.getInstance();
        }

        List<ContactWorkEndTime> result = new LinkedList<ContactWorkEndTime>();
        for (Contact contact : contactList) {
            Calendar workEndTime = ScheduleEvaluator.findWorkEndTime(getSchedule(contact), t, workMinutes);
            long relativeTime = workEndTime.getTimeInMillis() - t.getTimeInMillis();
            result.add(new ContactWorkEndTime(contact, workEndTime.getTime(), relativeTime));
        }

        ContactWorkEndTime[] resultArray = new ContactWorkEndTime[result.size()];
        result.toArray(resultArray);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(resultArray);
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = "/slas", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<SLA[]> querySLAs(@QueryParam("o") String objectId, @QueryParam("r") long contactRoleId, @QueryParam("rn") String contactRoleName, @QueryParam("t") String timestamp, @QueryParam("name") String name) {

        log.debug("querySLAs; {o:'{}', r:'{}', rn:'{}', t:'{}', name:'{}'}", objectId, contactRoleId, timestamp, name);
        if (contactRoleName != null && contactRoleName.length() > 0) {
            Long id = mapContactRole(contactRoleName);
            if (id != null) {
                contactRoleId = id;
            }
        }
        Calendar t = null;
        if (timestamp != null) {
            try {
                t = ScheduleEvaluator.parseTimestampToUtcCalendar(timestamp);
            } catch (Exception e) {
                log.warn("querySLAs; Failed to parse timestamp {}: {}", timestamp, e);
            }
        }

        List<ObjectRole> list = repository.getQuery().filter("objectId", objectId).filter("contactRoleId", contactRoleId).list();

        if (list.size() > 1) {
            log.error("query; expected 0 or 1 ObjectContact intances, found={}", list.size());
            //Todo return Response.status(ClientResponse.Status.CONFLICT).build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (list.size() < 1) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        ObjectRole instance = list.get(0);
        List<SLA> slaList = new LinkedList<SLA>();
        for (SLA sla : instance.getSlaList()) {
            if (ScheduleEvaluator.evaluate(sla.getSchedule(), t) != 0) {
                if (name != null) {
                    if (name.equals(sla.getName())) {
                        slaList.add(sla);
                    }
                } else {
                    slaList.add(sla);
                }
            }

        }
        SLA[] slaArray = new SLA[slaList.size()];
        slaList.toArray(slaArray);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(slaArray);
    }

    @GetMapping(value = "/slas2", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<SLA[]> querySLAs2(@QueryParam("objectId") String objectId, @QueryParam("contactRole") String contactRole, @QueryParam("timestamp") String timestamp, @QueryParam("tenantId") String tenantId) throws Exception {

        log.debug("querySLAs2; objectId={}", objectId);
        log.debug("querySLAs2; contactRole={}", contactRole);
        log.debug("querySLAs2; timestamp={}", timestamp);
        log.debug("querySLAs2; tenantId={}", tenantId);

        List<SLA> slaList = new ArrayList<SLA>();
        if (tenantId == null || tenantId.trim().equals("") || tenantId.trim().isEmpty()) {
            // Todo  slaList = slaRepository.querySLAs(objectId, contactRole);
        } else {
            //Todo slaList = slaRepository.querySLAs(objectId, contactRole,Long.parseLong(tenantId));
        }

        Calendar t = null;
        if (timestamp != null) {
            try {
                t = ScheduleEvaluator.parseTimestampToUtcCalendar(timestamp);
            } catch (Exception e) {
                log.warn("querySLAs2; Failed to parse timestamp {}: {}", timestamp, e);
            }
        }


        List<SLA> filteredList = new LinkedList<SLA>();
        for (SLA sla : slaList) {
            if (ScheduleEvaluator.evaluate(sla.getSchedule(), t) != 0) {
                filteredList.add(sla);
            }
        }
        slaList = filteredList;

        SLA[] slaArray = new SLA[slaList.size()];
        slaList.toArray(slaArray);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(slaArray);
    }

    @Override
    protected Set<String> getFilterColumns() {
        return FILTER_COLUMNS;
    }

    @Override
    protected Set<String> getSearchColumns() {
        return SEARCH_COLUMNS;
    }

    @Override
    protected Set<String> getOrderColumns() {
        return ORDER_COLUMNS;
    }

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        log.debug("setEntityManagerFactory");
        this.entityManagerFactory = entityManagerFactory;
    }

    private Long mapContactRole(String name) {
        Long id = null;
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            TypedQuery<Long> query = em.createNamedQuery(ContactRoleQueries.FIND_CONTACT_ROLE_ID_BY_NAME, Long.class);
            query.setParameter(NAME, name);
            id = query.getSingleResult();

        } catch (NoResultException e) {
            // ignore
        } catch (Exception e) {
            log.error("mapContactRole; exception={}; message={}", e.getClass().getName(), e.getMessage());
            log.debug("mapContactRole; exception {}", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        log.debug("mapContactRole; id={}; name={}", id, name);
        return id;
    }


    private int mapTypeId(String mapFrom, String mapTo) {
        int id = 0;
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            TypedQuery<Integer> query = em.createNamedQuery(FIND_MAP_ID_BY_MAPFROM_MAPTO, Integer.class);
            log.info("mapTypeId; query = {} ", query);
            query.setParameter(MAPFROM, mapFrom);
            query.setParameter(MAPTO, mapTo);
            id = query.getSingleResult();

        } catch (NoResultException e) {
            // ignore
        } catch (Exception e) {
            log.error("mapTypeId; exception={}; message={}", e.getClass().getName(), e.getMessage());
            log.debug("mapTypeId; exception {}", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        log.info("mapTypeId; id={}; mapFrom = {} ; mapTo = {} ", id, mapFrom, mapTo);
        if (id == 0) {
            log.info("mapTypeId; insert the new values in maptype table");
            em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            MappingType newMapType = new MappingType();
            newMapType.setMapType("Custom_" + mapFrom + "_" + mapTo);
            newMapType.setMapFrom(mapFrom);
            newMapType.setMapTo(mapTo);
            newMapType.setFromFlag("FALSE");
            newMapType.setToFlag("FALSE");
            em.persist(newMapType);
            em.flush();
            em.getTransaction().commit();
            log.info("mapTypeId; id={}", newMapType.getMapTypeId());
            return newMapType.getMapTypeId();

        }
        return id;
    }


    private Long mapAddressType(String name) {
        Long id = null;
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            TypedQuery<Long> query = em.createNamedQuery(FIND_ADDRESS_TYPE_ID_BY_NAME, Long.class);
            query.setParameter(NAME, name);
            id = query.getSingleResult();

        } catch (NoResultException e) {
            // ignore
        } catch (Exception e) {
            log.error("mapAddressType; exception={}; message={}", e.getClass().getName(), e.getMessage());
            log.debug("mapAddressType; exception {}", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        log.debug("mapAddressType; id={}; name={}", id, name);
        return id;
    }

    @Autowired
    public void setContactRepository(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Autowired
    public void setSlaRepository(SLARepository slaRepository) {
        this.slaRepository = slaRepository;
    }

    @Autowired
    public void setContactRoleRepository(ContactRoleRepository contactRoleRepository) {
        this.contactRoleRepository = contactRoleRepository;
    }

    @Autowired
    public void setCalendarDal(CalendarDal calendarDal) {
        log.trace("setCalendarDal; {}", calendarDal);
        this.calendarDal = calendarDal;
    }

    @GetMapping(value = "/buttonsEnable", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Map<String, Object>> getConfigurationValue() {
        Map<String, Object> contactButtons = new HashMap<String, Object>();
        try {
            contactButtons.put(PROPERTY_ADD_BUTTTON, Boolean.parseBoolean(configuration.getString(PROPERTY_ADD_BUTTTON, DEFAULT_ADD_BUTTTON_ENABLE)));
            contactButtons.put(PROPERTY_ADD_POLICY_TO_ROLE, Boolean.parseBoolean(configuration.getString(PROPERTY_ADD_POLICY_TO_ROLE, DEFAULT_ADD_POLICY_TO_ROLE_ENABLE)));
            contactButtons.put(PROPERTY_ADD_PARTY_TO_ROLE, Boolean.parseBoolean(configuration.getString(PROPERTY_ADD_PARTY_TO_ROLE, DEFAULT_ADD_PARTY_TO_ROLE_ENABLE)));
            contactButtons.put(PROPERTY_ADD_PARTY_TO_ROLE_MAP_FROM, configuration.getString(PROPERTY_ADD_PARTY_TO_ROLE_MAP_FROM, "ActionRule"));

        } catch (Exception e) {
            log.error("getConfigurationValue; exception={}; message={}", e.getClass(), e.getMessage());
            log.debug("getConfigurationValue; exception {}", e);
        }
        return ResponseEntity.ok().body(contactButtons);
    }

    private final Schedule getSchedule(Contact contact) {
        if (contact.getUser() != null) {
            return parseUserCalendar(contact.getUser().getUserCalendar(), contact.getUser().getOrgCalendar(), contact.getUser().getOrganizationId(), contact.getUser().getUserId(), false);
        }
        return null;
    }

    private final Schedule getSchedule(Contact contact, boolean includeOrgCal) {
        if (contact.getUser() != null) {
            return parseUserCalendar(contact.getUser().getUserCalendar(), contact.getUser().getOrgCalendar(), contact.getUser().getOrganizationId(), contact.getUser().getUserId(), includeOrgCal);
        }
        return null;
    }

    public final Schedule parseUserCalendar(com.esq.rbac.service.calendar.domain.Calendar userCalendar, com.esq.rbac.service.calendar.domain.Calendar orgCalendar, Long organizationId, Integer userid, boolean includeOrgCal) {
        //order is like this:
        //1.user holiday rules
        //2.user work within period rules
        //(if user work rule within period it wont add 3,4,5,6,7 order)
        //(if user work rule not within period it will add 3,4,5,6,7 order)
        //3.org holiday within period rules
        //4.org work within period rules
        //5.user work not within period all rules
        //6.org holiday not within period rules
        //7.org work not within period all rules

        // find default org work calenar if user org work calendar is not found
        if (orgCalendar == null) {
            Long defaultCalendar = Lookup.getDefaultWorkCalendarIdByOrganization(organizationId);
            if (defaultCalendar != null) {
                orgCalendar = calendarDal.getById(defaultCalendar);
            }
        }

        List<Long> holidayCals = Lookup.getDefaultHolidayCalendarsIdByOrganization(organizationId);

        Schedule schedule = new Schedule();
        schedule.setName(userCalendar != null && userCalendar.getName() != null ? userCalendar.getName() : orgCalendar != null && orgCalendar.getName() != null ? orgCalendar.getName() : "");
//		schedule.setTimeZone(orgCalendar != null ? orgCalendar.getTimeZone() : userCalendar != null ?
//				(userCalendar.getTimeZone().equalsIgnoreCase("Not selected")? "UTC": userCalendar.getTimeZone()):"UTC");
        schedule.setTimeZone(userDal.getById(userid).getTimeZone());
//		TimeZone scheduleTimeZone = TimeZone.getTimeZone(schedule
//				.getTimeZone());
        TimeZone scheduleTimeZone = getTimeZoneByOffset(schedule.getTimeZone());
        Calendar timestamp = Calendar.getInstance(scheduleTimeZone);
        boolean ignoreOrgWorkCalendar = false;
        List<ScheduleRule> rules = new LinkedList<ScheduleRule>();
        List<ScheduleRule> userRulesNotWithInPeriod = new LinkedList<ScheduleRule>();
        List<ScheduleRule> orgHolidayRulesNotWithInPeriod = new LinkedList<ScheduleRule>();
        List<ScheduleRule> orgRulesNotWithInPeriod = new LinkedList<ScheduleRule>();
        if (userCalendar != null && userCalendar.getRules() != null && !userCalendar.getRules().isEmpty()) {
            for (com.esq.rbac.service.schedulerule.domain.ScheduleRule rbacScheduleRule : userCalendar.getRules()) {
                ScheduleRule dspScheduleRule = new ScheduleRule();
                dspScheduleRule.setDayOfWeek(rbacScheduleRule.getDayOfWeek());
                dspScheduleRule.setDescription(rbacScheduleRule.getDescription());
                dspScheduleRule.setFromDate(rbacScheduleRule.getFromDate());
                dspScheduleRule.setHour(rbacScheduleRule.getHour());
                dspScheduleRule.setIsOpen(rbacScheduleRule.getIsOpen());
                dspScheduleRule.setToDate(rbacScheduleRule.getToDate());
                if (!rbacScheduleRule.getIsOpen() && !ScheduleEvaluator.isScheduleOld(timestamp, rbacScheduleRule.getToDate()))
                    rules.add(dspScheduleRule);
                // RBAC-1123 starts
                //TODO::Ignore stale schedules
                if (!includeOrgCal
//						&& ignoreOrgWorkCalendar == false
                        && dspScheduleRule.getIsOpen()) {
                    if (!ScheduleEvaluator.isScheduleOld(timestamp, dspScheduleRule.getToDate())) {
                        rules.add(dspScheduleRule);
                        ignoreOrgWorkCalendar = true;
                    }

//					if (ScheduleEvaluator.isWithinPeriod(timestamp,
//							dspScheduleRule.getFromDate(),
//							dspScheduleRule.getToDate()) == true) {
//						ignoreOrgWorkCalendar = true;
//						rules.add(dspScheduleRule);
//					}else{
//						userRulesNotWithInPeriod.add(dspScheduleRule);
//					}
                }
                // RBAC-1123 ends

            }
            Collections.sort(rules, new Comparator<ScheduleRule>() {
                @Override
                public int compare(ScheduleRule o1, ScheduleRule o2) {
                    return Boolean.valueOf(o1.getIsOpen()).compareTo(Boolean.valueOf(o2.getIsOpen()));
                }
            });
        }

        if (holidayCals != null && !holidayCals.isEmpty()) {
            for (Long calId : holidayCals) {
                com.esq.rbac.service.calendar.domain.Calendar holCalendar = calendarDal.getById(calId);
                if (holCalendar != null && holCalendar.getRules() != null && !holCalendar.getRules().isEmpty()) {
                    for (com.esq.rbac.service.schedulerule.domain.ScheduleRule rbacScheduleRule : holCalendar.getRules()) {
                        ScheduleRule dspScheduleRule = new ScheduleRule();
                        dspScheduleRule.setDayOfWeek(rbacScheduleRule.getDayOfWeek());
                        dspScheduleRule.setDescription(rbacScheduleRule.getDescription());
                        dspScheduleRule.setFromDate(rbacScheduleRule.getFromDate());
                        dspScheduleRule.setHour(rbacScheduleRule.getHour());
                        dspScheduleRule.setIsOpen(rbacScheduleRule.getIsOpen());
                        dspScheduleRule.setToDate(rbacScheduleRule.getToDate());
                        if (!ScheduleEvaluator.isScheduleOld(timestamp, dspScheduleRule.getToDate())) {
                            rules.add(dspScheduleRule);
                        }
                        //TODO::Ignore stale schedules
//						if (ScheduleEvaluator.isWithinPeriod(timestamp,
//								dspScheduleRule.getFromDate(),
//								dspScheduleRule.getToDate()) == true) {
//							rules.add(dspScheduleRule);
//						}else{
//							orgHolidayRulesNotWithInPeriod.add(dspScheduleRule);
//						}
                    }
                }
            }
        }

        if (!ignoreOrgWorkCalendar && orgCalendar != null && orgCalendar.getRules() != null && !orgCalendar.getRules().isEmpty()) {
            for (com.esq.rbac.service.schedulerule.domain.ScheduleRule rbacScheduleRule : orgCalendar.getRules()) {
                ScheduleRule dspScheduleRule = new ScheduleRule();
                dspScheduleRule.setDayOfWeek(rbacScheduleRule.getDayOfWeek());
                dspScheduleRule.setDescription(rbacScheduleRule.getDescription());
                dspScheduleRule.setFromDate(rbacScheduleRule.getFromDate());
                dspScheduleRule.setHour(rbacScheduleRule.getHour());
                dspScheduleRule.setIsOpen(rbacScheduleRule.getIsOpen());
                dspScheduleRule.setToDate(rbacScheduleRule.getToDate());
                if (ScheduleEvaluator.isWithinPeriod(timestamp, dspScheduleRule.getFromDate(), dspScheduleRule.getToDate())) {
                    rules.add(dspScheduleRule);
                } else {
                    orgRulesNotWithInPeriod.add(dspScheduleRule);
                }
            }
        }

        if (!ignoreOrgWorkCalendar && !userRulesNotWithInPeriod.isEmpty()) {
            rules.addAll(userRulesNotWithInPeriod);
        }
        if (!ignoreOrgWorkCalendar && !orgHolidayRulesNotWithInPeriod.isEmpty()) {
            rules.addAll(orgHolidayRulesNotWithInPeriod);
        }
        if (!ignoreOrgWorkCalendar && !orgRulesNotWithInPeriod.isEmpty()) {
            rules.addAll(orgRulesNotWithInPeriod);
        }
        Collections.sort(rules, new Comparator<ScheduleRule>() {
            @Override
            public int compare(ScheduleRule o1, ScheduleRule o2) {
                return Boolean.valueOf(o1.getIsOpen()).compareTo(Boolean.valueOf(o2.getIsOpen()));
            }
        });
        log.debug("parseUserCalendar; rules ={}", rules);

        schedule.setRules(rules);
        schedule.setTimeZone(getTimeZone(schedule.getTimeZone()));
        return schedule;
    }

    @PostMapping(value = "/copyDispatchMap", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Object> updateContact(CloneObjectRole objectContact, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        // To check unique object mapping Need to discuss about the combinations of object id with service then we will add the
        // unique check in the object mapping

        List<ObjectRole> toObjectRole = objectContact.getTo();
        //ResponseEntity<ObjectRole>  resp = null;
        int flag = 0;
        ObjectRole of = objectContact.getFrom();
        ObjectRole objectFrom = objectContactRepository.readObjectRole(of.getObjectKey(), of.getTenantId());
        //	objectFrom.getf
//		@SuppressWarnings("unchecked")
//		List<ObjectRole> contactList = repository.getQuery().filter("objectId", objectFrom.getObjectId()).filter("contactRoleId", objectFrom.getContactRoleId()).list();

//		ObjectRole instance = contactList.get(0);
//
//		  List<Contact> contactsList = new LinkedList<Contact>();
//	        for (Contact contact : instance.getContactList()) {
//	            if (ScheduleEvaluator.evaluate(getSchedule(contact), t) != 0) {
//	                if (addressType == null || contact.getAddressTypeId() == addressType) {
//	                    contactsList.add(contact);
//	                }
//	            }
//	        }

        //ObjectRole objectFrom = repository.readById(of.getId());
        for (ObjectRole or : toObjectRole) {

            or.setTenantId(objectFrom.getTenantId());
            or.setContactRoleId(objectFrom.getContactRoleId());
            or.setScheduleId(objectFrom.getScheduleId());
            or.setObjectContact(objectFrom.isObjectContact());
            or.setMapTypeId(objectFrom.getMapTypeId());
            or.setObjectKey(or.getObjectKey());
            or.setContactRole(objectFrom.getContactRole());
            //or.setContactIdList(objectFrom.getContactIdList());
            or.setContacts(objectFrom.getContacts());
            or.setAppKey(objectFrom.getAppKey());
            /*



             */
            String tenantId = Long.toString(or.getTenantId());
			/*MultivaluedMap<String, String> multiValuedMap = new MultivaluedMapImpl();
			multiValuedMap.put("tenantIds", Arrays.asList(new String[]{tenantId}));
			ScopeConstraint scopeConstraint;
			int count = 0;
			try {
				scopeConstraint= scopeConstraintDal.getByScopeName("ActionRule");
			//	scopeConstraint= scopeConstraintDal.getByScopeName("ActionRule");
				if (scopeConstraint != null) {
					ConstraintData[] constraintDataArray = externalDbDal.getData(
							scopeConstraint.getApplicationName(),
							scopeConstraint.getSqlQuery(), multiValuedMap);
					for(int i = 0 ; i < constraintDataArray.length ; i++){
						if(or.getObjectKey().equals(constraintDataArray[i].getId())){
							count = 1;
							break;
						}
					}
					if(count != 1){
						throw new Exception();
					}
				}
			}catch (NoResultException e) {
				log.debug("create; NoResultException= {}", e);
				} catch (Exception e) {
					log.error("create; exception={}; message={}", e.getClass()
							.getName(), e.getMessage());
					log.debug("create; exception {}", e);
					throw new ErrorInfoException("ActionRule not found in SSTOB database");
				}*/
            int result = 0;
            try {
                result = objectContactRepository.objectNameSearch(or.getObjectId().trim(), or.getTenantId());
            } catch (Exception e) {
                log.error("Error " + e);
            }
            if (result != 0) {
                log.error("create;Failed to create object, ({ }) already exist", or.getObjectId().trim());
                logException("create;exception={}", new RestException(RestErrorMessages.CREATE_OBJECT_FAILED, "Failed to create resource"));
                throw new RestException(RestErrorMessages.CREATE_OBJECT_FAILED, "Failed to create resource", or.getObjectId());
            }
            ResponseEntity<ObjectRole> response;
            try {

                response = super.create(or);
                if (response.getStatusCodeValue() == 200) {
                    flag++;
                }

            } catch (Exception e1) {
                log.error("Error in creating objectRole" + e1);

            }


        }
        if (flag == toObjectRole.size()) {

            return ResponseEntity.ok().body(objectContact);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private TimeZone getTimeZoneByOffset(String timeZone) {
        String timeZoneOffSet = cultureDal.getOffsetFromTimeZone(timeZone);
        if (timeZoneOffSet.equals("+00:00")) {
            timeZoneOffSet = "Z";
        }
        ZoneId zId = ZoneId.of(ZoneOffset.of(timeZoneOffSet).getId());
        TimeZone tz = TimeZone.getTimeZone(zId);
        return tz;
    }
}
