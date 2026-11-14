package com.example.banking.ledger.controller;

import com.example.banking.common.*;
import com.example.banking.ledger.dto.LedgerDtos;
import com.example.banking.ledger.service.LedgerService;
import com.example.banking.security.Actor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/accounts/{id}")
@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
public class LedgerController {
    private final LedgerService service;

    public LedgerController(LedgerService service) {
        this.service = service;
    }

    @GetMapping("/transactions")
    public PageResponse<LedgerDtos.Entry> history(@AuthenticationPrincipal Actor actor, @PathVariable UUID id,
            @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to, Pageable page) {
        return PageResponse.from(service.history(actor, id, from, to, checked(page)));
    }

    @GetMapping("/statement")
    public LedgerDtos.Statement statement(@AuthenticationPrincipal Actor actor, @PathVariable UUID id,
            @RequestParam Instant from, @RequestParam Instant to, Pageable page) {
        return service.statement(actor, id, from, to, checked(page));
    }

    private Pageable checked(Pageable page) {
        return Pages.checked(page, Set.of("id", "createdAt", "amount", "type"));
    }
}
