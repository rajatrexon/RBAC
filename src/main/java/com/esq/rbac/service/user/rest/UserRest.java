package com.esq.rbac.service.user.rest;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.group.service.GroupDal;
import com.esq.rbac.service.loginlog.domain.LoginLog;
import com.esq.rbac.service.loginlog.domain.LoginType;
import com.esq.rbac.service.loginlog.service.LoginLogService;
import com.esq.rbac.service.loginservice.email.EmailDal;
import com.esq.rbac.service.loginservice.embedded.ChangePasswordPolicy;
import com.esq.rbac.service.loginservice.embedded.ChangePasswordRequest;
import com.esq.rbac.service.loginservice.embedded.LoginResponse;
import com.esq.rbac.service.loginservice.service.LoginService;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.makerchecker.service.MakerCheckerDal;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.role.service.RoleDal;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.user.userimport.service.UserImportService;
import com.esq.rbac.service.user.vo.UserDTO;
import com.esq.rbac.service.user.vo.UserWithLogoutData;
import com.esq.rbac.service.user.vo.UserWithTenantId;
import com.esq.rbac.service.userhistory.domain.UserHistory;
import com.esq.rbac.service.userhistory.service.UserHistoryService;
import com.esq.rbac.service.util.*;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.commons.FieldRules;
import com.esq.rbac.service.validation.annotation.ValidationRules;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserRest {

    public static final String USER_ERROR = "userDetailsError";
    public static final String DETAILS_INFO = "errorDetails";
    private Validator validator;
    private UserDal userDal;
    private AuditLogger auditLogger;
    private LoginLogService loginLogDal;
    private ImportUtil importUtil;
    private UserImportService userImportService;
    private LoginService loginService;
    private MakerCheckerDal makerCheckerDal;
    private GroupDal groupDal;
    private OrganizationMaintenanceDal organizationMaintenanceDal;
    private DeploymentUtil deploymentUtil;
    private RoleDal roleDal;
    private UserHistoryService userHistoryDal;

    @Autowired
    public void setUserHistoryDal(UserHistoryService userHistoryDal) {
        this.userHistoryDal = userHistoryDal;
    }

    @Autowired
    public void setRoleDal(RoleDal roleDal) {
        this.roleDal = roleDal;
    }


    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        log.trace("setDeploymentUtil; {};", deploymentUtil);
        log.info("========= DeployementUtil Parameter Values =======");
        log.info("{}", deploymentUtil.stringify());
        this.deploymentUtil = deploymentUtil;
    }

    @Autowired
    public void setGroupDal(GroupDal groupDal) {
        log.trace("setGroupDal; {}", groupDal);
        this.groupDal = groupDal;
    }


    @Autowired
    public void setOrganizationMaintenanceDal(OrganizationMaintenanceDal organizationMaintenanceDal) {
        log.trace("setOrganizationMaintenanceDal; {}", organizationMaintenanceDal);
        this.organizationMaintenanceDal = organizationMaintenanceDal;
    }


    @Autowired
    public void setLoginService(LoginService loginService) {
        log.trace("setLoginService; {}", loginService);
        this.loginService = loginService;
    }

    @Autowired
    public void setMakerCheckerDal(MakerCheckerDal makerCheckerDal) {
        this.makerCheckerDal = makerCheckerDal;
    }

    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }


    @Autowired
    public void setLoginLogDal(LoginLogService loginLogDal) {
        log.trace("setLoginLogDal; {}", loginLogDal);
        this.loginLogDal = loginLogDal;
    }

    @Autowired
    public void setUserDal(UserDal userDal, AuditLogService auditLogDal, EmailDal emailDal) {
        log.trace("setUserDal");
        this.userDal = userDal;
        this.auditLogger = new AuditLogger(auditLogDal);

    }


    @EventListener
    public void fillUserLookupTable(ApplicationStartedEvent event) {
        log.trace("fillUserLookupTable");
        Lookup.fillUserLookupTable(userDal.getUserIdNames(null));
    }

    @Autowired
    public void setImportUtil(ImportUtil importUtil) {
        log.trace("setImportUtil; {};", importUtil);
        this.importUtil = importUtil;
    }

    @Autowired
    public void setUserImportService(UserImportService userImportService) {
        log.trace("setUserImportService userImportService={};", userImportService);
        this.userImportService = userImportService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "loggedInTenant", description = "loggedInTenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<User> create(@RequestHeader org.springframework.http.HttpHeaders headers, @RequestBody User user) throws Exception {
        log.trace("create; user={}", user);
        validate(user);
        if (deploymentUtil.isAssertPasswords()) {
            if (user.getChangePassword() == null) {
                throw new ErrorInfoException("mustRequirePassword");
            }
        }
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        /** Added By Fazia to check maker checker enabled for tenant **/
        List<String> loggedInTenant = headers.get("loggedInTenant");

        boolean checkGroupId = false;
        boolean checkOrganizationId = false;

        if (user.getOrganizationId() != null) {
            List<Map<String, Object>> orgList = getOrganizationByTenantId(Long.parseLong(loggedInTenant.get(0)));
            for (Map<String, Object> map : orgList) {
                if (map.get("organizationId").equals(user.getOrganizationId())) {
                    checkOrganizationId = true;
                    break;
                }
            }
            if (!checkOrganizationId) {
                log.error("Error while creating user:" + user.getUserName() + " as the orgId:" + user.getOrganizationId() + " does not belong to the tenant used");
                StringBuilder sb = new StringBuilder();
                sb.append(USER_ERROR).append("; ");
                sb.append(DETAILS_INFO).append("=").append("Organization " + Lookup.getOrganizationNameById(user.getOrganizationId()));
                log.info("create; {}", sb);
                ErrorInfoException errorInfo = new ErrorInfoException(USER_ERROR, sb.toString());
                errorInfo.getParameters().put(DETAILS_INFO, "Organization " + Lookup.getOrganizationNameById(user.getOrganizationId()));
                throw errorInfo;
            }
        }

        if (user.getGroupId() != null && user.getGroupId() > 0) {
            List<Map<String, Object>> secGrplist = getSecurityGroupByTenantId(Long.parseLong(loggedInTenant.get(0)));
            for (Map<String, Object> map : secGrplist) {

                if (map.get("groupId").equals(user.getGroupId())) {
                    checkGroupId = true;
                    break;
                }

            }
            if (!checkGroupId) {
                log.error("Error while cretaing user:" + user.getUserName() + " as groupid:" + user.getGroupId() + " does not belong to the tenant used");
                StringBuilder sb = new StringBuilder();
                sb.append(USER_ERROR).append("; ");
                sb.append(DETAILS_INFO).append("=").append("Group " + Lookup.getGroupName(user.getGroupId()));
                log.info("create; {}", sb);
                ErrorInfoException errorInfo = new ErrorInfoException(USER_ERROR, sb.toString());
                errorInfo.getParameters().put(DETAILS_INFO, "Group " + Lookup.getGroupName(user.getGroupId()));
                throw errorInfo;
            }
        }

        user.setLoggedInTenantId(Long.parseLong(loggedInTenant.get(0)));
        /** End **/
        User retUser = hidePasswordDetails(userDal.create(user, userId, "User", "Create"));
        retUser = hideIVRPinDetails(retUser);
        if (retUser.getIdentities() != null && !retUser.getIdentities().isEmpty()) {
            userDal.evictSecondLevelCacheById(retUser.getUserId());
        }
        Lookup.updateUserLookupTable(retUser);
        //auditLogger.logCreate(userId, user.getUserName(), "User", "Create", userDal.getObjectChangeSet());
        return ResponseEntity.ok(retUser);
    }

    private List<Map<String, Object>> getSecurityGroupByTenantId(Long tenantId) {

        log.trace("getSecurityGroupByTenantId; tenantId={}", tenantId);
        try {
            MultivaluedMap<String, String> tenantIdMap = new MultivaluedHashMap<>();
            tenantIdMap.putSingle("groupScopeQuery", "(g.tenantId in (" + tenantId + ") )");
            OptionPage optionPage = new OptionPage(tenantIdMap, 0, Integer.MAX_VALUE);
            OptionSort optionSort = new OptionSort(tenantIdMap);
            OptionFilter optionFilter = new OptionFilter(tenantIdMap);
            Options options = new Options(optionPage, optionSort, optionFilter);

            return groupDal.getGroupIdNames(options);

        } catch (Exception e) {

            throw new ErrorInfoException(e.getMessage());
        }
    }

    private List<Map<String, Object>> getOrganizationByTenantId(Long tenantId) {

        log.trace("getOrganizationByTenantId; tenantId={}", tenantId);
        try {
            MultivaluedMap<String, String> tenantIdMap = new MultivaluedHashMap<>();
            tenantIdMap.putSingle("organizationScopeQuery", "(org.tenantId in (" + tenantId + ") )");
            OptionPage optionPage = new OptionPage(tenantIdMap, 0, Integer.MAX_VALUE);
            OptionSort optionSort = new OptionSort(tenantIdMap);
            OptionFilter optionFilter = new OptionFilter(tenantIdMap);
            Options options = new Options(optionPage, optionSort, optionFilter);

            return organizationMaintenanceDal.getOrganizationByTenantId(options);

        } catch (Exception e) {

            throw new ErrorInfoException(e.getMessage());
        }
    }

    @PostMapping(value = "/importFromCSV", consumes = MediaType.APPLICATION_OCTET_STREAM, produces = MediaType.TEXT_HTML)
    @Parameters({@Parameter(name = "fileName", description = "fileName", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<String> importFromCSV(@RequestHeader org.springframework.http.HttpHeaders headers, @Context HttpServletRequest httpRequest, @RequestParam(value = "currentTenantId") Long currentTenantId) throws Exception {
        String fileName = headers.get("fileName").get(0);
        if (importUtil.isValidateFileName()) {
            importUtil.validateFileName(fileName);
        }
        byte[] inputCSV = importUtil.checkContentSizeAndReturnBytes(httpRequest.getInputStream(), fileName);

        Integer loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
        String loggedInUserName = Lookup.getUserName(loggedInUserId);

        log.info("importFromCSV; Starting upload for user={}; fileName={}; sizeInBytes={};", loggedInUserName, fileName, inputCSV.length);
        importUtil.validateTypeOfData(inputCSV, fileName);
        String fileNamePrefix = new Date().getTime() + "-" + RandomStringUtils.random(8, true, true) + "-" + loggedInUserName;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));

        String logCSVPath = importUtil.getImportCSVLogFolder() != null ? importUtil.getImportCSVLogFolder() : System.getProperty(RBACUtil.RBAC_CONF_DIR).concat("/../../" + importUtil.getDefaultCSVStorageFolder() + "\\");

        // if files are stored on disk and path is incorrect, throw an exception
        if (importUtil.isStoreImportedCSVFiles()) {
            File fd = new File(logCSVPath);
            log.info("importFromCSV; csv import storage path is: {}", logCSVPath);
            if (!fd.exists()) {
                if (fd.isAbsolute()) {
                    fd.mkdirs();
                } else {
                    log.error("importFromCSV; deploymentUtil.importCSVLogFolder property in init.properties file is incorrect");
                    throw new ErrorInfoException("CSVImportLogFolderIncorrect");
                }
            }
            if (!fd.isDirectory()) {
                log.error("importFromCSV; Please modify the deploymentUtil.importCSVLogFolder property in init.properties file ");
                throw new ErrorInfoException("CSVImportLogFolderIncorrect");
            }
        }

        userImportService.importFromCSV(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(inputCSV))), bufferedWriter, loggedInUserId, currentTenantId);

        byte[] output = byteArrayOutputStream.toByteArray();
        String outputFileName = fileNamePrefix + "-output.csv";
        String logFileName = fileNamePrefix + "-output.txt";
        FileOutputStream fos = new FileOutputStream(logCSVPath + outputFileName);
        fos.write(output);
        fos.close();
        //RBAC-1308
        FileOutputStream fosLog = new FileOutputStream(logCSVPath + logFileName);
        fosLog.write(("Original File Name: " + fileName).getBytes());
        fosLog.close();
        return ResponseEntity.ok().body(importUtil.encryptFileName(outputFileName));
    }

    @GetMapping(value = "/getImportedCSV", produces = "application/vnd.ms-excel;charset=utf-8")
    public ResponseEntity<StreamingResponseBody> getImportedCSV(@RequestParam(value = "pathId") String pathId) throws IOException {
        String fileName = null;
        try {
            fileName = importUtil.decryptFileName(pathId);
        } catch (Exception e) {
            log.error("getImportedCSV; Exception={};", e);
            throw new ErrorInfoException("fileNameNotFound", "File name not found");
        }
        String logCSVPath = importUtil.getImportCSVLogFolder() != null ? importUtil.getImportCSVLogFolder() : System.getProperty(RBACUtil.RBAC_CONF_DIR).concat("/../../" + importUtil.getDefaultCSVStorageFolder() + "\\");
        final String completeFilePath = logCSVPath + fileName;
        StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File(completeFilePath));
                } catch (IOException e) {
                    log.error("getImportedCSV; Exception={};", e);
                    throw new ErrorInfoException("fileNotFound", "File not found");
                }
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    fis.close();
                } catch (IOException e) {
                    log.error("getImportedCSV; Exception={};", e);
                    throw new ErrorInfoException("errorWhileReadingWriting", "Error while reading file");
                }
                if (!importUtil.isStoreImportedCSVFiles()) {
                    try {
                        FileUtils.forceDelete(new File(completeFilePath));
                    } catch (IOException e) {
                        log.info("getImportedCSV; Exception={};", e);
                    }
                }
            }
        };
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + fileName).header("Set-Cookie", "csvImport=true; path=/").body(stream);
    }

    //RBAC-1892
    @GetMapping(value = "/ExportCSVData", produces = "application/vnd.ms-excel;charset=utf-8")
    @Parameters({@Parameter(name = "fileName", description = "fileName", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "firstName", description = "firstName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationId", description = "organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationName", description = "organizationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isShared", description = "isShared", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "variableName", description = "variableName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userIdList", description = "Enter User Id by comma separated...", schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true),})
    public ResponseEntity<byte[]> exportUserCSV(@RequestHeader org.springframework.http.HttpHeaders headers, HttpServletRequest request) throws Exception {

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach(uriInfo::addAll);

        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionFilter);
        log.trace("getCustomDataCSV; params={}", options);
        String fileName = headers.get("fileName").get(0);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
        List<User> userList = userDal.getList(options);
        Map<Integer, Set<String>> getRoleListByGroup = roleDal.getRolesByGroup();
        userImportService.exportToCSV(userList, getRoleListByGroup, bufferedWriter);
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) {
                try {
                    byteArrayOutputStream.writeTo(os);
                } catch (IOException e) {
                    log.error("exportUserCSV; Exception={};", e);
                    throw new ErrorInfoException("errorWhileReadingWriting", "Error while writing file");
                }
            }
        };

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + fileName).header("Set-Cookie", "csvExport=true; path=/").body(byteArray);
    }

    //RBAC-1892 End
    @PutMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "loggedInTenant", description = "loggedInTenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<UserWithLogoutData> update(@RequestHeader org.springframework.http.HttpHeaders headers, @RequestBody User user) throws Exception {
        log.trace("update; user={}", user);
        validate(user);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        /** Added By Fazia to check maker checker enabled for tenant **/
        //List<String> loggedInTenant = headers.getRequestHeader("loggedInTenant");
        //user.setLoggedInTenantId( Long.parseLong(loggedInTenant.get(0)));
        /** End **/

        List<String> loggedInTenant = headers.get("loggedInTenant");
        boolean checkGroupId = false;
        boolean checkOrganizationId = false;

        if (user.getOrganizationId() != null) {
            List<Map<String, Object>> orgList = getOrganizationByTenantId(Long.parseLong(loggedInTenant.get(0)));
            for (Map<String, Object> map : orgList) {
                if (map.get("organizationId").equals(user.getOrganizationId())) {
                    checkOrganizationId = true;
                    break;
                }
            }
            if (!checkOrganizationId) {
                log.error("Error while updating user:" + user.getUserName() + " as the orgId:" + user.getOrganizationId() + " does not belong to the tenant used");
                StringBuilder sb = new StringBuilder();
                sb.append(USER_ERROR).append("; ");
                sb.append(DETAILS_INFO).append("=").append("Organization " + Lookup.getOrganizationNameById(user.getOrganizationId()));
                log.info("update; {}", sb);
                ErrorInfoException errorInfo = new ErrorInfoException(USER_ERROR, sb.toString());
                errorInfo.getParameters().put(DETAILS_INFO, "Organization " + Lookup.getOrganizationNameById(user.getOrganizationId()));
                throw errorInfo;
            }
        }

        if (user.getGroupId() != null && user.getGroupId() > 0) {
            List<Map<String, Object>> secGrplist = getSecurityGroupByTenantId(Long.parseLong(loggedInTenant.get(0)));
            for (Map<String, Object> map : secGrplist) {

                if (map.get("groupId").equals(user.getGroupId())) {
                    checkGroupId = true;
                    break;
                }

            }
            if (!checkGroupId) {
                log.error("Error while Updating user:" + user.getUserName() + " as groupid:" + user.getGroupId() + " does not belong to the tenant used");
                StringBuilder sb = new StringBuilder();
                sb.append(USER_ERROR).append("; ");
                sb.append(DETAILS_INFO).append("=").append("Group " + Lookup.getGroupName(user.getGroupId()));
                log.info("update; {}", sb);
                ErrorInfoException errorInfo = new ErrorInfoException(USER_ERROR, sb.toString());
                errorInfo.getParameters().put(DETAILS_INFO, "Group " + Lookup.getGroupName(user.getGroupId()));
                throw errorInfo;
            }
        }
        UserWithLogoutData userWithLogoutData = userDal.update(user, userId, headers.get("clientIp").get(0));
        hidePasswordDetails(userWithLogoutData.getUser());
        hideIVRPinDetails(userWithLogoutData.getUser());
        // Lookup.fillUserLookupTable(userDal.getUserIdNames(null));
        Lookup.updateUserLookupTable(userWithLogoutData.getUser());

        return ResponseEntity.ok(userWithLogoutData);
    }


    @PostMapping(value = "/{userId}/setPassword", consumes = MediaType.TEXT_PLAIN)
    public void setPassword(@PathVariable("userId") int userId, String password) throws Exception {
        log.trace("setPassword; userId={}; password=*****", userId);
        userDal.setPassword(userId, password);
    }

    @PostMapping(value = "/changePassword", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public void changePassword(@RequestBody ChangePasswordRequest request) throws Exception {
        log.trace("changePassword; request={}", request);
        try {

            /*// RBAC-1465 check if the user has changed the password no of time from configuration*/
            ChangePasswordPolicy changePolicy = loginService.checkChangePasswordPolicy(request.getUserName());
            if (changePolicy != null && changePolicy.getIsPolicyViolated()) {
                //System.out.print("**********"+isChangePasswordNotExceedNoOfTimesInLast24Hrs+"*******************");
                loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_CHANGE_PASSWORD_RBAC, true, request.getClientIP(), request.getService(), LoginResponse.CHANGE_PASSWORD_POLICY_VIOLATED, request.getSessionHash(), request.getAppKey()));
                StringBuilder sb = new StringBuilder();
                sb.append(LoginResponse.CHANGE_PASSWORD_POLICY_VIOLATED).append("; ");
                log.info("changePassword; {}", sb);
                log.info("changePolicy; {}", changePolicy);
                ErrorInfoException e = new ErrorInfoException(LoginResponse.CHANGE_PASSWORD_POLICY_VIOLATED, sb.toString());
                e.getParameters().put("changePolicy", changePolicy.toString());
                throw e;
            }


            userDal.changePassword(request.getUserName(), request.getOldPassword(), request.getNewPassword());
            loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_CHANGE_PASSWORD_RBAC, true, request.getClientIP(), request.getService(), LoginResponse.PASSWORD_CHANGE_SUCCESSFULL, request.getSessionHash(), request.getAppKey()));
        } catch (Exception e) {
            if (e instanceof ErrorInfoException) {
                loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_CHANGE_PASSWORD_RBAC, false, request.getClientIP(), request.getService(), ((ErrorInfoException) e).getCode(), request.getSessionHash(), request.getAppKey()));
            } else {
                loginLogDal.create(LoginLog.createLoginLog(request.getUserName(), LoginLog.LOG_TYPE_CHANGE_PASSWORD_RBAC, false, request.getClientIP(), request.getService(), e.toString(), request.getSessionHash(), request.getAppKey()));
            }
            throw e;
        }
    }

    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<User> getById(@PathVariable("userId") int userId) {
        log.trace("getById; userId={}", userId);
        return ResponseEntity.ok(hidePasswordDetails(hideIVRPinDetails(userDal.getById(userId))));
    }

    @DeleteMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "loggedInTenant", description = "loggedInTenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<UserWithLogoutData> deleteById(@RequestHeader org.springframework.http.HttpHeaders headers, @PathVariable("userId") int userId) throws IOException {
        log.trace("deleteById; userId={}", userId);
        String userName = userDal.getById(userId).getUserName();
        String clientIp = headers.get("clientIp").get(0);
        Integer loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
        /** Added By Fazia to check maker checker enabled for tenant **/
        List<String> loggedInTenant = headers.get("loggedInTenant");
        /** End **/
        /** RBAC-2730 */
        UserWithLogoutData userWithLogoutData = userDal.softDeleteById(userId, loggedInUserId, clientIp, 0, Long.parseLong(loggedInTenant.get(0)));
        User user = userWithLogoutData.getUser();
        UserHistory userHistory = new UserHistory();
        userHistory.setUserId(String.valueOf(user.getUserId()));
        userHistory.setUserName(userName);
        userHistory.setIsStatus(0);
        userHistory.setGroupId(user.getGroupId() != null && user.getGroupId() > -1 ? user.getGroupId() : null);
        userHistory.setCreatedOn(DateTime.now().toDate());
        userHistory.setCreatedBy(loggedInUserId);
        UserHistory userHistoryData = userHistoryDal.create(userHistory);
        if (user != null && user.getMakerCheckerId() == null) // // RBAC-1985
            auditLogger.logCreate(loggedInUserId, userName, "User", "Delete");
        Lookup.deleteFromUserLookupTable(userId); // RBAC-2730
        return ResponseEntity.ok(userWithLogoutData);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "firstName", description = "firstName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "emailAddress", description = "emailAddress", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationId", description = "organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationName", description = "organizationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isShared", description = "isShared", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "variableName", description = "variableName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userIdList", description = "Enter User Id by comma separated...", schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true)})
    public ResponseEntity<User[]> list(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        log.trace("list; requestUri={}", request.getRequestURI());
        System.out.println("inside the get all user api...");


        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);

        Options options = new Options(optionPage, optionSort, optionFilter);
        List<User> list = new ArrayList<User>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = userDal.searchList(options);
        } else {
            list = userDal.getList(options);
        }
        for (User user : list) {
            hidePasswordDetails(user);
            hideIVRPinDetails(user);
        }
        User[] array = new User[list.size()];
        list.toArray(array);

