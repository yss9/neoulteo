package com.neoulteo.domain.attraction.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.neoulteo.domain.attraction.dao.DbAttractionDao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController {
    private final DbAttractionDao attractionDao;

    public AttractionController(DbAttractionDao attractionDao) {
        this.attractionDao = attractionDao;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(@RequestParam(name = "areaCode", required = false) String areaCode,
            @RequestParam(name = "gugunCode", required = false) String gugunCode,
            @RequestParam(name = "contentTypeId", required = false) String contentTypeId,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ok(attractionDao.searchAttractions(areaCode, gugunCode, contentTypeId, keyword));
    }

    @GetMapping("/sidos")
    public ResponseEntity<Map<String, Object>> sidos() {
        return ok(attractionDao.loadSidos());
    }

    @GetMapping("/guguns")
    public ResponseEntity<Map<String, Object>> guguns(
            @RequestParam(name = "sidoCode", required = false) String sidoCode) {
        return ok(attractionDao.loadGuguns(sidoCode));
    }

    private ResponseEntity<Map<String, Object>> ok(List<Map<String, String>> items) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("items", items);
        return ResponseEntity.ok(body);
    }
}
