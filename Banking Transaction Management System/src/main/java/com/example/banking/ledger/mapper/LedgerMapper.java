package com.example.banking.ledger.mapper;

import com.example.banking.ledger.entity.Transaction;
import com.example.banking.ledger.dto.LedgerDtos;

public final class LedgerMapper {
    private LedgerMapper() {
    }

    public static LedgerDtos.Entry entry(Transaction t) {
        return new LedgerDtos.Entry(t.getId(), t.getTransferId(), t.getAccountId(), t.getType().name(), t.getAmount(),
                t.getBalanceAfter(), t.getCreatedAt());
    }
}
