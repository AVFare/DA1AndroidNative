package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class NewsDetailResponse extends NewsResponse {

    @SerializedName("fullDescription")
    private String fullDescription;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getFullDescription() {
        return fullDescription;
    }

    public String getContent() {
        return fullDescription;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
