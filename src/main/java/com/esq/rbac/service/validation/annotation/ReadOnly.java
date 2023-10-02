package com.esq.rbac.service.validation.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly {

    public boolean readOnly() default true;
}
