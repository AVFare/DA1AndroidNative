package com.example.da1androidnative.data.model;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ActivityResponse {
    @SerializedName("activityId")
    private long activityId;
    @SerializedName("name")
    private String name;
    @SerializedName("destination")
    private String destination;
    @SerializedName("category")
    private String category;
    @SerializedName("durationMinutes")
    private int durationMinutes;
    @SerializedName("availableSpots")
    private int availableSpots;
    @SerializedName("price")
    private Double basePrice;
    private List<ActivityImageResponse> images;

    public String getFirstImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageUrl();
        }
        return null; // O una URL de imagen por defecto
    }

    public long getId() {
        return activityId;
    }

    public String getName() {
        return name;
    }

    public String getDestination() {
        return destination;
    }

    public String getCategory() {
        return category;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getAvailableSpots() {
        return availableSpots;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public List<ActivityImageResponse> getImages() {
        return images;
    }
}