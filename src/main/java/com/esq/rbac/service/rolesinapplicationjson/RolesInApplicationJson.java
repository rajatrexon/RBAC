package com.esq.rbac.service.rolesinapplicationjson;

import java.util.Set;
import java.util.TreeSet;

public class RolesInApplicationJson implements Comparable<RolesInApplicationJson>{

	private Integer applicationId;
	private String applicationName;
	private Set<RoleJson> roles = new TreeSet<RoleJson>();
	
	public RolesInApplicationJson(){
		
	}
	
	public RolesInApplicationJson(Integer applicationId , String applicationName){
		this.applicationId = applicationId;
		this.applicationName = applicationName;
	}
	
	public Integer getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Integer applicationId) {
		this.applicationId = applicationId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public Set<RoleJson> getRoles() {
		return roles;
	}

	public void addRole(Integer roleId, String roleName) {
		RoleJson roleJson = new RoleJson();
		roleJson.setRoleId(roleId);
		roleJson.setRoleName(roleName);
		roles.add(roleJson);
	}

	@Override
	public int compareTo(RolesInApplicationJson o) {
		if (o instanceof RolesInApplicationJson) {
			return o.applicationName.compareToIgnoreCase(applicationName);
		}
		return 0;
	}
	
	@Override
	public int hashCode() {
		return applicationId.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof RolesInApplicationJson) {
			return this.applicationId.equals(((RolesInApplicationJson) o).getApplicationId());
		}
		return false;
	}

	public static class RoleJson implements Comparable<RoleJson> {

		private Integer roleId;
		private String roleName;
		
		public Integer getRoleId() {
			return roleId;
		}

		public void setRoleId(Integer roleId) {
			this.roleId = roleId;
		}

		public String getRoleName() {
			return roleName;
		}

		public void setRoleName(String roleName) {
			this.roleName = roleName;
		}


		@Override
		public int compareTo(RoleJson o) {
			if (o instanceof RoleJson) {
				return o.roleName.compareToIgnoreCase(roleName);
			}
			return 0;
		}
		
		@Override
		public int hashCode() {
			return roleId.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof RoleJson) {
				return this.roleId.equals(((RoleJson) o).getRoleId());
			}
			return false;
		}
	}
}
