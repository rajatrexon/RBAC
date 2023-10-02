package com.esq.rbac.service.base.repository;
import com.esq.rbac.service.base.query.Query;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public abstract class BaseRepository<EntityType, ID>  implements JpaRepository<EntityType, ID>  {

    @Autowired
    EntityManager entityManager;

    private Class<EntityType> entityType;

    public BaseRepository(Class<EntityType> entityType) {
        this.entityType = entityType;
    }

    public Query<EntityType> getQuery() {
        return new Query<>(entityType, entityManager);
    }
}
