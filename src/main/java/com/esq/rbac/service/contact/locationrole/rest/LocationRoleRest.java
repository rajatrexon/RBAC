/*
 * Copyright ©2012 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.service.contact.locationrole.rest;


import com.esq.rbac.service.contact.locationrole.repository.LocationRoleRepository;
import com.esq.rbac.service.contact.helpers.LocationRolesContainer;
import com.esq.rbac.service.contact.locationrole.domain.LocationRole;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/locationRole")
@Slf4j
public class LocationRoleRest {

    @Autowired
    private LocationRoleRepository locationRoleRepo;

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<List<LocationRole>> locationRoles() {
        log.trace("locationRoles;");
        LocationRolesContainer list = null;

        List<LocationRole> roles = locationRoleRepo.getRoles();

        if (roles != null) {
            list = new LocationRolesContainer();
            list.setLocationRoles(roles);
        }
        return ResponseEntity.ok(roles);
    }
}
