package com.example.da1androidnative.data.network;

import com.example.da1androidnative.data.model.ActivityDetalleResponse;
import com.example.da1androidnative.data.model.AuthResponse;
import com.example.da1androidnative.data.model.LoginRequest;
import com.example.da1androidnative.data.model.OtpChallengeResponse;
import com.example.da1androidnative.data.model.OtpRequest;
import com.example.da1androidnative.data.model.OtpVerifyRequest;
import com.example.da1androidnative.data.model.PaginatedReservasResponse;
import com.example.da1androidnative.data.model.PaginatedSchedulesResponse;
import com.example.da1androidnative.data.model.RegisterRequest;
import com.example.da1androidnative.data.model.ActivityResponse;
import com.example.da1androidnative.data.model.PaginatedActivitiesResponse;
import com.example.da1androidnative.data.model.ReservaCancelledResponse;
import com.example.da1androidnative.data.model.ReservaDetalleResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
    @GET("activities")
    Call<PaginatedActivitiesResponse> getAllActivities();

    @GET("activities/{activityId}")
    Call<ActivityDetalleResponse> getDetalleActivity(@Path("activityId") long activityId);

    @GET("activities/{activityId}/schedules")
    Call<PaginatedSchedulesResponse> getHorariosActivity(@Path("activityId") long activityId);

    @GET("reservations/my")
    Call<PaginatedReservasResponse> getAllReservas();

    @GET("reservations/{reservationId}")
    Call<ReservaDetalleResponse> getDetalleReserva(@Path("reservationId") long reservationId);

    @DELETE("reservations/{reservationId}")
    Call<ReservaCancelledResponse> cancelReserva(@Path("reservationId") long reservationId);

    @POST("auth/otp/request")
    Call<OtpChallengeResponse> requestOtp(@Body OtpRequest request);

    @POST("auth/otp/resend")
    Call<OtpChallengeResponse> resendOtp(@Body OtpRequest request);

    @POST("auth/otp/verify")
    Call<AuthResponse> verifyOtp(@Body OtpVerifyRequest request);
}
