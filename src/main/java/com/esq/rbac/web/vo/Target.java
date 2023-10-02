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
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Target implements Comparable<Target> {

    private Integer targetId;
    @JsonBackReference("ApplicationTargets")
    private Application application;


    private String name;

    private String targetKey;
    private String description;
    private Set<String> labels;

    @JsonManagedReference("TargetOperations")
    private Set<Operation> operations;

    public Target() {
        // empty
    }

    public Target(String name, Application application) {
        setName(name);
        setApplication(application);
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public Application getApplication() {
        return application;
    }

    public final void setApplication(Application application) {
        // set new application
        this.application = application;

        // add this target to newly set application
        if (this.application != null) {
            if (this.application.getTargets() == null) {
                this.application.setTargets(new HashSet<Target>());
            }
            if (!this.application.getTargets().contains(this)) {
                this.application.getTargets().add(this);
            }
        }
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
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

    public Set<Operation> getOperations() {
        if (operations != null && !(operations instanceof TreeSet)) {
            operations = new TreeSet<Operation>(operations);
        }
        return operations;
    }

    public void setOperations(Set<Operation> operations) {
        this.operations = operations;
    }

    @Override
    public String toString() {
        String sb = "Target{targetId=" + targetId +
                "; application=" + (application != null ? Integer.toHexString(application.hashCode()) : "null") +
                "; name=" + name +
                "; targetKey=" + targetKey +
                "; description=" + description +
                "; labels=" + labels +
                "; operations=" + operations +
                '}';
        return sb;
    }

    @Override
    public int compareTo(Target o) {
        if (this.name != null) {
            return this.name.compareTo(o.name);
        }
        return 0;
    }
}
