package com.example.banking.ledger.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private UUID id;
    private UUID transferId;
    private UUID accountId;
    @Enumerated(EnumType.STRING)
    private Type type;
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(precision = 19, scale = 2)
    private BigDecimal balanceAfter;
    private Instant createdAt;

    protected Transaction() {
    }

    public Transaction(UUID transferId, UUID accountId, Type type, BigDecimal amount, BigDecimal balanceAfter,
            Instant time) {
        this.id = UUID.randomUUID();
        this.transferId = transferId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = time;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Type getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public enum Type {
        DEBIT, CREDIT
    }
}
