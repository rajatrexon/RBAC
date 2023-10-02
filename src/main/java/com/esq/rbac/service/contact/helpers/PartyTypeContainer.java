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

import com.esq.rbac.service.contact.party.partytype.domain.PartyType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "partyTypes")
public class PartyTypeContainer {

    private List<PartyType> partyRuleType = new ArrayList<PartyType>();
    
    public PartyTypeContainer() {
    }

    public PartyTypeContainer(List<PartyType> partyRuleType) {
        this.partyRuleType = partyRuleType;
    }

    public List<PartyType> getPartyType() {
        return partyRuleType;
    }

    @XmlElement(name = "partyType")
	@JsonProperty(value = "partyType")
    public void setPartyType(List<PartyType> partyType) {
        this.partyRuleType = partyType;
    }
}
