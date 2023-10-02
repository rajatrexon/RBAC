/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.service.util;

import com.esq.rbac.service.externaldb.service.ExternalDbDal;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import com.esq.rbac.service.scope.scopeconstraint.service.ScopeConstraintDal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Scope("prototype")
public class ChildAppPermValidatorSC implements ChildAppPermValidator {

	private static final String SCOPE_CONSTRAINT_ID = "scopeConstraintId";
	private static final String SCOPE_CONSTRAINT_QUERY_PARAMS = "queryParams";
	private ScopeConstraint scopeConstraint;
	private ScopeConstraintDal scopeConstraintDal;
	private ExternalDbDal externalDbDal;
	private String appKey;
	private JsonNode validatorData;
	private List<String> queryParams = new LinkedList<String>();

	@Autowired
	public void setExternalDbDal(ExternalDbDal externalDbDal) {
		this.externalDbDal = externalDbDal;
	}

	@Autowired
	public void setScopeConstraintDal(ScopeConstraintDal scopeConstraintDal) {
		this.scopeConstraintDal = scopeConstraintDal;
	}

	public ScopeConstraintDal getScopeConstraintDal() {
		return scopeConstraintDal;
	}

	public boolean validate(String userName) {
		if (scopeConstraint != null) {
			Map<String, String> paramMap = new LinkedHashMap<String, String>();
			for (String param : queryParams) {
				if (param.equalsIgnoreCase("userId")) {
					paramMap.put(param, Lookup.getUserId(userName).toString());
				}
			}
			try{
				List<Map<String, Object>> result = externalDbDal.getRowSetData(
						scopeConstraint.getApplicationName(),
						scopeConstraint.getSqlQuery(), paramMap);
				if (result != null) {
					String isValid = (String) result.get(0).get("result");
					if ("true".equalsIgnoreCase(isValid)) {
						return true;
					} else {
						return false;
					}
				}
				log.info("validate; result={};", result);
			}
			catch(Exception e){
				log.error("validate; Exception={};", e); 
			}
		}
		return true;
	}

	@Override
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getAppKey() {
		return appKey;
	}

	@Override
	public synchronized void setValidatorData(JsonNode validatorData) {
		this.validatorData = validatorData;
		if (this.validatorData != null) {
			try {
				Integer scopeConstraintId = Integer.valueOf(this.validatorData.path(SCOPE_CONSTRAINT_ID).asText());
				this.scopeConstraint = scopeConstraintDal.getById(scopeConstraintId);
			    ArrayNode queryParamsList = (ArrayNode) this.validatorData.get(SCOPE_CONSTRAINT_QUERY_PARAMS);
				
				if (queryParamsList != null) {
					for (int i = 0; i < queryParamsList.size(); i++) {
						queryParams.add(queryParamsList.path(i).asText());
					}
				}
			} catch (Exception e) {
				log.error("setValidatorData; Exception={};", e);
			}
		}
	}
}
