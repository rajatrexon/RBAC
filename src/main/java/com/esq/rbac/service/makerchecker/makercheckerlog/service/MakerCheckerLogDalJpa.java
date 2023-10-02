package com.esq.rbac.service.makerchecker.makercheckerlog.service;

import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.makerchecker.domain.MakerChecker;
import com.esq.rbac.service.makerchecker.makercheckerlog.domain.MakerCheckerLog;
import com.esq.rbac.service.makerchecker.makercheckerlog.repository.MakerCheckerLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class MakerCheckerLogDalJpa extends BaseDalJpa implements MakerCheckerLogDal {

    @Autowired
    private MakerCheckerLogRepository makerCheckerLogRepository;

    @Override
    public MakerCheckerLog createEntry(MakerChecker makerChecker) {
        MakerCheckerLog makerCheckerLog = new MakerCheckerLog();
        try {
            makerCheckerLog.setMakerCheckerId(makerChecker.getId());
            makerCheckerLog.setEntityJson(makerChecker.getEntityJson());
            makerCheckerLog.setEntityStatus(makerChecker.getEntityStatus());
            makerCheckerLog.setIsValid(makerChecker.getIsValid());
            makerCheckerLog.setTransactionBy(makerChecker.getTransactionBy());
            makerCheckerLog.setTransactionOn(makerChecker.getTransactionOn());
            makerCheckerLog.setEntityId(makerChecker.getEntityId());
            makerCheckerLog.setEntityName(makerChecker.getEntityName());
            makerCheckerLog.setEntityType(makerChecker.getEntityType());
            makerCheckerLog.setRejectReason(makerChecker.getRejectReason());
            makerCheckerLog.setOrganizationId(makerChecker.getOrganizationId());
            makerCheckerLog.setTenantId(makerChecker.getTenantId());
            makerCheckerLog.setEntityValue(makerChecker.getEntityValue());
            makerCheckerLog.setCreatedBy(Integer.valueOf(makerChecker.getCreatedBy()));
            makerCheckerLog.setCreatedOn(makerChecker.getCreatedOn());
            makerCheckerLogRepository.saveAndFlush(makerCheckerLog);

        } catch (Exception ex) {

            ErrorInfoException e = new ErrorInfoException("genError");
            log.info(ex.getMessage());
            throw e;
        }

        return makerCheckerLog;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MakerCheckerLog> getByMakerCheckerId(Integer makerCheckerId) {
        List<MakerCheckerLog> list=makerCheckerLogRepository.findByMakerCheckerId(makerCheckerId);
        if(list != null && list.size() > 0)
            return list;
        else
            return null;
    }

    @Override
    public void deleteHistoryByMakerCheckerId(Long makerCheckerId) {
        makerCheckerLogRepository.deleteByMakerCheckerId(makerCheckerId);
    }

}

