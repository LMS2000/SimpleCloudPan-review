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
@Constraint(validatedBy = {NotEmptyConstraintValidator.class})
public @interface NotEmptyCheck {
    String message() default "字符串不能为空";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
