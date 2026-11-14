package com.example.banking.report.dto;

import java.math.BigDecimal;
import java.time.*;

public record DailyReport(LocalDate date, BigDecimal totalDebits, BigDecimal totalCredits, long transactionCount,
        Instant generatedAt) {
}
