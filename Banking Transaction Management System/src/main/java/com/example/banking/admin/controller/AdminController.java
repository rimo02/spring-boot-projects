package com.example.banking.admin.controller;
import com.example.banking.account.dto.AccountDtos;
import com.example.banking.admin.service.AdminService;
import com.example.banking.common.*;
import com.example.banking.ledger.dto.LedgerDtos;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
@RestController @RequestMapping("/api/admin") @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service=service; }
    @GetMapping("/accounts") public PageResponse<AccountDtos.View> accounts(Pageable page) {
        return PageResponse.from(service.accounts(Pages.checked(page,Set.of("id","createdAt","displayName"))));
    }
    @GetMapping("/transactions") public PageResponse<LedgerDtos.Entry> transactions(@RequestParam(required=false) UUID accountId,
        @RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,Pageable page) {
        return PageResponse.from(service.transactions(accountId,from,to,Pages.checked(page,Set.of("id","createdAt","amount","type"))));
    }
}
