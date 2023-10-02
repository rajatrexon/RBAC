package com.esq.rbac.service.contact.party.repository;


import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.party.queries.PartyQueries;
import com.esq.rbac.service.contact.party.domain.Party;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("PartyRepository")
public class PartyRepository extends Repository<Party> {

    private static final String SQL_WILDCARD = "%";
    private static final String PARAMETER_Q = "q";
    private static final String ORDER_BY = " order by ";
    private static final String ASCENDING = " asc";
    private static final String DESCENDING = " desc";
    private static final Map<String, String> SORT_FIELDS;

    static {
        SORT_FIELDS = new HashMap<>();
        SORT_FIELDS.put("id", "p.id");
        SORT_FIELDS.put("name", "p.name");
        SORT_FIELDS.put("tenantId", "p.tenantId");
        SORT_FIELDS.put("type.name", "pt.name");
        SORT_FIELDS.put("code", "p.code");
        SORT_FIELDS.put("updatedTime", "p.updatedTime");
        SORT_FIELDS.put("department.name", "d.name");
    }

    public PartyRepository() {
        super(Party.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Party update(long id, Party party) {
        log.debug("update; id={}", id);
        party.setId(id);
        return super.update(id, party);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Party readById(long id) {
        log.debug("readById; id={}", id);
        return super.readById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(long id) {
        log.debug("deleteById; id={}", id);
        super.deleteById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Party create(Party instance) {
        return super.create(instance);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Party> list(String asc, String desc, int first, int max, long... tenantScope) {
        StringBuilder sb = new StringBuilder();
        log.debug("list;tenantScope={}",tenantScope);
        if(tenantScope.length==0){
            sb.append(PartyQueries.LIST_QUERY);
        }else if(tenantScope.length==1){
            sb.append(PartyQueries.LIST_QUERY);
            sb.append(" where p.tenantId = ").append(tenantScope[0]);
        }else{
            sb.append(PartyQueries.LIST_QUERY);
            String tenantList= StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
            sb.append(" where p.tenantId in ( ").append(tenantList).append(")");
        }
        if (asc != null && SORT_FIELDS.containsKey(asc)) {
            sb.append(ORDER_BY).append(SORT_FIELDS.get(asc)).append(ASCENDING);
        } else if (desc != null && SORT_FIELDS.containsKey(desc)) {
            sb.append(ORDER_BY).append(SORT_FIELDS.get(desc)).append(DESCENDING);
        }

        TypedQuery<Party> query = entityManager.createQuery(sb.toString(), Party.class);
        if (first != 0) {
            query.setFirstResult(first);
        }
        if (max > 0) {
            query.setMaxResults(max);
        }

        return query.getResultList();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Party> fullTextSearch(String q,long... tenantScope) throws Exception {
        return fullTextSearch(q, null, null, 0, 0,tenantScope);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Party> fullTextSearch(String q, String asc, String desc, int first, int max,long... tenantScope) {
        StringBuilder sb = new StringBuilder();
        log.debug("fullTextSearch;tenantScope={}",tenantScope);
        if(tenantScope.length==0){
            sb.append(PartyQueries.FULL_TEXT_SEARCH);
        }else if(tenantScope.length==1){
            sb.append(PartyQueries.FULL_TEXT_SEARCH);
            sb.append(" and p.tenantId = ").append(tenantScope[0]);
        }else{
            sb.append(PartyQueries.FULL_TEXT_SEARCH);
            String tenantList=StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
            sb.append(" and p.tenantId in ( ").append(tenantList).append(")");
        }
        if (asc != null && SORT_FIELDS.containsKey(asc)) {
            sb.append(ORDER_BY).append(SORT_FIELDS.get(asc)).append(ASCENDING);
        } else if (desc != null && SORT_FIELDS.containsKey(desc)) {
            sb.append(ORDER_BY).append(SORT_FIELDS.get(desc)).append(DESCENDING);
        }

        TypedQuery<Party> query = entityManager.createQuery(sb.toString(), Party.class);
        query.setParameter(PARAMETER_Q, wildcarded(q));
        if (first != 0) {
            query.setFirstResult(first);
        }
        if (max > 0) {
            query.setMaxResults(max);
        }
        return query.getResultList();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int fullTextCount(String q,long... tenantScope) throws Exception {
        StringBuilder sb = new StringBuilder();
        log.debug("fullTextCount;tenantScope={}",tenantScope);
        if(tenantScope.length==0){
            sb.append(PartyQueries.FULL_TEXT_COUNT);
        }else if(tenantScope.length==1){
            sb.append(PartyQueries.FULL_TEXT_COUNT);
            sb.append(" and p.tenantId = ").append(tenantScope[0]);
        }else{
            sb.append(PartyQueries.FULL_TEXT_COUNT);
            String tenantList=StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
            sb.append(" and p.tenantId in ( ").append(tenantList).append(")");
        }
        TypedQuery<Long> query = entityManager.createQuery(sb.toString(), Long.class);
        query.setParameter(PARAMETER_Q, wildcarded(q));
        return (int) query.getSingleResult().longValue();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int partyContactSearch(long q) throws Exception {
        log.debug("partyContactSearch;q ={}",q);
        Query query = entityManager.createNativeQuery(PartyQueries.PART_CONTACT_SEARCH);
        query.setParameter(1, q);
        return (int)((Number) query.getSingleResult()).intValue();
    }

    private String wildcarded(String q) {
        StringBuilder sb = new StringBuilder();
        if (q.startsWith(SQL_WILDCARD) == false) {
            sb.append(SQL_WILDCARD);
        }
        sb.append(q.toLowerCase());
        if (q.endsWith(SQL_WILDCARD) == false) {
            sb.append(SQL_WILDCARD);
        }
        return sb.toString();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int partyNameSearch(String name,long... tenantScope) throws Exception {
        log.debug("partyNameSearch;tenantScope={}; name ={}",tenantScope,name);
        StringBuilder sb = new StringBuilder();
        if(tenantScope.length==0){
            sb.append(PartyQueries.PARTY_NAME_SEARCH);
        }else if(tenantScope.length==1){
            sb.append(PartyQueries.PARTY_NAME_SEARCH);
            sb.append(" and tenant_id = ").append(tenantScope[0]);
        }else{
            sb.append(PartyQueries.PARTY_NAME_SEARCH);
            String tenantList=StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
            sb.append(" and tenant_id in ( ").append(tenantList).append(")");
        }
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter(1, name);
        return (int)((Number) query.getSingleResult()).intValue();
    }

}

