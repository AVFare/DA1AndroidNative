package com.example.da1androidnative.data.model;

import java.util.List;

public class PaginatedActivitiesResponse {
    private List<ActivityResponse> content;

    public List<ActivityResponse> getContent() {
        return content;
    }

    public void setContent(List<ActivityResponse> content) {
        this.content = content;
    }
}