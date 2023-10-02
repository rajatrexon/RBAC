package com.esq.rbac.service.util;

import com.esq.rbac.service.auditlog.domain.AuditLog;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.targetoperations.TargetOperations;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.contact.domain.Contact;
import com.esq.rbac.service.contact.embedded.AuditLogJson;
import com.esq.rbac.service.contact.queries.ContactQueries;
import com.esq.rbac.service.contact.repository.ContactRepository;
import com.esq.rbac.service.user.domain.User;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class ContactDispatcherUtil {

    private static final String CONF_DISPATCHER_APPLICATIONNAME = "dispatcher.applicationName";
    private static final String DEFAULT_DISPATCHER_APPLICATIONNAME = "Dispatcher";

    private EntityManager entityManager;
    private ContactRepository contactRepository;
    private Configuration configuration;
    private String applicationName;
    private AuditLogService auditLogDal;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        log.trace("setEntityManager; {}", entityManager);
        this.entityManager = entityManager;
    }

    @Autowired
    public void setContactRepository(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Autowired
    public void setAuditLogDal(AuditLogService auditLogDal) {
        log.trace("setAuditLogDal; {}", auditLogDal);
        this.auditLogDal = auditLogDal;
    }

    @Resource(name = "propertyConfig")
    public void setConfiguration(Configuration configuration) {
        log.trace("setConfiguration;");
        this.configuration = configuration;
        applicationName = this.configuration.getString(
                CONF_DISPATCHER_APPLICATIONNAME,
                DEFAULT_DISPATCHER_APPLICATIONNAME);
    }

    private void createAuditLog(String target, String operation,
                                Map<String, String> properties, Integer userId) {
        AuditLogJson auditLogJson = new AuditLogJson();
        auditLogJson.setTargetName(target);
        auditLogJson.setOperationName(operation);
        auditLogJson.setQueryField1(target + "." + operation);
        auditLogJson.setIsAlertable(false);
        auditLogJson.setUserId(userId);
        postAuditLogData(auditLogJson, properties);
    }

    private AuditLogJson postAuditLogData(AuditLogJson auditLogJson,
                                          Map<String, String> properties) {
        log.info("postAuditLogData; logData={}", auditLogJson);
        auditLogJson.setApplicationName(applicationName);
        if (properties != null) {
            auditLogJson.setProperties(properties);
        }
        /*
         * ClientResponse clientResponse = null; try { clientResponse =
         * restClient .resource(RESOURCE_PATH) .entity(auditLogJson,
         * MediaType.APPLICATION_JSON) .accept(MediaType.APPLICATION_JSON)
         * .post(ClientResponse.class);
         * log.info("postAuditLogData; response={}", clientResponse); return
         * clientResponse.getEntity(AuditLogJson.class); } catch (Exception e) {
         * log.warn("postAuditLogData; Exception={} ", e); } finally{ try{
         * if(clientResponse!=null){ clientResponse.close(); } }
         * catch(ClientHandlerException ce){
         * log.debug("postAuditLogData; ClientHandlerException={}", ce); } }
         */
        try {
            AuditLog aLog = auditLogDal.create(AuditLogUtil
                    .convertToAuditLog(auditLogJson));
            return AuditLogUtil.convertToAuditLogJson(aLog);
        } catch (Exception e) {
            log.error("postAuditLogData; Exception={};", e);
        }
        return null;

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateContactsForUserUpdation(User user, int loggedInUserId, boolean isSharedMadeFalse) {
        TypedQuery<Object[]> query = entityManager
                .createQuery(
                        "select c.id, c.channel from Contact c where c.userId = :userId",
                        Object[].class);
        query.setParameter("userId", user.getUserId());
        List<Object[]> resultList = query.getResultList();
        if (resultList != null && !resultList.isEmpty()) {
            for (Object[] result : resultList) {
                try {
                    String address = getAddressForChannel(
                            result[1] != null ? ((Code) result[1]).getCodeValue()
                                    : null, user);
                    if (address == null || address.isEmpty()) {
                        createAuditLog(TargetOperations.PARTY_TARGET_NAME,
                                TargetOperations.DELETE_OPERATION,
                                ContactAuditUtil.convertToJSON(contactRepository
                                                .readById(Integer.parseInt(result[0].toString())),
                                        TargetOperations.DELETE_OPERATION),
                                loggedInUserId);
                        Query deleteQuery = entityManager
                                .createQuery(ContactQueries.DELETE_CONTACT_BY_ID);
                        deleteQuery.setParameter("id",
                                Integer.parseInt(result[0].toString()));
                        deleteQuery.executeUpdate();
                    } else {
                        Contact savedContact = contactRepository
                                .readById(Integer.parseInt(result[0].toString()));
                        Query updateQuery = entityManager
                                .createQuery(ContactQueries.UPDATE_CONTACT_ADDRESS_BY_ID);
                        updateQuery.setParameter("address", address);
                        updateQuery.setParameter("id",
                                Integer.parseInt(result[0].toString()));
                        updateQuery.executeUpdate();
                        Contact newContact = contactRepository.readById(Integer
                                .parseInt(result[0].toString()));
                        createAuditLog(TargetOperations.PARTY_TARGET_NAME,
                                TargetOperations.UPDATE_OPERATION,
                                ContactAuditUtil.compareObject(savedContact,
                                        newContact), loggedInUserId);

                    }
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }
        if(isSharedMadeFalse){
            TypedQuery<Long> query1 = entityManager
                    .createQuery(
                            "select c.id from Contact c where c.userId = :userId and c.objectRoleInternal.id in "
                                    + " (select o.id from ObjectRole o where o.tenantId != :tenantId)",
                            Long.class);
            query1.setParameter("userId", user.getUserId());
            Tenant hostTenant = Lookup.getHostTenant();
            query1.setParameter("tenantId", hostTenant!=null?hostTenant.getTenantId():100l);
            List<Long >resultList1 = query1.getResultList();
            if (resultList1 != null && !resultList1.isEmpty()) {
                for (Long id : resultList1) {
                    try {
                        createAuditLog(TargetOperations.PARTY_TARGET_NAME,
                                TargetOperations.DELETE_OPERATION,
                                ContactAuditUtil.convertToJSON(contactRepository
                                                .readById(id),
                                        TargetOperations.DELETE_OPERATION),
                                loggedInUserId);
                        Query deleteQuery = entityManager
                                .createQuery(ContactQueries.DELETE_CONTACT_BY_ID);
                        deleteQuery.setParameter("id",id);
                        deleteQuery.executeUpdate();
                    }
                    catch (IllegalArgumentException e) {
                        // ignore
                    }
                }
            }
        }

    }

    public static String getAddressForChannel(String channel, User user)
            throws IllegalArgumentException {
        if ("SMS".equalsIgnoreCase(channel)) {
            return parseValuesForAddress(user.getPhoneNumber(),
                    user.getHomePhoneNumber());
        }
        if ("Email".equalsIgnoreCase(channel)) {
            return parseValuesForAddress(user.getEmailAddress(),
                    user.getHomeEmailAddress());
        }
        if ("Email-To".equalsIgnoreCase(channel)) {
            return parseValuesForAddress(user.getEmailAddress(),
                    user.getHomeEmailAddress());
        }
        if ("Email-CC".equalsIgnoreCase(channel)) {
            return parseValuesForAddress(user.getEmailAddress(),
                    user.getHomeEmailAddress());
        }
        if ("Voice".equalsIgnoreCase(channel)) {
            return parseValuesForAddress(user.getPhoneNumber(),
                    user.getHomePhoneNumber());
        }
        throw new IllegalArgumentException();
    }

    private static String parseValuesForAddress(String... values) {
        List<String> resultList = new LinkedList<String>();
        if (values != null && values.length != 0) {
            for (String value : values) {
                if (value != null && !value.isEmpty()) {
                    resultList.add(value);
                }
            }
        }
        return StringUtils.join(resultList, ", ");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateContactsForUserDeletion(Integer userId, int loggedInUserId) {
        TypedQuery<Number> listQuery = entityManager.createQuery(
                "select c.id from Contact c where c.userId = :userId",
                Number.class);
        listQuery.setParameter("userId", userId);
        List<Number> listContactIds = listQuery.getResultList();
        if (listContactIds != null && !listContactIds.isEmpty()) {
            for (Number contactId : listContactIds) {
                createAuditLog(TargetOperations.PARTY_TARGET_NAME,
                        TargetOperations.DELETE_OPERATION,
                        ContactAuditUtil.convertToJSON(contactRepository
                                        .readById(contactId.intValue()),
                                TargetOperations.DELETE_OPERATION),
                        loggedInUserId);
            }
            Query deleteQuery = entityManager
                    .createNamedQuery(ContactQueries.DELETE_CONTACT_BY_ID);
            deleteQuery.setParameter("userId", userId);
            deleteQuery.executeUpdate();
        }
    }
}
