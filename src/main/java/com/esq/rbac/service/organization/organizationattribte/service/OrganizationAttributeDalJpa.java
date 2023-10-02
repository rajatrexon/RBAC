package com.esq.rbac.service.organization.organizationattribte.service;

import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.service.ApplicationDal;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.organizationattribte.domain.OrganizationAttribute;
import com.esq.rbac.service.organization.organizationattribte.repository.OrganizationAttributeRepository;
import com.esq.rbac.service.organization.embedded.OrganizationAttributeInfo;
import com.esq.rbac.service.organization.embedded.OrganizationAttributeWithTenant;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service@Slf4j
public class OrganizationAttributeDalJpa extends BaseDalJpa implements OrganizationAttributeDal {


    private OrganizationAttributeRepository organizationAttributeRepository;

    @Autowired
    public void setOrganizationAttributeRepository(OrganizationAttributeRepository organizationAttributeRepository){
        this.organizationAttributeRepository = organizationAttributeRepository;
    }

    private ApplicationDal applicationDal;

    @Autowired
    public void setApplicationDal(ApplicationDal applicationDal){
        this.applicationDal = applicationDal;
    }

    private OrganizationMaintenanceDal orgDal;

    @Autowired
    public void setOrgDal(OrganizationMaintenanceDal orgDal){
        this.orgDal = orgDal;
    }


    private EntityManager entityManager;

    @Autowired
    public void setEntityManager(EntityManager entityManager){
        this.entityManager = entityManager;
    }


    @Override
    public OrganizationAttribute create(OrganizationAttribute organizationAttribute) {
        if (!isAttributeValid(organizationAttribute)) {
            ErrorInfoException errorInfo = new ErrorInfoException("validationError","rbacIdsInvalid");
            errorInfo.getParameters().put("value", "Appication Id and Organization Id are invalid");
            throw errorInfo;
        }

        if (isAttributeExists(organizationAttribute)) {

            return update(organizationAttribute);
        }
        else{
            organizationAttributeRepository.save(organizationAttribute);
        }
        return organizationAttribute;
    }

