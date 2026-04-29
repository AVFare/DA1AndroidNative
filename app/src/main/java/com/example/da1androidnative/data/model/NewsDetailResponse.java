package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class NewsDetailResponse extends NewsResponse {

    @SerializedName(value = "fullDescription", alternate = {"content", "body", "fullContent"})
    private String content;

    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    private String updatedAt;

    @SerializedName(value = "author", alternate = {"publishedBy", "source"})
    private String author;

    public String getContent() {
        return content;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getAuthor() {
        return author;
    }
}
