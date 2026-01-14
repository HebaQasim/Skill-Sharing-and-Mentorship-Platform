package com.company.skillplatform.common.exception;

public enum ErrorCode {

    // Auth
    EMAIL_ALREADY_EXISTS,
    INVALID_CREDENTIALS,
    ROLE_NOT_FOUND,

    // User
    USER_NOT_FOUND,
    USER_DISABLED,
    CANNOT_DISABLE_SELF,
    DEPARTMENT_SCOPE_VIOLATION,
    NOT_FOUND,


    // Session
    SESSION_CONFLICT,
    SESSION_NOT_FOUND,

    // Validation
    VALIDATION_ERROR,

    // Security
    UNAUTHORIZED,
    FORBIDDEN,

    // System
    INTERNAL_ERROR,

    //
    APPROVAL_REQUEST_NOT_FOUND,
    CONFLICT,
    // Skill
    SKILL_NOT_FOUND,
    SKILL_REJECTED,

    //notification
    NOTIFICATION_NOT_FOUND,

    //post
    POST_NOT_FOUND,
    POST_NOT_PUBLISHED,
    // attachments
    ATTACHMENT_NOT_FOUND,
    ATTACHMENT_NOT_ALLOWED,

    // Comments
    COMMENT_NOT_FOUND,
    COMMENT_NOT_ALLOWED,

    //feedback
    FEEDBACK_NOT_FOUND

}

