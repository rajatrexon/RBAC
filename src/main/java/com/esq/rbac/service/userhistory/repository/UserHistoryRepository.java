package com.esq.rbac.service.userhistory.repository;

import com.esq.rbac.service.userhistory.domain.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Integer>{
}
