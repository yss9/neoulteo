package com.neoulteo.batch.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TourBatchComparisonResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<TourAttractionDto> apiItems = new ArrayList<>();
    private List<TourAttractionDto> dbItems = new ArrayList<>();
    private List<TourAttractionChangeDto> newItems = new ArrayList<>();
    private List<TourAttractionChangeDto> changedItems = new ArrayList<>();
    private List<TourAttractionChangeDto> missingItems = new ArrayList<>();
    private List<TourAttractionChangeDto> unchangedItems = new ArrayList<>();

    public List<TourAttractionDto> getApiItems() {
        return apiItems;
    }

    public void setApiItems(List<TourAttractionDto> apiItems) {
        this.apiItems = apiItems == null ? new ArrayList<>() : apiItems;
    }

    public List<TourAttractionDto> getDbItems() {
        return dbItems;
    }

    public void setDbItems(List<TourAttractionDto> dbItems) {
        this.dbItems = dbItems == null ? new ArrayList<>() : dbItems;
    }

    public List<TourAttractionChangeDto> getNewItems() {
        return newItems;
    }

    public void setNewItems(List<TourAttractionChangeDto> newItems) {
        this.newItems = newItems == null ? new ArrayList<>() : newItems;
    }

    public List<TourAttractionChangeDto> getChangedItems() {
        return changedItems;
    }

    public void setChangedItems(List<TourAttractionChangeDto> changedItems) {
        this.changedItems = changedItems == null ? new ArrayList<>() : changedItems;
    }

    public List<TourAttractionChangeDto> getMissingItems() {
        return missingItems;
    }

    public void setMissingItems(List<TourAttractionChangeDto> missingItems) {
        this.missingItems = missingItems == null ? new ArrayList<>() : missingItems;
    }

    public List<TourAttractionChangeDto> getUnchangedItems() {
        return unchangedItems;
    }

    public void setUnchangedItems(List<TourAttractionChangeDto> unchangedItems) {
        this.unchangedItems = unchangedItems == null ? new ArrayList<>() : unchangedItems;
    }

    public int getApiItemCount() {
        return apiItems.size();
    }

    public int getNewItemCount() {
        return newItems.size();
    }

    public int getChangedItemCount() {
        return changedItems.size();
    }

    public int getMissingItemCount() {
        return missingItems.size();
    }

    public int getUnchangedItemCount() {
        return unchangedItems.size();
    }
}
