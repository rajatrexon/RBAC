package com.esq.rbac.service.appcodemap.domain;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "rbac", name = "applicationCodesMap")
@Data
@NoArgsConstructor
public class ApplicationCodesMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "applicationId", nullable = false)
    private Integer applicationId;

    @Column(name = "codeId", nullable = false)
    private Integer codeId;

    @Column(name = "createdBy")
    private Integer createdBy;


}

