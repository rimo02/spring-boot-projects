package com.example.banking.common.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BankingException {
    public InsufficientBalanceException() {
        super(HttpStatus.CONFLICT, "INSUFFICIENT_BALANCE", "Insufficient balance");
    }
}
