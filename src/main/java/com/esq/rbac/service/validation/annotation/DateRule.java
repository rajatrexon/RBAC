package com.esq.rbac.service.validation.annotation;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DateRule {
    private boolean isDate = true;

    public DateRule() {
    }

    public DateRule(Boolean isDate) {
        this.isDate = isDate;
    }

    @XmlAttribute
    public Boolean getIsDate() {
        return this.isDate;
    }

    public void setIsDate(boolean isDate) {
        this.isDate = isDate;
    }
}
