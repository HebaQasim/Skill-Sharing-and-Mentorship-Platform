package com.company.skillplatform.common.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ApiError(
        int status,
        ErrorCode code,
        String message,
        LocalDateTime timestamp,
        Map<String, List<String>> validationErrors
) {
}
