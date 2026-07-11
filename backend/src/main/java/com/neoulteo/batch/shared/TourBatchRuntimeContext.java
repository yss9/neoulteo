package com.neoulteo.batch.shared;

import java.util.ArrayList;
import java.util.List;

import com.neoulteo.batch.dto.TourAttractionDto;
import com.neoulteo.batch.dto.TourBatchComparisonResult;
import org.springframework.stereotype.Component;

@Component
public class TourBatchRuntimeContext {
    private List<TourAttractionDto> tourItems = new ArrayList<>();
    private TourBatchComparisonResult comparisonResult;

    public void clear() {
        tourItems = new ArrayList<>();
        comparisonResult = null;
    }

    public List<TourAttractionDto> getTourItems() {
        return tourItems;
    }

    public void setTourItems(List<TourAttractionDto> tourItems) {
        this.tourItems = tourItems == null ? new ArrayList<>() : tourItems;
    }

    public TourBatchComparisonResult getComparisonResult() {
        return comparisonResult;
    }

    public void setComparisonResult(TourBatchComparisonResult comparisonResult) {
        this.comparisonResult = comparisonResult;
    }
}
