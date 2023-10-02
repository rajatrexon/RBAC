/*
 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

import java.util.Date;
import java.util.Set;

public class Role {

    private Integer roleId;
    private Integer applicationId;
//    @Size(min = 1, max = 32)
//    @SpecialCharValidator
    private String name;
    //@Size(min = 0, max = 500)
    private String description;
    private Set<String> labels;
    private Set<Integer> operationIds;
    private Integer createdBy;
    private Date createdOn;
    private Integer updatedBy;
    private Date updatedOn;
    

    public Role() {
        // empty
    }

    public Role(String name, Application application) {
        this.name = name;
        this.applicationId = application.getApplicationId();
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public Set<Integer> getOperationIds() {
        return operationIds;
    }

    public void setOperationIds(Set<Integer> operationIds) {
        this.operationIds = operationIds;
    }

    public Integer getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Integer getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role{roleId=").append(roleId);
        sb.append("; applicationId=").append(applicationId);
        sb.append("; name=").append(name);
        sb.append("; description=").append(description);
        sb.append("; operationIds=").append(operationIds);
        sb.append("; createdOn=").append(createdOn == null ? "" : createdOn);
        sb.append("; createdBy=").append(createdBy == null ? "0" : createdBy);
        sb.append("; updatedOn=").append(updatedOn == null ? "" : updatedOn);
        sb.append("; updatedBy=").append(updatedBy == null ? "0" : updatedBy);
        sb.append('}');
        return sb.toString();
    }
}
