package com.esq.rbac.service.util;

import com.esq.rbac.service.auditlog.domain.AuditLog;
import com.esq.rbac.service.contact.embedded.AuditLogJson;
import com.esq.rbac.service.contact.embedded.AuditLogJsonV3;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.util.json.JacksonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class AuditLogUtil {

    private static final ObjectMapper jsonMapper = JacksonFactory
            .getObjectMapper();
    private static String application = RBACUtil.RBAC_UAM_APPLICATION_NAME;

    public static AuditLog convertToAuditLog(AuditLogJson auditLogRequest)
            throws IOException {
        AuditLog auditLog = new AuditLog();

        Boolean isSuccess = Boolean.FALSE;

        Integer applicationId = Lookup.getApplicationId(auditLogRequest
                .getApplicationName());
        Integer targetId = Lookup.getTargetId(
                auditLogRequest.getApplicationName(),
                auditLogRequest.getTargetName());
        Integer operationId = Lookup.getOperationId(
                auditLogRequest.getApplicationName(),
                auditLogRequest.getTargetName(),
                auditLogRequest.getOperationName());

        if (applicationId > -1 && targetId > -1 && operationId > -1) {
            isSuccess = Boolean.TRUE;
        }

        auditLog.setAuditLogId(auditLogRequest.getAuditLogId());
        auditLog.setCreatedTime(new Date());
        auditLog.setUserId(auditLogRequest.getUserId());
        auditLog.setApplicationId(applicationId);
        auditLog.setTargetId(targetId);
        auditLog.setOperationId(operationId);
        auditLog.setQueryField1(auditLogRequest.getQueryField1());
        auditLog.setQueryField2(auditLogRequest.getQueryField2());
        auditLog.setIsAlertable(auditLogRequest.getIsAlertable());
        auditLog.setIsSuccess(isSuccess);
        auditLog.setIsCompressed(Boolean.FALSE);
        String logBuffer = jsonMapper.writeValueAsString(auditLogRequest
                .getProperties());
        auditLog.setLogBuffer(logBuffer);
        /*
         * if (logBuffer.length() > AuditLog.LOG_BUFFER_MAX_SIZE) {
         * auditLog.setLogBuffer(logBuffer.substring(0,
         * AuditLog.LOG_BUFFER_MAX_SIZE)); } else {
         * auditLog.setLogBuffer(logBuffer); }
         */

        return auditLog;
    }

    public static AuditLogJson convertToAuditLogJson(AuditLog auditLog)
            throws IOException {
        AuditLogJson auditLogRequest = new AuditLogJson();

        auditLogRequest.setAuditLogId(auditLog.getAuditLogId());
        auditLogRequest.setCreatedTime(auditLog.getCreatedTime());
        auditLogRequest.setUserId(auditLog.getUserId());
        auditLogRequest.setApplicationName(Lookup.getApplicationName(auditLog
                .getApplicationId()));
        auditLogRequest.setTargetName(Lookup.getTargetName(
                auditLog.getApplicationId(), auditLog.getTargetId()));
        auditLogRequest.setOperationName(Lookup.getOperationName(
                auditLog.getApplicationId(), auditLog.getTargetId(),
                auditLog.getOperationId()));
        auditLogRequest.setQueryField1(auditLog.getQueryField1());
        auditLogRequest.setQueryField2(auditLog.getQueryField2());
        auditLogRequest.setIsAlertable(auditLog.getIsAlertable());
        auditLogRequest.setProperties(jsonMapper.readTree(auditLog
                .getLogBuffer()));

        return auditLogRequest;
    }

    public static List<AuditLogJson> convertToAuditLogJsonList(
            List<AuditLog> auditLogs) throws IOException {
        List<AuditLogJson> auditLogJsons = new ArrayList<AuditLogJson>();
        for (AuditLog auditLog : auditLogs) {
            auditLogJsons.add(convertToAuditLogJson(auditLog));
        }

        return auditLogJsons;
    }

    public static AuditLog createAuditLog(Integer userId, String target, String operation, Map<String, String> objectChanges) throws Exception {
        String logBuffer = jsonMapper.writeValueAsString(objectChanges);
        Boolean isSuccess = Boolean.FALSE;
        Integer applicationId = Lookup.getApplicationId(application);
        Integer targetId = Lookup.getTargetId(application, target);
        Integer operationId = Lookup.getOperationId(application, target, operation);
        if (applicationId > -1 && targetId > -1 && operationId > -1) {
            isSuccess = Boolean.TRUE;
        }
        AuditLog auditLog = new AuditLog();
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

    public static AuditLogJson convertToAuditLogJsonFromV3(AuditLogJsonV3 auditV3) {
        AuditLogJson auditV2 = new AuditLogJson();
        auditV2.setAuditLogId(auditV3.getAuditLogId());
        auditV2.setCreatedTime(auditV3.getCreatedTime());
        auditV2.setIsAlertable(auditV3.getIsAlertable());
        auditV2.setOperationName(auditV3.getOperationName());
        auditV2.setProperties(auditV3.getProperties());
        auditV2.setQueryField1(auditV3.getQueryField1());
        auditV2.setQueryField2(auditV3.getQueryField2());
        auditV2.setTargetName(auditV3.getTargetName());
        auditV2.setUserId(auditV3.getUserId());
        return auditV2;
    }
}

