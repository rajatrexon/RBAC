package com.esq.rbac.service.makerchecker.makercheckerlog.service;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.makerchecker.domain.MakerChecker;
import com.esq.rbac.service.makerchecker.makercheckerlog.domain.MakerCheckerLog;

import java.util.List;

public interface MakerCheckerLogDal extends BaseDal {

    MakerCheckerLog createEntry(MakerChecker makerChecker);

    List<MakerCheckerLog> getByMakerCheckerId(Integer makerCheckerId);

    void deleteHistoryByMakerCheckerId(Long makerCheckerId);
}