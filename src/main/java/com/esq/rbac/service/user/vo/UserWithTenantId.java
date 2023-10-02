package com.esq.rbac.service.user.vo;

import com.esq.rbac.service.user.domain.User;

public class UserWithTenantId extends User {
    private Long tenantId;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
