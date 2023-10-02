package com.esq.rbac.service.contact.repository;

import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.queries.ContactQueries;
import com.esq.rbac.service.patternmatcher.PatternMatcher2;
import com.esq.rbac.service.contact.domain.Contact;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service("ContactRepository")
public class ContactRepository extends Repository<Contact> {

    private static final long RELOAD_TIMEOUT_MILLIS = 60 * 1000; // 1 minute
    private static final String PARAM_OBJECT_ID = "objectId";
    private static final String PARAM_OBJECT_ID_LIST = "objectIdList";
    private static final String PARAM_CONTACT_ROLE = "contactRole";
    private static final String PARAM_ADDRESS_TYPE = "addressType";
    private static final String PARAM_CHANNEl = "channel";
    private static final String PARAM_LIFECYCLE = "lifecycle";
    private static final String PARAM_ATM_SCHEDULE = "atmSchedule";
    private static final String PARAM_TENANT_ID = "tenantId";
    //RBAC-2130 start
    private static final String PARAM_ISCC = "iscc";
    //RBAC-2130 end
    private static long patternsReloadedTime = 0;
    private static final AtomicReference<PatternMatcher2> matcher = new AtomicReference<PatternMatcher2>();

    public ContactRepository() {
        super(Contact.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Contact update(long id, Contact contactMethod) {
        contactMethod.setId(id);
        log.debug("update; {}", contactMethod);
        return super.update(id, contactMethod);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Contact> queryContacts(String objectId, String contactRole, String addressType, long... tenantId) throws Exception {
        reloadPatterns();
        List<String> queryVector = matcher.get().find(objectId, contactRole, addressType);
        log.debug("queryContacts; queryVector={} ,tenantId={}", queryVector,tenantId);
        if (queryVector == null) {
            return Collections.emptyList();
        }
        StringBuilder sb = new StringBuilder();
        if(tenantId!=null && tenantId.length==1){
            sb.append(ContactQueries.QUERY_CONTACTS_BY_TENANT);
        }else{
            sb.append(ContactQueries.QUERY_CONTACTS);
        }
        TypedQuery<Contact> query = entityManager.createQuery(sb.toString(), Contact.class);
        query.setParameter(PARAM_OBJECT_ID, queryVector.get(0));
        query.setParameter(PARAM_CONTACT_ROLE, queryVector.get(1));
        query.setParameter(PARAM_ADDRESS_TYPE, queryVector.get(2));
        if(tenantId!=null && tenantId.length==1){
            query.setParameter(PARAM_TENANT_ID, tenantId[0]);
        }
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    private void reloadPatterns() {
        long now = System.currentTimeMillis();
        log.debug("realoadPatterns; patternsReloadedTime={}, now={}", patternsReloadedTime, now);
        if (matcher.get() != null && now - patternsReloadedTime < RELOAD_TIMEOUT_MILLIS) {
            return;
        }

        Query query = entityManager.createNamedQuery(ContactQueries.QUERY_OBJECT_MAPPING_PATTERNS);
        List<Object[]> result = query.getResultList();
        log.debug("realodPatterns; result.size={}", result.size());

        PatternMatcher2 m = new PatternMatcher2();
        for (Object[] vector : result) {
            List<String> stringVector = Arrays.asList(
                    (String) vector[0], (String) vector[1], (String) vector[2]);
            log.debug("reloadPatterns; {}", stringVector);
            m.add(stringVector);
        }

        matcher.set(m);
        patternsReloadedTime = now;
    }

    @SuppressWarnings("unchecked")
    public List<Contact> queryActionRuleContacts(long tenant,List<String> actionruleNameList) {
        log.debug("queryActionRuleContacts; tenant={}, actionruleNameList={}",tenant,actionruleNameList);
        TypedQuery<Contact> query = entityManager.createQuery(ContactQueries.QUERY_ACTION_RULE_CONTACTS,Contact.class);
        query.setParameter(PARAM_OBJECT_ID_LIST, actionruleNameList);
        query.setParameter(PARAM_TENANT_ID, tenant);
        return query.getResultList();
    }

    public List<Contact> queryDispatchMapWithChannel(long tenantId,String actionRule,String lyfecycle,String atmSchedule,String channel) {
        log.debug("queryDispatchMapWithChannel; tenantId={}; actionruleName={}; lyfecycle={};atmSchedule={},channel={}",tenantId,actionRule,lyfecycle,atmSchedule,channel);
        TypedQuery<Contact> query = entityManager.createQuery(ContactQueries.QUERY_DISPATCH_MAP_WITH_CHANNEL,Contact.class);
        query.setParameter(PARAM_OBJECT_ID, actionRule);
        query.setParameter(PARAM_LIFECYCLE, lyfecycle);
        query.setParameter(PARAM_CHANNEl,channel);
        query.setParameter(PARAM_ATM_SCHEDULE, atmSchedule);
        query.setParameter(PARAM_TENANT_ID, tenantId);
        return query.getResultList();
    }

    public List<Contact> queryDispatchMap(long tenantId,String actionRule,String lyfecycle,String atmSchedule) {
        log.debug("queryDispatchMap; tenantId={}; actionruleName={}; lyfecycle={};schedule={}",tenantId,actionRule,lyfecycle,atmSchedule);
        TypedQuery<Contact> query = entityManager.createQuery(ContactQueries.QUERY_DISPATCH_MAP,Contact.class);
        query.setParameter(PARAM_OBJECT_ID, actionRule);
        query.setParameter(PARAM_LIFECYCLE, lyfecycle);
        query.setParameter(PARAM_ATM_SCHEDULE, atmSchedule);
        query.setParameter(PARAM_TENANT_ID, tenantId);
        return query.getResultList();
    }

    public List<Contact> queryDispatchMapWithoutAtmSched(long tenantId,String actionRule,String lyfecycle) {
        log.debug("queryDispatchMap; tenantId={}; actionruleName={}; lyfecycle={}",tenantId,actionRule,lyfecycle);
        TypedQuery<Contact> query = entityManager.createQuery(ContactQueries.QUERY_DISPATCH_MAP_WITHOUT_ATM_SCHED,Contact.class);
        query.setParameter(PARAM_OBJECT_ID, actionRule);
        query.setParameter(PARAM_LIFECYCLE, lyfecycle);
        query.setParameter(PARAM_TENANT_ID, tenantId);
        return query.getResultList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteContactById(long id) {
        Query deleteQuery = entityManager.createQuery(ContactQueries.DELETE_CONTACT_BY_ID);
        deleteQuery.setParameter("id",id);
        deleteQuery.executeUpdate();
    }
    //RBAC-2130 start
    public List<Contact> queryDispatchMapWithChannelBySeq(long tenantId, String actionRule, String lifecycle,
                                                          String atmSchedule, String channel, Boolean includeCC) {
        log.debug("queryDispatchMapWithChannel; tenantId={}; actionruleName={}; lyfecycle={};atmSchedule={},channel={},includeCC={}",
                tenantId,actionRule,lifecycle,atmSchedule,channel,includeCC);
        includeCC = (includeCC != null) ? includeCC : false;

        TypedQuery<Contact> query = entityManager.createQuery(ContactQueries.QUERY_DISPATCH_MAP_WITH_CHANNEL_BY_SEQ,Contact.class);
        if(includeCC)
            query = entityManager.createQuery(ContactQueries.QUERY_DISPATCH_MAP_WITH_CHANNEL_BY_SEQ_CC,Contact.class);
        query.setParameter(PARAM_OBJECT_ID, actionRule);
        query.setParameter(PARAM_LIFECYCLE, lifecycle);
        query.setParameter(PARAM_CHANNEl,channel);
        query.setParameter(PARAM_ATM_SCHEDULE, atmSchedule);
        query.setParameter(PARAM_TENANT_ID, tenantId);
        if(!includeCC)
            query.setParameter(PARAM_ISCC, includeCC);
        return query.getResultList();
    }

    public List<Contact> queryDispatchMapBySeq(long tenantId, String actionRule, String lifecycle, String atmSchedule,
                                               Boolean includeCC) {
        log.debug("queryDispatchMap; tenantId={}; actionruleName={}; lyfecycle={};schedule={},includeCC={}",tenantId,actionRule,lifecycle,atmSchedule,includeCC);
        includeCC = (includeCC != null) ? includeCC : false;
        TypedQuery<Contact> query = entityManager.createQuery(ContactQueries.QUERY_DISPATCH_MAP_BY_SEQ,Contact.class);
        if(includeCC)
            query = entityManager.createQuery(ContactQueries.QUERY_DISPATCH_MAP_BY_SEQ_CC,Contact.class);
        query.setParameter(PARAM_OBJECT_ID, actionRule);
        query.setParameter(PARAM_LIFECYCLE, lifecycle);
        query.setParameter(PARAM_ATM_SCHEDULE, atmSchedule);
        query.setParameter(PARAM_TENANT_ID, tenantId);
        if(!includeCC)
            query.setParameter(PARAM_ISCC, includeCC);
        return query.getResultList();
    }
    //RBAC-2130 end
}

