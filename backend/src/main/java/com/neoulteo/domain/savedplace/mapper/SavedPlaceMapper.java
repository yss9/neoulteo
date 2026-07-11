package com.neoulteo.domain.savedplace.mapper;

import java.util.List;

import com.neoulteo.domain.savedplace.dto.SavedPlaceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SavedPlaceMapper {
    List<SavedPlaceDto> findByUserId(@Param("userId") Long userId);

    boolean save(SavedPlaceDto dto);

    boolean deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
