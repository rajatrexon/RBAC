package com.esq.rbac.service.contact.messagetemplate.repository;


import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.messagetemplate.domain.MessageTemplate;
import com.esq.rbac.service.contact.messagetemplate.queries.MessageTemplateQueries;
import com.esq.rbac.service.contact.queries.ContactQueries;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TemplateRepository extends Repository<MessageTemplate> {

    private static final String PARAM_ADDRESS_TYPE = "addressType";
    private static final String PARAM_CHANNEL = "channel";
    private static final String PARAM_APP_KEY = "appKey";
    private static final String PARAM_TENANT_ID = "tenantId";
    private static final String PARAM_TEMPLATEID = "templateId";
    private static final Map<String, String> SORT_FIELDS;
    private static final String ASCENDING = " asc";
    private static final String DESCENDING = " desc";
    private static final String ORDER_BY = " order by ";
    private static final int FULL_TEXT_SEARCH_NATIVE_PARAMETERS = 5;
    private static final String SQL_WILDCARD = "%";
    static {
        SORT_FIELDS = new HashMap<String, String>();
        SORT_FIELDS.put("objectId", "o.object_id");
        SORT_FIELDS.put("contactRole", "r.name");
        SORT_FIELDS.put("schedule", "s.name");
        SORT_FIELDS.put("tenantId", "o.tenant_id");
        SORT_FIELDS.put("tenantName", "t.tenantName");
        SORT_FIELDS.put("id", "tp.id");
        SORT_FIELDS.put("name", "tp.name");
        SORT_FIELDS.put("templateType", "tp.template_type");
        SORT_FIELDS.put("templateName", "t.templateName");
    }
    private static TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
    };

    public TemplateRepository() {
        super(MessageTemplate.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MessageTemplate update(long id, MessageTemplate template) {
        template.setId(id);
        return super.update(id, template);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int templateNameSearch(String name,long...tenantScope) throws Exception {
        log.debug("templateNameSearch; name={},tenantScope ={}", name,tenantScope);
        StringBuilder sb = new StringBuilder();
        if (tenantScope.length == 0) {
            sb.append(MessageTemplateQueries.TEMPLATE_NAME_SEARCH);
        } else if (tenantScope.length == 1) {
            sb.append(MessageTemplateQueries.TEMPLATE_NAME_SEARCH);
            sb.append(" and tenant_id = ").append("" + tenantScope[0]);
        } else {
            String tenantList = StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
            sb.append(MessageTemplateQueries.TEMPLATE_NAME_SEARCH);
            sb.append(" and tenant_id ").append(" in (").append(tenantList).append(")");
        }
        Query query = entityManager.createQuery(sb.toString());
        query.setParameter(1, name);
        return (int)((Number) query.getSingleResult()).intValue();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int templateAddressTypeSearch(long channelId,long...tenantScope) throws Exception {
        log.debug("templateAddressTypeSearch; channelId={},tenantScope ={}", channelId,tenantScope);
        StringBuilder sb = new StringBuilder();
        if (tenantScope.length == 0) {
            sb.append(MessageTemplateQueries.TEMPLATE_ADDRESS_TYPE_SEARCH);
        } else if (tenantScope.length == 1) {
            sb.append(MessageTemplateQueries.TEMPLATE_ADDRESS_TYPE_SEARCH);
            sb.append(" and tenant_id = ").append("" + tenantScope[0]);
        } else {
            String tenantList = StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
            sb.append(MessageTemplateQueries.TEMPLATE_ADDRESS_TYPE_SEARCH);
            sb.append(" and tenant_id ").append(" in (").append(tenantList).append(")");
        }
        Query query = entityManager.createQuery(sb.toString());
        query.setParameter(1, channelId);
        return (int)((Number) query.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<MessageTemplate> queryTemplateWithAddressType(String addressType, long tenantId) throws Exception {
        log.debug("queryTemplateWithAddressType; addressType={},tenantId ={}", addressType,tenantId);
        Query query = entityManager.createQuery(MessageTemplateQueries.QUERY_TEMPLATE,MessageTemplate.class);
        query.setParameter(PARAM_ADDRESS_TYPE, addressType);
        query.setParameter(PARAM_TENANT_ID, tenantId);
        return  query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<MessageTemplate> queryTemplateWithChannelType(String channel, String appKey, long... tenantScope) throws Exception {
        log.debug("queryTemplateWithChannelType; channel={},tenantScope ={}, appKey={}", channel,tenantScope,appKey);
        Query query = entityManager.createQuery(MessageTemplateQueries.QUERY_TEMPLATE_WITH_CHANNEL,MessageTemplate.class);
        query.setParameter(PARAM_CHANNEL, channel);
        query.setParameter(PARAM_APP_KEY, appKey);
        Long[] longs = ArrayUtils.toObject(tenantScope);
        List<Long> tenantScopeList = Arrays.asList(longs);
        query.setParameter(PARAM_TENANT_ID, tenantScopeList);
        return  query.getResultList();
    }
    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean isTemplateUsedInMapping(long templateId){
        log.debug("isTemplateUsedInMapping; templateId={}" , templateId);
        Query query = entityManager.createQuery(ContactQueries.IS_TEMPLATE_USED_IN_MAPPING,Number.class);
        query.setParameter(PARAM_TEMPLATEID, templateId);
        return  (int)((Number) query.getSingleResult()).intValue() > 0 ? true : false;
    }


    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<MessageTemplate> templateSearchWithTenantName(String q, String asc, String desc, int first, int max, Boolean isTenantVisible,long... tenantScope) throws Exception {
        log.debug("templateSearchWithTenantName; q={}; asc={}; desc={}; first={}; max={};tenantScope ={},isTenantVisible ={}", new Object[]{
                q, asc, desc, first, max,tenantScope,isTenantVisible
        });

        StringBuilder sb = new StringBuilder();
        String nativeQuery = MessageTemplateQueries.TEMPLATE_SEARCH_WITH_TENANTNAME;
        if(tenantScope != null && tenantScope.length==1){
            sb.append(nativeQuery);
            sb.append(" and tp.tenant_id = ").append(tenantScope[0]);
        }else{
            if(tenantScope == null){
                sb.append(nativeQuery);
                String tenantList=StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
            }
            else{
                sb.append(nativeQuery);
                String tenantList=StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
                sb.append(" and tp.tenant_id in ( ").append(tenantList).append(")");
            }
        }

        if (asc != null && SORT_FIELDS.containsKey(asc)) {
            sb.append(ORDER_BY).append(SORT_FIELDS.get(asc)).append(ASCENDING);

        } else if (desc != null && SORT_FIELDS.containsKey(desc)) {
            sb.append(ORDER_BY).append(SORT_FIELDS.get(desc)).append(DESCENDING);

        }

        Query query = entityManager.createNativeQuery(sb.toString(), MessageTemplate.class);
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

    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<MessageTemplate> templateSortOnTenantName( String asc, String desc, int first, int max ,long... tenantScope) throws Exception {
        log.debug("TEMPLATE_SORT_ON_TENANTNAME;  asc={}; desc={}; first={}; max={};tenantScope ={}", new Object[]{
                asc, desc, first, max,tenantScope
        });

        StringBuilder sb = new StringBuilder();
        if(tenantScope == null){
            sb.append(MessageTemplateQueries.TEMPLATE_SORT_ON_TENANTNAME);
        }
        else{
            if(tenantScope.length==1){
                sb.append(MessageTemplateQueries.TEMPLATE_SORT_ON_TENANTNAME);
                sb.append(" and tp.tenant_id = ").append(tenantScope[0]);
            }else{
                sb.append(MessageTemplateQueries.TEMPLATE_SORT_ON_TENANTNAME);
                String tenantList=StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
                sb.append(" and tp.tenant_id in ( ").append(tenantList).append(")");
            }
        }
        if (asc != null && SORT_FIELDS.containsKey(asc)) {
            if(asc.equalsIgnoreCase("tenantId")){
                asc = "tenantName";
            }
            sb.append(ORDER_BY).append(SORT_FIELDS.get(asc)).append(ASCENDING);

        } else if (desc != null && SORT_FIELDS.containsKey(desc)) {
            if(desc.equalsIgnoreCase("tenantId")){
                desc = "tenantName";
            }
            sb.append(ORDER_BY).append(SORT_FIELDS.get(desc)).append(DESCENDING);
        }
        Query query = entityManager.createQuery(sb.toString(), MessageTemplate.class);
        if (first != 0) {
            query.setFirstResult(first);
        }
        if (max > 0) {
            query.setMaxResults(max);
        }
        return query.getResultList();
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

    public static String getJsonDefinitionByChannel(String jsonDefinition, String channelLowerCase) throws Exception{
        HashMap<String, String> channelTemplateJson = new ObjectMapper().readValue(jsonDefinition, typeRef);
        return channelTemplateJson.get(channelLowerCase.toLowerCase());
    }
}

