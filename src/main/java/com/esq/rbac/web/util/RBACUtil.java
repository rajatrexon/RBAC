/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.web.util;

import com.esq.rbac.web.vo.Culture;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

public class RBACUtil {

	private static final Logger log = LoggerFactory.getLogger(RBACUtil.class);

	private static ObjectMapper objectMapper = new ObjectMapper();

	private static TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
	};

	public static final String RBAC_UAM_APPLICATION_NAME = "RBAC UAM";

	public static final String APP_KEY_IDENTIFIER_HEADER = "appKey";

	public static final String APP_KEY_IDENTIFIER_PARAM = "appKey";

	public static final String TICKET_HEADER_IDENTIFIER = "ticket";

	public static final String LOCALE_IDENTIFIER = "locale";

	public static final String DEFAULT_LOCALE_STRING = "en_US";

	public static final String DEFAULT_LOCALE_ENGLISH = "en-US";

	public static final String SESSION_REGISTRY_ACTION_DESTROY = "DESTROY";

	public static final String JAVA_APP_SERVICE_URL_IDENTIFIER = "login/cas";

	public static final String CAS_TICKET_SESSION_ATTRIBUTE = "casTicket";

	public static final String RBAC_CONF_DIR = "esq.conf.dir";

	public static final int RBAC_APPLICATION_ID = 100;

	public static final String APP_DASHBOARD_APPLICATION_NAME = "App Dashboard";

	public static final String SCOPE_KEY_USER_VIEW = "User.View";

	public static final String SCOPE_KEY_GROUP_VIEW = "Group.View";

	public static final String SCOPE_KEY_ROLE_VIEW = "Role.View";

	public static final String SCOPE_KEY_TENANT = "tenantScope";

	public static final String USER_SCOPE_QUERY = "userScopeQuery";

	public static final String GROUP_SCOPE_QUERY = "groupScopeQuery";

	public static final String ROLE_SCOPE_QUERY = "roleScopeQuery";

	public static final String TENANT_SCOPE_QUERY = "tenantScopeQuery";

	public static final String ORGANIZATION_SCOPE_QUERY = "organizationScopeQuery";

	public static final String CHECK_ENTITY_PERM_IDENTIFIER = "entityId";

	public static final String HOST_TENANT_TYPE_CODE_VALUE = "Host";

	public static final String TARGET_KEY_TENANT = "Tenant";

	public static final String UTC =  "(UTC) Coordinated Universal Time";

	public static final String SCOPE_KEY_REVOKE_APPLICATION_ACCESS = "revokeAppAccessScope";

	public static final String REVOKE_APP_ACCESS_SCOPE_QUERY = "revokeAppAccessScopeQuery";

	 public enum LOGOUT_ACTION {
		 	LOGOUT_ALL(0),
	    	LOGOUT_SSO(1),
	    	LOGOUT_NON_SSO(2),
	    	LOGOUT_NATIVE(3),
	    	LOGOUT_NONE(4),
		 	LOGOUT_SSO_TICKET(5),
		 	LOGOUT_NON_SSO_TICKET(6),
		 	LOGOUT_LINK_CLICK(7),
		 	LOGOUT_APP_KEY(8),
		 	LOGOUT_SSO_RESTART(9),
		 	LOGOUT_REFRESH_SSO(10);
			private Integer code;
			LOGOUT_ACTION(Integer code){
				this.code = code;
			}
	    	public Integer getCode(){
				return code;
			}
	    	};

	public static final String SESSION_LOGIN_INFO = "loginInfo";

	public static final String DEVICE_NORMAL = "WEB";
	public static final String DEVICE_TABLET = "TABLET";
	public static final String DEVICE_MOBILE = "MOBILE";
	public static final String DEVICE_NATIVE = "NATIVE";

	public static final String TENANT_LOGO_URN = "/tenantLogo/{tenantId}";
	static final int MINUTES_PER_HOUR = 60;
	static final int SECONDS_PER_MINUTE = 60;
	static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

	public static String encodeForScopeQuery(String input){
		try{
			if(input!=null && !input.isEmpty()){
				input=input.replaceAll("%", "%25");
			}
		}
		catch(Exception e){
			log.error("encodeForScopeQuery; Exception={}", e);
		}
		return input;
	}

	//kept header name static as rbac-lib-client goes to various applications & there is no common way to inject this value till we move to variables/REST call
	public static String getRemoteAddress(HttpServletRequest request){
		 String remoteAddress = request.getHeader(DeploymentUtil.getCLIENT_IP_HEADER());
		 if (remoteAddress == null || remoteAddress.length() == 0 || "unknown".equalsIgnoreCase(remoteAddress)) {
			 	remoteAddress = request.getRemoteAddr();
	        }
		 return remoteAddress;
	}

	public static String hashString(String input){
		if(input!=null && !input.isEmpty()){
			try{
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				digest.update(input.getBytes("UTF-8"));
				byte[] hash = digest.digest();
				StringBuilder sb = new StringBuilder();
	            for (int i = 0; i < hash.length; i++) {
	                sb.append(Integer.toHexString((hash[i] & 0xff) + 0x100).substring(1));
	            }
	            return sb.toString();
			}
			catch(Exception e){
				log.error("hashString; Exception={}", e);
				return "notHashed";
			}
		}
		return "";
	}

	public static String getServiceIdentifier(String service) {
		if(service!=null && !service.isEmpty()){
			try{
				 UriComponentsBuilder  uriComponentsBuilder = UriComponentsBuilder.fromUriString(service);
				 UriComponents uri = uriComponentsBuilder.build();
				 log.debug("getServiceIdentifier; service={}; uriPath={};",service, uri.getPath());
				 return uri.getPath()!=null?uri.getPath():"";
			}
			catch(Exception e){
				log.error("getServiceIdentifier; Exception={}", e);
				return service;
			}
		}
		return "";
	}

	public static Map<String, String> getHeaderMap(HttpServletRequest request) {
		Map<String, String> requestHeaderMap = new HashMap<String, String>();
		if(DeploymentUtil.getHEADERS_TO_LOG()==null){
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String key = headerNames.nextElement();
				String value = request.getHeader(key);
				requestHeaderMap.put(key, value);
			}
		}
		else{
			for(String headerName:DeploymentUtil.getHEADERS_TO_LOG()){
				requestHeaderMap.put(headerName, request.getHeader(headerName));
			}
		}
		return requestHeaderMap;
	}

