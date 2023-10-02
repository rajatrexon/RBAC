package com.esq.rbac.service.makerchecker.rest;

import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.exception.ErrorInfo;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.makerchecker.contraints.MakerCheckerConstraints;
import com.esq.rbac.service.makerchecker.domain.MakerChecker;
import com.esq.rbac.service.makerchecker.repository.MakerCheckerRepository;
import com.esq.rbac.service.makerchecker.service.MakerCheckerDal;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.validation.annotation.ValidationRules;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping(value = "/makerChecker")
public class MakerCheckerRest {

    private MakerCheckerDal makerCheckerDal;

    @Autowired
    private MakerCheckerRepository checkerRepository;

    private Validator validator;
    private DeploymentUtil deploymentUtil;

    @Autowired
    public void setMakerChecker(MakerCheckerDal makerCheckerDal, DeploymentUtil deploymentUtil) {
        log.trace("setMakerChecker; {}", makerCheckerDal);
        this.makerCheckerDal = makerCheckerDal;
        this.deploymentUtil = deploymentUtil;
    }

    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }


    @GetMapping(value = "/{makerCheckerId}", produces = MediaType.APPLICATION_JSON)
    public Object getById(@PathVariable("makerCheckerId") int makerCheckerId) {
        log.trace("getById; makerCheckerId={}", makerCheckerId);
        return makerCheckerDal.getEntityByMakerCheckerId(makerCheckerId);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON)
    public List<MakerChecker> list() {
        return checkerRepository.findAll();
    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response list(@Context UriInfo uriInfo) {
//        log.trace("list; requestUri={}", uriInfo.getRequestUri());
//
//        OptionPage optionPage = new OptionPage(uriInfo.getQueryParameters(), 0, Integer.MAX_VALUE);
//        OptionSort optionSort = new OptionSort(uriInfo.getQueryParameters());
//        OptionFilter optionFilter = new OptionFilter(uriInfo.getQueryParameters());
//        Options options = new Options(optionPage, optionSort, optionFilter);
//        List<MakerChecker> list = new ArrayList<MakerChecker>();
//        if (uriInfo.getQueryParameters().containsKey(SearchUtils.SEARCH_PARAM)) {
//            list = makerCheckerDal.searchList(options);
//        } else {
//            list = makerCheckerDal.getList(options);
//        }
//
//        log.info("=================IN APP LAYER======================");
//
//        MakerChecker[] array = new MakerChecker[list.size()];
//        list.toArray(array);
//        log.info("list = {} ",list);
//        return Response.ok().entity(array).expires(new Date()).build();
//    }
//

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "loggedInTenant", description = "loggedInTenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "entityName", description = "entityName", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "makerCheckerIdForAction", description = "makerCheckerIdForAction", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, description = "makercheckerId", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "entityToShow", description = "entityName", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "type", description = "type", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "loggedInUserName", description = "loggedInUserName", in = ParameterIn.QUERY, schema = @Schema(type = "string")),})
    public int count(@RequestHeader org.springframework.http.HttpHeaders headers, HttpServletRequest request) {
        log.trace("count; requestUri={}", request.getRequestURI());

        Map<String, String[]> parameterMap = request.getParameterMap();
        log.trace("list; requestUri={}", request.getRequestURI());
        System.out.println("inside the get all user api...");

        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        String loggedInTenant = headers.get("loggedInTenant").get(0);
        optionFilter.addFilter("loggedInTenant", loggedInTenant);
        Options options = new Options(optionSort, optionFilter);

        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return makerCheckerDal.getSearchCount(options);
        } else {
            return makerCheckerDal.getCount(options);
        }
    }

    @GetMapping(value = "/validationRules", produces = MediaType.APPLICATION_JSON)
    public ValidationRules getValidationRules() {
        ValidationRules validationRules = new ValidationRules();
        validationRules.getFieldRulesList().addAll(ValidationUtil.retrieveValidationRules(User.class));
        return validationRules;
    }

    @GetMapping(value = "/customMakerCheckerInfo", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "loggedInTenant", description = "loggedInTenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "entityName", description = "entityName", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "makerCheckerIdForAction", description = "makerCheckerIdForAction", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, description = "makercheckerId", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "entityToShow", description = "entityName", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "type", description = "type", in = ParameterIn.QUERY, schema = @Schema(type = "string")), @Parameter(name = "loggedInUserName", description = "loggedInUserName", in = ParameterIn.QUERY, schema = @Schema(type = "string")),})
    public ResponseEntity<String> customMakerCheckerInfo(@RequestHeader HttpHeaders headers, HttpServletRequest request) {
        log.trace("getCustomUserInfo; requestUri={}", request.getRequestURI());
        String loggedInTenant = headers.get("loggedInTenant").get(0);
        try {
            if (!Lookup.checkMakerCheckerEnabledInTenant(Long.parseLong(loggedInTenant)) && !deploymentUtil.getIsMakercheckerActivated()) {
                //  return Response.ok().entity(null).expires(new Date()).build();
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        optionFilter.addFilter("loggedInTenant", loggedInTenant);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            list = makerCheckerDal.searchMakerCheckerInfo(options);
        } else {
            list = makerCheckerDal.getMakerCheckerInfo(options);
        }
        return ResponseEntity.ok().body(new Gson().toJson(list));
    }

    @GetMapping(value = "/checkEntityPermission", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> checkEntityPermission(HttpServletRequest request) {

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionFilter);
        return ResponseEntity.ok(makerCheckerDal.checkEntityPermission(Integer.parseInt(optionFilter.getFilter(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER)), options));
    }

    @GetMapping(value = "/checkEntityPermissionForEntity", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> checkEntityPermissionForEntity(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionFilter);
        return ResponseEntity.ok(makerCheckerDal.checkEntityPermissionForEntity(options));
    }

    @PostMapping(value = "/approveOrRejectMakerCheckerEntity", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "loggedInTenant", description = "loggedInTenantId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "clientIp", description = "clientIp", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<Integer> approveOrRejectMakerCheckerEntity(@RequestHeader org.springframework.http.HttpHeaders headers, HttpServletRequest request, MakerChecker objMakerChecker) {
        /* Added for validation */
        MakerCheckerConstraints makerChecker = new MakerCheckerConstraints();
        makerChecker.setRejectReason(objMakerChecker.getRejectReason());
        validate(makerChecker);
        log.info("invalidate; requestUri={}", request.getRequestURI());

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        String clientIp = headers.get("clientIp").get(0);
        Integer userId = Integer.parseInt(headers.get("userId").get(0));
        List<String> loggedInTenant = headers.get("loggedInTenant");
        Long tenantId = 100L;
        if (loggedInTenant != null && loggedInTenant.size() > 0) tenantId = Long.parseLong(loggedInTenant.get(0));
        return makerCheckerDal.approveOrRejectMakerCheckerEntity(options, objMakerChecker, userId, clientIp, tenantId);
    }

    @GetMapping(value = "/getHistoryOfMakerChecker/{id}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<String> getHistoryOfMakerChecker(@PathVariable("id") int makerCheckerId) {
        List<Map<String, Object>> list = makerCheckerDal.getHistoryByMakerCheckerId(makerCheckerId);
        return ResponseEntity.ok().body(new Gson().toJson(list));
    }

    private void validate(MakerCheckerConstraints makerChecker) {
        Set<ConstraintViolation<MakerCheckerConstraints>> violations = validator.validate(makerChecker);
        if (violations.size() > 0) {
            log.warn("validate; violations={}", violations);

            ConstraintViolation<MakerCheckerConstraints> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException(ErrorInfo.XSS_ERROR_CODE, v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }
}
