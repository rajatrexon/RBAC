package com.esq.rbac.service.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Fazia
 * @Date 04-JAN-2020
 * @Description  Used Bean Validation for custom constraint for validating a string 
 * 				 to exclude special characters
 * @Usage This constraint will be added to every field where the validation is required 
 * 		  by simply adding @SpecialCharValidator to the field. 
 */

/**
 * @Target is where our annotations can be used
 * @Retention specifies how the marked annotation is stored. We choose RUNTIME,
 *            so it can be used by the runtime environment.
 * @Constraint marks an annotation as being a Bean Validation constraint.The
 *             element validatedBy specifies the classes implementing the
 *             constraint.
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { SpecialCharValidationConstraint.class })
public @interface SpecialCharValidator {
	/**
	 * @MandatoryProperty
	 * @Description: Returns the default key for creating error messages, this
	 *               enables us to use message interpolation
	 */
	String message() default " in {value}";

	/**
	 * @MandatoryProperty
	 * @Description: Allows us to specify validation groups for our constraints
	 */
	Class<?>[] groups() default {};

	/**
	 * @MandatoryProperty
	 * @Description: Can be used by clients of the Bean Validation API to assign
	 *               custom payload objects to a constraint
	 */
	Class<? extends Payload>[] payload() default {};
}
