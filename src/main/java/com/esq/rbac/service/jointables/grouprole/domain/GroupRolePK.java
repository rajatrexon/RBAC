package com.esq.rbac.service.jointables.grouprole.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupRolePK implements Serializable {
    private Integer groupId;
    private Integer roleId;

    public int hashCode() {
        return (int) this.groupId.hashCode() + (int) this.roleId.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GroupRolePK))
            return false;
        GroupRolePK pk = (GroupRolePK) obj;
        return pk.groupId.equals(this.groupId) && pk.roleId.equals(this.roleId);
    }

}
