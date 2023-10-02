package com.esq.rbac.web.vo;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Group {


    private Integer groupId;
    @Size(min = 1, max = 300)
    //@SpecialCharValidator
    private String name;
    @Size(min = 0, max = 500)
    private String description;
    private List<String> labels;
    /*@JsonManagedReference("GroupVariables")
	private Set<Variable> variables;*/
    private Restriction restrictions;
    private Calendar calendar;
    private Set<Integer> roleIds;

    @JsonManagedReference("scopeDefinitions")
    private Set<ScopeDefinition> scopeDefinitions;

    @JsonManagedReference("AttributesDataGroup")
    private Set<AttributesData> attributesData;
    private Long tenantId;
    private Integer createdBy;
    private Date createdOn;
    private Integer updatedBy;
    private Date updatedOn;
    private boolean isTemplate = false;

    public Group() {
    }

    public Group(String name) {
        setName(name);
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

  /*  public Set<Variable> getVariables() {
		return variables;
	}

	public void setVariables(Set<Variable> variables) {
		this.variables = variables;
	}*/

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public Restriction getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Restriction restrictions) {
        this.restrictions = restrictions;
    }

    public Set<Integer> getRolesIds() {
        return roleIds;
    }

    public void setRolesIds(Set<Integer> roleIds) {
        this.roleIds = roleIds;
    }

    public Set<ScopeDefinition> getScopeDefinitions() {
        if (scopeDefinitions != null && !(scopeDefinitions instanceof TreeSet<ScopeDefinition>)) {
            scopeDefinitions = new TreeSet<ScopeDefinition>(scopeDefinitions);
        }
        return scopeDefinitions;
    }

    public void setScopeDefinitions(Set<ScopeDefinition> scopeDefinitions) {
        this.scopeDefinitions = scopeDefinitions;
    }

    public Set<AttributesData> getAttributesData() {
        if (attributesData != null && !(attributesData instanceof TreeSet)) {
            attributesData = new TreeSet<AttributesData>(attributesData);
        }
        return attributesData;
    }

    public void setAttributesData(Set<AttributesData> attributesData) {
        this.attributesData = attributesData;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public boolean getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Group{groupId=").append(groupId);
        sb.append("; name=").append(name);
        sb.append("; description=").append(description);
        sb.append("; labels=").append(labels);
        /*        sb.append("; variables=").append(getVariables()); */
        sb.append("; roleIds=").append(roleIds);
        sb.append("; scopeDefinitions=").append(scopeDefinitions);
        sb.append("; calendar=").append(calendar);
        sb.append("; attributesData=").append(attributesData);
        sb.append("; tenantId=").append(tenantId);
        sb.append("; isTemplate=").append(isTemplate);
        sb.append("; createdOn=").append(createdOn == null ? "" : createdOn);
        sb.append("; createdBy=").append(createdBy == null ? "0" : createdBy);
        sb.append("; updatedOn=").append(updatedOn == null ? "" : updatedOn);
        sb.append("; updatedBy=").append(updatedBy == null ? "0" : updatedBy);
        sb.append("}");
        return sb.toString();
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }
}
