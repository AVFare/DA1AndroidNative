package com.example.da1androidnative.data.model;
import com.google.gson.annotations.SerializedName;
public class AuthResponse {
    @SerializedName("token")
    private String token;
    @SerializedName("userId")
    private Long userId;

    public AuthResponse() {}
    public String getToken() { return token; }
    public void setToken(String token) {
        this.token = token;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
