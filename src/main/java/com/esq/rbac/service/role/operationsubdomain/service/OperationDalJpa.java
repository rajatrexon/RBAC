package com.esq.rbac.service.role.operationsubdomain.service;

import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.role.operationsubdomain.domain.Operation;
import com.esq.rbac.service.role.operationsubdomain.repository.OperationRepository;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class OperationDalJpa implements OperationDal{
    private static final Logger log = LoggerFactory.getLogger(OperationDalJpa.class);
    private EntityManager em;

    OperationRepository operationRepository;

    public OperationDalJpa(OperationRepository operationRepository){
        this.operationRepository=operationRepository;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager");
        this.em = em;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Operation create(Operation operation) {
//        em.persist(operation);
        operationRepository.save(operation);
        return operation;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Operation update(Operation operation) {
//        return em.merge(operation);
        return operationRepository.save(operation);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Operation getById(int operationId) {
//        return em.find(Operation.class, operationId);
        return  operationRepository.findById(operationId).orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(int operationId) {
//        Query query = em.createNamedQuery("deleteOperationById");
//        query.setParameter("operationId", operationId);
//        query.executeUpdate();
        operationRepository.deleteOperationById(operationId);
    }

    @Override
    public List<Operation> getList(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getList(em, Operation.class, "select o from Operation o", options);
    }

    @Override
    public int getCount(Options options) {
        return 0;
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        if (filters != null) {

            String applicationId = filters.get("applicationId");
            if (applicationId != null && applicationId.length() > 0) {
                result.addCondition("o.target.application.applicationId = :applicationId");
                result.addParameter("applicationId", Integer.valueOf(applicationId));
            }

            String targetId = filters.get("targetId");
            if (targetId != null && targetId.length() > 0) {
                result.addCondition("o.target.targetId = :targetId");
                result.addParameter("targetId", Integer.valueOf(targetId));
            }

            String name = filters.get("name");
            if (name != null && name.length() > 0) {
                result.addCondition("o.name = :name");
                result.addParameter("name", name);
            }

            String label = filters.get("label");
            if (label != null && label.length() > 0) {
                result.addCondition(":label member of o.labels");
                result.addParameter("label", label);
            }

        }
        return result;
    }
}
