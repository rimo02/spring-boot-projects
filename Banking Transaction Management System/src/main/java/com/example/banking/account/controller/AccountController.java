package com.example.banking.account.controller;

import com.example.banking.account.dto.AccountDtos;
import com.example.banking.account.service.AccountService;
import com.example.banking.common.*;
import com.example.banking.security.Actor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDtos.View create(@AuthenticationPrincipal Actor actor,
            @Valid @RequestBody AccountDtos.Create request) {
        return service.create(actor, request);
    }

    @GetMapping
    public PageResponse<AccountDtos.View> mine(@AuthenticationPrincipal Actor actor, Pageable page) {
        return PageResponse.from(service.mine(actor, Pages.checked(page, Set.of("id", "createdAt", "displayName"))));
    }

    @GetMapping("/{id}")
    public AccountDtos.View get(@AuthenticationPrincipal Actor actor, @PathVariable UUID id) {
        return service.get(actor, id);
    }

    @GetMapping("/{id}/profile")
    public AccountDtos.Profile profile(@AuthenticationPrincipal Actor actor, @PathVariable UUID id) {
        return service.profile(actor, id);
    }

    @PatchMapping("/{id}/profile")
    public AccountDtos.Profile rename(@AuthenticationPrincipal Actor actor, @PathVariable UUID id,
            @Valid @RequestBody AccountDtos.Rename request) {
        return service.rename(actor, id, request);
    }
}
