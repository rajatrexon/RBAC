package com.esq.rbac.service.restriction.repository;

import com.esq.rbac.service.restriction.domain.Restriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestrictionRepository extends JpaRepository<Restriction,Integer> {
}
