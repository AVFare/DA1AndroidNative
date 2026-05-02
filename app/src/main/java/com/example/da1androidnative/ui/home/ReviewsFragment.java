package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ReviewableReservationResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.home.adapter.ReviewableReservationsAdapter;
import com.example.da1androidnative.ui.util.ToastHelper;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ReviewsFragment extends Fragment implements ReviewableReservationsAdapter.OnReviewClickListener {

    @Inject
    ApiService apiService;

    private ReviewableReservationsAdapter reviewableAdapter;
    private TextView emptyReviewableText;
    private LinearProgressIndicator progressIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressIndicator = view.findViewById(R.id.progressIndicator);

        Toolbar toolbar = view.findViewById(R.id.reviewsToolbar);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        emptyReviewableText = view.findViewById(R.id.emptyReviewableText);

        setupRecyclerView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (reviewableAdapter != null) {
            loadReviewableReservations();
        }
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView(View view) {
        RecyclerView reviewableRecyclerView = view.findViewById(R.id.reviewableReservationsRecyclerView);
        reviewableRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewableRecyclerView.setNestedScrollingEnabled(false);
        reviewableAdapter = new ReviewableReservationsAdapter(getContext(), this);
        reviewableRecyclerView.setAdapter(reviewableAdapter);
    }

    private void loadReviewableReservations() {
        setLoading(true);
        apiService.getReviewableReservations().enqueue(new Callback<List<ReviewableReservationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReviewableReservationResponse>> call,
                                   @NonNull Response<List<ReviewableReservationResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<ReviewableReservationResponse> items = response.body();
                    if (items == null) {
                        items = new ArrayList<>();
                    }
                    reviewableAdapter.setReservations(items);
                    emptyReviewableText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    ToastHelper.show(getContext(), "Error al cargar actividades para calificar");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReviewableReservationResponse>> call, @NonNull Throwable t) {
                setLoading(false);
                ToastHelper.show(getContext(), "Error de red: " + t.getMessage());
            }
        });
    }

    @Override
    public void onReviewClick(ReviewableReservationResponse reservation) {
        Bundle args = new Bundle();
        args.putLong("reservationId", reservation.getReservationId());
        args.putString("activityName", reservation.getActivityName());
        args.putString("completedAt", reservation.getCompletedAt());
        args.putString("expiresAt", reservation.getExpiresAt());
        NavHostFragment.findNavController(this).navigate(R.id.action_reviewsFragment_to_reviewCreateFragment, args);
    }
}