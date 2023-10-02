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
package com.esq.rbac.service.attributes.service;

import com.esq.rbac.service.attributes.domain.AttributesData;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.masterattributes.domain.MasterAttributes;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AttributesDalJpa extends BaseDalJpa implements AttributesDal {

    private static final Logger log = LoggerFactory.getLogger(AttributesDalJpa.class);
    private static final Map<String, String> SORT_COLUMNS = new LinkedHashMap<String, String>();

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager");
        this.em = em;
        this.entityClass = AttributesData.class;
    }
   
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AttributesData create(AttributesData attributeData) {
    	em.persist(attributeData);
        return attributeData;
    }

    
    public Integer getAttributeId(Integer scopeConstraintId) {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<MasterAttributes> query = qb.createQuery(MasterAttributes.class);
        Root<MasterAttributes> masterAttributes = query.from(MasterAttributes.class);
        query.where(qb.equal(masterAttributes.get("scopeConstraintId"), scopeConstraintId));
        List<MasterAttributes> result = em.createQuery(query).getResultList();
        return result.get(0).getAttributeId();
	}
    
    
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AttributesData updateAtributeName(String attributeDataId, String attributeDataValue) {
    	Integer attrDataId = 0;
    	try{
    		attrDataId = Integer.parseInt(attributeDataId);
    	}catch(Exception e){
    		return null;
    	}
    	Query query = em.createNamedQuery("updateAttributeData");
    	query.setParameter("attributeDataValue", attributeDataValue);
    	query.setParameter("attributeDataId", attrDataId);
    	query.setParameter("valueReferenceId", attributeDataId);
    	query.executeUpdate();
    	AttributesData ad = em.find(AttributesData.class, attrDataId);
    	return ad;
    }
    
	private Filters prepareFilters(Options options) {

		Filters result = new Filters();
		OptionFilter optionFilter = options == null ? null : options
				.getOption(OptionFilter.class);
		Map<String, String> filters = optionFilter == null ? null
				: optionFilter.getFilters();
		if (filters != null) {
			String userScopeQuery = filters.get(RBACUtil.USER_SCOPE_QUERY);
			String groupScopeQuery = filters.get(RBACUtil.GROUP_SCOPE_QUERY);
			if ( (userScopeQuery != null && userScopeQuery.length() > 1) && (groupScopeQuery != null && groupScopeQuery.length() > 1) ) {
				result.addCondition("((UserEntity.userId in (select u.userId from User u where "
						+ "(" + userScopeQuery + ") )) " + " or " + " (GroupEntity.groupId in (select g.groupId from Group g where "
						+ "(" + groupScopeQuery + ") )) )");
			}
			else if (groupScopeQuery != null && groupScopeQuery.length() > 1) {
				result.addCondition("GroupEntity.groupId in (select g.groupId from Group g where "
						+ "(" + groupScopeQuery + ") ) ");
			}
			else if ( userScopeQuery != null && userScopeQuery.length() > 1) {
				result.addCondition("UserEntity.userId in (select u.userId from User u where "
						+ "(" + userScopeQuery + ") ) ");
			}
		}
		return result;
	}
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getByAttributeId(Integer attributeId, Options options) {
    	Filters filters = prepareFilters(options);
    	filters.addCondition("a.valueReferenceId is not null");
    	filters.addCondition("a.attributeId=:attributeId");
    	filters.addParameter("attributeId", attributeId);
	    List<Object[]> list = filters.getList(em, Object[].class, "select distinct a.attributeDataValue as name, a.valueReferenceId as id from AttributesData a "
	    		+ "LEFT OUTER JOIN a.user as UserEntity LEFT OUTER JOIN a.group as GroupEntity", options, SORT_COLUMNS);
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if(list!=null && !list.isEmpty()){
	        for (Object[] pair : list) {
	        	Map<String, Object> row = new HashMap<String, Object>();
	        	row.put("name", pair[0]);
	        	row.put("id", pair[1]);
	        	result.add(row);
	        }
		}
		return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getAttributeAssociation(String valueReferenceId) {
		Query query = em.createNamedQuery("getAssociatedRBACGroups");
		query.setParameter("valueReferenceId", valueReferenceId);
		Object result = query.getSingleResult();
		return result;
    }
}
