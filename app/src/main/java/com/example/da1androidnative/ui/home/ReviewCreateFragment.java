package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import com.example.da1androidnative.ui.util.ToastHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ReviewRequest;
import com.example.da1androidnative.data.model.ReviewResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.google.android.material.button.MaterialButton;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ReviewCreateFragment extends Fragment {

    private static final int MAX_COMMENT_LENGTH = 300;

    @Inject
    ApiService apiService;

    private long reservationId;
    private RatingBar activityRatingBar;
    private RatingBar guideRatingBar;
    private EditText commentEditText;
    private TextView commentCounterText;
    private MaterialButton submitButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.reviewCreateToolbar);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        activityRatingBar = view.findViewById(R.id.activityRatingBar);
        guideRatingBar = view.findViewById(R.id.guideRatingBar);
        commentEditText = view.findViewById(R.id.commentEditText);
        commentCounterText = view.findViewById(R.id.commentCounterText);
        submitButton = view.findViewById(R.id.submitReviewButton);

        bindArguments(view);
        setupCommentCounter();
        submitButton.setOnClickListener(v -> submitReview());
    }

    private void bindArguments(View view) {
        Bundle args = getArguments();
        if (args == null) {
            ToastHelper.show(getContext(), "Reserva invalida");
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        reservationId = args.getLong("reservationId", -1L);
        ((TextView) view.findViewById(R.id.reviewCreateActivityText)).setText(args.getString("activityName", ""));
        ((TextView) view.findViewById(R.id.reviewCreateDateText)).setText("Finalizada: " + formatDateTime(args.getString("completedAt", "")));
        ((TextView) view.findViewById(R.id.reviewCreateDeadlineText)).setText("Disponible hasta: " + formatDateTime(args.getString("expiresAt", "")));

        if (reservationId == -1L) {
            ToastHelper.show(getContext(), "Reserva invalida");
            NavHostFragment.findNavController(this).navigateUp();
        }
    }

    private void setupCommentCounter() {
        updateCommentCounter(0);
        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCommentCounter(s.length());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateCommentCounter(int length) {
        commentCounterText.setText(getString(R.string.review_comment_counter_format, length, MAX_COMMENT_LENGTH));
    }

    private void submitReview() {
        int activityRating = Math.round(activityRatingBar.getRating());
        int guideRating = Math.round(guideRatingBar.getRating());

        if (activityRating < 1 || guideRating < 1) {
            ToastHelper.show(getContext(), R.string.review_rating_required);
            return;
        }

        String comment = commentEditText.getText().toString().trim();
        if (comment.isEmpty()) {
            comment = null;
        }

        submitButton.setEnabled(false);
        ReviewRequest request = new ReviewRequest(reservationId, activityRating, guideRating, comment);
        apiService.createReview(request).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReviewResponse> call, @NonNull Response<ReviewResponse> response) {
                submitButton.setEnabled(true);
                if (response.isSuccessful()) {
                    ToastHelper.show(getContext(), R.string.review_submit_success);
                    NavHostFragment.findNavController(ReviewCreateFragment.this).navigateUp();
                } else {
                    ToastHelper.show(getContext(), R.string.review_submit_error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReviewResponse> call, @NonNull Throwable t) {
                submitButton.setEnabled(true);
                ToastHelper.show(getContext(), "Error de red: " + t.getMessage());
            }
        });
    }

    private String formatDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.replace("T", " ");
    }
}
