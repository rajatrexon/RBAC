package com.esq.rbac.service.variable.rest;


import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.scope.builder.ScopeBuilder;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.variable.domain.Variable;
import com.esq.rbac.service.variable.service.VariableDal;
import com.esq.rbac.service.variable.util.VariableUtil;
import com.esq.rbac.service.variable.variableinfov2.domain.VariableInfoV2;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/variables/v2")
@Slf4j
public class VariableRestV2 {


    private VariableDal variableDal;
    private Validator validator;
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
    public void setScopeBuilder(ScopeBuilder scopeBuilder) {
        log.trace("setScopeBuilder scopeBuilder={}", scopeBuilder);
        this.scopeBuilder = scopeBuilder;
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "appKey", description = "appKey", required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<VariableInfoV2> create(@RequestBody VariableInfoV2 variableInfoV2, @RequestHeader HttpHeaders headers) throws Exception {
        if (headers.get("appKey") != null && !headers.get("appKey").isEmpty()) {
            variableInfoV2.setAppKey(headers.get("appKey").get(0));
        }
        log.debug("create; variable={}", variableInfoV2);
        Variable variable = variableDal.toVariableV2(variableInfoV2);
        validate(variable);
        scopeBuilder.refreshScopeBuilder();
        return ResponseEntity.ok(VariableUtil.fromVariableV2(variableDal.create(variable)));

    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "appKey", description = "appKey", required = false, schema = @Schema(type = "string"), in = ParameterIn.HEADER)})
    public ResponseEntity<VariableInfoV2> update(@RequestBody VariableInfoV2 variableInfoV2, @RequestHeader HttpHeaders headers) throws Exception {
        if (headers.get("appKey") != null && !headers.get("appKey").isEmpty()) {
            variableInfoV2.setAppKey(headers.get("appKey").get(0));
        }
        log.debug("update; VariableInfoV2={}", variableInfoV2);
        Variable variable = variableDal.toVariableV2(variableInfoV2);
        validate(variable);
        scopeBuilder.refreshScopeBuilder();
        return ResponseEntity.ok(VariableUtil.fromVariableV2(variableDal.update(variable)));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({@Parameter(name = "appKey", description = "appKey", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "variableName", description = "variableName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "applicationName", description = "applicationName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "groupName", description = "groupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "appKey", description = "appKey", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<VariableInfoV2[]> list(HttpServletRequest request, @RequestHeader HttpHeaders headers) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        log.trace("list; requestUri={}", request.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        if (headers.get("appKey") != null && !headers.get("appKey").isEmpty()) {
            optionFilter.addFilter("appKey", headers.get("appKey").get(0));
        }
        Options options = new Options(optionPage, optionSort, optionFilter);

        List<VariableInfoV2> list = variableDal.getListV2(options);

        VariableInfoV2[] array = new VariableInfoV2[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }

    @DeleteMapping
    @Parameters({@Parameter(name = "appKey", description = "appKey", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY)})
    public void delete(@RequestBody VariableInfoV2 variableInfoV2, @RequestHeader HttpHeaders headers) throws Exception {
        if (headers.get("appKey") != null && !headers.get("appKey").isEmpty()) {
            variableInfoV2.setAppKey(headers.get("appKey").get(0));
        }
        log.debug("delete; variable={}", variableInfoV2);
        Variable variable = variableDal.toVariableV2(variableInfoV2);
        variableDal.delete(variable);
    }

    private void validate(Variable variable) {
        Set<ConstraintViolation<Variable>> violations = validator.validate(variable);
        if (!violations.isEmpty()) {
            log.debug("VariableRestV2; validationResult={}", violations);

            ConstraintViolation<Variable> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }
}
