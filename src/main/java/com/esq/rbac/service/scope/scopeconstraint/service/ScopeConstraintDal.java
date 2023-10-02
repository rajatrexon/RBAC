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

package com.esq.rbac.service.scope.scopeconstraint.service;

import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import com.esq.rbac.service.util.dal.Options;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

public interface ScopeConstraintDal {

	ScopeConstraint getById(int constraintId);
	
	void clearCache();
	
	void clearQueryCache();

	ScopeConstraint getByScopeName(String scopeName);

	ScopeConstraint getByScopeId(int scopeId);

	void executeExternaUpdate(Group group);

	List<ScopeConstraint> getConstraints(Options options);
	
	String getAttributeDataByScopeConstraintId(
			Integer scopeConstraintId, Options options, Integer userId, HttpServletRequest servletRequest);
	
	String getAttributeDataByAttributeId(Integer attributeId, Options options, Integer userId, HttpServletRequest servletRequest);

	List<ScopeConstraint> getScopeConstraintsForQueryBuilder(String scopeKey);


}
