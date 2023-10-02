package com.esq.rbac.service.contact.objectrole.repository;

import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.domain.Contact;
import com.esq.rbac.service.contact.objectrole.queries.ObjectRoleQueries;
import com.esq.rbac.service.contact.objectrole.domain.ObjectRole;
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


@Service
@Slf4j
public class ObjectRoleRepository extends Repository<ObjectRole> {

       private static final String SQL_WILDCARD = "%";
        //private static final String PARAMETER_Q = "q";
        private static final int FULL_TEXT_SEARCH_NATIVE_PARAMETERS = 6;
        private static final String ORDER_BY = " order by ";
        private static final String ASCENDING = " asc";
        private static final String DESCENDING = " desc";
        private static final Map<String, String> SORT_FIELDS;
        private static final String PARAMETER_OBJECTKEY = "objectKey";
        private static final String PARAMETER_TENANT_ID = "tenantId";
        private static final String DEFAULT_APP_KEY = "IMS";

        static {
                SORT_FIELDS = new HashMap<String, String>();
                SORT_FIELDS.put("objectId", "o.object_id");
                SORT_FIELDS.put("contactRole", "r.name");
                SORT_FIELDS.put("schedule", "s.name");
                SORT_FIELDS.put("tenantId", "o.tenant_id");
                SORT_FIELDS.put("tenantName", "t.tenantName");
        }

        public ObjectRoleRepository() {
                super(ObjectRole.class);
        }

        @Override
        @Transactional(propagation = Propagation.REQUIRED)
        public ObjectRole create(ObjectRole objectRole) {
                super.create(objectRole);
                entityManager.flush();
                entityManager.refresh(objectRole);
                entityManager.detach(objectRole);
                return objectRole;
        }

        @Transactional(propagation = Propagation.REQUIRED)
        public ObjectRole update2(long id, ObjectRole party) {
                log.debug("update; id={}", id);
                party.setId(id);
                return super.update(id, party);
        }

        @Override
        @Transactional(propagation = Propagation.REQUIRED)
        public ObjectRole update(long id, ObjectRole objectContact) {
                List<Long> contactIds = objectContact.getContactIdList();
                List<Long> slaIdList = objectContact.getSlaIdList();
                List<Contact> contacts = objectContact.getContacts();

                // first update without any list - will be removed
                objectContact.setId(id);
                objectContact.setContactIdList(null);
                objectContact.setContactList(null);
                objectContact.setSlaIdList(null);
                objectContact.setSlaList(null);
                objectContact.setContacts(null);
                ObjectRole result = super.update(id, objectContact);
                entityManager.flush();

                // then update with list - will be added
                if ((contactIds != null && contactIds.size() > 0) || (slaIdList != null && slaIdList.size() > 0) || (contacts != null && contacts.size() > 0)) {
                        objectContact.setContactIdList(contactIds);
                        objectContact.setSlaIdList(slaIdList);
                        objectContact.setContacts(contacts);
                        result = super.update(id, objectContact);
                        entityManager.flush();
                }

                entityManager.refresh(result);
                entityManager.detach(result);
                return result;
        }

        @SuppressWarnings("unchecked")
        @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
        public List<ObjectRole> list(String asc, String desc, int first, int max,String isObjectContact,String appKey,long... tenantScope) {
                log.debug("list; asc={}; desc={}; first={}; max={} ;tenantScope ={},isObjectContact={}, appKey={}", new Object[]{
                        asc, desc, first, max ,tenantScope,isObjectContact,appKey
                });

                StringBuilder sb = new StringBuilder();
                String nativeQuery = ObjectRoleQueries.LIST_QUERY_NATIVE;
                if(asc != null && asc.equalsIgnoreCase("tenantid")){
                        asc = "tenantName";
                        nativeQuery = ObjectRoleQueries.LIST_QUERY_NATIVE_SPECIAL;
                }
                else if(desc != null && desc.equalsIgnoreCase("tenantid")) {
                        desc = "tenantName";
                        nativeQuery = ObjectRoleQueries.LIST_QUERY_NATIVE_SPECIAL;
                }

                if(tenantScope.length==0){
                        sb.append(nativeQuery);
                        checkAppKey(appKey, sb);
                }else if(tenantScope.length==1){
                        sb.append(nativeQuery);
                        checkAppKey(appKey, sb);
                        sb.append(" and  o.tenant_id = ").append(tenantScope[0]).append("");
                }else{
                        sb.append(nativeQuery);
                        String tenantList= StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
                        checkAppKey(appKey, sb);
                        sb.append(" and  o.tenant_id in ( ").append(tenantList).append(")");
                }



                Boolean isValidObjectContact = isObjectContact.equalsIgnoreCase("true") ? true : false;
                sb.append(" and o.isObjectContact ='").append(isValidObjectContact).append("'");

                if (asc != null && SORT_FIELDS.containsKey(asc)) {
                        sb.append(ORDER_BY).append(SORT_FIELDS.get(asc)).append(ASCENDING);
                } else if (desc != null && SORT_FIELDS.containsKey(desc)) {
                        sb.append(ORDER_BY).append(SORT_FIELDS.get(desc)).append(DESCENDING);
                }

                jakarta.persistence.Query query = entityManager.createNativeQuery(sb.toString(), ObjectRole.class);
                if (first != 0) {
                        query.setFirstResult(first);
                }
                if (max > 0) {
                        query.setMaxResults(max);
                }
                return query.getResultList();
        }

