package com.esq.rbac.service.user.restrictionvalidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = {RestrictionValidator.class})
@Documented
public @interface RestrictionValidation {

    String message() default "invalidRestriction";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

