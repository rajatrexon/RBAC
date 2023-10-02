package com.esq.rbac.service.validation.annotation;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MandatoryRule {
    private boolean isMandatory = true;

    public MandatoryRule() {
    }

    public MandatoryRule(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    @XmlAttribute
    public Boolean getIsMandatory() {
        return this.isMandatory;
    }

    public void setIsMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }
}
