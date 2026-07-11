package com.neoulteo.domain.hotplace.dto;

import java.io.Serializable;

public class HotplaceDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Integer attractionContentId;
    private String writerEmail;
    private String writerName;
    private String date;
    private String description;
    private String createdAt;
    private String updatedAt;

    private String name;
    private String address;
    private String imageUrl;
    private String firstimage;
    private String type;
    private Integer contentTypeId;
    private Integer areaCode;
    private Integer siGunGuCode;
    private String latitude;
    private String longitude;
    private String mapy;
    private String mapx;
    private Integer hotplaceCount;

    public HotplaceDto() {
    }

    public HotplaceDto(Long userId, Integer attractionContentId, String date, String description) {
        this.userId = userId;
        this.attractionContentId = attractionContentId;
        this.date = date;
        this.description = description;
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

    public String getWriterEmail() {
        return writerEmail;
    }

    public void setWriterEmail(String writerEmail) {
        this.writerEmail = writerEmail;
    }

    public String getWriterName() {
        return writerName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Integer getHotplaceCount() {
        return hotplaceCount;
    }

    public void setHotplaceCount(Integer hotplaceCount) {
        this.hotplaceCount = hotplaceCount;
    }
}
