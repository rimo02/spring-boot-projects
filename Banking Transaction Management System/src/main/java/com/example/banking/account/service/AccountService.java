package com.example.banking.account.service;

import com.example.banking.account.dto.AccountDtos;
import com.example.banking.account.entity.Account;
import com.example.banking.account.mapper.AccountMapper;
import com.example.banking.account.repository.AccountRepository;
import com.example.banking.common.exception.*;
import com.example.banking.security.Actor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final AccountProfileCache cache;

    public AccountService(AccountRepository accounts, AccountProfileCache cache) {
        this.accounts = accounts;
        this.cache = cache;
    }

    @Transactional
    public AccountDtos.View create(Actor actor, AccountDtos.Create request) {
        return AccountMapper.view(accounts.save(new Account(actor.id(), request.displayName().strip())));
    }

    @Transactional(readOnly = true)
    public Page<AccountDtos.View> mine(Actor actor, Pageable page) {
        return accounts.findByOwnerId(actor.id(), page).map(AccountMapper::view);
    }

    @Transactional(readOnly = true)
    public AccountDtos.View get(Actor actor, UUID id) {
        return AccountMapper.view(requireAccess(actor, id));
    }

    @Transactional(readOnly = true)
    public AccountDtos.Profile profile(Actor actor, UUID id) {
        // Authorization always consults PostgreSQL, even on a cache hit.
        Account account = requireAccess(actor, id);
        return cache.get(id).orElseGet(() -> {
            var profile = AccountMapper.profile(account);
            cache.put(profile);
            return profile;
        });
    }

    @Transactional
    public AccountDtos.Profile rename(Actor actor, UUID id, AccountDtos.Rename request) {
        Account account = accounts.lockById(id).orElseThrow(AccountNotFoundException::new);
        checkAccess(actor, account);
        account.rename(request.displayName().strip());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cache.evict(id);
            }
        });
        return AccountMapper.profile(account);
    }

    public Account requireAccess(Actor actor, UUID id) {
        Account a = accounts.findById(id).orElseThrow(AccountNotFoundException::new);
        checkAccess(actor, a);
        return a;
    }

    public void checkAccess(Actor actor, Account account) {
        if (!actor.admin() && !account.getOwnerId().equals(actor.id()))
            throw new UnauthorizedAccessException();
    }
}
