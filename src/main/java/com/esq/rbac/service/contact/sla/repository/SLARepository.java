package com.esq.rbac.service.contact.sla.repository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.sla.domain.SLA;
import com.esq.rbac.service.contact.sla.queries.SLAQueries;
import com.esq.rbac.service.patternmatcher.PatternMatcher2;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SLARepository extends Repository<SLA> {

    private static final long RELOAD_TIMEOUT_MILLIS = 60 * 1000; // 1 minute
    private static final String QUERY_OBJECT_MAPPING_PATTERNS = "queryObjectMappingSLAPatterns";
    private static final String QUERY_SLAS = "querySlas";
    private static final String QUERY_SLAS_BY_TENANT ="querySlasByTenant";
    private static final String PARAM_OBJECT_ID = "objectId";
    private static final String PARAM_CONTACT_ROLE = "contactRole";
    private static final String PARAM_TENANT_ID = "tenantId";
    private static long patternsReloadedTime = 0;
    private static final AtomicReference<PatternMatcher2> matcher = new AtomicReference<>();

    private static final String SLA_SEARCH =
            " SELECT count(csla.id)"
                    + " FROM contact.sla  csla"
                    + " LEFT OUTER JOIN contact.objectrole_sla cors ON (cors.sla_id=csla.id)"
                    + " WHERE (cors.sla_id=?) ";



    private static final String SLA_NAME_SEARCH ="select count(1) from contact.sla where name= ? ";

    public SLARepository() {
        super(SLA.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SLA update(long id, SLA sla) {
        sla.setId(id);
        return super.update(id, sla);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<SLA> querySLAs(String objectId, String contactRole,long... tenantId) throws Exception {
        reloadPatterns();
        List<String> queryVector = matcher.get().find(objectId, contactRole);
        log.debug("querySLAs; queryVector={},tenantId ={}", queryVector,tenantId);

        if (queryVector == null) {
            return Collections.emptyList();
        }
        StringBuilder sb = new StringBuilder();
        if(tenantId!=null && tenantId.length==1){
            sb.append(SLAQueries.QUERY_SLAS_BY_TENANT);
        }else{
            sb.append(SLAQueries.QUERY_SLAS);
        }

        TypedQuery<SLA> query = entityManager.createQuery(sb.toString(), SLA.class);
        query.setParameter(PARAM_OBJECT_ID, queryVector.get(0));
        query.setParameter(PARAM_CONTACT_ROLE, queryVector.get(1));
        if(tenantId!=null && tenantId.length==1){
            query.setParameter(PARAM_TENANT_ID, tenantId[0]);
        }
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    private synchronized void reloadPatterns() {
        long now = System.currentTimeMillis();
        log.debug("realoadPatterns; patternsReloadedTime={}, now={}", patternsReloadedTime, now);
        if (matcher.get() != null && now - patternsReloadedTime < RELOAD_TIMEOUT_MILLIS) {
            return;
        }

        Query query = entityManager.createNamedQuery(QUERY_OBJECT_MAPPING_PATTERNS);
        List<Object[]> result = query.getResultList();
        log.debug("realodPatterns; result.size={}", result.size());

        PatternMatcher2 m = new PatternMatcher2();
        for (Object[] vector : result) {
            List<String> stringVector = Arrays.asList(
                    (String) vector[0], (String) vector[1]);
            log.debug("reloadPatterns; {}", stringVector);
            m.add(stringVector);
        }

        matcher.set(m);
        patternsReloadedTime = now;
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int slaSearch(long q) throws Exception {
        log.debug("slaSearch; q={}",q);
        Query query = entityManager.createQuery(SLAQueries.SLA_SEARCH);
        query.setParameter(1, q);
        return (int)((Number) query.getSingleResult()).intValue();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int slaNameSearch(String name,Long tenantId) throws Exception {
        log.debug("scheduleNameSearch; name={},tenantId ={}", name,tenantId);
        StringBuilder sb = new StringBuilder();
        if(tenantId == null){
            sb.append(SLAQueries.SLA_NAME_SEARCH);
        }else {
            sb.append(SLAQueries.SLA_NAME_SEARCH);
            sb.append(" and tenant_id = ").append(tenantId);
        }
        Query query = entityManager.createQuery(sb.toString());
        query.setParameter(1, name);
        return (int)((Number) query.getSingleResult()).intValue();
    }
}
