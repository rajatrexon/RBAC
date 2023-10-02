package com.esq.rbac.service.util;

import com.esq.rbac.service.auditlog.domain.AuditLog;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.util.json.JacksonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AuditLogger {

    private static final ObjectMapper jsonMapper = JacksonFactory.getObjectMapper();

    @Autowired
    private AuditLogService auditLogDal;
    private String application = RBACUtil.RBAC_UAM_APPLICATION_NAME;

    public AuditLogger(AuditLogService auditLogDal) {
        this.auditLogDal = auditLogDal;
    }

    public void logCreate(Integer userId, String name, String target, String operation) throws IOException {
        log.debug("logCreate; name={}, target={}, operation={}", name, target, operation);

        Map<String, String> jsonObj = new HashMap<String, String>();
        jsonObj.put("name", name);

        AuditLog auditLog = createAuditLog(userId, target, operation, jsonMapper.writeValueAsString(jsonObj));

        auditLogDal.create(auditLog);
    }

    public void logCreate(Integer userId, String name, String target, String operation, Map<String, String> objectChanges) throws IOException {
        log.debug("logCreate; name={}, target={}, operation={}, objectChanges={}", name, target, operation, objectChanges);

        AuditLog auditLog = createAuditLog(userId, target, operation, jsonMapper.writeValueAsString(objectChanges));

        auditLogDal.create(auditLog);
    }

    public void logCreate() {
    }

    private AuditLog createAuditLog(Integer userId, String target, String operation, String logBuffer) {
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
}
