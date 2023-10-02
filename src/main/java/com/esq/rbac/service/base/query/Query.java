package com.esq.rbac.service.base.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Query<EntityType> {

    private static final String WILDCARD_CHARACTER = "%";
    private EntityManager entityManager;
    private CriteriaBuilder listCriteriaBuilder;
    private CriteriaQuery<EntityType> listCriteriaQuery;
    private Root<EntityType> listFrom;
    private List<Predicate> listFilters = new ArrayList<Predicate>();
    private CriteriaBuilder countCriteriaBuilder;
    private CriteriaQuery<Long> countCriteriaQuery;
    private Root<EntityType> countFrom;
    private List<Predicate> countFilters = new ArrayList<Predicate>();

    public Query(Class<EntityType> entityType, EntityManager entityManager) {
        this.entityManager = entityManager;

        // list
        listCriteriaBuilder = entityManager.getCriteriaBuilder();
        listCriteriaQuery = listCriteriaBuilder.createQuery(entityType);
        listFrom = listCriteriaQuery.from(entityType);

        // count
        countCriteriaBuilder = entityManager.getCriteriaBuilder();
        countCriteriaQuery = countCriteriaBuilder.createQuery(Long.class);
        countFrom = countCriteriaQuery.from(entityType);
        countCriteriaQuery.select(countCriteriaBuilder.count(countFrom));
    }

    public <T> Query filter(String field, T value) {
        listFilters.add(listCriteriaBuilder.equal(listFrom.get(field), value));
        countFilters.add(countCriteriaBuilder.equal(countFrom.get(field), value));
        return this;
    }

    public Query search(String value, String... fieldNameArray) {
        return search(value, Arrays.asList(fieldNameArray));
    }

    public Query scopeIn(String column, long... valueArray) {
        Long[] valueArrayBoxed = ArrayUtils.toObject(valueArray);
        List<Long> valueList=Arrays.asList(valueArrayBoxed);
        List<Predicate> conditionList = new ArrayList<Predicate>();
        Expression<String> listField = listFrom.get(column);
        conditionList.add(listField.in(valueList));

        Predicate[] conditionArray = new Predicate[conditionList.size()];
        listFilters.add(listCriteriaBuilder.or(conditionList.toArray(conditionArray)));

        conditionList = new ArrayList<Predicate>();
        Expression<String> countField = countFrom.get(column);
        conditionList.add(countField.in(valueList));
        conditionArray = new Predicate[conditionList.size()];
        countFilters.add(countCriteriaBuilder.or(conditionList.toArray(conditionArray)));
        return this;
    }

    public Query search(String value, Collection<String> fieldNameCollection) {
        String query = decorateQuery(value);

        // list conditions
        List<Predicate> conditionList = new ArrayList<Predicate>();
        for (String fieldName : fieldNameCollection) {
            Expression<String> listField = listFrom.get(fieldName);
            Expression<String> listFieldLower = listCriteriaBuilder.lower(listField);
            conditionList.add(listCriteriaBuilder.like(listFieldLower, query));
        }
        Predicate[] conditionArray = new Predicate[conditionList.size()];
        listFilters.add(listCriteriaBuilder.or(conditionList.toArray(conditionArray)));

        // count conditions
        conditionList = new ArrayList<Predicate>();
        for (String fieldName : fieldNameCollection) {
            Expression<String> countField = countFrom.get(fieldName);
            Expression<String> countFieldLower = countCriteriaBuilder.lower(countField);
            conditionList.add(countCriteriaBuilder.like(countFieldLower, query));
        }
        conditionArray = new Predicate[conditionList.size()];
        countFilters.add(countCriteriaBuilder.or(conditionList.toArray(conditionArray)));
        return this;
    }

    public Query orderAscending(String field) {
        listCriteriaQuery.orderBy(listCriteriaBuilder.asc(listFrom.get(field)));
        return this;
    }

    public Query orderDescending(String field) {
        listCriteriaQuery.orderBy(listCriteriaBuilder.desc(listFrom.get(field)));
        return this;
    }

    public long count() {
        if (countFilters.size() > 0) {
            Predicate tmp[] = new Predicate[countFilters.size()];
            countCriteriaQuery.where(countFilters.toArray(tmp));
        }
        TypedQuery<Long> typedQuery = entityManager.createQuery(countCriteriaQuery);
        return typedQuery.getSingleResult();
    }

    public List<EntityType> list() {
        if (listFilters.size() > 0) {
            Predicate tmp[] = new Predicate[listFilters.size()];
            listCriteriaQuery.where(listFilters.toArray(tmp));
        }
        TypedQuery<EntityType> typedQuery = entityManager.createQuery(listCriteriaQuery);
        return typedQuery.getResultList();
    }

    public List<EntityType> list(int firstResult, int maxResults) {
        if (listFilters.size() > 0) {
            Predicate tmp[] = new Predicate[listFilters.size()];
            listCriteriaQuery.where(listFilters.toArray(tmp));
        }
        TypedQuery<EntityType> typedQuery = entityManager.createQuery(listCriteriaQuery);
        typedQuery.setFirstResult(firstResult);
        typedQuery.setMaxResults(maxResults);
        return typedQuery.getResultList();
    }

    public List<EntityType> listOnAppKey(int firstResult, int maxResults, String appKey) {
        if (listFilters.size() > 0) {
            Predicate tmp[] = new Predicate[listFilters.size()];
            listCriteriaQuery.where(listFilters.toArray(tmp));
        }
        TypedQuery<EntityType> typedQuery = entityManager.createQuery(listCriteriaQuery);
        typedQuery.setFirstResult(firstResult);
        typedQuery.setMaxResults(maxResults);
        typedQuery.setParameter("appKey", appKey);
        return typedQuery.getResultList();
    }

    public static String decorateQuery(String query) {
        String queryLowerTrimed = query.trim().toLowerCase();
        StringBuilder sb = new StringBuilder();
        if (queryLowerTrimed.startsWith(WILDCARD_CHARACTER) == false) {
            sb.append(WILDCARD_CHARACTER);
        }
        sb.append(queryLowerTrimed);
        if (queryLowerTrimed.endsWith(WILDCARD_CHARACTER) == false) {
            sb.append(WILDCARD_CHARACTER);
        }
        return sb.toString();
    }
}
