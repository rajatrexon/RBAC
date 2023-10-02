package com.esq.rbac.service.commons;

import com.esq.rbac.service.validation.annotation.*;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@XmlRootElement
public class FieldRules {
    private String fieldName;
    private SizeRule sizeRule;
    private LengthRule lengthRule;
    private MandatoryRule mandatoryRule;
    private DateRule dateRule;
    private RegexRule regexRule;
    private ReadOnlyRule readOnlyRule;

    public FieldRules() {
    }

    public ReadOnlyRule getReadOnlyRule() {
        return this.readOnlyRule;
    }

    public void setReadOnlyRule(ReadOnlyRule readOnlyRule) {
        this.readOnlyRule = readOnlyRule;
    }

    public RegexRule getRegexRule() {
        return this.regexRule;
    }

    public void setRegexRule(RegexRule regexRule) {
        this.regexRule = regexRule;
    }

    @XmlAttribute
    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public SizeRule getSizeRule() {
        return this.sizeRule;
    }

    public void setSizeRule(SizeRule sizeRule) {
        this.sizeRule = sizeRule;
    }

    public MandatoryRule getMandatoryRule() {
        return this.mandatoryRule;
    }

    public void setMandatoryRule(MandatoryRule mandatoryRule) {
        this.mandatoryRule = mandatoryRule;
    }

    public DateRule getDateRule() {
        return this.dateRule;
    }

    public void setDateRule(DateRule dateRule) {
        this.dateRule = dateRule;
    }
}

