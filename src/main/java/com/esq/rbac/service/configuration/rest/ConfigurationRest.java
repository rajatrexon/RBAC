package com.esq.rbac.service.configuration.rest;


import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.configuration.domain.Configuration;
import com.esq.rbac.service.configuration.service.ConfigurationDal;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.ldapuserservice.service.LdapUserService;
import com.esq.rbac.service.util.AuditLogger;
import com.esq.rbac.service.util.DeploymentUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@RequestMapping(value = "/configuration")
@RestController
public class ConfigurationRest {

    private AuditLogger auditLogger;
    private ConfigurationDal configurationDal;
    private LdapUserService ldapUserService;

    private DeploymentUtil deploymentUtil;

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        log.debug("setDeploymentUtil; {}", deploymentUtil);
        this.deploymentUtil = deploymentUtil;
    }

    @Autowired
    public void setConfigurationDal(ConfigurationDal configurationDal, AuditLogService auditLogDal) {
        log.trace("setConfigurationDal; {}", configurationDal);
        this.configurationDal = configurationDal;
        this.auditLogger = new AuditLogger(auditLogDal);
    }

    @Autowired
    public void setLdapUserService(LdapUserService ldapUserService) {
        log.debug("setLdapUserService; {}", ldapUserService);
        this.ldapUserService = ldapUserService;
    }

    @GetMapping
    public ResponseEntity<Configuration[]> getConfiguration() {
        List<Configuration> configList = configurationDal.getConfiguration();
        Configuration[] array = new Configuration[configList.size()];
        configList.toArray(array);
        return ResponseEntity.ok(array);
    }

    @PutMapping
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<Configuration[]> updateConfiguration(@RequestHeader HttpHeaders headers, @RequestBody Configuration[] configuration) {

        validateConfig(configuration);
        try {
            boolean updatedRows = configurationDal.updateConfiguration(Arrays.asList(configuration));
            try {
                ldapUserService.reloadConfiguration();
            } catch (Exception e) {
                log.warn("updateConfiguration; ldapUserService.reloadConfiguration Exception={}", e);
            }
            if (updatedRows) {
                List<Configuration> configList = configurationDal.getConfiguration();
                Configuration[] array = new Configuration[configList.size()];
                configList.toArray(array);
                Integer userId = Integer.parseInt(headers.get("userId").get(0));
                auditLogger.logCreate(userId, "Configuration", "Configuration", "Update", configurationDal.getObjectChangeSet());
                return ResponseEntity.ok(array);
            }
        } catch (IOException e) {
            log.error("updateConfiguration; exception={}", e);
        }
        return null;
    }

    public boolean validateConfig(Configuration[] configuration) {
        Configuration[] validateConfig = configuration;
        for (Configuration config : validateConfig) {
            if (config.getConfType().equalsIgnoreCase("TEXT")) {
                String regex = "^$|^[0-9,\\s]{1,3}$";
                if (Pattern.compile(regex).matcher(config.getConfValue()).matches()) {
                    return true;
                } else {
                    throw new ErrorInfoException("validationError", "enter valid input");
                }
            } else if (config.getConfType().equalsIgnoreCase("STRING") || config.getConfType().equalsIgnoreCase("PASSWORD")) {
                String regex = "^$|^[a-zA-Z,\\s]{1,512}$";
                if (Pattern.compile(regex).matcher(config.getConfValue()).matches()) {
                    return true;
                } else {
                    throw new ErrorInfoException("validationError", "enter valid input");
                }
            } else if (config.getConfType().equalsIgnoreCase("ENABLE")) {
                if (config.getConfValue().equalsIgnoreCase("TRUE") || config.getConfValue().equalsIgnoreCase("FALSE")) {
                    return true;
                } else {
                    throw new ErrorInfoException("validationError", "enter valid input");
                }
            } else {
                return true;
            }
        }
        return true;
    }
}

