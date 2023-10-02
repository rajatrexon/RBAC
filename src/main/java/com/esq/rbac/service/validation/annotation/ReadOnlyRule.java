package com.esq.rbac.service.validation.annotation;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ReadOnlyRule {
    private boolean readOnly = true;

    public ReadOnlyRule() {
    }

    @XmlAttribute
    public boolean getIsReadOnly() {
        return this.readOnly;
    }

    public void setReadOnlyRule(Boolean readOnly) {
        this.readOnly = readOnly;
    }
}
