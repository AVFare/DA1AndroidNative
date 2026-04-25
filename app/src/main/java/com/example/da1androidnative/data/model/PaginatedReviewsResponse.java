package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginatedReviewsResponse {
    @SerializedName("content")
    private List<ReviewResponse> content;

    public List<ReviewResponse> getContent() {
        return content;
    }
}
