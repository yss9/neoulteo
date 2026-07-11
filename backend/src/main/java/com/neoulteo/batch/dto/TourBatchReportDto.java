package com.neoulteo.batch.dto;

public class TourBatchReportDto {
    private Long id;
    private String reportDate;
    private String areaCode;
    private String contentTypeId;
    private int itemCount;
    private int newItemCount;
    private int changedItemCount;
    private int missingItemCount;
    private int unchangedItemCount;
    private String changeSummary;
    private String aiSummary;
    private String pdfPath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getContentTypeId() {
        return contentTypeId;
    }

    public void setContentTypeId(String contentTypeId) {
        this.contentTypeId = contentTypeId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getNewItemCount() {
        return newItemCount;
    }

    public void setNewItemCount(int newItemCount) {
        this.newItemCount = newItemCount;
    }

    public int getChangedItemCount() {
        return changedItemCount;
    }

    public void setChangedItemCount(int changedItemCount) {
        this.changedItemCount = changedItemCount;
    }

    public int getMissingItemCount() {
        return missingItemCount;
    }

    public void setMissingItemCount(int missingItemCount) {
        this.missingItemCount = missingItemCount;
    }

    public int getUnchangedItemCount() {
        return unchangedItemCount;
    }

    public void setUnchangedItemCount(int unchangedItemCount) {
        this.unchangedItemCount = unchangedItemCount;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }
}
