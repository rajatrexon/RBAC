package com.esq.rbac.web.vo;

public class Culture {
	
	Integer id;
	String shortName;
	String fullName;
	Boolean supported;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public Boolean getSupported() {
		return supported;
	}
	public void setSupported(Boolean supported) {
		this.supported = supported;
	}
	
	
	
}
