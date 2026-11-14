package com.example.banking.account.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class AccountDtos {
    private AccountDtos() {
    }

    public record Create(@NotBlank @Size(max = 80) String displayName) {
    }

    public record Rename(@NotBlank @Size(max = 80) String displayName) {
    }

    public record View(UUID id, UUID ownerId, String displayName, String currency, BigDecimal balance,
            Instant createdAt) {
    }

    // Deliberately excludes balance and credentials. Only this immutable profile is
    // cached.
    public record Profile(UUID id, UUID ownerId, String displayName, String currency) {
    }
}
