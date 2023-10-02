/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.vo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.cache.annotation.Cacheable;

import java.util.HashSet;

@Cacheable("attributesData")
public class AttributesData implements Comparable<AttributesData> {

	private Integer attributeDataId;
	@JsonBackReference("AttributesDataGroup")
    private Group group;
	@JsonBackReference("AttributesDataUser")
    private User user;
	private Integer attributeId;
	private String valueReferenceId;
	private String attributeDataValue;
	
	public AttributesData(){
		
	}

	public Integer getAttributeDataId() {
		return attributeDataId;
	}

	public void setAttributeDataId(Integer attributeDataId) {
		this.attributeDataId = attributeDataId;
	}

	public Group getGroup() {
		return group;
	}

	public final void setGroup(Group group) {
        // set new group
        this.group = group;

        // add this group to newly set attribute data
        if (this.group != null) {
            if (this.group.getAttributesData() == null) {
                this.group.setAttributesData(new HashSet<AttributesData>());
            }
            if (this.group.getAttributesData().contains(this) == false) {
                this.group.getAttributesData().add(this);
            }
        }
    }

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		 // set new group
        this.user = user;

        // add this group to newly set attribute data
        if (this.user != null) {
            if (this.user.getAttributesData() == null) {
                this.user.setAttributesData(new HashSet<AttributesData>());
            }
            if (this.user.getAttributesData().contains(this) == false) {
                this.user.getAttributesData().add(this);
            }
        }
	}

	public Integer getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(Integer attributeId) {
		this.attributeId = attributeId;
	}

	public String getValueReferenceId() {
		return valueReferenceId;
	}

	public void setValueReferenceId(String valueReferenceId) {
		this.valueReferenceId = valueReferenceId;
	}

	public String getAttributeDataValue() {
		return attributeDataValue;
	}

	public void setAttributeDataValue(String attributeDataValue) {
		this.attributeDataValue = attributeDataValue;
	}
	
	@Override
	public int compareTo(AttributesData o) {
		if (this.attributeId != null) {
			return this.attributeId.compareTo(o.attributeId);
		}
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
	    sb.append("AttributesData{attributeDataId=").append(attributeDataId);
	    sb.append("; user=").append(user!=null?user.getUserName():"");
	    sb.append("; group=").append(group!=null?group.getName():"");
		sb.append("; attributeId=").append(attributeId);
		sb.append("; valueReferenceId=").append(valueReferenceId);
		sb.append("; attributeDataValue=").append(attributeDataValue);
		sb.append("}");
		return sb.toString();
	}
}
