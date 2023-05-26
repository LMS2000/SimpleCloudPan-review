package com.infrastructure.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD,PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {RangeConstraintValidator.class})
public @interface RangeCheck {
    int[] range();

    String message() default "范围不合法";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
