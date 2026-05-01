package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginatedActivitiesResponse {
    @SerializedName("content")
    private List<ActivityResponse> content;
    @SerializedName("last")
    private Boolean last;
    @SerializedName("totalPages")
    private Integer totalPages;

    public List<ActivityResponse> getContent() {
        return content;
    }

    public void setContent(List<ActivityResponse> content) {
        this.content = content;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
