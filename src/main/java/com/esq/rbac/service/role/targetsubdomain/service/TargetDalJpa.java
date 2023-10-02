package com.esq.rbac.service.role.targetsubdomain.service;

import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.role.targetsubdomain.domain.Target;
import com.esq.rbac.service.role.targetsubdomain.repository.TargetRepository;

import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class TargetDalJpa implements TargetDal{
    private static final Logger log = LoggerFactory.getLogger(TargetDalJpa.class);

    private TargetRepository  targetRepository;

    protected EntityManager em;

//    protected Class entityClass;

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager");
        this.em = em;
    }

    public TargetDalJpa(TargetRepository targetRepository){
        this.targetRepository=targetRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Target create(Target target) {
//        em.persist(target);
        targetRepository.save(target);
        return target;

    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Target update(Target target) {
//        return em.merge(target);
        return targetRepository.save(target);
    }


    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Target getById(int targetId) {
//        return em.find(Target.class, targetId);
        return targetRepository.findById(targetId).orElse(null);
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(int targetId) {
//        Query query = em.createNamedQuery("deleteTargetById");
//        query.setParameter("targetId", targetId);
//        query.executeUpdate();
        targetRepository.deleteById(targetId);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Target> getList(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getList(em, Target.class, "select t from Target t", options);
    }

    @Override
    public int getCount(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getCount(em, "select count(t) from Target t");
    }


    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        if (filters != null) {

            String applicationId = filters.get("applicationId");
            if (applicationId != null && applicationId.length() > 0) {
                result.addCondition("t.application.applicationId = :applicationId");
                result.addParameter("applicationId", Integer.valueOf(applicationId));
            }

            String name = filters.get("name");
            if (name != null && name.length() > 0) {
                result.addCondition("t.name = :name");
                result.addParameter("name", name);
            }

            String label = filters.get("label");
            if (label != null && label.length() > 0) {
                result.addCondition(":label member of t.labels");
                result.addParameter("label", label);
            }

        }
        return result;
    }
}
