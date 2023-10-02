package com.esq.rbac.service.restriction.domain;

import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.restrictionvalidator.RestrictionValidation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "restriction", schema = "rbac")
@RestrictionValidation
public class Restriction {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "restrictionIdGenerator")
	@TableGenerator(name = "restrictionIdGenerator", schema = "rbac", table = "idSequence",
			pkColumnName = "idName", valueColumnName = "idValue",
			pkColumnValue = "restrictionId", initialValue = 1, allocationSize = 10)
	@Column(name = "restrictionId")
	private Integer restrictionId;

	@Column(name = "timeZone", nullable = true, length = 32)
	private String timeZone;

	@Column(name = "dayOfWeek", nullable = false, length = 7)
	private String dayOfWeek;

	@Column(name = "hours", nullable = true, length = 24)
	private String hours;

	@Column(name = "fromDate", nullable = true, length = 10)
	private String fromDate;

	@Column(name = "toDate", nullable = true, length = 10)
	private String toDate;

	@ElementCollection
	@CollectionTable(name = "allowedIP", schema = "rbac", joinColumns = @JoinColumn(name = "restrictionId"))
	@Column(name = "ipAddressRange")
	private List<String> allowedIPs;


	@ElementCollection
	@CollectionTable(name = "disallowedIP", schema = "rbac", joinColumns = @JoinColumn(name = "restrictionId"))
	@Column(name = "ipAddressRange")
	private List<String> disallowedIPs;

	@OneToOne(mappedBy = "restrictions")
	private User userId;

}