        @SuppressWarnings("unchecked")
        @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
        public List<ObjectRole> fullTextSearch(String q, String asc, String desc, int first, int max,String isObjectContact,Boolean isTenantVisible, long... tenantScope) throws Exception {
                log.debug("fullTextSearch; q={}; asc={}; desc={}; first={}; max={};tenantScope ={},isObjectContact={},isTenant ={}", new Object[]{
                        q, asc, desc, first, max,tenantScope,isObjectContact,isTenantVisible
                });

                StringBuilder sb = new StringBuilder();
                String nativeQuery = ObjectRoleQueries.FULL_TEXT_SEARCH_NATIVE;
                if(isTenantVisible)
                {
                        nativeQuery = ObjectRoleQueries.FULL_TEXT_SEARCH_NATIVE_SPECIAL;
                }
                if(tenantScope.length==0){
                        sb.append(nativeQuery);
                }else if(tenantScope.length==1){
                        sb.append(nativeQuery);
                        sb.append(" and o.tenant_id = ").append(tenantScope[0]);
                }else{
                        sb.append(nativeQuery);
                        String tenantList=StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
                        sb.append(" and o.tenant_id in ( ").append(tenantList).append(")");
                }

                Boolean isValidObjectContact = isObjectContact.equalsIgnoreCase("true") ? true : false;

                sb.append(" and o.isObjectContact ='").append(isValidObjectContact).append("'");

                if (asc != null && SORT_FIELDS.containsKey(asc)) {
                        sb.append(ORDER_BY).append(SORT_FIELDS.get(asc)).append(ASCENDING);

                } else if (desc != null && SORT_FIELDS.containsKey(desc)) {
                        sb.append(ORDER_BY).append(SORT_FIELDS.get(desc)).append(DESCENDING);

                }

                Query query = entityManager.createNativeQuery(sb.toString(), ObjectRole.class);
                for (int i = 1; i <= FULL_TEXT_SEARCH_NATIVE_PARAMETERS; i++) {
                        query.setParameter(i, wildcarded(q));
                }
                if (first != 0) {
                        query.setFirstResult(first);
                }
                if (max > 0) {
                        query.setMaxResults(max);
                }
                return query.getResultList();
        }

        public StringBuilder checkAppKey(String appKey, StringBuilder sb) {
                if(appKey == null|| appKey == "All") {
                        return sb;
                }else {
                        return sb.append(" and o.appKey = '").append(appKey).append("'");
                }
        }

        @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
        public int fullTextCount(String q,String isObjectContact,String appKey, long... tenantScope) throws Exception {
                log.debug("fullTextCount; q={},tenantScope ={},isObjectContact={}, appKey={}", q,tenantScope,isObjectContact, appKey);
                StringBuilder sb = new StringBuilder();
                if(tenantScope.length==0){
                        sb.append(ObjectRoleQueries.FULL_TEXT_COUNT_NATIVE);
                        checkAppKey(appKey, sb);
                }else if(tenantScope.length==1){
                        sb.append(ObjectRoleQueries.FULL_TEXT_COUNT_NATIVE);
                        sb.append(" and o.tenant_id =  ").append(tenantScope[0]);
                        checkAppKey(appKey, sb);
                }else{
                        sb.append(ObjectRoleQueries.FULL_TEXT_COUNT_NATIVE);
                        String tenantList=StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
                        sb.append(" and o.tenant_id in (").append(tenantList).append(" )");
                        checkAppKey(appKey, sb);
                }

                Boolean isValidObjectContact = isObjectContact.equalsIgnoreCase("true") ? true : false;
                sb.append(" and o.isObjectContact ='").append(isValidObjectContact).append("'");

                Query query = entityManager.createNativeQuery(sb.toString());
                for (int i = 1; i <= FULL_TEXT_SEARCH_NATIVE_PARAMETERS; i++) {
                        query.setParameter(i, wildcarded(q));
                }
                return ((Number) query.getSingleResult()).intValue();
        }

        @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
        public int objectNameSearch(String objectId, long... tenantScope) throws Exception {
                log.debug("objectNameSearch; objectId={},tenantScope ={}", objectId,tenantScope);
                StringBuilder sb = new StringBuilder();
                if (tenantScope.length == 0) {
                        sb.append(ObjectRoleQueries.OBJECT_NAME_SEARCH);
                } else if (tenantScope.length == 1) {
                        sb.append(ObjectRoleQueries.OBJECT_NAME_SEARCH);
                        sb.append(" and tenant_id ");
                        sb.append("= ");
                        sb.append("" + tenantScope[0]);
                } else {
                        String tenantList = StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
                        sb.append(ObjectRoleQueries.OBJECT_NAME_SEARCH);
                        sb.append(" and tenant_id ");
                        sb.append(" in (").append(tenantList).append(")");
                }
                Query query = entityManager.createNativeQuery(sb.toString());
                query.setParameter(1, objectId);
                return (int) ((Number) query.getSingleResult()).intValue();
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
        public ObjectRole  readObjectRole(String objectKey,long tenantId) {
                TypedQuery<ObjectRole> query = entityManager.createQuery(ObjectRoleQueries.READ_OBJECTROLE, ObjectRole.class);
                query.setParameter(PARAMETER_OBJECTKEY, objectKey);
                query.setParameter(PARAMETER_TENANT_ID, tenantId);
                return query.getSingleResult();

        }

        @SuppressWarnings("unchecked")
        @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
        public List<Object> readActionRulesInObjectRole() {
                Query query = entityManager.createNativeQuery(ObjectRoleQueries.READ_ACTIONRULE);
                return query.getResultList();
        }
}

