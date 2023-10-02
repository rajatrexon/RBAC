package com.esq.rbac.service.systemstate.repository;

import com.esq.rbac.service.systemstate.domain.SystemState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemStateRepository extends JpaRepository<SystemState, Integer> {

    SystemState findByIdentifier(String identifier);
}
