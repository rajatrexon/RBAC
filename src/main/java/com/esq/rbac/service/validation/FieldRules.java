/*
 * Copyright ©2012 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.service.validation;

import com.esq.rbac.service.validation.annotation.*;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FieldRules {

    private String fieldName;
    private LengthRule lengthRule;
    private MandatoryRule mandatoryRule;
    private DateRule dateRule;
    private RegexRule regexRule;
    private ReadOnlyRule readOnlyRule;

    public ReadOnlyRule getReadOnlyRule() {
        return readOnlyRule;
    }

    public void setReadOnlyRule(ReadOnlyRule readOnlyRule) {
        this.readOnlyRule = readOnlyRule;
    }

    public RegexRule getRegexRule() {
        return regexRule;
    }

    public void setRegexRule(RegexRule regexRule) {
        this.regexRule = regexRule;
    }

    @XmlAttribute
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public LengthRule getLengthRule() {
        return lengthRule;
    }

    public void setLengthRule(LengthRule lengthRule) {
        this.lengthRule = lengthRule;
    }

    public MandatoryRule getMandatoryRule() {
        return mandatoryRule;
    }

    public void setMandatoryRule(MandatoryRule mandatoryRule) {
        this.mandatoryRule = mandatoryRule;
    }

    public DateRule getDateRule() {
        return dateRule;
    }

    public void setDateRule(DateRule dateRule) {
        this.dateRule = dateRule;
    }
}