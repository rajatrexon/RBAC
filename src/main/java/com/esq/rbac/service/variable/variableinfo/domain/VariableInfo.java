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
package com.esq.rbac.service.variable.variableinfo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//This class uses a special de-serializer, so any field modifications must be done in com.esq.rbac.security.util.VariableInfoDeserializer(RBAC-1165)
public class VariableInfo {

	private String variableName;
	private String variableValue;
	private String applicationName;
	private String userName;
	private String groupName;

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof VariableInfo) {
			VariableInfo other = (VariableInfo) o;
			if (this.getVariableName() != null && this.getVariableName().equalsIgnoreCase(other.getVariableName())) {
				if ((this.getApplicationName() + this.getUserName() + this.getGroupName())
						.equalsIgnoreCase(other.getApplicationName() + other.getUserName() + other.getGroupName())) {
					return true;
				}
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (this.getVariableName() + this.getApplicationName() + this.getUserName() + this.getGroupName())
				.toLowerCase().hashCode();
	}
}
