package com.esq.rbac.service.validation.annotation;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
public class FieldRules {
    private String fieldName;
    private SizeRule sizeRule;
    private LengthRule lengthRule;
    private MandatoryRule mandatoryRule;
    private DateRule dateRule;
    private RegexRule regexRule;
    private ReadOnlyRule readOnlyRule;
}

