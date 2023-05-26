package com.infrastructure.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotEmptyConstraintValidator implements ConstraintValidator<NotEmptyCheck,String> {
    @Override
    public void initialize(NotEmptyCheck constraintAnnotation) {

    }

    @Override
    public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
       return !(str==null||str.isEmpty());
    }
}