    @Override
    public Boolean isAttributeValid(OrganizationAttribute organizationAttribute) {
        StringBuilder queryText = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        List<Object> paramList= new LinkedList<Object>();
        queryText.append(" select case when COUNT(1) >= 1 then 1 else 0 end from ");
        whereClause.append(" where ");
        if (organizationAttribute.getApplicationId() != null) {
            queryText.append(" rbac.application a ");
            whereClause.append(" a.applicationId = ? ");
            paramList.add(organizationAttribute.getApplicationId());
        }
        if (organizationAttribute.getOrganizationId() != null) {

            queryText.append(" ,");
            whereClause.append(" and ");

            queryText.append(" rbac.Organization g ");
            whereClause.append(" g.OrganizationId = ? ");
            paramList.add(organizationAttribute.getOrganizationId());
        }


        //whereClause.append(") then 1 else 0 end");
        queryText.append(whereClause);
        log.trace("isAttributeValid; queryText={}", queryText.toString());

        Query query = entityManager.createNativeQuery(queryText.toString());
        if(!paramList.isEmpty()){
            for(int i=1; i<=paramList.size();i++ ){
                query.setParameter(i, paramList.get(i-1));
            }
        }
        if (query != null && query.getSingleResult() != null) {
            log.debug("isAttributeValid; queryResult={}"
                    + query.getSingleResult());
            return (Integer) query.getSingleResult() == 1;
        }
        return false;
    }
    public boolean isAttributeExists(OrganizationAttribute organizationAttribute) {
        Integer existingId =getExistingOrganizationAttributeId(organizationAttribute);
        if (existingId != null) {
            log.debug("isAttributeExists; existingId={}"
                    + existingId);
            return existingId > 0 ? true : false;
        }
        return false;
    }


    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    private Integer getExistingOrganizationAttributeId(OrganizationAttribute organizationAttribute){
        StringBuilder queryText = new StringBuilder();
        Map<String, Object> paramList= new HashMap<String, Object>();
        queryText
                .append("select v.attributeId from OrganizationAttribute v where ");
        if (organizationAttribute.getOrganizationId() != null) {
            queryText.append(" v.organizationId = :organizationId "
                    + " and ");
            paramList.put("organizationId", organizationAttribute.getOrganizationId());
        } else {
            queryText.append(" v.organizationId IS NULL " + " and ");
        }

        if (organizationAttribute.getApplicationId() != null) {
            queryText.append(" v.applicationId = :applicationId "
                    + " and ");
            paramList.put("applicationId", organizationAttribute.getApplicationId());
        } else {
            queryText.append(" v.applicationId IS NULL " + " and ");
        }

        queryText.append(" v.attributeName= :attributeName ");
        paramList.put("attributeName", organizationAttribute.getAttributeName());
        log.trace("getExistingOrganizationAttributeId; queryText={}", queryText.toString());

        TypedQuery<Integer> query = entityManager.createQuery(queryText.toString(), Integer.class);
        if(!paramList.isEmpty()){
            for(String paramKey:paramList.keySet()){
                query.setParameter(paramKey, paramList.get(paramKey));
            }
        }
        Integer queryResult = null;
        try{
            queryResult = query.getSingleResult();
        }
        catch(NoResultException e){
            return null;
        }
        return queryResult;
    }
    @Override
    public OrganizationAttribute update(OrganizationAttribute organizationAttribute) {
        Integer existingAttributeId = getExistingOrganizationAttributeId(organizationAttribute);
        if (existingAttributeId == null || existingAttributeId < 1) {
            ErrorInfoException errorInfo = new ErrorInfoException("validationError","organizationAttributeDoesntExists");
            errorInfo.getParameters().put("value", "OrganizationAttribute Doesn't Exist");
            throw errorInfo;
        }

        if (!isAttributeValid(organizationAttribute)) {
            ErrorInfoException errorInfo = new ErrorInfoException("validationError","rbacIdsInvalid");
            errorInfo.getParameters().put("value", "Appication Id and Organization Id are invalid");
            throw errorInfo;
        }
        OrganizationAttribute existingOrganizationAttribute = em.find(OrganizationAttribute.class, existingAttributeId);
        if(existingOrganizationAttribute==null){
            ErrorInfoException errorInfo = new ErrorInfoException("validationError","updateOrganizationAttributeFailure");
            errorInfo.getParameters().put("value", "Updation of OrganizationAttribute Failed");
            throw errorInfo;
        }
        existingOrganizationAttribute.setAttributeValue(organizationAttribute.getAttributeValue());

        return organizationAttributeRepository.save(existingOrganizationAttribute);
    }

    @Override
    public OrganizationAttribute toOrganizationAttribute(OrganizationAttributeInfo organizationAttributeInfo) {
        if(organizationAttributeInfo.getAttributeName()==null ||
                (organizationAttributeInfo.getApplicationName()==null && organizationAttributeInfo.getOrganizationId()==null )){
            ErrorInfoException errorInfo = new ErrorInfoException("validationError","missingParams");
            errorInfo.getParameters().put("value", "One or more of required params are missing");
            throw errorInfo;
        }

        Application application=applicationDal.getByName(organizationAttributeInfo.getApplicationName());

        if(application==null) {
            ErrorInfoException errorInfo = new ErrorInfoException("validationError","application obj is null");
            errorInfo.getParameters().put("value", "Application id not found");
            throw errorInfo;
        }
        organizationAttributeInfo.setApplicationId(application.getApplicationId());
        OrganizationAttribute organizationAttribute = new OrganizationAttribute();
        organizationAttribute.setAttributeName(organizationAttributeInfo.getAttributeName());
        organizationAttribute.setAttributeValue(organizationAttributeInfo.getAttributeValue()); // pattern Match
        organizationAttribute.setTimezone(organizationAttributeInfo.getTimezone());
        organizationAttribute.setAttributeKey(organizationAttributeInfo.getCode().getCodeValue());
        organizationAttribute.setCodeId(organizationAttributeInfo.getCode().getCodeId());
        try{
            if(organizationAttributeInfo.getApplicationId()!=null ){
                organizationAttribute.setApplicationId(organizationAttributeInfo.getApplicationId());
            }
            if(organizationAttributeInfo.getOrganizationId()!=null ){
                organizationAttribute.setOrganizationId(organizationAttributeInfo.getOrganizationId());
            }

        }
        catch(Exception e){
            log.error("toVariable Exception={};",e);
        }
        finally{
            if((organizationAttributeInfo.getApplicationId()==null || organizationAttributeInfo.getApplicationId()==0)
                    || (organizationAttributeInfo.getOrganizationId()==null || organizationAttributeInfo.getOrganizationId()==0 ) ){
                ErrorInfoException errorInfo = new ErrorInfoException("validationError","invalidParams");
                errorInfo.getParameters().put("value", "ApplicationId/OrganizationId are invalid");
                throw errorInfo;
            }
        }
        return organizationAttribute;
    }


