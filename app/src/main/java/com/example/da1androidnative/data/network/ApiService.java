package com.example.da1androidnative.data.network;

import com.example.da1androidnative.data.model.AuthResponse;
import com.example.da1androidnative.data.model.LoginRequest;
import com.example.da1androidnative.data.model.OtpChallengeResponse;
import com.example.da1androidnative.data.model.OtpRequest;
import com.example.da1androidnative.data.model.OtpVerifyRequest;
import com.example.da1androidnative.data.model.RegisterRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/otp/request")
    Call<OtpChallengeResponse> requestOtp(@Body OtpRequest request);

    @POST("auth/otp/resend")
    Call<OtpChallengeResponse> resendOtp(@Body OtpRequest request);

    @POST("auth/otp/verify")
    Call<AuthResponse> verifyOtp(@Body OtpVerifyRequest request);
}
