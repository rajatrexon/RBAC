package com.esq.rbac.service.base.repository;

import com.esq.rbac.service.base.query.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class Repository<EntityType> {

    private Class<EntityType> entityType;
    protected EntityManager entityManager;

    protected Repository(Class<EntityType> entityType) {
        this.entityType = entityType;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public EntityType create(EntityType instance) {
        entityManager.persist(instance);
        return instance;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public EntityType update(long id, EntityType instance) {
        return entityManager.merge(instance);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public EntityType readById(long id) {
        EntityType result = entityManager.find(entityType, id);
        log.debug("readById; {}", result);
        return result;
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public EntityType readByName(String name) {
        EntityType result = entityManager.find(entityType, name);
        log.debug("readByName; {}", result);
        return result;
    }



    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(long id) {
        EntityType managed = entityManager.find(entityType, id);
        if (managed == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(entityType);
            sb.append(" not found id: ").append(id);
            throw new IllegalArgumentException(sb.toString());
        }

        entityManager.remove(managed);
        log.debug("deleteById; {}", managed);
    }

    public Query<EntityType> getQuery() {
        return new Query<EntityType>(entityType, entityManager);
    }
}
