package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ConfirmChangePasswordRequest {
    @SerializedName("code")
    private int code;
    @SerializedName("newPassword")
    private String newPassword;

    public ConfirmChangePasswordRequest(int code, String newPassword) {
        this.code = code;
        this.newPassword = newPassword;
    }

    public int getCode() {
        return code;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
