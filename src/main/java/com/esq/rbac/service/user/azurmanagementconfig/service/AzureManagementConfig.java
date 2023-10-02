package com.esq.rbac.service.user.azurmanagementconfig.service;

import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.user.azurmanagementconfig.AzureADUser;
import com.esq.rbac.service.user.azurmanagementconfig.AzureADUserResponse;
import com.esq.rbac.service.user.azurmanagementconfig.AzureIdentities;
import com.esq.rbac.service.user.azurmanagementconfig.PasswordProfile;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.embedded.UserIdentity;
import com.esq.rbac.service.util.DeploymentUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Data
@Component
public class AzureManagementConfig {

    enum SignInType {emailAddress};

    private static final Logger log = LoggerFactory.getLogger(AzureManagementConfig.class);

    private String azureUserMgmtUrl;

    private String username;

    private String password;

    private Integer readTimeoutMs = 15000;

    private Integer connectTimeoutMs = 15000;

    @Autowired
    private DeploymentUtil deploymentUtil;
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(readTimeoutMs);
        factory.setConnectTimeout(connectTimeoutMs);
        return factory;
    }

    private HttpComponentsClientHttpRequestFactory patchClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        return requestFactory;

    }

    public String createUser(User user) {
        log.info("Create User: Is Azure User management Enabled {}", deploymentUtil.isAzureUserMgmtEnabled());

        if(user == null)
            return null;

        if (!deploymentUtil.isAzureUserMgmtEnabled())
            return null;

        if (user.getIsEnabled() == null || !user.getIsEnabled()) {
            log.info("Login Enabled is false. Not creating user in Azure AD");
            return null;
        }

        if (deploymentUtil.getUsernameIgnoreAzureUserMgmtRegex() != null
                && Pattern.matches(deploymentUtil.getUsernameIgnoreAzureUserMgmtRegex(), user.getUserName()))
            return null;

        AzureADUser azureUser = new AzureADUser();
        azureUser.setAccountEnabled(user.getIsEnabled());

        azureUser.setDisplayName(user.getFirstName() + " " + user.getLastName());
        azureUser.setGivenName(user.getFirstName());
        azureUser.setSurname(user.getLastName());
        azureUser.setMail(user.getEmailAddress());
        azureUser.setMailNickname(
                getMailNickName(user.getEmailAddress() != null ? user.getEmailAddress() : user.getUserName()));
//		if (user.getEmailAddress() != null && !user.getEmailAddress().isEmpty()) {
//			azureUser.getOtherMails().add(user.getEmailAddress());
//		}
        if (user.getHomeEmailAddress() != null && !user.getHomeEmailAddress().isEmpty()) {
            azureUser.getOtherMails().add(user.getHomeEmailAddress());
        }
        azureUser.setMobilePhone(user.getPhoneNumber());

        if (user.getHomePhoneNumber() != null && !user.getHomePhoneNumber().isEmpty()) {
            azureUser.getBusinessPhones().add(user.getHomePhoneNumber());
        }

        AzureIdentities identities = new AzureIdentities();
        identities.setSignInType(SignInType.emailAddress.toString());
        identities.setIssuer(deploymentUtil.getAzureUserIdentityIssuer());
        identities.setIssuerAssignedId(user.getUserName());
        Set<AzureIdentities> identityList = new HashSet<AzureIdentities>();
        identityList.add(identities);
        azureUser.setIdentities(identityList);

        azureUser.setUserType("Member");
        azureUser.setPasswordPolicies("DisablePasswordExpiration");
        PasswordProfile pwdProfile = new PasswordProfile();
        pwdProfile.setForceChangePasswordNextSignIn(user.getChangePasswordFlag());
        pwdProfile.setPassword(user.getChangePassword());

        azureUser.setPasswordProfile(pwdProfile);
        azureUser
                .setExtension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaClientID(deploymentUtil.getOauth2ClientId());
        azureUser.setExtension_3b1724e0cfb042908690028a932e5d69_RBACRedirectUrl(deploymentUtil.getAzureRedirectUri());
//		azureUser.setExtension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaUserPrincipalName(user.getUserName());
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(azureUser);
            log.debug("msalUser {}", json);
        } catch (IOException e) {

        }

        try {
            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AzureADUser> entity = new HttpEntity<AzureADUser>(azureUser, headers);
            ResponseEntity<AzureADUserResponse> response = restTemplate.exchange(azureUserMgmtUrl, HttpMethod.POST, entity,
                    AzureADUserResponse.class);
            AzureADUserResponse object = response.getBody();
            log.debug("response {}", response.getStatusCode());
            log.info("{}", object);
            String uniqueId = object.getId();
            return uniqueId;
//			if (response != null && (!response.getStatusCode().equals(HttpStatus.CREATED) || !response.getStatusCode().equals(HttpStatus.OK))) {
//				ErrorInfoException e = new ErrorInfoException("genError");
//				e.getParameters().put("value", "User Create Failed: Azure responded with a status "
//						+ response.getStatusCode() + " for User Create.");
//				throw e;
//			}
        } catch (HttpClientErrorException e) {
            String message = "User Create Failed:" + e.getMessage();
            String responseBody = e.getResponseBodyAsString();
            log.error("statusText {}, status {}, responseBody {}", e.getStatusText(), e.getStatusCode(), responseBody);
            if (responseBody != null && !responseBody.isEmpty()) {
                try {
                    JsonNode object = objectMapper.readTree(responseBody);
                    JsonNode errorObj = object.get("error");
                    message = errorObj.get("message").asText();
                } catch (Exception ce) {

                }
            }
            log.error("Azure Create User; Exception={}", e);
            ErrorInfoException errrInfo = new ErrorInfoException("genError");
            errrInfo.getParameters().put("value", message);
            throw errrInfo;

        } catch (Exception ex) {
            log.error("Azure Create User; Exception={}", ex);
            ErrorInfoException e = new ErrorInfoException("genError");
            e.getParameters().put("value", "User Create Failed:" + ex.getMessage());
            throw e;
        }
    }

    public String getMailNickName(String mail) {
        if (mail == null || mail == "")
            return "";

        String arr[] = mail.split("@");
        return arr[0];
    }

    public void deleteUser(String userName) {
        log.info("Delete User: Is Azure User management Enabled {}", deploymentUtil.isAzureUserMgmtEnabled());

        if(userName == null)
            return;

        if (!deploymentUtil.isAzureUserMgmtEnabled())
            return;

        if(deploymentUtil.getUsernameIgnoreAzureUserMgmtRegex() != null &&
                Pattern.matches(deploymentUtil.getUsernameIgnoreAzureUserMgmtRegex(), userName))
            return;

        try {
            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AzureADUser> entity = new HttpEntity<AzureADUser>(headers);
            ResponseEntity<AzureADUser> response = restTemplate.exchange(azureUserMgmtUrl + "/" + userName,
                    HttpMethod.DELETE, entity, AzureADUser.class);

            log.debug("response {}", response.getStatusCode());
            log.info("{}", Arrays.asList(response.getBody()));
            if (response != null && !response.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                ErrorInfoException e = new ErrorInfoException("genError");
                e.getParameters().put("value", "User Delete Failed: Azure responded with a status "
                        + response.getStatusCode() + " for User Delete.");
                throw e;
            }
        } catch (HttpClientErrorException e) {
            String message = "User Delete Failed:" + e.getMessage();
            String responseBody = e.getResponseBodyAsString();
            log.error("statusText {}, status {}, responseBody {}", e.getStatusText(), e.getStatusCode(), responseBody);
            if (responseBody != null && !responseBody.isEmpty()) {
                try {
                    JsonNode object = new ObjectMapper().readTree(responseBody);
                    JsonNode errorObj = object.get("error");
                    message = errorObj.get("message").asText();
                } catch (Exception ce) {

                }
            }
            log.error("Azure Create User; Exception={}", e);
            ErrorInfoException errrInfo = new ErrorInfoException("genError");
            errrInfo.getParameters().put("value", message);
            throw errrInfo;

        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().equalsIgnoreCase("404 Not Found")) {
                log.error("Azure Delete: User {} not found in Azure AD ", userName);
                return;
            }
            log.error("Azure Delete User; Exception={}", ex);
            ErrorInfoException e = new ErrorInfoException("genError");
            e.getParameters().put("value", "User Delete Failed: " + ex.getMessage());
            throw e;
        }
    }

    public Boolean isAzureUserMgmtEnabled() {
        return deploymentUtil.isAzureUserMgmtEnabled();
    }

    public void updateUser(Map<String, Object> updateObj, String azureObjectId, String userName) {
        log.info("Update User: Is Azure User management Enabled {}", deploymentUtil.isAzureUserMgmtEnabled());
        if (!deploymentUtil.isAzureUserMgmtEnabled())
            return;

        if (updateObj != null && updateObj.isEmpty())
            return;

        if(userName == null)
            return;

        if(deploymentUtil.getUsernameIgnoreAzureUserMgmtRegex() != null &&
                Pattern.matches(deploymentUtil.getUsernameIgnoreAzureUserMgmtRegex(), userName))
            return;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = "";
            try {
                json = objectMapper.writeValueAsString(updateObj);
                log.debug("updateObj {}", json);
            } catch (JsonProcessingException ex) {
                ErrorInfoException e = new ErrorInfoException("genError");
                e.getParameters().put("value", "MSAL User Update JSON Parse Exception");
                throw e;
            }

            RestTemplate restTemplate = new RestTemplate(patchClientHttpRequestFactory());
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(json, headers);
            String URI = azureUserMgmtUrl + "/" + azureObjectId;
            log.debug("Update URI {}",URI);
            ResponseEntity<AzureADUserResponse> response = restTemplate.exchange(URI, HttpMethod.PATCH,
                    entity, AzureADUserResponse.class);

            AzureADUserResponse object = response.getBody();
            log.debug("response {}", response.getStatusCode());
            log.info("{}", object);
            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                ErrorInfoException e = new ErrorInfoException("genError");
                e.getParameters().put("value", "User Update Failed: Azure responded with a status "
                        + response.getStatusCode() + " for User Update.");
                throw e;
            }
        } catch (HttpClientErrorException e) {
            String message = "User Update Failed:" + e.getMessage();
            String responseBody = e.getResponseBodyAsString();
            log.error("statusText {}, status {}, responseBody {}", e.getStatusText(), e.getStatusCode(), responseBody);
            if (responseBody != null && !responseBody.isEmpty()) {
                try {
                    JsonNode object = new ObjectMapper().readTree(responseBody);
                    JsonNode errorObj = object.get("error");
                    message = errorObj.get("message").asText();
                } catch (Exception ce) {

                }
            }
            log.error("Azure Update User; Exception={}", e);
            ErrorInfoException errrInfo = new ErrorInfoException("genError");
            errrInfo.getParameters().put("value", message);
            throw errrInfo;

        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().equalsIgnoreCase("404 Not Found")) {
                log.error("Azure Update: ObjectId {} not found in Azure AD ", azureObjectId);
                return;
            }
            log.error("Azure Update User; Exception={}", ex);
            ErrorInfoException e = new ErrorInfoException("genError");
            e.getParameters().put("value", "User Update Failed: " + ex.getMessage());
            throw e;
        }
    }

    public void updateAzurePassword(User user, String newPassword) {
        log.info("Update User Password: Is Azure User management Enabled {}", deploymentUtil.isAzureUserMgmtEnabled());
        if (!deploymentUtil.isAzureUserMgmtEnabled())
            return;

        Map<String, Object> updateObj = new HashMap<String, Object>();
        PasswordProfile pwdProfile = new PasswordProfile();
        pwdProfile.setForceChangePasswordNextSignIn(Boolean.FALSE);
        pwdProfile.setPassword(newPassword);
        updateObj.put("passwordProfile", pwdProfile);

        String azureIdentity = null;
        List<UserIdentity> userIdentity = user.getIdentities();
        if(userIdentity != null && !userIdentity.isEmpty()) {
            for(UserIdentity ident: userIdentity) {
                if(ident.getIdentityType().equalsIgnoreCase(UserIdentity.AZURE_AD_ACCOUNT))
                    azureIdentity = ident.getIdentityId();
            }
        }
        updateUser(updateObj, azureIdentity,user.getUserName());
    }
}
