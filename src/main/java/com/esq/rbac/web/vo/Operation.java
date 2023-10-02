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
package com.esq.rbac.web.vo;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.HashSet;
import java.util.Set;

public class Operation implements Comparable<Operation> {

    private Integer operationId;
    @JsonBackReference("TargetOperations")
    private Target target;

    private String name;

    private String operationKey;


    private String description;
    private Set<String> labels;
    private Set<Integer> scopeIds;

    public Operation() {
        // empty
    }

    public Operation(String name, Target target) {
        setName(name);
        setTarget(target);
    }

    public Integer getOperationId() {
        return operationId;
    }

    public void setOperationId(Integer operationId) {
        this.operationId = operationId;
    }

    public Target getTarget() {
        return target;
    }

    public final void setTarget(Target target) {
        // set new target
        this.target = target;

        // add this operation to new target
        if (this.target != null) {
            if (this.target.getOperations() == null) {
                this.target.setOperations(new HashSet<Operation>());
            }
            if (!this.target.getOperations().contains(this)) {
                this.target.getOperations().add(this);
            }
        }
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public Set<Integer> getScopeIds() {
        return scopeIds;
    }

    public void setScopeIds(Set<Integer> scopeIds) {
        this.scopeIds = scopeIds;
    }

    @Override
    public String toString() {
        String sb = "Operation{operationId=" + operationId +
                "; target=" + (target != null ? Integer.toHexString(target.hashCode()) : "null") +
                "; name=" + name +
                "; operationKey=" + operationKey +
                "; description=" + description +
                "; labels=" + labels +
                "; scopeIds=" + scopeIds +
                '}';
        return sb;
    }

    @Override
    public int compareTo(Operation o) {
        if (this.name != null) {
            return this.name.compareTo(o.name);
        }
        return 0;
    }
}
