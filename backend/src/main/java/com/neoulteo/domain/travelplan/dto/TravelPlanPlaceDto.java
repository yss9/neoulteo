package com.neoulteo.domain.travelplan.dto;

import java.io.Serializable;

public class TravelPlanPlaceDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer attractionContentId;
    private int dayNo = 1;
    private Integer contentTypeId;
    private String contentTypeName;
    private String name;
    private String address;
    private String imageUrl;
    private String firstimage;
    private String latitude;
    private String longitude;
    private String mapy;
    private String mapx;

    public TravelPlanPlaceDto() {
    }

    public TravelPlanPlaceDto(Integer attractionContentId, String name) {
        this.attractionContentId = attractionContentId;
        this.name = name;
    }

    public TravelPlanPlaceDto(Integer attractionContentId, int dayNo, String name) {
        this.attractionContentId = attractionContentId;
        this.dayNo = Math.max(dayNo, 1);
        this.name = name;
    }

    public Integer getAttractionContentId() {
        return attractionContentId;
    }

    public void setAttractionContentId(Integer attractionContentId) {
        this.attractionContentId = attractionContentId;
    }

    public int getDayNo() {
        return dayNo;
    }

    public void setDayNo(int dayNo) {
        this.dayNo = Math.max(dayNo, 1);
    }

    public Integer getContentTypeId() {
        return contentTypeId;
    }

    public void setContentTypeId(Integer contentTypeId) {
        this.contentTypeId = contentTypeId;
    }

    public String getContentTypeName() {
        return contentTypeName;
    }

    public void setContentTypeName(String contentTypeName) {
        this.contentTypeName = contentTypeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFirstimage() {
        return firstimage;
    }

    public void setFirstimage(String firstimage) {
        this.firstimage = firstimage;
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

    public String getMapy() {
        return mapy;
    }

    public void setMapy(String mapy) {
        this.mapy = mapy;
    }

    public String getMapx() {
        return mapx;
    }

    public void setMapx(String mapx) {
        this.mapx = mapx;
    }
}
