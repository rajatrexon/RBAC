/*
 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.service.scope.service;


import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.role.operationsubdomain.repository.OperationRepository;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.scope.repository.ScopeRepository;
import com.esq.rbac.service.scope.scopedefinition.repository.ScopeDefinitionRepository;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ScopeDalJpa extends BaseDalJpa implements ScopeDal {

    public static final String DUPLICATED_SCOPE = "duplicatedScope";
    public static final String DUPLICATED_NAME = "duplicatedName";
    public static final String DELETE_CONSTRAINT = "deleteScopeConstraint";
    public static final String DELETE_CONSTRAINT_FK_SCOPEDEF = "deleteConstraintFkScopeDefinition";
    public static final String DELETE_CONSTRAINT_FK_OPSCOPE = "deleteConstraintFkOperationScope";
    private static final Logger log = LoggerFactory.getLogger(ScopeDalJpa.class);
    private static final Map<String, String> SORT_COLUMNS;

    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("name", "s.name");
    }
    private static final String SEARCH_SCOPES = "select distinct s.* from rbac.scope s "
            + "where ( lower(s.name) like ? or lower(s.description) like ? ) and s.applicationId= ?  ";
    private static final String COUNT_SCOPES = "select count(distinct s.scopeId) from rbac.scope s "
            + "where ( lower(s.name) like ? or lower(s.description) like ? ) and s.applicationId= ? ";

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager");
        this.em = em;
        this.entityClass = Scope.class;
    }


    ScopeRepository scopeRepository;

    @Autowired
    public void setScopeRepository(ScopeRepository scopeRepository) {
        log.trace("setScopeRepository");
        this.scopeRepository = scopeRepository;
    }


    OperationRepository operationRepository;
    @Autowired
    public void setOperationRepository(OperationRepository operationRepository) {
        log.trace("setOperationRepository");
        this.operationRepository = operationRepository;
    }


    ScopeDefinitionRepository scopeDefinitionRepository;

    @Autowired
    public void setScopeDefinitionRepository(ScopeDefinitionRepository scopeDefinitionRepository) {
        log.trace("setScopeDefinitionRepository");
        this.scopeDefinitionRepository = scopeDefinitionRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Scope create(Scope scope, int userId) {
        if (isScopeNameDuplicate(scope.getApplicationId(), scope.getName(), scope.getScopeId()) == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_SCOPE).append("; ");
            sb.append(DUPLICATED_NAME).append("=").append(scope.getName());
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_SCOPE, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_NAME, scope.getName());
            throw errorInfo;
        }
        scope.setCreatedBy(userId);
        scope.setCreatedOn(DateTime.now().toDate());
        if (!em.contains(scope)) {
            scope = em.merge(scope); // Merge the detached entity to make it managed
        }
        em.persist(scope);
        return scope;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Scope update(Scope scope, int userId) {
        if (scope.getScopeId() == null) {
            throw new IllegalArgumentException("scopeId missing");
        }
        Scope existingScope = em.find(Scope.class, scope.getScopeId());
        if (existingScope == null) {
            throw new IllegalArgumentException("scopeId invalid");
        }

        if (isScopeNameDuplicate(scope.getApplicationId(), scope.getName(), scope.getScopeId()) == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(DUPLICATED_SCOPE).append("; ");
            sb.append(DUPLICATED_NAME).append("=").append(scope.getName());
            log.info("create; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DUPLICATED_SCOPE, sb.toString());
            errorInfo.getParameters().put(DUPLICATED_NAME, scope.getName());
            throw errorInfo;
        }
        setObjectChangeSet(existingScope, scope);
        
        existingScope.setUpdatedBy(userId);
        existingScope.setUpdatedOn(DateTime.now().toDate());
        existingScope.setName(scope.getName());
        existingScope.setScopeKey(scope.getScopeKey());
        existingScope.setDescription(scope.getDescription());
        existingScope.setLabels(scope.getLabels());
        Scope sc = em.merge(existingScope);

        return sc;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Scope getById(int scopeId) {
        return em.find(Scope.class, scopeId);
    }
    
    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Scope getByScopeKey(String scopeKey) {
//        TypedQuery<Scope> query = em.createNamedQuery("getScopeByScopeKey", Scope.class);
//        query.setParameter("scopeKey", scopeKey);
//        return query.getSingleResult();

        return scopeRepository.getScopeByScopeKey(scopeKey);

    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(int scopeId, Boolean force) {
        if (isScopeInScopeDefinition(scopeId) == 1) {
            Scope scope = getById(scopeId);
            StringBuilder sb = new StringBuilder();
            sb.append(DELETE_CONSTRAINT).append("; ");
            sb.append(DELETE_CONSTRAINT_FK_SCOPEDEF).append("=").append(scopeId);
            log.info("deleteById; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(DELETE_CONSTRAINT, sb.toString());
            errorInfo.getParameters().put(DELETE_CONSTRAINT_FK_SCOPEDEF, String.valueOf(scope.getName()));
            throw errorInfo;
        } else {
            if (isScopeInOperationScope(scopeId) == 1 && !force) {
                Scope scope = getById(scopeId);
                StringBuilder sb = new StringBuilder();
                sb.append(DELETE_CONSTRAINT).append("; ");
                sb.append(DELETE_CONSTRAINT_FK_OPSCOPE).append("=").append(scopeId);
                log.info("deleteById; {}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(DELETE_CONSTRAINT, sb.toString());
                errorInfo.getParameters().put(DELETE_CONSTRAINT_FK_OPSCOPE, String.valueOf(scope.getName()));
                throw errorInfo;
            } else {
//                Query query = em.createNamedQuery("deleteScopeById");
//                query.setParameter("scopeId", scopeId);
//                query.executeUpdate();

                scopeRepository.deleteById(scopeId);
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Scope> getList(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getList(em, Scope.class, "select s from Scope s", options, SORT_COLUMNS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Scope> getListGlobal() {

//        TypedQuery<Scope> query = em.createNamedQuery("getGlobalScope", Scope.class);

        List<Scope> scope = scopeRepository.getGlobalScope();
        if (scope != null) {
            log.debug("Global scope; global={}", scope);
//            List<Scope> scope = query.getResultList();

            return scope;
        }
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Integer getCount(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getCount(em, "select count(s) from Scope s");
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int isScopeNameDuplicate(Integer applicationId, String name, Integer scopeId) {
//    	TypedQuery<Scope> query = em.createNamedQuery("getScopeByNameAndApplicationId", Scope.class);
//        query.setParameter("name", name.toLowerCase());
//        query.setParameter("applicationId", applicationId);
//        List<Scope> fetchedScopeIds = query.getResultList();

        List<Scope> fetchedScopeIds = scopeRepository.getScopeByNameAndApplicationId(name.toLowerCase(),applicationId);
        if (fetchedScopeIds != null && !fetchedScopeIds.isEmpty()) {
            if (scopeId != null && scopeId.intValue() > 0) {
                for (Iterator<Scope> iterator = fetchedScopeIds.iterator(); iterator.hasNext();) {
                	Scope fetchedScope = (Scope) iterator.next();
                    if (fetchedScope != null && scopeId.equals(fetchedScope.getScopeId())) {
                        // updating same scope
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
    public int isScopeInOperationScope(Integer scopeId) {
//        TypedQuery<Integer> query = em.createNamedQuery("isScopeInOperationScope", Integer.class);
//        query.setParameter("scopeId", scopeId);

        Integer isScopeInOperationScope =operationRepository.isScopeInOperationScope(scopeId);
        if (isScopeInOperationScope != null) {
            log.debug("isScopeInOperationScope; queryResult={}" + isScopeInOperationScope);
            return isScopeInOperationScope;
        }
        return 0;
    }

    @Override
    public int isScopeInScopeDefinition(Integer scopeId) {
//        TypedQuery<Integer> query = em.createNamedQuery("isScopeInScopeDefinition", Integer.class);
//        query.setParameter("scopeId", scopeId);
        Integer isScopeInScopeDefinition = scopeDefinitionRepository.isScopeInScopeDefinition(scopeId);
        if (isScopeInScopeDefinition != null) {
            log.debug("isScopeInScopeDefinition; queryResult={}" + isScopeInScopeDefinition);
            return isScopeInScopeDefinition;
        }
        return 0;
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        if (filters != null) {

            String applicationId = filters.get("applicationId");
            if (applicationId != null && applicationId.length() > 0) {
                result.addCondition("s.applicationId = :applicationId");
                result.addParameter("applicationId", Integer.valueOf(applicationId));
            }

            String name = filters.get("name");
            if (name != null && name.length() > 0) {
                result.addCondition("s.name = :name");
                result.addParameter("name", name);
            }

            String mandatory = filters.get("mandatory");
            if (mandatory != null && mandatory.length() > 0) {
                result.addCondition("s.isMandatory = :isMandatory");
                result.addParameter("isMandatory", Boolean.valueOf(mandatory));
            }

            String label = filters.get("label");
            if (label != null && label.length() > 0) {
                result.addCondition(":label member of s.labels");
                result.addParameter("label", label);
            }

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Scope> searchList(Options options) {
        String q = SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase();
        String appId = SearchUtils.getSearchParam(options, "applicationId");
        StringBuilder sb = new StringBuilder();
        sb.append(SEARCH_SCOPES);
        sb.append(SearchUtils.getOrderByParam(options, SORT_COLUMNS));
        Query query = em.createNativeQuery(sb.toString(), Scope.class);
        String wildcardedq = SearchUtils.wildcarded(q);
        query.setParameter(1, wildcardedq);
        query.setParameter(2, wildcardedq);
        query.setParameter(3, Integer.parseInt(appId));
        OptionPage optionPage = options != null ? options
                .getOption(OptionPage.class) : null;
        if (optionPage != null) {
            query.setFirstResult(optionPage.getFirstResult());
            query.setMaxResults(optionPage.getMaxResults());
        }
        List<Scope> scopes = query.getResultList();
        return scopes;
    }

    @Override
    public Integer getSearchCount(Options options) {
        String q = SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM);
        String appId = SearchUtils.getSearchParam(options, "applicationId");
        StringBuilder sb = new StringBuilder();
        sb.append(COUNT_SCOPES);
        Query query = em.createNativeQuery(sb.toString());
        String wildcardedq = SearchUtils.wildcarded(q);
        query.setParameter(1, wildcardedq);
        query.setParameter(2, wildcardedq);
        query.setParameter(3, Integer.parseInt(appId));
        return ((Number) query.getSingleResult()).intValue();
    }

    private void setObjectChangeSet(Scope oldScope, Scope newScope) {
        clearObjectChangeSet();

        putToObjectChangeSet(OBJECTCHANGES_SCOPEID, newScope.getScopeId().toString());
        putToObjectChangeSet(OBJECTNAME, oldScope.getName());

        checkPutToObjectChangeSet(OBJECTCHANGES_SCOPENAME, newScope.getName(), oldScope.getName(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_SCOPEKEY, newScope.getScopeKey(), oldScope.getScopeKey(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_DESCRIPTION, newScope.getDescription(), oldScope.getDescription(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_LABELS, newScope.getLabels(), oldScope.getLabels(), null, null);
        checkPutToObjectChangeSet(OBJECTCHANGES_ISMANDATORY, newScope.getIsMandatory(), oldScope.getIsMandatory(), null, null);
    }
}
