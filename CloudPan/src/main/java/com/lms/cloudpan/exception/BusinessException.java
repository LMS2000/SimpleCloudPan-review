package com.lms.cloudpan.exception;

import com.lms.contants.HttpCode;
import io.swagger.models.auth.In;

public class BusinessException extends RuntimeException{
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(HttpCode httpCode) {
        super(httpCode.getMessage());
        this.code = httpCode.getCode();
    }

    public BusinessException(HttpCode httpCode, String message) {
        super(message);
        this.code = httpCode.getCode();
    }

    public int getCode() {
        return code;
    }


    public static void throwIfOperationAdmin(Integer uid){
        throwIf(uid==1);
    }

    public static void throwIf(boolean flag){
        if(flag)throw new BusinessException(HttpCode.OPERATION_ERROR);
    }
    public static  void throwIfNot(boolean flag){
        if(!flag)throw new BusinessException(HttpCode.OPERATION_ERROR);
    }
    public static void throwIf(boolean flag,HttpCode httpCode){
        if(flag)throw new BusinessException(httpCode);
    }
    public static  void throwIfNot(boolean flag,HttpCode httpCode){
        if(!flag)throw new BusinessException(httpCode);
    }

    public static void throwIf(boolean flag,HttpCode httpCode,String message){
        if(flag)throw new BusinessException(httpCode,message);
    }
    public static  void throwIfNot(boolean flag,HttpCode httpCode,String message){
        if(!flag)throw new BusinessException(httpCode,message);
    }


}
