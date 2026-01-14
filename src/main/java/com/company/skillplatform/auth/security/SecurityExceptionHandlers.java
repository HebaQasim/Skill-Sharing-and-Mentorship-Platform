package com.company.skillplatform.auth.security;

import com.company.skillplatform.common.exception.ApiError;
import com.company.skillplatform.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityExceptionHandlers  implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        log.debug(
                "Unauthorized access | path={} | method={} | reason={}",
                request.getRequestURI(),
                request.getMethod(),
                authException.getMessage()
        );

        ApiError body = new ApiError(
                401,
                ErrorCode.UNAUTHORIZED,
                "Unauthorized",
                LocalDateTime.now(),
                Map.of()
        );

        write(response, 401, body);
    }
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        log.warn(
                "Access denied | path={} | method={} | reason={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage()
        );

        ApiError body = new ApiError(
                403,
                ErrorCode.FORBIDDEN,
                "Forbidden",
                LocalDateTime.now(),
                Map.of()
        );

        write(response, 403, body);
    }
    private void write(HttpServletResponse response, int status, ApiError body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
