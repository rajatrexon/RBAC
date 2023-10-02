package com.esq.rbac.service.commons;

import com.esq.rbac.service.contact.annotation.Date;
import com.esq.rbac.service.validation.annotation.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ValidationUtil {
    private static final Logger log = LoggerFactory.getLogger(ValidationUtil.class);

    public ValidationUtil() {
    }

    public static <T> List<FieldRules> retrieveValidationRules(Class<T> entityType) {
        List<FieldRules> rules = new ArrayList();

        try {
            Field[] fields = entityType.getDeclaredFields();
            Field[] var3 = fields;
            int var4 = fields.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Field f = var3[var5];
                Annotation[] annotations = f.getAnnotations();
                if (annotations != null && annotations.length != 0) {
                    log.debug("retrieveValidationRules; field '{}' has {} annotations", f.getName(), annotations.length);
                    DateRule dateRule = null;
                    RegexRule regexRule = null;
                    ReadOnlyRule readOnlyRule = null;
                    SizeRule sizeRule = null;
                    MandatoryRule mandatoryRule = null;
                    Annotation[] var13 = annotations;
                    int var14 = annotations.length;

                    for (int var15 = 0; var15 < var14; ++var15) {
                        Annotation a = var13[var15];
                        if (a.annotationType() == Date.class) {
                            Date d = (Date) a;
                            dateRule = new DateRule(d.date());
                        } else if (a.annotationType() == Regex.class) {
                            Regex r = (Regex) a;
                            regexRule = new RegexRule(r.regex());
                        } else if (a.annotationType() == ReadOnly.class) {
                            readOnlyRule = new ReadOnlyRule();
                        } else if (a.annotationType() == Size.class) {
                            Size ro = (Size) a;
                            sizeRule = new SizeRule(ro.min(), ro.max());
                        } else if (a.annotationType() == NotNull.class) {
                            mandatoryRule = new MandatoryRule();
                        } else if (a.annotationType() == Pattern.class) {
                            Pattern p = (Pattern) a;
                            regexRule = new RegexRule(p.regexp());
                        }
                    }

                    if (mandatoryRule != null || dateRule != null || regexRule != null || readOnlyRule != null || sizeRule != null) {
                        FieldRules fieldRules = new FieldRules();
                        fieldRules.setFieldName(f.getName());
                        fieldRules.setMandatoryRule(mandatoryRule);
                        fieldRules.setDateRule(dateRule);
                        fieldRules.setRegexRule(regexRule);
                        fieldRules.setReadOnlyRule(readOnlyRule);
                        fieldRules.setSizeRule(sizeRule);
                        rules.add(fieldRules);
                    }
                }
            }
        } catch (Exception var18) {
            log.error("retrieveValidationRules; type={}; exception={}", entityType.getClass().getName(), var18.toString());
            log.debug("retrieveValidationRules; exception", var18);
        }

        return rules;
    }
}