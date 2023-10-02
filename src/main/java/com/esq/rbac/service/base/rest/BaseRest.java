package com.esq.rbac.service.base.rest;
/*
 * Copyright ©2012 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import com.esq.rbac.service.base.error.RestErrorMessages;
import com.esq.rbac.service.base.exception.RestException;
import com.esq.rbac.service.base.maskentity.MaskEntity;
import com.esq.rbac.service.base.query.Query;
import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.base.security.SecurityContext;
import com.esq.rbac.service.base.security.UserInfo;
import com.esq.rbac.service.base.vo.Count;
import com.esq.rbac.service.base.vo.EntityTypePermission;
import com.esq.rbac.service.commons.ValidationUtil;
import com.esq.rbac.service.validation.annotation.ValidationRules;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
public abstract class BaseRest<EntityType> {

    protected static final String PARAM_Q = "q";
    protected static final String PARAM_FIRST = "first";
    protected static final String PARAM_MAX = "max";
    protected static final String IS_TENANT_VISIBLE = "isTenantVisible";
    protected static final String PARAM_ASCENDING = "asc";
    protected static final String PARAM_DESCENDING = "desc";
    protected static final String PARAM_TENANT_SCOPE = "tenantIdList";
    protected static final String PARAM_TENANT_ID = "tenantId";
    protected static final int PARAM_FIRST_DEFAULT = 0;
    protected static final int PARAM_MAX_DEFAULT = 10;
    private Class<EntityType> entityType;
    protected Repository<EntityType> repository;
    //    protected AuthorizationStrategy authorization;
    private long createRequests = 0;
    private long readRequests = 0;
    private long updateRequests = 0;
    private long deleteRequests = 0;
    private long countRequests = 0;
    private long listRequests = 0;
    private String lastException;
    private final Date startedTime = new Date();
    private static final org.springframework.http.CacheControl cacheControl;
    protected static final String CUSTOM_ERROR_HANDLING_PARAM = "suppressDefaultAjaxError";
    protected static final String PARAM_APP_KEY = "appKey";
    //    private class ScopeInfo {
//
//        private String name;
//        private String value;
//
//        public ScopeInfo(String scopeDef) {
//            if (scopeDef == null)
//                return;
//            String[] tmp = scopeDef.split("=", 2);
//            if (tmp.length != 2)
//                return;
//            name = tmp[0];
//            value = tmp[1];
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public String getValue() {
//            return value;
//        }
//    }
    static {
        cacheControl = org.springframework.http.CacheControl.noCache().noStore().mustRevalidate();
    }

    protected BaseRest(Class<EntityType> entityType, Repository<EntityType> repository) {
        this.entityType = entityType;
        this.repository = repository;
    }

    //    @Inject
//    @Required
//    public void setAuthorizationStrategy(AuthorizationStrategy authorization) {
//        this.authorization = authorization;
//    }
    @ManagedAttribute
    public Date getStartedTime() {
        return startedTime;
    }

    @ManagedAttribute
    public long getCreateRequests() {
        return createRequests;
    }

    protected ResponseEntity<EntityType> create(EntityType entity) throws Exception {
        //verifyPermission("create");
        createRequests++;
        try {
            log.debug(new StringBuilder().append("create;").append(entityType.getName()).toString());
            EntityType createdEntity = repository.create(entity);
            //return Response.ok().entity(createdEntity).(cacheControl).expires(new Date()).build();
            return  ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache())
                    .header("Expires",new Date().toString()).body(createdEntity);
        } catch (Exception e) {
            logException("create", e);
            throw new RestException(RestErrorMessages.CREATE_FAILED, "Failed to create resource");
        }
    }

    protected ResponseEntity<EntityType>  copyDispatchMap(EntityType entity) throws Exception {
        //verifyPermission("create");
        createRequests++;
        try {
            log.debug(new StringBuilder().append("create;").append(entityType.getName()).toString());
            EntityType createdEntity = repository.create(entity);
            return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache())
                    .header("Expires",new Date().toString()).body(createdEntity);

        } catch (Exception e) {
            logException("create", e);
            throw new RestException(RestErrorMessages.CREATE_FAILED, "Failed to create resource");
        }
    }

    @GetMapping(value = "/rules", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<ValidationRules> rules() {
        try {
            log.debug(new StringBuilder().append("rules;").append(entityType.getName()).toString());
            ValidationRules validationRules = new ValidationRules();
            validationRules.getFieldRulesList().addAll(ValidationUtil.retrieveValidationRules(entityType));
            return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache())
                    .header("Expires",new Date().toString()).body(validationRules);
        } catch (Exception e) {
            logException("rules", e);
            throw new RestException(RestErrorMessages.READ_FAILED, "Failed to read resource");
        }
    }

    @GetMapping(value = "/permission", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<EntityTypePermission> permission() {
        String entityTypeName = entityType.getSimpleName();
        log.debug("permission; entityTypeName=" + entityTypeName);

        // defaults
        EntityTypePermission response = new EntityTypePermission();
        response.setCanCreate(false);
        response.setCanRead(false);
        response.setCanUpdate(false);
        response.setCanDelete(false);

        UserInfo currentUserInfo = SecurityContext.getCurrentUserInfo();
        if (currentUserInfo != null) {
            response.setCanCreate(currentUserInfo.hasPermission(getPermissionCode("Create")));
            response.setCanRead(currentUserInfo.hasPermission(getPermissionCode("View")));
            response.setCanUpdate(currentUserInfo.hasPermission(getPermissionCode("Modify")));
            response.setCanDelete(currentUserInfo.hasPermission(getPermissionCode("Delete")));
        }

        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache())
                .header("Expires",new Date().toString()).body(response);
    }

    @ManagedAttribute
    public long getUpdateRequests() {
        return updateRequests;
    }

    protected ResponseEntity<EntityType> update(long id, EntityType entity) throws Exception {
        //verifyPermission("update");
        updateRequests++;
        try {
            log.debug(new StringBuilder().append("update;").append(entityType.getName()).append(";").append(id).toString());
            EntityType updatedEntity = repository.update(id, entity);
            return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache())
                    .header("Expires",new Date().toString()).body(updatedEntity);
        } catch (Exception e) {
            log.error("update; Exception ={}", e);
            throw new RestException(RestErrorMessages.UPDATE_FAILED, "Failed to update resource");
        }
    }

    @ManagedAttribute
    public long getReadRequests() {
        return readRequests;
    }

//    @GET
//    @Path("/{id}")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<EntityType> readById(@PathVariable("id") long id) {
        return readById(id, null);
    }

    protected ResponseEntity<EntityType> readById(long id, MaskEntity maskEntity) {
//        verifyPermission("read");
        readRequests++;
        EntityType entity = null;
        try {
            log.debug(new StringBuilder().append("read;").append(entityType.getName()).append(";").append(id).toString());
            entity = repository.readById(id);
            if (maskEntity != null) {
                maskEntity.mask(entity);
            }

        } catch (Exception e) {
            log.error("update; Exception ={}", e);
            throw new RestException(RestErrorMessages.READ_FAILED, "Failed to read entity");
        }
        if (entity == null) {
           //Todo throw new RestException(Responses.NOT_FOUND, RestErrorMessages.READ_NOT_FOUND, "Resource with id '{0}' doesn't exist");
            throw new RestException(404, RestErrorMessages.READ_NOT_FOUND, "Resource with id '{0}' doesn't exist");

        }
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache())
                .header("Expires",new Date().toString()).body(entity);
    }

    @ManagedAttribute
    public long getDeleteRequests() {
        return deleteRequests;
    }

    protected void deleteById(long id) {
//        verifyPermission("delete");
        deleteRequests++;
        try {
            log.debug(new StringBuilder().append("delete;").append(entityType.getName()).append(";").append(id).toString());
            repository.deleteById(id);

        } catch (Exception e) {
            log.error("deleteById; Exception ={}", e);
            throw new RestException(RestErrorMessages.DELETE_FAILED, "Failed to delete resource");
        }
    }

    @ManagedAttribute
    public long getCountRequests() {
        return countRequests;
    }

//    @GET
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Path("/count")
    @GetMapping(value = "/count", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Count> count(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) {
        //final String permissionOperation = "View";
//        verifyPermission(permissionOperation);
        countRequests++;
        try {
            log.debug(request.getRequestURI());
            Map<String, String[]> parameterMap = request.getParameterMap();
            MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

            parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
//            ScopeInfo scopeInfo = getScopeInfo(permissionOperation);
//            if (scopeInfo != null) {
//                queryParams.add(scopeInfo.getName(), scopeInfo.getValue());
//                log.debug("count; injected filter: " + scopeInfo.getName() + "=" + scopeInfo.getValue());
//            }
            String appKey =null;// getParameterSingle(queryParams, PARAM_APP_KEY, null);
            if(parameterMap != null && parameterMap.containsKey(PARAM_APP_KEY)) {
                if (parameterMap.get(PARAM_APP_KEY).length > 1) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Only one value allowed for parameter ").append(PARAM_APP_KEY);
                    log.warn(sb.toString());
                    throw new RestException(RestErrorMessages.PARAMETER_ONLY_ONE, "Only one value allowed for parameter: {0}", PARAM_APP_KEY);
                }
                appKey = uriInfo.getFirst(PARAM_APP_KEY);
                //appKey = queryParams.get(PARAM_APP_KEY)
            }
            Query<EntityType> query = buildQuery(uriInfo);
            //  verifyNoParametersLeft(queryParams);
            if(appKey != null) {
                //query.search(appKey, getSearchColumns());
                query.filter("appKey", appKey);
            }
            String tenantScope= getTenantScope(uriInfo,headers);
            log.debug("count; tenantScope={}",tenantScope);
            if(tenantScope != null && !tenantScope.trim().isEmpty() && !tenantScope.trim().equals("[]")){
                long[] tenantArr=getTenantData(tenantScope);
                query.scopeIn(PARAM_TENANT_ID, tenantArr);
            }
            long result = query.count();
            log.debug(new StringBuilder().append("count;").append(entityType.getName()).append(";").append(result).toString());
            Count count = new Count(result);
            return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache())
                    .header("Expires",new Date().toString()).body(count);
        } catch (RestException e) {
            throw e;
        } catch (Exception e) {
            log.error("count; Exception ={}", e);
            throw new RestException(RestErrorMessages.COUNT_FAILED, "Count resource");
        }
    }


    public ResponseEntity<Count> count(HttpServletRequest request) {
        return count(request,null);
    }

    @ManagedAttribute
    public long getListRequests() {
        return listRequests;
    }

    protected ResponseEntity<Object[]> list(HttpServletRequest request) {
        return list(request, null,null);
    }


    protected ResponseEntity<Object[]> list(HttpServletRequest request, org.springframework.http.HttpHeaders headers) {
        return list(request, null,headers);
    }

    protected ResponseEntity<Object[]> listOnAppKey(HttpServletRequest request, org.springframework.http.HttpHeaders headers) {
        return listOnAppKey(request, null,headers);
    }

    @SuppressWarnings("unchecked")
    protected ResponseEntity listOnAppKey(HttpServletRequest request, MaskEntity maskEntity, org.springframework.http.HttpHeaders headers) {
        //final String permissionOperation = "View";
//        verifyPermission(permissionOperation);
        listRequests++;
        try {
            log.debug(request.getRequestURI());
            //MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

            Map<String, String[]> parameterMap = request.getParameterMap();
            MultivaluedMap<String,String> queryParams = new MultivaluedHashMap<>();

            parameterMap.forEach((key,values)->queryParams.addAll(key,Arrays.asList(values)));
//            ScopeInfo scopeInfo = getScopeInfo(permissionOperation);
//            if (scopeInfo != null) {
//                queryParams.add(scopeInfo.getName(), scopeInfo.getValue());
//                log.debug("list; injected filter " + scopeInfo.getName() + "=" + scopeInfo.getValue());
//            }


            // paging
            int firstResult = getParameterSingle(queryParams, PARAM_FIRST, PARAM_FIRST_DEFAULT);
            int maxResults = getParameterSingle(queryParams, PARAM_MAX, PARAM_MAX_DEFAULT);

            // filtering and search
            Query<EntityType> query = buildQuery(queryParams);
            String appKey = getParameterSingle(queryParams, PARAM_APP_KEY, null);
            // sorting
            String asc = getParameterSingle(queryParams, PARAM_ASCENDING, null);
            String desc = getParameterSingle(queryParams, PARAM_DESCENDING, null);
            String tenantScope= getTenantScope(queryParams,headers);
            log.debug("list; tenantScope={}",tenantScope);
            if(tenantScope != null && !tenantScope.trim().isEmpty() && !tenantScope.trim().equals("[]")){
                long[] tenantArr=getTenantData(tenantScope);
                query.scopeIn(PARAM_TENANT_ID, tenantArr);
            }

            if (asc != null && desc != null) {
                log.warn("Only one order direction (asc, desc) can be chosen");
                throw new RestException(RestErrorMessages.SORT_ONLY_ONE, "Only one order direction (asc, desc) can be chosen");
            }
            if (asc != null) {
                if (getOrderColumns().contains(asc) == false) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unsuported order column: ").append(asc);
                    log.warn(sb.toString());
                    throw new RestException(RestErrorMessages.SORT_NOT_SUPPORTED, "Unsuported order column: {0}", asc);
                }
                query.orderAscending(asc);
            }
            if (desc != null) {
                if (getOrderColumns().contains(desc) == false) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unsuported order column: ").append(desc);
                    log.warn(sb.toString());
                    throw new RestException(RestErrorMessages.SORT_NOT_SUPPORTED, "Unsuported order column: {0}", desc);
                }
                query.orderDescending(desc);
            }
            if(appKey != null) {
                //query.search(appKey, getSearchColumns());
                query.filter("appKey", appKey);
            }

            // no unexpected parameters allowed
            verifyNoParametersLeft(queryParams);

            List<EntityType> result = query.list(firstResult, maxResults);
            if (maskEntity != null) {
                for (EntityType item : result) {
                    maskEntity.mask(item);
                }
            }
            EntityType[] a = (EntityType[]) Array.newInstance(entityType, result.size());
            log.debug(new StringBuilder().append("list;").append(entityType.getName()).append(";").append(result.size()).toString());
            return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache())
                    .header("Expires",new Date().toString()).body(result.toArray(a));
        } catch (RestException e) {
            throw e;
        } catch (Exception e) {
            log.error("list; Exception ={}", e);
            throw new RestException(RestErrorMessages.LIST_FAILED, "List resource");
        }
    }


    @SuppressWarnings("unchecked")
    protected ResponseEntity<Object[]> list(HttpServletRequest request, MaskEntity maskEntity, @RequestHeader org.springframework.http.HttpHeaders headers) {
        //final String permissionOperation = "View";
//        verifyPermission(permissionOperation);
        listRequests++;
        try {
            log.debug(request.getRequestURI());
            //MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

            Map<String, String[]> parameterMap = request.getParameterMap();
            MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

            parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));

//            ScopeInfo scopeInfo = getScopeInfo(permissionOperation);
//            if (scopeInfo != null) {
//                queryParams.add(scopeInfo.getName(), scopeInfo.getValue());
//                log.debug("list; injected filter " + scopeInfo.getName() + "=" + scopeInfo.getValue());
//            }


            // paging
            int firstResult = getParameterSingle(uriInfo, PARAM_FIRST, PARAM_FIRST_DEFAULT);
            int maxResults = getParameterSingle(uriInfo, PARAM_MAX, PARAM_MAX_DEFAULT);
            // filtering and search
            Query<EntityType> query = buildQuery(uriInfo);
            String appKey = getParameterSingle(uriInfo, PARAM_APP_KEY, null);
            // sorting
            String asc = getParameterSingle(uriInfo, PARAM_ASCENDING, null);
            String desc = getParameterSingle(uriInfo, PARAM_DESCENDING, null);
            String tenantScope= getTenantScope(uriInfo,headers);
            log.debug("list; tenantScope={}",tenantScope);
            if(tenantScope != null && !tenantScope.trim().isEmpty() && !tenantScope.trim().equals("[]")){
                long[] tenantArr=getTenantData(tenantScope);
                query.scopeIn(PARAM_TENANT_ID, tenantArr);
            }

            if (asc != null && desc != null) {
                log.warn("Only one order direction (asc, desc) can be chosen");
                throw new RestException(RestErrorMessages.SORT_ONLY_ONE, "Only one order direction (asc, desc) can be chosen");
            }
            if (asc != null) {
                if (getOrderColumns().contains(asc) == false) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unsuported order column: ").append(asc);
                    log.warn(sb.toString());
                    throw new RestException(RestErrorMessages.SORT_NOT_SUPPORTED, "Unsuported order column: {0}", asc);
                }
                query.orderAscending(asc);
            }
            if (desc != null) {
                if (getOrderColumns().contains(desc) == false) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unsuported order column: ").append(desc);
                    log.warn(sb.toString());
                    throw new RestException(RestErrorMessages.SORT_NOT_SUPPORTED, "Unsuported order column: {0}", desc);
                }
                query.orderDescending(desc);
            }
            if(appKey != null) {
                //query.search(appKey, getSearchColumns());
                query.filter("appKey", appKey);
            }

            // no unexpected parameters allowed
            verifyNoParametersLeft(uriInfo);

            List<EntityType> result = query.list(firstResult, maxResults);
            if (maskEntity != null) {
                for (EntityType item : result) {
                    maskEntity.mask(item);
                }
            }
            EntityType[] a = (EntityType[]) Array.newInstance(entityType, result.size());
            log.debug(new StringBuilder().append("list;").append(entityType.getName()).append(";").append(result.size()).toString());
            return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache())
                    .header("Expires",new Date().toString()).body(result.toArray(a));
        } catch (RestException e) {
            throw e;
        } catch (Exception e) {
            log.error("list; Exception ={}", e);
            throw new RestException(RestErrorMessages.LIST_FAILED, "List resource");
        }
    }

    private Query<EntityType> buildQuery(MultivaluedMap<String, String> queryParams) {
        Query<EntityType> query = repository.getQuery();

        // search across one or more fields/columns
        Set<String> searchColumns = getSearchColumns();
        String value = getParameterSingle(queryParams, PARAM_Q, null);
        if (value != null && searchColumns != null && searchColumns.size() > 0) {
            query.search(value, searchColumns);
        }

        // field/column filtering
        for (String columnName : getFilterColumns()) {
            value = getParameterSingle(queryParams, columnName, null);
            if (value != null) {
                query.filter(columnName, value);
            }
        }

        return query;
    }

    protected String getParameterSingle(MultivaluedMap<String, String> queryParams, String name, String defaultValue) {
        if (queryParams.containsKey(name) == false) {
            return defaultValue;
        }
        if (queryParams.get(name).size() > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("Only one value allowed for parameter ").append(name);
            log.warn(sb.toString());
            throw new RestException(RestErrorMessages.PARAMETER_ONLY_ONE, "Only one value allowed for parameter: {0}", name);
        }
        String result = queryParams.getFirst(name);
        queryParams.remove(name);
        return result;
    }

    protected int getParameterSingle(MultivaluedMap<String, String> queryParams, String name, int defaultValue) {
        String value = getParameterSingle(queryParams, name, null);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    private void verifyNoParametersLeft(MultivaluedMap<String, String> queryParams) {
        if (queryParams.size() > 0) {
            StringBuilder sb = new StringBuilder();
            List<String> unexpectedParameters = new LinkedList<String>();
            for (String name : queryParams.keySet()) {
                if (unexpectedParameters.contains(name) == false && !name.trim().equalsIgnoreCase("tenantScope")) {
                    unexpectedParameters.add(name);
                    sb.append(name).append("; ");
                }
            }

            if (unexpectedParameters.size() > 0) {
                throw new RestException(RestErrorMessages.PARAMETER_UNEXPECTED, "Unexpected parameter(s): {0}", sb.toString());
            }
        }
    }

    protected void logException(String method, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(";exception=").append(e);
        log.warn(sb.toString());
        lastException = sb.toString();
    }

    @ManagedAttribute
    public String getLastException() {
        return lastException;
    }

    public static org.springframework.http.CacheControl  getCacheControl() {
        return cacheControl;
    }

    private String getPermissionCode(String operation) {
        return entityType.getSimpleName() + "." + operation;
    }

//    private void verifyPermission(String operation) {
//        String permissionCode = getPermissionCode(operation);
//        if (authorization.hasPermission(permissionCode) == false) {
//            throw new RestException(RestErrorMessages.ACCESS_DENIED, "Access denied", permissionCode);
//        }
//    }
//    private ScopeInfo getScopeInfo(String operation) {
//        String permissionCode = getPermissionCode(operation);
//        String scopeDef = authorization.getScope(permissionCode);
//        ScopeInfo scopeInfo = new ScopeInfo(scopeDef);
//        if (scopeInfo.getName()==null || scopeInfo.getValue()==null) {
//            return null;
//        }
//        return scopeInfo;
//    }

    //@SuppressWarnings("unchecked")
    public static long[] getTenantData(String tenantdeatails) {
        log.info("getTenantData; tenantScope={}",tenantdeatails);
        long[] result = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            if(tenantdeatails!=null && !tenantdeatails.trim().equals("")){
                long[] tenantScopeArr=mapper.readValue(tenantdeatails,long[].class);
                if( tenantScopeArr!=null && !(tenantScopeArr.length==0)){
                    log.info("getTenantData; tenantScopeArr={}",Arrays.toString(tenantScopeArr));
                    result=tenantScopeArr;
                    return result;
                }
            }
        } catch (JsonParseException e) {
            log.error("getTenantData; JsonParseException ={}",e.toString());
        } catch (JsonMappingException e) {
            log.error("getTenantData; JsonMappingException ={}",e.toString());
        } catch (IOException e) {
            log.error("getTenantData; IOException ={}",e.toString());
        }
        return result;
    }


    protected String getTenantScope(MultivaluedMap<String,String> uriInfo, org.springframework.http.HttpHeaders headers) {
        String tenantScope = "";
        if(headers!=null && headers.get(PARAM_TENANT_SCOPE) == null){
            return tenantScope;
        }
        if(headers!=null && headers.get(PARAM_TENANT_SCOPE)!=null){
            tenantScope=headers.get(PARAM_TENANT_SCOPE).get(0);
        }
        if(tenantScope == null || tenantScope.trim().isEmpty() || tenantScope.trim().equals("[]")){
            tenantScope = getParameterSingle(uriInfo, PARAM_TENANT_SCOPE, null);
        }
        return tenantScope;
    }

    protected abstract Set<String> getFilterColumns();

    protected abstract Set<String> getSearchColumns();

    protected abstract Set<String> getOrderColumns();
}
