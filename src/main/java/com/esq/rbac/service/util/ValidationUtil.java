package com.esq.rbac.service.util;

import com.esq.rbac.service.validation.FieldRules;
import com.esq.rbac.service.validation.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ValidationUtil {

    @SuppressWarnings("unused")
    public static <T> List<FieldRules> retrieveValidationRules(Class<T> entityType) {

        List<FieldRules> rules = new ArrayList<FieldRules>();
        try {
            Field[] fields = entityType.getDeclaredFields();
            for (Field f : fields) {
                Annotation[] annotations = f.getAnnotations();
                if (annotations == null || annotations.length == 0) {
                    continue;
                }

                log.debug("retrieveValidationRules; field '" + f.getName() + "' has " + annotations.length + " annotations");
                MandatoryRule mandatoryRule = null;
                DateRule dateRule = null;
                LengthRule lenghtRule = null;
                RegexRule regexRule = null;
                ReadOnlyRule readOnlyRule = null;

                for (Annotation a : annotations) {
                    if (a.annotationType() == Length.class) {
                        Length l = (Length) a;
                        lenghtRule = new LengthRule(l.min(), l.max());

                    } else if (a.annotationType() == Mandatory.class) {
                        Mandatory m = (Mandatory) a;
                        mandatoryRule = new MandatoryRule(m.mandatory());
                    } else if (a.annotationType() == ValidateDate.class) {
                        ValidateDate d = (ValidateDate) a;
                        dateRule = new DateRule(d.date());
                    } else if (a.annotationType() == Regex.class) {
                        Regex r = (Regex) a;
                        regexRule = new RegexRule(r.regex());
                    } else if (a.annotationType() == ReadOnly.class) {
                        ReadOnly ro = (ReadOnly) a;
                        readOnlyRule = new ReadOnlyRule();
                    }

                }

                if (lenghtRule != null || mandatoryRule != null || dateRule != null || regexRule != null || readOnlyRule != null) {
                    com.esq.rbac.service.validation.FieldRules fieldRules = new FieldRules();
                    fieldRules.setFieldName(f.getName());
                    fieldRules.setLengthRule(lenghtRule);
                    fieldRules.setMandatoryRule(mandatoryRule);
                    fieldRules.setDateRule(dateRule);
                    fieldRules.setRegexRule(regexRule);
                    fieldRules.setReadOnlyRule(readOnlyRule);
                    rules.add(fieldRules);
                }
            }

        } catch (Exception ex) {
            log.error("Error retrievening validation rules for object: " + entityType.getClass().getName(), ex);
        }

        return rules;
    }
}

