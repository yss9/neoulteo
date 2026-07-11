package com.neoulteo.domain.travelplan.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TravelPlanDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String writerEmail;
    private String title;
    private int durationDays = 1;
    private List<TravelPlanPlaceDto> places;
    private String createdAt;
    private boolean isShared;

    public TravelPlanDto(String id, String writerEmail, String title, int durationDays,
            List<TravelPlanPlaceDto> places, String createdAt, boolean isShared) {
        this.id = id;
        this.writerEmail = writerEmail;
        this.title = title;
        this.durationDays = Math.max(durationDays, 1);
        this.places = new ArrayList<>(places);
        this.createdAt = createdAt;
        this.isShared = isShared;
    }

    public TravelPlanDto(String id, String writerEmail, List<TravelPlanPlaceDto> places, String createdAt, boolean isShared) {
        this(id, writerEmail, null, 1, places, createdAt, isShared);
    }

    public TravelPlanDto(String id, String writerEmail, String title, int durationDays,
            List<TravelPlanPlaceDto> places, String createdAt) {
        this(id, writerEmail, title, durationDays, places, createdAt, false);
    }

    public TravelPlanDto(String id, String writerEmail, List<TravelPlanPlaceDto> places, String createdAt) {
        this(id, writerEmail, places, createdAt, false);
    }

    public String getId() {
        return id;
    }

    public String getWriterEmail() {
        return writerEmail;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public List<TravelPlanPlaceDto> getPlaces() {
        return new ArrayList<>(places);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isShared() {
        return isShared;
    }
}
