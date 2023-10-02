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

package com.esq.rbac.service.scope.scopeconstraint.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.springframework.cache.annotation.Cacheable;


@Entity
@Table(schema = "rbac", name = "scopeConstraint")
//Todo@Cacheable
public class ScopeConstraint {


	@Id
	@Column(name = "constraintId")

	private Integer constraintId;

	@Size(min = 1, max = 32)
	@Column(name = "applicationName", nullable = false)
	private String applicationName;


	@Column(name = "applicationId")
	private Integer applicationId;

	@Size(min = 1, max = 128)
	@Column(name = "scopeName", nullable = false)
	private String scopeName;



	@Column(name = "scopeId")

	private Integer scopeId;
	@Size(min = 0, max = 64)
	@Column(name = "description")
	private String description;

	@Size(min = 0, max = 32)
	@Column(name = "dataType")
	private String dataType;

	@Size(min = 0, max = 32)
	@Column(name = "regex")
	private String regex;


	@Column(name = "lengthData")
	private Integer lengthData;
	@Size(min = 1, max = 256)
	@Column(name = "sqlQuery")
	private String sqlQuery;
	@Column(name = "updateSqlQuery")
	private String updateSqlQuery;


	@Column(name = "listPath")
	private String listPath;


	@Column(name = "updatePath")
	private String updatePath;


	@Column(name = "sourceType")
	private String sourceType;

	@Size(min = 1, max = 256)
    @Column(name = "comparators")
	private String comparators;

	@Size(min = 0, max = 512)
	@Column(name = "comments")
	private String comments;



	@Column(name = "isNegation")
	private Boolean isNegation;

	public Integer getConstraintId() {
		return constraintId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public Integer getApplicationId() {
		return applicationId;
	}

	public String getScopeName() {
		return scopeName;
	}

	public Integer getScopeId() {
		return scopeId;
	}

	public String getDescription() {
		return description;
	}

	public String getDataType() {
		return dataType;
	}

	public String getRegex() {
		return regex;
	}

	public Integer getLengthData() {
		return lengthData;
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public String getComparators() {
		return comparators;
	}

	public String getComments() {
		return comments;
	}

	public String getSourceType() {
		return sourceType;
	}

	public String getUpdatePath() {
		return updatePath;
	}

	public String getListPath() {
		return listPath;
	}

	public String getUpdateSqlQuery() {
		return updateSqlQuery;
	}
	
	public Boolean getIsNegation() {
		return isNegation;
	}
	
	public void hideSqlQuery(){
		sqlQuery = "";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ScopeConstraint{constraintId=").append(constraintId);
		sb.append("; applicationName=").append(applicationName);
		sb.append("; applicationId=").append(applicationId);
		sb.append("; scopeName=").append(scopeName);
		sb.append("; scopeId=").append(scopeId);
		sb.append("; description=").append(description);
		sb.append("; dataType=").append(dataType);
		sb.append("; regex=").append(regex);
		sb.append("; lengthData=").append(lengthData);
		sb.append("; sqlQuery=").append("******");
		sb.append("; updateSqlQuery=").append(updateSqlQuery);
		sb.append("; listPath=").append(listPath);
		sb.append("; updatePath=").append(updatePath);
		sb.append("; sourceType=").append(sourceType);
		sb.append("; comparators=").append(comparators);
		sb.append("; comments=").append(comments);
		sb.append("; isNegation=").append(isNegation);
		sb.append("}");
		return sb.toString();
	}
}
