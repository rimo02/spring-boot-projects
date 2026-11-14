package com.example.banking.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateRequestException extends BankingException {
    public DuplicateRequestException() {
        super(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", "Idempotency key already used with a different payload");
    }
}
