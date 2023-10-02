/*
 * Copyright (c)2017 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.service.application.childapplication.childapplicationlicense;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@Table( name = "childApplicationLicense",schema="rbac")
public class ChildApplicationLicense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "childAppLicenseId")
	private Integer childAppLicenseId;
	@Column(name = "license")
	private String license;
	@Column(name = "additionalData")
	private String additionalData;
	@Column(name = "createdBy")
	private Integer createdBy;
	@Column(name = "createdOn")
	@Convert(converter = UtcDateConverter.class)
	private Date createdOn;
	@Column(name = "updatedBy")
	private Integer updatedBy;
	@Column(name = "updatedOn")
	@Convert(converter = UtcDateConverter.class)
	private Date updatedOn;

	@OneToOne(mappedBy = "childApplicationLicense", cascade = CascadeType.ALL)
	private ChildApplication childApplication;

	public ChildApplicationLicense() {

	}
}
