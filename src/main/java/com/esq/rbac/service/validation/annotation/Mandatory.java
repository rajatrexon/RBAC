package com.esq.rbac.service.validation.annotation;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@XmlRootElement
public @interface Mandatory {

    public boolean mandatory() default true;
}

