package com.esq.rbac.service.userexternalrecord.repository;

import com.esq.rbac.service.userexternalrecord.domain.UserExternalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserExternalRecordRepository extends JpaRepository<UserExternalRecord,Integer> {

    UserExternalRecord findByExternalRecordId(String externalRecordId);
}
