package com.example.banking.report.service;

import com.example.banking.report.dto.DailyReport;
import com.example.banking.common.exception.BankingException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.*;

@Service
public class ReportService {
    private final JdbcTemplate jdbc;

    public ReportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void generate(LocalDate date) {
        if (!date.isBefore(LocalDate.now(ZoneOffset.UTC)))
            throw new IllegalArgumentException("Report date must be a completed UTC day");
        jdbc.update(
                """
                        INSERT INTO daily_reports(report_date,total_debits,total_credits,transaction_count,generated_at)
                        SELECT ?,coalesce(sum(amount) FILTER(WHERE type='DEBIT'),0),
                                 coalesce(sum(amount) FILTER(WHERE type='CREDIT'),0),count(*),now()
                        FROM transactions WHERE created_at>=? AND created_at<?
                        ON CONFLICT(report_date) DO UPDATE SET total_debits=excluded.total_debits,
                            total_credits=excluded.total_credits,transaction_count=excluded.transaction_count,generated_at=excluded.generated_at
                        """,
                date, Timestamp.from(date.atStartOfDay(ZoneOffset.UTC).toInstant()),
                Timestamp.from(date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));
    }

    @Transactional(readOnly = true)
    public DailyReport get(LocalDate date) {
        return jdbc.query("SELECT * FROM daily_reports WHERE report_date=?", (rs, n) -> new DailyReport(date,
                rs.getBigDecimal("total_debits"), rs.getBigDecimal("total_credits"), rs.getLong("transaction_count"),
                rs.getTimestamp("generated_at").toInstant()), date).stream().findFirst()
                .orElseThrow(
                        () -> new BankingException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "Report not generated"));
    }

    public String csv(DailyReport r) {
        return "date,total_debits,total_credits,transaction_count,generated_at\n" + r.date() + ","
                + r.totalDebits().toPlainString()
                + "," + r.totalCredits().toPlainString() + "," + r.transactionCount() + "," + r.generatedAt() + "\n";
    }
}
