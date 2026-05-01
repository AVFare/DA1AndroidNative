package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class DestinationOptionResponse {
    @SerializedName("destinationId")
    private Long destinationId;

    @SerializedName("name")
    private String name;

    public Long getDestinationId() {
        return destinationId;
    }

    public String getName() {
        return name;
    }
}
