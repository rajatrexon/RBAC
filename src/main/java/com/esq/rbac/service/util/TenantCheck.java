package com.esq.rbac.service.util;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TenantCheck {
    boolean throwException() default true;
    Class<? extends TenantCheckHandler> handler() default TenantCheckHandler.None.class;
}
