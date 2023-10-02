package com.esq.rbac.service.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Pattern;

/**
 * @author Fazia
 * @Date 04-JAN-2020
 * @Desciption: The custom java class for validating Strings by our
 *              SpecialCharValidator constraint. The implementation of
 *              ConstraintValidator<SpecialCharValidator, String> says it
 *              accepts SpecialCharValidator as an annotation and the input
 *              value must be a type of String
 */
public class SpecialCharValidationConstraint implements ConstraintValidator<SpecialCharValidator, String> {

	private DeploymentUtil deploymentUtil;

	@Autowired
	public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
		this.deploymentUtil = deploymentUtil;
	}

	@Override
	public void initialize(SpecialCharValidator constraintAnnotation) {

	}

	/**
	 * @Description: isValid is the method that received the input value and decides
	 *               whether it is valid or is not.
	 */
	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		}
		boolean isValid = false;
		String disableRegex = null;
		try {
			disableRegex = deploymentUtil.getSpecialCharacterRegex();
		}catch(Exception e) {
			//do nothing
		}

		if (disableRegex == null || disableRegex.isEmpty()) {
			disableRegex = "<>="; // override angle brackets and equals sign if no regex is specified
		}
		String regexPattern = "^([^" + disableRegex + "]*)$";
		if (Pattern.matches(regexPattern, value + "")) {
			isValid = true;
		}

		if (!isValid) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext
					.buildConstraintViolationWithTemplate(
							"Invalid character found. " + disableRegex + " characters are not allowed")
					.addConstraintViolation();
		}
		return isValid;
	}

}
