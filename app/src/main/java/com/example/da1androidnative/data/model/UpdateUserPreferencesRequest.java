package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateUserPreferencesRequest {
    @SerializedName("travelPreferences")
    private List<String> travelPreferences;

    public UpdateUserPreferencesRequest(List<String> travelPreferences) {
        this.travelPreferences = travelPreferences;
    }

    public List<String> getTravelPreferences() {
        return travelPreferences;
    }

    public void setTravelPreferences(List<String> travelPreferences) {
        this.travelPreferences = travelPreferences;
    }
}
