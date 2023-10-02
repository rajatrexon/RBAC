package com.esq.rbac.service.password.paswordHistory.repository;

import com.esq.rbac.service.password.paswordHistory.domain.PasswordHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory,Long> {

    @Query("select h from User u join PasswordHistory h on (h.userId = u.userId) where u.userName=:userName order by h.setTime desc")
    Page<PasswordHistory> getPasswordHistoryByUserName(@Param("userName") String userName, Pageable pageable);

    @Query("select h from PasswordHistory h where h.userId=:userId order by h.setTime desc")
    Page<PasswordHistory> getPasswordHistoryByUserId(@Param("userId") Integer userId,Pageable pageable);

    @Query(nativeQuery = true, value = "select count(*) from rbac.passwordHistory ph where ph.userId=? and ph.setTime between (select DATEADD(HOUR,?,GETUTCDATE())) and (select GETUTCDATE())")
    int noOfPasswordChanged(@Param("userId") Long userId, @Param("hours") int hours);

}
