package com.esq.rbac.service.ldapuserservice.service;

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.exception.ErrorInfo;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.ldapuserservice.service.LdapUserService;
import com.esq.rbac.service.loginservice.service.LoginServiceImpl;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.usersync.domain.UserSync;
import com.esq.rbac.service.usersync.service.UserSyncService;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.EncryptionUtils;
import com.esq.rbac.service.util.EsqPasswordDecrypter;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.fop.util.LogUtil;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.*;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.CollectingAuthenticationErrorCallback;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.GreaterThanOrEqualsFilter;
import org.springframework.ldap.filter.LessThanOrEqualsFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
@Slf4j
public class LdapUserServiceImpl implements LdapUserService {

    private LdapContextSource ldapContextSource;
    @Autowired
    public void setLdapContextSource(LdapContextSource ldapContextSource) {
        this.ldapContextSource = ldapContextSource;
    }


    private LdapTemplate ldapTemplate;

    @Autowired
    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }


    private LdapContextSource ldapContextSourceRoot;

    @Autowired
    public void setLdapContextSourceRoot(LdapContextSource ldapContextSourceRoot) {
        this.ldapContextSourceRoot = ldapContextSourceRoot;
    }

    private LdapTemplate ldapTemplateRoot;

    @Autowired
    public void setLdapTemplateRoot(LdapTemplate ldapTemplateRoot) {
        this.ldapTemplateRoot = ldapTemplateRoot;
    }
    /* RBAC-1259 END */

    private Configuration configuration;

    @Autowired
    public void setDependencies(@Qualifier("DatabaseConfigurationWithCache") Configuration configuration) {
        log.trace("setDependencies; configuration={}", configuration);
        this.configuration = configuration;
        try {
            reloadConfiguration();
        } catch (Exception e) {
            log.error("setDependencies; Exception={}", e);
        }
    }

    private static final String OBJECT_CLASS = "objectclass";
    private static final String OBJECT_CLASS_USER = "person";
    private static final String PROP_LDAP_USER_MAPPING = "ldapUser.";
    private static String PROP_LDAP_USERNAME_MAPPING = "cn";
    private static final String PROP_LDAP_TEST_MAPPING = "ldapTestFields";
    //	public static final String PROP_LDAP_USER_SEARCH_FIELDS = "ldapSearchFields";
    public static final String CONF_LDAP_IMPORT_ENABLED_ALL = "all.ldapUsers.importEnabled";
    public static final String CONF_LDAP_IMPORT_ENABLED_BY_NAME = "userName.ldapUsers.importEnabled";
    public static final String CONF_LDAP_URL = "ldap.url";
    public static final String CONF_LDAP_BASE = "ldap.base";
    public static final String CONF_LDAP_USER_DN = "ldap.userDn";
    public static final String CONF_LDAP_USER_PASSWORD = "ldap.userPassword";
    private static final String ERROR_CONNECTION = "ldapConnectionFailure";
    private static final String ERROR_CONFIGURATION = "ldapConfigurationFailure";
    private static final String ERROR_AUTHENTICATION = "ldapAuthenticationFailure";
    private static final String ERROR_BASE_PATH = "baseOrNotFoundError";
    private static final String ERROR_NO_RECORD_FOUND = "noLDAPRecordFound";
    //private static final String DEFAULT_SEARCH_USER_FIELDS = "cn,sAMAccountName";

    /* RBAC-1259 START */
    public static final Integer LDAP_PENDING = 0;
    public static final Integer LDAP_CREATED = 1;
    public static final Integer LDAP_CONFLICT = 2;
    public static final Integer LDAP_UPDATED = 3;
    public static final Integer LDAP_DISCARD = 4;
    public static final String CONF_LDAP_DETAILS_SYNC_ENABLED = "ldapUsers.details.syncEnabled";
    public static final String CONF_LDAP_APPEND_IDENTITY_ENABLED = "ldap.addUserIdentityEntry.enabled";
    public static final String CONF_LDAP_APPEND_IDENTITY_VALUE = "ldap.addUserIdentityEntry.value";
    public static final String LDAP_MAPPED_USERNAME = "userNameLdap";


    private static final Integer CONF_LDAP_SEARCH_SCOPE = 2;

    private static final String CONF_LDAP_SYNC_ROOT_DN = "";

    private static final String CONF_LDAP_SYNC_IDENTIFIER = "highestCommittedUSN";

    private static final String CONF_LDAP_SEARCH_SYNC_IDENTIFIER = "uSNChanged";

    /* RBAC-1259 END */
    private static Map<String, String> userFieldMappings = new LinkedHashMap<String, String>();
    private static List<String> ldapTestFields = new LinkedList<String>();
    //	private static List<String> ldapUserSearchFields = new LinkedList<String>();
    private static List<String> listAttributesToSync = Arrays
            .asList(new String[] {"name", "displayName", "objectCategory","userPrincipalName","highestCommittedUSN", "objectGUID", "homephone","cn","distinguishedName","sAMAccountName","sn","givenName"});


    private UserSyncService userSyncDal;

    @Autowired
    public void setUserSyncDal(UserSyncService userSyncDal) {
        this.userSyncDal = userSyncDal;
    }

    private UserDal userDal;

    @Autowired
    public void setUserDal(UserDal userDal) {
        this.userDal = userDal;
    }


    private DeploymentUtil deploymentUtil;

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        this.deploymentUtil = deploymentUtil;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void initializeLdapTemplate() throws Exception {
        ldapContextSource = new LdapContextSource();
        ldapContextSource.setUserDn(configuration.getString(CONF_LDAP_USER_DN));
        ldapContextSource.setPassword(decryptPassword(configuration.getString(CONF_LDAP_USER_PASSWORD)));
        ldapContextSource.setUrl(configuration.getString(CONF_LDAP_URL));
        ldapContextSource.setBase(configuration.getString(CONF_LDAP_BASE));
        ldapContextSource.afterPropertiesSet();
        ldapTemplate = new LdapTemplate();
        ldapTemplate.setContextSource(ldapContextSource);
        ldapTemplate.setIgnorePartialResultException(true);
        /* RBAC-1259 START */
        initializeLdapTemplateForRoot();
        /* RBAC-1259 END */
    }

    /* RBAC-1259 START */
    @Transactional(propagation = Propagation.REQUIRED)
    private void initializeLdapTemplateForRoot() throws Exception {
        ldapContextSourceRoot = new LdapContextSource();
        ldapContextSourceRoot.setUserDn(configuration.getString(CONF_LDAP_USER_DN));
        EsqPasswordDecrypter esqPasswordDecrypter = new EsqPasswordDecrypter();
        esqPasswordDecrypter.setInput(configuration.getString(CONF_LDAP_USER_PASSWORD));
        ldapContextSourceRoot.setPassword((String) esqPasswordDecrypter.getObject());
        ldapContextSourceRoot.setUrl(configuration.getString(CONF_LDAP_URL));
        ldapContextSourceRoot.setBase(CONF_LDAP_SYNC_ROOT_DN);
        ldapContextSourceRoot.afterPropertiesSet();
        ldapTemplateRoot = new LdapTemplate();
        ldapTemplateRoot.setContextSource(ldapContextSourceRoot);
        ldapTemplateRoot.setIgnorePartialResultException(true);

    }

    /* RBAC-1259 END */
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
                if (((String) key).startsWith(PROP_LDAP_USER_MAPPING)) {
                    userFieldMappings.put(((String) key).substring(9, ((String) key).length()),
                            properties.getProperty((String) key));
                }
            }
            PROP_LDAP_USERNAME_MAPPING = properties.getProperty(PROP_LDAP_USER_MAPPING + "userName");
            String[] testFields = properties.getProperty(PROP_LDAP_TEST_MAPPING).toLowerCase().split(",");
