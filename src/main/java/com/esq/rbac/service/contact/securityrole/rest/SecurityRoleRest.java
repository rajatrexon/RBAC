/*
 * Copyright ©2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.service.contact.securityrole.rest;

import com.esq.rbac.service.contact.helpers.ContactUserRest;
import com.esq.rbac.service.contact.helpers.SecurityRoleContainer;
import com.esq.rbac.service.contact.securityrole.domain.SecurityRole;
import com.esq.rbac.service.contact.securityrole.repository.SecurityRoleRepository;
import com.esq.rbac.service.targetoperations.TargetOperations;
import com.esq.rbac.service.util.ContactAuditUtil;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/securityRole")
public class SecurityRoleRest {

    private SecurityRoleRepository repository;
    private ContactUserRest userRest;

    @Autowired
    public void setUserRest(ContactUserRest userRest) {
        log.trace("setUserRest;");
        this.userRest = userRest;
    }

    @Autowired
    public void setRepository(SecurityRoleRepository repository) {
        this.repository = repository;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<List<SecurityRole>> getSecurityRolesJson(@QueryParam("q") String q) {
        log.debug("getSecurityRolesJson");
        List<SecurityRole> securityRoles = repository.getSecurityRoles(q);
        return ResponseEntity.ok().body(securityRoles);
    }

    @GetMapping(produces = MediaType.APPLICATION_XML)
    public ResponseEntity<SecurityRoleContainer> getSecurityRolesXml(@QueryParam("q") String q) {
        log.debug("getSecurityRolesXml");
        List<SecurityRole> securityRoles = repository.getSecurityRoles(q);
        return ResponseEntity.ok().body(new SecurityRoleContainer(securityRoles));
        //return Response.ok().entity(new SecurityRoleContainer(securityRoles)).build();
    }

    // Todo Added PathParam
    @PostMapping(consumes = MediaType.TEXT_PLAIN, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<SecurityRole> create(@PathParam("name") String name, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        log.info("create; name={}", name);
        SecurityRole securityRoles = repository.createFromName(name);
        try {
            userRest.createAuditLog(TargetOperations.PARTY_TARGET_NAME, TargetOperations.CREATE_OPERATION, ContactAuditUtil.convertToJSON(securityRoles, TargetOperations.CREATE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("create;exception={}", e);
        }
        return ResponseEntity.ok().body(securityRoles);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<SecurityRole> update(@PathVariable("id") long id,@RequestBody SecurityRole securityRoles, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        SecurityRole savedSecurityRole = repository.readById(id);
        SecurityRole newSecurityRole = repository.update(id, securityRoles);
        try {
            userRest.createAuditLog(TargetOperations.PARTY_TARGET_NAME, TargetOperations.UPDATE_OPERATION, ContactAuditUtil.compareObject(savedSecurityRole, newSecurityRole), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("update;exception={}", e);
        }
        return ResponseEntity.ok().body(newSecurityRole);
    }

}
