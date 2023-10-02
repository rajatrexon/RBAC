package com.esq.rbac.service.user.restrictionvalidator;

import com.esq.rbac.service.restriction.domain.Restriction;
import com.esq.rbac.service.restriction.util.RestrictionUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class RestrictionValidator implements ConstraintValidator<RestrictionValidation, Restriction> {

    @Override
    public boolean isValid(Restriction value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (value == null) {
            return true;
        }

        boolean isValid = true;
        try {
            value.setFromDate(RestrictionUtil.checkDate(value.getFromDate()));
        } catch (Exception e) {
            isValid = false;
            context
                    .buildConstraintViolationWithTemplate("fromDateInvalid")
                    .addConstraintViolation();
        }
        try {
            value.setToDate(RestrictionUtil.checkDate(value.getToDate()));
        } catch (Exception e) {
            isValid = false;
            context
                    .buildConstraintViolationWithTemplate("toDateInvalid")
                    .addConstraintViolation();
        }
        return isValid;
    }
}

