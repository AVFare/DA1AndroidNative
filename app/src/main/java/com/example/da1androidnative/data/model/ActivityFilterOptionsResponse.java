package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ActivityFilterOptionsResponse {
    @SerializedName("destinations")
    private List<DestinationOptionResponse> destinations;

    @SerializedName("categories")
    private List<String> categories;

    public List<DestinationOptionResponse> getDestinations() {
        return destinations;
    }

    public List<String> getCategories() {
        return categories;
    }
}
