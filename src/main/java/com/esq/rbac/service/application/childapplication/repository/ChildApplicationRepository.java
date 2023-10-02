package com.esq.rbac.service.application.childapplication.repository;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ChildApplicationRepository extends JpaRepository<ChildApplication, Integer> {

    @Query("SELECT c.childApplicationName FROM ChildApplication c WHERE c.application.applicationId = " +
            "(SELECT a.applicationId FROM Application a WHERE a.name = ?1) AND c.isDefault = TRUE")
    String getRBACContextName(String applicationName);

    @Query("SELECT c FROM ChildApplication c WHERE c.childApplicationName = ?1")
    ChildApplication getChildApplicationByChildApplicationName( String childApplicationName);

    @Query("SELECT c FROM ChildApplication c WHERE c.appKey = ?1")
    ChildApplication getChildApplicationByAppKey(String appKey);

    @Query("SELECT c FROM ChildApplication c WHERE c.childApplicationName IN :childApplicationNames")
    List<ChildApplication> validateChildAppName(@Param("childApplicationNames") Set<String> childApplicationNames);

    @Query("SELECT c FROM ChildApplication c WHERE c.appKey IN :childAppKeys")
    List<ChildApplication> validateChildAppKey(@Param("childAppKeys")Set<String> childAppKeys);
}
