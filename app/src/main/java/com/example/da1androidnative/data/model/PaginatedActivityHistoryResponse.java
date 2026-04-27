package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginatedActivityHistoryResponse {
    @SerializedName("content")
    private List<ActivityHistoryResponse> content;

    public List<ActivityHistoryResponse> getContent() {
        return content;
    }

    public void setContent(List<ActivityHistoryResponse> content) {
        this.content = content;
    }
}