//        return Response.ok().entity(array).expires(new Date()).build();
        return ResponseEntity.ok().body(array);
    }

    private List<User> prepareSharedUsers(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();
        OptionPage optionPage = new OptionPage(multivaluedMap, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(multivaluedMap);
        OptionFilter optionFilter = new OptionFilter(multivaluedMap);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<User> list = new LinkedList<User>();
        String tenantIdFilter = null;
        if (optionFilter.getFilter("tenantId") != null && !optionFilter.getFilter("tenantId").isEmpty()) {
            tenantIdFilter = optionFilter.getFilter("tenantId");
            if (multivaluedMap.containsKey(SearchUtils.SEARCH_PARAM)) {
                list = userDal.searchList(options);
            } else {
                list = userDal.getList(options);
            }
        }

        String hostIncluded = optionFilter.getFilter("host");
        if (hostIncluded != null && !hostIncluded.isEmpty() && hostIncluded.equalsIgnoreCase("true")) {
            Tenant hostTenant = Lookup.getHostTenant();
            String hostTenantId = hostTenant != null ? hostTenant.getTenantId().toString() : "100";
            if (tenantIdFilter == null || !(tenantIdFilter.equalsIgnoreCase(hostTenantId))) {
                MultivaluedMap<String, String> queryMap = new MultivaluedHashMap<>();
                queryMap.putSingle("tenantId", hostTenantId);
                queryMap.putSingle("isShared", "true");
                optionFilter = new OptionFilter(queryMap);
                options = new Options(optionPage, optionSort, optionFilter);
                list.addAll(userDal.getList(options));
            }
        }
        return list;
    }

    private List<UserDTO> prepareSharedUsersMinimal(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> multivaluedHashMap.addAll(key, values));

        OptionPage optionPage = new OptionPage(multivaluedHashMap, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(multivaluedHashMap);
        OptionFilter optionFilter = new OptionFilter(multivaluedHashMap);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<User> list = new LinkedList<User>();
        String tenantIdFilter = null;
        if (optionFilter.getFilter("tenantId") != null && !optionFilter.getFilter("tenantId").isEmpty()) {
            tenantIdFilter = optionFilter.getFilter("tenantId");
            if (multivaluedHashMap.containsKey(SearchUtils.SEARCH_PARAM)) {
                list = userDal.searchList(options);
            } else {
                list = userDal.getList(options);
            }
        }

        String hostIncluded = optionFilter.getFilter("host");
        if (hostIncluded != null && !hostIncluded.isEmpty() && hostIncluded.equalsIgnoreCase("true")) {
            Tenant hostTenant = Lookup.getHostTenant();
            String hostTenantId = hostTenant != null ? hostTenant.getTenantId().toString() : "100";
            if (tenantIdFilter == null || !(tenantIdFilter.equalsIgnoreCase(hostTenantId))) {
                MultivaluedMap<String, String> queryMap = new MultivaluedHashMap<>();
                queryMap.putSingle("tenantId", hostTenantId);
                queryMap.putSingle("isShared", "true");
                optionFilter = new OptionFilter(queryMap);
                options = new Options(optionPage, optionSort, optionFilter);
                list.addAll(userDal.getList(options));
            }
        }

        List<UserDTO> userMinimal = new ArrayList<UserDTO>();
        if (list != null && !list.isEmpty()) {

            for (User user : list) {
                UserDTO um = new UserDTO(user.getUserId(), user.getGroupId(), user.getFirstName(), user.getLastName(), user.getUserName(), user.getEmailAddress(), user.getPhoneNumber(), user.getHomeEmailAddress(), user.getHomePhoneNumber(), user.getIsShared(), user.getOrganizationId());
                userMinimal.add(um);
            }
        }
        return userMinimal;
    }

    @GetMapping(value = "/shared", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "host", description = "host value is like: true|false", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<User[]> getSharedUsers(HttpServletRequest request) {
        log.trace("getSharedUsers; requestUri={}", request.getRequestURI());

        List<User> list = prepareSharedUsers(request);

        for (User user : list) {
            hidePasswordDetails(user);
            hideIVRPinDetails(user);
        }
        User[] array = new User[list.size()];
        list.toArray(array);

//        return Response.ok().entity(array).expires(new Date()).build();
        return ResponseEntity.ok().body(array);
    }

    @GetMapping(value = "/shared/v2", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "host", description = "host value is like: true|false", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<List<UserDTO>> getSharedUsersMinimal(HttpServletRequest request) {
        log.trace("getSharedUsers; requestUri={}", request.getRequestURI());
        List<UserDTO> list = prepareSharedUsersMinimal(request);
//        return Response.ok().entity(list).expires(new Date()).build();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/sharedWithTenant", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "host", description = "host value is like: true|false", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<UserWithTenantId[]> getSharedUsersWithTenant(HttpServletRequest request) {
        log.trace("getSharedUsersWithTenant; requestUri={}", request.getRequestURI());

        List<User> list = prepareSharedUsers(request);
        List<UserWithTenantId> listResult = new LinkedList<UserWithTenantId>();
        //hide password details & add tenantId
        for (User user : list) {
            hidePasswordDetails(user);
            hideIVRPinDetails(user);
            UserWithTenantId userWithTenantId = new UserWithTenantId();
            userWithTenantId.setTenantId(Lookup.getTenantIdByOrganizationId(user.getOrganizationId()));
            BeanUtils.copyProperties(user, userWithTenantId);
            listResult.add(userWithTenantId);
        }
        UserWithTenantId[] array = new UserWithTenantId[listResult.size()];
        listResult.toArray(array);

//        return Response.ok().entity(array).expires(new Date()).build();
        return ResponseEntity.ok().body(array);
    }

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "firstName", description = "firstName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationId", description = "organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationName", description = "organizationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isShared", description = "isShared", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "variableName", description = "variableName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userIdList", description = "Enter User Id by comma separated...", schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true)})
    public ResponseEntity<Integer> count(HttpServletRequest request) {
        log.trace("count; requestUri={}", request.getRequestURI());

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>();
        parameterMap.forEach(multivaluedHashMap::addAll);

        OptionSort optionSort = new OptionSort(multivaluedHashMap);
        OptionFilter optionFilter = new OptionFilter(multivaluedHashMap);
        Options options = new Options(optionSort, optionFilter);
        if (multivaluedHashMap.containsKey(SearchUtils.SEARCH_PARAM)) {
            return ResponseEntity.ok(userDal.getSearchCount(options));
        } else {
            return ResponseEntity.ok(userDal.getCount(options));
        }
    }

    @GetMapping(value = "/permissions", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<List<String>> getPermissions(@RequestParam("userName") String userName, @RequestParam("applicationName") String applicationName) {
        log.trace("getPermissions; userName={}; applicationName={}", userName, applicationName);
        return ResponseEntity.ok(userDal.getUserPermissions(userName, applicationName));
    }

    @GetMapping(value = "/validationRules", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<ValidationRules> getValidationRules() {
        ValidationRules validationRules = new ValidationRules();
        validationRules.getFieldRulesList().addAll(ValidationUtil.retrieveValidationRules(User.class));
        for (FieldRules fr : validationRules.getFieldRulesList()) {
            if (fr.getFieldName().equals("userName")) {
                fr.getSizeRule().setMin(deploymentUtil.getUserNameMinLength());
                break;
            }

        }

//        FieldRules fr=new FieldRules();
//        fr.setFieldName("userName");
//        fr.setSizeRule(new SizeRule(deploymentUtil.getUserNameMinLength(),32));
//        validationRules.getFieldRulesList().add(fr);
        return ResponseEntity.ok(validationRules);
    }

    private void validate(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (violations.size() > 0) {
            log.warn("validate; violations={}", violations);

            ConstraintViolation<User> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }

    private User hidePasswordDetails(User user) {
        user.setPasswordSalt(null);
        user.setPasswordHash(null);
        return user;
    }

    private User hideIVRPinDetails(User user) {
        user.setIvrPinSalt(null);
        user.setIvrPinHash(null);
        return user;
    }

    @GetMapping(value = "/usersNotAssignToGroup", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "firstName", description = "firstName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationId", description = "organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationName", description = "organizationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isShared", description = "isShared", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "variableName", description = "variableName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userIdList", description = "Enter User Id by comma separated...", schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true)})
    public ResponseEntity<User[]> usersNotAssignToGroup(HttpServletRequest request) {
        log.trace("usersNotAssignToGroup; requestUri={}", request.getRequestURI());

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>();
        parameterMap.forEach(multivaluedHashMap::addAll);

        OptionFilter optionFilter = new OptionFilter(multivaluedHashMap);
        Options options = new Options(optionFilter);
        List<User> list = userDal.getUsersNotAssignToGroup(options);
        log.trace("usersNotAssignToGroup; size " + (list != null ? list.size() : 0));
        for (User user : list) {
            hidePasswordDetails(user);
            hideIVRPinDetails(user);
        }
        User[] array = new User[list.size()];
        list.toArray(array);

//        return Response.ok().entity(array).expires(new Date()).build();
        return ResponseEntity.ok().body(array);
    }

    @GetMapping(value = "/getUsersOfAnotherGroup", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "firstName", description = "firstName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationId", description = "organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationName", description = "organizationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isShared", description = "isShared", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "variableName", description = "variableName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userIdList", description = "Enter User Id by comma separated...", schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true)})
    public ResponseEntity<User[]> getUsersOfAnotherGroup(HttpServletRequest request) {
        log.trace("getUsersOfAnotherGroup; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>();
        parameterMap.forEach(multivaluedHashMap::addAll);
        String groupIdStr = multivaluedHashMap.getFirst("groupId");
        @SuppressWarnings("unused") Integer groupId = null;
        try {
            groupId = Integer.parseInt(groupIdStr);
        } catch (NumberFormatException pe) {
            log.trace("getUsersOfAnotherGroup; groupid parsing failed.. expected in case of null , groupIdStr = {}", groupIdStr);
        }
        OptionFilter optionFilter = new OptionFilter(multivaluedHashMap);
        Options options = new Options(optionFilter);
        List<User> list = userDal.getUsersOfAnotherGroup(options);
        log.trace("getUsersOfAnotherGroup; list size ={}", (list != null ? list.size() : 0));
        for (User user : list) {
            hidePasswordDetails(user);
            hideIVRPinDetails(user);
        }
        User[] array = new User[list.size()];
        list.toArray(array);
        return ResponseEntity.ok().body(array);
    }

    @GetMapping(value = "/checkTenantIdInOrgAndGroup", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "organizationId", description = "organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY, required = true), @Parameter(name = "groupId", description = "groupId", schema = @Schema(type = "string"), in = ParameterIn.QUERY, required = true),})
    public ResponseEntity<Boolean> checkTenantIdInOrgAndGroup(HttpServletRequest request) {
        log.trace("checkTenantIdInOrgAndGroup; requestUri={}", request.getRequestURI());
//        String organizationIdStr = uriInfo.getQueryParameters().getFirst("organizationId");
        String organizationIdStr = request.getParameter("organizationId");

        Long organizationId = null;
        try {
            organizationId = Long.parseLong(organizationIdStr);
        } catch (NumberFormatException pe) {
            log.trace("checkTenantIdInOrgAndGroup; organizationid parsing failed.. expected in case of null , organizationIdStr = {}", organizationIdStr);
        }
//        String groupIdStr = uriInfo.getQueryParameters().getFirst("groupId");
        String groupIdStr = request.getParameter("groupId");

        Long groupId = null;
        try {
            if (groupIdStr != null && !groupIdStr.equals("-1")) groupId = Long.parseLong(groupIdStr);
            else
//                return Response.ok().entity(true).expires(new Date()).build();
                return ResponseEntity.ok().body(true);
        } catch (NumberFormatException pe) {
            log.trace("checkTenantIdInOrgAndGroup; groupid parsing failed.. expected in case of null , groupIdStr = {}", groupIdStr);
        }
        boolean allowFlag = userDal.checkTenantIdInOrgAndGroup(organizationId, groupId);
//        return Response.ok().entity(allowFlag).expires(new Date()).build();
        return ResponseEntity.ok().body(allowFlag);
    }

    @GetMapping(value = "/userIdNames", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "firstName", description = "firstName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationId", description = "organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationName", description = "organizationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isShared", description = "isShared", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "variableName", description = "variableName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userIdList", description = "Enter User Id by comma separated...", schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true)})
    public ResponseEntity<StreamingResponseBody> getUserIdNames(@RequestParam MultiValueMap<String, String> uriInfo) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        log.trace("getUserIdNames; requestUri={}", request.getRequestURI());
        MultivaluedMap<String, String> multiMap = new MultivaluedHashMap<>();
        uriInfo.forEach(multiMap::addAll);
        OptionPage optionPage = new OptionPage(multiMap, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(multiMap);
        OptionFilter optionFilter = new OptionFilter(multiMap);
        Options options = new Options(optionPage, optionSort, optionFilter);
        final List<Map<String, Object>> list = userDal.getUserIdNames(options);

//        old one as using the jersy api element.
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.beginArray();
                for (Map<String, Object> message : list) {
                    gson.toJson(message, Map.class, writer);
                }
                writer.endArray();
                writer.close();
            }
        };

