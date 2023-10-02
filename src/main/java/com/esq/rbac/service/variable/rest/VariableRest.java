package com.esq.rbac.service.variable.rest;


import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.scope.builder.ScopeBuilder;
import com.esq.rbac.service.util.AuditLogger;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.variable.domain.Variable;
import com.esq.rbac.service.variable.service.VariableDal;
import com.esq.rbac.service.variable.util.VariableUtil;
import com.esq.rbac.service.variable.variableinfo.domain.VariableInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/variables")
@Slf4j
public class VariableRest {


    private VariableDal variableDal;
    private Validator validator;
    private AuditLogger auditLogger;
    private ScopeBuilder scopeBuilder;

    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }

    @Autowired
    public void setVariableDal(VariableDal variableDal) {
        log.trace("setVariableDal;");
        this.variableDal = variableDal;
    }

    @Autowired
    public void setAuditLogger(AuditLogService auditLogDal) {
        log.trace("setAuditLogger;");
        this.auditLogger = new AuditLogger(auditLogDal);
    }

    @Autowired
    public void setScopeBuilder(ScopeBuilder scopeBuilder) {
        log.trace("setScopeBuilder scopeBuilder={}", scopeBuilder);
        this.scopeBuilder = scopeBuilder;
    }

    //    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VariableInfo> create(@RequestBody VariableInfo variableInfo) throws Exception {
        log.debug("create; variable={}", variableInfo);
        Variable variable = variableDal.toVariable(variableInfo);
        validate(variable);
        scopeBuilder.refreshScopeBuilder();
        return ResponseEntity.ok(VariableUtil.fromVariable(variableDal.create(variable)));
    }

    //TODO __> variableInfoArray binding...
    @PostMapping(value = "/userVariables/{userId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public void createUserVariables(@RequestBody VariableInfo[] variableInfoArray, @PathVariable("userId") Integer userId, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("createUserVariables; variableInfoArray={}", variableInfoArray != null ? Arrays.asList(variableInfoArray) : null);
        synchronized (this) {
            OptionFilter optionFilter = new OptionFilter();
            optionFilter.addFilter("userName", Lookup.getUserName(userId));
            List<VariableInfo> oldVariables = variableDal.getList(new Options(optionFilter));
            List<VariableInfo> newVariables = new LinkedList<>();
            Integer loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
            if (variableInfoArray != null) {
                for (VariableInfo variableInfo : variableInfoArray) {
                    Variable variable = variableDal.toVariable(variableInfo);
                    validate(variable);
                    newVariables.add(VariableUtil.fromVariable(variableDal.create(variable)));
                }
            }

            List<VariableInfo> deletedVarInfos = new LinkedList<VariableInfo>(oldVariables);
            deletedVarInfos.removeAll(newVariables);

            if (deletedVarInfos != null && !deletedVarInfos.isEmpty()) {
                for (VariableInfo variableInfo : deletedVarInfos) {
                    variableDal.delete(variableDal.toVariable(variableInfo));
                }
            }
            createAuditLog(oldVariables, newVariables, loggedInUserId, Lookup.getUserName(userId), "User", "Update");
        }
        scopeBuilder.refreshScopeBuilder();
    }


    //    TODO__>   variableInfoArray Binding..
    @PostMapping(value = "/groupVariables/{groupId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public void createGroupVariables(@RequestBody VariableInfo[] variableInfoArray, @PathVariable("groupId") Integer groupId, @RequestHeader HttpHeaders headers) throws Exception {
        log.trace("createUserVariables; variableInfoArray={}", variableInfoArray != null ? Arrays.asList(variableInfoArray) : null);
        synchronized (this) {
            OptionFilter optionFilter = new OptionFilter();
            optionFilter.addFilter("groupName", Lookup.getGroupName(groupId));
            List<VariableInfo> oldVariables = variableDal.getList(new Options(optionFilter));
            List<VariableInfo> newVariables = new LinkedList<VariableInfo>();
            Integer loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
            if (variableInfoArray != null) {
                for (VariableInfo variableInfo : variableInfoArray) {
                    Variable variable = variableDal.toVariable(variableInfo);
                    validate(variable);
                    newVariables.add(VariableUtil.fromVariable(variableDal.create(variable)));
                }
            }

            List<VariableInfo> deletedVarInfos = new LinkedList<VariableInfo>(oldVariables);
            deletedVarInfos.removeAll(newVariables);

            if (deletedVarInfos != null && !deletedVarInfos.isEmpty()) {
                for (VariableInfo variableInfo : deletedVarInfos) {
                    variableDal.delete(variableDal.toVariable(variableInfo));
                }
            }
            createAuditLog(oldVariables, newVariables, loggedInUserId, Lookup.getGroupName(groupId), "Group", "Update");
        }
        scopeBuilder.refreshScopeBuilder();
    }

    private void createAuditLog(List<VariableInfo> oldVariables, List<VariableInfo> newVariables, Integer loggedInUserId, String entityName, String target, String operation) throws Exception {
        Map<String, String> auditLogJson = new HashMap<>();
        auditLogJson.put("name", entityName);
        if (newVariables != null && oldVariables != null) {
            Set<VariableInfo> removedKeys = new HashSet<VariableInfo>(oldVariables);
            removedKeys.removeAll(newVariables);

            Set<VariableInfo> addedKeys = new HashSet<VariableInfo>(newVariables);
            addedKeys.removeAll(oldVariables);

            Map<VariableInfo, String> beforeMap = new HashMap<VariableInfo, String>();
            for (VariableInfo varInfo : oldVariables) {
                beforeMap.put(varInfo, varInfo.getVariableValue());
            }
            Map<VariableInfo, String> afterMap = new HashMap<VariableInfo, String>();
            for (VariableInfo varInfo : newVariables) {
                afterMap.put(varInfo, varInfo.getVariableValue());
            }
            Set<Map.Entry<VariableInfo, String>> changedEntries = new HashSet<Map.Entry<VariableInfo, String>>(afterMap.entrySet());
            changedEntries.removeAll(beforeMap.entrySet());

            for (VariableInfo varInfo : removedKeys) {
                auditLogJson.put("Variable[" + varInfo.getVariableName() + (varInfo.getApplicationName() != null ? "[" + varInfo.getApplicationName() + "]" : "") + "]:old", varInfo.getVariableValue());
            }
            for (VariableInfo varInfo : addedKeys) {
                auditLogJson.put("Variable[" + varInfo.getVariableName() + (varInfo.getApplicationName() != null ? "[" + varInfo.getApplicationName() + "]" : "") + "]:new", varInfo.getVariableValue());
            }
            for (Map.Entry<VariableInfo, String> varInfo : changedEntries) {
                auditLogJson.put("Variable[" + varInfo.getKey().getVariableName() + (varInfo.getKey().getApplicationName() != null ? "[" + varInfo.getKey().getApplicationName() + "]" : "") + "]:new", varInfo.getValue());
                if (beforeMap.get(varInfo.getKey()) != null) {
                    auditLogJson.put("Variable[" + varInfo.getKey().getVariableName() + (varInfo.getKey().getApplicationName() != null ? "[" + varInfo.getKey().getApplicationName() + "]" : "") + "]:old", beforeMap.get(varInfo.getKey()));
                }
            }
        }
        if (auditLogJson.size() > 1) {
            auditLogger.logCreate(loggedInUserId, entityName, target, operation, auditLogJson);
        }
    }


    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VariableInfo update(@RequestBody VariableInfo variableInfo) throws Exception {
        log.debug("update; variableInfo={}", variableInfo);
        Variable variable = variableDal.toVariable(variableInfo);
        validate(variable);
        scopeBuilder.refreshScopeBuilder();
        return VariableUtil.fromVariable(variableDal.update(variable));
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "variableName", description = "variableName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "applicationName", description = "applicationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupName", description = "groupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "appKey", description = "appKey", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<VariableInfo[]> list(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("list; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));


        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<VariableInfo> list = variableDal.getList(options);

        VariableInfo[] array = new VariableInfo[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }

    //    @DELETE
//    @Produces(MediaType.APPLICATION_JSON)
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody VariableInfo variableInfo) throws Exception {
        log.debug("delete; variable={}", variableInfo);
        Variable variable = variableDal.toVariable(variableInfo);
        variableDal.delete(variable);
    }

    private void validate(Variable variable) {
        Set<ConstraintViolation<Variable>> violations = validator.validate(variable);
        if (violations.size() > 0) {
            log.debug("VariableRest; validationResult={}", violations);

            ConstraintViolation<Variable> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }
}
