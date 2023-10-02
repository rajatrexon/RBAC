package com.esq.rbac.service.userhistory.service;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.userhistory.domain.UserHistory;

import java.util.List;

public interface UserHistoryService extends BaseDal {

    UserHistory create(UserHistory userHistory);
    List<UserHistory> getAllUserHistory();
}
