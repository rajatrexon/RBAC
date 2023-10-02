package com.esq.rbac.service.scope.scopedefinition.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScopeDefinitionPK implements Serializable {
    private Integer groupId;
    private Integer scopeId;
}
