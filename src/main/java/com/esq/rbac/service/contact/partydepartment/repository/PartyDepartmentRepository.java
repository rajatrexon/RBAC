package com.esq.rbac.service.contact.partydepartment.repository;

import com.esq.rbac.service.contact.partydepartment.queries.PartyDepartmentQueries;
import com.esq.rbac.service.contact.partydepartment.domain.PartyDepartment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
public class PartyDepartmentRepository {

    private static final String SQL_WILDCARD = "%";
    private EntityManagerFactory entityManagerFactory = null;
    private EntityManager entityManager=null;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        log.debug("setEntityManagerFactory");
        this.entityManagerFactory = entityManagerFactory;
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<PartyDepartment> getDepartments(String q) {
        TypedQuery<PartyDepartment> query = entityManager.createQuery(
                PartyDepartmentQueries.LIST_PARTY_DEPARTMENTS, PartyDepartment.class);
        if (q == null) {
            q = "";
        }
        query.setParameter("q", wildcarded(q));
        return query.getResultList();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public PartyDepartment readById(long id) {
        PartyDepartment result = entityManager.find(PartyDepartment.class, id);
        log.debug("readById; result={}", result);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PartyDepartment createFromName(String name) {
        PartyDepartment department = new PartyDepartment();
        entityManager = entityManagerFactory.createEntityManager();
        department.setName(name);
        entityManager.getTransaction().begin();
        entityManager.persist(department);
        entityManager.getTransaction().commit();
        entityManager.refresh(department);
        return department;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(long id) {
        PartyDepartment department = readById(id);
        entityManager.remove(department);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PartyDepartment update(long id, PartyDepartment department) {
        return entityManager.merge(department);
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
}

