package com.esq.rbac.service.util.externaldatautil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import static java.rmi.server.LogStream.log;

@Slf4j
public class ExternalServiceRestClient {

    private  RestTemplate restTemplate;
    private String connectTimeoutMs;
    private String readTimeoutMs;
    private String userName;
    private String password;
    private String baseUrl;
    private int defaultConnectTimeoutMs = 5000;
    private int defaultReadTimeoutMs = 5000;
    private Map<String, String> additionalProperties;


    public ExternalServiceRestClient() {
        this(false);
    }

    public ExternalServiceRestClient(boolean ignoreUnknownFields) {
//        restTemplate = new RestTemplate();

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        if(connectTimeoutMs!=null&&!connectTimeoutMs.isEmpty())
            httpRequestFactory.setConnectTimeout(Integer.parseInt(connectTimeoutMs));
        else
            httpRequestFactory.setConnectTimeout(defaultConnectTimeoutMs);
        try{
            if(readTimeoutMs!=null&&!readTimeoutMs.isEmpty())
                httpRequestFactory.wait(Long.parseLong(readTimeoutMs));
            else
                httpRequestFactory.wait(defaultReadTimeoutMs);
        }catch (Exception e){
            log("unable to se wait time");
        }
        restTemplate = new RestTemplate(httpRequestFactory);
        ObjectMapper objectMapper = new ObjectMapper();
        if (ignoreUnknownFields) {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        restTemplate.setMessageConverters(Collections.singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));
    }


    public void setConnectTimeoutMs(String connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
//        try {
//            int timeout = Integer.parseInt(this.connectTimeoutMs);
//            restTemplate.getInterceptors().add(new TimeoutInterceptor(timeout));
//        } catch (Exception e) {
//            restTemplate.getInterceptors().add(new TimeoutInterceptor(defaultConnectTimeoutMs));
//        }
    }

    public void setReadTimeoutMs(String readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
//        try {
//            int timeout = Integer.parseInt(this.readTimeoutMs);
//            restTemplate.getInterceptors().add(new TimeoutInterceptor(timeout));
//        } catch (Exception e) {
//            restTemplate.getInterceptors().add(new TimeoutInterceptor(defaultReadTimeoutMs));
//        }
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
        // Not applicable for RestTemplate, handled by underlying HTTP client automatically.
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        // Not applicable for RestTemplate, handled by underlying HTTP client automatically.
    }


    public String getUrl(String... parts) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.baseUrl);
        if (parts != null && parts.length > 0) {
            for (String p : parts) {
                sb.append("/");
                sb.append(p);
            }
        }
        ExternalServiceRestClient.log.debug("getUrl; {}", sb.toString());
        return sb.toString();
    }

    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
            String authString = userName + ":" + password;
//            byte[] authBytes = Base64.encodeBase64(authString.getBytes());
            byte[] authBytes = Base64.getEncoder().encode(authString.getBytes());
            String authHeader = "Basic " + new String(authBytes);
            headers.set("Authorization", authHeader);
        }
        return headers;
    }

    public ResponseEntity<String> get(String... parts) {
        String url = getUrl(parts);
        HttpHeaders headers = createHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    public RestTemplate getRestTemplate(){
        return this.restTemplate;
    }

    public ResponseEntity<String> post(Object requestBody, String... parts) {
        String url = getUrl(parts);
        HttpHeaders headers = createHeaders();
        HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

}
