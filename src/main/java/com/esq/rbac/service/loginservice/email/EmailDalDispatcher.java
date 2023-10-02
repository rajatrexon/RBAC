package com.esq.rbac.service.loginservice.email;

import com.esq.rbac.service.loginservice.embedded.LoginRequest;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.util.EncryptionUtils;
import freemarker.cache.FileTemplateLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class EmailDalDispatcher implements EmailDal{


    private Template template;

    private String templatePath;

    private String restURL;

    private boolean alertEnabled;

    private Integer readTimeoutMills = 15000;

    private Integer connectionTimeoutMills = 15000;

    private static final ExecutorService executorService;


    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public void setRestURL(String restURL) {
        this.restURL = restURL;
    }

    public void setAlertEnabled(boolean alertEnabled) {
        this.alertEnabled = alertEnabled;
    }

    public Integer getReadTimeoutMills() {
        return readTimeoutMills;
    }

    public void setReadTimeoutMills(Integer readTimeoutMills) {
        this.readTimeoutMills = readTimeoutMills;
    }

    public Integer getConnectionTimeoutMills() {
        return connectionTimeoutMills;
    }

    public void setConnectionTimeoutMills(Integer connectionTimeoutMills) {
        this.connectionTimeoutMills = connectionTimeoutMills;
    }

    static {
        executorService = Executors.newCachedThreadPool();
    }

    public void init() throws Exception {
        freemarker.template.Configuration ftlConfig = new freemarker.template.Configuration();
        FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(
                new File(templatePath));
        ftlConfig.setTemplateLoader(fileTemplateLoader);
        template = ftlConfig.getTemplate("dispactherEmailAlert.ftl");
    }

    @Override
    public void sendAlert(User user, String alertId, String alertType) {
        if (alertEnabled) {
            final String alertData;
            HashMap<String, String> userData = new HashMap<String, String>();
            Map<String, Object> root = new HashMap<String, Object>();
            userData.put("username", user.getUserName());
            userData.put("firstname", user.getFirstName());
            userData.put("lastname", user.getLastName());
            userData.put("emailaddress", user.getEmailAddress());
            userData.put("alertType", alertType);
            try{
                if(!StringUtils.isEmpty(user.getIvrUserId())){
                    userData.put("userIVRId", user.getIvrUserId());
                }
                if(!StringUtils.isEmpty(user.getIvrPin())){
                    userData.put("userIVRPin", EncryptionUtils.encryptPassword(user.getIvrPin()));
                }
                userData.put("password", EncryptionUtils.encryptPassword(user.getChangePassword()));
                root.put("properties", userData);
                root.put("source", "RBAC");
                root.put("clientReferenceKey", alertId);
                alertData = processTemplate(root);
                //log.info("sendAlert; alertData={}", alertData);
            } catch (Exception e) {
                log.error("sendAlert; Exception={}",e);
                return;
            }
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_XML);
                        HttpEntity<String> entity = new HttpEntity<String>(alertData,
                                headers);
                        restTemplate.postForEntity(restURL, entity, String.class);
                    }
                    catch (Exception e) {
                        log.error("run; Exception={}",e);
                    }
                } } );

        }
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(readTimeoutMills);
        factory.setConnectTimeout(connectionTimeoutMills);
        return factory;
    }

    private String processTemplate(Map<String, Object> rootMap) throws Exception {
        StringWriter sw = new StringWriter();
        template.process(rootMap, sw);
        return sw.toString();
    }

    @Override
    public void sendFailedLoginAlert(User user, LoginRequest request, String channelType) {
        String alertData = null;
        HashMap<String, String> userData = new HashMap<String, String>();
        Map<String, Object> root = new HashMap<String, Object>();
        userData.put("username", user.getUserName());
        userData.put("firstname", user.getFirstName() != null ? user.getFirstName() : "");
        userData.put("lastname", user.getLastName() != null ? user.getLastName() : "");
        userData.put("emailaddress", user.getEmailAddress() != null ? user.getEmailAddress() : "");
        userData.put("mobile", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        userData.put("alertType", "failedLoginAttempt");
        userData.put("channelType", channelType);
        userData.put("IPAddress", request.getClientIP());
        userData.put("failedLoginDateTime", request.getStrCurrentDateTime() != null ?  request.getStrCurrentDateTime() : "");
        userData.put("alertId",user.getUserId()+""+(new Date()).getTime()+""+(new Random().nextInt(999)));
        try {
            root.put("properties", userData);
            root.put("source", "RBAC");
            root.put("clientReferenceKey", user.getUserId()+"");
            alertData = processTemplate(root);
        } catch (Exception e) {
            log.error("sendFailedLoginAlert; Exception={}", e);
        }
        try {
            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<String>(alertData, headers);
            restTemplate.postForEntity(restURL, entity, String.class);
            log.info("Alert sent for login attempt failure");
        } catch (Exception e) {
            log.error("sendFailedLoginAlert; run; Exception={}", e);
        }

    }
}
