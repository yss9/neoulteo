package com.neoulteo.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TourBatchScheduler {
    private final JobLauncher jobLauncher;
    private final Job tourCollectJob;

    public TourBatchScheduler(JobLauncher jobLauncher,
            @Qualifier("tourCollectJob") Job tourCollectJob) {
        this.jobLauncher = jobLauncher;
        this.tourCollectJob = tourCollectJob;
    }

    @Scheduled(cron = "0 0 9 1 * *", zone = "Asia/Seoul")
    public void runMonthlyTourBatch() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(tourCollectJob, params);
        } catch (Exception e) {
            System.err.println("[TourBatch] scheduled run failed: " + e.getMessage());
        }
    }
}
