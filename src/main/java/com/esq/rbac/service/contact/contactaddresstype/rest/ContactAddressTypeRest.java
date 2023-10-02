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
package com.esq.rbac.service.contact.contactaddresstype.rest;

import com.esq.rbac.service.contact.contactaddresstype.domain.ContactAddressType;
import com.esq.rbac.service.contact.contactaddresstype.repository.ContactAddressTypeRepository;
import com.esq.rbac.service.contact.helpers.ContactAddressTypesContainer;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressType")
public class ContactAddressTypeRest {


    private ContactAddressTypeRepository roleRepo;

    @Autowired
    public void setRoleRepo(ContactAddressTypeRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addressTypes() {
        ContactAddressTypesContainer container = new ContactAddressTypesContainer();
        List<ContactAddressType> list = roleRepo.getRoles();
        if (log.isDebugEnabled() && list != null) {
            log.debug("Roles: " + list.size());
        }
        if (list != null) {
            container.setRoles(list);
        }
        return Response.ok().entity(container).build();
    }
}
