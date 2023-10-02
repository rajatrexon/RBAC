package com.esq.rbac.service.systemstate.service;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.systemstate.domain.SystemState;
import com.esq.rbac.service.systemstate.repository.SystemStateRepository;
import com.esq.rbac.service.util.AuditLogHelperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;


@Slf4j
@Service
public class SystemStateServiceImpl extends BaseDalJpa implements SystemStateService {


    private AuditLogService auditLogService;

    @Autowired
    public void setAuditLogService(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    private SystemStateRepository systemStateRepository;

    @Autowired
    public void setSystemStateRepository(SystemStateRepository systemStateRepository) {
        this.systemStateRepository = systemStateRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SystemState create(SystemState systemState, int systemStateId, String target, String operation) {
        SystemState systemStateModel=systemStateRepository.save(systemState);
        auditLogService.createSyncLog(systemStateId, systemState.getIdentifier(), target, operation, getObjectChangeSetLocal(null, systemState));
        return systemStateModel;
    }

    private Map<String, String> getObjectChangeSetLocal(SystemState oldSystemState, SystemState newSystemState) {
        AuditLogHelperUtil logHelperUtil = new AuditLogHelperUtil();
        //UserSync userSync = new UserSync();
        logHelperUtil.putToObjectChangeSet(OBJECTNAME,
                newSystemState != null ? newSystemState.getIdentifier() : oldSystemState.getIdentifier());
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_SYSTEMSTATEDATA,
                (newSystemState != null) ? newSystemState.getSystemData() : null,
                (oldSystemState != null) ? oldSystemState.getSystemData() : null, null, null);
        return logHelperUtil.getObjectChangeSet();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SystemState update(SystemState systemState, AuditLogInfo auditLogInfo) {
        //SystemState dbSystemState = em.find(SystemState.class, systemState.getId());
        SystemState dbSystemState = systemStateRepository.findById(systemState.getId()).get();
        SystemState oldSystemState = new SystemState();
        BeanUtils.copyProperties(dbSystemState, oldSystemState);
        dbSystemState.setIdentifier(systemState.getIdentifier());
        dbSystemState.setSystemData(systemState.getSystemData());

        auditLogService.createSyncLog(auditLogInfo.getLoggedInUserId(), oldSystemState.getId().toString(),
                auditLogInfo.getTarget(), auditLogInfo.getOperation(),
                getObjectChangeSetLocal(oldSystemState, dbSystemState));
        SystemState reSystemState = systemStateRepository.save(dbSystemState);
        return reSystemState;
    }


    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public SystemState getByIdentifier(String identifier) {
        return systemStateRepository.findByIdentifier(identifier);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Integer id, AuditLogInfo auditLogInfo) {
        systemStateRepository.deleteById(id);
    }
}
