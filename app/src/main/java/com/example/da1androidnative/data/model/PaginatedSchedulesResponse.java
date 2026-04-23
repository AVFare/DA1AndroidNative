package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginatedSchedulesResponse {
    @SerializedName("content")
    private List<ScheduleResponse> content;

    public List<ScheduleResponse> getContent() {
        return content;
    }

    public void setContent(List<ScheduleResponse> content) {
        this.content = content;
    }
}
