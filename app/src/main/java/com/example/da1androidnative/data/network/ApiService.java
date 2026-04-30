package com.example.da1androidnative.data.network;

import com.example.da1androidnative.data.model.ActivityDetalleResponse;
import com.example.da1androidnative.data.model.ActivityHistoryDetailResponse;
import com.example.da1androidnative.data.model.ActivityHistoryResponse;
import com.example.da1androidnative.data.model.AuthResponse;
import com.example.da1androidnative.data.model.ConfirmChangePasswordRequest;
import com.example.da1androidnative.data.model.ConfirmChangePasswordResponse;
import com.example.da1androidnative.data.model.ConfirmEmailChangeRequest;
import com.example.da1androidnative.data.model.InitiateEmailChangeRequest;
import com.example.da1androidnative.data.model.LoginRequest;
import com.example.da1androidnative.data.model.NewsDetailResponse;
import com.example.da1androidnative.data.model.NewsResponse;
import com.example.da1androidnative.data.model.OtpChallengeResponse;
import com.example.da1androidnative.data.model.OtpRequest;
import com.example.da1androidnative.data.model.OtpVerifyRequest;
import com.example.da1androidnative.data.model.PaginatedActivityHistoryResponse;
import com.example.da1androidnative.data.model.PaginatedReservasResponse;
import com.example.da1androidnative.data.model.PaginatedSchedulesResponse;
import com.example.da1androidnative.data.model.RegisterRequest;
import com.example.da1androidnative.data.model.ActivityResponse;
import com.example.da1androidnative.data.model.PaginatedActivitiesResponse;
import com.example.da1androidnative.data.model.ReservaCancelledResponse;
import com.example.da1androidnative.data.model.ReservaDetalleResponse;
import com.example.da1androidnative.data.model.ReservaRequest;
import com.example.da1androidnative.data.model.ReviewRequest;
import com.example.da1androidnative.data.model.ReviewResponse;
import com.example.da1androidnative.data.model.ReviewableReservationResponse;
import com.example.da1androidnative.data.model.UpdateUserPreferencesRequest;
import com.example.da1androidnative.data.model.UserProfileResponse;
import com.example.da1androidnative.data.model.UpdateUserProfileRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST
    Call<Void> initiateChangePassword();

    @POST
    Call<ConfirmChangePasswordResponse> ConfirmChangePassword(@Body ConfirmChangePasswordRequest confirmChangePasswordRequest);

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

    @POST("reservations")
    Call<ReservaRequest> reserveActivity(@Body ReservaRequest reservaRequest);

    @DELETE("reservations/{reservationId}")
    Call<ReservaCancelledResponse> cancelReserva(@Path("reservationId") long reservationId);

    @POST("auth/otp/request")
    Call<OtpChallengeResponse> requestOtp(@Body OtpRequest request);

    @POST("auth/otp/resend")
    Call<OtpChallengeResponse> resendOtp(@Body OtpRequest request);

    @POST("auth/otp/verify")
    Call<AuthResponse> verifyOtp(@Body OtpVerifyRequest request);

    @GET("ratings/pending")
    Call<List<ReviewableReservationResponse>> getReviewableReservations();

    @POST("ratings")
    Call<ReviewResponse> createReview(@Body ReviewRequest request);

    @GET("activity/history")
    Call<PaginatedActivityHistoryResponse> getActivityHistory(
            @Query("fromDate") String fromDate,
            @Query("toDate") String toDate,
            @Query("destinationId") Long destinationId,
            @Query("status") String status,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    @GET("activity/history/{reservationId}")
    Call<ActivityHistoryDetailResponse> getActivityHistoryDetail(@Path("reservationId") long reservationId);

    @GET("profile")
    Call<UserProfileResponse> getMyProfile();

    @PUT("profile")
    Call<UserProfileResponse> updateMyProfile(@Body UpdateUserProfileRequest request);

    @PUT("profile/preferences")
    Call<UserProfileResponse> updateMyPreferences(@Body UpdateUserPreferencesRequest request);

    @DELETE("profile/me")
    Call<Void> deleteAccount();

    @POST("profile/me/email-change/initiate")
    Call<Void> initiateEmailChange(@Body InitiateEmailChangeRequest request);

    @POST("profile/me/email-change/confirm")
    Call<Void> confirmEmailChange(@Body ConfirmEmailChangeRequest request);

    @GET("news")
    Call<List<NewsResponse>> getNews();

    @GET("news/{newsId}")
    Call<NewsDetailResponse> getNewsDetail(@Path("newsId") long newsId);
}
