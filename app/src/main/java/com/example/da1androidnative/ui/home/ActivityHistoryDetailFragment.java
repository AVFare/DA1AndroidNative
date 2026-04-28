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

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ActivityHistoryDetailResponse;
import com.example.da1androidnative.data.network.ApiService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ActivityHistoryDetailFragment extends Fragment {

    @Inject ApiService apiService;

    private long reservationId;
    private View progressBar;
    private View contentGroup;
    private TextView activityNameText;
    private TextView destinationText;
    private TextView dateText;
    private TextView guideText;
    private TextView durationText;
    private TextView meetingPointText;
    private TextView cancellationPolicyText;
    private TextView ratingStatusText;
    private TextView activityRatingText;
    private TextView guideRatingText;
    private TextView commentText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_history_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reservationId = getArguments() != null ? getArguments().getLong("reservationId", -1L) : -1L;

        bindViews(view);
        Toolbar toolbar = view.findViewById(R.id.historyDetailToolbar);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        if (reservationId == -1L) {
            Toast.makeText(getContext(), "Reserva invalida", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        loadDetail();
    }

    private void bindViews(View view) {
        progressBar = view.findViewById(R.id.historyDetailProgressBar);
        contentGroup = view.findViewById(R.id.historyDetailContent);
        activityNameText = view.findViewById(R.id.historyDetailActivityName);
        destinationText = view.findViewById(R.id.historyDetailDestination);
        dateText = view.findViewById(R.id.historyDetailDate);
        guideText = view.findViewById(R.id.historyDetailGuide);
        durationText = view.findViewById(R.id.historyDetailDuration);
        meetingPointText = view.findViewById(R.id.historyDetailMeetingPoint);
        cancellationPolicyText = view.findViewById(R.id.historyDetailCancellationPolicy);
        ratingStatusText = view.findViewById(R.id.historyDetailRatingStatus);
        activityRatingText = view.findViewById(R.id.historyDetailActivityRating);
        guideRatingText = view.findViewById(R.id.historyDetailGuideRating);
        commentText = view.findViewById(R.id.historyDetailComment);
    }

    private void loadDetail() {
        progressBar.setVisibility(View.VISIBLE);
        contentGroup.setVisibility(View.GONE);

        apiService.getActivityHistoryDetail(reservationId).enqueue(new Callback<ActivityHistoryDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityHistoryDetailResponse> call, @NonNull Response<ActivityHistoryDetailResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    bindDetail(response.body());
                    contentGroup.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "No se pudo cargar el detalle", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivityHistoryDetailResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindDetail(ActivityHistoryDetailResponse detail) {
        activityNameText.setText(orDash(detail.getActivityName()));
        destinationText.setText("Destino: " + orDash(detail.getDestination()));
        dateText.setText("Fecha: " + orDash(detail.getDate()));
        guideText.setText("Guia: " + orDash(detail.getGuideName()));
        durationText.setText("Duracion: " + formatDuration(detail.getDurationMinutes()));
        meetingPointText.setText(orDash(detail.getMeetingPoint()));
        cancellationPolicyText.setText(orDash(detail.getCancellationPolicy()));

        if (detail.isHasRating()) {
            ratingStatusText.setText("Calificacion dejada");
            activityRatingText.setText("Actividad: " + formatStars(detail.getActivityStars()));
            guideRatingText.setText("Guia: " + formatStars(detail.getGuideStars()));
            commentText.setText(isBlank(detail.getComment()) ? "Sin comentario" : detail.getComment());
        } else {
            ratingStatusText.setText("Sin calificacion");
            activityRatingText.setText("Actividad: -");
            guideRatingText.setText("Guia: -");
            commentText.setText("Todavia no dejaste una calificacion para esta actividad.");
        }
    }

    private String formatStars(Integer stars) {
        return stars == null ? "-" : stars + "/5";
    }

    private String formatDuration(Integer minutes) {
        if (minutes == null) return "-";
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (hours > 0) {
            return hours + " h " + remainingMinutes + " min";
        }
        return remainingMinutes + " min";
    }

    private String orDash(String value) {
        return isBlank(value) ? "-" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
