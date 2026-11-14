package com.example.banking.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BankingException.class)
    ResponseEntity<ApiError> banking(BankingException e, HttpServletRequest r) {
        return error(e.getStatus(), e.getCode(), e.getMessage(), r);
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HandlerMethodValidationException.class, HttpMessageNotReadableException.class,
            ServletRequestBindingException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class })
    ResponseEntity<ApiError> invalid(Exception e, HttpServletRequest r) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request validation failed", r);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> authentication(Exception e, HttpServletRequest r) {
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Invalid credentials", r);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(Exception e, HttpServletRequest r) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", r);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(Exception e, HttpServletRequest r) {
        return error(HttpStatus.CONFLICT, "DATA_CONFLICT", "Request conflicts with existing data", r);
    }

    @ExceptionHandler({ PessimisticLockingFailureException.class, QueryTimeoutException.class })
    ResponseEntity<ApiError> busy(Exception e, HttpServletRequest r) {
        return error(HttpStatus.CONFLICT, "RESOURCE_BUSY", "Resource busy; retry with the same idempotency key", r);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest r) {
        log.error("event=request_failed exception={}", e.getClass().getSimpleName());
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", r);
    }

    private ResponseEntity<ApiError> error(HttpStatus s, String code, String message, HttpServletRequest r) {
        return ResponseEntity.status(s).body(ApiError.of(s.value(), code, message, r.getRequestURI()));
    }
}
