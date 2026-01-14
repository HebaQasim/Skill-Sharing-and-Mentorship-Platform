package com.company.skillplatform.common.exception;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(ErrorCode code, String message) {
        super(code, message);
    }
}

