package com.esq.rbac.service.makerchecker.makercheckerlog.domain;
/**
 * The MakerCheckerLog is an Entity class
 * Used to store log data MakerChecker
 * makerCheckerId is foreign key here
 *
 * @author Pankaj
 * @version 1.0
 * @company liberin technologies pvt limited
 * @since 2018-12-05
 */


import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "rbac", name = "makerCheckerLog")
public class MakerCheckerLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "entityType")
    private String entityType;

    @NotNull
    @Column(name = "entityName")
    private String entityName;

    @NotNull
    @Column(name = "entityValue")
    private String entityValue;

    @NotNull
    @Column(name = "entityJson")
    private String entityJson;

    @Column(name = "transactionBy")
    private Integer transactionBy;

    @NotNull
    @Column(name = "transactionOn")
    @Convert(converter = UtcDateConverter.class)
    private Date transactionOn;

    @Column(name = "createdBy")
    private Integer createdBy;

    @NotNull
    @Column(name = "createdOn")
    @Convert(converter = UtcDateConverter.class)
    private Date createdOn;

    @Column(name = "rejectReason")
    private String rejectReason;

    @Column(name = "entityStatus")
    private Integer entityStatus;

    @NotNull
    @Column(name = "organizationId", nullable = false)
    private Long organizationId;

    @Column(name = "isValid")
    private Boolean isValid;

    @Column(name = "entityId")
    private Integer entityId;

    @NotNull
    @Column(name = "makerCheckerId")
    private Long makerCheckerId;

    @Column(name = "tenantId")
    private Long tenantId;
}
