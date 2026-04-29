package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class NewsResponse {

    @SerializedName(value = "newsId", alternate = {"id"})
    private long newsId;

    @SerializedName("title")
    private String title;

    @SerializedName(value = "shortDescription", alternate = {"summary", "description", "excerpt"})
    private String summary;

    @SerializedName(value = "imageUrl", alternate = {"image", "coverImage"})
    private String imageUrl;

    @SerializedName(value = "publishedAt", alternate = {"createdAt", "date"})
    private String publishedAt;

    public long getNewsId() {
        return newsId;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
}
