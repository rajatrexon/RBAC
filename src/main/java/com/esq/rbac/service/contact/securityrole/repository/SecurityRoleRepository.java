package com.esq.rbac.service.contact.securityrole.repository;


import com.esq.rbac.service.contact.securityrole.queries.SecurityRoleQueries;
import com.esq.rbac.service.contact.securityrole.domain.SecurityRole;
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

@Service
@Slf4j
public class SecurityRoleRepository {

    private EntityManagerFactory entityManagerFactory = null;
    private EntityManager entityManager=null;
    private static final String SQL_WILDCARD = "%";

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        log.debug("setEntityManagerFactory");
        this.entityManagerFactory = entityManagerFactory;
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
    public List<SecurityRole> getSecurityRoles(String q) {
        TypedQuery<SecurityRole> query = entityManager.createQuery(
                SecurityRoleQueries.LIST_SECURITY_ROLES, SecurityRole.class);
        if (q == null) {
            q = "";
        }
        query.setParameter("q", wildcarded(q));
        return query.getResultList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public SecurityRole createFromName(String name) {
        SecurityRole securityRole = new SecurityRole();
        entityManager = entityManagerFactory.createEntityManager();
        securityRole.setName(name);
        entityManager.getTransaction().begin();
        entityManager.persist(securityRole);
        entityManager.getTransaction().commit();
        entityManager.refresh(securityRole);
        return securityRole;

    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public SecurityRole readById(long id) {
        SecurityRole result = entityManager.find(SecurityRole.class, id);
        log.debug("readById; result={}", result);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public SecurityRole update(long id, SecurityRole securityRole) {
        return entityManager.merge(securityRole);
    }


}





