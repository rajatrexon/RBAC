package com.esq.rbac.service.util.externaldatautil;

import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class ExternalDataAccessScopeBuilderImpl implements ExternalDataAccessScopeBuilder{
    public ExternalServiceRestClient appRestClient;

    private String filtersUrlPattern = "fields/%(scopeKey)";

    private String filtersDataUrlPattern = "fields/%(dataKey)/values?parentValue=%(parentValue)";

    private String scopeSqlBuilderPattern; //= "evaluate?scopeKey=%(scopeKey)&type=LINQ";

    @Autowired
    private HybridScopeHandler hybridScopeHandler;

    public void setAppRestClient(ExternalServiceRestClient appRestClient) {
        this.appRestClient = appRestClient;
        if(appRestClient.getAdditionalProperties()!=null){
            if(appRestClient.getAdditionalProperties().get("filtersUrlPattern")!=null){
                this.filtersUrlPattern =  appRestClient.getAdditionalProperties().get("filtersUrlPattern");
            }
            if(appRestClient.getAdditionalProperties().get("filtersDataUrlPattern")!=null){
                this.filtersDataUrlPattern =  appRestClient.getAdditionalProperties().get("filtersDataUrlPattern");
            }
            if(appRestClient.getAdditionalProperties().get("scopeSqlBuilderPattern")!=null){
                this.scopeSqlBuilderPattern =  appRestClient.getAdditionalProperties().get("scopeSqlBuilderPattern");
            }
        }
    }

    @Override
    public String list(ScopeConstraint scopeConstraint, HttpServletRequest servletRequest,
                       Integer userId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String update(ScopeConstraint scopeConstraint, String data,
                         String contentType, Integer userId) {
        // TODO Auto-generated method stub
        return null;
    }

//    @Autowired
//    public void setHybridScopeHandler(HybridScopeHandler hybridScopeHandler){
//        this.hybridScopeHandler = hybridScopeHandler;
//    }

    @Override
    public String getFilters(String scopeKey, String userName,
                             String additionalMap) {
        log.trace("getFilters; scopeKey={}; userName={}; additionalMap={}", scopeKey, userName, additionalMap);
        Map<String, String> values = new HashMap<>();
        RestTemplate restTemplate = null;
        String url=null;
        try {
            values.put("scopeKey", URIUtil.encodePathQuery(scopeKey));
            if(additionalMap != null && this.filtersUrlPattern.indexOf("isHost") != -1)
            {
                JsonParser parse = new JsonParser();
                JsonObject jsonObject = (JsonObject) parse.parse(additionalMap);
                Long tenantId = (jsonObject.get("selectedTenantId")).getAsLong();
                boolean isHost = Lookup.getTenantIsHostById(tenantId);
                values.put("isHost",isHost+"");
                log.info("isHost = {} ",isHost);
            }
            StrSubstitutor sub = new StrSubstitutor(values, "%(", ")");
//            webResource = appRestClient.resource(sub.replace(this.filtersUrlPattern));
//            return webResource
//                    .queryParam("additionalMap", additionalMap)
//                    .header("userName", userName)
//                    .accept(MediaType.APPLICATION_JSON).get(String.class);
             restTemplate = appRestClient.getRestTemplate();
             url = appRestClient.getUrl(sub.replace(this.filtersUrlPattern));
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            builder.queryParam("additionalMap",additionalMap);
            url = builder.build().toUriString();
            HttpHeaders headers = appRestClient.createHeaders();
            headers.set("userName",userName);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<?> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url,HttpMethod.GET,entity,String.class).getBody();

        } catch (Exception e) {
            log.error("getFilters; uri={}; Exception={};", restTemplate.getUriTemplateHandler().expand(url), e);
        }
        return null;
    }

    @Override
    public String getFilterKeyData(String sourcePath, String dataKey,
                                   String scopeKey, String userName, String additionalMap, String parentValue) {
        Map<String, String> values = new HashMap<String, String>();
//        WebResource webResource = null;
        RestTemplate restTemplate = null;
        String url = null;
        try {
            values.put("dataKey", URIUtil.encodePathQuery(dataKey));
            values.put(
                    "parentValue",
                    (parentValue != null) ? URIUtil
                            .encodePathQuery(parentValue) : "");

            if(additionalMap != null && this.filtersDataUrlPattern.indexOf("isHost") != -1)
            {
                JsonParser parse = new JsonParser();
                JsonObject jsonObject = (JsonObject) parse.parse(additionalMap);
                Long tenantId = (jsonObject.get("selectedTenantId")).getAsLong();
                boolean isHost = Lookup.getTenantIsHostById(tenantId);
                String identifiers = Lookup.getTenantIdentifiersById(tenantId);
                values.put("selectedTenantId", tenantId+"");
                values.put("isHost",isHost+"");
                values.put("tenantIdentifierField",identifiers);
                log.info("selectedTenantId = {},hostLoggedIn = {} ,identifiers = {} ",tenantId,isHost,identifiers);
            }

            StrSubstitutor sub = new StrSubstitutor(values, "%(", ")");
//            webResource = appRestClient
//                    .resource(sub.replace(this.filtersDataUrlPattern));
            restTemplate = appRestClient.getRestTemplate();
            log.info("webResource = {},",restTemplate);
            String groovyResponse = hybridScopeHandler.getFilterKeyData(sourcePath, dataKey, scopeKey, userName, additionalMap, parentValue);
            if(groovyResponse!=null && !groovyResponse.isEmpty()){
                return groovyResponse;
            }
//            String response = webResource
//                    .queryParam("dataKey", dataKey)
//                    .queryParam("additionalMap", additionalMap)
//                    .queryParam("scopeKey", scopeKey)
//                    .header("userName", userName)
//                    .accept(MediaType.APPLICATION_JSON).get(String.class);
             url = appRestClient.getUrl(sub.replace(this.filtersDataUrlPattern));
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            builder.queryParam("dataKey",dataKey);
            builder.queryParam("additionalMap",additionalMap);
            builder.queryParam("scopeKey", scopeKey);
            url = builder.build().toUriString();
            HttpHeaders headers = appRestClient.createHeaders();
            headers.set("userName",userName);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<?> entity = new HttpEntity<>(headers);

            String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();

            if(response!=null && !response.isEmpty()){
                if(response.charAt(0)=='"' && response.charAt(response.length()-1)=='"'){
                    response =  response.substring(1, response.length()-1);
                    response =  StringEscapeUtils.unescapeJavaScript(response);
                }
            }
            return response;
        } catch (Exception e) {
            log.error("getFilterKeyData; uri={}; Exception={};", restTemplate.getUriTemplateHandler().expand(url), e);
        }
        return null;
    }

    @Override
    public String validateAndBuildQuery(String scopeSql, String scopeJson, String scopeKey,
                                        String userName, String additionalMap) {
        log.trace(
                "validateAndBuildQuery; scopeSql={}; scopeJson={}; scopeKey={}; userName={}; additionalMap={}",
                scopeSql, scopeJson, scopeKey, userName, additionalMap);
        String groovyResponse = hybridScopeHandler.validateAndBuildQuery(scopeSql, scopeJson, scopeKey,
                userName,  additionalMap);
        if(groovyResponse!=null && !groovyResponse.isEmpty()){
            log.info("validateAndBuildQuery; scopeJson={}; groovyResponse={};", scopeJson, groovyResponse);
            return groovyResponse;
        }
        if(this.scopeSqlBuilderPattern!=null){
            Map<String, String> values = new HashMap<String, String>();
//            WebResource webResource = null;
            RestTemplate restTemplate = null;
            String url = null;
            try {
                values.put("scopeKey", URIUtil.encodePathQuery(scopeKey));
                StrSubstitutor sub = new StrSubstitutor(values, "%(", ")");
//                webResource = appRestClient
//                        .resource(sub.replace(this.scopeSqlBuilderPattern));
//                String response = webResource
//                        .queryParam("additionalMap", additionalMap)
//                        .header("userName", userName)
//                        .accept(MediaType.APPLICATION_JSON).entity(scopeJson, MediaType.APPLICATION_JSON).post(String.class);
                restTemplate = appRestClient.getRestTemplate();
                url = appRestClient.getUrl(sub.replace(this.scopeSqlBuilderPattern));
                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
                builder.queryParam("additionalMap",additionalMap);
                HttpHeaders headers = appRestClient.createHeaders();
                headers.set("userName",userName);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                HttpEntity<?> entity = new HttpEntity<>(headers);
                 url = builder.build().toUriString();
                String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
                if(response!=null && !response.isEmpty()){
                    if(response.charAt(0)=='"' && response.charAt(response.length()-1)=='"'){
                        response =  response.substring(1, response.length()-1);
                        response =  StringEscapeUtils.unescapeJavaScript(response);
                    }
                }
                log.info("validateAndBuildQuery; uri={}; scopeJson={}; response={};", restTemplate.getUriTemplateHandler().expand(url), scopeJson, response);
                return response;
            } catch (Exception e) {
                log.error("validateAndBuildQuery; uri={}; scopeJson={}; Exception={};", restTemplate.getUriTemplateHandler().expand(url), scopeJson, e);
            }
        }
        return null;
    }

}
