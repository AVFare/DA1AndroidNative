package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.PaginatedReviewableReservationsResponse;
import com.example.da1androidnative.data.model.PaginatedReviewsResponse;
import com.example.da1androidnative.data.model.ReviewResponse;
import com.example.da1androidnative.data.model.ReviewableReservationResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.home.adapter.ReviewableReservationsAdapter;
import com.example.da1androidnative.ui.home.adapter.ReviewsAdapter;

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
    private ReviewsAdapter reviewsAdapter;
    private TextView emptyReviewableText;
    private TextView emptyReviewsText;

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

        Toolbar toolbar = view.findViewById(R.id.reviewsToolbar);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        emptyReviewableText = view.findViewById(R.id.emptyReviewableText);
        emptyReviewsText = view.findViewById(R.id.emptyReviewsText);

        setupRecyclerViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (reviewableAdapter != null && reviewsAdapter != null) {
            loadReviewableReservations();
            //TODO: Reimplementar loadMyReviews()
            //loadMyReviews();
        }
    }

    private void setupRecyclerViews(View view) {
        RecyclerView reviewableRecyclerView = view.findViewById(R.id.reviewableReservationsRecyclerView);
        reviewableRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewableRecyclerView.setNestedScrollingEnabled(false);
        reviewableAdapter = new ReviewableReservationsAdapter(getContext(), this);
        reviewableRecyclerView.setAdapter(reviewableAdapter);

        RecyclerView reviewsRecyclerView = view.findViewById(R.id.myReviewsRecyclerView);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewsRecyclerView.setNestedScrollingEnabled(false);
        reviewsAdapter = new ReviewsAdapter(getContext());
        reviewsRecyclerView.setAdapter(reviewsAdapter);
    }

    private void loadReviewableReservations() {
        apiService.getReviewableReservations().enqueue(new Callback<List<ReviewableReservationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReviewableReservationResponse>> call,
                                   @NonNull Response<List<ReviewableReservationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReviewableReservationResponse> items = response.body();
                    if (items.isEmpty()) {
                        items = new ArrayList<>();
                    }
                    reviewableAdapter.setReservations(items);
                    emptyReviewableText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(getContext(), "Error al cargar actividades para calificar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReviewableReservationResponse>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    //TODO: Reimplementar endpoint de myReviews

//    private void loadMyReviews() {
//        apiService.getMyReviews().enqueue(new Callback<PaginatedReviewsResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<PaginatedReviewsResponse> call,
//                                   @NonNull Response<PaginatedReviewsResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<ReviewResponse> items = response.body().getContent();
//                    if (items == null) {
//                        items = new ArrayList<>();
//                    }
//                    reviewsAdapter.setReviews(items);
//                    emptyReviewsText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
//                } else {
//                    Toast.makeText(getContext(), "Error al cargar calificaciones", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<PaginatedReviewsResponse> call, @NonNull Throwable t) {
//                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }

    @Override
    public void onReviewClick(ReviewableReservationResponse reservation) {
        Bundle args = new Bundle();
        args.putLong("reservationId", reservation.getReservationId());
        args.putString("activityName", reservation.getActivityName());
        //TODO: Implementar los demas campos
//        args.putString("destination", reservation.getDestination());
//        args.putString("guideName", reservation.getGuideName());
//        args.putString("activityDate", reservation.getActivityDate());
//        args.putString("reviewDeadline", reservation.getReviewDeadline());
        NavHostFragment.findNavController(this).navigate(R.id.action_reviewsFragment_to_reviewCreateFragment, args);
    }
}
