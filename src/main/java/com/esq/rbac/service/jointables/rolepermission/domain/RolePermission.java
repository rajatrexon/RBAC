package com.esq.rbac.service.jointables.rolepermission.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(schema = "rbac", name = "rolePermission")
@IdClass(RolePermissionPK.class)
public class RolePermission {

    @Id
    @Column(name = "roleId")
    private Integer roleId;

    @Id
    @Column(name = "operationId")
    private Integer operationId;
}
