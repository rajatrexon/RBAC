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
package com.esq.rbac.service.contact.helpers;

import com.esq.rbac.service.contact.contactrole.domain.ContactRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "ContactRole")
public class ObjectContactRoleContainer {

    private List<ContactRole> roles = new ArrayList<ContactRole>();

    public List<ContactRole> getRoles() {
        return roles;
    }

    @XmlElement(name = "name")
	@JsonProperty(value = "name")
    public void setRoles(List<ContactRole> roles) {
        this.roles = roles;
    }
}