//        this is of the new one where we using the springframework web.
        StreamingResponseBody streamingResponseBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.beginArray();
                list.forEach(message -> gson.toJson(message, Map.class, writer));
                writer.endArray();
                writer.close();
            }
        };

//        return Response.ok().entity(stream).expires(new Date()).build();
        return ResponseEntity.ok().body(streamingResponseBody);
    }

    @GetMapping(value = "/auditUserIdNames", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<StreamingResponseBody> getAuditUserIdNames(HttpServletRequest request) {
        log.trace("getUserIdNames; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();
        parameterMap.forEach(multivaluedMap::addAll);
        OptionPage optionPage = new OptionPage(multivaluedMap, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(multivaluedMap);
        OptionFilter optionFilter = new OptionFilter(multivaluedMap);
        Options options = new Options(optionPage, optionSort, optionFilter);
        final List<Map<String, Object>> list = userDal.getUserIdNames(options);
        if (deploymentUtil.isUserSoftDelete()) {
            List<UserHistory> result = userHistoryDal.getAllUserHistory();
            if (!Objects.isNull(result)) {
                for (UserHistory obj : result) {
                    Map<String, Object> temp = new HashMap<String, Object>();
                    temp.put("userId", obj.getUserId());
                    temp.put("userName", obj.getUserName() + " ( Deleted )");
                    temp.put("groupId", obj.getGroupId());
                    list.add(temp);
                }
            }
        }
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.beginArray();
                for (Map<String, Object> message : list) {
                    gson.toJson(message, Map.class, writer);
                }
                writer.endArray();
                writer.close();
            }
        };

        StreamingResponseBody streamingResponseBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.beginArray();
                list.forEach(message -> gson.toJson(message, Map.class, writer));
                writer.endArray();
                writer.close();
            }
        };

