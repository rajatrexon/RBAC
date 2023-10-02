package com.esq.rbac.service.distributiongroup.repository;

import com.esq.rbac.service.distributiongroup.domain.DistributionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DistributionGroupRepository extends JpaRepository<DistributionGroup, Integer> {



    // Named query for getDistributionGroupByDistId
    @Query("select dist from DistributionGroup dist where dist.distId=:distId")
    DistributionGroup getDistributionGroupByDistId(@Param("distId") Integer distId);

    // Named query for getDistributionByName
    @Query("select d from DistributionGroup d where d.distName=:distName AND d.isActive = 1")
    DistributionGroup getDistributionByName(@Param("distName") String distName);
}

