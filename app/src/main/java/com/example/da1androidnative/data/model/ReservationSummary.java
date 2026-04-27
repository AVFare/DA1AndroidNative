package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ReservationSummary {
    @SerializedName("confirmed")
    private int confirmed;
    @SerializedName("cancelled")
    private int cancelled;
    @SerializedName("completed")
    private int completed;

    public ReservationSummary(int confirmed, int cancelled, int completed) {
        this.confirmed = confirmed;
        this.cancelled = cancelled;
        this.completed = completed;
    }

    public int getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(int confirmed) {
        this.confirmed = confirmed;
    }

    public int getCancelled() {
        return cancelled;
    }

    public void setCancelled(int cancelled) {
        this.cancelled = cancelled;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }
}
