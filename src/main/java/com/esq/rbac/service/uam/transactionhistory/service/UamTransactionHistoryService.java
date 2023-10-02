package com.esq.rbac.service.uam.transactionhistory.service;

import com.esq.rbac.service.uam.transactionhistory.domain.UamTransactionHistory;

public interface UamTransactionHistoryService {

    UamTransactionHistory getUamDetails(String ticketNumber);

    UamTransactionHistory saveUamDetails(UamTransactionHistory uamTransactionHistory);

}
