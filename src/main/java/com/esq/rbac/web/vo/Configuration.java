package com.esq.rbac.web.vo;

public class Configuration {

	private String confKey;
	private String confValue;
	private String confType;
	private String confOrder;
	private String confGroup;
	private String isVisible;
	private String subGroup;
	public Configuration() {
		// TODO Auto-generated constructor stub
	}
	
	public Configuration(String confKey, String confValue, String confType, String confOrder, String confGroup, String subGroup) {
		this.confKey = confKey;
		this.confValue = confValue;
		this.confType = confType;
		this.confOrder = confOrder;
		this.confGroup = confGroup;
		this.isVisible = "0";
		this.subGroup = subGroup;
	}
	public String getConfKey() {
		return confKey;
	}
	public void setConfKey(String confKey) {
		this.confKey = confKey;
	}
	public String getConfValue() {
		return confValue;
	}
	public void setConfValue(String confValue) {
		this.confValue = confValue;
	}
	public String getConfType() {
		return confType;
	}
	public void setConfType(String confType) {
		this.confType = confType;
	}
	public String getConfOrder() {
		return confOrder;
	}
	public void setConfOrder(String confOrder) {
		this.confOrder = confOrder;
	}
	public String getConfGroup() {
		return confGroup;
	}
	public void setConfGroup(String confGroup) {
		this.confGroup = confGroup;
	}
	public String getIsVisible() {
		return isVisible;
	}
	public void setisVisible(String confShowHide) {
		this.isVisible = confShowHide;
	}

	public void setSubGroup(String subGroup) {
		this.subGroup = subGroup;
	}

	public String getSubGroup() {
		return subGroup;
	}

}
