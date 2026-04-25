package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginatedReviewableReservationsResponse {
    @SerializedName("content")
    private List<ReviewableReservationResponse> content;

    public List<ReviewableReservationResponse> getContent() {
        return content;
    }
}
