package com.esq.rbac.service.codes.rest;

import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.codes.service.CodeDal;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping(value = CodeRest.RESOURCE_PATH)
@Slf4j
public class CodeRest {
    public static final String RESOURCE_PATH = "codes";

    private CodeDal codeDal;

    @Autowired
    public void setCodeDal(CodeDal codeDal) {
        log.trace("setCodeDal; {};", codeDal);
        this.codeDal = codeDal;

    }


    @EventListener
    public void fillCodesLookupTable(ApplicationStartedEvent event){
        log.trace("fillCodesLookupTable");
        Lookup.fillCodesTable(codeDal.list(null));
    }


    @Parameters({
            @Parameter(name = "codeType", description = "codeType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "codeValue", description = "codeValue", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "parentType", description = "parentType", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "parentCodeValue", description = "parentCodeValue", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "parentCodeId", description = "codeId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Code[]> list(HttpServletRequest servletRequest) {

        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("list; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0,
                Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        if(optionSort==null || optionSort.getSortProperties()==null || optionSort.getSortProperties().isEmpty()){

            //Todo Use MultivaluedHashMap instead of MultiValuedMapImpl
            MultivaluedMap<String, String> sortmap = new MultivaluedHashMap<>();
            sortmap.add("asc", "displayOrder");
            optionSort=new OptionSort(sortmap);
        }
        OptionFilter optionFilter = new OptionFilter(
                uriInfo);
        Options options = new Options(optionPage, optionSort, optionFilter);
        List<Code> list = new ArrayList<>();
        list = codeDal.list(options);
        Code[] array = new Code[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }


    @GetMapping(value="/application",produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Code[]> codesByApplication(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("codesByApplication; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionFilter optionFilter = new OptionFilter(uriInfo);
        List<Code> list = new ArrayList<Code>();
        list = codeDal.getCodesByApplication(optionFilter);
        Code[] array = new Code[list.size()];
        list.toArray(array);

        return ResponseEntity.ok(array);
    }
}

