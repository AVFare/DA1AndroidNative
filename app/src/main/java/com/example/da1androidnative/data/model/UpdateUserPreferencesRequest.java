package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateUserPreferencesRequest {
    @SerializedName("preferences")
    private List<String> preferences;

    public UpdateUserPreferencesRequest(List<String> preferences) {
        this.preferences = preferences;
    }

    public List<String> getTravelPreferences() {
        return preferences;
    }

    public void setTravelPreferences(List<String> preferences) {
        this.preferences = preferences;
    }
}
