package com.example.banking.account.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.example.banking.common.exception.InsufficientBalanceException;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID ownerId;
    private String displayName;
    private String currency;
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balance;
    private Instant createdAt;

    protected Account() {
    }

    public Account(UUID ownerId, String displayName) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.displayName = displayName;
        this.currency = "INR";
        this.balance = new BigDecimal("0.00");
        this.createdAt = Instant.now();
    }

    public void debit(BigDecimal amount) {
        requirePositive(amount);
        if (balance.compareTo(amount) < 0)
            throw new InsufficientBalanceException();
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2)
            throw new IllegalArgumentException("Invalid amount");
    }

    public void rename(String name) {
        displayName = name;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