//        return Response.ok().entity(stream).expires(new Date()).build();
        return ResponseEntity.ok().body(streamingResponseBody);
    }

    @GetMapping(value = "/userIdNamesWithScope", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "firstName", description = "firstName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationId", description = "organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationName", description = "organizationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isShared", description = "isShared", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "variableName", description = "variableName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userIdList", description = "Enter User Id by comma separated...", schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true)})
    public ResponseEntity<StreamingResponseBody> getUserIdNamesWithScope(HttpServletRequest request) {
        log.trace("getUserIdNamesWithScope; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach(uriInfo::addAll);
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        final List<Map<String, Object>> list = userDal.getUserIdNamesWithScope(options);
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.beginArray();
                for (Map<String, Object> message : list) {
                    gson.toJson(message, Map.class, writer);
                }
                writer.endArray();
                writer.close();
            }
        };

        StreamingResponseBody streamingResponseBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.beginArray();
                list.forEach(message -> gson.toJson(message, Map.class, writer));
                writer.endArray();
                writer.close();
            }
        };
//        return Response.ok().entity(stream).expires(new Date()).build();
        return ResponseEntity.ok().body(streamingResponseBody);
    }

    @GetMapping(value = "/customUserInfo", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "firstName", description = "firstName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationId", description = "organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationName", description = "organizationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isShared", description = "isShared", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "variableName", description = "variableName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userIdList", description = "Enter User Id by comma separated...", schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true)})
    public ResponseEntity<String> getCustomUserInfo(HttpServletRequest request) {
        log.trace("getCustomUserInfo; requestUri={}", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach(uriInfo::addAll);
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = userDal.searchCustomUserInfo(options);
        } else {
            list = userDal.getCustomUserInfo(options);
        }
//        return Response.ok().entity(new Gson().toJson(list).toString()).expires(new Date()).build();
        return ResponseEntity.ok().body(new Gson().toJson(list));
    }

    @GetMapping(value = "/checkEntityPermission", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, description = RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, schema = @Schema(type = "string"), in = ParameterIn.QUERY, required = true), @Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "firstName", description = "firstName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupId", description = "groupId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantName", description = "tenantName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationId", description = "organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "organizationName", description = "organizationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "isShared", description = "isShared", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "variableName", description = "variableName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userIdList", description = "Enter User Id by comma separated...", schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true)})
    public ResponseEntity<Boolean> checkEntityPermission(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionFilter);
