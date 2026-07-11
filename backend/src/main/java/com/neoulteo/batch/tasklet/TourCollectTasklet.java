package com.neoulteo.batch.tasklet;

import java.util.List;

import com.neoulteo.batch.client.TourApiClient;
import com.neoulteo.batch.dto.TourAttractionDto;
import com.neoulteo.batch.shared.TourBatchRuntimeContext;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class TourCollectTasklet implements Tasklet {
    private final TourApiClient tourApiClient;
    private final TourBatchRuntimeContext runtimeContext;

    public TourCollectTasklet(TourApiClient tourApiClient, TourBatchRuntimeContext runtimeContext) {
        this.tourApiClient = tourApiClient;
        this.runtimeContext = runtimeContext;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception {
        runtimeContext.clear();
        List<TourAttractionDto> items = tourApiClient.fetchConfiguredContentTypeAttractions();
        runtimeContext.setTourItems(items);
        System.out.println("[TourBatch] collected attractions = " + items.size());
        return RepeatStatus.FINISHED;
    }
}
