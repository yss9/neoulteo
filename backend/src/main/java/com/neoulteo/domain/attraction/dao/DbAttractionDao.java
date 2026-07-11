package com.neoulteo.domain.attraction.dao;

import java.util.List;
import java.util.Map;

import com.neoulteo.domain.attraction.mapper.AttractionMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DbAttractionDao {
    private final AttractionMapper attractionMapper;

    public DbAttractionDao(AttractionMapper attractionMapper) {
        this.attractionMapper = attractionMapper;
    }

    public List<Map<String, String>> loadSidos() {
        return attractionMapper.loadSidos();
    }

    public List<Map<String, String>> loadGuguns(String sidoCode) {
        return attractionMapper.loadGuguns(sidoCode);
    }

    public List<Map<String, String>> searchAttractions(String areaCode, String gugunCode,
            String contentTypeId, String keyword) {
        return attractionMapper.searchAttractions(areaCode, gugunCode, contentTypeId, keyword);
    }
}