    @Override
    public void delete(OrganizationAttribute organizationAttribute) {
        if (!isAttributeExists(organizationAttribute)) {
            ErrorInfoException errorInfo = new ErrorInfoException(
                    "AttributeDoesntExists", "Attribute Doesn't Exist");
            throw errorInfo;
        }
        StringBuilder queryText = new StringBuilder();
        queryText.append("delete from OrganizationAttribute v where ");
        Map<String, Object> paramList= new HashMap<String, Object>();
        if (organizationAttribute.getOrganizationId() != null) {
            queryText.append(" v.organizationId = :organizationId "
                    + " and ");
            paramList.put("organizationId",organizationAttribute.getOrganizationId());
        } else {
            queryText.append(" v.organizationId IS NULL " + " and ");
        }
        if (organizationAttribute.getApplicationId() != null) {
            queryText.append(" v.applicationId = :applicationId "
                    +   " and ");
            paramList.put("applicationId", organizationAttribute.getApplicationId());
        } else {
            queryText.append(" v.applicationId IS NULL " + " and ");
        }

        queryText.append(" v.attributeName = :attributeName ");
        paramList.put("attributeName",organizationAttribute.getAttributeName() );
        log.trace("delete; queryText={}", queryText.toString());
        Query query = entityManager.createQuery(queryText.toString());
        if(!paramList.isEmpty()){
            for(String paramKey:paramList.keySet()){
                query.setParameter(paramKey, paramList.get(paramKey));
            }
        }
        query.executeUpdate();
    }

