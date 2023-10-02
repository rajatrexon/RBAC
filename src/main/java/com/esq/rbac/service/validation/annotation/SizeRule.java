package com.esq.rbac.service.validation.annotation;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SizeRule {
    private int min = 0;
    private int max = 0;

    public SizeRule() {
    }

    public SizeRule(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @XmlAttribute
    public int getMin() {
        return this.min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    @XmlAttribute
    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}

