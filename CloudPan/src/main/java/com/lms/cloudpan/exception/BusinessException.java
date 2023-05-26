package com.lms.cloudpan.exception;

import com.lms.contants.HttpCode;

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

}
