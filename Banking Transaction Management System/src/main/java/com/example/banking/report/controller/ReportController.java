package com.example.banking.report.controller;

import com.example.banking.report.dto.DailyReport;
import com.example.banking.report.service.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports/daily")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {
    private final ReportService reports;
    private final ReportJobService jobs;

    public ReportController(ReportService reports, ReportJobService jobs) {
        this.reports = reports;
        this.jobs = jobs;
    }

    @PostMapping("/{date}/run")
    public ReportJobService.Execution run(@PathVariable LocalDate date) {
        return jobs.run(date);
    }

    @GetMapping("/{date}")
    public DailyReport get(@PathVariable LocalDate date) {
        return reports.get(date);
    }

    @GetMapping(value = "/{date}/csv", produces = "text/csv")
    public ResponseEntity<String> csv(@PathVariable LocalDate date) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions-" + date + ".csv")
                .body(reports.csv(reports.get(date)));
    }
}
