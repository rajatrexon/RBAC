package com.esq.rbac.service.scope.scopeconstraint.rest;


import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.externaldb.service.ExternalDbDal;
import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import com.esq.rbac.service.scope.scopeconstraint.service.ScopeConstraintDal;
import com.esq.rbac.service.session.constraintdata.domain.ConstraintData;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.ExternalDataAccessRBAC;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.ScopeRestrictionUtil;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.util.externaldatautil.ExternalDataAccessUtil;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/scopeConstraints")
@Tag(name= "/scopeConstraints")
@Slf4j
public class ScopeConstraintRest {

    private ScopeConstraintDal scopeConstraintDal;
    private ScopeRestrictionUtil scopeRestrictionUtil;
    private ExternalDbDal externalDbDal;
    private final String APPLICATION_RBAC = "RBAC";
    private ExternalDataAccessRBAC externalDataAccessRBAC;
    private ExternalDataAccessUtil externalDataAccessUtil;
    private DeploymentUtil deploymentUtil;
    private UserDal userDal;
    private final static int ZERO_WIDTH_SPACE_PDF_CHARACTER_INTERVAL = 10;
    private static final String SCOPE_DEFINITION_DETAIL_SCOPECONSTRAINT_NAME = "getScopeDefinitionDetailData";
    private static final String SCOPE_DEFINITION_DETAIL_FILTER_KEY = "filterId";
    private String randomNumberForAuditLog = null; //RBAC-1450

    //prevent DOS attack, limit no. of concurrent export requests
    private static Semaphore reportSemaphore = null;
    private static Semaphore xsltSemaphore = null;

    private static final Map<String, String> reportCountMap = new HashMap<String, String>();
    private static final String STRING_VALIDATOR = "[<>=%&#!;']";
    private static final String NUMERIC_VALIDATOR = "^[,0-9]+$";
    private AuditLogService auditLogDal;
    @Autowired
    public void setAuditLogDal(AuditLogService auditLogDal){
        this.auditLogDal = auditLogDal;
    }

    static{
        reportCountMap.put("accessMatrix", "accessMatrixCount");
        reportCountMap.put("accessMatrixGroup", "accessMatrixGroupCount");
        reportCountMap.put("auditLogAppTargetOperation", "auditLogAppTargetOperationCount");
        reportCountMap.put("auditLogAppTarget", "auditLogAppTargetCount");
        reportCountMap.put("auditLogApp", "auditLogAppCount");
        reportCountMap.put("auditLog", "auditLogCount");
        reportCountMap.put("groupScopeDescription", "groupScopeDescriptionCount");
        reportCountMap.put("loginLog", "loginLogCount");
        reportCountMap.put("userActivity", "userActivityCount");
    }

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        log.trace("setDeploymentUtil; {}",deploymentUtil);
        this.deploymentUtil = deploymentUtil;
        reportSemaphore = new Semaphore(this.deploymentUtil.getReportSemaphoreSize());
        xsltSemaphore = new Semaphore(this.deploymentUtil.getPdfReportXsltLockCount());
    }
    @Autowired
    public void setScopeRestrictionUtil(ScopeRestrictionUtil scopeRestrictionUtil) {
        this.scopeRestrictionUtil = scopeRestrictionUtil;
    }

    @Autowired
    public void setExternalDataAccessUtil(ExternalDataAccessUtil externalDataAccessUtil) {
        log.trace("setExternalDataAccessUtil; externalDataAccessUtil={}", externalDataAccessUtil);
        this.externalDataAccessUtil = externalDataAccessUtil;
    }

    @Autowired
    public void setScopeConstraintDal(ScopeConstraintDal scopeConstraintDal,
                                      ExternalDbDal externalDbDal) {
        log.trace("setScopeConstraintDal; externalDbDal={}", externalDbDal);
        this.scopeConstraintDal = scopeConstraintDal;
        this.externalDbDal = externalDbDal;
    }

    @Autowired
    public void setExternalDataAccessRBAC(ExternalDataAccessRBAC externalDataAccessRBAC) {
        this.externalDataAccessRBAC = externalDataAccessRBAC;
    }

    @Autowired
    public void setUserDal(UserDal userDal) {
        this.userDal = userDal;
    }

    public void validateDate(String dateString) {
        if (dateString == null) {
            return;
        }
        String date = dateString.replaceAll("[-]","/");
        try {
            new SimpleDateFormat("yyyy/mm/dd").parse(date);
        } catch (Exception e) {
            log.error("validateDate; dateString={}; format={}; e={};",
                    date, e);
            throw new ErrorInfoException("invalidDate");
        }
    }

