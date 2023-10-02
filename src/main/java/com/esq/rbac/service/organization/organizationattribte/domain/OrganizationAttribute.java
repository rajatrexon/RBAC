package com.esq.rbac.service.organization.organizationattribte.domain;

import com.esq.rbac.service.util.SpecialCharValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class OrganizationAttribute {
//
//    @Size(min = 1, max = 32)
//    @SpecialCharValidator
//    private String attributeName;
//    @Size(min = 1)
//    private String attributeValue;
//
//    private Integer applicationId;
//    private Integer attributeId;
//    private Long organizationId;
//    private String timezone;
//    private String attributeKey;
//    private Long codeId;
//}

@Entity
@Table(schema = "rbac", name = "organizationAttributes")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attributeId")
    private Integer attributeId;

    @Column(name = "attributeName", nullable = false, length = 32)
    private String attributeName;

    @Column(name = "attributeValue")
    private String attributeValue;

    @Column(name = "applicationId")
    private Integer applicationId;

    @Column(name = "organizationId")
    private Long organizationId;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "attributeKey")
    private String attributeKey;

    @Column(name = "codeId")
    private Long codeId;

    // Add getters and setters if needed
}
