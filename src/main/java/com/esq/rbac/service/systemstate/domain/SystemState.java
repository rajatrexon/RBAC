package com.esq.rbac.service.systemstate.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "rbac", name = "systemState")
public class SystemState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "systemData")
    private String systemData;
}
