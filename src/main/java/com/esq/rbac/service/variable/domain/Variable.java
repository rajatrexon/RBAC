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
package com.esq.rbac.service.variable.domain;


import com.esq.rbac.service.util.SpecialCharValidator;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "variable", schema = "rbac")
public class Variable {

    @Column(name = "variableName", nullable = false, length = 32)
    @SpecialCharValidator
    private String variableName;
    @Column(name = "variableValue")
    @Size(min = 1)
    private String variableValue;

    @Column(name = "groupId")
    private Integer groupId;

    @Column(name = "userId")
    private Integer userId;

    @Column(name = "applicationId")
    private Integer applicationId;


    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "variableIdGenerator")
    @TableGenerator(name = "variableIdGenerator", schema = "rbac", table = "idSequence",
            pkColumnName = "idName", valueColumnName = "idValue", pkColumnValue = "variableId",
            initialValue = 1, allocationSize = 1)
    @Column(name = "variableId")
    private Integer variableId;

    @Column(name = "childApplicationId")
    private Integer childApplicationId;

    public static Map<String, String> convertSetOfVariablesToMap(Set<Variable> variableSet) {
        Map<String, String> result = new TreeMap<String, String>();

        if (variableSet != null) {
            for (Variable v : variableSet) {
                result.put(v.getVariableName(), v.getVariableValue());
            }
        }

        return result;
    }
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Variable) {
            Variable other = (Variable) o;
            if (other.getVariableId() != null) {
                return other.variableId.equals(this.variableId);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.variableId != null) {
            return this.variableId.hashCode();
        }
        return 0;
    }

}
