package com.esq.rbac.service.contact.contactrole.repository;


import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.contactrole.domain.ContactRole;
import com.esq.rbac.service.contact.queries.ContactQueries;
import com.esq.rbac.service.patternmatcher.PatternMatcher2;
import com.esq.rbac.service.contact.contactrole.queries.ContactRoleQueries;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service("ContactRoleRepository")
public class ContactRoleRepository extends Repository<ContactRole> {
    private static long patternsReloadedTime = 0;
    private static final AtomicReference<PatternMatcher2> matcher = new AtomicReference<PatternMatcher2>();
    private static final String PARAM_OBJECT_ID = "objectId";
    private static final long RELOAD_TIMEOUT_MILLIS = 60 * 1000; // 1 minute
    private static final String QUERY_OBJECT_MAPPING_PATTERNS = "queryObjectIds";
    private static final String PARAM_TENANT_ID = "tenantId";

    public ContactRoleRepository() {
        super(ContactRole.class);
    }

    @Override
    public void deleteById(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContactRole create(ContactRole instance) {
        throw new UnsupportedOperationException();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ContactRole createFromName(String name,long... tenantId) {
        // first check if it already exist
        StringBuilder sb = new StringBuilder();
        if(tenantId!=null && tenantId.length==1){
            sb.append(ContactRoleQueries.CONTACT_ROLE_BY_NAME_AND_TENANT);
        }else{
            sb.append(ContactRoleQueries.CONTACT_ROLE_BY_NAME);
        }
        TypedQuery<ContactRole> query = entityManager.createQuery(sb.toString(), ContactRole.class);
        query.setParameter("name", name);
        if(tenantId!=null && tenantId.length==1){
            query.setParameter(PARAM_TENANT_ID, tenantId[0]);
        }

        try {
            ContactRole foundRole = query.getSingleResult();
            if (foundRole != null) {
                return foundRole;
            }
        } catch (NoResultException e) {
            // ignore
        }

        ContactRole contactRole = new ContactRole();
        contactRole.setName(name);
        contactRole.setTenantId(tenantId!=null && tenantId.length==1?tenantId[0]:null);
        super.create(contactRole);
        return contactRole;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ContactRole update(long id, ContactRole instance) {
        throw new UnsupportedOperationException();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<ContactRole> getRoles(long... tenantScope) {
        StringBuilder sb = new StringBuilder();
        log.debug("fullTextSearch;tenantScope={}",tenantScope);
        if(tenantScope.length==0){
            sb.append(ContactRoleQueries.LIST_CONTACT_ROLES);
        }else if(tenantScope.length==1){
            sb.append(ContactRoleQueries.LIST_CONTACT_ROLES_WITH_TENANT_ID);
        }else{
            sb.append(ContactRoleQueries.LIST_CONTACT_ROLES_WITH_ID_TENANT_ID_LIST);
        }
        TypedQuery<ContactRole> query = entityManager.createQuery(sb.toString(), ContactRole.class);
        if(tenantScope!=null && tenantScope.length==1){
            query.setParameter(PARAM_TENANT_ID, tenantScope[0]);
        }else if(tenantScope!=null && tenantScope.length>1){
            Long[] longs = ArrayUtils.toObject(tenantScope);
            List<Long> tenantScopeList = Arrays.asList(longs);
            query.setParameter(PARAM_TENANT_ID, tenantScopeList);
        }
        return query.getResultList();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<String> queryContactRoleName(String objectId,long... tenantId) throws Exception {
        log.debug("queryContactRoleName; objectId={},tenantId={}", objectId,tenantId);
        reloadPatterns();
        List<String> queryVector = matcher.get().find(objectId);
        log.debug("queryContactRoleName; queryVector={}", queryVector);
        if (queryVector == null) {
            return Collections.emptyList();
        }
        StringBuilder sb = new StringBuilder();
        if(tenantId!=null && tenantId.length==1){
            sb.append(ContactRoleQueries.QUERY_CONTACT_ROLE_NAME_BY_TENANT);
        }else{
            sb.append(ContactRoleQueries.QUERY_CONTACT_ROLE_NAME);
        }
        TypedQuery<String> query = entityManager.createQuery(sb.toString(), String.class);
        query.setParameter(PARAM_OBJECT_ID, queryVector.get(0));

        log.trace("queryContactRoleName; query={}",query);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    private void reloadPatterns() {
        long now = System.currentTimeMillis();
        log.debug("realoadPatterns; patternsReloadedTime={}, now={}", patternsReloadedTime, now);
        if (matcher.get() != null && now - patternsReloadedTime < RELOAD_TIMEOUT_MILLIS) {
            return;
        }

        Query query = entityManager.createNamedQuery(ContactQueries.QUERY_OBJECT_IDS);
        List<Object> result = query.getResultList();
        log.debug("realodPatterns; result.size={}", result.size());

        PatternMatcher2 m = new PatternMatcher2();
        for (Object vector : result) {
            List<String> stringVector = Arrays.asList(
                    (String) vector);
            log.debug("reloadPatterns; {}", stringVector);
            m.add(stringVector);
        }
        matcher.set(m);
        patternsReloadedTime = now;
    }
}
