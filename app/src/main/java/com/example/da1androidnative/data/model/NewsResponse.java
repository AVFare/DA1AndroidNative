package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class NewsResponse {

    @SerializedName("newsId")
    private long newsId;

    @SerializedName("title")
    private String title;

    @SerializedName("shortDescription")
    private String shortDescription;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("publishedAt")
    private String publishedAt;

    public long getNewsId() {
        return newsId;
    }

    public String getTitle() {
        return title;
    }

    public String getShortDescription() {
        return shortDescription;
    }
    public String getSummary() {
        return shortDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
}
