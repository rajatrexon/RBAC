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
package com.esq.rbac.service.filters.domain;

import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class Filters {
    private final List<String> conditions = new LinkedList<String>();
    private final Map<String, Object> parameters = new TreeMap<String, Object>();

    public void addCondition(String condition) {
        this.conditions.add(condition);
    }

    public void addParameter(String paramName, Object paramValue) {
        this.parameters.put(paramName, paramValue);
    }

    public List<String> getConditions() {
        return conditions;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public <T> List<T> getList(EntityManager em, Class<T> type, String queryText, Options options) {

        StringBuilder sb = new StringBuilder();
        sb.append(queryText);

        boolean isFirst = true;
        for (String condition : this.getConditions()) {
            if (isFirst) {
                isFirst = false;
                sb.append(" where ");
            } else {
                sb.append(" and ");
            }
            sb.append(condition);
        }

        TypedQuery<T> query = em.createQuery(sb.toString(), type);
        for (Map.Entry<String, Object> entry : this.getParameters().entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        OptionPage optionPage = options != null ? options.getOption(OptionPage.class) : null;
        if (optionPage != null) {
            query.setFirstResult(optionPage.getFirstResult());
            query.setMaxResults(optionPage.getMaxResults());
        }

        return query.getResultList();
    }

	@Transactional
	public <T> List<T> getList(EntityManager em, Class<T> type,
							   String queryText, Options options,
							   Map<String, String> sortColumnsMap) {

		StringBuilder sb = new StringBuilder();
		sb.append(queryText);

		boolean isFirst = true;
		for (String condition : this.getConditions()) {
			if (isFirst) {
				isFirst = false;
				sb.append(" where ");
			} else {
				sb.append(" and ");
			}
			sb.append(condition);
		}

		OptionSort optionSort = options != null ? options
				.getOption(OptionSort.class) : null;
		if (optionSort != null) {
			List<String> sortColumns = new LinkedList<String>();
			for (String property : optionSort.getSortProperties()) {
				String sortBy = " asc";
				if (property.startsWith("-")) {
					sortBy = " desc";
					property = property.substring(1);
				}
				if(sortColumnsMap.get(property)!=null){
					sortColumns.add(sortColumnsMap.get(property) + sortBy);
				}
			}
			if (!sortColumns.isEmpty()) {
				sb.append(" order by ");
				int i = 1;
				for (String column : sortColumns) {
					if (i > 1) {
						sb.append(", ");
					}
					sb.append(column);
					i++;
				}
			}
		}
		TypedQuery<T> query = em.createQuery(sb.toString(), type);
		for (Map.Entry<String, Object> entry : this.getParameters().entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}

		OptionPage optionPage = options != null ? options
				.getOption(OptionPage.class) : null;
		if (optionPage != null) {
			query.setFirstResult(optionPage.getFirstResult());
			query.setMaxResults(optionPage.getMaxResults());
		}
		System.out.println("Inside the list': :");
		doSomeApplicationWork(em);
		System.out.println("Class Query : : "+query.getResultList());
		return query.getResultList();
	}

	public void doSomeApplicationWork(EntityManager em) {
		try{
			System.out.println("EnitityManager : : "+em.toString());
			System.out.println("inside the new Method: ");
			String query = "select a from Application a";
			TypedQuery<Application> query1 = em.createQuery(query, Application.class);
			List<Application> resultList = query1.getResultList();
			System.out.println("queryResult: " + resultList.toString());
			System.out.println("Finish of new Method");
		}catch (Exception e){
		}
	}

	public <T> List<T> getListWithGrouping(EntityManager em, Class<T> type,
			String queryText, Options options,
			Map<String, String> sortColumnsMap, List<String> groupByColumns, Map<String, String> groupColumnsMap) {

		StringBuilder sb = new StringBuilder();
		sb.append(queryText);

		boolean isFirst = true;
		for (String condition : this.getConditions()) {
			if (isFirst) {
				isFirst = false;
				sb.append(" where ");
			} else {
				sb.append(" and ");
			}
			sb.append(condition);
		}

		OptionSort optionSort = options != null ? options
				.getOption(OptionSort.class) : null;
		if (optionSort != null) {
			List<String> sortColumns = new LinkedList<String>();
			for (String property : optionSort.getSortProperties()) {
				String sortBy = " asc";
				if (property.startsWith("-")) {
					sortBy = " desc";
					property = property.substring(1);
				}
				if(sortColumnsMap.get(property)!=null){
					sortColumns.add(sortColumnsMap.get(property) + sortBy);
				}
			}
			if (!sortColumns.isEmpty()) {
				sb.append(" order by ");
				int i = 1;
				for (String column : sortColumns) {
					if (i > 1) {
						sb.append(", ");
					}
					sb.append(column);
					i++;
				}
			}
		}
		if(groupByColumns!=null && !groupByColumns.isEmpty()){
			List<String> groupColumns = new LinkedList<String>();
				for (String property : groupByColumns) {
					if(groupColumnsMap.get(property)!=null){
						groupColumns.add(groupColumnsMap.get(property));
					}
				}
				if (!groupColumns.isEmpty()) {
					sb.append(" group by ");
					int i = 1;
					for (String column : groupColumns) {
						if (i > 1) {
							sb.append(", ");
						}
						sb.append(column);
						i++;
					}
				}
		}
		TypedQuery<T> query = em.createQuery(sb.toString(), type);
		for (Map.Entry<String, Object> entry : this.getParameters().entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}

		OptionPage optionPage = options != null ? options
				.getOption(OptionPage.class) : null;
		if (optionPage != null) {
			query.setFirstResult(optionPage.getFirstResult());
			query.setMaxResults(optionPage.getMaxResults());
		}

		return query.getResultList();
	}

    public int getCount(EntityManager em, String queryText) {
        StringBuilder sb = new StringBuilder();
        sb.append(queryText);

        boolean isFirst = true;
        for (String condition : this.getConditions()) {
            if (isFirst) {
                isFirst = false;
                sb.append(" where ");
            } else {
                sb.append(" and ");
            }
            sb.append(condition);
        }

        TypedQuery<Number> query = em.createQuery(sb.toString(), Number.class);
        for (Map.Entry<String, Object> entry : this.getParameters().entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.getSingleResult().intValue();
    }
   
}
