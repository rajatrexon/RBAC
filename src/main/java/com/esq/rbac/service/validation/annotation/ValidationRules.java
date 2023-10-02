package com.esq.rbac.service.validation.annotation;

import com.esq.rbac.service.commons.FieldRules;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.LinkedList;
import java.util.List;

@XmlRootElement
public class ValidationRules {
    private List<FieldRules> fieldRulesList = new LinkedList();

    public ValidationRules() {
    }

    public List<FieldRules> getFieldRulesList() {
        return this.fieldRulesList;
    }

    public void setFieldRulesList(List<FieldRules> fieldRulesList) {
        this.fieldRulesList = fieldRulesList;
    }
}
