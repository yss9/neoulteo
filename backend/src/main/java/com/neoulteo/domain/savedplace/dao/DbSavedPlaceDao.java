package com.neoulteo.domain.savedplace.dao;

import java.util.List;

import com.neoulteo.domain.savedplace.dto.SavedPlaceDto;
import com.neoulteo.domain.savedplace.mapper.SavedPlaceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DbSavedPlaceDao {
    private final SavedPlaceMapper savedPlaceMapper;

    public DbSavedPlaceDao(SavedPlaceMapper savedPlaceMapper) {
        this.savedPlaceMapper = savedPlaceMapper;
    }

    public List<SavedPlaceDto> findByUserId(Long userId) {
        return savedPlaceMapper.findByUserId(userId);
    }

    public boolean save(SavedPlaceDto dto) {
        return savedPlaceMapper.save(dto);
    }

    public boolean deleteByIdAndUserId(Long id, Long userId) {
        return savedPlaceMapper.deleteByIdAndUserId(id, userId);
    }
}
