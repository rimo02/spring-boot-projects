package com.example.banking.transfer.service;

import com.example.banking.account.entity.Account;
import com.example.banking.account.repository.AccountRepository;
import com.example.banking.common.exception.*;
import com.example.banking.ledger.entity.Transaction;
import com.example.banking.ledger.repository.TransactionRepository;
import com.example.banking.security.Actor;
import com.example.banking.transfer.dto.TransferDtos;
import com.example.banking.transfer.entity.TransferRequest;
import com.example.banking.transfer.mapper.TransferMapper;
import com.example.banking.transfer.repository.TransferRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;
import org.slf4j.*;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private final AccountRepository accounts;
    private final TransferRequestRepository requests;
    private final TransactionRepository ledger;

    public TransferService(AccountRepository accounts, TransferRequestRepository requests,
            TransactionRepository ledger) {
        this.accounts = accounts;
        this.requests = requests;
        this.ledger = ledger;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 30)
    public TransferDtos.Receipt transfer(Actor actor, String key, TransferDtos.Create input) {
        validate(key, input);
        var amount = input.amount().setScale(2, RoundingMode.UNNECESSARY);
        String hash = fingerprint(
                input.sourceAccountId() + "|" + input.destinationAccountId() + "|" + amount.toPlainString());
        // Fast replay avoids unnecessary account locks. The reservation below closes
        // the race on a miss.
        var existing = requests.findByUserIdAndIdempotencyKey(actor.id(), key);
        if (existing.isPresent())
            return replay(existing.get(), hash);
        if (!accounts.existsById(input.sourceAccountId()))
            throw new AccountNotFoundException();
        if (!accounts.existsByIdAndOwnerId(input.sourceAccountId(), actor.id()))
            throw new UnauthorizedAccessException();
        if (!accounts.existsById(input.destinationAccountId()))
            throw new AccountNotFoundException();
        UUID transferId = UUID.randomUUID();
        int inserted = requests.reserve(transferId, actor.id(), key, hash, input.sourceAccountId(),
                input.destinationAccountId(), amount);
        TransferRequest request = requests.findByUserIdAndIdempotencyKey(actor.id(), key).orElseThrow();
        if (inserted == 0)
            return replay(request, hash);
        // Every writer must use this ordering. UUID natural ordering is sufficient as a
        // global order.
        UUID first = input.sourceAccountId().compareTo(input.destinationAccountId()) < 0 ? input.sourceAccountId()
                : input.destinationAccountId();
        UUID second = first.equals(input.sourceAccountId()) ? input.destinationAccountId() : input.sourceAccountId();
        Account a = accounts.lockById(first).orElseThrow(AccountNotFoundException::new);
        Account b = accounts.lockById(second).orElseThrow(AccountNotFoundException::new);
        Account debit = a.getId().equals(input.sourceAccountId()) ? a : b;
        Account credit = a.getId().equals(input.destinationAccountId()) ? a : b;
        // Re-check ownership after locks; never authorize spending from a cache.
        if (!debit.getOwnerId().equals(actor.id()))
            throw new UnauthorizedAccessException();
        debit.debit(amount);
        credit.credit(amount);
        // PostgreSQL timestamps have microsecond precision; first responses and replays
        // must match.
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        ledger.saveAll(List.of(
                new Transaction(transferId, debit.getId(), Transaction.Type.DEBIT, amount, debit.getBalance(), now),
                new Transaction(transferId, credit.getId(), Transaction.Type.CREDIT, amount, credit.getBalance(),
                        now)));
        request.complete(now);
        // Flush before returning; any SQL failure rolls back both balances, ledger and
        // key reservation.
        ledger.flush();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("event=transfer_completed transfer_id={}", transferId);
            }
        });
        return TransferMapper.receipt(request);
    }

    private TransferDtos.Receipt replay(TransferRequest previous, String hash) {
        if (!previous.getPayloadHash().equals(hash))
            throw new DuplicateRequestException();
        if (previous.getStatus() != TransferRequest.Status.COMPLETED)
            throw new BankingException(
                    org.springframework.http.HttpStatus.CONFLICT, "REQUEST_IN_PROGRESS",
                    "Retry with the same idempotency key");
        return TransferMapper.receipt(previous);
    }

    private void validate(String key, TransferDtos.Create input) {
        if (key == null || !key.matches("[A-Za-z0-9._:-]{1,128}") || input == null || input.sourceAccountId() == null
                || input.destinationAccountId() == null || input.sourceAccountId().equals(input.destinationAccountId())
                || input.amount() == null || input.amount().signum() <= 0 || input.amount().scale() > 2
                || input.amount().precision() - input.amount().scale() > 17)
            throw new IllegalArgumentException("Invalid transfer");
    }

    private String fingerprint(String value) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
