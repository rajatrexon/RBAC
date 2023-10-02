package com.esq.rbac.service.validation.annotation;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegexRule {
    private String regex;

    public RegexRule() {
    }

    public RegexRule(String regex) {
        this.regex = regex;
    }

    @XmlAttribute
    public String getRegex() {
        return this.regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}