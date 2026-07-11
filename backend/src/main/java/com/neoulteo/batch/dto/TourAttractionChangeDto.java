package com.neoulteo.batch.dto;

import java.io.Serializable;

public class TourAttractionChangeDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String contentId;
    private String title;
    private String changeType;
    private String changeDescription;
    private String contentTypeId;
    private String areaCode;
    private String siGunGuCode;

    public TourAttractionChangeDto() {
    }

    public TourAttractionChangeDto(String contentId, String title, String changeType,
            String changeDescription, String contentTypeId, String areaCode, String siGunGuCode) {
        this.contentId = contentId;
        this.title = title;
        this.changeType = changeType;
        this.changeDescription = changeDescription;
        this.contentTypeId = contentTypeId;
        this.areaCode = areaCode;
        this.siGunGuCode = siGunGuCode;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }

    public String getContentTypeId() {
        return contentTypeId;
    }

    public void setContentTypeId(String contentTypeId) {
        this.contentTypeId = contentTypeId;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getSiGunGuCode() {
        return siGunGuCode;
    }

    public void setSiGunGuCode(String siGunGuCode) {
        this.siGunGuCode = siGunGuCode;
    }
}
