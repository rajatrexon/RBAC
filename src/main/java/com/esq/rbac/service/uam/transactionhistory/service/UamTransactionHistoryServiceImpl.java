package com.esq.rbac.service.uam.transactionhistory.service;
import com.esq.rbac.service.uam.transactionhistory.domain.UamTransactionHistory;
import com.esq.rbac.service.uam.transactionhistory.repository.UamTransactionHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UamTransactionHistoryServiceImpl implements  UamTransactionHistoryService{


    private UamTransactionHistoryRepository uamTransactionHistoryRepository;


    public void setUamTransactionHistoryRepository(UamTransactionHistoryRepository uamTransactionHistoryRepository){
        this.uamTransactionHistoryRepository=uamTransactionHistoryRepository;
    }



    @Override
    @Transactional
    public UamTransactionHistory getUamDetails(String ticketNumber) {
        try{
            UamTransactionHistory uamTransactionHistory=uamTransactionHistoryRepository.findByTicketNumber(ticketNumber);
            if(uamTransactionHistory!=null) {
                return uamTransactionHistory;
            }
        }catch (Exception e){
            log.info("getUamDetails; no value found for ticketNumber {}", ticketNumber);
        }
        return null;
    }

    @Override
    @Transactional
    public UamTransactionHistory saveUamDetails(UamTransactionHistory uamTransactionHistory) {
        return uamTransactionHistoryRepository.save(uamTransactionHistory);
    }
}
