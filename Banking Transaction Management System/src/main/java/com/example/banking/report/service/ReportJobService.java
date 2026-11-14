package com.example.banking.report.service;

import com.example.banking.common.exception.BankingException;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.*;
import java.time.*;

@Service
public class ReportJobService {
    private static final Logger log = LoggerFactory.getLogger(ReportJobService.class);
    private final JobLauncher launcher;
    private final Job job;

    public ReportJobService(JobLauncher launcher, @Qualifier("dailyTransactionSummary") Job job) {
        this.launcher = launcher;
        this.job = job;
    }

    public record Execution(Long executionId, String status) {
    }

    public Execution run(LocalDate date) {
        if (!date.isBefore(LocalDate.now(ZoneOffset.UTC)))
            throw new IllegalArgumentException("Report date must be a completed UTC day");
        try {
            var execution = launcher.run(job,
                    new JobParametersBuilder().addString("date", date.toString()).toJobParameters());
            if (execution.getStatus() != BatchStatus.COMPLETED)
                throw new BankingException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "REPORT_FAILED", "Report generation failed; retry the same date");
            return new Execution(execution.getId(), execution.getStatus().name());
        } catch (JobInstanceAlreadyCompleteException e) {
            return new Execution(null, "ALREADY_COMPLETED");
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobParametersInvalidException e) {
            throw new BankingException(HttpStatus.CONFLICT, "REPORT_JOB_CONFLICT", "Report job cannot run now");
        }
    }

    @Scheduled(cron = "${banking.reports.cron}", zone = "UTC")
    public void scheduled() {
        try {
            run(LocalDate.now(ZoneOffset.UTC).minusDays(1));
        } catch (RuntimeException e) {
            log.error("event=daily_report_failed exception={}", e.getClass().getSimpleName());
        }
    }
}
