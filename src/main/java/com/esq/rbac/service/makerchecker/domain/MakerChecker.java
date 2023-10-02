package com.esq.rbac.service.makerchecker.domain;

import com.esq.rbac.service.util.SpecialCharValidator;
import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Table(schema = "rbac", name = "makerChecker")
public class MakerChecker implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Transient
    String makerCheckerIdsForAction;
    @Transient
    Integer isApproveFlag;
    @Transient
    String transactionByName;
    @Transient
    String operation;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotNull
    private String entityType;
    @NotNull
    private String entityName;
    @NotNull
    private String entityValue;
    @NotNull
    private String entityJson;
    private Integer transactionBy;
    @NotNull
    @Convert(converter = UtcDateConverter.class)
    private Date transactionOn;
    private Integer createdBy;
    @NotNull
    @Convert(converter = UtcDateConverter.class)
    private Date createdOn;
    @Size(min = 0, max = 500)
    @Pattern(regexp = "^([^<>=]*)$", message = "Reject Reason should not have <,> and =")
    @SpecialCharValidator
    private String rejectReason;
    private Integer entityStatus;
    private Long organizationId;
    private Boolean isValid;
    private Integer entityId;
    private Long tenantId;
}

