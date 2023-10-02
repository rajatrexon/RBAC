package com.esq.rbac.service.jointables.operationscope.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(schema = "rbac", name = "operationScope")
@IdClass(OperationScopePK.class)
@Data
public class OperationScope implements Serializable {
    @Id
    private Integer operationId;

    @Id
    private Integer scopeId;

}
