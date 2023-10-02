package com.esq.rbac.service.configuration.repository;

import com.esq.rbac.service.configuration.domain.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Integer> {

    @Query("SELECT c.confKey, c.confValue, c.confType, c.confOrder, c.confGroup, c.subGroup " +
            "FROM Configuration c " +
            "WHERE c.isVisible = '1' " +
            "ORDER BY c.confGroup, c.confOrder")
    List<Object[]> findVisibleConfigurations();

    @Modifying
    @Transactional
    @Query("UPDATE Configuration c SET c.confValue = :newConfValue WHERE c.confKey = :confKey")
    Integer updateConfValueByConfKey(@Param("newConfValue") String newConfValue, @Param("confKey") String confKey);
}

