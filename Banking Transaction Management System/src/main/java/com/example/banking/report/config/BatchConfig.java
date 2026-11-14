package com.example.banking.report.config;
import com.example.banking.report.service.ReportService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import java.time.LocalDate;
@Configuration
public class BatchConfig {
    @Bean Job dailyTransactionSummary(JobRepository repository,PlatformTransactionManager manager,ReportService reports) {
        var step=new StepBuilder("aggregateDailyTransactions",repository).tasklet((contribution,context)-> {
            String date=(String)context.getStepContext().getJobParameters().get("date");
            reports.generate(LocalDate.parse(date));
            return RepeatStatus.FINISHED;
        },manager).build();
        return new JobBuilder("dailyTransactionSummary",repository).start(step).build();
    }
}
