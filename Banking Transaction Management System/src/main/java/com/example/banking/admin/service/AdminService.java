package com.example.banking.admin.service;

import com.example.banking.account.dto.AccountDtos;
import com.example.banking.account.mapper.AccountMapper;
import com.example.banking.account.repository.AccountRepository;
import com.example.banking.ledger.dto.LedgerDtos;
import com.example.banking.ledger.service.LedgerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.util.UUID;

@Service
@PreAuthorize("hasRole('ADMIN')")
@Transactional(readOnly = true)
public class AdminService {
    private final AccountRepository accounts;
    private final LedgerService ledger;

    public AdminService(AccountRepository accounts, LedgerService ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    public Page<AccountDtos.View> accounts(Pageable page) {
        return accounts.findAll(page).map(AccountMapper::view);
    }

    public Page<LedgerDtos.Entry> transactions(UUID accountId, Instant from, Instant to, Pageable page) {
        return ledger.query(accountId, from, to, page);
    }
}
