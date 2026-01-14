package com.company.skillplatform.common.exception;

public class BusinessException extends BaseException {

    public BusinessException(ErrorCode code, String message) {
        super(code, message);
    }
}

