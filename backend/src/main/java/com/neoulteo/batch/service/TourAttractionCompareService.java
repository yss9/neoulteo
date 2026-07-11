package com.neoulteo.batch.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.neoulteo.batch.dto.TourAttractionChangeDto;
import com.neoulteo.batch.dto.TourAttractionDto;
import com.neoulteo.batch.dto.TourBatchComparisonResult;
import org.springframework.stereotype.Service;

@Service
public class TourAttractionCompareService {
    public TourBatchComparisonResult compare(List<TourAttractionDto> apiItems, List<TourAttractionDto> dbItems) {
        TourBatchComparisonResult result = new TourBatchComparisonResult();
        result.setApiItems(apiItems);
        result.setDbItems(dbItems);

        Map<String, TourAttractionDto> dbByContentId = toContentIdMap(dbItems);
        Map<String, TourAttractionDto> apiByContentId = toContentIdMap(apiItems);
        List<TourAttractionChangeDto> newItems = new ArrayList<>();
        List<TourAttractionChangeDto> changedItems = new ArrayList<>();
        List<TourAttractionChangeDto> unchangedItems = new ArrayList<>();
        List<TourAttractionChangeDto> missingItems = new ArrayList<>();

        for (TourAttractionDto apiItem : safe(apiItems)) {
            TourAttractionDto dbItem = dbByContentId.get(apiItem.getContentId());
            if (dbItem == null) {
                newItems.add(toChange(apiItem, "NEW", "TourAPI에는 있지만 DB에는 없는 신규 관광지입니다."));
                continue;
            }

            String changeDescription = describeChanges(dbItem, apiItem);
            if (changeDescription.isBlank()) {
                unchangedItems.add(toChange(apiItem, "UNCHANGED", "변경 없음"));
            } else {
                changedItems.add(toChange(apiItem, "CHANGED", changeDescription));
            }
        }

        for (TourAttractionDto dbItem : safe(dbItems)) {
            if (!apiByContentId.containsKey(dbItem.getContentId())) {
                missingItems.add(toChange(dbItem, "MISSING", "DB에는 있지만 이번 TourAPI 응답에는 없는 관광지입니다."));
            }
        }

        result.setNewItems(newItems);
        result.setChangedItems(changedItems);
        result.setUnchangedItems(unchangedItems);
        result.setMissingItems(missingItems);
        return result;
    }

    public String buildChangeSummary(TourBatchComparisonResult result) {
        if (result == null) {
            return "비교 결과가 없습니다.";
        }
        return "신규 " + result.getNewItemCount()
                + "건, 변경 " + result.getChangedItemCount()
                + "건, 삭제 후보 " + result.getMissingItemCount()
                + "건, 동일 " + result.getUnchangedItemCount() + "건";
    }

    private Map<String, TourAttractionDto> toContentIdMap(List<TourAttractionDto> items) {
        Map<String, TourAttractionDto> map = new LinkedHashMap<>();
        for (TourAttractionDto item : safe(items)) {
            if (hasText(item.getContentId())) {
                map.put(item.getContentId(), item);
            }
        }
        return map;
    }

    private String describeChanges(TourAttractionDto oldItem, TourAttractionDto newItem) {
        List<String> changes = new ArrayList<>();
        addChange(changes, "관광지명", oldItem.getTitle(), newItem.getTitle());
        addChange(changes, "주소", oldItem.getAddr1(), newItem.getAddr1());
        addChange(changes, "이미지", oldItem.getFirstImage1(), newItem.getFirstImage1());
        addDecimalChange(changes, "위도", oldItem.getLatitude(), newItem.getLatitude());
        addDecimalChange(changes, "경도", oldItem.getLongitude(), newItem.getLongitude());
        addChange(changes, "콘텐츠타입", oldItem.getContentTypeId(), newItem.getContentTypeId());
        addChange(changes, "지역코드", oldItem.getAreaCode(), newItem.getAreaCode());
        addChange(changes, "시군구코드", oldItem.getSiGunGuCode(), newItem.getSiGunGuCode());
        return String.join("; ", changes);
    }

    private void addChange(List<String> changes, String fieldName, String oldValue, String newValue) {
        if (!normalize(oldValue).equals(normalize(newValue))) {
            changes.add(fieldName + ": " + display(oldValue) + " -> " + display(newValue));
        }
    }

    private void addDecimalChange(List<String> changes, String fieldName, String oldValue, String newValue) {
        BigDecimal oldDecimal = toDecimal(oldValue);
        BigDecimal newDecimal = toDecimal(newValue);
        if (oldDecimal != null && newDecimal != null) {
            if (oldDecimal.compareTo(newDecimal) != 0) {
                changes.add(fieldName + ": " + display(oldValue) + " -> " + display(newValue));
            }
            return;
        }
        addChange(changes, fieldName, oldValue, newValue);
    }

    private BigDecimal toDecimal(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private TourAttractionChangeDto toChange(TourAttractionDto item, String changeType, String description) {
        return new TourAttractionChangeDto(
                item.getContentId(),
                item.getTitle(),
                changeType,
                description,
                item.getContentTypeId(),
                item.getAreaCode(),
                item.getSiGunGuCode());
    }

    private List<TourAttractionDto> safe(List<TourAttractionDto> items) {
        return items == null ? List.of() : items;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String display(String value) {
        return hasText(value) ? value.trim() : "-";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
