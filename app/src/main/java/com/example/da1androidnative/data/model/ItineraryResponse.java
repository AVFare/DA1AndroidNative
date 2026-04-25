package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ItineraryResponse {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("latitude")
    private double latitude;
    
    @SerializedName("longitude")
    private double longitude;
    
    @SerializedName("orderIndex")
    private int orderIndex;

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getOrderIndex() { return orderIndex; }
}