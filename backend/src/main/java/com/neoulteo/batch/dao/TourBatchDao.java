package com.neoulteo.batch.dao;

import java.util.List;

import com.neoulteo.batch.dto.TourAttractionDto;
import com.neoulteo.batch.dto.TourBatchReportDto;
import com.neoulteo.batch.dto.TourBatchComparisonResult;
import com.neoulteo.batch.mapper.TourBatchMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TourBatchDao {
    private final TourBatchMapper tourBatchMapper;

    public TourBatchDao(TourBatchMapper tourBatchMapper) {
        this.tourBatchMapper = tourBatchMapper;
    }

    public int saveAttractions(List<TourAttractionDto> attractions) {
        int count = 0;
        for (TourAttractionDto attraction : attractions) {
            count += tourBatchMapper.upsertAttraction(attraction);
        }
        return count;
    }

    public List<TourAttractionDto> findAttractionsForComparison(String areaCode, String contentTypeId) {
        return tourBatchMapper.findAttractionsForComparison(areaCode, contentTypeId);
    }

    public TourBatchReportDto createReport(String reportDate, String areaCode, String contentTypeId,
            TourBatchComparisonResult comparison, String changeSummary) {
        TourBatchReportDto report = new TourBatchReportDto();
        report.setReportDate(reportDate);
        report.setAreaCode(areaCode);
        report.setContentTypeId(contentTypeId);
        report.setItemCount(comparison.getApiItemCount());
        report.setNewItemCount(comparison.getNewItemCount());
        report.setChangedItemCount(comparison.getChangedItemCount());
        report.setMissingItemCount(comparison.getMissingItemCount());
        report.setUnchangedItemCount(comparison.getUnchangedItemCount());
        report.setChangeSummary(changeSummary);
        tourBatchMapper.insertReport(report);
        return report;
    }

    public void updateSummary(Long reportId, String summary) {
        tourBatchMapper.updateReportSummary(reportId, summary);
    }

    public void updatePdfPath(Long reportId, String pdfPath) {
        tourBatchMapper.updateReportPdfPath(reportId, pdfPath);
    }
}