//    @GET
//    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
//    @Path("/{constraintId}/data")
    @GetMapping(path = "/{constraintId}/data",produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<ConstraintData[]> getConstraintsData(
            @PathParam("constraintId") int constraintId) {
        ScopeConstraint scopeConstraint = scopeConstraintDal
                .getById(constraintId);

        if (scopeConstraint != null) {
            log.trace("getConstraintsData; scopeName={}",
                    scopeConstraint.getScopeName());
            ConstraintData[] constraintDataArray = externalDbDal.getData(
                    scopeConstraint.getApplicationName(),
                    scopeConstraint.getSqlQuery(), null);
            log.trace("getConstraintsData; constraintDataArray={}",
                    constraintDataArray!=null? Arrays.asList(constraintDataArray):null);
            return ResponseEntity.ok(constraintDataArray);
        }
        return ResponseEntity.ok(new ConstraintData[] {});
    }

//    @GET
//    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
//    @Path("/{constraintId}/inListData")
    @GetMapping(path = "/{constraintId}/inListData",produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "userId", description = "userId", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "scopeId", description = "scopeId", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "first", description = "first", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "max", description = "max", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "fromDate", description = "fromDate", schema = @Schema(type = "string" , format="date"), in = ParameterIn.QUERY),
            @Parameter(name = "toDate", description = "toDate", schema = @Schema(type = "string", format="date"), in = ParameterIn.QUERY),

    })
    public ResponseEntity<ConstraintData[]> getConstraintsInListData(
            @PathParam("constraintId") int constraintId, HttpServletRequest request) {
        ScopeConstraint scopeConstraint = scopeConstraintDal
                .getById(constraintId);
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));

        if (scopeConstraint != null) {
            log.trace("getConstraintsInListData; scopeName={}",
                    scopeConstraint.getScopeName());
            uriInfo.add("groupId",userDal.getById(Integer.parseInt(uriInfo.getFirst("userId"))).getGroupId().toString());

            log.debug("ScopeQuery Inlist {}",scopeConstraint.getSqlQuery());
            log.debug("QueryParams Inlist {}",uriInfo);
            ConstraintData[] constraintDataArray = externalDbDal.getData(
                    scopeConstraint.getApplicationName(),
                    scopeConstraint.getSqlQuery(), uriInfo);
            log.debug("getConstraintsInListData; constraintDataArray={}",
                    constraintDataArray!=null?Arrays.asList(constraintDataArray):null);
            if(constraintDataArray!=null && constraintDataArray.length > 0){
                try{
                    ScopeConstraint scopeConstraintDetail = scopeConstraintDal
                            .getByScopeName(SCOPE_DEFINITION_DETAIL_SCOPECONSTRAINT_NAME);
                    if(scopeConstraintDetail!=null && deploymentUtil.getInListScopedApplicationsList().contains(scopeConstraint.getApplicationName().toLowerCase())){
                        Map<String, String> filters = new TreeMap<String, String>();
                        filters.put("groupId", uriInfo.getFirst("groupId"));
                        filters.put("scopeId", uriInfo.getFirst("scopeId"));
                        List<Map<String, Object>> scopeConstraintDetailData = externalDbDal.getRowSetData(scopeConstraintDetail.getApplicationName(), scopeConstraintDetail.getSqlQuery(), filters);
                        if(scopeConstraintDetailData!=null && !scopeConstraintDetailData.isEmpty()){
                            log.trace("getConstraintsInListData; scopeConstraintDetailData={};", scopeConstraintDetailData);
                            List<String> filterIds = new ArrayList<String>();
                            for(Map<String, Object> temp:scopeConstraintDetailData){
                                filterIds.add(temp.get(SCOPE_DEFINITION_DETAIL_FILTER_KEY).toString());
                            }
                            List<ConstraintData> constraintDataArrayResponseList = new ArrayList<ConstraintData>();
                            for(ConstraintData cd:constraintDataArray){
                                if(filterIds.contains(cd.getId()) == true){
                                    constraintDataArrayResponseList.add(cd);
                                }
                            }
                            log.trace("getConstraintsInListData; constraintDataArrayResponseList={};", constraintDataArrayResponseList);
                            return ResponseEntity.ok(constraintDataArrayResponseList.toArray(new ConstraintData[constraintDataArrayResponseList.size()]));
                        }
                    }
                }
                catch(Exception e){
                    log.error("getConstraintsInListData; Exception={}", e);
                }
            }
            return ResponseEntity.ok(constraintDataArray);
        }
        return ResponseEntity.ok(new ConstraintData[] {});
    }


    @GetMapping(path = "/{constraintName}/customData",produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", schema = @Schema(type = "string"), in = ParameterIn.HEADER, required=true),
            @Parameter(name = "userId", description = "userId", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "scopeId", description = "scopeId", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.GROUP_SCOPE_QUERY, description = RBACUtil.GROUP_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "first", description = "first", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "max", description = "max", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "fromDateRaw", description = "fromDateRaw", schema = @Schema(type = "string",  format="date"), in = ParameterIn.QUERY),
            @Parameter(name = "toDateRaw", description = "toDateRaw", schema = @Schema(type = "string",  format="date"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<StreamingResponseBody> getCustomData(
            @PathParam("constraintName") String constraintName,
            HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception{
        ScopeConstraint scopeConstraint = scopeConstraintDal.getByScopeName(constraintName);

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionFilter optionFilter = new OptionFilter(uriInfo);

        List<String> restrictionIds = new LinkedList<String>();
        if(constraintName.equals("auditLog") || constraintName.equals("auditLogCount") || constraintName.equals("accessMatrix") || constraintName.equals("accessMatrixCount") || constraintName.equals("loginLog") || constraintName.equals("loginLogCount")
                || constraintName.equals("auditLogApp") || constraintName.equals("auditLogAppCount") || constraintName.equals("auditLogAppTarget") || constraintName.equals("auditLogAppTargetCount") || constraintName.equals("auditLogAppTargetOperation")
                || constraintName.equals("auditLogAppTargetOperationCount")){
            String userScopeQuery = uriInfo.get(RBACUtil.USER_SCOPE_QUERY) != null && uriInfo.get(RBACUtil.USER_SCOPE_QUERY).size() > 0 ? uriInfo.get(RBACUtil.USER_SCOPE_QUERY).get(0) : null;
            restrictionIds = scopeRestrictionUtil.handleForUserRestriction(userScopeQuery);
        }
        else if(constraintName.equals("accessMatrixGroup") || constraintName.equals("accessMatrixGroupCount") || constraintName.equals("groupScopeDescription") || constraintName.equals("groupScopeDescriptionCount") || constraintName.equals("userActivity") || constraintName.equals("userActivityCount")){
            String groupScopeQuery = uriInfo.get(RBACUtil.GROUP_SCOPE_QUERY) != null && uriInfo.get(RBACUtil.GROUP_SCOPE_QUERY).size() > 0 ? uriInfo.get(RBACUtil.GROUP_SCOPE_QUERY).get(0) : null;
            restrictionIds = scopeRestrictionUtil.handleForGroupRestriction(groupScopeQuery);
        }
        Map<String, String> filters = optionFilter.getFilters();
        if(!filters.isEmpty() && filters.containsKey("userId")){
            String userId = String.valueOf(filters.get("userId")).replaceAll("[\\[\\]]","");
            numericValidator(NUMERIC_VALIDATOR, userId);
        }
        if(!filters.isEmpty() && filters.containsKey("operationId")){
            String operationId = String.valueOf(filters.get("operationId")).replaceAll("[\\[\\]]","");
            numericValidator(NUMERIC_VALIDATOR, operationId);
        }
        if(!filters.isEmpty() && filters.containsKey("applicationId")){
            String applicationId = String.valueOf(filters.get("applicationId")).replaceAll("[\\[\\]]","");
            numericValidator(NUMERIC_VALIDATOR, applicationId);
        }
        if(!filters.isEmpty() && filters.containsKey("groupId")){
            String groupId = String.valueOf(filters.get("groupId")).replaceAll("[\\[\\]]","");
            numericValidator(NUMERIC_VALIDATOR, groupId);
        }
        if(!filters.isEmpty() && filters.containsKey("currentLoggedInUser")){
            stringValidator(STRING_VALIDATOR, filters.get("currentLoggedInUser"));
        }
        if(!filters.isEmpty() && filters.containsKey("randomNumber")){
            stringValidator(STRING_VALIDATOR, filters.get("randomNumber"));
        }
        if(!filters.isEmpty() && filters.containsKey("asc")){
            stringValidator(STRING_VALIDATOR, filters.get("asc"));
        }
        if(!filters.isEmpty() && filters.containsKey("desc")){
            stringValidator(STRING_VALIDATOR, filters.get("desc"));
        }
        if (filters.get("fromDateRaw") != null && filters.get("fromDateRaw").trim() != ""
                && filters.get("toDateRaw") != null && filters.get("toDateRaw").trim() != "") {
            validateDate(filters.get("fromDateRaw"));
            validateDate(filters.get("toDateRaw"));
        } else if (filters.get("fromDateRaw") != null && filters.get("fromDateRaw").trim() != "") {
            validateDate(filters.get("fromDateRaw"));
        } else if (filters.get("toDateRaw") != null && filters.get("toDateRaw").trim() != "") {
            validateDate(filters.get("toDateRaw"));
        }
        int firstCount = 1;
        String filterRandomNumber = null;
        try {
            firstCount = Integer.parseInt(filters.get("first"));
            filterRandomNumber = filters.get("randomNumber");
        }catch (Exception e) {
            log.debug("Not an integer value");
        }
        if(deploymentUtil.isEnableAuditInfoInAuditLog()) {//RBAC-1450
            if (constraintName.equals("auditLog") && firstCount < 2) {
                if (!filterRandomNumber.equalsIgnoreCase(randomNumberForAuditLog)) {
                    Integer userId = Integer.parseInt(headers.get("userId").get(0));
                    String auditSearchForUser = filters.get("userId");
                    String userSearch = "all users";
                    if (auditSearchForUser != null && !auditSearchForUser.equalsIgnoreCase("All")) {
                        try {
                            Integer userIdForSearch = Integer.parseInt(auditSearchForUser);
                            userSearch = userDal.getById(userIdForSearch).getUserName();
                        } catch (Exception e1) {
                            userSearch = "all users";
                        }
                    }
                    String timePeriod = "";
                    if (filters.get("fromDateRaw") != null && filters.get("fromDateRaw").trim() != ""
                            && filters.get("toDateRaw") != null && filters.get("toDateRaw").trim() != "") {
                        timePeriod = " for the time period " + filters.get("fromDateRaw") + " to " + filters.get("toDateRaw");
                    } else if (filters.get("fromDateRaw") != null && filters.get("fromDateRaw").trim() != "") {
                        timePeriod = " from " + filters.get("fromDateRaw")+" uptill now";
                    } else if (filters.get("toDateRaw") != null && filters.get("toDateRaw").trim() != "") {
                        timePeriod = " upto " + filters.get("toDateRaw");
                    }
                    Map<String, String> objectChanges = new TreeMap<String, String>();
                    objectChanges.put("", "Accessed Audit Log Report for " + userSearch + timePeriod);
                    auditLogDal.createSyncLog(userId, "View Audit Log", "Report", "Audit.Log", objectChanges);
                    randomNumberForAuditLog = filterRandomNumber;
                }

            }
        }
        if (scopeConstraint != null) {
            if(log.isTraceEnabled()){
                log.trace("getCustomData; scopeName={}; restrictionIds={};",
                        scopeConstraint.getScopeName(), restrictionIds);
            }
            String sql = scopeConstraint.getSqlQuery();
            final List<Map<String, Object>> customDataResponse = externalDbDal
                    .getCustomData(scopeConstraint.getApplicationName(), sql,
                            filters, constraintName, restrictionIds);
            if(log.isTraceEnabled()){
                log.trace("getCustomData; constraintDataArray={}",
                        customDataResponse);
            }
            StreamingResponseBody stream = new StreamingResponseBody() {
                @Override
                public void writeTo(OutputStream os) throws IOException {
                    Gson gson = new Gson();
                    JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.beginArray();
                    for (Map<String, Object> message : customDataResponse) {
                        gson.toJson(message, Map.class, writer);
                    }
                    writer.endArray();
                    writer.close();
                }
            };
           return ResponseEntity.status(HttpStatus.OK).contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(stream);
        }
        return null;
    }
    private void numericValidator(String stringValidator, String attribute) {
        if(!attribute.equalsIgnoreCase("All")){
            if(!Pattern.compile(stringValidator).matcher(attribute).matches()){
                throw new ErrorInfoException("validationError", "Invalid Parameter " + attribute);
            }
        } else if (attribute.trim().equalsIgnoreCase("All")) {
            stringValidator(STRING_VALIDATOR, attribute);
        }
    }
    private void stringValidator(String stringValidator, String attribute) {
        Pattern res = Pattern.compile(stringValidator);
        Matcher mat = res.matcher(attribute);
        if(mat.find()){
            throw new ErrorInfoException("validationError", "Invalid Parameter " + attribute);
        }
    }

    @GetMapping(path = "/{scopeConstraintId}/attributeData",produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name = RBACUtil.USER_SCOPE_QUERY, description = RBACUtil.USER_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = RBACUtil.GROUP_SCOPE_QUERY, description = RBACUtil.GROUP_SCOPE_QUERY, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
        public String getAttributeData(
            @PathParam("scopeConstraintId") String scopeConstraintId,
            HttpServletRequest servletRequest,
            @RequestHeader org.springframework.http.HttpHeaders headers) {
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        if(userId != null && userId > 0) {
            User user = userDal.getById(userId);
            if(user != null) {
                uriInfo.add("groupId", user.getGroupId()+"");
            }
        }
        Options options = new Options(new OptionFilter(uriInfo));
        return scopeConstraintDal.getAttributeDataByScopeConstraintId(Integer.parseInt(scopeConstraintId), options, userId, servletRequest);
    }
//    @GET
//    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
//    @Path("/getPartyAttributeAssociation")
    @GetMapping(path = "/getPartyAttributeAssociation",produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name = "valueReferenceId", description = "valueReferenceId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public String getPartyAttributeAssociation(
            HttpServletRequest request,
            @RequestHeader org.springframework.http.HttpHeaders headers) {
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        String valueReferenceId = null;
        if(uriInfo != null && uriInfo != null && uriInfo.get("valueReferenceId") != null && uriInfo.get("valueReferenceId").size() > 0){
            valueReferenceId = uriInfo.get("valueReferenceId").get(0);
            log.trace("getPartyAttributeAssociation; valueReferenceId={}", valueReferenceId);
            if(valueReferenceId != null){
                return externalDataAccessRBAC.getAttributeAssociation(valueReferenceId, userId);
            }
        }
        return null;

    }


    @PostMapping(path = "/{scopeConstraintId}/attributeData",produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
    })
    public String postAttributeData(
            @PathParam("scopeConstraintId") String scopeConstraintId,
            String data, @HeaderParam("Content-Type") String contentType,
            HttpServletRequest request,
            @RequestHeader org.springframework.http.HttpHeaders headers) {
        ScopeConstraint scopeConstraint = scopeConstraintDal.getById(Integer.parseInt(scopeConstraintId));
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        if (scopeConstraint != null && scopeConstraint.getSourceType()!=null ) {
            log.trace("postAttributeData; scopeName={}",
                    scopeConstraint.getScopeName());
            if(scopeConstraint.getSourceType().equals(APPLICATION_RBAC))
                return externalDataAccessRBAC.update(scopeConstraint, data, contentType, userId);
            else
                return externalDataAccessUtil.getExternalDataAccessMap().get(scopeConstraint.getSourceType()).get(scopeConstraint.getApplicationName()).update(scopeConstraint, data, contentType, userId);
        }
        return null;
    }

//    @GET
//    @Produces("application/vnd.ms-excel;charset=utf-8")
//    @Path("/{constraintName}/ExportData")
@GetMapping(value = "/{constraintName}/ExportData", produces = "application/vnd.ms-excel;charset=utf-8")
    @Operation(description = "ExportData",hidden = true)
    public ResponseEntity<StreamingResponseBody> getCustomDataCSV(
            @PathVariable("constraintName") final String constraintName,
            HttpServletRequest request) throws Exception {
        final ScopeConstraint scopeConstraint = scopeConstraintDal
                .getByScopeName(constraintName);
    Map<String, String[]> parameterMap = request.getParameterMap();
    MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

    parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionFilter optionFilter = new OptionFilter(
                uriInfo);
        final Map<String, String> filters = optionFilter.getFilters();
        if (scopeConstraint != null) {
            List<String> restrictionIds = new LinkedList<String>();
            if (constraintName.equals("auditLog") || constraintName.equals("auditLogCount") || constraintName.equals("accessMatrix") || constraintName.equals("accessMatrixCount")|| constraintName.equals("loginLog") || constraintName.equals("loginLogCount")
                    || constraintName.equals("auditLogApp") || constraintName.equals("auditLogAppCount") || constraintName.equals("auditLogAppTarget") || constraintName.equals("auditLogAppTargetCount") || constraintName.equals("auditLogAppTargetOperation")
                    || constraintName.equals("auditLogAppTargetOperationCount")) {
                String userScopeQuery = uriInfo.get(RBACUtil.USER_SCOPE_QUERY) != null && uriInfo.get(RBACUtil.USER_SCOPE_QUERY).size() > 0 ? uriInfo.get(RBACUtil.USER_SCOPE_QUERY).get(0) : null;
                restrictionIds = scopeRestrictionUtil.handleForUserRestriction(userScopeQuery);
            } else if (constraintName.equals("accessMatrixGroup") || constraintName.equals("accessMatrixGroupCount") || constraintName.equals("groupScopeDescription") || constraintName.equals("groupScopeDescriptionCount") || constraintName.equals("userActivity") || constraintName.equals("userActivityCount")) {
                String groupScopeQuery = uriInfo.get(RBACUtil.GROUP_SCOPE_QUERY) != null && uriInfo.get(RBACUtil.GROUP_SCOPE_QUERY).size() > 0 ? uriInfo.get(RBACUtil.GROUP_SCOPE_QUERY).get(0) : null;
                restrictionIds = scopeRestrictionUtil.handleForGroupRestriction(groupScopeQuery);
            }

            log.trace("getCustomDataCSV; scopeName={}", scopeConstraint.getScopeName());
            final String sql = scopeConstraint.getSqlQuery();

            //check the no. of records present, this counter will be used to iterate over the records
            List<Map<String, Object>> countResponse = externalDbDal.getCustomData(scopeConstraint.getApplicationName(), scopeConstraintDal
                    .getByScopeName(reportCountMap.get(constraintName)).getSqlQuery(), filters, constraintName, restrictionIds);
            final int count = Integer.parseInt(countResponse.get(0).get("Count").toString());

            final List<String> finalRestrictionIds = restrictionIds;
            restrictionIds =  null;
            if (count > 0) {
                StreamingResponseBody  stream = new StreamingResponseBody() {
                    @Override
                    public void writeTo(OutputStream os) throws IOException, WebApplicationException
                    {
                        try{
                            reportSemaphore.tryAcquire(deploymentUtil.getReportSemaphoreTimeoutSecs(), TimeUnit.SECONDS);
                            ICsvMapWriter writer = null;
                            byte[] enc = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
                            os.write(enc);
                            writer = new CsvMapWriter(new OutputStreamWriter(os, "UTF-8"), CsvPreference.EXCEL_PREFERENCE);

                            String fileName = filters.size() > 0 && filters.containsKey("fileName") ? filters.get("fileName") : "";
                            if (!fileName.equals("")) {
                                fileName = fileName.replaceAll("_All", "");
                                fileName = fileName.replaceAll("_", " ");
                                if (fileName.indexOf(".csv") > -1)
                                    fileName = fileName.substring(0, fileName.indexOf(".csv"));
                            }

                            writer.writeComment(fileName);
                            writer.writeComment("");
                            writer.writeComment("");
                            String[] header = null;
                            int reportIterationSize = 100;
                            if(constraintName.equals("auditLog") || constraintName.equals("auditLogApp") || constraintName.equals("auditLogAppTarget")
                                    || constraintName.equals("auditLogAppTargetOperation"))
                            {
                                reportIterationSize = deploymentUtil.getAuditLogreportIterationDataSize();
                            }
                            else if(constraintName.equals("groupScopeDescription"))
                            {
                                reportIterationSize = deploymentUtil.getScopeDetailsreportIterationDataSize();
                            }
                            else {
                                reportIterationSize = deploymentUtil.getReportIterationDataSize();
                            }
                            for (int i=1; i<=count; i=i+reportIterationSize) {
                                filters.put("first", Integer.valueOf(i).toString());
                                filters.put("max", Integer.valueOf(i+reportIterationSize-1).toString());
                                List<Map<String, Object>> customDataResponse = externalDbDal.getCustomData(scopeConstraint.getApplicationName(), sql, filters, constraintName, finalRestrictionIds);
                                if(i==1){
                                    header = customDataResponse.get(0).keySet().toArray(new String[0]);
                                    writer.writeHeader(header);
                                }
                                for(Map<String, Object> map:customDataResponse){
                                    writer.write(map, header);
                                }
                                customDataResponse = null;
                            }
                            writer.close();
                        }
                        catch(Exception e){
                            log.error("getCustomDataCSV; Exception={};", e);
                        }
                        finally{
                            reportSemaphore.release();
                        }
                    }
                };
                return ResponseEntity.ok().header("Expires",new Date().toString()).body(stream);
            }
        }
        return null;

    }


//    @GET
//    @Produces(javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM)
//    @Path("/{constraintName}/ExportPDFData")
    @GetMapping(path = "/{constraintName}/ExportPDFData",produces = MediaType.APPLICATION_OCTET_STREAM)
    @Operation(description = "ExportDataPDF",hidden = true)
    public ResponseEntity<StreamingResponseBody> getCustomDataPDF(
            @PathParam("constraintName") final String constraintName,
            HttpServletRequest request) throws Exception {

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        final ScopeConstraint scopeConstraint = scopeConstraintDal.getByScopeName(constraintName);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        final Map<String, String> filters = optionFilter.getFilters();
        final String fileName = filters.size() > 0 && filters.containsKey("fileName")?filters.get("fileName"):"";

        String tmpReportLocation=deploymentUtil.getTempReportFolder();
        if(tmpReportLocation.isEmpty()){ // if tempReportFolder is not configure then try creating a "temp" folder in RBAC install folder
            tmpReportLocation = new String(System.getProperty(RBACUtil.RBAC_CONF_DIR).concat("/../../temp"));
        }
        StringBuilder folderNameRandom = new StringBuilder().append("\\").append((RandomStringUtils.random(10, true, true)));//added to append a suffix to the file name generated
        final File fd = new File(tmpReportLocation.concat(folderNameRandom.toString()));//create a temporary folder at tempReportLocation
        log.trace("getCustomDataPDF; Report location is: {}", fd.getAbsolutePath());
        if(!fd.exists()){
            //log.info("Creating directory for reports at {}", tmpReportLocation);
            if(fd.isAbsolute())
            {
                fd.mkdirs();
            }
            else
            {
                log.error("getCustomDataPDF; deploymentUtil.tempReportFolder property in init.properties file is incorrect");
                throw new ErrorInfoException("reportGenerationError");
            }
        }
        if(!fd.isDirectory())
        {
            log.error("getCustomDataPDF; Please modify the deploymentUtil.tempReportFolder property in init.properties file ");
            throw new ErrorInfoException("reportGenerationError");
        }
        List<String> restrictionIds = new LinkedList<String>();
        if(constraintName.equals("auditLog") || constraintName.equals("auditLogCount") || constraintName.equals("accessMatrix") || constraintName.equals("accessMatrixCount")|| constraintName.equals("loginLog") || constraintName.equals("loginLogCount")
                || constraintName.equals("auditLogApp") || constraintName.equals("auditLogAppCount") || constraintName.equals("auditLogAppTarget") || constraintName.equals("auditLogAppTargetCount") || constraintName.equals("auditLogAppTargetOperation")
                || constraintName.equals("auditLogAppTargetOperationCount")){
            String userScopeQuery = uriInfo.get(RBACUtil.USER_SCOPE_QUERY) != null && uriInfo.get(RBACUtil.USER_SCOPE_QUERY).size() > 0 ? uriInfo.get(RBACUtil.USER_SCOPE_QUERY).get(0) : null;
            restrictionIds = scopeRestrictionUtil.handleForUserRestriction(userScopeQuery);
        }
        else if(constraintName.equals("accessMatrixGroup") || constraintName.equals("accessMatrixGroupCount") || constraintName.equals("groupScopeDescription") || constraintName.equals("groupScopeDescriptionCount") || constraintName.equals("userActivity") || constraintName.equals("userActivityCount")){
            String groupScopeQuery = uriInfo.get(RBACUtil.GROUP_SCOPE_QUERY) != null && uriInfo.get(RBACUtil.GROUP_SCOPE_QUERY).size() > 0 ? uriInfo.get(RBACUtil.GROUP_SCOPE_QUERY).get(0) : null;
            restrictionIds = scopeRestrictionUtil.handleForGroupRestriction(groupScopeQuery);
        }
        final List<String> finalRestrictionIds = restrictionIds;
        restrictionIds = null;
        if (scopeConstraint != null) {
            //check the no. of records present, this counter will be used to iterate over the records
            List<Map<String, Object>> countResponse = externalDbDal.getCustomData(scopeConstraint.getApplicationName(), scopeConstraintDal
                    .getByScopeName(reportCountMap.get(constraintName)).getSqlQuery(), filters, constraintName, finalRestrictionIds);
            final int recordsCount = Integer.parseInt(countResponse.get(0).get("Count").toString());
            if (recordsCount > 0) {
                StreamingResponseBody stream = new StreamingResponseBody() {
                    @Override
                    public void writeTo(OutputStream os) throws IOException,
                            WebApplicationException {
                        int pageCount = 1;
                        try {
                            int reportIterationSize = 100;
                            String xslFileName = constraintName;
                            if(constraintName.equals("auditLog") || constraintName.equals("auditLogApp") || constraintName.equals("auditLogAppTarget")
                                    || constraintName.equals("auditLogAppTargetOperation"))
                            {
                                reportIterationSize = deploymentUtil.getAuditLogreportIterationDataSize();
                                xslFileName = "auditLog";
                            }
                            else if(constraintName.equals("groupScopeDescription"))
                            {
                                reportIterationSize = deploymentUtil.getScopeDetailsreportIterationDataSize();
                            }
                            else {
                                reportIterationSize = deploymentUtil.getReportIterationDataSize();
                            }
                            reportSemaphore.tryAcquire(deploymentUtil.getReportSemaphoreTimeoutSecs(), TimeUnit.SECONDS);
                            PDFMergerUtility ut = new PDFMergerUtility();
                            ut.setDestinationStream(os);
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE dd-MMM-yyyy hh:mm:ss");
                            Date currDate = new Date();
                            String dateString = sdf.format(currDate);
                            //File fd = new File(System.getProperty(CONF_DIR).concat("/../../temp"));


                            if(filters.containsKey("userId") && filters.get("userId") != null && (filters.get("userId").equals("All") || filters.get("userId").contains(","))){
                                xslFileName += "All.xsl";
                            }else if(filters.containsKey("userId") && filters.get("userId") != null && !filters.get("userId").equals("All")){
                                xslFileName += "Single.xsl";
                            }if(filters.containsKey("groupId") && filters.get("groupId") != null && filters.get("groupId").equals("All")){
                                xslFileName += "All.xsl";
                            }else if(filters.containsKey("groupId") && filters.get("groupId") != null && !filters.get("groupId").equals("All")){
                                xslFileName += "Single.xsl";
                            }if(filters.containsKey("userName") && filters.get("userName") != null && filters.get("userName").equals("All")){
                                xslFileName += "All.xsl";
                            }else if(filters.containsKey("userName") && filters.get("userName") != null && !filters.get("userName").equals("All")){
                                xslFileName += "Single.xsl";
                            }

                            int maxAllowed = 1000;
                            if(constraintName.equals("auditLog") || constraintName.equals("auditLogApp") || constraintName.equals("auditLogAppTarget")
                                    || constraintName.equals("auditLogAppTargetOperation"))
                            {
                                maxAllowed = deploymentUtil.getAuditLogReportPDFMaxRecords();
                            }
                            else if(constraintName.equals("groupScopeDescription"))
                            {
                                maxAllowed = deploymentUtil.getScopeDetailsReportPDFMaxRecords();
                            }
                            else {
                                maxAllowed = deploymentUtil.getReportPDFMaxRecords();
                            }
                            for (int i=1; i<=maxAllowed && i<=recordsCount; i=i+reportIterationSize) {
                                if(i==1){
                                    StringBuilder pdfFileNameOrg = new StringBuilder();
                                    pdfFileNameOrg.append((fileName.indexOf("\\.") > -1 ? fileName.split("\\.")[0] : fileName)).append("_").append(RandomStringUtils.random(8, true, true)).append(".pdf");
                                    //RBAC-1308
                                    String pdfFileNameHash = DigestUtils.md5Hex(pdfFileNameOrg.toString()).toUpperCase()+".pdf";
                                    File f = new File(fd, pdfFileNameHash);
                                    FileOutputStream pdfFStream = new FileOutputStream(f);
                                    createPDFStream(fileName, constraintName, "docHeader.xsl", new ArrayList<Map<String, Object>>(), pdfFStream, pageCount, dateString, recordsCount, maxAllowed);
                                    ut.addSource(f);
                                    PDDocument doc = PDDocument.load(f);
                                    pageCount = pageCount + doc.getNumberOfPages();
                                    doc.close();
                                }
                                filters.put("first", Integer.valueOf(i).toString());
                                filters.put("max", Integer.valueOf(i+reportIterationSize-1).toString());
                                List<Map<String, Object>> subResp = externalDbDal.getCustomData(scopeConstraint.getApplicationName(), scopeConstraint.getSqlQuery(), filters, constraintName, finalRestrictionIds);
                                StringBuilder pdfFileNameOrg = new StringBuilder();
                                pdfFileNameOrg.append((fileName.indexOf("\\.") > -1 ? fileName.split("\\.")[0] : fileName)).append("_").append(RandomStringUtils.random(8, true, true)).append(".pdf");
                                //RBAC-1308
                                String pdfFileNameHash = DigestUtils.md5Hex(pdfFileNameOrg.toString()).toUpperCase()+".pdf";
                                File f = new File(fd, pdfFileNameHash);
                                FileOutputStream pdfFStream = new FileOutputStream(f);
                                createPDFStream(fileName, constraintName, xslFileName, subResp, pdfFStream, pageCount, dateString, recordsCount, maxAllowed);
                                subResp = null;
                                ut.addSource(f);
                                PDDocument doc = PDDocument.load(f);
                                pageCount = pageCount + doc.getNumberOfPages();
                                doc.close();
                            }
                            ut.setDestinationFileName(fileName);
                            ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

                        }
                        catch (ErrorInfoException ex)
                        {
                            throw ex;
                        }
                        catch (Exception e) {
                            log.error("getCustomDataPDF; exception e={}", e);
                        }
                        finally{
                            reportSemaphore.release();
                            try{
                                FileUtils.deleteDirectory(fd);
                            }
                            catch(Exception e){
                                log.info("getCustomDataPDF; file delete exception e={}", e);
                            }
                        }
                    }
                };
                return ResponseEntity.ok().header("Expires",new Date().toString()).body(stream);
            }
        }
        return null;
    }

    @GetMapping(path = "/scopeConstraintsCheck/{scopeId}/{groupId}",produces = MediaType.APPLICATION_JSON)
    public String[] checkConstraints(@PathVariable("scopeId") Integer scopeId,
                                     @PathVariable("groupId") Integer groupId) {
        // fetch this information from scopeConstraint table using
        // applicationId, scopeId & groupId
        return new String[] {};
    }

//    @GET
//    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @GetMapping(produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name ="scopeId", description = "scopeId", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name ="scopeName", description = "scopeName", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<ScopeConstraint[]> getConstraints(HttpServletRequest request) {

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0,
                Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(
                uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<ScopeConstraint> scopeConstraints = scopeConstraintDal
                .getConstraints(options);
        if(scopeConstraints!=null){
            for (ScopeConstraint sc : scopeConstraints) {
                sc.hideSqlQuery();
            }
            return ResponseEntity.ok(scopeConstraints.toArray(new ScopeConstraint[scopeConstraints
                    .size()]));
        }
        return ResponseEntity.ok(new ScopeConstraint[0]);
    }

//    @GET
//    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
//    @Path("/constructScopeDefinition/{scopeMap}")
    @GetMapping(path = "/constructScopeDefinition/{scopeMap}",produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name ="first", description = "first", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name ="max", description = "max", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name ="searchText", description = "searchText", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name ="tanentSelected", description = "tanentSelected", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public String constructScopeDefinition(
            @PathParam("scopeMap") String scopeMap) {

        return "";
    }

    public <K, V> void toXml(List<Map<String, Object>> list, Writer out, String fileName, String constraintName, String dateString)
            throws IOException, XMLStreamException {
        XMLStreamWriter xsw = null;

        try {
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            xsw = xof.createXMLStreamWriter(out);
            xsw.writeStartDocument("utf-8", "1.0");
            xsw.writeStartElement("data");
            xsw.writeAttribute("dateTime", dateString);
            fileName = fileName!= null?fileName:"";
            xsw.writeAttribute("fileName", fileName);
            for (Map<String, Object> map : list) {
                xsw.writeStartElement("entries");
                for (Map.Entry<String, Object> e : map.entrySet()) {
                    Object value = e.getValue();
                    String valueString = "";
                    if(value != null){
                        valueString = value.toString();
                        if(valueString.length()>deploymentUtil.getPdfReportMaxCellLength()){
                            valueString = valueString.substring(0, deploymentUtil.getPdfReportMaxCellLength())+deploymentUtil.getPdfReportCellTruncationMessage();
                        }
                        if(constraintName.equals("auditLog") || constraintName.equals("groupScopeDescription")){
                            //valueString = StringEscapeUtils.escapeJson(valueString);
                            //RBAC-613 Zero width space
                            valueString = insertPeriodically(valueString,"\u200B", ZERO_WIDTH_SPACE_PDF_CHARACTER_INTERVAL);
                            xsw.writeAttribute(e.getKey().toString().replaceAll(" ", "_"), valueString);
                        }else{
                            //RBAC-613 Zero width space
                            valueString = insertPeriodically(valueString,"\u200B", ZERO_WIDTH_SPACE_PDF_CHARACTER_INTERVAL);
                            xsw.writeAttribute(e.getKey().toString().replaceAll(" ", "_"), valueString);
                        }
                    }
                    else
                        xsw.writeAttribute(e.getKey().toString().replaceAll(" ", "_"), e.getValue() != null?e.getValue().toString():"");
                }
                xsw.writeEndElement();
            }
            xsw.writeEndDocument();
        }catch(Exception e){
            log.error("toXml exception e={}", e);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("toXml exception e={}", e);
                }
            }
            if (xsw != null) {
                try {
                    xsw.close();
                } catch (XMLStreamException e) {
                    log.error("toXml exception e={}", e);
                }
            }
        }
    }
    private boolean createPDFStream(String fileName, String constraintName, String xslFileName, List<Map<String, Object>> customDataResponse, FileOutputStream fout, int count, String dateString, int recordsCount, int maxAllowed){
        StringWriter sw = new StringWriter();
        try {
            xsltSemaphore.tryAcquire(deploymentUtil.getReportSemaphoreTimeoutSecs(), TimeUnit.SECONDS);
            if (!fileName.equals("")) {
                fileName = fileName.replaceAll("_All", "");
                fileName = fileName.replaceAll("_", " ");
                if (fileName.indexOf(".pdf") > -1)
                    fileName = fileName.substring(0,
                            fileName.indexOf(".pdf"));
            }

            toXml(customDataResponse, sw, fileName, constraintName, dateString);
            if(log.isTraceEnabled()){
                log.trace("createPDFStream(), content={}", sw.toString());
            }

            StreamSource transformSource = new StreamSource(getClass()
                    .getClassLoader().getResourceAsStream(xslFileName));


            String systemId = System.getProperty(RBACUtil.RBAC_CONF_DIR).concat("/reportTemplates/").concat(xslFileName);

            transformSource.setSystemId(systemId);
            FopFactory fopFactory = FopFactory.newInstance();
            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            fopFactory.setFontBaseURL(System.getProperty(RBACUtil.RBAC_CONF_DIR).concat("/fonts"));

            Configuration cfg = cfgBuilder.buildFromFile(new File(System.getProperty(RBACUtil.RBAC_CONF_DIR).concat("/reportFontConfig.xml")));
            fopFactory.setUserConfig(cfg);
            log.trace("createPDFStream(),before setting getFontBaseURL={}", fopFactory.getBaseURL());

            fopFactory.setStrictValidation(false);

            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            InputStream is = new ByteArrayInputStream(sw.toString().getBytes
                    (Charset.forName("UTF-8")));
            //null it & hope GC throws it away
            sw = null;
            StreamSource source = new StreamSource(is, "UTF-8");
            TransformerFactory transfact = TransformerFactory
                    .newInstance();

            Transformer xslfoTransformer = transfact
                    .newTransformer(transformSource);
            xslfoTransformer.setParameter("initPageNumber", count);
            xslfoTransformer.setParameter("recordsCount", recordsCount);
            xslfoTransformer.setParameter("maxAllowed", maxAllowed);
            Fop fop = fopFactory.newFop("application/pdf", foUserAgent,
                    fout);

            Result res = new SAXResult(fop.getDefaultHandler());

            xslfoTransformer.transform(source, res);

            fout.close();
            return true;
        } catch (Exception e) {
            log.error("createPDFStream exception e={}", e);
        }
        finally{
            xsltSemaphore.release();
        }
        return false;
    }

    public static String insertPeriodically(String text, String insert,
                                            int period) {
        StringBuilder builder = new StringBuilder(text.length()
                + insert.length() * (text.length() / period) + 1);

        int index = 0;
        String prefix = "";
        while (index < text.length()) {
            // Don't put the insert in the very first iteration.
            // This is easier than appending it *after* each substring
            builder.append(prefix);
            prefix = insert;
            builder.append(text.substring(index,
                    Math.min(index + period, text.length())));
            index += period;
        }
        return builder.toString();
    }
    /*Added By Pankaj For Global User Search*/

//    @GET
//    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
//    @Path("/{constraintName}/globalUserSearchCustomData")
    @GetMapping(path = "/{constraintName}/globalUserSearchCustomData",produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name ="first", description = "first", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name ="max", description = "max", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name ="searchText", description = "searchText", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name ="tanentSelected", description = "tanentSelected", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<StreamingResponseBody> getGlobalUserSearchCustomData(
            @PathParam("constraintName") String constraintName,
            HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception{

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        /*Added For Pagination*/
        Integer first=0;

        Integer max=0;


        if(uriInfo.get("first")!=null) {
            try {
                first=Integer.parseInt(uriInfo.get("first").get(0));
            }catch(Exception e) {

            }
        }

        if(uriInfo.get("max")!=null) {
            try {
                max=Integer.parseInt(uriInfo.get("max").get(0));
            }catch(Exception e) {

            }
        }

        OptionPage optionFilter = new OptionPage(
                uriInfo,first,max);
        Options options=new Options(optionFilter);

        String searchText="";

        String tanentSelected="";

        if(uriInfo.get("searchText")!=null) {

            searchText=uriInfo.get("searchText").get(0);
        }

        if(uriInfo.get("tanentSelected")!=null) {

            tanentSelected=uriInfo.get("tanentSelected").get(0);

        }

        Integer userId = Integer.parseInt(headers.get("userId").get(0));

        final List<Map<String, Object>> list = userDal.searchGlobalCustomUserInfo(searchText,tanentSelected,userId,options);

			/*String sql = scopeConstraint.getSqlQuery();
			final List<Map<String, Object>> customDataResponse = externalDbDal
					.getCustomData(scopeConstraint.getApplicationName(), sql,
							filters, constraintName, restrictionIds);
			if(log.isTraceEnabled()){
				log.trace("getCustomData; constraintDataArray={}",
					customDataResponse);
			}*/
        StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.beginArray();
                for (Map<String, Object> message : list) {
                    gson.toJson(message, Map.class, writer);
                }
                writer.endArray();
                writer.close();
            }
        };
        return ResponseEntity.status(HttpStatus.OK).contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(stream);
        //}
        //return null;
    }

    /*added by pankaj for global user search */
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/{constraintName}/globalUserSearchCustomDataCount")
    @GetMapping(value = "/{constraintName}/globalUserSearchCustomDataCount",produces = MediaType.APPLICATION_JSON)
    @Parameters({
            @Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),
            @Parameter(name ="searchText", description = "searchText", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name ="tanentSelected", description = "tanentSelected", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    public ResponseEntity<StreamingResponseBody> getGlobalUserSearchCustomDataCount(
            @PathParam("constraintName") String constraintName,
            HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception{

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        String searchText="";

        String tanentSelected="";

        if(uriInfo.get("searchText")!=null) {
            searchText=uriInfo.get("searchText").get(0);
            String regex = "[<>=%&#!;']";
            Pattern res = Pattern.compile(regex);
            Matcher mat = res.matcher(searchText);
            if(mat.find()){
                throw new ErrorInfoException("validationError", "Invalid Search Text");
            }
        }

        if(uriInfo.get("tanentSelected")!=null) {
            tanentSelected=uriInfo.get("tanentSelected").get(0);
            if(!tanentSelected.isEmpty()){
                String regex = "^[,0-9]+$";
                if(!Pattern.compile(regex).matcher(tanentSelected).matches()){
                    throw new ErrorInfoException("validationError", "Invalid Tenant");
                }
            }
        }

        Integer userId = Integer.parseInt(headers.get("userId").get(0));

        final List<Map<String, Object>> list = userDal.searchGlobalCustomUserInfoCount(searchText,tanentSelected,userId);

			/*String sql = scopeConstraint.getSqlQuery();
			final List<Map<String, Object>> customDataResponse = externalDbDal
					.getCustomData(scopeConstraint.getApplicationName(), sql,
							filters, constraintName, restrictionIds);
			if(log.isTraceEnabled()){
				log.trace("getCustomData; constraintDataArray={}",
					customDataResponse);
			}*/
        StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                Gson gson = new Gson();
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.beginArray();
                for (Map<String, Object> message : list) {
                    gson.toJson(message, Map.class, writer);
                }
                writer.endArray();
                writer.close();
            }
        };
        return ResponseEntity.status(HttpStatus.OK).contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(stream);
        //}
        //return null;
    }
    /*added by pankaj for global user search pdf export*/
//    @GET
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
//    @Path("/{constraintName}/ExportPDFDataGlobalUserSearch")
    @GetMapping(path = "/{constraintName}/ExportPDFDataGlobalUserSearch",produces = MediaType.APPLICATION_OCTET_STREAM)
    @Operation(description = "ExportPDFDataGlobalUserSearch",hidden = true)
    public ResponseEntity<StreamingResponseBody> getCustomDataPDFGlobalUserSearch(
            @PathParam("constraintName") final String constraintName,
            HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {

        String searchTextTemp="";

        String tanentSelectedTemp="";

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        if(uriInfo.get("searchText")!=null) {

            searchTextTemp=uriInfo.get("searchText").get(0);
        }

        if(uriInfo.get("tanentSelected")!=null) {

            tanentSelectedTemp=uriInfo.get("tanentSelected").get(0);

        }

        final String searchText=searchTextTemp;
        final String tanentSelected=tanentSelectedTemp;
        final Integer userId = Integer.parseInt(headers.get("userId").get(0));

        OptionFilter optionFilter = new OptionFilter(uriInfo);
        final Map<String, String> filters = optionFilter.getFilters();
        final String fileName = filters.size() > 0 && filters.containsKey("fileName")?filters.get("fileName"):"";

        String tmpReportLocation=deploymentUtil.getTempReportFolder();
        if(tmpReportLocation.isEmpty()){ // if tempReportFolder is not configure then try creating a "temp" folder in RBAC install folder
            tmpReportLocation = new String(System.getProperty(RBACUtil.RBAC_CONF_DIR).concat("/../../temp"));
        }
        StringBuilder folderNameRandom = new StringBuilder().append("\\").append((RandomStringUtils.random(10, true, true)));//added to append a suffix to the file name generated
        final File fd = new File(tmpReportLocation.concat(folderNameRandom.toString()));//create a temporary folder at tempReportLocation
        log.trace("getCustomDataPDF; Report location is: {}", fd.getAbsolutePath());
        if(!fd.exists()){
            //log.info("Creating directory for reports at {}", tmpReportLocation);
            if(fd.isAbsolute())
            {
                fd.mkdirs();
            }
            else
            {
                log.error("getCustomDataPDF; deploymentUtil.tempReportFolder property in init.properties file is incorrect");
                throw new ErrorInfoException("reportGenerationError");
            }
        }
        if(!fd.isDirectory())
        {
            log.error("getCustomDataPDF; Please modify the deploymentUtil.tempReportFolder property in init.properties file ");
            throw new ErrorInfoException("reportGenerationError");
        }


        //check the no. of records present, this counter will be used to iterate over the records
        List<Map<String, Object>> countResponse = userDal.searchGlobalCustomUserInfoCount(searchText,tanentSelected,userId);
        final int recordsCount = Integer.parseInt(countResponse.get(0).get("Count").toString());
        if (recordsCount > 0) {
            StreamingResponseBody stream = new StreamingResponseBody() {
                @Override
                public void writeTo(OutputStream os) throws IOException,
                        WebApplicationException {
                    int pageCount = 1;
                    try {
                        int reportIterationSize = 100;
                        if(constraintName.equals("globalUserSearch"))
                        {
                            reportIterationSize = deploymentUtil.getGlobalUserSearchreportIterationDataSize();
                        }
                        reportSemaphore.tryAcquire(deploymentUtil.getReportSemaphoreTimeoutSecs(), TimeUnit.SECONDS);
                        PDFMergerUtility ut = new PDFMergerUtility();
                        ut.setDestinationStream(os);
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd-MMM-yyyy hh:mm:ss");
                        Date currDate = new Date();
                        String dateString = sdf.format(currDate);
                        //File fd = new File(System.getProperty(CONF_DIR).concat("/../../temp"));

                        String xslFileName = constraintName;

                        if(searchText.trim().equals("")) {
                            xslFileName += "All.xsl";
                        }else {
                            xslFileName += "Single.xsl";
                        }

                        int maxAllowed = 1000;
                        if(constraintName.equals("globalUserSearch"))
                        {
                            maxAllowed = deploymentUtil.getGlobalUserSearchReportPDFMaxRecords();
                        }

                        for (int i=1; i<=maxAllowed && i<=recordsCount; i=i+reportIterationSize) {
                            if(i==1){
                                StringBuilder pdfFileNameOrg = new StringBuilder();
                                pdfFileNameOrg.append((fileName.indexOf("\\.") > -1 ? fileName.split("\\.")[0] : fileName)).append("_").append(RandomStringUtils.random(8, true, true)).append(".pdf");
                                //RBAC-1308
                                String pdfFileNameHash = DigestUtils.md5Hex(pdfFileNameOrg.toString()).toUpperCase()+".pdf";
                                File f = new File(fd, pdfFileNameHash);
                                FileOutputStream pdfFStream = new FileOutputStream(f);
                                createPDFStream(fileName, constraintName, "docHeader.xsl", new ArrayList<Map<String, Object>>(), pdfFStream, pageCount, dateString, recordsCount, maxAllowed);
                                ut.addSource(f);
                                PDDocument doc = PDDocument.load(f);
                                pageCount = pageCount + doc.getNumberOfPages();
                                doc.close();
                            }
                            filters.put("first", Integer.valueOf(i).toString());
                            filters.put("max", Integer.valueOf(i+reportIterationSize-1).toString());
                            List<Map<String, Object>> subResp = userDal.searchGlobalCustomUserInfo(searchText,tanentSelected,userId,new Options());
                            StringBuilder pdfFileNameOrg = new StringBuilder();
                            pdfFileNameOrg.append((fileName.indexOf("\\.") > -1 ? fileName.split("\\.")[0] : fileName)).append("_").append(RandomStringUtils.random(8, true, true)).append(".pdf");
                            //RBAC-1308
                            String pdfFileNameHash = DigestUtils.md5Hex(pdfFileNameOrg.toString()).toUpperCase()+".pdf";
                            File f = new File(fd, pdfFileNameHash);
                            FileOutputStream pdfFStream = new FileOutputStream(f);
                            createPDFStream(fileName, constraintName, xslFileName, subResp, pdfFStream, pageCount, dateString, recordsCount, maxAllowed);
                            subResp = null;
                            ut.addSource(f);
                            PDDocument doc = PDDocument.load(f);
                            pageCount = pageCount + doc.getNumberOfPages();
                            doc.close();
                        }
                        ut.setDestinationFileName(fileName);
                        ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

                    }
                    catch (ErrorInfoException ex)
                    {
                        throw ex;
                    }
                    catch (Exception e) {
                        log.error("getCustomDataPDF; exception e={}", e);
                    }
                    finally{
                        reportSemaphore.release();
                        try{
                            FileUtils.deleteDirectory(fd);
                        }
                        catch(Exception e){
                            log.info("getCustomDataPDF; file delete exception e={}", e);
                        }
                    }
                }
            };
            return ResponseEntity.status(HttpStatus.OK).contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .body(stream);
        }

        return null;
    }

    /*added by pankaj for global user search csv export*/

//    @GET
//    @Produces("application/vnd.ms-excel;charset=utf-8")
//    @Path("/{constraintName}/ExportCSVDataGlobalUserSearch")
    @GetMapping(path = "/{constraintName}/ExportCSVDataGlobalUserSearch",produces = "application/vnd.ms-excel;charset=utf-8")
    @Operation(description = "ExportCSVDataGlobalUserSearch",hidden = true)
    public ResponseEntity<StreamingResponseBody> getCustomDataCSVGlobalUserSearch(
            @PathParam("constraintName") final String constraintName,
            HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {

        String searchTextTemp="";

        String tanentSelectedTemp="";

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        if(uriInfo.get("searchText")!=null) {

            searchTextTemp=uriInfo.get("searchText").get(0);
        }

        if(uriInfo.get("tanentSelected")!=null) {

            tanentSelectedTemp=uriInfo.get("tanentSelected").get(0);

        }

        final String searchText=searchTextTemp;
        final String tanentSelected=tanentSelectedTemp;
        final Integer userId = Integer.parseInt(headers.get("userId").get(0));


        OptionFilter optionFilter = new OptionFilter(
                uriInfo);
        final Map<String, String> filters = optionFilter.getFilters();

        List<String> restrictionIds = new LinkedList<String>();

        //check the no. of records present, this counter will be used to iterate over the records
        List<Map<String, Object>> countResponse = userDal.searchGlobalCustomUserInfoCount(searchText,tanentSelected,userId);
        final int count = Integer.parseInt(countResponse.get(0).get("Count").toString());

        if (count > 0) {
            StreamingResponseBody stream = new StreamingResponseBody()  {
                @Override
                public void writeTo(OutputStream os) throws IOException, WebApplicationException
                {
                    try{
                        reportSemaphore.tryAcquire(deploymentUtil.getReportSemaphoreTimeoutSecs(), TimeUnit.SECONDS);
                        ICsvMapWriter writer = null;
                        byte[] enc = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
                        os.write(enc);
                        writer = new CsvMapWriter(new OutputStreamWriter(os, "UTF-8"), CsvPreference.EXCEL_PREFERENCE);

                        String fileName = filters.size() > 0 && filters.containsKey("fileName") ? filters.get("fileName") : "";
                        if (!fileName.equals("")) {
                            fileName = fileName.replaceAll("_All", "");
                            fileName = fileName.replaceAll("_", " ");
                            if (fileName.indexOf(".csv") > -1)
                                fileName = fileName.substring(0, fileName.indexOf(".csv"));
                        }

                        writer.writeComment(fileName);
                        writer.writeComment("");
                        writer.writeComment("");
                        String[] header = null;
                        int reportIterationSize = 100;
                        if(constraintName.equals("globalUserSearch"))
                        {
                            reportIterationSize = deploymentUtil.getGlobalUserSearchreportIterationDataSize();
                        }
                        for (int i=1; i<=count; i=i+reportIterationSize) {
                            filters.put("first", Integer.valueOf(i).toString());
                            filters.put("max", Integer.valueOf(i+reportIterationSize-1).toString());
                            List<Map<String, Object>> customDataResponse = userDal.searchGlobalCustomUserInfo(searchText,tanentSelected,userId,new Options());
                            if(i==1){
                                header = customDataResponse.get(0).keySet().toArray(new String[0]);
                                writer.writeHeader(header);
                            }
                            for(Map<String, Object> map:customDataResponse){
                                writer.write(map, header);
                            }
                            customDataResponse = null;
                        }
                        writer.close();
                    }
                    catch(Exception e){
                        log.error("getCustomDataCSV; Exception={};", e);
                    }
                    finally{
                        reportSemaphore.release();
                    }
                }
            };
             return ResponseEntity.status(HttpStatus.OK).contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(stream);
        }

        return null;

    }
}
