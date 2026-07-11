package com.neoulteo.domain.savedplace.dto;

import java.io.Serializable;

public class SavedPlaceDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Integer attractionContentId;
    private String sourceType;
    private String createdAt;

    private String name;
    private String address;
    private String imageUrl;
    private String firstimage;
    private Integer contentTypeId;
    private Integer areaCode;
    private Integer siGunGuCode;
    private String latitude;
    private String longitude;
    private String mapy;
    private String mapx;

    public SavedPlaceDto() {
    }

    public SavedPlaceDto(Long userId, Integer attractionContentId, String sourceType) {
        this.userId = userId;
        this.attractionContentId = attractionContentId;
        this.sourceType = sourceType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAttractionContentId() {
        return attractionContentId;
    }

    public void setAttractionContentId(Integer attractionContentId) {
        this.attractionContentId = attractionContentId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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

    public Integer getContentTypeId() {
        return contentTypeId;
    }

    public void setContentTypeId(Integer contentTypeId) {
        this.contentTypeId = contentTypeId;
    }

    public Integer getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode;
    }

    public Integer getSiGunGuCode() {
        return siGunGuCode;
    }

    public void setSiGunGuCode(Integer siGunGuCode) {
        this.siGunGuCode = siGunGuCode;
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
