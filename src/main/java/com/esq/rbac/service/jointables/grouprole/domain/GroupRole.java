package com.esq.rbac.service.jointables.grouprole.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(schema = "rbac",name = "groupRole")
@IdClass(GroupRolePK.class)
@Data
public class GroupRole implements Serializable {
    @Id
    private Long groupId;
    @Id
    private Long roleId;
}
