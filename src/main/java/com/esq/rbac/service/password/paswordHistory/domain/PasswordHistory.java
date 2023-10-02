package com.esq.rbac.service.password.paswordHistory.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "passwordHistory", schema = "rbac")
@Data
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "passwordHistoryId")
    private Long passwordHistoryId;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "setTime", nullable = false)
    @Convert(converter = UtcDateConverter.class)
    private Date setTime;

    @Column(name = "passwordSalt", length = 200)
    private String passwordSalt;

    @Column(name = "passwordHash", length = 200)
    private String passwordHash;



}

