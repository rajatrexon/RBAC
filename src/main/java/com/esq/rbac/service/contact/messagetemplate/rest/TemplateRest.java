package com.esq.rbac.service.contact.messagetemplate.rest;

import com.esq.rbac.service.base.error.RestErrorMessages;
import com.esq.rbac.service.base.exception.RestException;
import com.esq.rbac.service.base.rest.BaseRest;
import com.esq.rbac.service.contact.helpers.ContactUserRest;
import com.esq.rbac.service.contact.messagetemplate.domain.MessageTemplate;
import com.esq.rbac.service.contact.messagetemplate.repository.TemplateRepository;
import com.esq.rbac.service.exception.ErrorInfo;
import com.esq.rbac.service.targetoperations.TargetOperations;
import com.esq.rbac.service.util.ContactAuditUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/template")
public class TemplateRest extends BaseRest<MessageTemplate> {

    protected static final String PARAM_APP_KEY_DEFAULT = "ALL";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String TEMPLATE_TYPE = "templateType";
    private static final Set<String> FILTER_COLUMNS;
    private static final Set<String> ORDER_COLUMNS;
    private static final Set<String> SEARCH_COLUMNS;
    private static final String PARAM_APP_KEY = "appKey";
    private static final String APPKEY = "appKey";

    static {
        FILTER_COLUMNS = new HashSet<>(Arrays.asList(NAME, DESCRIPTION, TEMPLATE_TYPE, APPKEY));

        ORDER_COLUMNS = new HashSet<String>(Arrays.asList(ID, NAME, DESCRIPTION, TEMPLATE_TYPE, PARAM_TENANT_ID, APPKEY));

        SEARCH_COLUMNS = new HashSet<String>(Arrays.asList(NAME, DESCRIPTION, TEMPLATE_TYPE, APPKEY));
    }

    private ContactUserRest userRest;
    private final TemplateRepository templateRepository;

    @Autowired
    public TemplateRest(TemplateRepository templateRepository) {
        super(MessageTemplate.class, templateRepository);
        this.templateRepository = templateRepository;
    }


