package com.neoulteo.domain.travelplan.mapper;

import java.util.List;

import com.neoulteo.domain.travelplan.dto.TravelPlanDto;
import com.neoulteo.domain.travelplan.dto.TravelPlanPlaceRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TravelPlanMapper {
    List<TravelPlanPlaceRow> findRowsByWriterEmail(@Param("writerEmail") String writerEmail);

    List<TravelPlanPlaceRow> findRowsById(@Param("id") String id);

    List<TravelPlanPlaceRow> findSharedRowsById(@Param("id") String id);

    boolean insertPlan(TravelPlanDto dto);

    boolean insertPlace(@Param("planId") String planId, @Param("dayNo") int dayNo,
            @Param("placeOrder") int placeOrder,
            @Param("placeName") String placeName, @Param("attractionContentId") Integer attractionContentId);

    boolean existsByIdAndWriterEmail(@Param("id") String id, @Param("writerEmail") String writerEmail);

    boolean updatePlan(TravelPlanDto dto);

    boolean deletePlacesByPlanId(@Param("planId") String planId);

    boolean deleteByIdAndWriterEmail(@Param("id") String id, @Param("writerEmail") String writerEmail);

    boolean updateShared(@Param("id") String id, @Param("writerEmail") String writerEmail,
            @Param("shared") boolean shared);
}
