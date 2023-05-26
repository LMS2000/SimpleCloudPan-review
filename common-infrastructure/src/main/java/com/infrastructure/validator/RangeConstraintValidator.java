package com.infrastructure.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class RangeConstraintValidator implements ConstraintValidator<RangeCheck,Integer> {
    private RangeCheck rangeCheck;
    @Override
    public void initialize(RangeCheck constraintAnnotation) {
     this.rangeCheck=constraintAnnotation;
    }

    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        if(integer==null){
            return false;
        }
        int[] range = rangeCheck.range();
        //如果integer不在range表示的范围内就校验失败
        return Arrays.stream(range).anyMatch(integer::equals);
    }
}
