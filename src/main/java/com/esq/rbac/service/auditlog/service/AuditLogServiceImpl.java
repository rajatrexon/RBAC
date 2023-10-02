package com.esq.rbac.service.auditlog.service;
import com.esq.rbac.service.application.repository.ApplicationRepo;
import com.esq.rbac.service.auditlog.domain.AuditLog;
import com.esq.rbac.service.auditlog.repository.AuditLogRepository;
import com.esq.rbac.service.auditlogprocessor.AuditLogDalProcessor;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.util.json.JacksonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    //private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);


    @Autowired
    private AuditLogRepository auditLogRepository;




    private EntityManager em;
    private int THREAD_SIZE = 1;
    ExecutorService userExecutor = Executors.newFixedThreadPool(THREAD_SIZE);
    private static final ObjectMapper jsonMapper = JacksonFactory.getObjectMapper();

    private AuditLogDalProcessor auditLogDalProcessor;

    @Autowired
    public void setAuditLogDalProcessor(AuditLogDalProcessor auditLogDalProcessor) {
        log.trace("setAuditLogDalProcessor; {};", auditLogDalProcessor);
        this.auditLogDalProcessor = auditLogDalProcessor;

    }

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager");
        this.em = em;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLog create(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<AuditLog> getList(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getList(em,AuditLog.class, "select a from AuditLog a", options);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<AuditLog> getAuditLogByUserId(int userId, Options options) {
        Pageable pageable = null;
        OptionPage optionPage = options != null ? options.getOption(OptionPage.class) : null;
        if (optionPage != null) {
            pageable = PageRequest.of(optionPage.getFirstResult(), optionPage.getMaxResults());
        }
        Page<AuditLog> page = auditLogRepository.findAuditLogByUserId(userId,pageable);
        return page.getContent();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Object[]> getAuditLogHistoryFeedByUserId(int userId, Options options) {
        Pageable pageable = null;
        OptionPage optionPage = options != null ? options.getOption(OptionPage.class) : null;
        if (optionPage != null){
            pageable = PageRequest.of(optionPage.getFirstResult(), optionPage.getMaxResults());
        }
        List<Object[]> auditList = auditLogRepository.findAuditLogHistoryFeedByUserId(userId,pageable);
        return auditList;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(int auditLogId) {
        Optional<AuditLog> optionalAuditLog = auditLogRepository.findById(auditLogId);
        if (optionalAuditLog.isPresent()) {
            AuditLog auditLog = optionalAuditLog.get();
            auditLogRepository.delete(auditLog);
        }
    }

    @Override
    public int getCount(Options options){
        Filters filters = new Filters();
        return filters.getCount(em, "select count(a) from AuditLog a");
    }

    @Override
    public void createAsyncLog(Integer userId, String name, String target, String operation, Map<String, String> objectChanges) {
        log.debug("createAsyncLog; name={}, target={}, operation={}, objectChanges={}", name, target, operation, objectChanges);
        AuditLog auditLog = null;
        try {
            auditLog = createAuditLog(userId, target, operation, jsonMapper.writeValueAsString(objectChanges));
            userExecutor.execute(getUserLogRunnable(auditLog));
        } catch (Exception e) {
            log.error("createAsyncLog; name={}, target={}, operation={}, objectChanges={}, exception={}", name, target, operation, objectChanges, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createAuditLogUseFromAuditLogProcessor(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createSyncLog(Integer userId, String name, String target,
                              String operation, Map<String, String> objectChanges) {
        log.trace("createSyncLog; name={}, target={}, operation={}, objectChanges={}",name, target, operation, objectChanges);
        AuditLog auditLog = null;
        try {
            auditLog = createAuditLog(userId, target, operation,jsonMapper.writeValueAsString(objectChanges));
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error(
                    "createSyncLog; name={}, target={}, operation={}, objectChanges={}, exception={}",
                    name, target, operation, objectChanges, e);
        }
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        if (filters
                != null) {

            String applicationId = filters.get("applicationId");
            if (applicationId != null && applicationId.length() > 0) {
                result.addCondition("a.applicationId = :applicationId");
                result.addParameter("applicationId", Integer.valueOf(applicationId));
            }

            String targetId = filters.get("targetId");
            if (targetId != null && targetId.length() > 0) {
                result.addCondition("a.targetId=:targetId");
                result.addParameter("targetId", Integer.valueOf(targetId));
            }

            String operationId = filters.get("operationId");
            if (operationId != null && operationId.length() > 0) {
                result.addCondition("a.operationId=:operationId");
                result.addParameter("operationId", Integer.valueOf(operationId));
            }

            String logBuffer = filters.get("logBuffer");
            if (logBuffer != null && logBuffer.length() > 0) {
                result.addCondition("a.logBuffer like :logBuffer");
                result.addParameter("logBuffer", "%"+logBuffer+"%");
            }

        }
        return result;
    }
    private AuditLog createAuditLog(Integer userId, String target, String operation, String logBuffer) {
        String application = RBACUtil.RBAC_UAM_APPLICATION_NAME;
        Boolean isSuccess = Boolean.FALSE;
        Integer applicationId = Lookup.getApplicationId(application);
        Integer targetId = Lookup.getTargetId(application, target);
        Integer operationId = Lookup.getOperationId(application, target, operation);
        if (applicationId > -1 && targetId > -1 && operationId > -1) {
            isSuccess = Boolean.TRUE;
        }
        AuditLog auditLog = new AuditLog();
        log.debug("logCreate; Lookup; applicationId={0}, targetId={1}, operationId={2}", applicationId, targetId, operationId);
        auditLog.setApplicationId(applicationId);
        auditLog.setCreatedTime(new Date());
        auditLog.setTargetId(targetId);
        auditLog.setOperationId(operationId);
        auditLog.setUserId(userId);
        auditLog.setIsAlertable(Boolean.TRUE);
        auditLog.setIsSuccess(isSuccess);
        auditLog.setIsCompressed(Boolean.FALSE);
        auditLog.setLogBuffer(logBuffer);
       /* if (logBuffer.length() > AuditLog.LOG_BUFFER_MAX_SIZE) {
            auditLog.setLogBuffer(logBuffer.substring(0, AuditLog.LOG_BUFFER_MAX_SIZE));
        } else {
            auditLog.setLogBuffer(logBuffer);
        }*/
        return auditLog;
    }

    public Runnable getUserLogRunnable( final AuditLog auditLog){
        return new Runnable(){
            @Override
            public void run() {
                log.debug("run;");
                try {
                    auditLogDalProcessor.createAuditLog(auditLog);
                } catch (Exception e) {

                    log.error("getUserLogRunnable; exception={}", e);
                }
            }
        };
    }
}
