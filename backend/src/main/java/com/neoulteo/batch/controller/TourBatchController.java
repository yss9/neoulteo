package com.neoulteo.batch.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.neoulteo.batch.dto.TourBatchComparisonResult;
import com.neoulteo.batch.shared.TourBatchContextKeys;
import com.neoulteo.batch.shared.TourBatchRuntimeContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch/tour")
public class TourBatchController {
    private final JobLauncher jobLauncher;
    private final Job tourCollectJob;
    private final TourBatchRuntimeContext runtimeContext;

    public TourBatchController(JobLauncher jobLauncher,
            @Qualifier("tourCollectJob") Job tourCollectJob,
            TourBatchRuntimeContext runtimeContext) {
        this.jobLauncher = jobLauncher;
        this.tourCollectJob = tourCollectJob;
        this.runtimeContext = runtimeContext;
    }

    @PostMapping
    @Secured("ROLE_USER")
    public Map<String, Object> runTourBatch() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(tourCollectJob, params);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", execution.getStatus().isUnsuccessful() == false);
        body.put("message", "Tour batch finished with status: " + execution.getStatus());
        body.put("jobStatus", execution.getStatus().toString());
        body.put("exitStatus", execution.getExitStatus().getExitCode());
        body.put("pdfPath", execution.getExecutionContext().getString(TourBatchContextKeys.PDF_PATH, null));
        TourBatchComparisonResult comparison = runtimeContext.getComparisonResult();
        if (comparison != null) {
            body.put("apiItemCount", comparison.getApiItemCount());
            body.put("newItemCount", comparison.getNewItemCount());
            body.put("changedItemCount", comparison.getChangedItemCount());
            body.put("missingItemCount", comparison.getMissingItemCount());
            body.put("unchangedItemCount", comparison.getUnchangedItemCount());
        }
        return body;
    }
}
