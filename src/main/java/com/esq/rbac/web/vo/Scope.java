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

public class Scope {

    private Integer scopeId;
//    @Size(min = 1, max = 100)
//    @SpecialCharValidator
    private String name;
//    @Size(min = 1, max = 100)
//    @SpecialCharValidator
    private String scopeKey;
//    @Size(min = 0, max = 500)
    private String description;
    private Integer applicationId;
    private boolean isMandatory;
    private Set<String> labels;
    private Integer updatedBy;
    private Date updatedOn;
    private Integer createdBy;
    private Date createdOn;
    
//    @Transient
    String displayState = "block";
    
    public String getDisplayState() {
		return displayState;
	}

	public void setDisplayState(String displayState) {
		this.displayState = displayState;
	}
	
    public Integer getScopeId() {
        return scopeId;
    }

    public void setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScopeKey() {
		return scopeKey;
	}

	public void setScopeKey(String scopeKey) {
		this.scopeKey = scopeKey;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Scope{scopeId=").append(scopeId);
        sb.append("; name=").append(name);
        sb.append("; scopeKey=").append(scopeKey);
        sb.append("; description=").append(description);
        sb.append("; applicationId=").append(applicationId);
        sb.append("; isMandatory=").append(isMandatory);
        sb.append("; labels=").append(labels);
        sb.append("; displayState=").append(displayState);
        sb.append("; createdOn=").append(createdOn == null ? "" : createdOn);
        sb.append("; createdBy=").append(createdBy == null ? "0" : createdBy);
        sb.append("; updatedOn=").append(updatedOn == null ? "" : updatedOn);
        sb.append("; updatedBy=").append(updatedBy == null ? "0" : updatedBy);
        sb.append("}");
        return sb.toString();
    }
}