    @Autowired
    public void setUserRest(ContactUserRest userRest) {
        log.trace("setUserRest; ");
        this.userRest = userRest;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<MessageTemplate> create(@RequestBody MessageTemplate template, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        int nameResult = 0;
        try {
            nameResult = templateRepository.templateNameSearch(template.getName().trim(), template.getTenantId());
        } catch (Exception e1) {
            log.warn("create;exception={}", e1.getMessage());
        }
        if (nameResult != 0) {
            log.error("create;Failed to create template, ({ }) already exist", template.getName().trim());
            logException("create;exception={}", new RestException(RestErrorMessages.CREATE_TEMPLATE_FAILED, "Failed to create resource"));
            throw new RestException(RestErrorMessages.CREATE_TEMPLATE_FAILED, "Failed to create resource", template.getName().trim());
        }
        ResponseEntity<MessageTemplate> response = super.create(template);
        MessageTemplate createdTemplate = super.readById(template.getId()).getBody();
        log.info("create; response={}", response);
        String trimJsonDefinition = createdTemplate.getJsonDefinition();
        String jsonDefinition = trimJsonDefinition.replace("\"", "");
        createdTemplate.setJsonDefinition(jsonDefinition);
        try {
            userRest.createAuditLog(TargetOperations.TEMPLATE_TARGET_NAME, TargetOperations.CREATE_OPERATION, ContactAuditUtil.convertToJSON(createdTemplate, TargetOperations.CREATE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("create;exception={}", e);
        }
        return response;
    }

    @PutMapping(value = "{id}", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<MessageTemplate> update(@PathVariable("id") long id,@RequestBody MessageTemplate template, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        int nameResult = 0;
        MessageTemplate savedTemplate = super.readById(id).getBody();
        if (!(savedTemplate.getName().trim().equalsIgnoreCase(template.getName().trim())) && !(savedTemplate.getChannelId() == template.getChannelId())) {
            try {
                nameResult = templateRepository.templateNameSearch(template.getName().trim(), template.getTenantId());
            } catch (Exception e1) {
                log.warn("update;exception={}", e1.getMessage());
            }
            if (nameResult != 0) {
                log.error("update;Failed to update template, ({ }) already exist", template.getName().trim());
                logException("update;exception={}", new RestException(RestErrorMessages.UPDATE_TEMPLATE_FAILED, "Failed to update resource"));
                throw new RestException(RestErrorMessages.UPDATE_TEMPLATE_FAILED, "Failed to update resource", template.getName().trim());
            }
        }
        ResponseEntity<MessageTemplate> response = null;

        String trimSavedJsonDefinition = savedTemplate.getJsonDefinition();
        String savedJsonDefinition = trimSavedJsonDefinition.replace("\"", "");
        savedTemplate.setJsonDefinition(savedJsonDefinition);
        response = super.update(id, template);
        MessageTemplate newTemplate = super.readById(id).getBody();
        String trimNewJsonDefinition = newTemplate.getJsonDefinition();
        String newJsonDefinition = trimNewJsonDefinition.replace("\"", "");
        newTemplate.setJsonDefinition(newJsonDefinition);
        try {
            userRest.createAuditLog(TargetOperations.TEMPLATE_TARGET_NAME, TargetOperations.UPDATE_OPERATION, ContactAuditUtil.compareObject(savedTemplate, newTemplate), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("update;exception={}", e);
        }
        return response;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Object[]> list(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) {
        //MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> queryParams.addAll(key, Arrays.asList(values)));

        List<String> templateTenantIdList = queryParams.get("templateTenantIdList");
        try {
            if (queryParams.containsKey(IS_TENANT_VISIBLE)) {
                String q = queryParams.getFirst(PARAM_Q);
                String asc = getParameterSingle(queryParams, PARAM_ASCENDING, null);
                String desc = getParameterSingle(queryParams, PARAM_DESCENDING, null);
                int first = getParameterSingle(queryParams, PARAM_FIRST, PARAM_FIRST_DEFAULT);
                int max = getParameterSingle(queryParams, PARAM_MAX, PARAM_MAX_DEFAULT);
                String tenantScope = getTenantScope(queryParams, headers);
                String appKey = getParameterSingle(queryParams, PARAM_APP_KEY, null);

                boolean isTenantVisible = getParameterSingle(queryParams, IS_TENANT_VISIBLE, null).equalsIgnoreCase("true");
                List<MessageTemplate> result = templateRepository.templateSearchWithTenantName(q, asc, desc, first, max, isTenantVisible, getTenantData(tenantScope));
                queryParams.remove(IS_TENANT_VISIBLE);
                MessageTemplate[] array = new MessageTemplate[result.size()];
                array = result.toArray(array);
                // return Response.ok().entity(a).cacheControl(cacheControl).expires(new Date()).build();
                return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).header("Expires", new Date().toString()).body(array);
            }
        } catch (RestException e) {
            logException("list", e);
            throw new RestException(RestErrorMessages.LIST_FAILED, "List resource");
        } catch (Exception e) {
            logException("Exception", e);
            new ErrorInfo(ErrorInfo.INTERNAL_ERROR);
        }
        log.debug("list, templateTenantIdList={}", templateTenantIdList);
        if (templateTenantIdList != null && !templateTenantIdList.isEmpty()) {
            log.debug("list, for shared host templateTenantIdList={}", templateTenantIdList);
            headers.get(PARAM_TENANT_SCOPE).add(0, templateTenantIdList.toString());
            queryParams.remove("templateTenantIdList");
            return super.list(request, headers);
        } else {
            return super.list(request, headers);
        }
    }

    @GetMapping(value = "/withChannel", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<List<MessageTemplate>> queryTemplateWithChannel(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers, @QueryParam("tenantId") Long tenantId, @QueryParam("hostTenantId") Long hostTenantId, @QueryParam("channel") String channel, @QueryParam("appKey") String appKey) throws Exception {
        List<MessageTemplate> queryTemplateList;
        if (tenantId != null && hostTenantId != null) {
            log.debug("queryTemplateWithChannel, tenantId={},hostTenantId={},channel={}, appKey={}}", tenantId, hostTenantId, channel, appKey);
            queryTemplateList = templateRepository.queryTemplateWithChannelType(channel, appKey, tenantId, hostTenantId);
        } else {
            String tenantScope = getTenantScope(null, headers);
            long[] tenantArr = getTenantData(tenantScope);
            log.debug("queryTemplateWithChannel, tenantArr={},channel={}, appKey={}", tenantArr, channel, appKey);
            queryTemplateList = templateRepository.queryTemplateWithChannelType(channel, appKey, tenantArr);
        }
        MessageTemplate[] queryTemplateArray = new MessageTemplate[queryTemplateList.size()];
        queryTemplateList.toArray(queryTemplateArray);
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).header("Expires", new Date().toString()).body(queryTemplateList);
    }

    @GetMapping(value = "/withAddressType", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<List<MessageTemplate>> queryTemplate(@QueryParam("addressType") String addressType, @QueryParam("tenantId") String tenantId) throws Exception {
        log.debug("queryTemplate; addressType={}", addressType);
        log.debug("queryTemplate; tenantId={}", tenantId);
        List<MessageTemplate> queryTemplateList = templateRepository.queryTemplateWithAddressType(addressType, Long.parseLong(tenantId));
        MessageTemplate[] queryTemplateArray = new MessageTemplate[queryTemplateList.size()];
        queryTemplateList.toArray(queryTemplateArray);
        return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(queryTemplateList);

    }

    @DeleteMapping(value = "/{id}")
    public void deleteById(@PathVariable("id") long id, @RequestHeader org.springframework.http.HttpHeaders headers) {
        ResponseEntity<MessageTemplate> res = super.readById(id);
        Boolean isMapped = templateRepository.isTemplateUsedInMapping(id);
        if (Boolean.TRUE.equals(isMapped)) {
            RestException r = new RestException("templateMappingFound", "templateMappingFound", CUSTOM_ERROR_HANDLING_PARAM);
            throw r;
        }
        MessageTemplate objectTemplate = res.getBody();
        String trimJsonDefinition = objectTemplate.getJsonDefinition();
        String jsonDefinition = trimJsonDefinition.replace("\"", "");
        objectTemplate.setJsonDefinition(jsonDefinition);
        synchronized (this) {
            super.deleteById(id);
        }
        try {
            userRest.createAuditLog(TargetOperations.TEMPLATE_TARGET_NAME, TargetOperations.DELETE_OPERATION, ContactAuditUtil.convertToJSON(objectTemplate, TargetOperations.DELETE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("deleteById;exception={}", e);
        }
    }

    @Override
    protected Set<String> getFilterColumns() {
        return FILTER_COLUMNS;
    }

    @Override
    protected Set<String> getSearchColumns() {
        return SEARCH_COLUMNS;
    }

    @Override
    protected Set<String> getOrderColumns() {
        return ORDER_COLUMNS;
    }
}

