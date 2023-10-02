package com.esq.rbac.service.uam.transactionhistory.repository;

import com.esq.rbac.service.uam.transactionhistory.domain.UamTransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UamTransactionHistoryRepository extends JpaRepository<UamTransactionHistory,Integer> {
    UamTransactionHistory findByTicketNumber(String ticketNumber);
}
