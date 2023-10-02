package com.esq.rbac.service.contact.partydepartment.rest;


import com.esq.rbac.service.targetoperations.TargetOperations;
import com.esq.rbac.service.contact.helpers.ContactUserRest;
import com.esq.rbac.service.contact.helpers.PartyDepartmentContainer;
import com.esq.rbac.service.contact.partydepartment.domain.PartyDepartment;
import com.esq.rbac.service.contact.partydepartment.repository.PartyDepartmentRepository;
import com.esq.rbac.service.util.ContactAuditUtil;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/department")
public class PartyDepartmentRest {

    private PartyDepartmentRepository repository;
    private ContactUserRest userRest;
    @Autowired
    public void setUserRest(ContactUserRest userRest) {
        log.trace("setUserRest;");
        this.userRest = userRest;
    }

    @Autowired
    public void setRepository(PartyDepartmentRepository repository) {
        this.repository = repository;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<List<PartyDepartment>> getPartyDepartmentsJson(@QueryParam("q") String q) {
        log.debug("getPartyDepartmentsJson()");
        List<PartyDepartment> departments = repository.getDepartments(q);
        return ResponseEntity.ok(departments);
    }

    @GetMapping(produces = MediaType.APPLICATION_XML)
    public ResponseEntity<PartyDepartmentContainer> getPartyDepartmentsXml(@QueryParam("q") String q) {
        log.debug("getPartyDepartmentsXml()");
        List<PartyDepartment> departments = repository.getDepartments(q);
        return ResponseEntity.ok(new PartyDepartmentContainer(departments));
    }

    @PostMapping(consumes = MediaType.TEXT_PLAIN, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<PartyDepartment> create(@QueryParam("name") String name, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        log.debug("create; name={}", name);
        PartyDepartment department = repository.createFromName(name);
        try {
            userRest.createAuditLog(TargetOperations.PARTY_TARGET_NAME,TargetOperations.CREATE_OPERATION, ContactAuditUtil.convertToJSON(department, TargetOperations.CREATE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("create;exception={}",e);
        }
        return ResponseEntity.ok(department);

    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<PartyDepartment> update(@PathVariable("id") long id,@RequestBody PartyDepartment department, @RequestHeader HttpHeaders headers) throws Exception {
        PartyDepartment savedDepartment=repository.readById(id);
        PartyDepartment newDepartment=repository.update(id, department);
        try {
            userRest.createAuditLog(TargetOperations.PARTY_TARGET_NAME,TargetOperations.UPDATE_OPERATION, ContactAuditUtil.compareObject(savedDepartment, newDepartment), headers.get("userId").get(0));
        }
        catch (Exception e) {
            log.warn("update;exception={}",e);
        }
        return ResponseEntity.ok(newDepartment);
    }
}
