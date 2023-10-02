package com.esq.rbac.service.templatevariable.rest;

import com.esq.rbac.service.base.error.RestErrorMessages;
import com.esq.rbac.service.base.exception.RestException;
import com.esq.rbac.service.base.rest.BaseRest;
import com.esq.rbac.service.templatevariable.helpers.TemplateVariableInfo;
import jakarta.annotation.Resource;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@RequestMapping("/templateVariable")
@Slf4j
public class TemplateVariableRest {

    private static final String CONFIG_KEY = "messageTemplate.variable";
    private static final String CONFIG_PREFIX_KEY = "messageTemplate.variablePrefix";
    private static final String CONFIG_SUFFIX_KEY = "messageTemplate.variableSuffix";
    private static final String DEFAULT_PREFIX = "%";
    private static final String DEFAULT_SUFFIX = "%";
    private Configuration configuration;

   @Resource(name="propertyConfig")
    @Autowired
    public void setConfiguration(Configuration configuration) {
        log.trace("setConfiguration;");
        this.configuration = configuration;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<List<String>> getTemplateVariables() {
        try {
            List<String> variableNameList = new LinkedList<>(loadVariableNames());
            Collections.sort(variableNameList);
            return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(variableNameList);

        } catch (Exception e) {
            log.error("getTemplateVariables; exception {}", e);
            throw new RestException(RestErrorMessages.LIST_FAILED, "Internal error while retrieving list of variables for message templates");
        }
    }

    @GetMapping(value = "/info",produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<TemplateVariableInfo> getVariableInfo() {
        try {
            return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(loadInfo());
        } catch (Exception e) {
            log.error("getVariableInfo; exception {}", e);
            throw new RestException(RestErrorMessages.READ_FAILED, "Internal error while retrieving message template info");
        }
    }

    private Set<String> loadVariableNames() {
        String[] variableNames = configuration.getStringArray(CONFIG_KEY);
        Set<String> variableNameSet = new HashSet<String>(Arrays.asList(variableNames));
        return variableNameSet;
    }

    private TemplateVariableInfo loadInfo() {
        TemplateVariableInfo info = new TemplateVariableInfo();
        info.setPrefix(configuration.containsKey(CONFIG_PREFIX_KEY) ? configuration.getString(CONFIG_PREFIX_KEY) : DEFAULT_PREFIX);
        info.setSuffix(configuration.containsKey(CONFIG_SUFFIX_KEY) ? configuration.getString(CONFIG_SUFFIX_KEY) : DEFAULT_SUFFIX);
        return info;
    }
}
