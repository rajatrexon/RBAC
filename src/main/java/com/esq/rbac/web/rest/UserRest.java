/*
 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.rest;
import com.esq.rbac.web.client.RestClient;
import com.esq.rbac.web.client.SSORestClient;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.exception.ClientHandlerException;
import com.esq.rbac.web.exception.ErrorInfo;
import com.esq.rbac.web.exception.ErrorInfoException;
import com.esq.rbac.web.exception.ForbiddenException;
import com.esq.rbac.web.util.ImportUtil;
import com.esq.rbac.web.util.JCryptionUtil;
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.util.WebParamsUtil;
import com.esq.rbac.web.vo.LoginType;
import com.esq.rbac.web.vo.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Mono;
import javax.crypto.BadPaddingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.*;

@RestController
@RequestMapping(UserRest.RESOURCE_PATH)
public class UserRest {

    private static final Logger log = LoggerFactory.getLogger(UserRest.class);
    public static final String RESOURCE_PATH = "users";
    public static final String ERROR_ENCRYPTION_CODE = "encryptionFailed";

	@Autowired
    private RestClient restClient;
    private UserDetailsService userDetailsService;
    private ImportUtil importUtil;
    private SSORestClient sSORestClient;
    private int usersExportDataResponseWriteSizeBytes = 1024; // stream read buffer size for reports
    
    @Autowired
    public void setRestClient(RestClient restClient) {
        log.debug("setRestClient");
        this.restClient = restClient;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        log.debug("setUserDetailsService; {}", userDetailsService);
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setImportUtil(ImportUtil importUtil) {
        log.trace("setImportUtil; {};", importUtil);
        this.importUtil = importUtil;
    }
    
    @Autowired
    public void setsSORestClient(SSORestClient sSORestClient) {
		this.sSORestClient = sSORestClient;
	}

	//history feed used to create user and change password on next login
	@PostMapping(value = "/historyFeed",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<User>> create(@RequestBody User user, HttpServletRequest httpServletRequest) throws Exception {
        log.trace("create; user={}", user);
        userDetailsService.verifyPermission("User.Create");
		if(user.getGeneratePasswordFlag().equals(Boolean.FALSE) && user.getChangePassword() != null && 
				httpServletRequest.getSession() != null && httpServletRequest.getSession().getAttribute("keys") != null){
    		KeyPair keys = (KeyPair) httpServletRequest.getSession().getAttribute("keys");
			try {
				user.setChangePassword(new String(JCryptionUtil.decrypt(user.getChangePassword(), keys.getPrivate())));
				httpServletRequest.getSession().removeAttribute("keys");
			} catch (BadPaddingException e) {
				log.error("create; ip={}; userName={};  BadPaddingException={}",
						RBACUtil.getRemoteAddress(httpServletRequest), user.getUserName(),  e);
				ErrorInfoException errorInfo = new ErrorInfoException(ERROR_ENCRYPTION_CODE, ERROR_ENCRYPTION_CODE);
				throw errorInfo;
			}
    	}
        if(user.getChangePassword()!=null && !user.getChangePassword().isEmpty()){
        	userDetailsService.verifyPermission("User.CreatePassword");
        }
        if(user.getGeneratePasswordFlag()!=null && user.getGeneratePasswordFlag().equals(Boolean.TRUE)){
        	userDetailsService.verifyPermission("User.GeneratePassword");
        }
        if(user.getGroupId()!=null && user.getGroupId()!=0){
        	userDetailsService.verifyPermission("User.AssignGroup");
        }
        if(user.getRestrictions()!=null && (  
	        		(user.getRestrictions().getAllowedIPs()!=null && !user.getRestrictions().getAllowedIPs().isEmpty()) 
	        		|| (user.getRestrictions().getDayOfWeek()!=null && !user.getRestrictions().getDayOfWeek().isEmpty())            
	        		|| (user.getRestrictions().getDisallowedIPs()!=null && !user.getRestrictions().getDisallowedIPs().isEmpty()) 
	        		|| (user.getRestrictions().getFromDate()!=null && !user.getRestrictions().getFromDate().isEmpty()) 
	        		|| (user.getRestrictions().getTimeZone()!=null && !user.getRestrictions().getTimeZone().isEmpty()) 
	        		|| (user.getRestrictions().getHours()!=null && !user.getRestrictions().getHours().isEmpty()) 
	        		|| (user.getRestrictions().getToDate()!=null && !user.getRestrictions().getToDate().isEmpty()) 
	        		) 
        		){
        			userDetailsService.verifyPermission("User.Restrictions");
        }
		return restClient.resource(RESOURCE_PATH)
				.build().post()
				.header("loggedInTenant", String.valueOf(userDetailsService.getCurrentUserDetailsRBAC().getSelectedTenantList().get(0)))
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(user)
				.retrieve().bodyToMono(User.class)
				.map(users -> ResponseEntity.ok(users));
    }

	@PostMapping(value = "/importFromCSV",consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_HTML_VALUE)
	public Mono<ResponseEntity<String>> importFromCSV(@RequestPart("files[]") InputStream uploadedInputStream,
													  @RequestParam("files[]") MultipartFile fileDetail, @QueryParam(value="currentTenantId") String currentTenantId, HttpServletResponse httpResponse) throws Exception {
		 userDetailsService.verifyPermission("User.Import");
//		 log.info("importFromCSV; Starting upload for user={}; fileName={};",
//					userDetailsService.getCurrentUserDetails().getUsername(), fileDetail.getOriginalFilename());
//		 try{
//			 if(importUtil.isValidateFileName()){
//				 importUtil.validateFileName(fileDetail.getFileName());
//			 }
//			 byte[] inputCSV = importUtil.checkContentSizeAndReturnBytes(uploadedInputStream, fileDetail.getFileName());
//
//			 log.info("importFromCSV; Validating upload for user={}; fileName={}; sizeInBytes={};",
//					userDetailsService.getCurrentUserDetails().getUsername(), fileDetail.getFileName(), inputCSV.length);
//			 importUtil.validateTypeOfData(inputCSV, fileDetail.getFileName());

		try {
			if (importUtil.isValidateFileName()) {
				importUtil.validateFileName(fileDetail.getOriginalFilename());
			}
			byte[] inputCSV = importUtil.checkContentSizeAndReturnBytes(uploadedInputStream, fileDetail.getOriginalFilename());

			log.info("importFromCSV; Validating upload for user={}; fileName={}; sizeInBytes={};",
			userDetailsService.getCurrentUserDetails().getUsername(), fileDetail.getOriginalFilename(), inputCSV.length);
			importUtil.validateTypeOfData(inputCSV, fileDetail.getOriginalFilename());

//			 ClientResponse clientResponse = restClient.resource(RESOURCE_PATH, "importFromCSV")
//					.queryParam("currentTenantId", currentTenantId)
//					.header("userId", userDetailsService.getCurrentUserDetails().getUserInfo().getUserId())
//					.header("fileName", fileDetail.getFileName())
//					.type(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class, new ByteArrayInputStream(inputCSV));
//			return Mapper.toResponse(clientResponse, httpResponse);

			Mono<String> responseBodyMono = restClient.resource(RESOURCE_PATH, "importFromCSV")
					.build().post()
					.uri(uriBuilder -> uriBuilder
							.queryParam("currentTenantId", currentTenantId).build())
					.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))					.header("fileName", fileDetail.getOriginalFilename())
					.accept(MediaType.APPLICATION_OCTET_STREAM)
					.bodyValue(new ByteArrayInputStream(inputCSV))
					.retrieve()
					.bodyToMono(String.class);

			return responseBodyMono.map(responseBody -> {
				if (HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(responseBody).getStatusCode())) {
					// If the status code is 200, return a ResponseEntity with the response body
					return ResponseEntity.ok(responseBody);
				} else {
					// If the status code is not 200, return a ResponseEntity with the appropriate status code
					return ResponseEntity.status(ResponseEntity.ok(responseBody).getStatusCode()).body(responseBody);
				}
			});
		} finally {
			if (uploadedInputStream != null) {
				try {
					uploadedInputStream.close();
				} catch (Exception e) {

				}
			}
		}
	}
	@GetMapping(value = "/getImportedCSV", produces = "application/vnd.ms-excel;charset=utf-8")
	public Mono<ResponseEntity> getImportedCSV(@QueryParam(value="pathId") String pathId) throws Exception {
		userDetailsService.verifyPermission("User.Import");

		Mono<ResponseEntity<byte[]>> clientResponse = restClient.resource(RESOURCE_PATH, "getImportedCSV")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("pathId", pathId).build())
				.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				.accept(MediaType.APPLICATION_OCTET_STREAM)
				.retrieve()
				.toEntity(byte[].class);


		return clientResponse.flatMap(responseEntity -> {
			if (HttpStatus.OK.isSameCodeAs(responseEntity.getStatusCode())) {
				byte[] bytes = responseEntity.getBody();
				InputStream inputStream = new ByteArrayInputStream(bytes);

				StreamingResponseBody stream = os -> {
					try {
						byte[] buffer = new byte[usersExportDataResponseWriteSizeBytes];
						int bytesRead;
						while ((bytesRead = inputStream.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}
						inputStream.close();
					} catch (Exception e) {
						log.error("getCustomDataExport; Exception={};", e);
					}
				};


				return Mono.just(ResponseEntity.ok()
						.header("Content-Disposition", responseEntity.getHeaders().getFirst("Content-Disposition"))
						.header("Set-Cookie", responseEntity.getHeaders().getFirst("Set-Cookie")).body(stream));
			} else {
				return Mono.just(ResponseEntity.status(responseEntity.getStatusCode())
						.body(clientResponse.block().toString()));
			}
		});
	}

	 //RBAC-1892 Start

		@GetMapping(value = "/ExportCSVData", produces = "application/vnd.ms-excel;charset=utf-8")
		public Mono<ResponseEntity> ExportCSVData(HttpServletRequest httpRequest) {
			MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
			userDetailsService.verifyPermission("User.Export");
			log.trace("ExportCSVData; params={};", params);
			String scopeQuery = userDetailsService.extractScopeForUserView();
			log.trace("list; scopeQuery={}", scopeQuery);
			params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));

			String fileName = "UserExport_" + userDetailsService.getCurrentUserDetails().getUserInfo().getUserName() + "_"
					+ new Date().getTime() + ".csv";
			Mono<ResponseEntity<byte[]>> clientResponse = restClient.resource(RESOURCE_PATH, "ExportCSVData")
					.build().get()
					.uri(uriBuilder -> uriBuilder
							.queryParams(params).build())
					.header("fileName", fileName)
					.header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
					.header("loggedInTenant", String.valueOf(userDetailsService.getCurrentUserDetailsRBAC().getSelectedTenantList().get(0)))
					.accept(MediaType.APPLICATION_OCTET_STREAM)
					.accept(MediaType.valueOf("application/vnd.ms-excel;charset=utf-8"))
					.retrieve()
					.toEntity(byte[].class);

			return clientResponse.flatMap(responseEntity -> {
				if (HttpStatus.OK.isSameCodeAs(responseEntity.getStatusCode())) {
					byte[] bytes = responseEntity.getBody();
					InputStream inputStream = new ByteArrayInputStream(bytes);

					StreamingResponseBody stream = os -> {
						try {
							byte[] buffer = new byte[usersExportDataResponseWriteSizeBytes];
							int bytesRead;
							while ((bytesRead = inputStream.read(buffer)) != -1) {
								os.write(buffer, 0, bytesRead);
							}
							inputStream.close();
						} catch (Exception e) {
							log.error("ExportCSVData; Exception={};", e);
						}
					};


					return Mono.just(ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + fileName)
                    .header("Set-Cookie", "fileDownload=true; path=/").body(stream));
				} else {
					return Mono.just(ResponseEntity.status(responseEntity.getStatusCode())
							.contentType(MediaType.TEXT_PLAIN)
							.body(responseEntity.toString()));
				}
			});
		}
		//RBAC-1892 End

	
	
	
	

	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<User>> update(@RequestBody User user, HttpServletRequest httpServletRequest) throws Exception {
        log.trace("update; user={}", user);
        userDetailsService.verifyPermission("User.Update");
        checkEntityPermission(user.getUserId(), "User.Update");
    	if(user.getChangePassword() != null && httpServletRequest.getSession() != null && httpServletRequest.getSession().getAttribute("keys") != null){
    		KeyPair keys = (KeyPair) httpServletRequest.getSession().getAttribute("keys");
			try {
				user.setChangePassword(new String(JCryptionUtil.decrypt(user.getChangePassword(), keys.getPrivate())));
				httpServletRequest.getSession().removeAttribute("keys");
			} catch (BadPaddingException e) {
				log.error("update; ip={}; userName={}; BadPaddingException={};", 
						RBACUtil.getRemoteAddress(httpServletRequest), user.getUserName(), e);
				ErrorInfoException errorInfo = new ErrorInfoException(ERROR_ENCRYPTION_CODE, ERROR_ENCRYPTION_CODE);
				throw errorInfo;
			}
    	}
    	
        if(user.getChangePassword()!=null && !user.getChangePassword().isEmpty()){
        	userDetailsService.verifyPermission("User.CreatePassword");
        }
        if(user.getGeneratePasswordFlag()!=null && user.getGeneratePasswordFlag().equals(Boolean.TRUE)){
        	userDetailsService.verifyPermission("User.GeneratePassword");
        }
        if(user.getUserId().equals(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId())){
        	userDetailsService.verifyPermission("User.SelfUpdate");
        }
        Mono<User> existingUser = restClient.resource(RESOURCE_PATH, Integer.toString(user.getUserId()))
				.build().get()
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(User.class);
		existingUser.map(user1 -> {
			if( (user.getGroupId()!=null && !user.getGroupId().equals(user1.getGroupId()))
					|| (user1.getGroupId()!=null && !user1.getGroupId().equals(user.getGroupId()))){
				userDetailsService.verifyPermission("User.AssignGroup");
			}
			if(!user1.getIsEnabled().equals(user.getIsEnabled()) || !user1.getIsLocked().equals(user.getIsLocked())
					|| !user1.getChangePasswordFlag().equals(user.getChangePasswordFlag())
			){
				userDetailsService.verifyPermission("User.StatusManagement");
			}
            return user1;
        });

//        return sSORestClient.resource(RESOURCE_PATH)
//                .header("userId", userDetailsService.getCurrentUserDetails().getUserInfo().getUserId())
//                .header("clientIp", RBACUtil.getRemoteAddress(httpServletRequest))
//                .header("loggedInTenant", userDetailsService.getCurrentUserDetailsRBAC().getSelectedTenantList().get(0))// Added bY fazia to get logged in tenant Id for maker checker
//                .accept(MediaType.APPLICATION_JSON)
//                .entity(user, MediaType.APPLICATION_JSON)
//                .put(User.class);

		        return sSORestClient.resource(RESOURCE_PATH).build().put()
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
						.header("loggedInTenant", String.valueOf(userDetailsService.getCurrentUserDetailsRBAC().getSelectedTenantList().get(0)))// Added bY fazia to get logged in tenant Id for maker checker
                .header("clientIp", RBACUtil.getRemoteAddress(httpServletRequest))
                .accept(MediaType.APPLICATION_JSON)
						.bodyValue(user).retrieve().bodyToMono(User.class)
						.map(user1 -> ResponseEntity.ok(user1));
    }

	@PostMapping(value = "/{userId}/setPassword", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> setPassword(@PathVariable("userId") int userId,@RequestBody String password) throws Exception {
        log.trace("setPassword; userId={}; password=*****", userId);
        userDetailsService.verifyPermission("User.Create");
        return restClient.resource(RESOURCE_PATH, Integer.toString(userId), "setPassword")
				.build().post()
                .accept(MediaType.APPLICATION_JSON)
				.bodyValue(password)
				.retrieve().bodyToMono(String.class)
				.map(user2->ResponseEntity.ok(user2));
    }

	@PostMapping(value = "/changePassword", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> changePassword(@RequestBody String request) throws Exception {
        log.trace("changePassword; request={}", request);
        userDetailsService.verifyPermission("User.Create");
        return restClient.resource(RESOURCE_PATH, "changePassword")
				.build().post()
                .accept(MediaType.APPLICATION_JSON)
				.bodyValue(request).retrieve()
				.bodyToMono(String.class)
				.map(users->ResponseEntity.ok(users));
    }
    

	@GetMapping(value = "/encryptionKeys", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> getEncryptionKeys(HttpServletRequest request)  throws Exception{
		Map<String, String> keyPair = new HashMap<String, String>();
		KeyPair keys = null;
		keys = JCryptionUtil.generateKeyPair();
		request.getSession().setAttribute("keys", keys);
		byte[] pbk = keys.getPublic().getEncoded();
		String publicKeyStr = new String(Base64.encode(pbk));
		keyPair.put("key", publicKeyStr);
		return keyPair;
    }
	@GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<User>> getById(@PathVariable("userId") int userId) {
        log.trace("getById; userId={}", userId);
        userDetailsService.verifyPermission("User.View");
        checkEntityPermission(userId, "User.View");
        return restClient.resource(RESOURCE_PATH, Integer.toString(userId))
				.build().get()
                .accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(User.class)
				.map(users->ResponseEntity.ok(users));
    }

	@DeleteMapping("/{userId}")
    public Mono<ResponseEntity<User>> deleteById(@PathVariable("userId") int userId, HttpServletRequest httpRequest) {
    	 log.trace("deleteById; userId={}", userId);
         userDetailsService.verifyPermission("User.Delete");
         checkEntityPermission(userId, "User.Delete");
         String clientIp = RBACUtil.getRemoteAddress(httpRequest);
       return restClient.resource(RESOURCE_PATH, Integer.toString(userId))
				.build().delete()
			   .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))
				  .header("loggedInTenant", String.valueOf(userDetailsService.getCurrentUserDetailsRBAC().getSelectedTenantList().get(0)))// Added bY fazia to get logged in tenant Id for maker checker
                 .header("clientIp", clientIp)
				.retrieve().bodyToMono(User.class).map(users->ResponseEntity.ok(users));
    }

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<User[]>> list(HttpServletRequest servletRequest) {
        log.trace("list; requestUri={}", servletRequest.getRequestURI());
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        userDetailsService.verifyPermission("User.View");
        String scopeQuery = userDetailsService.extractScopeForUserView();
        log.trace("list; scopeQuery={}", scopeQuery);
        uriInfo.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH)
				.build().get()
				.uri(uriBuilder -> uriBuilder.queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(User[].class)
				.map(users->ResponseEntity.ok(users));
    }

	@GetMapping(value="/count",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Integer>> count(HttpServletRequest httpRequest) {
        MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
        log.trace("count; requestUri={}", params);
   	 	
        userDetailsService.verifyPermission("User.View");
        String scopeQuery = userDetailsService.extractScopeForUserView();
        log.trace("count; scope={}", scopeQuery);
        params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
       return restClient.resource(RESOURCE_PATH, "count")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Integer.class)
				.map(integer -> ResponseEntity.ok(integer));
    }

	@GetMapping(value = "/validationRules", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getValidationRules() {
        userDetailsService.verifyPermission("User.View");
		return restClient.resource(RESOURCE_PATH)
				.build().get().uri("/validationRules")
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(String.class).map(response -> ResponseEntity.ok(response));
    }

	@GetMapping(value = "/usersNotAssignToGroup", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<User[]>>  usersNotAssignToGroup(HttpServletRequest servletRequest) {
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        userDetailsService.verifyPermission("User.View");
        String scopeQuery = userDetailsService.extractScopeForUserView();
        log.trace("usersNotAssignToGroup; scopeQuery={}", scopeQuery);
        uriInfo.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
        return restClient.resource(RESOURCE_PATH,"usersNotAssignToGroup")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(User[].class).map(uses->ResponseEntity.ok(uses));
    }

	@PostMapping(value = "/getUsersOfAnotherGroup", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<User[]>> getUsersOfAnotherGroup(HttpServletRequest httpRequest) {
    	 MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
    	 userDetailsService.verifyPermission("User.View");
    	 String scopeQuery = userDetailsService.extractScopeForUserView();
         log.trace("getUsersOfAnotherGroup; scopeQuery={}", scopeQuery);
         params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH,"getUsersOfAnotherGroup")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(User[].class).map(uses->ResponseEntity.ok(uses));
    }

	@GetMapping(value = "/userIdNames", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getUserIdNames(HttpServletRequest servletRequest) {

		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

		userDetailsService.verifyPermission("User.View");
    	 String scopeQuery = userDetailsService.extractScopeForUserView();
         log.trace("getUserIdNames; scopeQuery={}", scopeQuery);
         uriInfo.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "userIdNames")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(String.class)
				.map(string -> ResponseEntity.ok(string));
    }

	@GetMapping(value = "/auditUserIdNames", produces = MediaType.APPLICATION_JSON_VALUE)
	public  Mono<ResponseEntity<String>> getAuditUserIdNames(HttpServletRequest servletRequest) {

		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

		userDetailsService.verifyPermission("User.View");
		String scopeQuery = userDetailsService.extractScopeForUserView();
		log.trace("getUserIdNames; scopeQuery={}", scopeQuery);
		uriInfo.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "auditUserIdNames")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(String.class)
				.map(string -> ResponseEntity.ok(string));
	}

	@PostMapping(value = "/checkTenantIdInOrgAndGroup", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> checkTenantIdInOrgAndGroup(HttpServletRequest httpRequest) {
    	 MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
    	 log.trace("checkTenantIdInOrgAndGroup; uriInfo.getQueryParameters()={}", params);

		return restClient.resource(RESOURCE_PATH, "checkTenantIdInOrgAndGroup")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(String.class)
				.map(string -> ResponseEntity.ok(string));
    }


	@PostMapping(value = "/customUserInfo", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getCustomUserInfo(HttpServletRequest httpRequest) {
    	 MultiValueMap<String, String> params = WebParamsUtil.paramsToMap(httpRequest);
    	 userDetailsService.verifyPermission("User.View");
    	 String scopeQuery = userDetailsService.extractScopeForUserView();
         log.trace("getCustomUserInfo; scopeQuery={}", scopeQuery);
         params.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		return restClient.resource(RESOURCE_PATH, "customUserInfo")
				.build().get().uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(String.class)
				.map(string -> ResponseEntity.ok(string));
    }
    
    private void checkEntityPermission(Integer userId, String permission) {
		MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
		String scopeQuery = userDetailsService.extractScopeForUserView();
		queryMap.add(RBACUtil.USER_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scopeQuery));
		queryMap.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, Integer.toString(userId));
		Mono<Boolean> permissionCheckMono = restClient.resource(RESOURCE_PATH, "checkEntityPermission")
				.build()
				.get()
				.uri(uriBuilder -> uriBuilder.queryParams(queryMap).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Boolean.class).flatMap(permissionResult -> {
					if (Boolean.TRUE.equals(permissionResult)) {
					}
					return Mono.empty();
				});
				ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.ACCESS_DENIED);
				errorInfo.add("permission", permission);
				errorInfo.add("entity", "User");
				errorInfo.add(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, userId.toString());
				new ForbiddenException(errorInfo.getErrorCode());
	}

	@PostMapping(value = "/isUserAssociatedinDispatchContact", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Boolean>> isUserAssociatedinDispatchContact(HttpServletRequest httpRequest) {
		MultiValueMap params = WebParamsUtil.paramsToMap(httpRequest);
		return restClient
				.resource(RESOURCE_PATH, "isUserAssociatedinDispatchContact")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Boolean.class).map(ResponseEntity::ok);
	}

	@GetMapping(value = "/loginTypes", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<LoginType[]>> loginTypes(HttpServletRequest servletRequest) {
		Map<String, String[]> parameterMap = servletRequest.getParameterMap();
		MultiValueMap<String, String> uriInfo = new LinkedMultiValueMap<>();
		parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

		return restClient.resource(RESOURCE_PATH,"loginTypes")
				.build().get()
				.uri(uriBuilder -> uriBuilder
						.queryParams(uriInfo).build())
                .accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(LoginType[].class)
				.map(ResponseEntity::ok);
    }
    
    //RBAC-1562 Starts
	@GetMapping(value = "/isTwoFactorActiveForUser/{tenantId}",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Boolean>>  isTwoFactorActiveForUser(@PathVariable("tenantId") Long tenantId) {
		return restClient.resource(RESOURCE_PATH, "isTwoFactorActiveForUser",Long.toString(tenantId))
				.build().get()
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Boolean.class).map(bool->ResponseEntity.ok(bool));
    }
    //RBAC-1562 Ends

	@GetMapping(value = "/isAzureUserMgmtEnabled",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Boolean>> isAzureUserMgmtEnabled() {
		return restClient.resource(RESOURCE_PATH, "isAzureUserMgmtEnabled")
				.build().get()
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Boolean.class).map(bool -> ResponseEntity.ok(bool));
	}

	@GetMapping(value = "/isAssertPasswordsEnabled",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Boolean>> isAssertPasswordsEnabled() {
		return restClient.resource(RESOURCE_PATH, "isAssertPasswordsEnabled")
				.build().get()
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Boolean.class).map(bool -> ResponseEntity.ok(bool));
	}
}
