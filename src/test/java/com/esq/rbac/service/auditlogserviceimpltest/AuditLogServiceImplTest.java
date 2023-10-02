package com.esq.rbac.service.auditlogserviceimpltest;

import com.esq.rbac.service.auditlog.domain.AuditLog;
import com.esq.rbac.service.auditlog.repository.AuditLogRepository;
import com.esq.rbac.service.auditlog.service.AuditLogServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;


@SpringBootTest
@Slf4j
class AuditLogServiceImplTest {

    @Mock
    AuditLogRepository auditLogRepository;

    @InjectMocks
    AuditLogServiceImpl auditLogDal;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {


        final Integer applicationId = 201;
        final Date createdTime = new Date();
        final Integer targetId = 205;
        final Integer operationId = 218;
        final Integer userId = 1004;
        final String queryField1 = "User.Update";
        final String queryField2 = "User.Update2";
        final Boolean isAlertable = false;
        final Boolean isSuccess = true;
        final Boolean isCompressed = false;
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("targetuserId", "1002");
        properties.put("firstName:old", "Jim");
        properties.put("firstName:new", "James");
        properties.put("nickName:new", "Jim");
        log.info("auditLogTest");

        AuditLog auditLog = new AuditLog();
        auditLog.setApplicationId(applicationId);
        auditLog.setTargetId(targetId);
        auditLog.setOperationId(operationId);
        auditLog.setCreatedTime(createdTime);
        auditLog.setUserId(userId);
        auditLog.setQueryField1(queryField1);
        auditLog.setQueryField2(queryField2);
        auditLog.setIsAlertable(isAlertable);
        auditLog.setIsSuccess(isSuccess);
        auditLog.setIsCompressed(isCompressed);
        auditLog.setLogBuffer(properties.toString());

        log.info("auditLogTest; create");

        when(auditLogRepository.save(auditLog)).thenReturn(auditLog);

        AuditLog createdAuditLog = auditLogDal.create(auditLog);

        Assertions.assertEquals(auditLog,createdAuditLog);




    }

    @Test
    void getList() {
    }

    @Test
    void getAuditLogByUserId() {
    }

    @Test
    void getAuditLogHistoryFeedByUserId() {
    }

    @Test
    void deleteById() {
    }

    @Test
    void getCount() {
    }

    @Test
    void createAsyncLog() {
    }

    @Test
    void createAuditLogUseFromAuditLogProcessor() {
    }

    @Test
    void createSyncLog() {
    }

    @Test
    void getUserLogRunnable() {
    }
}