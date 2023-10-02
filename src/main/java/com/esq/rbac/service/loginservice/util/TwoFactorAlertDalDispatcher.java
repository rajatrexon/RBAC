package com.esq.rbac.service.loginservice.util;

import com.esq.rbac.service.loginservice.embedded.LoginResponse;
import com.esq.rbac.service.loginservice.embedded.TwoFactorAuthVO;
import com.esq.rbac.service.user.domain.User;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class TwoFactorAlertDalDispatcher implements TwoFactorAlertDal{

    private Template template;

    private String templatePath;

    private String restURL;

    private Integer readTimeoutMills = 15000;

    private Integer connectionTimeoutMills = 15000;

    private static final Logger log = LoggerFactory.getLogger(TwoFactorAlertDalDispatcher.class);

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public void setRestURL(String restURL) {
        this.restURL = restURL;
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

    public void init() throws Exception {
        freemarker.template.Configuration ftlConfig = new freemarker.template.Configuration();
        FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(new File(templatePath));
        ftlConfig.setTemplateLoader(fileTemplateLoader);
        template = ftlConfig.getTemplate("dispactherEmailAlert.ftl");
    }


    @Override
    public LoginResponse sendAlert(User user, TwoFactorAuthVO twoFactorAuthVO) {
        final String alertData;
        LoginResponse loginResponse = new LoginResponse();
        HashMap<String, String> userData = new HashMap<String, String>();
        Map<String, Object> root = new HashMap<String, Object>();
        userData.put("username", user.getUserName());
        userData.put("firstname", user.getFirstName() != null ? user.getFirstName() : "");
        userData.put("lastname", user.getLastName() != null ? user.getLastName() : "");
        userData.put("emailaddress", user.getEmailAddress() != null ? user.getEmailAddress() : "");
        userData.put("mobile", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        userData.put("alertType", "twoFactAuthVerify");
        userData.put("channelType", twoFactorAuthVO.getChannelType());
        userData.put("OTP", twoFactorAuthVO.getToken());
        userData.put("alertId",(new Random().nextInt(999999))+"");

        try {
            root.put("properties", userData);
            root.put("source", "RBAC");
            root.put("clientReferenceKey", user.getUserId()+"");
            alertData = processTemplate(root);
        } catch (Exception e) {
            log.error("sendAlert; Exception={}", e);
            loginResponse.setResultCode(LoginResponse.OTP_SENDING_FAILED);
            loginResponse.setResultMessage(e.getMessage());
            return loginResponse;
        }
        try {
            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<String>(alertData, headers);
            restTemplate.postForEntity(restURL, entity, String.class);
            loginResponse.setResultCode(LoginResponse.OTP_SENT);
        } catch (Exception e) {
            log.error("run; Exception={}", e);
            loginResponse.setResultCode(LoginResponse.OTP_SENDING_FAILED);
            loginResponse.setResultMessage(e.getMessage());
            return loginResponse;
        }

        return loginResponse;
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
}
