package com.infrastructure.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {MultipartFileNotEmptyConstraintValidator.class})
public @interface  MultipartFileNotEmptyCheck {
    String message() default "文件不能为空";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