//			String[] userSearchFields = DEFAULT_SEARCH_USER_FIELDS.toLowerCase().split(",");
//			if (properties.getProperty(PROP_LDAP_USER_SEARCH_FIELDS) != null) {
//				userSearchFields = properties.getProperty(PROP_LDAP_USER_SEARCH_FIELDS).toLowerCase().split(",");
//			}
            ldapTestFields = Arrays.asList(testFields);
//			ldapUserSearchFields = Arrays.asList(userSearchFields);
            log.info("mappings ={}", userFieldMappings);
        } catch (Exception e) {
            log.error("static; Exception={}", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void reloadConfiguration() throws Exception {
        if (configuration == null) {
            return;
        }
        log.trace("reloadConfiguration; all.ldapUsers.importEnabled={} userName.ldapUsers.importEnable={}",
                configuration.getBoolean(CONF_LDAP_IMPORT_ENABLED_ALL, false),
                configuration.getBoolean(CONF_LDAP_IMPORT_ENABLED_BY_NAME, false));
        if (!configuration.getBoolean(CONF_LDAP_IMPORT_ENABLED_ALL, false)
                && !configuration.getBoolean(CONF_LDAP_IMPORT_ENABLED_BY_NAME, false)) {
            ldapContextSource = null;
            ldapTemplate = null;
            return;
        }
        initializeLdapTemplate();
    }

    private static class PersonAttributesMapper implements AttributesMapper<User> {
        public User mapFromAttributes(Attributes attrs) throws NamingException {
            BeanWrapper user = new BeanWrapperImpl(new User());
            try {
                Set<String> arrKeys = userFieldMappings.keySet();
                for (String key : arrKeys) {
                    String getKeyVak = userFieldMappings.get(key);
                    Attribute attrValue = attrs.get(getKeyVak);
                    if (attrValue != null) {
                        try {
                            log.debug("key = {}, value = {} ", key, attrValue.get());
                            user.setPropertyValue(key, attrValue.get());
                        } catch (NamingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("PersonAttributesMapper; mapFromAttributes; Exception={}", e);
            }
            return (User) user.getWrappedInstance();
        }
    }

    private static class CustomAttributesMapper implements AttributesMapper<Map<String, List<String>>> {
        @SuppressWarnings("unchecked")
        public Map<String, List<String>> mapFromAttributes(Attributes attrs) throws NamingException {
            Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
            try {
                final List<Attribute> attributesList = Collections.list((NamingEnumeration<Attribute>) attrs.getAll());
                for (Attribute attr : attributesList) {
                    if (ldapTestFields.contains(attr.getID().toLowerCase())) {
                        if (result.containsKey(attr.getID())) {
                            result.get(attr.getID()).add(attr.get().toString());
                        } else {
                            List<String> tempValues = new LinkedList<String>();
                            tempValues.add(attr.get().toString());
                            result.put(attr.getID(), tempValues);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("CustomAttributesMapper; mapFromAttributes; Exception={}", e);
            }
            return result;
        }
    }

    @Override
    public boolean checkUser(String username, String password) {
        String filterUserName = userFieldMappings.get("userName");

        try {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter(OBJECT_CLASS, OBJECT_CLASS_USER));
            filter.and(new EqualsFilter(filterUserName, username));
            // log.info("filter ={} ", filter.toString());
            CollectingAuthenticationErrorCallback errorCallback = new CollectingAuthenticationErrorCallback();
            boolean result = ldapTemplate.authenticate("", filter.toString(), password, errorCallback);
            if (!result) {
                Exception error = errorCallback.getError();
                log.error("{}",error.getMessage());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public List<String> getAllUserNames() {

        List<String> userNamesList = new LinkedList<String>();
        try {
            userNamesList = ldapTemplate.search(query().where(OBJECT_CLASS).is(OBJECT_CLASS_USER),
                    new AttributesMapper<String>() {
                        public String mapFromAttributes(Attributes attrs) throws NamingException {
                            return attrs.get(PROP_LDAP_USERNAME_MAPPING).get().toString();
                        }
                    });
            if (userNamesList == null || userNamesList.isEmpty()) {
                log.error("getAllUserNames; NO RECORDS FOUND");
                throw new ErrorInfoException(ERROR_NO_RECORD_FOUND);
            }
        } catch (Exception e) {
            if (!(e instanceof ErrorInfoException)) {
                log.error("getUserDetails; Exception={}", e);
            }
            handleException(e);
        }
        return userNamesList;
    }

    @Override
    public User getUserDetails(String searchParam) {
        log.debug("search params " + searchParam);
        User user = null;
        String filterUserName = userFieldMappings.get("userName");

        try {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter(OBJECT_CLASS, OBJECT_CLASS_USER));
            filter.and(new EqualsFilter(filterUserName, searchParam));
            //	log.info("filter {} ", filter.toString());
            List<User> users = ldapTemplate.search(query().filter(filter), new PersonAttributesMapper());
            if (users == null || users.isEmpty()) {
                log.error("getUserDetails; NO RECORDS FOUND");
                throw new ErrorInfoException(ERROR_NO_RECORD_FOUND);
            }
            user = users.get(0);
            //	log.info("user fetched = {} ", user);
        } catch (Exception e) {
            if (!(e instanceof ErrorInfoException)) {
                log.error("getUserDetails; Exception={}", e);
            }
        }
        return user;
    }
    @Override
    public boolean isUserImportEnabled() {
        return (configuration.getBoolean(CONF_LDAP_IMPORT_ENABLED_BY_NAME, false) && isLdapEnabled());
    }

    @Override
    public boolean isBulkImportEnabled() {
        return (configuration.getBoolean(CONF_LDAP_IMPORT_ENABLED_ALL, false) && isLdapEnabled());
    }

    @Override
    public boolean isLdapDetailsSyncEnabled() {
        return (configuration.getBoolean(CONF_LDAP_DETAILS_SYNC_ENABLED, false) && isLdapEnabled());
    }

    @Override
    public boolean isLdapEnabled() {
        return configuration.getBoolean(LoginServiceImpl.CONF_WINDOWS_LOGIN_ENABLED, false);
    }

    @Override
    public String getLDAPDefaultValueForIdentity() {
        String value = "";
        if(configuration.getBoolean(CONF_LDAP_APPEND_IDENTITY_ENABLED, false)) {
            value =  configuration.getString(CONF_LDAP_APPEND_IDENTITY_VALUE, "");
            if(value == null || value.equalsIgnoreCase("null"))
                value = "";
        }
        return value;
    }

    @Override
    public Map<String, List<String>> testConnection(String url, String userDn, String password, String base) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        LdapContextSource testContext = new LdapContextSource();
        testContext.setUserDn(userDn);
        try {
            testContext.setPassword(decryptPassword(password));
        } catch (Exception e1) {
            log.debug("testConnection; esqPasswordDecrypter; Exception={}", e1);
            handleException(e1);
        }
        try {
            testContext.setUrl(url);
            testContext.setBase(base);
            testContext.afterPropertiesSet();
            LdapTemplate testLdapTemplate = new LdapTemplate();
            testLdapTemplate.setContextSource(testContext);
            testLdapTemplate.setIgnorePartialResultException(true);
            String arr[] = userDn.split(",");
            String userDet = arr[0];
            String cnArr[] = userDet.split("=");
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter(OBJECT_CLASS, OBJECT_CLASS_USER));
            filter.and(new EqualsFilter(cnArr[0], cnArr[1]));
            result = testLdapTemplate
                    .search(query().filter(filter), new CustomAttributesMapper())
                    .subList(0, 1).get(0);
            if (result == null || result.isEmpty()) {
                log.error("testConnection; ERROR_CONNECTION");
                throw new ErrorInfoException(ERROR_CONNECTION);
            }
        } catch (Exception e) {
            log.debug("testConnection; Exception={}", e);
            handleException(e);
        }
        return result;
    }

    private String decryptPassword(String password) throws Exception {
        if(deploymentUtil.isEnableStandardPasswordHashing()){
            EncryptionUtils decrypt = new EncryptionUtils();
            decrypt.setInput(configuration.getString(CONF_LDAP_USER_PASSWORD));
            return (String) decrypt.getObject();
        }else{
            EsqPasswordDecrypter esqPasswordDecrypter = new EsqPasswordDecrypter();
            esqPasswordDecrypter.setInput(configuration.getString(CONF_LDAP_USER_PASSWORD));
            return (String) esqPasswordDecrypter.getObject();
        }
    }

    private void handleException(Exception e) {
        if (e instanceof ErrorInfoException) {
            throw (ErrorInfoException) e;
        }
        if (e instanceof ConfigurationException) {
            throw new ErrorInfoException(ERROR_CONFIGURATION);
        }
        if (e instanceof CommunicationException) {
            throw new ErrorInfoException(ERROR_CONNECTION);
        }
        if (e instanceof AuthenticationException) {
            throw new ErrorInfoException(ERROR_AUTHENTICATION);
        }
        if (e instanceof NameNotFoundException || e instanceof InvalidNameException) {
            throw new ErrorInfoException(ERROR_BASE_PATH);
        } else {
            throw new ErrorInfoException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public void performFirstSync(Integer loggedInUserId, String clientIp) throws Exception {
        Long maxIdentifier = getMaxIdentifierValue();
        log.debug("performFirstSync; maxIdentifier={};", maxIdentifier);

        String filter = getFirstSyncSearchFilter(maxIdentifier);
        log.debug("performFirstSync; filter={}; ", filter);
        String[] attributesToSync = new String[listAttributesToSync.size()];
        listAttributesToSync.toArray(attributesToSync);
        // search(String base, String filter, int searchScope, String[] attrs,
        // AttributesMapper<Long> mapper)
        ldapTemplateRoot.search(configuration.getString(CONF_LDAP_BASE), filter, CONF_LDAP_SEARCH_SCOPE, attributesToSync,
                new AttributesMapper<String>() {
                    @Override
                    public String mapFromAttributes(Attributes attributes) throws NamingException {
                        log.debug("performFirstSync; attributes={};", attributes);
                        HashMap<String, Object> userDetailMap = new HashMap<String, Object>();
                        for (String attr : listAttributesToSync) {
                            // log.info("performFirstSync; attr={}; value={};",
                            // attr,
                            // attributes.get(attr)!=null?attributes.get(attr).get():"null");
                            if (attributes.get(attr) != null) {
                                /*
                                 * if(attr=="objectGUID"){ byte[] guid = (byte[])attributes.get(attr).get();
                                 * LdapSyncRunner.bytesToUUID(guid); }
                                 */
                                userDetailMap.put(attr, attributes.get(attr).get());
                            }
                        }

                        // log.info("performFirstSync; userDetailMap={};",
                        // userDetailMap);
                        try {
                            UserSync usersync = new UserSync();
                            usersync.setStatus(LDAP_PENDING);
                            String getUrname = (String) userDetailMap.get(PROP_LDAP_USERNAME_MAPPING);
                            userDetailMap.put(LDAP_MAPPED_USERNAME, getUrname);

                            User userInRBAC = userDal.getByUserName(getUrname);
                            if (userInRBAC != null) {
                                usersync.setStatus(LDAP_CONFLICT);
                                usersync.setUser(userInRBAC);
                            }

                            usersync.setSyncData(new ObjectMapper().writeValueAsString(userDetailMap));
                            usersync.setUpdatedSyncData(null);
                            // Set<String> keys = userDetailMap.keySet();
                            // for (String key : keys) {
                            // if (key == "objectGUID" && userDetailMap.get(key) != null) {
                            if (userDetailMap != null && userDetailMap.containsKey("objectGUID")
                                    && userDetailMap.get("objectGUID") != null) {
//								MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
                                String guidStr = (String) attributes.get("objectGUID").get();
                                byte[] guid = guidStr.getBytes();
                                UUID u = bytesToUUID(guid);
                                if(u !=null) {
//								queryParams.add("externalRecordId", u.toString());
                                    usersync.setExternalRecordId(u.toString());
                                    // userDetailMap.get("objectGUID").toString());
//								Options options = new Options(new OptionFilter(queryParams));
//									if ((userSyncDal.list(options)).size() == 0) {
                                    userSyncDal.create(usersync, new AuditLogInfo(loggedInUserId, clientIp,
                                            u.toString(), "UserSync", "Create"));
                                    log.debug("performFirstSync; userSyncDalCreate={};", userSyncDal);
//									} else {
//										usersync.setUserSyncId(userSyncDal.list(options).get(0).getUserSyncId());
//										userSyncDal.update(usersync, new AuditLogInfo(loggedInUserId, clientIp,
//												u.toString(), "UserSync", "Update"));
//										log.debug("performFirstSync; userSyncDalUpdate={};", userSyncDal);
//								}
                                }
                            }
                            // }

                        } catch (Exception e) {
                            log.error("performFirstSync; Exception={};", e);

                        }

                        return "";
                    }
                });}

    @Override
    public Long getMaxIdentifierValue() throws Exception {
        Long identifier = 0L;
        try {

            identifier = ldapTemplateRoot.lookup(CONF_LDAP_SYNC_ROOT_DN, new String[] {CONF_LDAP_SYNC_IDENTIFIER},
                    new AttributesMapper<Long>() {
                        @Override
                        public Long mapFromAttributes(Attributes attributes) throws NamingException {
                            log.debug("performFirstSync; attributes={};", attributes);
                            String value = (String) attributes.get(CONF_LDAP_SYNC_IDENTIFIER).get();
                            return Long.parseLong(value);
                        }

                    });
        } catch (Exception e) {
            if (!(e instanceof ErrorInfoException)) {
                log.error("getUserDetails; Exception={}", e);
            }
            handleException(e);
        }

        return identifier;
    }

    public static UUID bytesToUUID(byte[] bytes) {
        if (bytes != null && bytes.length == 16) {
            long msb = bytes[3] & 0xFF;
            msb = msb << 8 | (bytes[2] & 0xFF);
            msb = msb << 8 | (bytes[1] & 0xFF);
            msb = msb << 8 | (bytes[0] & 0xFF);

            msb = msb << 8 | (bytes[5] & 0xFF);
            msb = msb << 8 | (bytes[4] & 0xFF);

            msb = msb << 8 | (bytes[7] & 0xFF);
            msb = msb << 8 | (bytes[6] & 0xFF);

            long lsb = bytes[8] & 0xFF;
            lsb = lsb << 8 | (bytes[9] & 0xFF);
            lsb = lsb << 8 | (bytes[10] & 0xFF);
            lsb = lsb << 8 | (bytes[11] & 0xFF);
            lsb = lsb << 8 | (bytes[12] & 0xFF);
            lsb = lsb << 8 | (bytes[13] & 0xFF);
            lsb = lsb << 8 | (bytes[14] & 0xFF);
            lsb = lsb << 8 | (bytes[15] & 0xFF);

            return new UUID(msb, lsb);
        }else if (bytes != null && bytes.length == 15) {
            long msb = bytes[3] & 0xFF;
            msb = msb << 8 | (bytes[2] & 0xFF);
            msb = msb << 8 | (bytes[1] & 0xFF);
            msb = msb << 8 | (bytes[0] & 0xFF);

            msb = msb << 8 | (bytes[5] & 0xFF);
            msb = msb << 8 | (bytes[4] & 0xFF);

            msb = msb << 8 | (bytes[7] & 0xFF);
            msb = msb << 8 | (bytes[6] & 0xFF);

            long lsb = bytes[8] & 0xFF;
            lsb = lsb << 8 | (bytes[9] & 0xFF);
            lsb = lsb << 8 | (bytes[10] & 0xFF);
            lsb = lsb << 8 | (bytes[11] & 0xFF);
            lsb = lsb << 8 | (bytes[12] & 0xFF);
            lsb = lsb << 8 | (bytes[13] & 0xFF);
            lsb = lsb << 8 | (bytes[14] & 0xFF);
            return new UUID(msb, lsb);
        }
        return null;
    }

    public String getFirstSyncSearchFilter(Long maxIdentifier) {
        AndFilter filter = new AndFilter();
        filter.and(new LessThanOrEqualsFilter(CONF_LDAP_SEARCH_SYNC_IDENTIFIER, maxIdentifier.toString()));
        filter.and(new EqualsFilter(OBJECT_CLASS, OBJECT_CLASS_USER));
        return filter.encode();
    }

    public String getNextSyncSearchFilter(Long maxIdentifier) {
        AndFilter filter = new AndFilter();
        filter.and(new GreaterThanOrEqualsFilter(CONF_LDAP_SEARCH_SYNC_IDENTIFIER, maxIdentifier.toString()));
        filter.and(new EqualsFilter(OBJECT_CLASS, OBJECT_CLASS_USER));
        return filter.encode();
    }

    @Override
    public void performNextSync(Long maxIdentifier, Integer loggedInUserId, String clientIp) {
        log.debug("performNextSync; maxIdentifier={};", maxIdentifier);

        String filter = getNextSyncSearchFilter(maxIdentifier);
        log.debug("performNextSync; filter={}; ", filter);

        String[] attributesToSync = new String[listAttributesToSync.size()];
        listAttributesToSync.toArray(attributesToSync);
        // search(String base, String filter, int searchScope, String[] attrs,
        // AttributesMapper<Long> mapper)
        ldapTemplateRoot.search(configuration.getString(CONF_LDAP_BASE), filter, CONF_LDAP_SEARCH_SCOPE, attributesToSync,
                new AttributesMapper<String>() {
                    @Override
                    public String mapFromAttributes(Attributes attributes) throws NamingException {
                        log.debug("performNextSync; attributes={};", attributes);
                        HashMap<String, Object> userDetailMap = new HashMap<String, Object>();
                        for (String attr : listAttributesToSync) {
                            // log.info("performFirstSync; attr={}; value={};",
                            // attr,
                            // attributes.get(attr)!=null?attributes.get(attr).get():"null");
                            if (attributes.get(attr) != null) {
                                /*
                                 * if(attr=="objectGUID"){ byte[] guid =
                                 * (byte[])attributes.get(attr).get();
                                 * LdapSyncRunner.bytesToUUID(guid); }
                                 */
                                userDetailMap.put(attr, attributes.get(attr).get());
                            }
                        }
                        int addToSync = 1;
                        // log.info("performFirstSync; userDetailMap={};",
                        // userDetailMap);
                        try {
                            UserSync usersync = new UserSync();
                            usersync.setStatus(LDAP_PENDING);
                            String getUrname = (String) userDetailMap.get(PROP_LDAP_USERNAME_MAPPING);
                            User userInRBAC = userDal.getByUserName(getUrname);
                            userDetailMap.put(LDAP_MAPPED_USERNAME, getUrname);

                            if (userInRBAC != null) {
                                usersync.setStatus(LDAP_CONFLICT);
                                usersync.setUser(userInRBAC);

                                UserSync existingUser = userSyncDal.getByUserId(userInRBAC.getUserId());
                                if (existingUser != null) {
                                    addToSync = 0;
                                    Map<String, String> existingSyncValue = (new ObjectMapper()).readValue(existingUser.getSyncData(), new TypeReference<Map<String, String>>() {
                                    });

                                    for (Map.Entry<String, String> mapValue : existingSyncValue.entrySet()) {
                                        String key = mapValue.getKey();
                                        String value = mapValue.getValue();

                                        Set<String> arrKeys = userFieldMappings.keySet();
                                        for (String keyMapper : arrKeys) {
                                            String getKeyVak = userFieldMappings.get(keyMapper);
                                            if (getKeyVak.equalsIgnoreCase(key) && userDetailMap.containsKey(key) && !key.equalsIgnoreCase("objectGUID")) {
                                                String existingVal = (String) userDetailMap.getOrDefault(key, "");
                                                if (!existingVal.equals(value)) {
                                                    addToSync++;
                                                    break;
                                                }}}}
                                    if (addToSync > 0) {
                                        usersync.setStatus(LDAP_UPDATED);
                                        usersync.setSyncData(existingUser.getSyncData());
                                        usersync.setUpdatedSyncData(new ObjectMapper().writeValueAsString(userDetailMap));
                                    }
                                }}else
                                usersync.setSyncData(new ObjectMapper().writeValueAsString(userDetailMap));
//							Set<String> keys = userDetailMap.keySet();
//							for (String key : keys) {
//								if (key == "objectGUID" && userDetailMap.get(key) != null) {
                            if (userDetailMap != null && userDetailMap.containsKey("objectGUID") && userDetailMap.get("objectGUID") != null && addToSync > 0) {
                                MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
                                String guidStr = (String) attributes.get("objectGUID").get();
                                byte[] guid = guidStr.getBytes();
                                UUID u = bytesToUUID(guid);
                                if (u != null) {
                                    queryParams.add("externalRecordId", u.toString());
                                    usersync.setExternalRecordId(u.toString());
                                    // userDetailMap.get("objectGUID").toString());
                                    Options options = new Options(new OptionFilter(queryParams));
                                    if ((userSyncDal.list(options)).size() == 0) {
                                        userSyncDal.create(usersync, new AuditLogInfo(loggedInUserId, clientIp, u.toString(), "UserSync", "Create"));
                                        log.debug("performNextSync; userSyncDalCreate={};", userSyncDal);
                                    } else {
                                        usersync.setUserSyncId(userSyncDal.list(options).get(0).getUserSyncId());
                                        userSyncDal.update(usersync, new AuditLogInfo(loggedInUserId, clientIp, u.toString(), "UserSync", "Update"));
                                        log.debug("performNextSync; userSyncDalUpdate={};", userSyncDal);
                                    }
                                }
                            }
//							}
                        } catch (Exception e) {
                            log.error("performNextSync; Exception={};", e);
                        }
                        return "";
                    }
                });
        String strfilterdelete = "";
        AndFilter filterDelete = new AndFilter();
        filterDelete.and(new EqualsFilter("isDeleted", "TRUE"));
        filterDelete.and(new EqualsFilter(OBJECT_CLASS, OBJECT_CLASS_USER));
        strfilterdelete = filterDelete.encode();

        ldapTemplateRoot.search(configuration.getString(CONF_LDAP_BASE), strfilterdelete,
                new AttributesMapper<String>() {
                    @Override
                    public String mapFromAttributes(Attributes attributes) throws NamingException {
                        log.debug("performFirstSync; attributes={};", attributes);

                        return "";

                    }
                });
    }
}
