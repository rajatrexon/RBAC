package com.esq.rbac.service.userhistory.service;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.userhistory.domain.UserHistory;
import com.esq.rbac.service.userhistory.repository.UserHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserHistoryServiceImpl extends BaseDalJpa implements UserHistoryService {


    private UserHistoryRepository userHistoryRepository;

    @Autowired
    public UserHistoryServiceImpl(UserHistoryRepository userHistoryRepository) {
        this.userHistoryRepository = userHistoryRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(UserHistoryServiceImpl.class);
    private static final Map<String, String> SORT_COLUMNS = new LinkedHashMap<String, String>();

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public UserHistory create(UserHistory userHistory) {
        return userHistoryRepository.save(userHistory);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<UserHistory> getAllUserHistory() {
        return userHistoryRepository.findAll();
    }
}
