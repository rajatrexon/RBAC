package com.esq.rbac.service.scope.scopeBuilderdefault.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(schema = "rbac", name = "scopeBuilderDefaults")
@Data
public class ScopeBuilderDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer scopeBuilderId;

    private String name;

    private Integer scopeId;

    private String scopeKey;

    private String lhsJson;

    private String rhsJson;

    private boolean isEnabled;

    private Integer createdBy;

    @Convert(converter = UtcDateConverter.class)
    private Date createdOn;

    private Integer updatedBy;

    @Convert(converter = UtcDateConverter.class)
    private Date updatedOn;

}

