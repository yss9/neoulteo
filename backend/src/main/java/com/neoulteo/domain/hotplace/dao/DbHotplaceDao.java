package com.neoulteo.domain.hotplace.dao;

import java.util.List;

import com.neoulteo.domain.hotplace.mapper.HotplaceMapper;
import com.neoulteo.domain.hotplace.dto.HotplaceDto;
import org.springframework.stereotype.Repository;

@Repository
public class DbHotplaceDao {
    private final HotplaceMapper hotplaceMapper;

    public DbHotplaceDao(HotplaceMapper hotplaceMapper) {
        this.hotplaceMapper = hotplaceMapper;
    }

    public List<HotplaceDto> findPublicHotplaces() {
        return hotplaceMapper.findPublicHotplaces();
    }

    public List<HotplaceDto> findPublicHotplacesByRegion(String region, Integer areaCode) {
        return hotplaceMapper.findPublicHotplacesByRegion(region, areaCode);
    }

    public List<HotplaceDto> findPopularHotplaces(String region, Integer areaCode, int limit) {
        return hotplaceMapper.findPopularHotplaces(region, areaCode, limit);
    }

    public List<HotplaceDto> findByUserId(Long userId) {
        return hotplaceMapper.findByUserId(userId);
    }

    public boolean existsByUserIdAndAttractionContentId(Long userId, Integer attractionContentId) {
        return hotplaceMapper.existsByUserIdAndAttractionContentId(userId, attractionContentId);
    }

    public boolean save(HotplaceDto dto) {
        return hotplaceMapper.save(dto);
    }

    public boolean updateByIdAndUserId(HotplaceDto dto) {
        return hotplaceMapper.updateByIdAndUserId(dto);
    }

    public boolean deleteByIdAndUserId(Long id, Long userId) {
        return hotplaceMapper.deleteByIdAndUserId(id, userId);
    }
}
