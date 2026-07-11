package com.neoulteo.batch.config;

import com.neoulteo.batch.tasklet.TourAiTasklet;
import com.neoulteo.batch.tasklet.TourCollectTasklet;
import com.neoulteo.batch.tasklet.TourPdfTasklet;
import com.neoulteo.batch.tasklet.TourSaveTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TourBatchJobConfig {
    @Bean
    public Step tourCollectStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TourCollectTasklet tourCollectTasklet) {
        return new StepBuilder("tourCollectStep", jobRepository)
                .tasklet(tourCollectTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step tourSaveStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TourSaveTasklet tourSaveTasklet) {
        return new StepBuilder("tourSaveStep", jobRepository)
                .tasklet(tourSaveTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step tourAiStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TourAiTasklet tourAiTasklet) {
        return new StepBuilder("tourAiStep", jobRepository)
                .tasklet(tourAiTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step tourPdfStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TourPdfTasklet tourPdfTasklet) {
        return new StepBuilder("tourPdfStep", jobRepository)
                .tasklet(tourPdfTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job tourCollectJob(JobRepository jobRepository,
            @Qualifier("tourCollectStep") Step tourCollectStep,
            @Qualifier("tourSaveStep") Step tourSaveStep,
            @Qualifier("tourAiStep") Step tourAiStep,
            @Qualifier("tourPdfStep") Step tourPdfStep) {
        return new JobBuilder("tourCollectJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(tourCollectStep)
                .next(tourSaveStep)
                .next(tourAiStep)
                .next(tourPdfStep)
                .build();
    }
}
