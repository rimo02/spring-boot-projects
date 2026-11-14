package com.example.banking.ledger.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.example.banking.common.PageResponse;

public final class LedgerDtos {
    private LedgerDtos() {
    }

    public record Entry(UUID id, UUID transferId, UUID accountId, String type, BigDecimal amount,
            BigDecimal balanceAfter, Instant createdAt) {
    }

    public record Statement(UUID accountId, Instant from, Instant to, BigDecimal openingBalance,
            BigDecimal closingBalance,
            BigDecimal debits, BigDecimal credits, long transactionCount, PageResponse<Entry> entries) {
    }
}
