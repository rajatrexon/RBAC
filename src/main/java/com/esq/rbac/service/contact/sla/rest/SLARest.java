package com.esq.rbac.service.contact.sla.rest;

import com.esq.rbac.service.base.error.RestErrorMessages;
import com.esq.rbac.service.base.exception.RestException;
import com.esq.rbac.service.base.rest.BaseRest;
import com.esq.rbac.service.targetoperations.TargetOperations;
import com.esq.rbac.service.contact.sla.domain.SLA;
import com.esq.rbac.service.contact.sla.repository.SLARepository;
import com.esq.rbac.service.contact.helpers.ContactUserRest;
import com.esq.rbac.service.util.ContactAuditUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/sla")
//@ManagedResource(objectName = "com.esq.dispatcher.contacts:type=REST,name=SLA")
public class SLARest extends BaseRest<SLA> {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final Set<String> FILTER_COLUMNS;
    private static final Set<String> ORDER_COLUMNS;
    private static final Set<String> SEARCH_COLUMNS;
    private ContactUserRest userRest;
    private SLARepository slaRepository;

    static {
        FILTER_COLUMNS = new HashSet<String>(Arrays.asList(
                NAME, DESCRIPTION));

        ORDER_COLUMNS = new HashSet<String>(Arrays.asList(
                ID, NAME, DESCRIPTION,PARAM_TENANT_ID));

        SEARCH_COLUMNS = new HashSet<String>(Arrays.asList(
                NAME, DESCRIPTION));
    }

    @Autowired
    public SLARest(SLARepository slaRepository) {
        super(SLA.class, slaRepository);
        this.slaRepository=slaRepository;
    }


    @Autowired
    public void setUserRest(ContactUserRest userRest) {
        log.trace("setUserRest; {}", userRest);
        this.userRest = userRest;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<SLA> create(@RequestBody SLA sla, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        int result = 0;
        try {
            result = slaRepository.slaNameSearch(sla.getName().trim(),sla.getTenantId());
        } catch (Exception e1) {
            log.warn("create;exception={}",  e1.getMessage());
        }
        if (result != 0) {
            logException("create;exception={}", new RestException(RestErrorMessages.CREATE_SLA_FAILED,"Failed to create resource"));
            throw new RestException(RestErrorMessages.CREATE_SLA_FAILED, "Failed to create resource", sla.getName().trim());
        }
        ResponseEntity<SLA> response= super.create(sla);
        SLA createdSLA=(SLA) super.readById(sla.getId()).getBody();
        log.debug("create; response={}",response);
        try {
            userRest.createAuditLog(TargetOperations.SLA_TARGET_NAME,
                    TargetOperations.CREATE_OPERATION, ContactAuditUtil.convertToJSON(
                            createdSLA, TargetOperations.CREATE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("create;exception={}",e);
        }

        return response;
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<SLA> update(@PathVariable("id") long id,@RequestBody SLA sla, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        SLA savedSLA=(SLA) super.readById(id).getBody();
        int result = 0;
        if(!savedSLA.getName().trim().equalsIgnoreCase(sla.getName().trim())){
            try {
                result = slaRepository.slaNameSearch(sla.getName().trim(),sla.getTenantId());
            } catch (Exception e1) {
                log.warn("update;exception={}", e1.getMessage());
            }
            if (result != 0) {
                logException("update;exception={}", new RestException(RestErrorMessages.UPDATE_SLA_FAILED,"Failed to update resource"));
                throw new RestException(RestErrorMessages.UPDATE_SLA_FAILED, "Failed to update resource", sla.getName().trim());
            }
        }
        ResponseEntity<SLA> response = null;
        response=super.update(id, sla);
        SLA newSLA=(SLA) super.readById(id).getBody();
        log.debug("update;response={}",response);
        try {
            userRest.createAuditLog(
                    TargetOperations.SLA_TARGET_NAME,
                    TargetOperations.UPDATE_OPERATION,
                    ContactAuditUtil.compareObject(savedSLA, newSLA), headers.get("userId").get(0));
        }
        catch (Exception e) {
            log.warn("update;exception={}",e);
        }
        return response;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public ResponseEntity<Object[]> list(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) {
        return super.list(request,headers);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") long id, @RequestHeader org.springframework.http.HttpHeaders headers) {
        ResponseEntity<SLA> res=super.readById(id);
        log.debug("deleteById;res={}",res);
        SLA objectSLA=(SLA) res.getBody();
        int result = 0;
        try {
            result = slaRepository.slaSearch(id);
        } catch (Exception e1) {
            log.warn("deleteById;exception={}", e1);
        }
        if (result != 0) {
            logException("deleteById;exception={}", new RestException(RestErrorMessages.DELETE_NOT_ALLOWED_SLA,"Failed to delete resource"));
            throw new RestException(RestErrorMessages.DELETE_NOT_ALLOWED_SLA,"Failed to delete resource");
        }
        try {
            super.deleteById(id);
            userRest.createAuditLog(TargetOperations.SLA_TARGET_NAME,
                    TargetOperations.DELETE_OPERATION, ContactAuditUtil.convertToJSON(
                            objectSLA, TargetOperations.DELETE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("deleteById;exception={}",e);
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
