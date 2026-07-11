package com.neoulteo.domain.travelplan.dao;

import java.util.ArrayList;
import java.util.List;

import com.neoulteo.domain.travelplan.mapper.TravelPlanMapper;
import com.neoulteo.domain.travelplan.dto.TravelPlanDto;
import com.neoulteo.domain.travelplan.dto.TravelPlanPlaceDto;
import com.neoulteo.domain.travelplan.dto.TravelPlanPlaceRow;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DbTravelPlanDao {
    private final TravelPlanMapper travelPlanMapper;

    public DbTravelPlanDao(TravelPlanMapper travelPlanMapper) {
        this.travelPlanMapper = travelPlanMapper;
    }

    public List<TravelPlanDto> findByWriterEmail(String writerEmail) {
        List<TravelPlanPlaceRow> rows = travelPlanMapper.findRowsByWriterEmail(writerEmail);
        return groupRows(rows);
    }

    public TravelPlanDto findById(String id) {
        List<TravelPlanPlaceRow> rows = travelPlanMapper.findRowsById(id);
        List<TravelPlanDto> plans = groupRows(rows);
        return plans.isEmpty() ? null : plans.get(0);
    }

    public TravelPlanDto findSharedById(String id) {
        List<TravelPlanPlaceRow> rows = travelPlanMapper.findSharedRowsById(id);
        List<TravelPlanDto> plans = groupRows(rows);
        return plans.isEmpty() ? null : plans.get(0);
    }

    private List<TravelPlanDto> groupRows(List<TravelPlanPlaceRow> rows) {
        List<TravelPlanDto> plans = new ArrayList<>();

        String currentId = null;
        String currentWriterEmail = null;
        String title = null;
        int durationDays = 1;
        String createdAt = null;
        boolean isShared = false;
        List<TravelPlanPlaceDto> places = new ArrayList<>();

        for (TravelPlanPlaceRow row : rows) {
            String id = row.getId();
            if (currentId != null && !currentId.equals(id)) {
                plans.add(new TravelPlanDto(currentId, currentWriterEmail, title, durationDays, places, createdAt, isShared));
                places = new ArrayList<>();
            }

            currentId = id;
            currentWriterEmail = row.getWriterEmail();
            title = row.getTitle();
            durationDays = row.getDurationDays();
            createdAt = row.getCreatedAt();
            isShared = row.isShared();

            if (row.getPlaceName() != null) {
                places.add(toPlaceDto(row));
            }
        }

        if (currentId != null) {
            plans.add(new TravelPlanDto(currentId, currentWriterEmail, title, durationDays, places, createdAt, isShared));
        }

        return plans;
    }

    @Transactional
    public boolean save(TravelPlanDto dto) {
        if (!travelPlanMapper.insertPlan(dto)) {
            return false;
        }

        return insertPlaces(dto);
    }

    @Transactional
    public boolean update(TravelPlanDto dto) {
        if (!travelPlanMapper.existsByIdAndWriterEmail(dto.getId(), dto.getWriterEmail())) {
            return false;
        }

        if (!travelPlanMapper.updatePlan(dto)) {
            return false;
        }
        travelPlanMapper.deletePlacesByPlanId(dto.getId());
        return insertPlaces(dto);
    }

    @Transactional
    public boolean deleteByIdAndWriterEmail(String id, String writerEmail) {
        return travelPlanMapper.deleteByIdAndWriterEmail(id, writerEmail);
    }

    @Transactional
    public boolean updateShared(String id, String writerEmail, boolean shared) {
        return travelPlanMapper.updateShared(id, writerEmail, shared);
    }

    private boolean insertPlaces(TravelPlanDto dto) {
        List<TravelPlanPlaceDto> places = dto.getPlaces();
        for (int i = 0; i < places.size(); i++) {
            TravelPlanPlaceDto place = places.get(i);
            if (!travelPlanMapper.insertPlace(dto.getId(), place.getDayNo(), i, place.getName(),
                    place.getAttractionContentId())) {
                return false;
            }
        }
        return true;
    }

    private TravelPlanPlaceDto toPlaceDto(TravelPlanPlaceRow row) {
        TravelPlanPlaceDto dto = new TravelPlanPlaceDto();
        dto.setAttractionContentId(row.getAttractionContentId());
        dto.setDayNo(row.getDayNo());
        dto.setContentTypeId(row.getContentTypeId());
        dto.setContentTypeName(row.getContentTypeName());
        dto.setName(row.getPlaceName());
        dto.setAddress(row.getAddress());
        dto.setImageUrl(row.getImageUrl());
        dto.setFirstimage(row.getFirstimage());
        dto.setLatitude(row.getLatitude());
        dto.setLongitude(row.getLongitude());
        dto.setMapy(row.getMapy());
        dto.setMapx(row.getMapx());
        return dto;
    }
}
