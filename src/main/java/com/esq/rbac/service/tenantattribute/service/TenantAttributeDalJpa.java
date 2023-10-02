package com.esq.rbac.service.tenantattribute.service;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.childapplication.repository.ChildApplicationRepository;
import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.codes.service.CodeDal;
import com.esq.rbac.service.tenantattribute.domain.TenantAttribute;
import com.esq.rbac.service.tenantattribute.repository.TenantAttributeRepository;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TenantAttributeDalJpa implements TenantAttributeDal{


    private EntityManager em;

    @Autowired
    public void setEntityManager(EntityManager em){
        this.em = em;
    }

    private CodeDal codeDal;
    @Autowired
    public void setCodeDal(CodeDal codeDal){
        this.codeDal = codeDal;
    }


    private ChildApplicationRepository childApplicationRepository;

    @Autowired
    public void setChildApplicationRepository(ChildApplicationRepository childApplicationRepository){
        this.childApplicationRepository = childApplicationRepository;
    }


    TenantAttributeRepository tenantAttributeRepository;

    @Autowired
    public void setTenantAttributeRepository(TenantAttributeRepository tenantAttributeRepository){
        this.tenantAttributeRepository = tenantAttributeRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public TenantAttribute create(TenantAttribute tenantAttribute) {
        try {
            // TODO Auto-generated method stub
            if (tenantAttribute.getAttributeId() == null) {
                // to get the applicationId by using appkey
//                TypedQuery<ChildApplication> query = em.createNamedQuery("getChildApplicationByAppKey",
//                        ChildApplication.class);
//                query.setParameter("appKey", tenantAttribute.getAppKey());
//                ChildApplication childApp = query.getSingleResult();
                ChildApplication childApp = childApplicationRepository.getChildApplicationByAppKey(tenantAttribute.getAppKey());
                tenantAttribute.setApplicationId(childApp.getApplication().getApplicationId());
//                em.persist(tenantAttribute);
                tenantAttributeRepository.save(tenantAttribute);
                OptionFilter optionFilter = new OptionFilter();
                optionFilter.addFilter("codeId", tenantAttribute.getCodeId() + "");
                Options options = new Options(optionFilter);
                List<Code> allCodes = codeDal.list(options);
                if (allCodes != null && allCodes.size() > 0)
                    tenantAttribute.setCodeName(allCodes.get(0).getName());
                return tenantAttribute;
            } else
                return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<TenantAttribute> getTenantAttributesByTenantIdAndAppKey(Long tenantId, String appKey) {
//        TypedQuery<TenantAttribute> query = em.createNamedQuery("getTenantAttributesByTenantIdAndAppKey",
//                TenantAttribute.class);
//        query.setParameter("tenantId", tenantId);
//        query.setParameter("appKey", appKey);
        List<Code> allCodes = codeDal.list(null);
        Map<Long, Code> codeMap = allCodes.stream().collect(Collectors.toMap(Code::getCodeId, Function.identity()));
//        List<TenantAttribute> attrList = query.getResultList();
        List<TenantAttribute> attrList = tenantAttributeRepository.getTenantAttributesByTenantIdAndAppKey(tenantId, appKey);
        attrList.forEach(attr -> {
            if (codeMap.containsKey(attr.getCodeId())) {
                Code code = codeMap.get(attr.getCodeId());
                attr.setCodeName(code.getName());
            }
        });
        return attrList;

    }

    @Override
    public List<TenantAttribute> getTenantAttributesByAppKey(String appKey) {
        // TODO Auto-generated method stub
//        TypedQuery<TenantAttribute> query = em.createNamedQuery("getTenantAttributesByAppKey", TenantAttribute.class);
//        query.setParameter("appKey", appKey);
        List<Code> allCodes = codeDal.list(null);
        Map<Long, Code> codeMap = allCodes.stream().collect(Collectors.toMap(Code::getCodeId, Function.identity()));
//        List<TenantAttribute> attrList = query.getResultList();
        List<TenantAttribute> attrList = tenantAttributeRepository.getTenantAttributesByAppKey(appKey);
        attrList.forEach(attr -> {
            if (codeMap.containsKey(attr.getCodeId())) {
                Code code = codeMap.get(attr.getCodeId());
                attr.setCodeName(code.getName());
            }
        });
        return attrList;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public TenantAttribute update(TenantAttribute tenantAttribute) {
        if (tenantAttribute.getAttributeId() != null) {
            // to get the applicationId by using appkey
//            TypedQuery<ChildApplication> query = em.createNamedQuery("getChildApplicationByAppKey",
//                    ChildApplication.class);
//            query.setParameter("appKey", tenantAttribute.getAppKey());
//            ChildApplication childApp = query.getSingleResult();
            ChildApplication childApp = childApplicationRepository.getChildApplicationByAppKey(tenantAttribute.getAppKey());
            tenantAttribute.setApplicationId(childApp.getApplication().getApplicationId());
//            em.merge(tenantAttribute);
            tenantAttributeRepository.save(tenantAttribute);
            OptionFilter optionFilter = new OptionFilter();
            optionFilter.addFilter("codeId", tenantAttribute.getCodeId() + "");
            Options options = new Options(optionFilter);
            List<Code> allCodes = codeDal.list(options);
            if (allCodes != null && allCodes.size() > 0)
                tenantAttribute.setCodeName(allCodes.get(0).getName());
            return tenantAttribute;
        } else {
            return null;
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteTenantAttributeByAttributeId(Integer attributeId) {
        if (attributeId != null) {
//            TypedQuery<TenantAttribute> query = em.createNamedQuery("deleteTenantAttributeByAttributeId",
//                    TenantAttribute.class);
//            query.setParameter("attributeId", attributeId);
//            query.executeUpdate();
            tenantAttributeRepository.deleteTenantAttributeByAttributeId(attributeId);
        }
    }

}
