package com.example.banking.ledger.service;

import com.example.banking.account.service.AccountService;
import com.example.banking.common.PageResponse;
import com.example.banking.ledger.dto.LedgerDtos;
import com.example.banking.ledger.entity.Transaction;
import com.example.banking.ledger.mapper.LedgerMapper;
import com.example.banking.ledger.repository.TransactionRepository;
import com.example.banking.security.Actor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.UUID;

@Service
public class LedgerService {
    private final TransactionRepository transactions;
    private final AccountService accounts;
    private final JdbcTemplate jdbc;

    public LedgerService(TransactionRepository transactions, AccountService accounts, JdbcTemplate jdbc) {
        this.transactions = transactions;
        this.accounts = accounts;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Page<LedgerDtos.Entry> history(Actor actor, UUID account, Instant from, Instant to, Pageable page) {
        accounts.requireAccess(actor, account);
        return query(account, from, to, page);
    }

    public Page<LedgerDtos.Entry> query(UUID account, Instant from, Instant to, Pageable page) {
        validateRange(from, to);
        Specification<Transaction> spec = (root, q, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (account != null)
                predicates.add(cb.equal(root.get("accountId"), account));
            if (from != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null)
                predicates.add(cb.lessThan(root.get("createdAt"), to));
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return transactions.findAll(spec, page).map(LedgerMapper::entry);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public LedgerDtos.Statement statement(Actor actor, UUID id, Instant from, Instant to, Pageable page) {
        validateRange(from, to);
        var account = accounts.requireAccess(actor, id);
        // Derive historical balances from current balance minus all subsequent ledger
        // deltas.
        // This also accommodates the development fixture's documented opening balance.
        BigDecimal opening = account.getBalance().subtract(deltaSince(id, from));
        BigDecimal closing = account.getBalance().subtract(deltaSince(id, to));
        return jdbc.queryForObject("""
                SELECT coalesce(sum(amount) FILTER (WHERE type='DEBIT'),0) AS debits,
                       coalesce(sum(amount) FILTER (WHERE type='CREDIT'),0) AS credits, count(*) AS count
                FROM transactions WHERE account_id=? AND created_at>=? AND created_at<?
                """,
                (rs, n) -> new LedgerDtos.Statement(id, from, to, opening, closing, rs.getBigDecimal("debits"),
                        rs.getBigDecimal("credits"),
                        rs.getLong("count"), PageResponse.from(query(id, from, to, page))),
                id, Timestamp.from(from), Timestamp.from(to));
    }

    private BigDecimal deltaSince(UUID id, Instant time) {
        return jdbc.queryForObject("""
                SELECT coalesce(sum(CASE WHEN type='CREDIT' THEN amount ELSE -amount END),0)
                FROM transactions WHERE account_id=? AND created_at>=?
                """, BigDecimal.class, id, Timestamp.from(time));
    }

    private void validateRange(Instant from, Instant to) {
        if (from != null && to != null && !from.isBefore(to))
            throw new IllegalArgumentException("from must precede to");
    }
}
