package com.esq.rbac.service.culture.service;

import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.culture.domain.Culture;
import com.esq.rbac.service.culture.embedded.ApplicationCulture;
import com.esq.rbac.service.culture.embedded.ResourceStrings;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.timezonemaster.domain.TimeZoneMaster;
import com.esq.rbac.service.timezonemaster.repository.TimeZoneMasterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
@Data
public class CultureDalJpa extends BaseDalJpa implements CultureDal {

    TimeZoneMasterRepository timeZoneMasterRepository;

    @Value("${resource.manager.url}")
    private String resourceManagerURL;

    @Value("${resource.manager.username}")
    private String username;

    @Value("${resource.manager.password}")
    private String password;

    @Value("${resource.manager.readTimeoutMills}")
    private Integer readTimeoutMills = 15000;

    @Value("${resource.manager.connectionTimeoutMills}")
    private Integer connectionTimeoutMills = 15000;

    @Autowired
    public void setTimeZoneMasterRepository(TimeZoneMasterRepository timeZoneMasterRepository) {
        this.timeZoneMasterRepository = timeZoneMasterRepository;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager");
        this.em = em;
        this.entityClass = TimeZoneMaster.class;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(readTimeoutMills);
        factory.setConnectTimeout(connectionTimeoutMills);
        log.debug("ResourceManager; readTimeoutMills {}, connectionTimeoutMills {}", readTimeoutMills, connectionTimeoutMills);
        return factory;
    }

    @Override
    public List<Culture> getSupportedCultures() {
        List<Culture> culturesList = new ArrayList<>();
        if (getResourceManagerURL() != null && !getResourceManagerURL().isEmpty()) {
            try {
                RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(username, password);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Culture> entity = new HttpEntity<Culture>(headers);
                ResponseEntity<Culture[]> response = restTemplate.exchange(resourceManagerURL + "/resources/cultures/supported", HttpMethod.GET, entity, Culture[].class);
                log.debug("response {}", response);
                if (response.getBody() != null) {
                    log.debug("response {}", response.getStatusCode());
                    culturesList = Arrays.asList(response.getBody());
                }
            } catch (Exception e) {
                log.error("run; Exception={}", e);
                culturesList.add(getDefaultCulture());
            }
        } else {
            log.debug("No resource Manager config found. Using default language as en-US");
            culturesList.add(getDefaultCulture());
        }

        return culturesList;
    }

    private Culture getDefaultCulture() {
        Culture culture = new Culture();
        culture.setFullName("English (United States)");
        culture.setShortName("en-US");
        culture.setSupported(true);
        return culture;
    }

    @Override
    public List<ApplicationCulture> getAllApplicationSupportedCulture() {
        List<ApplicationCulture> culturesList = new ArrayList<ApplicationCulture>();
        if (getResourceManagerURL() != null && !getResourceManagerURL().isEmpty()) {
            try {
                RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(username, password);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ApplicationCulture> entity = new HttpEntity<ApplicationCulture>(headers);
                ResponseEntity<List<ApplicationCulture>> response = restTemplate.exchange(resourceManagerURL + "/application/applications", HttpMethod.GET, entity, new ParameterizedTypeReference<List<ApplicationCulture>>() {
                });
                log.debug("response {}", response);
                if (response != null && response.getBody() != null) culturesList = response.getBody();
            } catch (Exception e) {
                log.error("run; Exception={}", e);
                culturesList = null;
            }
        }
        return culturesList;
    }

    @Override
    public ApplicationCulture assingLanguageToApplication(ApplicationCulture applicationCulture) throws Exception {
        if (getResourceManagerURL() != null && !getResourceManagerURL().isEmpty()) {
            try {
                RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(username, password);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ApplicationCulture> entity = new HttpEntity<ApplicationCulture>(applicationCulture, headers);
                ResponseEntity<ApplicationCulture> response = restTemplate.exchange(resourceManagerURL + "/application", HttpMethod.POST, entity, ApplicationCulture.class);
                log.debug("response {}", response);
                return getByApplicationKey(applicationCulture.getName());
            } catch (Exception e) {
                log.error("run; Exception={}", e);
                throw e;
            }
        } else throw new Exception("Cannot find Resource Manager configuration");


    }

