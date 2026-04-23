package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginatedReservasResponse {
    @SerializedName("content")
    private List<ReservaResponse> content;

    public List<ReservaResponse> getContent() {
        return content;
    }

    public void setContent(List<ReservaResponse> content) {
        this.content = content;
    }
}