    @Override
    public void deleteByOrganizationId(Long organizationId) {
        StringBuilder queryText = new StringBuilder();
        queryText.append("delete from OrganizationAttribute v where ");
        Map<String, Object> paramList= new HashMap<String, Object>();
        if (organizationId != null && organizationId!=0) {
            queryText.append(" v.organizationId = :organizationId "
            );
            paramList.put("organizationId",organizationId);
        } else {
            queryText.append(" v.organizationId IS NULL ");
        }
        log.trace("delete; queryText={}", queryText.toString());
        Query query = em.createQuery(queryText.toString());
        if(!paramList.isEmpty()){
            for(String paramKey:paramList.keySet()){
                query.setParameter(paramKey, paramList.get(paramKey));
            }
        }
        query.executeUpdate();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<OrganizationAttributeInfo> getList(Options options) {
        CustomFilters filters = prepareFilters(options);
        return getList(
                entityManager,
                "select v.attributeName, v.attributeValue, a.name,v.organizationId,v.applicationId from rbac.organizationAttributes v "
                        + "left join rbac.application a on (a.applicationId=v.applicationId) "
                        + "left join rbac.organization u on (u.organizationId=v.organizationId) ",
                options, filters);
    }

    private CustomFilters prepareFilters(Options options) {

        CustomFilters result = new CustomFilters();
        OptionFilter optionFilter = options == null ? null : options
                .getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null
                : optionFilter.getFilters();
        if (filters != null) {

            String attributeName = filters.get("attributeName");
            if (attributeName != null && attributeName.length() > 0) {
                result.addCondition("v.attributeName = ? ");
                result.paramList.add(attributeName);
            }

            String applicationName = filters.get("applicationName");
            if (applicationName != null && applicationName.length() > 0) {
                result.addCondition("a.name = ? ");
                result.paramList.add(applicationName);
            }

            String organizationId = filters.get("organizationId");
            if (organizationId != null && organizationId.length() > 0) {
                result.addCondition("u.organizationId = ? ");
                result.paramList.add(organizationId);
            }else {

                result.addCondition(" u.organizationId IS NULL ");
            }


        }
        return result;
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    private List<OrganizationAttributeInfo> getList(EntityManager em, String queryText,
                                                    Options options, CustomFilters filters) {

        StringBuilder sb = new StringBuilder();
        sb.append(queryText);

        boolean isFirst = true;

        for (String condition : filters.getConditions()) {
            if (isFirst) {
                isFirst = false;
                sb.append(" where ");
            } else {
                sb.append(" and ");
            }
            sb.append(condition);
        }
        log.trace("getList; stringQuery={}", sb.toString());
        Query query = em.createNativeQuery(sb.toString());
        if(!filters.paramList.isEmpty()){
            for(int i=1; i<=filters.paramList.size();i++ ){
                query.setParameter(i, filters.paramList.get(i-1));
            }
        }
        OptionPage optionPage = options != null ? options
                .getOption(OptionPage.class) : null;
        if (optionPage != null) {
            query.setFirstResult(optionPage.getFirstResult());
            query.setMaxResults(optionPage.getMaxResults());
        }
        log.trace("getList; query={}", query);

        @SuppressWarnings("unchecked")
        List<Object[]> list = query.getResultList();
        List<OrganizationAttributeInfo> resultList = new ArrayList<OrganizationAttributeInfo>();
        if(list!=null && !list.isEmpty()){
            for (Object[] pair : list) {
                OrganizationAttributeInfo temp = new OrganizationAttributeInfo();
                temp.setAttributeName(pair[0].toString());
                temp.setAttributeValue((pair[1] != null ? pair[1]
                        .toString() : null));

                temp.setApplicationName((pair[2] != null ? pair[2]
                        .toString() : null));
                temp.setOrganizationId((pair[3] != null ? (Long)pair[3]: null));
                temp.setApplicationId((pair[4] != null ? (Integer)pair[4] : null));
                resultList.add(temp);
            }
        }
        return resultList;
    }



    private static class CustomFilters extends Filters{
        List<Object> paramList= new LinkedList<Object>();

    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<OrganizationAttributeWithTenant> getListForOrganizationAttributeWithTenant(OrganizationAttributeWithTenant organizationAttributeWithTenant) {
        /*Get Organization Id Based On Tenant Id*/
        Organization organizationMaster = orgDal.getById(organizationAttributeWithTenant.getOrganizationId());
        OrganizationAttributeWithTenant organizationAttributeWithTenantObj= new OrganizationAttributeWithTenant();

        if (organizationMaster != null) {
            organizationAttributeWithTenantObj = new OrganizationAttributeWithTenant();
            organizationAttributeWithTenantObj.setOrganizationId(organizationMaster.getOrganizationId());
            organizationAttributeWithTenantObj.setOrganizationName(organizationMaster.getOrganizationName());
            organizationAttributeWithTenantObj.setOrganizationFullName(organizationMaster.getOrganizationFullName());
            organizationAttributeWithTenantObj.setRemarks(organizationMaster.getRemarks());
            organizationAttributeWithTenantObj.setOrganizationType(organizationMaster.getOrganizationType());
            organizationAttributeWithTenantObj.setOrganizationSubType(organizationMaster.getOrganizationSubType());
            organizationAttributeWithTenantObj.setParentOrganizationId(organizationMaster.getParentOrganizationId());
            organizationAttributeWithTenantObj.setOrganizationURL(organizationMaster.getOrganizationURL());
            organizationAttributeWithTenantObj.setTenantId(organizationMaster.getTenantId());
            organizationAttributeWithTenantObj.setCreatedBy(organizationMaster.getCreatedBy());
            organizationAttributeWithTenantObj.setCreatedOn(organizationMaster.getCreatedOn());
        }


        Long parentOrg = organizationMaster.getParentOrganizationId();

        Long tenantOrganization = 100L;

        if(organizationMaster.getTenantId() != 100)
        {
            StringBuilder sbForOrgIds=new StringBuilder();
            sbForOrgIds.append("select o1.organizationId from Organization o1 where o1.organizationId = " +
                    "	 (Select o2.organizationId from Organization o2 where o2.organizationName = " +
                    "	 (select t1.tenantName from Tenant t1 where t1.tenantId = o1.tenantId)) AND o1.tenantId = "+organizationMaster.getTenantId());
            TypedQuery<Long>  queryForOrgIdsBasedOnTenantId = em.createQuery(sbForOrgIds.toString(),Long.class);
            tenantOrganization=queryForOrgIdsBasedOnTenantId.getSingleResult();

        }
        List<OrganizationAttribute> orgAttributes = getOrganizationAttribute(organizationAttributeWithTenant.getOrganizationId(), parentOrg,tenantOrganization,organizationAttributeWithTenant.getAppKey());
        organizationAttributeWithTenantObj.setOrganizationAttributes(orgAttributes);

        List<OrganizationAttributeWithTenant> attributeWithTenants=new ArrayList<>();
        attributeWithTenants.add(organizationAttributeWithTenantObj);
        return attributeWithTenants;
    }

    private List<OrganizationAttribute> getOrganizationAttribute(Long organizationId, Long parentOrg,
                                                                 Long tenantOrganization, String appKey) {

        //For requested Organization
        List<OrganizationAttribute> orgAtt = getOrganizationAttributeByOrgIdAndAppKey(organizationId,appKey);
        List<String> attNames = new ArrayList<>();
        if(orgAtt != null && !orgAtt.isEmpty()) {
            attNames = orgAtt.stream().map(org -> org.getAttributeName()).collect(Collectors.toList());
        }

        //For Parent Organization
        if(parentOrg != null) {
            List<OrganizationAttribute> orgAttParent = getOrganizationAttributeByOrgIdAndAppKey(parentOrg,appKey);
            for( OrganizationAttribute orgAttP:orgAttParent) {
                if(attNames != null && !attNames.contains(orgAttP.getAttributeName())) {
                    orgAtt.add(orgAttP);
                    attNames.add(orgAttP.getAttributeName());
                }
            }
        }

        //For Host Organization
        if(tenantOrganization != null) {
            List<OrganizationAttribute> orgAttHost = getOrganizationAttributeByOrgIdAndAppKey(tenantOrganization,appKey);
            for( OrganizationAttribute orgAttH:orgAttHost) {
                if(attNames != null && !attNames.contains(orgAttH.getAttributeName())) {
                    orgAtt.add(orgAttH);
                    attNames.add(orgAttH.getAttributeName());
                }
            }
        }
        return orgAtt;
    }
    private List<OrganizationAttribute> getOrganizationAttributeByOrgIdAndAppKey(Long organizationId, String appKey) {
        if (appKey != null && !appKey.isEmpty()) {
            TypedQuery<OrganizationAttribute> query = em.createNamedQuery("getOrganizationAttributesByOrgIdAndAppKey",
                    OrganizationAttribute.class);
            query.setParameter("organizationId", organizationId);
            query.setParameter("appKey", appKey);
            return query.getResultList();
        } else {
            TypedQuery<OrganizationAttribute> query = em.createNamedQuery("getOrganizationAttributesByOrgId",
                    OrganizationAttribute.class);
            query.setParameter("organizationId", organizationId);
            return query.getResultList();
        }
    }
}
