package com.esq.rbac.service.role.targetsubdomain.repository;

import com.esq.rbac.service.role.targetsubdomain.domain.Target;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TargetRepository extends JpaRepository<Target,Integer> {
    @Modifying
    @Query("delete from Target t where t.targetId = :targetId")
    void deleteTargetById(@Param("targetId") Long targetId);

}
