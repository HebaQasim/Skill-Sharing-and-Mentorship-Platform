package com.company.skillplatform.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //  Business & domain exceptions
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiError> handleBaseException(BaseException ex) {

        HttpStatus status = mapStatus(ex.getErrorCode());

        log.warn(
                "Business exception | code={} | message={}",
                ex.getErrorCode(),
                ex.getMessage()
        );

        return buildResponse(
                status,
                ex.getErrorCode(),
                ex.getMessage(),
                null
        );
    }

    //  DTO validation (@RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        Map<String, List<String>> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(e ->
                errors.computeIfAbsent(e.getField(), k -> new ArrayList<>())
                        .add(e.getDefaultMessage())
        );


        log.info("DTO validation failed | errors={}", errors);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Validation failed",
                errors
        );
    }

    //  Path / query validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, List<String>> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(v -> {
            String field = v.getPropertyPath().toString();
            errors.computeIfAbsent(field, k -> new ArrayList<>())
                    .add(v.getMessage());
        });

        log.info("Constraint violation | errors={}", errors);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Validation failed",
                errors
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleMethodValidation(HandlerMethodValidationException ex) {

        Map<String, List<String>> errors = new HashMap<>();

        ex.getAllValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(error -> {
                    String field = result.getMethodParameter().getParameterName();
                    errors.computeIfAbsent(field, k -> new ArrayList<>()) // create list if missing
                            .add(error.getDefaultMessage());
                    // add message
                })
        );

        return ResponseEntity.badRequest().body(
                new ApiError(
                        400,
                        ErrorCode.VALIDATION_ERROR,
                        "Validation failed",
                        LocalDateTime.now(),
                        errors
                )
        );
    }
    //  Unexpected / system errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {

        log.error("Unexpected system error", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "Unexpected server error",
                null
        );
    }

    //  Central response builder
    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            ErrorCode code,
            String message,
            Map<String, List<String>> validationErrors) {

        return ResponseEntity
                .status(status)
                .body(new ApiError(
                        status.value(),
                        code,
                        message,
                        LocalDateTime.now(),
                        validationErrors
                ));
    }

    //  Central HTTP status mapping
    private HttpStatus mapStatus(ErrorCode code) {
        return switch (code) {
            case EMAIL_ALREADY_EXISTS, VALIDATION_ERROR ,SKILL_REJECTED,POST_NOT_PUBLISHED-> HttpStatus.BAD_REQUEST;
            case USER_NOT_FOUND, ROLE_NOT_FOUND, SESSION_NOT_FOUND,NOT_FOUND ,APPROVAL_REQUEST_NOT_FOUND ,SKILL_NOT_FOUND,NOTIFICATION_NOT_FOUND,POST_NOT_FOUND,ATTACHMENT_NOT_FOUND,FEEDBACK_NOT_FOUND-> HttpStatus.NOT_FOUND;
            case SESSION_CONFLICT,CONFLICT -> HttpStatus.CONFLICT;
            case UNAUTHORIZED ,INVALID_CREDENTIALS,USER_DISABLED
                    -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
