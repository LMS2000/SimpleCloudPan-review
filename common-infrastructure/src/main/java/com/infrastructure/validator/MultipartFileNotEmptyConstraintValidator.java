package com.infrastructure.validator;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

//文件校验器
public class MultipartFileNotEmptyConstraintValidator implements ConstraintValidator<MultipartFileNotEmptyCheck, MultipartFile> {


    @Override
    public void initialize(MultipartFileNotEmptyCheck constraintAnnotation) {

    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
       return !(file==null||file.isEmpty());
    }
}
