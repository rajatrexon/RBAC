package com.esq.rbac.service.organization.organizationattribte.rest;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.calendar.service.CalendarDal;
import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.organization.embedded.OrganizationAttributeInfo;
import com.esq.rbac.service.organization.embedded.OrganizationAttributeWithTenant;
import com.esq.rbac.service.organization.organizationattribte.domain.OrganizationAttribute;
import com.esq.rbac.service.organization.organizationattribte.service.OrganizationAttributeDal;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.AuditLogger;
import com.esq.rbac.service.util.OrganizationAttributeUtil;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping(value = "/organizationAttributes")
public class OrganizationAttributeRest {

    private OrganizationAttributeDal organizationAttributeDal;
    private CalendarDal calendarDal;

    private AuditLogger auditLogger;
    private Validator validator;

    private UserDal userDal;

    @Autowired
    public void setUserDal(UserDal userDal) {
        this.userDal = userDal;
    }

    @Autowired
    public void setOrganizationAttributeDal(OrganizationAttributeDal organizationAttributeDal) {
        log.trace("setVariableDal;");
        this.organizationAttributeDal = organizationAttributeDal;
    }

    @Autowired
    public void setCalendarDal(CalendarDal calendarDal) {
        this.calendarDal = calendarDal;
    }

    @Autowired
    public void setAuditLogger(AuditLogService auditLogDal) {
        log.trace("setAuditLogger;");
        this.auditLogger =  new AuditLogger(auditLogDal);
    }

    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }

    @PostMapping(consumes =MediaType.APPLICATION_JSON_VALUE, produces =MediaType.APPLICATION_JSON_VALUE)
    public OrganizationAttributeInfo create(@RequestBody OrganizationAttributeInfo organizationAttributeInfo) throws Exception {
        log.debug("create; organizationAttribute={}", organizationAttributeInfo);
        OrganizationAttribute organizationAttribute = organizationAttributeDal.toOrganizationAttribute(organizationAttributeInfo);
        validate(organizationAttribute);
        //scopeBuilder.refreshScopeBuilder();
        organizationAttribute.setTimezone(calendarDal.getTimeZoneByOrganizationId(organizationAttribute.getOrganizationId()));
        return OrganizationAttributeUtil.fromOrganizationAttribute(organizationAttributeDal.create(organizationAttribute));


    }

    private void validate(OrganizationAttribute organizationAttribute) {
        Set<ConstraintViolation<OrganizationAttribute>> violations = validator.validate(organizationAttribute);
        if (violations.size() > 0) {
            log.debug("OrganizationAttributeRest; validationResult={}", violations);

            ConstraintViolation<OrganizationAttribute> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError",v.getMessage());
            e.getParameters().put("value", v.getMessage()+" in "+v.getPropertyPath());
            throw e;
        }
    }

    @DeleteMapping(produces =MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody OrganizationAttributeInfo organizationAttributeInfo) throws Exception {
        log.debug("delete; organizationAttribute={}", organizationAttributeInfo);
        OrganizationAttribute organizationAttribute = organizationAttributeDal.toOrganizationAttribute(organizationAttributeInfo);
        organizationAttributeDal.delete(organizationAttribute);
    }


    @PostMapping(value = "/{organizationId}" ,consumes =MediaType.APPLICATION_JSON_VALUE, produces =MediaType.APPLICATION_JSON_VALUE)
    public OrganizationAttributeInfo[] createOrganizationAttributes(@RequestBody OrganizationAttributeInfo[] organizationAttributeInfoArray, @PathParam("organizationId") Long organizationId, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("createOrganizationAttributes; organizationAttributeInfoArray={}", organizationAttributeInfoArray!=null? Arrays.asList(organizationAttributeInfoArray):null);
        synchronized (this) {

            Boolean isValid = true;

            List<OrganizationAttribute> organizationAttributes=new ArrayList<OrganizationAttribute>();

            if(organizationAttributeInfoArray!=null && organizationAttributeInfoArray.length>0){
                for(OrganizationAttributeInfo organizationAttributeInfo:organizationAttributeInfoArray){

                    Code code=organizationAttributeInfo.getCode();

                    if(code!=null) {

                        isValid= Pattern.matches(code.getValidationRegex(), organizationAttributeInfo.getAttributeValue().trim().toString());

                    }


                    if(!isValid) {

                        ErrorInfoException errorInfo = new ErrorInfoException("validationError","invalid attribute value");
                        if(code.getRemarks()!=null && !code.getRemarks().isEmpty()) {
                            errorInfo.getParameters().put("value", "Attribute Name " + organizationAttributeInfo.getAttributeName()  +" value is not valid " + organizationAttributeInfo.getAttributeValue() +". Hint: "+code.getRemarks());
                        }else {
                            errorInfo.getParameters().put("value", "Attribute Name " + organizationAttributeInfo.getAttributeName()  +" value is not valid " + organizationAttributeInfo.getAttributeValue() +". Valid regex is "+code.getValidationRegex());
                        }

                        throw errorInfo;


                    }
                    OrganizationAttribute organizationAttribute = organizationAttributeDal.toOrganizationAttribute(organizationAttributeInfo);

                    validate(organizationAttribute);
                    organizationAttribute.setTimezone(calendarDal.getTimeZoneByOrganizationId(organizationAttribute.getOrganizationId()));

                    organizationAttributes.add(organizationAttribute);



                }
            }

            if(isValid)
            {
                //delete all
                organizationAttributeDal.deleteByOrganizationId(organizationId);

                for(OrganizationAttribute organizationAttribute:organizationAttributes) {
                    organizationAttributeDal.create(organizationAttribute);
                }

                //create all
            }


            return organizationAttributeInfoArray;
        }

    }

    @PutMapping(consumes =MediaType.APPLICATION_JSON_VALUE, produces =MediaType.APPLICATION_JSON_VALUE)
    public OrganizationAttributeInfo update(@RequestBody OrganizationAttributeInfo organizationAttributeInfo) throws Exception {
        log.debug("update; OrganizationAttributeInfo={}", organizationAttributeInfo);
        OrganizationAttribute organizationAttribute = organizationAttributeDal.toOrganizationAttribute(organizationAttributeInfo);
        validate(organizationAttribute);
        organizationAttribute.setTimezone(calendarDal.getTimeZoneByOrganizationId(organizationAttribute.getOrganizationId()));
        //scopeBuilder.refreshScopeBuilder();
        organizationAttribute.setTimezone(calendarDal.getTimeZoneByOrganizationId(organizationAttribute.getOrganizationId()));
        return OrganizationAttributeUtil.fromOrganizationAttribute(organizationAttributeDal.update(organizationAttribute));
    }

    @Parameters({
            @Parameter(name="attributeName", description="attributeName", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name="applicationName", description="applicationName", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name="organizationId", description="organizationId", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public OrganizationAttributeInfo[] list(HttpServletRequest httpRequest) {
        log.trace("list; requestUri={}", httpRequest.getRequestURI());

        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(
                uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(
                uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<OrganizationAttributeInfo> list = organizationAttributeDal.getList(options);

        OrganizationAttributeInfo[] array = new OrganizationAttributeInfo[list.size()];
        list.toArray(array);

        return array;
    }

    @Parameters({
            @Parameter(name="appKey", description="appKey", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name="organizationId", description="organizationId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    @GetMapping(value = "/organization" ,produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrganizationAttributeWithTenant> getListForOrganizationAttributeWithTenant(HttpServletRequest httpRequest) throws Exception {
        OrganizationAttributeWithTenant organizationAttributeWithTenant=null;

        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultivaluedMap<String,String> multivaluedMap = new MultivaluedHashMap<>();
        parameterMap.forEach((key,values)->multivaluedMap.addAll(key,Arrays.asList(values)));
       // MultivaluedMap<String, String> multivaluedMap=uriInfo.getQueryParameters();

        String organizationId=null;

        if(multivaluedMap.get("organizationId")!=null && !multivaluedMap.get("organizationId").isEmpty()) {
            try {
                organizationId=multivaluedMap.get("organizationId").get(0);
            }catch(Exception e) {

                throw new ErrorInfoException("Error while getting organizationId ");
            }

        }
        String appKey= null;

        try {
            appKey= multivaluedMap.get("appKey").get(0);
        }catch(Exception e) {
            throw new ErrorInfoException("Error while getting appKey ");
        }

        if(appKey ==null ||  appKey.isEmpty()) {

            throw new ErrorInfoException("appKey is mandatory");
        }

        organizationAttributeWithTenant=new OrganizationAttributeWithTenant();

        try {
            organizationAttributeWithTenant.setOrganizationId(Long.parseLong(organizationId));
        }catch(Exception e) {

            throw new ErrorInfoException("Error while getting organizationId ");
        }

        try {
            organizationAttributeWithTenant.setAppKey(appKey);
        }catch(Exception e) {

            throw new ErrorInfoException("Error while getting appKey ");
        }

        return organizationAttributeDal.getListForOrganizationAttributeWithTenant(organizationAttributeWithTenant);


    }

    @GetMapping(value = "/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrganizationAttributeWithTenant> getOrganizationAttributes(HttpServletRequest httpRequest) throws Exception {
        Long organizationId = null;
        String appKey = null;

        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        log.debug("getOrganizationAttributes; filters {}", filters);
        if (filters != null) {
            String orgId = filters.get("orgId");
            if (orgId != null && orgId.length() > 0) {
                organizationId = Long.parseLong(orgId);
            }
            String userId = filters.get("userId");
            if (organizationId == null && userId != null && !userId.isEmpty()) {
                User user = userDal.getById(Integer.parseInt(userId));
                organizationId = user.getOrganizationId();

            }
            String userName = filters.get("userName");
            if (organizationId == null && userName != null && !userName.isEmpty()) {
                User user = userDal.getByUserName(userName);
                organizationId = user.getOrganizationId();

            }
            String appKeyReq = filters.get("appKey");
            if (appKeyReq != null && !appKeyReq.isEmpty()) {
                appKey = appKeyReq;

            }
        }
        if (organizationId == null)
            throw new ErrorInfoException("Cannot find organization Id in the request");

        OrganizationAttributeWithTenant organizationAttributeWithTenant = new OrganizationAttributeWithTenant();
        organizationAttributeWithTenant.setOrganizationId(organizationId);
        organizationAttributeWithTenant.setAppKey(appKey);
        return organizationAttributeDal.getListForOrganizationAttributeWithTenant(organizationAttributeWithTenant);
    }
}

