package com.esq.rbac.service.organization.embedded;

import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.util.SpecialCharValidator;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationAttributeInfo {

    private String attributeName;
    private String attributeValue;
    private Integer applicationId;
    private String applicationName;
    private Code code;
    private Integer attributeId;
    private Long organizationId;
    private String timezone;
}
