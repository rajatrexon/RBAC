package com.esq.rbac.service.jointables.rolepermission.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class RolePermissionPK implements Serializable{
    private Integer roleId;
    private Integer operationId;

    public int hashCode() {
        return (int) this.roleId.hashCode() + (int) this.operationId.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof RolePermissionPK))
            return false;
        RolePermissionPK pk = (RolePermissionPK) obj;
        return pk.roleId.equals(this.roleId) && pk.operationId.equals(this.operationId);
    }
}
