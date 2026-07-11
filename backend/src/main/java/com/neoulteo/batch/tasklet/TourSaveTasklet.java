package com.neoulteo.batch.tasklet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.neoulteo.batch.client.TourApiClient;
import com.neoulteo.batch.dao.TourBatchDao;
import com.neoulteo.batch.dto.TourAttractionDto;
import com.neoulteo.batch.dto.TourBatchReportDto;
import com.neoulteo.batch.dto.TourBatchComparisonResult;
import com.neoulteo.batch.service.TourAttractionCompareService;
import com.neoulteo.batch.shared.TourBatchContextKeys;
import com.neoulteo.batch.shared.TourBatchRuntimeContext;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TourSaveTasklet implements Tasklet {
    private final TourBatchDao tourBatchDao;
    private final TourApiClient tourApiClient;
    private final TourAttractionCompareService tourAttractionCompareService;
    private final TourBatchRuntimeContext runtimeContext;

    public TourSaveTasklet(TourBatchDao tourBatchDao, TourApiClient tourApiClient,
            TourAttractionCompareService tourAttractionCompareService,
            TourBatchRuntimeContext runtimeContext) {
        this.tourBatchDao = tourBatchDao;
        this.tourApiClient = tourApiClient;
        this.tourAttractionCompareService = tourAttractionCompareService;
        this.runtimeContext = runtimeContext;
    }

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        var ctx = chunkContext.getStepContext().getStepExecution().getJobExecution()
                .getExecutionContext();
        List<TourAttractionDto> items = runtimeContext.getTourItems();
        if (items == null) {
            items = List.of();
        }

        String reportAreaCode = resolveReportAreaCode(items);
        String reportContentTypeId = resolveReportContentTypeId();
        List<TourAttractionDto> dbItems = tourBatchDao.findAttractionsForComparison(
                reportAreaCode,
                "MULTI".equals(reportContentTypeId) ? null : reportContentTypeId);
        TourBatchComparisonResult comparison = tourAttractionCompareService.compare(items, dbItems);
        String changeSummary = tourAttractionCompareService.buildChangeSummary(comparison);

        int saved = tourBatchDao.saveAttractions(items);
        String reportDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        TourBatchReportDto report = tourBatchDao.createReport(
                reportDate,
                reportAreaCode,
                reportContentTypeId,
                comparison,
                changeSummary);

        runtimeContext.setComparisonResult(comparison);
        ctx.putLong(TourBatchContextKeys.REPORT_ID, report.getId());
        System.out.println("[TourBatch] saved attractions = " + saved
                + ", " + changeSummary
                + ", reportId = " + report.getId());
        return RepeatStatus.FINISHED;
    }

    private String resolveReportContentTypeId() {
        List<String> contentTypeIds = tourApiClient.getContentTypeIds();
        if (contentTypeIds.isEmpty()) {
            return "ALL";
        }
        if (contentTypeIds.size() == 1) {
            return contentTypeIds.get(0);
        }
        return "MULTI";
    }

    private String resolveReportAreaCode(List<TourAttractionDto> items) {
        Map<String, Long> areaCounts = items.stream()
                .map(TourAttractionDto::getAreaCode)
                .filter(this::hasText)
                .collect(Collectors.groupingBy(areaCode -> areaCode, Collectors.counting()));

        if (areaCounts.isEmpty()) {
            return hasText(tourApiClient.getAreaCode()) ? tourApiClient.getAreaCode() : "NATIONWIDE";
        }
        if (areaCounts.size() == 1) {
            return areaCounts.keySet().iterator().next();
        }
        return "NATIONWIDE";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
