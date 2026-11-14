package com.example.banking.transfer.controller;

import com.example.banking.security.Actor;
import com.example.banking.transfer.dto.TransferDtos;
import com.example.banking.transfer.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
public class TransferController {
    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping
    public TransferDtos.Receipt transfer(@AuthenticationPrincipal Actor actor,
            @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody TransferDtos.Create request) {
        return service.transfer(actor, key, request);
    }
}
