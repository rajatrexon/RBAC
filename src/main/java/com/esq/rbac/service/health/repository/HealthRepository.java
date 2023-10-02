package com.esq.rbac.service.health.repository;

import com.esq.rbac.service.health.domain.RBACHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HealthRepository extends JpaRepository<RBACHealth, Long> {

    @Query("SELECT rbacHealth FROM RBACHealth rbacHealth WHERE rbacHealth.componentName = :componentName")
    List<RBACHealth> getRBACHealthByAppName(@Param("componentName") String componentName);

    @Modifying
    @Query("INSERT INTO RBACHealth (componentName, healthUpdateTime, updateTime) VALUES (:componentName, :healthUpdateTime, :updateTime)")
    RBACHealth createHealthInfo(String componentName, Date healthUpdateTime, Date updateTime);

    @Modifying
    @Query("UPDATE RBACHealth SET healthUpdateTime = :healthUpdateTime, updateTime = :updateTime WHERE componentName = :componentName")
    RBACHealth updateHealthInfo(Date healthUpdateTime, Date updateTime, String componentName);
}
