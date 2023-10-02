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

import com.esq.rbac.service.contact.partydepartment.domain.PartyDepartment;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "partyDepartments")
public class PartyDepartmentContainer {

    private List<PartyDepartment> list = new ArrayList<PartyDepartment>();

    public PartyDepartmentContainer() {
    }

    public PartyDepartmentContainer(List<PartyDepartment> list) {
        this.list = list;
    }

    public List<PartyDepartment> getList() {
        return list;
    }

    @XmlElement(name = "partyDepartment")
	@JsonProperty(value = "partyDepartment")
    public void setList(List<PartyDepartment> list) {
        this.list = list;
    }
}
