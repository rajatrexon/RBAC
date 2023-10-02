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
package com.esq.rbac.service.variable.service;
import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.variable.domain.Variable;
import com.esq.rbac.service.variable.variableinfo.domain.VariableInfo;
import com.esq.rbac.service.variable.variableinfov2.domain.VariableInfoV2;

import java.util.List;

public interface VariableDal extends BaseDal {

	List<VariableInfo> getList(Options options);

	List<VariableInfoV2> getListV2(Options options);

	Variable create(Variable variable);

	boolean isVariableExists(Variable variable);

	boolean isVariableValid(Variable variable);

	Variable update(Variable variable);

	void delete(Variable variable);

	Variable toVariable(VariableInfo variableInfo);

	Variable toVariableV2(VariableInfoV2 variableInfo);

	public void deleteForCascade(Integer userId, Integer groupId, Integer applicationId);
	void cleanVariablesForApplicationChanges();
	
}
