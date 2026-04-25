package com.example.da1androidnative.ui.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ReviewableReservationResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ReviewableReservationsAdapter extends RecyclerView.Adapter<ReviewableReservationsAdapter.ViewHolder> {

    private final Context context;
    private final OnReviewClickListener listener;
    private List<ReviewableReservationResponse> reservations = new ArrayList<>();

    public interface OnReviewClickListener {
        void onReviewClick(ReviewableReservationResponse reservation);
    }

    public ReviewableReservationsAdapter(Context context, OnReviewClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setReservations(List<ReviewableReservationResponse> reservations) {
        this.reservations = reservations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reviewable_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewableReservationResponse reservation = reservations.get(position);
        holder.activityNameText.setText(reservation.getActivityName());
        holder.destinationText.setText("Destino: " + reservation.getDestination());
        holder.guideText.setText("Guia: " + reservation.getGuideName());
        holder.dateText.setText("Fecha: " + reservation.getActivityDate());
        holder.deadlineText.setText("Disponible hasta: " + reservation.getReviewDeadline());
        holder.reviewButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReviewClick(reservation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityNameText;
        TextView destinationText;
        TextView guideText;
        TextView dateText;
        TextView deadlineText;
        MaterialButton reviewButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            activityNameText = itemView.findViewById(R.id.reviewableActivityNameText);
            destinationText = itemView.findViewById(R.id.reviewableDestinationText);
            guideText = itemView.findViewById(R.id.reviewableGuideText);
            dateText = itemView.findViewById(R.id.reviewableDateText);
            deadlineText = itemView.findViewById(R.id.reviewableDeadlineText);
            reviewButton = itemView.findViewById(R.id.reviewableButton);
        }
    }
}
