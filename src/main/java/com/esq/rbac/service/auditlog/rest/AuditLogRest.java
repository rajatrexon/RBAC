package com.esq.rbac.service.auditlog.rest;

import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.service.ApplicationDal;
import com.esq.rbac.service.auditlog.domain.AuditLog;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.contact.embedded.AuditLogJson;
import com.esq.rbac.service.contact.embedded.AuditLogJsonV3;
import com.esq.rbac.service.util.AuditLogUtil;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auditlog")
@Slf4j
public class AuditLogRest {

    private AuditLogService auditLogDal;

    private ApplicationDal applicationDal;

    @Autowired
    public AuditLogRest(AuditLogService auditLogDal, ApplicationDal applicationDal) {
        this.auditLogDal = auditLogDal;
        this.applicationDal = applicationDal;
    }

    @PostMapping
    public ResponseEntity<AuditLogJson> create(@RequestBody AuditLogJson auditLogJson) throws Exception {
        log.trace("create; AuditLogJson={}", auditLogJson);
        AuditLog aLog = auditLogDal.create(AuditLogUtil.convertToAuditLog(auditLogJson));
        return ResponseEntity.status(HttpStatus.CREATED).body(AuditLogUtil.convertToAuditLogJson(aLog));
    }



    @PostMapping("/v3")
    public ResponseEntity<AuditLogJson> createV3(HttpServletRequest request, @RequestBody AuditLogJsonV3 auditLogJson) throws Exception {
        log.trace("create; AuditLogJson={}", auditLogJson);

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        log.trace("getAuditLogByUserId; requestUri={}", ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURI());
        OptionPage optionPage = new OptionPage(uriInfo, 0, 100);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        AuditLogJson aLogV2 = AuditLogUtil.convertToAuditLogJsonFromV3(auditLogJson);
        if(auditLogJson != null && auditLogJson.getAppKey() != null && !auditLogJson.getAppKey().isEmpty()) {
            String applicationName = applicationDal.getApplicationNameByAppKey(auditLogJson.getAppKey());
            log.debug("applicationName {} by Appkey {}",applicationName,auditLogJson.getAppKey());
            aLogV2.setApplicationName(applicationName);
        }
        AuditLog aLog = auditLogDal.create(AuditLogUtil.convertToAuditLog(aLogV2));
        return ResponseEntity.status(HttpStatus.CREATED).body(AuditLogUtil.convertToAuditLogJson(aLog));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AuditLogJson[]> getAuditLogByUserId(HttpServletRequest request, @PathVariable("userId") int userId) throws Exception {
        log.trace("getAuditLogByUserId; userId={}", userId);
        log.trace("getAuditLogByUserId; requestUri={}", ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, 100);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<AuditLogJson> list = AuditLogUtil.convertToAuditLogJsonList(auditLogDal.getAuditLogByUserId(userId,options));
        AuditLogJson[] array = new AuditLogJson[list.size()];
        list.toArray(array);
        return ResponseEntity.status(HttpStatus.OK).body(array);
    }


    @GetMapping("/historyFeed/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogHistoryFeedByUserId(HttpServletRequest request, @PathVariable("userId") int userId) throws Exception {
        log.trace("getAuditLogHistoryFeedByUserId; userId={}", userId);
        log.trace("getAuditLogHistoryFeedByUserId; requestUri={}", ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURI());

        List<AuditLog> resultList= new LinkedList<AuditLog>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, 10);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Object[]> result=auditLogDal.getAuditLogHistoryFeedByUserId(userId,options);
        if(result!=null && result.size() > 0){
            for(Object[] temp:result){
                AuditLog ae = new AuditLog();
                try{
                    ae.setLogBuffer("{}");
                    ae.setUserId(userId);
                    ae.setAuditLogId( (temp[0]!=null?Integer.parseInt(temp[0].toString()):null) );
                    ae.setCreatedTime( (temp[1]!=null? (Date)temp[1]:null) );
                    ae.setApplicationId( (temp[2]!=null?Integer.parseInt(temp[2].toString()):null) );
                    ae.setTargetId( (temp[3]!=null?Integer.parseInt(temp[3].toString()):null) );
                    ae.setOperationId( (temp[4]!=null?Integer.parseInt(temp[4].toString()):null) );
                }
                catch(ArrayIndexOutOfBoundsException aie){
                    // do nothing, fields are missing in database
                }
                catch(NullPointerException ne){
                    // do nothing, fields are null in database
                }
                resultList.add(ae);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(resultList);
    }

    @GetMapping
    public ResponseEntity<AuditLogJson[]> list(HttpServletRequest request) throws Exception {
        log.trace("list; requestUri={}", ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes())
                .getRequest().getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, 10);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<AuditLogJson> list = AuditLogUtil.convertToAuditLogJsonList(auditLogDal.getList(options));
        AuditLogJson[] array = new AuditLogJson[list.size()];
        list.toArray(array);
        return ResponseEntity.status(HttpStatus.OK).body(array);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> count(HttpServletRequest request) throws Exception {
        log.trace("count; requestUri={}", ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes())
                .getRequest().getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);
        return ResponseEntity.status(HttpStatus.OK).body(auditLogDal.getCount(options));
    }

}
