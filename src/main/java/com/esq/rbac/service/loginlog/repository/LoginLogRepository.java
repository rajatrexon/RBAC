package com.esq.rbac.service.loginlog.repository;

import com.esq.rbac.service.loginlog.domain.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Integer> {

    LoginLog findByUserName(String userName);
}
