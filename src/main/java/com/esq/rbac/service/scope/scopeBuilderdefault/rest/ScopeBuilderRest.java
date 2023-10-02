package com.esq.rbac.service.scope.scopeBuilderdefault.rest;

import com.esq.rbac.service.scope.builder.ScopeBuilder;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/scopeBuilder")
@Tag(name="/scopeBuilder")
@Slf4j
public class ScopeBuilderRest {


    private final ScopeBuilder scopeBuilder;

//    @Inject
//    public void setScopeBuilder(ScopeBuilder scopeBuilder){
//        log.trace("setScopeBuilder; scopeBuilder={}", scopeBuilder);
//        this.scopeBuilder = scopeBuilder;
//        this.scopeBuilder.refreshScopeBuilder();
//    }

    public ScopeBuilderRest(ScopeBuilder scopeBuilder) {
        log.trace("setScopeBuilder; scopeBuilder={}", scopeBuilder);
        this.scopeBuilder = scopeBuilder;
        this.scopeBuilder.refreshScopeBuilder();
    }

    @GetMapping(value = "/filters", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "scopeKey", description = "scopeKey", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "additionalMap", description = "additionalMap", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public String getScopeFilters(HttpServletRequest request, @RequestHeader HttpHeaders headers) {
        Integer.parseInt(headers.get("userId").get(0));

        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        log.trace("getScopeFilters; queryParams={}", uriInfo);
        return scopeBuilder.getFilters(uriInfo.getFirst("scopeKey"), uriInfo.getFirst("userName"), uriInfo.getFirst("additionalMap"));
    }

    @GetMapping(value = "/filterData/{sourcePath}", produces = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "scopeKey", description = "scopeKey", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "additionalMap", description = "additionalMap", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "dataKey", description = "dataKey", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "parentValue", description = "parentValue", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public String getFilterKeyData(HttpServletRequest request, @RequestHeader HttpHeaders headers, @PathVariable("sourcePath") String sourcePath) throws Exception {
        Integer.parseInt(headers.get("userId").get(0));
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        log.trace("getFilterKeyData; queryParams={}", uriInfo);
        return scopeBuilder.getFilterKeyData(sourcePath, uriInfo.getFirst("dataKey"), uriInfo.getFirst("scopeKey"), uriInfo.getFirst("userName"), uriInfo.getFirst("additionalMap"), null, uriInfo.getFirst("parentValue"));
    }

    //    TODO __>ScopeMap Binding
    @PostMapping(value = "/filterData/{sourcePath}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "scopeKey", description = "scopeKey", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "additionalMap", description = "additionalMap", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "dataKey", description = "dataKey", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "parentValue", description = "parentValue", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public String getFilterKeyDataWithScopes(HttpServletRequest request, @RequestHeader HttpHeaders headers, @PathVariable("sourcePath") String sourcePath, Map<String, String> scopeMap) throws Exception {
        Integer.parseInt(headers.get("userId").get(0));
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        log.trace("getFilterKeyDataWithScopes; queryParams={}", uriInfo);
        return scopeBuilder.getFilterKeyData(sourcePath, uriInfo.getFirst("dataKey"), uriInfo.getFirst("scopeKey"), uriInfo.getFirst("userName"), uriInfo.getFirst("additionalMap"), scopeMap, uriInfo.getFirst("parentValue"));
    }

    //    TODO __> scopeMap Binding
    @PostMapping(value = "/filterData", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "scopeKey", description = "scopeKey", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "additionalMap", description = "additionalMap", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "dataKey", description = "dataKey", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "parentValue", description = "parentValue", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public String getFilterKeyDataWithScopes(HttpServletRequest request, @RequestHeader HttpHeaders headers, Map<String, String> scopeMap) throws Exception {
        Integer.parseInt(headers.get("userId").get(0));
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        log.trace("getFilterKeyDataWithScopes; queryParams={}", uriInfo);
        return scopeBuilder.getFilterKeyData(null, uriInfo.getFirst("dataKey"), uriInfo.getFirst("scopeKey"), uriInfo.getFirst("userName"), uriInfo.getFirst("additionalMap"), scopeMap, uriInfo.getFirst("parentValue"));
    }


    //    TODO __> data binding.
    @PostMapping(value = "/validateScopeBuilderOutput", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER), @Parameter(name = "scopeKey", description = "scopeKey", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userName", description = "userName", schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "additionalMap", description = "additionalMap", schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public String validateScopeBuilderOutput(String data, HttpServletRequest request, @RequestHeader HttpHeaders headers) {
        Integer.parseInt(headers.get("userId").get(0));
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();

        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        log.trace("validateScopeBuilderOutput; queryParams={}; data={}", uriInfo, data);
        return scopeBuilder.validateScopeBuilderOutput(data, uriInfo.getFirst("scopeKey"), uriInfo.getFirst("userName"), uriInfo.getFirst("additionalMap"));
    }

}
