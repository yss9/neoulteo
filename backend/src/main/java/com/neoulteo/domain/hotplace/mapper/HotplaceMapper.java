package com.neoulteo.domain.hotplace.mapper;

import java.util.List;

import com.neoulteo.domain.hotplace.dto.HotplaceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HotplaceMapper {
    List<HotplaceDto> findPublicHotplaces();

    List<HotplaceDto> findPublicHotplacesByRegion(@Param("region") String region, @Param("areaCode") Integer areaCode);

    List<HotplaceDto> findPopularHotplaces(@Param("region") String region, @Param("areaCode") Integer areaCode,
            @Param("limit") int limit);

    List<HotplaceDto> findByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndAttractionContentId(@Param("userId") Long userId,
            @Param("attractionContentId") Integer attractionContentId);

    boolean save(HotplaceDto dto);

    boolean updateByIdAndUserId(HotplaceDto dto);

    boolean deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
