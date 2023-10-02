package com.esq.rbac.service.jointables.operationscope.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class OperationScopePK implements Serializable {
    private Integer operationId;
    private Integer scopeId;

    public int hashCode() {
        return (int) this.operationId.hashCode() + (int) this.scopeId.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof OperationScopePK))
            return false;
        OperationScopePK pk = (OperationScopePK) obj;
        return pk.operationId.equals(this.operationId) && pk.scopeId.equals(this.scopeId);
    }
}
