package com.esq.rbac.service.usersync.rest;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.ldapuserservice.service.LdapUserService;
import com.esq.rbac.service.ldapuserservice.service.LdapUserServiceImpl;
import com.esq.rbac.service.loginservice.email.EmailDal;
import com.esq.rbac.service.systemstate.domain.SystemState;
import com.esq.rbac.service.systemstate.service.SystemStateService;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.embedded.UserIdentity;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.user.vo.UserWithLogoutData;
import com.esq.rbac.service.userexternalrecord.domain.UserExternalRecord;
import com.esq.rbac.service.usersync.domain.UserSync;
import com.esq.rbac.service.usersync.dto.UserSyncDTO;
import com.esq.rbac.service.usersync.service.UserSyncService;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.annotation.Resource;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Named
@Singleton
//@Path("/userSync")
@RestController
@RequestMapping("/userSync")
@Slf4j
public class UserSyncRest {

    public static final String CONF_LDAP_URL = "ldap.url";
    public static final String CONF_LDAP_BASE = "ldap.base";
    public static final String CONF_LDAP_USER_DN = "ldap.userDn";
    //private static final String identifier = "ldap";
    public static final String CONF_LDAP_USER_PASSWORD = "ldap.userPassword";
    private static final String PROP_LDAP_USER_MAPPING = "ldapUser.";
    //private String boolVal;
    //	private static List<String> ldapTestFields = new LinkedList<String>();
    private static final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
    };
    private UserSyncService userSyncDal;
    private UserDal userDal;
    //	private AuditLogger auditLogger;
    private SystemStateService systemStateDal;
    private Long ldapHighestCommittedUSN;
    private boolean isSyncRunning = false;
    private Configuration configuration;
    private HashMap<String, Object> systemConfig;
    private LdapUserService ldapUserService;

    public static String getCurrentUtcTime() {
        ZonedDateTime zdt = ZonedDateTime.now(ZoneOffset.UTC);
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy HH:mm");
        return zdt.toString();

    }

    @Autowired
    public void setUserSyncDal(UserSyncService userSyncDal, AuditLogService auditLogDal, EmailDal emailDal) {
        log.debug("setUserSyncDal");
        this.userSyncDal = userSyncDal;
//		this.auditLogger = new AuditLogger(auditLogDal);
        // Lookup.fillUserLookupTable(userSyncDal.getList(null));
    }

    @Autowired
    public void setUserDal(UserDal userDal) {
        log.debug("setUserDal");
        this.userDal = userDal;

    }

    @Autowired
    public void setSystemStateDal(SystemStateService systemStateDal) {
        log.debug("setSystemStateDal");
        this.systemStateDal = systemStateDal;


    }

    @Autowired
    public void setLdapUserService(LdapUserService ldapUserService) {
        this.ldapUserService = ldapUserService;
    }

    @Autowired
    @Resource(name = "propertyConfig")
    public void setConfiguration(Configuration configuration) {
        log.debug("setDependencies; configuration={}", configuration);
        this.configuration = configuration;
    }

    @GetMapping(value = "/isSync", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean isSyncRunning() {
        synchronized (UserSyncRest.class) {
            return isSyncRunning;
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserSync[]> list(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.debug("list; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));


        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<UserSync> list = new ArrayList<>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = userSyncDal.searchList(options);
        } else {
            list = userSyncDal.list(options);
        }
        UserSync[] array = new UserSync[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> count(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.debug("count; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return ResponseEntity.ok(userSyncDal.getSearchCount(options));
        } else {
            return ResponseEntity.ok(userSyncDal.getCount(options));
        }
    }

    @GetMapping("/{userSyncId}")
    public ResponseEntity<UserSync> getById(@RequestParam("userSyncId") int userSyncId) {
        log.debug("getById; userId={}", userSyncId);
        return ResponseEntity.ok(userSyncDal.getById(userSyncId));
    }

    @GetMapping(value = "/getLastSyncDate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getLastSyncDate() {
        String lastSyncDate = null;
        try {
            String systemData = (systemStateDal.getByIdentifier("ldap")).getSystemData();
            systemConfig = new ObjectMapper().readValue(systemData, typeRef);
            lastSyncDate = (String) (systemConfig.get("syncDate"));
        } catch (Exception e) {
            // log.error("systemData; Exception={};", e);
        }
        return ResponseEntity.ok(lastSyncDate);
    }

	/*@DELETE
	@Path("/forceDelete/{userSyncId}")
	public void forceDeleteById(@Context HttpHeaders headers, @PathParam("userSyncId") int userSyncId)
			throws IOException {
		log.debug("deleteById; userSyncId={}", userSyncId);
		Integer userId = Integer.parseInt(headers.getRequestHeader("userId").get(0));

		try {
			String userSyncName = userSyncDal.getById(userSyncId).getExternalRecordId();
			userSyncDal.forceDeleteById(userSyncId, new AuditLogInfo(userId,
					headers.getRequestHeader("clientIp").get(0), userSyncName, "UserSync", "Delete"));
			// Lookup.fillTenants(tenantDal.list(null));
		} catch (ErrorInfoException e) {
			throw e;
		}
	}*/

    @DeleteMapping("/{userSyncId}")
    public void deleteById(@RequestHeader HttpHeaders headers, @RequestParam("userSyncId") int userSyncId) throws IOException {
        log.debug("deleteById; userSyncId={}", userSyncId);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));

        try {
            String userSyncName = userSyncDal.getById(userSyncId).getExternalRecordId();
            userSyncDal.deleteById(userSyncId, new AuditLogInfo(userId, headers.get("clientIp").get(0), userSyncName, "UserSync", "Discard"));
            // Lookup.fillTenants(tenantDal.list(null));
        } catch (ErrorInfoException e) {
            throw e;
        }
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createUpdate(@RequestBody List<UserSyncDTO> jsonObjectList, @RequestHeader HttpHeaders headers, HttpServletRequest httpServletRequest) throws Exception {

        for (UserSyncDTO userSyncDTO : jsonObjectList) {
            JsonNode jsonObject = null;
            Gson gson1 = new Gson();
            String jsonString1 = gson1.toJson(userSyncDTO);
            try {
                jsonObject = new ObjectMapper().readTree(jsonString1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            log.info("{}", jsonObject);
            User resultUser = null;
            Integer fromDataTable = 0;

            Integer jsonUserId = null;
            try {
                fromDataTable = jsonObject.path("fromDataTable").asInt();
                jsonUserId = jsonObject.path("userId").asInt();
            } catch (Exception e) {

            }
            String username = getValueFromJsonNode(jsonObject, "userNameLdap");
            //	jsonObject.path("userNameLdap").asText();
            String externalRecordId = getValueFromJsonNode(jsonObject, "externalRecordId");
            //jsonObject.path("externalRecordId").asText();
            BeanWrapper inputUser = null;
            // String usernameFromLdap = jsonObject.getString("name");
            User user = null;
            if (jsonUserId == null || jsonUserId == 0) inputUser = new BeanWrapperImpl(new User());
            else inputUser = new BeanWrapperImpl(userDal.getById(jsonUserId));

            Iterator<String> keys = jsonObject.fieldNames();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    inputUser.setPropertyValue(key, getValueFromJsonNode(jsonObject, key));
                } catch (Exception e) {
                    // ignore
                }
            }

            Map<String, String> userFieldMappings = new HashMap<String, String>();
            Iterator<String> keysItr = configuration.getKeys();
            while (keysItr.hasNext()) {

                String key = keysItr.next();
                if (key.startsWith(PROP_LDAP_USER_MAPPING)) {
                    userFieldMappings.put(key.substring(9), configuration.getString(key));
                }
            }
            JsonNode jsonObjectFromLDAP = null;
            if (fromDataTable.equals(1)) {
                User ldapDetailsFromUser = null;
                try {
                    ldapDetailsFromUser = ldapUserService.getUserDetails(username);
                } catch (Exception e) {
                    ErrorInfoException errorInfo = new ErrorInfoException(BaseDalJpa.USER_SYNC_LDAP_CONNECTION_EXCEPTION);
                    throw errorInfo;
                }
                if (ldapDetailsFromUser != null) {
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(ldapDetailsFromUser);
                    try {
                        jsonObjectFromLDAP = new ObjectMapper().readTree(jsonString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            log.info("fromDataTable {}, jsonObjectFromLDAP {}", fromDataTable, jsonObjectFromLDAP);
            for (String key : userFieldMappings.keySet()) {
                try {
                    String userMp = userFieldMappings.get(key);
                    Object value = null;
                    if (fromDataTable.equals(1) && jsonObjectFromLDAP != null) {
                        try {
                            value = getValueFromJsonNode(jsonObjectFromLDAP, key);
                        } catch (Exception e) {
                            value = jsonObject.get(userMp);
                            continue;
                        }
                    } else {
                        value = jsonObject.get(userMp);
                    }
                    if (value == null || value.equals("null")) value = null;
                    log.debug("value for {} = {}", key, value);
                    inputUser.setPropertyValue(key, value);
                } catch (Exception e) {
                    log.error("{}", e);
                }
            }
            user = (User) inputUser.getWrappedInstance();

            Integer loggedInUserId = Integer.parseInt(headers.get("loggedInuserId").get(0));
            Long loggedInTenantId = Long.valueOf(headers.get("loggedInTenant").get(0));
            user.setLoggedInTenantId(loggedInTenantId);
            String clientIp = RBACUtil.getRemoteAddress(httpServletRequest);
            log.debug("User to be Added or updated {}", user);

            int userSyncId = jsonObject.path("userSyncId").asInt();
            UserSync userSyncDet = userSyncDal.getById(userSyncId);

            String defaultIdentityToAppend = ldapUserService.getLDAPDefaultValueForIdentity();
            String userIdentity = defaultIdentityToAppend + username;

            if (jsonUserId == null || jsonUserId == 0) {
                List<UserIdentity> lstUserIdentity = new ArrayList<>();
                lstUserIdentity.add(new UserIdentity(UserIdentity.LDAP_ACCOUNT, userIdentity));
                user.setIdentities(lstUserIdentity);
                user.setIsEnabled(true);
                user.setIsChannelTypeEmail(true);
                User reUser = userDal.create(user, loggedInUserId, "User", "Create");
                resultUser = reUser;
            } else {
                List<UserIdentity> lstUserIdentity = user.getIdentities();
                log.info("lstUserIdentity {}", lstUserIdentity);
                if (lstUserIdentity != null && !lstUserIdentity.isEmpty()) {
                    Boolean isExistIdentity = false;
                    for (UserIdentity userIdent : lstUserIdentity) {
                        if (userIdent.getIdentityType().equalsIgnoreCase(UserIdentity.LDAP_ACCOUNT)) {
                            isExistIdentity = true;
                            break;
                        }
                    }
                    if (!isExistIdentity) {
                        log.info("Setting ldap UserIdentity {} ", userIdentity);
                        lstUserIdentity.add(new UserIdentity(UserIdentity.LDAP_ACCOUNT, userIdentity));
                        user.setIdentities(lstUserIdentity);
                    }
                } else {
                    lstUserIdentity = new ArrayList<>();
                    lstUserIdentity.add(new UserIdentity(UserIdentity.LDAP_ACCOUNT, userIdentity));
                    user.setIdentities(lstUserIdentity);
                }

                //check for existing user's email Id
                if (jsonUserId != null && jsonUserId > 0 && (user.getEmailAddress() == null || user.getEmailAddress().equalsIgnoreCase("null") || user.getEmailAddress().isEmpty())) {
                    User existingUser = userDal.getById(jsonUserId);
                    if (existingUser.getEmailAddress() != null && !existingUser.getEmailAddress().isEmpty())
                        user.setEmailAddress(existingUser.getEmailAddress());
                }

                User existingUser = userDal.getById(jsonUserId);
                existingUser = userDal.getChannelTypesForTwoFactorAuth(existingUser);
                user.setIsChannelTypeEmail(existingUser.getIsChannelTypeEmail());
                user.setIsChannelTypeSMS(existingUser.getIsChannelTypeSMS());
                UserWithLogoutData updated = userDal.update(user, loggedInUserId, clientIp);
                resultUser = updated.getUser();
            }

            if (resultUser.getIdentities() != null && !resultUser.getIdentities().isEmpty()) {
                userDal.evictSecondLevelCacheById(resultUser.getUserId());
            }
            Integer userId = resultUser.getUserId();
            UserExternalRecord uexRec = userSyncDal.findExternalRecordByUserId(userId);
            if (uexRec == null) {
                userSyncDal.createUserExternalRecord(externalRecordId, userId);
            } else {
                userSyncDet.setSyncData(userSyncDet.getUpdatedSyncData());
                userSyncDet.setUpdatedSyncData(null);
            }
            userSyncDet.setStatus(LdapUserServiceImpl.LDAP_CREATED);
            userSyncDet.setUser(resultUser);
            userSyncDal.update(userSyncDet, new AuditLogInfo(loggedInUserId, clientIp, userSyncDet.getSyncData(), "UserSync", "Update"));
            // userSyncDal.updateStatus(userSyncDet);

        }

    }

    private String getValueFromJsonNode(JsonNode jsonObject, String key) throws JsonProcessingException {
        JsonNode node = jsonObject.get(key);
        String value = node != null && node.isTextual() ? node.asText() : new ObjectMapper().writeValueAsString(node);
        return value;
    }
    /*
     * @POST
     *
     * @Path("/mapUser")
     *
     * @Produces(MediaType.APPLICATION_JSON)
     *
     * @Consumes(MediaType.APPLICATION_JSON) public void
     * getUserSyncInGroupsData(JSONObject jsonObject) {
     * log.debug("getUserSyncInGroupsData; jsonObject={}", jsonObject); try {
     * Integer userId = jsonObject.getInt("userId"); String externalRecordId =
     * jsonObject.getString("externalRecordId");
     * userSyncDal.createUserExternalRecord(externalRecordId, userId); }
     * catch (Exception e) {
     * log.debug("getUserSyncInGroupsData; jsonObject={}", jsonObject); } }
     */

    @PostMapping(value = "/LdapUser", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createLdapUser(@RequestBody User user, @RequestHeader HttpHeaders headers, HttpServletRequest httpServletRequest) throws Exception {
        Integer loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
        String clientIp = headers.get("clientIp").get(0);
        log.debug("{}", user);
        /*
         * user.setOrganizationId(jsonObject.getLong("organizationId"));
         * user.setGroupId(jsonObject.getInt("groupId"));
         */

        // User reUser = userDal.create(user, loggedInUserId, "User", "LDAP Import");
        log.debug("reUser; {}", user);

        Integer userId = user.getUserId();
        UserSync userSyncDet = userSyncDal.getByExternalRecordId(user.getExternalRecordId());
        userSyncDet.setUser(user);
        userSyncDet.setStatus(LdapUserServiceImpl.LDAP_CREATED);
        UserExternalRecord uexRec = userSyncDal.findExternalRecordByUserId(userId);
        if (uexRec == null) {
            userSyncDal.createUserExternalRecord(user.getExternalRecordId(), userId);
        } else {
            userSyncDet.setSyncData(userSyncDet.getUpdatedSyncData());
            userSyncDet.setUpdatedSyncData(null);
        }
        userSyncDal.update(userSyncDet, new AuditLogInfo(loggedInUserId, clientIp, userSyncDet.getSyncData(), "UserSync", "Update"));

        //userSyncDal.updateStatus(userSyncDet);
        // String userSyncName = userSyncDal.getById(userSyncId).getExternalRecordId();

        // userSyncDal.forceDeleteById(userSyncId, new AuditLogInfo(loggedInUserId,
        // headers.getRequestHeader("clientIp").get(0), userSyncName, "UserSync",
        // "Delete"));
        return ResponseEntity.ok(user);

    }

    @GetMapping(value = "/actionSync", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> actionSync(@RequestHeader HttpHeaders headers, HttpServletRequest httpServletRequest) throws Exception {
        try {
            synchronized (UserSyncRest.class) {
                isSyncRunning = true;
            }
            //
            Integer loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
            String clientIp = headers.get("clientIp").get(0);
            //


            // get syncIdentifier value;
		/*LdapSyncRunner<Long> runner = new LdapSyncRunner<Long>();
		// UserDal userDal = super.getBean(UserDal.class);
		runner.setUserSyncDal(userSyncDal);
		runner.setSource("ldap");
		runner.setUserDal(userDal);*/
            //
            try {
                String systemData = (systemStateDal.getByIdentifier("ldap")).getSystemData();

                systemConfig = new ObjectMapper().readValue(systemData, typeRef);
            } catch (Exception e) {
                //log.error("systemData; Exception={};", e);
            }

            if (systemConfig == null) {
                ldapUserService.performFirstSync(loggedInUserId, clientIp);
                ldapHighestCommittedUSN = ldapUserService.getMaxIdentifierValue();
                //
                Map<String, String> dataObj = new HashMap<String, String>();
                dataObj.put("ldapHighestCommittedUSN", ldapHighestCommittedUSN.toString());
                dataObj.put("syncDate", getCurrentUtcTime());
                SystemState systemState = new SystemState();
                systemState.setIdentifier("ldap");

                try {
                    systemState.setSystemData(new ObjectMapper().writeValueAsString(dataObj));
                } catch (Exception e) {
                    log.error("systemState; Exception={};", e);
                }
                //userDal.create(user, loggedInUserId, "User", "Import");
                systemStateDal.create(systemState, loggedInUserId, "SystemState", "Create");
                //


            } else {
                ldapHighestCommittedUSN = Long.parseLong((String) (systemConfig.get("ldapHighestCommittedUSN")));
                Long ldapMaxIdentifierValue = ldapUserService.getMaxIdentifierValue();
                if (ldapMaxIdentifierValue > ldapHighestCommittedUSN) {

                    ldapUserService.performNextSync(ldapHighestCommittedUSN, loggedInUserId, clientIp);
                    ldapHighestCommittedUSN = ldapMaxIdentifierValue;
                } else {
                    userSyncDal.updateExistingConflicts(loggedInUserId, clientIp);
                }
                //start
                Map<String, String> dataObj = new HashMap<String, String>();
                dataObj.put("ldapHighestCommittedUSN", ldapHighestCommittedUSN.toString());
                dataObj.put("syncDate", getCurrentUtcTime());
                SystemState systemState = systemStateDal.getByIdentifier("ldap");
                //systemState.setIdentifier("ldap");

                try {
                    systemState.setSystemData(new ObjectMapper().writeValueAsString(dataObj));
                } catch (Exception e) {
                    log.error("systemState; Exception={};", e);
                }
                systemStateDal.update(systemState, new AuditLogInfo(loggedInUserId, clientIp, systemStateDal.getByIdentifier("ldap").getId().toString(), "SystemState", "Update"));
                //ends

            }

        } finally {
            synchronized (UserSyncRest.class) {
                isSyncRunning = false;
            }
        }
        // isSyncRunning(isSyncRunning);
        return ResponseEntity.ok("abc");
    }


}
