package com.esq.rbac.service.distributiongroup.distusergroup.repository;

import com.esq.rbac.service.distributiongroup.distusergroup.domain.DistUserMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DistUserMapRepository extends JpaRepository<DistUserMap, Integer> {



    // Named query for deleteUsersFromDistributionGroup
    @Modifying
    @Query("DELETE FROM DistUserMap dm WHERE dm.distId = :distId AND dm.userId IN (:userIds)")
    void deleteUsersFromDistributionGroup(@Param("distId") Integer distId, @Param("userIds") Set<Integer> userIds);

    // Named query for getByUserIdandDistId
    @Query("select dm from DistUserMap dm where dm.distId=:distId and dm.userId=:userId")
    DistUserMap getByUserIdandDistId(@Param("distId") Integer distId, @Param("userId") Integer userId);
}
