package com.neoulteo.ai.resource;

public class RagAttractionDocument {
    private Integer contentId;
    private String title;
    private String address;
    private String contentTypeName;
    private String overview;
    private Integer areaCode;
    private Integer sigunguCode;

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContentTypeName() {
        return contentTypeName;
    }

    public void setContentTypeName(String contentTypeName) {
        this.contentTypeName = contentTypeName;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Integer getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode;
    }

    public Integer getSigunguCode() {
        return sigunguCode;
    }

    public void setSigunguCode(Integer sigunguCode) {
        this.sigunguCode = sigunguCode;
    }

    public String toSearchText() {
        StringBuilder sb = new StringBuilder();
        append(sb, "관광지명", title);
        append(sb, "주소", address);
        append(sb, "분류", contentTypeName);
        append(sb, "설명", overview);
        return sb.toString().trim();
    }

    private void append(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append('\n');
        }
        sb.append(label).append(": ").append(value);
    }
}
