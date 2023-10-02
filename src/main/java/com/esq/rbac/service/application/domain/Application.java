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
package com.esq.rbac.service.application.domain;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.role.targetsubdomain.domain.Target;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.esq.rbac.service.util.UtcDateConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

//Todo@Customizer(ApplicationVariableCustomizer.class)

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@Table(name = "application",schema = "rbac")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "applicationIdGenerator")
    @TableGenerator(name = "applicationIdGenerator", schema = "rbac", table = "idSequence",pkColumnName = "idName",valueColumnName = "idValue"
            , pkColumnValue = "applicationId",initialValue = 1, allocationSize = 10)
    @Column(name = "applicationId")
    private Integer applicationId;

    @SpecialCharValidator
    @Column(name = "name", nullable = false, length = 32)
    private String name;


    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "ssoAllowed")
    private Boolean ssoAllowed;

    @ElementCollection
    @CollectionTable(schema = "rbac", name = "label", joinColumns = @JoinColumn(name = "applicationId"))
    @Column(name = "labelName",length = 32)
    private Set<String> labels;
    /*@JsonManagedReference("ApplicationVariables")
    private Set<Variable> variables;*/


    @Valid
    @JsonManagedReference("ApplicationTargets")
//    @OneToMany(fetch = FetchType.EAGER,mappedBy = "application", orphanRemoval = true, cascade = CascadeType.ALL)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "application", cascade = CascadeType.ALL)
    private Set<Target> targets;

    @Valid
    @JsonManagedReference("ApplicationChild")
//    @OneToMany(mappedBy = "application",fetch = FetchType.EAGER,orphanRemoval = true, cascade = CascadeType.ALL)
    @OneToMany(mappedBy = "application",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ChildApplication> childApplications;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "createdOn")
    @Convert(converter = UtcDateConverter.class)
    private Date createdOn;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    @Column(name = "updatedOn")
    @Convert(converter = UtcDateConverter.class)
    private Date updatedOn;
    // private String homeUrl;
    // private String serviceUrl;

    public Application() {
        // empty
    }



    public Application(String name) {
        setName(name);
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

    public Boolean getSsoAllowed() {
        return ssoAllowed;
    }

    public void setSsoAllowed(Boolean ssoAllowed) {
        this.ssoAllowed = ssoAllowed;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

//    public Set<Variable> getVariables() {
//        return variables;
//    }
//
//    public void setVariables(Set<Variable> variables) {
//        this.variables = variables;
//    }



    public Set<Target> getTargets() {
        if (targets != null && !(targets instanceof TreeSet)) {
            targets = new TreeSet<Target>(targets);
        }
        return targets;
    }



    public void setTargets(Set<Target> targets) {
        this.targets = targets;
    }

    public Set<ChildApplication> getChildApplications() {
        if (childApplications != null && !(childApplications instanceof TreeSet)) {

            childApplications = new TreeSet<ChildApplication>(childApplications);
        }
        return childApplications;
    }

    @JsonIgnore
    public ChildApplication getChildApplicationById(Integer childApplicationId){
        if(childApplications!=null && !childApplications.isEmpty()){
            for(ChildApplication c: childApplications){
                if(c.getChildApplicationId()!=null && c.getChildApplicationId().equals(childApplicationId)){
                    return c;
                }
            }
        }
        return null;
    }

    public void setChildApplications(Set<ChildApplication> childApplications) {
        this.childApplications = childApplications;
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
    /*
    public String getHomeUrl() {
		return homeUrl;
	}

	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}
    
    public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
*/
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Aplication{applicationId=").append(applicationId);
        sb.append("; name=").append(name);
        sb.append("; description=").append(description);
        sb.append("; labels=").append(labels);
        /*  sb.append("; variables=").append(variables);*/
        /*  sb.append("; targets=").append(targets);*/
        sb.append("; createdOn=").append(createdOn == null ? "" : createdOn);
        sb.append("; createdBy=").append(createdBy == null ? "0" : createdBy);
        sb.append("; updatedOn=").append(updatedOn == null ? "" : updatedOn);
        sb.append("; updatedBy=").append(updatedBy == null ? "0" : updatedBy);
       /* sb.append("; homeUrl=").append(homeUrl == null ? "" : homeUrl);
        sb.append("; serviceUrl=").append(serviceUrl == null ? "" : serviceUrl);*/
        sb.append("}");
        return sb.toString();
    }
}
