package com.neoulteo.batch.dto;

import java.io.Serializable;

public class TourAttractionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String contentId;
    private String title;
    private String addr1;
    private String firstImage1;
    private String latitude;
    private String longitude;
    private String contentTypeId;
    private String areaCode;
    private String siGunGuCode;
    private String tel;

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

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        this.addr1 = addr1;
    }

    public String getFirstImage1() {
        return firstImage1;
    }

    public void setFirstImage1(String firstImage1) {
        this.firstImage1 = firstImage1;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
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

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
