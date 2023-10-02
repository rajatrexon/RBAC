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
package com.esq.rbac.service.contact.helpers;

import com.esq.rbac.service.contact.securityrole.domain.SecurityRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "securityRoles")
public class SecurityRoleContainer {

    private List<SecurityRole> list = new ArrayList<SecurityRole>();

    public SecurityRoleContainer() {
    }

    public SecurityRoleContainer(List<SecurityRole> list) {
        this.list = list;
    }

    public List<SecurityRole> getList() {
        return list;
    }

    @XmlElement(name = "securityRole")
	@JsonProperty(value = "securityRole")
    public void setList(List<SecurityRole> list) {
        this.list = list;
    }
}
