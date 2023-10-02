package com.esq.rbac.service.tenantattribute.rest;

import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.tenantattribute.domain.TenantAttribute;
import com.esq.rbac.service.tenantattribute.service.TenantAttributeDal;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/tenantAttributes")
public class TenantAttributeRest {

    private Validator validator;

    private TenantAttributeDal tenantAttributeDal;

    @Autowired
    public void setValidator(Validator validator) {
        log.trace("setValidator; {}", validator);
        this.validator = validator;
    }

    @Autowired
    public void setTenantAttributeDal(TenantAttributeDal tenantAttributeDal) {
        log.trace("setTenantAttributeDal;");
        this.tenantAttributeDal = tenantAttributeDal;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public TenantAttribute create(@RequestBody TenantAttribute tenantAttribute) throws Exception {
        log.debug("create; tenantAttribute={}", tenantAttribute);
        validate(tenantAttribute);
        return tenantAttributeDal.create(tenantAttribute);
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public TenantAttribute update(@RequestBody TenantAttribute tenantAttribute) throws Exception {
        log.debug("update; tenantAttribute={}", tenantAttribute);
        validate(tenantAttribute);
        return tenantAttributeDal.update(tenantAttribute);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public List<TenantAttribute> getTenantAttributes(@QueryParam("tenantId") Long tenantId,
                                                     @QueryParam("appKey") String appKey) throws Exception {
        if (appKey != null && !appKey.isEmpty()) {
            if (tenantId != null && tenantId != 0) {
                log.debug("retrive; tenantAttributes based on tenantId={}", tenantId);
                return tenantAttributeDal.getTenantAttributesByTenantIdAndAppKey(tenantId, appKey);
            } else {
                return tenantAttributeDal.getTenantAttributesByAppKey(appKey);
            }
        } else
            throw new Exception("Missing appKey in request");
    }

    @DeleteMapping("/{attributeId}")
    public void deleteTenantAttributeById(@RequestHeader HttpHeaders headers, @PathVariable("attributeId") Integer attributeId)
            throws Exception {
        log.debug("deleteTenantAttributeById; attributeId={}", attributeId);
        try {
            tenantAttributeDal.deleteTenantAttributeByAttributeId(attributeId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void validate(TenantAttribute tenantAttribute) {
        Set<ConstraintViolation<TenantAttribute>> violations = validator.validate(tenantAttribute);
        if (violations.size() > 0) {
            log.debug("TenantAttributeRest; tenantAttributeRest={}", violations);

            ConstraintViolation<TenantAttribute> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }

}

