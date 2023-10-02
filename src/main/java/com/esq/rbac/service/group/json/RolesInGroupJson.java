package com.esq.rbac.service.group.json;

import java.util.Set;
import java.util.TreeSet;

public class RolesInGroupJson implements Comparable<RolesInGroupJson>{

    private Integer groupId;
    private String groupName;
    private Set<RoleJson> roles = new TreeSet<RoleJson>();

    public RolesInGroupJson(){

    }

    public RolesInGroupJson(Integer groupId, String groupName){
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Set<RoleJson> getRoles() {
        return roles;
    }

    public void addRole(Integer roleId, String roleName , String applicationName) {
        RoleJson roleJson = new RoleJson();
        roleJson.setRoleId(roleId);
        roleJson.setRoleName(roleName);
        roleJson.setApplicationName(applicationName);
        roles.add(roleJson);
    }

    @Override
    public int compareTo(RolesInGroupJson o) {
        if (o instanceof RolesInGroupJson) {
            return o.groupName.compareToIgnoreCase(groupName);
        }
        return 0;
    }

    @Override
    public int hashCode() {
        return groupId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof RolesInGroupJson) {
            return this.groupId.equals(((RolesInGroupJson) o).getGroupId());
        }
        return false;
    }

    public static class RoleJson implements Comparable<RoleJson> {
        private Integer roleId;
        private String roleName;
        private String applicationName;

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

        public String getApplicationName() {
            return applicationName;
        }

        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        @Override
        public int compareTo(RoleJson o) {
            if (o instanceof RoleJson) {
                //return o.roleName.compareToIgnoreCase(roleName) + o.applicationName.compareToIgnoreCase(applicationName);
                //Fixing Jira issue: RBAC-2372
                return o.roleName.concat(o.applicationName).compareToIgnoreCase(roleName.concat(applicationName));
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
