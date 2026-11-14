package com.example.banking.transfer.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public final class TransferDtos {
    private TransferDtos() {}
    public record Create(@NotNull UUID sourceAccountId,@NotNull UUID destinationAccountId,
                         @NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount) {}
    public record Receipt(UUID transferId,UUID sourceAccountId,UUID destinationAccountId,
                          BigDecimal amount,String status,Instant completedAt) {}
}
