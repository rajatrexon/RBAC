package com.esq.rbac.service.validation.annotation;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LengthRule {

    private int min = 0;
    private int max = 0;

    public LengthRule() {
    }

    public LengthRule(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @XmlAttribute
    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    @XmlAttribute
    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}

