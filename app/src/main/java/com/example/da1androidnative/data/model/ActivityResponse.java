package com.example.da1androidnative.data.model;
import java.util.List;

public class ActivityResponse {
    private Long id;
    private String name;
    private String shortDescription;
    private Double basePrice;
    private List<ActivityImageResponse> images;

    public Long getId() {
        return id;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public List<ActivityImageResponse> getImages() { return images; }

    public String getFirstImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageUrl();
        }
        return null; // O una URL de imagen por defecto
    }
}