    @Override
    public ApplicationCulture updateApplicationLanguage(ApplicationCulture applicationCulture) throws Exception {
        if (getResourceManagerURL() != null && !getResourceManagerURL().isEmpty()) {
            try {
                RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(username, password);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ApplicationCulture> entity = new HttpEntity<ApplicationCulture>(applicationCulture, headers);
                ResponseEntity<ApplicationCulture> response = restTemplate.exchange(resourceManagerURL + "/application", HttpMethod.PUT, entity, ApplicationCulture.class);
                log.debug("response {}", response);
                return getByApplicationKey(applicationCulture.getName());
            } catch (Exception e) {
                log.error("run; Exception={}", e);
                throw e;
            }
        } else throw new Exception("Cannot find Resource Manager configuration");
    }

    @Override
    public ApplicationCulture getByApplicationKey(String appKey) throws Exception {

        if (getResourceManagerURL() != null && !getResourceManagerURL().isEmpty()) {
            ApplicationCulture culturesList = null;
            try {
                RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(username, password);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ApplicationCulture> entity = new HttpEntity<ApplicationCulture>(headers);
                ResponseEntity<ApplicationCulture> response = restTemplate.exchange(resourceManagerURL + "/application/name/" + appKey, HttpMethod.GET, entity, ApplicationCulture.class);
                log.debug("response {}", response);
                if (response != null && response.getBody() != null) culturesList = response.getBody();
            } catch (Exception e) {
                log.error("run; Exception={}", e);
                culturesList = null;
            }
            return culturesList;
        } else throw new Exception("Cannot find Resource Manager configuration");
    }

    @Override
    public ApplicationCulture getByApplicationId(Integer appId) throws Exception {
        if (getResourceManagerURL() != null && !getResourceManagerURL().isEmpty()) {
            ApplicationCulture culturesList = null;
            try {
                RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(username, password);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ApplicationCulture> entity = new HttpEntity<ApplicationCulture>(headers);
                ResponseEntity<ApplicationCulture> response = restTemplate.exchange(resourceManagerURL + "/application/id/" + appId, HttpMethod.GET, entity, ApplicationCulture.class);
                log.debug("response {}", response);
                if (response != null && response.getBody() != null) culturesList = response.getBody();
            } catch (Exception e) {
                log.error("run; Exception={}", e);
                culturesList = null;
            }

            return culturesList;
        } else throw new Exception("Cannot find Resource Manager configuration");
    }

    @Override
    public ResourceStrings getApplicationResourceStrings(HttpHeaders headersList) {
        ResourceStrings resourceStrings = new ResourceStrings();
        if (getResourceManagerURL() != null && !getResourceManagerURL().isEmpty()) {
            try {
                RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
                HttpHeaders headers = new HttpHeaders();
                //Todo revisit point incase not working//unchecked//
                MultivaluedMap<String, String> reqHeaders = new MultivaluedHashMap<>();
                reqHeaders.add("appName", headersList.get("appName").get(0));
                reqHeaders.add("cultureName", headersList.get("cultureName").get(0));
                Iterator<String> it = reqHeaders.keySet().iterator();
                while (it.hasNext()) {
                    String theKey = it.next();
                    headers.add(theKey, reqHeaders.getFirst(theKey));
                }
                headers.setBasicAuth(username, password);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ResourceStrings> entity = new HttpEntity<ResourceStrings>(headers);
                ResponseEntity<ResourceStrings> response = restTemplate.exchange(resourceManagerURL + "/resources/strings/application/culture", HttpMethod.GET, entity, ResourceStrings.class);
                log.debug("response {}", response);
                if (response != null && response.getBody() != null) resourceStrings = response.getBody();
            } catch (Exception e) {
                log.error("run; Exception={}", e);
                throw e;
            }
        }
        return resourceStrings;
    }

    @Override
    public String getOffsetFromTimeZone(String timeZone) {
        //Fetching the timeZOneObject on the basis of timeZone Name from lookup and return the offset for the value fetched
        TimeZoneMaster lookupTimeZoneMaster = Lookup.getTimeZoneFromTimeZoneName(timeZone);
        if (lookupTimeZoneMaster != null) {
            return lookupTimeZoneMaster.getTimeOffset();
        } else {
            return timeZoneMasterRepository.getOffsetOfTimeZone(timeZone);
        }
    }
}
