package com.neoulteo.batch.tasklet;

import com.neoulteo.batch.dao.TourBatchDao;
import com.neoulteo.batch.dto.TourBatchComparisonResult;
import com.neoulteo.batch.pdf.TourPdfReportService;
import com.neoulteo.batch.shared.TourBatchContextKeys;
import com.neoulteo.batch.shared.TourBatchRuntimeContext;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class TourPdfTasklet implements Tasklet {
    private final TourPdfReportService tourPdfReportService;
    private final TourBatchDao tourBatchDao;
    private final TourBatchRuntimeContext runtimeContext;

    public TourPdfTasklet(TourPdfReportService tourPdfReportService, TourBatchDao tourBatchDao,
            TourBatchRuntimeContext runtimeContext) {
        this.tourPdfReportService = tourPdfReportService;
        this.tourBatchDao = tourBatchDao;
        this.runtimeContext = runtimeContext;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception {
        var ctx = chunkContext.getStepContext().getStepExecution().getJobExecution()
                .getExecutionContext();
        TourBatchComparisonResult comparison = runtimeContext.getComparisonResult();
        String summary = ctx.getString(TourBatchContextKeys.AI_SUMMARY, "");
        Long reportId = ctx.getLong(TourBatchContextKeys.REPORT_ID);

        String pdfPath = tourPdfReportService.createReport(comparison, summary);
        tourBatchDao.updatePdfPath(reportId, pdfPath);
        ctx.putString(TourBatchContextKeys.PDF_PATH, pdfPath);

        System.out.println("[TourBatch] PDF created = " + pdfPath);
        return RepeatStatus.FINISHED;
    }
}
