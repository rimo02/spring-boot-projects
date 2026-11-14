package com.example.banking.common.exception;

import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends BankingException {
    public AccountNotFoundException() {
        super(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found");
    }
}
