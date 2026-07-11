package com.neoulteo.batch.tasklet;

import com.neoulteo.batch.ai.TourGmsAiService;
import com.neoulteo.batch.dao.TourBatchDao;
import com.neoulteo.batch.dto.TourBatchComparisonResult;
import com.neoulteo.batch.shared.TourBatchContextKeys;
import com.neoulteo.batch.shared.TourBatchRuntimeContext;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class TourAiTasklet implements Tasklet {
    private final TourGmsAiService tourGmsAiService;
    private final TourBatchDao tourBatchDao;
    private final TourBatchRuntimeContext runtimeContext;

    public TourAiTasklet(TourGmsAiService tourGmsAiService, TourBatchDao tourBatchDao,
            TourBatchRuntimeContext runtimeContext) {
        this.tourGmsAiService = tourGmsAiService;
        this.tourBatchDao = tourBatchDao;
        this.runtimeContext = runtimeContext;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        var ctx = chunkContext.getStepContext().getStepExecution().getJobExecution()
                .getExecutionContext();
        TourBatchComparisonResult comparison = runtimeContext.getComparisonResult();
        Long reportId = ctx.getLong(TourBatchContextKeys.REPORT_ID);

        String summary = tourGmsAiService.summarizeComparison(comparison);
        tourBatchDao.updateSummary(reportId, summary);
        ctx.putString(TourBatchContextKeys.AI_SUMMARY, summary);

        System.out.println("[TourBatch] AI summary created.");
        return RepeatStatus.FINISHED;
    }
}
