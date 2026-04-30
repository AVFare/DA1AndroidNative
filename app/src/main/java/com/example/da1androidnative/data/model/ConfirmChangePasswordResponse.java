package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ConfirmChangePasswordResponse {
    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }
}
