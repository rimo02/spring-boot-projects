package com.example.banking.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends BankingException {
    public UnauthorizedAccessException() {
        super(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied");
    }
}
