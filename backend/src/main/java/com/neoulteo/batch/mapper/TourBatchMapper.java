package com.neoulteo.batch.mapper;

import java.util.List;

import com.neoulteo.batch.dto.TourAttractionDto;
import com.neoulteo.batch.dto.TourBatchReportDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TourBatchMapper {
    List<TourAttractionDto> findAttractionsForComparison(
            @Param("areaCode") String areaCode,
            @Param("contentTypeId") String contentTypeId);

    int upsertAttraction(TourAttractionDto attraction);

    int insertReport(TourBatchReportDto report);

    int updateReportSummary(@Param("id") Long id, @Param("aiSummary") String aiSummary);

    int updateReportPdfPath(@Param("id") Long id, @Param("pdfPath") String pdfPath);
}
