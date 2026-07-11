package com.neoulteo.domain.attraction.mapper;

import java.util.List;
import java.util.Map;

import com.neoulteo.ai.resource.RagAttractionDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttractionMapper {
    List<Map<String, String>> loadSidos();

    List<Map<String, String>> loadGuguns(@Param("sidoCode") String sidoCode);

    List<Map<String, String>> searchAttractions(@Param("areaCode") String areaCode,
            @Param("gugunCode") String gugunCode,
            @Param("contentTypeId") String contentTypeId,
            @Param("keyword") String keyword);

    List<RagAttractionDocument> findRagAttractionDocuments(@Param("limit") Integer limit);
}
