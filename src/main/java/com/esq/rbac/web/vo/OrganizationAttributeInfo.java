package com.esq.rbac.web.vo;

public class OrganizationAttributeInfo {
	
	
    private String attributeName;
    private String attributeValue;
    private Integer applicationId;
    private String applicationName;
    private Code code;
    public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	private Integer attributeId;
    private Long organizationId;
	private String timezone;
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public String getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
	public Integer getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(Integer applicationId) {
		this.applicationId = applicationId;
	}
	public Integer getAttributeId() {
		return attributeId;
	}
	public void setAttributeId(Integer attributeId) {
		this.attributeId = attributeId;
	}
	public Long getOrganizationId() {
		return organizationId;
	}
	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}
	
	public Code getCode() {
		return code;
	}
	public void setCode(Code code) {
		this.code = code;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
        sb.append("(name=").append(attributeName);
        sb.append(", value=").append(attributeValue);
        if(applicationName!=null && !applicationName.isEmpty()){
        	sb.append(", applicationName=").append(applicationName);
        }
        if(organizationId!=null && organizationId!=0){
        	sb.append(", organizationId=").append(organizationId);
        }
        
        sb.append(")");
        return sb.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof VariableInfo) {
			OrganizationAttributeInfo other = (OrganizationAttributeInfo) o;
			if (this.getAttributeName() != null && this.getAttributeName().equalsIgnoreCase(other.getAttributeName())) {
				if ((this.getApplicationName() + this.getOrganizationId())
						.equalsIgnoreCase(other.getApplicationName() + other.getOrganizationId())) {
					return true;
				}
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (this.getAttributeName() + this.getApplicationName() + this.getOrganizationId())
				.toLowerCase().hashCode();
	}
	
	

}
