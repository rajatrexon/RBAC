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

package com.esq.rbac.service.externaldb.service;

import com.esq.rbac.service.session.constraintdata.domain.ConstraintData;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;
import java.util.Map;

public interface ExternalDbDal {

	public ConstraintData[] getData(String appName, String query, MultivaluedMap<String, String> parameters);
	public List<Map<String, Object>> getCustomData(String appName, String query, Map<String, String> filter, String constraintName, List<?> restrictionIds );
	public boolean executeProc(String appName, String procStatement,
			Object... arguments);
	List<Map<String, Object>> getRowSetData(String appName, String query, Map<String, String> filters);
	void validateDate(String dateString, String format);
}
