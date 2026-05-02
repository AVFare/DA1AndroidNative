package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SavedActivityCheckResponse {
    @SerializedName("content")
    private List<SavedActivityCheckItem> content;

    public List<SavedActivityCheckItem> getContent() {
        return content;
    }
}