//        return userDal.checkEntityPermission(Integer.parseInt(optionFilter.getFilter(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER)), options);
        return ResponseEntity.ok().body(userDal.checkEntityPermission(Integer.parseInt(optionFilter.getFilter(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER)), options));
    }

    @GetMapping(value = "/isUserAssociatedinDispatchContact", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "userId", schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true, required = true)})
    public ResponseEntity<Boolean> isUserAssociatedinDispatchContact(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach(uriInfo::addAll);
//        return userDal.isUserAssociatedinDispatchContact(Integer.parseInt(uriInfo.getQueryParameters().getFirst("userId")));
        return ResponseEntity.ok().body(userDal.isUserAssociatedinDispatchContact(Integer.parseInt(uriInfo.getFirst("userId"))));
    }

    @PostMapping(value = "/isUserAssociatedinDispatchContact", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<?> loginRequestIvr(@RequestParam Map<String, String> loginRequestParams) {
        String ivrUserId = loginRequestParams.get("ivrUserId");
        String ivrPin = loginRequestParams.get("ivrPin");
        User user = userDal.getByIVRUserId(ivrUserId);
        if (user != null && !Boolean.TRUE.equals(user.getIsIVRUserLocked())) {
            if (user.checkIVRPin(ivrPin, ivrUserId)) {
                loginLogDal.create(LoginLog.createLoginLog(user.getUserName(), LoginLog.LOG_TYPE_LOGIN_IVR, true, null, null, LoginResponse.IVR_LOGIN_SUCCESSFULL, null, null));
                hideIVRPinDetails(user);
                hidePasswordDetails(user);
                userDal.updateConsecutiveIVRLoginFailures(user.getUserId(), 0);
//                return Response.ok().entity(user).expires(new Date()).build();
                return ResponseEntity.ok().body(user);

            } else {
                int consecutiveIVRLoginFailures = user.getConsecutiveIVRLoginFailures() == null ? 0 : user.getConsecutiveIVRLoginFailures();
                userDal.updateConsecutiveIVRLoginFailures(user.getUserId(), consecutiveIVRLoginFailures + 1);
            }
        } else if (user != null && Boolean.TRUE.equals(user.getIsIVRUserLocked())) {
            loginLogDal.create(LoginLog.createLoginLog(ivrUserId, LoginLog.LOG_TYPE_LOGIN_IVR, false, null, null, LoginResponse.IVR_LOGIN_FAILED, null, null));
//            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"resultCode\":\"ivrUserLocked\"}").expires(new Date()).build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"resultCode\":\"ivrUserLocked\"}");

        }
        loginLogDal.create(LoginLog.createLoginLog(ivrUserId, LoginLog.LOG_TYPE_LOGIN_IVR, false, null, null, LoginResponse.IVR_LOGIN_FAILED, null, null));
//        return Response.status(Response.Status.UNAUTHORIZED).entity("{\"resultCode\":\"invalidCredentials\"}").expires(new Date()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"resultCode\":\"invalidCredentials\"}");
    }

    /* RBAC-1475 MakerChecker Start */

    /**
     * @param
     * @return
     * @Description For every new entity this response has to be created to return the entity value
     * for the approver. Example, a super tenant cannot generally see the users of its sub tenants.
     * But this response enables the super tenant to view only mode and approve / reject the user request in the system.
     */

    @GetMapping(value = "/MakerCheckerResponse", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "entityId", description = "entityId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "makerCheckerId", description = "makerCheckerId", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInUserName", description = "loggedInUserName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "entityName", description = "entityName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "makerCheckerIdForAction", description = "makerCheckerIdForAction", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, description = RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "entityToShow", description = "entityToShow", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "type", description = "type", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInTenant", description = "loggedInTenant", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<User> getByIdResponse(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach(uriInfo::addAll);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        User user = null;
        if (filters != null) {
            String userId = filters.get("entityId");
            if (userId != null && userId.length() > 0) {
                user = hidePasswordDetails(hideIVRPinDetails(userDal.getById(Integer.valueOf(userId))));
                user.setOrganizationName(Lookup.getOrganizationNameById(user.getOrganizationId()));
                Boolean canBeEditted = makerCheckerDal.checkIfEntityIsEditable1(uriInfo);
                user.setEditable(canBeEditted);
            }
        } else {

            ErrorInfoException e = new ErrorInfoException(BaseDalJpa.MKR_OPERATION_NOT_ALLOWED);
            throw e;
        }
//        return user;
        return ResponseEntity.ok().body(user);
    }
    /* RBAC-1475 MakerChecker End */

    @PostMapping(value = "/changeIVRPassword", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public void changeIVRPassword(@RequestParam Map<String, String> changeIVRPasswordRequestParams) throws Exception {
        String userName = changeIVRPasswordRequestParams.get("userName");
        String oldPassword = changeIVRPasswordRequestParams.get("oldPin");
        String newPassword = changeIVRPasswordRequestParams.get("newPin");

        try {
            userDal.changeIVRPassword(userName, oldPassword, newPassword);
            loginLogDal.create(LoginLog.createLoginLog(userName, LoginLog.LOG_TYPE_CHANGE_IVR_PASSWORD_RBAC, true, "", "", LoginResponse.IVR_PASSWORD_CHANGE_SUCCESSFULL, null, null));
        } catch (Exception e) {
            if (e instanceof ErrorInfoException) {
                loginLogDal.create(LoginLog.createLoginLog(userName, LoginLog.LOG_TYPE_CHANGE_IVR_PASSWORD_RBAC, false, "", "", ((ErrorInfoException) e).getCode(), null, null));
            } else {
                loginLogDal.create(LoginLog.createLoginLog(userName, LoginLog.LOG_TYPE_CHANGE_IVR_PASSWORD_RBAC, false, "", "", e.toString(), null, null));
            }
            throw e;
        }
    }

    @GetMapping(value = "/loginTypes", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<LoginType[]> getLoginTypes() throws Exception {
        log.trace("getLoginTypes;");
        return ResponseEntity.ok().body(loginService.getLoginTypes());
    }

    //RBAC-1562
    @GetMapping(value = "/isTwoFactorActiveForUser/{tenantId}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> isTwoFactorActiveForUser(@PathVariable("tenantId") Long tenantId) {
//        return userDal.checkTwoFactorActiveForUserAndTenant(tenantId);
        return ResponseEntity.ok().body(userDal.checkTwoFactorActiveForUserAndTenant(tenantId));
    }

    @GetMapping(value = "/isAzureUserMgmtEnabled", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> isAzureUserMgmtEnabled() {
//        return userDal.isAzureUserMgmtEnabled();
        return ResponseEntity.ok().body(userDal.isAzureUserMgmtEnabled());
    }

    @GetMapping(value = "/isAssertPasswordsEnabled", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> isAssertPasswordsEnabled() {
//        return userDal.isAssertPasswordsEnabled();
        return ResponseEntity.ok().body(userDal.isAssertPasswordsEnabled());
    }
}
