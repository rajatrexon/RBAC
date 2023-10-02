package com.esq.rbac.service.makerchecker.repository;
import com.esq.rbac.service.makerchecker.domain.MakerChecker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

@Repository
public interface MakerCheckerRepository extends JpaRepository<MakerChecker, Long> {

    @Modifying
    @Query("UPDATE MakerChecker m SET m.entityStatus = :entityStatus, m.transactionBy = :transactionBy, m.transactionOn = :transactionOn, m.rejectReason = :rejectReason WHERE m.id = :id")
    void updateMakerCheckerEntity(@Param("id") Long id, @Param("entityStatus") Integer entityStatus, @Param("transactionBy") Integer transactionBy, @Param("transactionOn") Date transactionOn, @Param("rejectReason") String rejectReason);

    @Query("SELECT m FROM MakerChecker m WHERE m.entityId = :entityId AND m.entityName = :entityName")
    List<MakerChecker> getMakerCheckerByEntityIdAndEntityName(@Param("entityId") Integer entityId, @Param("entityName") String entityName);

    @Modifying
    @Query("DELETE FROM MakerChecker m WHERE m.entityId = :entityId AND m.entityName = :entityName")
    void deleteEntryByEntityId(@Param("entityId") Integer entityId, @Param("entityName") String entityName);

    @Modifying
    @Query("UPDATE MakerChecker m SET m.transactionBy = null WHERE m.transactionBy = :transactionBy")
    void updateTransactionForUserInChecker(@Param("transactionBy") Integer transactionBy);

    @Modifying
    @Query("UPDATE MakerChecker m SET m.createdBy = null WHERE m.createdBy = :createdBy")
    void updateTransactionForUserInMaker(@Param("createdBy") Integer createdBy);

}