//Todo	public static String getDeviceType(HttpServletRequest request, String defaultValue){
//		String deviceType = defaultValue;
//        Device device = DeviceUtils.getCurrentDevice(request);
//		if(device != null){
//			deviceType = device.isMobile()? RBACUtil.DEVICE_MOBILE: (device.isTablet()?RBACUtil.DEVICE_TABLET : RBACUtil.DEVICE_NORMAL);
//		}
//		return deviceType;
//	}

	public static String writeMapAsString(Map<String, String> map) {
		ObjectMapper objectMapper = new ObjectMapper();
		String js = "";
		try {
			js = objectMapper.writeValueAsString(map);
		} catch (Exception e) {
			log.error("writeMapAsString; Exception={};", e);
		}
		return js;
	}

	public static Locale getLocaleFromString(String localeString){
		try{
			return LocaleUtils.toLocale(localeString);
		}
		catch(Exception e){
			log.error("getLocaleFromString; Exception={};", e);
		}
		return LocaleUtils.toLocale(DEFAULT_LOCALE_STRING);
	}

	public static String getAppKeyFromUrl(String service) {
		if(service!=null){
			UriComponentsBuilder uriComponentsBuilderForService = UriComponentsBuilder
					.fromUriString(service);
			UriComponents uriForService = uriComponentsBuilderForService.build();
			if (uriForService != null
					&& uriForService.getQueryParams() != null
					&& !uriForService.getQueryParams().isEmpty()) {
				Set<String> querySet = uriForService.getQueryParams().keySet();
				for(String key: querySet){
					if(key.equalsIgnoreCase(APP_KEY_IDENTIFIER_PARAM)){
						return uriForService.getQueryParams().getFirst(key);
					}
				}
			}
		}
		return null;
	}

	public static String getAppKeyFromHeader(HttpHeaders headers) {
		if(headers!=null){
			if(headers.get(APP_KEY_IDENTIFIER_HEADER)!=null && !headers.get(APP_KEY_IDENTIFIER_HEADER).isEmpty()){
				return headers.get(APP_KEY_IDENTIFIER_HEADER).get(0);
			}
		}
		return null;
	}

	public static String generateLogoutRequestId(){
    	return RandomStringUtils.random(20, true, true);
    }

	public static List<String> getServiceUrlsFromConfigServiceUrl(String serviceUrl){
		List<String> response = new LinkedList<String>();
		if(serviceUrl!=null && !serviceUrl.isEmpty()){
			if(serviceUrl.indexOf(";;") > -1){
				serviceUrl = serviceUrl.trim();
				String[] urlArray = serviceUrl.split(";;");
				if(urlArray!=null && urlArray.length > 0){
					for(String url:urlArray){
						response.add(url.trim());
					}
				}
			}
			else{
				response.add(serviceUrl);
			}
		}
		return response;
	}

	public static String extractHomeUrlFromServiceUrl(String serviceUrl) {
		String serviceUrlToHomeUrl = serviceUrl;
		if (serviceUrl != null && !serviceUrl.isEmpty()
				&& serviceUrl.contains(JAVA_APP_SERVICE_URL_IDENTIFIER)) {
			serviceUrlToHomeUrl = serviceUrl.substring(0,
					serviceUrl.indexOf(JAVA_APP_SERVICE_URL_IDENTIFIER));
		}
		return serviceUrlToHomeUrl;
	}

	public static String getTenantLogoUrl(Long tenantId){
		return TENANT_LOGO_URN.replace("{tenantId}", tenantId.toString());
	}

	@SuppressWarnings("unchecked")
	public static String extractScopeForUser(Map<String, String> scopeMap,
			List<Long> selectedTenantList, boolean excludeSession) {
		StringBuilder sb = new StringBuilder();
		String userScope = scopeMap!=null?scopeMap.get(RBACUtil.SCOPE_KEY_USER_VIEW):null;
		if (userScope != null && !userScope.isEmpty()) {
			sb.append(" "+userScope+" ");
		}
		String tenantScope = scopeMap!=null?scopeMap.get(RBACUtil.SCOPE_KEY_TENANT):null;
		if (tenantScope != null && !tenantScope.isEmpty()
				&& tenantScope.length() > 1) {
			try {
				HashMap<String, Object> object = objectMapper.readValue(tenantScope,
						typeRef);
				List<Number> tenantArray = (List<Number>) object
						.get("tenantIdList");
				if(tenantArray!=null && !tenantArray.isEmpty()){
					List<Long> tenantList = new LinkedList<Long>();
                    for (Number number : tenantArray) {
                        if (!excludeSession && selectedTenantList != null && !selectedTenantList.isEmpty() && (!selectedTenantList.contains(number.longValue()))) {
                            continue;
                        }
                        tenantList.add(number.longValue());
                    }
					if(!tenantList.isEmpty()){
						if(sb.length() > 1){
							sb.append(" and ");
						}
						sb.append(" (u.organizationId in ( select org.organizationId from Organization org where org.tenantId in ( " + StringUtils.join(tenantList, ",") + " ) ) ) ");
					}
					else{
						if(sb.length() > 1){
							sb.append(" and ");
						}
						sb.append(" (u.organizationId in ( select org.organizationId from Organization org where org.tenantId in (0) ) ) ");
					}
				}
				return sb.toString();
			} catch (Exception e) {
				log.error("extractScopeForUserView; Exception={};", e);
			}
		}
		else{
			if(!excludeSession && selectedTenantList!=null && !selectedTenantList.isEmpty()){
				if(sb.length() > 1){
					sb.append(" and ");
				}
				return sb.append(" (u.organizationId in ( select org.organizationId from Organization org where org.tenantId in ( " + StringUtils.join(selectedTenantList, ",") + " ) ) ) ")
						.toString();
			}
			else if(sb.length() > 1){
				return sb.toString();
			}
		}
	return null;
	}

	@SuppressWarnings("unchecked")
	public static String extractScopeForGroup(Map<String, String> scopeMap,
			List<Long> selectedTenantList, boolean excludeSession) {
		StringBuilder sb = new StringBuilder();
		String groupScope = scopeMap!=null?scopeMap.get(RBACUtil.SCOPE_KEY_GROUP_VIEW):null;
		if (groupScope != null && !groupScope.isEmpty()) {
			sb.append(" "+groupScope+" ");
		}
		String tenantScope = scopeMap!=null?scopeMap.get(RBACUtil.SCOPE_KEY_TENANT):null;
		if (tenantScope != null && !tenantScope.isEmpty()
				&& tenantScope.length() > 1) {
			try {
				HashMap<String, Object> object = objectMapper.readValue(tenantScope,
						typeRef);
				List<Number> tenantArray = (List<Number>) object
						.get("tenantIdList");
				if(tenantArray!=null && !tenantArray.isEmpty()){
					List<Long> tenantList = new LinkedList<Long>();
                    for (Number number : tenantArray) {
                        if (!excludeSession && selectedTenantList != null && !selectedTenantList.isEmpty() && (!selectedTenantList.contains(number.longValue()))) {
                            continue;
                        }
                        tenantList.add(number.longValue());
                    }
					if(!tenantList.isEmpty()){
						if(sb.length() > 1){
							sb.append(" and ");
						}
						sb.append(" (g.tenantId in ( " + StringUtils.join(tenantList, ",") + " ) ) ");
					}
				}
				return sb.toString();
			} catch (Exception e) {
				log.error("extractScopeForGroupView; Exception={};", e);
			}
		}
		else{
			if(!excludeSession && selectedTenantList!=null && !selectedTenantList.isEmpty()){
				if(sb.length() > 1){
					sb.append(" and ");
				}
				return sb.append(" (g.tenantId in ( " + StringUtils.join(selectedTenantList, ",") + " ) ) ")
						.toString();
			}
			else if(sb.length() > 1){
				return sb.toString();
			}
		}
	return null;
	}

	@SuppressWarnings("unchecked")
	public static String extractScopeForTenant(Map<String, String> scopeMap,
			List<Long> selectedTenantList, boolean excludeSession) {
		String tenantScope = scopeMap != null ? scopeMap
				.get(RBACUtil.SCOPE_KEY_TENANT) : null;
		if (tenantScope != null && !tenantScope.isEmpty()
				&& tenantScope.length() > 1) {
			try {

				HashMap<String, Object> object = objectMapper.readValue(tenantScope,
						typeRef);
				List<Number> tenantArray = (List<Number>) object
						.get("tenantIdList");
				if (tenantArray != null && tenantArray.size() > 0) {
					List<Number> tenantList = new LinkedList<Number>();
					for (int i = 0; i < tenantArray.size(); i++) {
						if (!excludeSession
								&& selectedTenantList != null
								&& !selectedTenantList.isEmpty()
								&& (selectedTenantList.contains(tenantArray
										.get(i).longValue()) == false)) {
							continue;
						}
						tenantList.add(tenantArray.get(i).longValue());
					}
					if (!tenantList.isEmpty()) {
						return " t.tenantId in ( "
								+ StringUtils.join(tenantList, ",") + " ) ";
					}
				}
			} catch (Exception e) {
				log.error("extractScopeForTenant; Exception={};", e);
			}

		} else {
			if (!excludeSession && selectedTenantList != null
					&& !selectedTenantList.isEmpty()) {
				return " t.tenantId in ( "
						+ StringUtils.join(selectedTenantList, ",") + " ) ";
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static String extractScopeForOrganization(
			Map<String, String> scopeMap, List<Long> selectedTenantList,
			boolean excludeSession) {
		String tenantScope = scopeMap != null ? scopeMap
				.get(RBACUtil.SCOPE_KEY_TENANT) : null;
		if (tenantScope != null && !tenantScope.isEmpty()
				&& tenantScope.length() > 1) {
			try {
				HashMap<String, Object> object = objectMapper.readValue(
						tenantScope, typeRef);
				List<Number> tenantArray = (List<Number>) object
						.get("tenantIdList");
				if (tenantArray != null && tenantArray.size() > 0) {
					List<Long> tenantList = new LinkedList<Long>();
					for (int i = 0; i < tenantArray.size(); i++) {
						if (!excludeSession && selectedTenantList != null
								&& !selectedTenantList.isEmpty()
								&& (selectedTenantList.contains(tenantArray
										.get(i).longValue()) == false)) {
							continue;
						}
						tenantList.add(tenantArray.get(i).longValue());
					}
					if (!tenantList.isEmpty()) {
						return " org.tenantId in ( "
								+ StringUtils.join(tenantList, ",") + " ) ";
					}
				}
			} catch (Exception e) {
				log.error("extractScopeForOrganization; Exception={};", e);
			}
		} else {
			if (!excludeSession && selectedTenantList != null && !selectedTenantList.isEmpty()) {
				return " org.tenantId in ( "
						+ StringUtils.join(selectedTenantList, ",") + " ) ";
			}
		}
		return null;
	}


	@SuppressWarnings("unchecked")
	public static String extractScopeForMakerChecker(Map<String, String> scopeMap,
			List<Long> selectedTenantList, boolean excludeSession) {
		StringBuilder sb = new StringBuilder();
		String userScope = scopeMap!=null?scopeMap.get(RBACUtil.SCOPE_KEY_USER_VIEW):null;
		if (userScope != null && !userScope.isEmpty()) {
			sb.append(" "+userScope+" ");
		}
		String tenantScope = scopeMap!=null?scopeMap.get(RBACUtil.SCOPE_KEY_TENANT):null;
		if (tenantScope != null && !tenantScope.isEmpty()
				&& tenantScope.length() > 1) {
			try {
				HashMap<String, Object> object = objectMapper.readValue(tenantScope,
						typeRef);
				List<Number> tenantArray = (List<Number>) object
						.get("tenantIdList");
				if(tenantArray!=null && !tenantArray.isEmpty()){
					List<Long> tenantList = new LinkedList<Long>();
                    for (Number number : tenantArray) {
                        if (!excludeSession && selectedTenantList != null && !selectedTenantList.isEmpty() && (!selectedTenantList.contains(number.longValue()))) {
                            continue;
                        }
                        tenantList.add(number.longValue());
                    }
					if(!tenantList.isEmpty()){
						if(sb.length() > 1){
							sb.append(" and ");
						}
						sb.append(" (m.organizationId in ( select org.organizationId from Organization org where org.tenantId in ( " + StringUtils.join(tenantList, ",") + " ) ) ) ");
					}
					else{
						if(sb.length() > 1){
							sb.append(" and ");
						}
						sb.append(" (m.organizationId in ( select org.organizationId from Organization org where org.tenantId in (0) ) ) ");
					}
				}
				return sb.toString();
			} catch (Exception e) {
				log.error("extractScopeForUserView; Exception={};", e);
			}
		}
		else{
			if(!excludeSession && selectedTenantList!=null && !selectedTenantList.isEmpty()){
				if(sb.length() > 1){
					sb.append(" and ");
				}
				return sb.append(" (m.organizationId in ( select org.organizationId from Organization org where org.tenantId in ( " + StringUtils.join(selectedTenantList, ",") + " ) ) ) ")
						.toString();
			}
			else if(sb.length() > 1){
				return sb.toString();
			}
		}
	return null;
	}

	public static Boolean isSelectedLanguageValidForApplication(List<Culture> applicationLocaleList,
			String preferredLanguage) {
		Boolean isValid = Boolean.FALSE;
		if(applicationLocaleList == null || applicationLocaleList.isEmpty() || preferredLanguage == null || preferredLanguage.isEmpty())
			return isValid;

		isValid = applicationLocaleList.stream().filter(e -> e.getSupported()).anyMatch(e-> e.getShortName().equalsIgnoreCase(preferredLanguage));
		return isValid;

	}

	public static String getDeviceType(HttpServletRequest request, String defaultValue){
		String deviceType = defaultValue;

		////// Todo
//		Device device = DeviceUtils.getCurrentDevice(request);
//		if(device != null){
//			deviceType = device.isMobile()? RBACUtil.DEVICE_MOBILE: (device.isTablet()?RBACUtil.DEVICE_TABLET : RBACUtil.DEVICE_NORMAL);
//		}
		return deviceType;
	}


	public static LocalDateTime convertDateUsingOffset(Date date, Integer timeOffset) {
		if (date == null)
			return null;


		LocalDateTime lastLoginTimeInUTC = date.toInstant()
			      .atZone(ZoneId.of("UTC"))
			      .toLocalDateTime();
		log.debug("dateInUTC {}", lastLoginTimeInUTC);
		if(timeOffset == null || timeOffset == 0)
			return lastLoginTimeInUTC;

		int hours = timeOffset / MINUTES_PER_HOUR;
		int minutes = timeOffset % MINUTES_PER_HOUR;
		ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(hours, minutes);
		OffsetDateTime timeUtc = lastLoginTimeInUTC.atOffset(ZoneOffset.UTC);
		OffsetDateTime offsetTime = timeUtc.withOffsetSameInstant(zoneOffset); // 21:11:06 +03:00
		LocalDateTime dateTime = offsetTime.toLocalDateTime();
//		log.debug("beforeOffset {}, afterOffset {}",date,dateTime);
		return dateTime;
	}

}
