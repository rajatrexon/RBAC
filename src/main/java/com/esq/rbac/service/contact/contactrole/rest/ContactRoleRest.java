package com.esq.rbac.service.contact.contactrole.rest;


import com.esq.rbac.service.base.rest.BaseRest;
import com.esq.rbac.service.contact.contactrole.domain.ContactRole;
import com.esq.rbac.service.contact.contactrole.repository.ContactRoleRepository;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/contactRole")
//@ManagedResource(objectName = "com.esq.rbac.service.user.contact.contactrole:type=REST,name=ContactRole")
public class ContactRoleRest extends BaseRest<ContactRole> {

    private ContactRoleRepository roleRepo;

    @Autowired
    public ContactRoleRest(ContactRoleRepository roleRepo) {
        super(ContactRole.class, roleRepo);
        this.roleRepo=roleRepo;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<List<ContactRole>> getRoles(@QueryParam("q") @DefaultValue("") String q, @RequestHeader org.springframework.http.HttpHeaders headers) {

        log.debug("getRoles; q={}", q);
        String tenantScope= getTenantScope(null,headers);
        String queryLowerCase = (q != null && q.isEmpty() == false)
                ? q.toLowerCase()
                : null;

        List<ContactRole> list = new LinkedList<>();
        List<ContactRole> dbList;
        if(tenantScope==null || tenantScope.trim().equals("") || tenantScope.trim().equals("[]")){
            dbList = roleRepo.getRoles();
        }else{
            dbList = roleRepo.getRoles(getTenantData(tenantScope));
        }

        if (dbList != null) {
            if (queryLowerCase == null) {
                list.addAll(dbList);
            } else {
                for (ContactRole role : dbList) {
                    if (role.getName().toLowerCase().contains(queryLowerCase)) {
                        list.add(role);
                    }
                }
            }
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<ContactRole> create(@RequestBody ContactRole role, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        try {
            ContactRole contactRole;
            if(role.getTenantId()==null ){
                contactRole= roleRepo.createFromName(role.getName());
            }else{
                contactRole= roleRepo.createFromName(role.getName(),role.getTenantId());
            }
            return ResponseEntity.ok(contactRole);
        } catch (Exception e) {
            log.error("create; exception={}", e.toString());
            log.debug("create; exception {}", e);
           // Todo throw new ConflictException(e.toString());
            throw new Exception(e);
        }
    }

    @Override
    protected Set<String> getFilterColumns() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Set<String> getSearchColumns() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Set<String> getOrderColumns() {
        // TODO Auto-generated method stub
        return null;
    }
}
