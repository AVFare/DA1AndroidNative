package com.example.da1androidnative.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ActivityHistoryResponse;
import com.example.da1androidnative.ui.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class ActivityHistoryAdapter extends RecyclerView.Adapter<ActivityHistoryAdapter.ViewHolder> {

    private List<ActivityHistoryResponse> activities = new ArrayList<>();
    private final OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(long reservationId);
    }

    public ActivityHistoryAdapter(OnActivityClickListener listener) {
        this.listener = listener;
    }

    public void setActivities(List<ActivityHistoryResponse> activities) {
        this.activities = activities != null ? activities : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(activities.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvDestination;
        private final TextView tvDate;
        private final TextView tvDuration;
        private final TextView tvGuide;
        private final TextView tvStatus;
        private final TextView tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvActivityName);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvGuide = itemView.findViewById(R.id.tvGuide);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRating = itemView.findViewById(R.id.tvRating);
        }

        public void bind(ActivityHistoryResponse activity, OnActivityClickListener listener) {
            tvName.setText(activity.getActivityName());
            tvDestination.setText(activity.getDestination());
            tvDate.setText("Fecha: " + DateUtils.formatDate(activity.getDate()));
            tvGuide.setText("Guía: " + activity.getGuideName());
            tvDuration.setText("Duración: " + formatDuration(activity.getDurationMinutes()));
            if (activity.isHasRating() && activity.getRating() != null) {
                tvRating.setText("Actividad: " + activity.getRating() + "/5 • Abrí para ver guía y comentario");
            } else {
                tvRating.setText("Sin calificación • Abrí para ver detalle");
            }

            setStatusView(activity.getStatus());

            itemView.setOnClickListener(v -> listener.onActivityClick(activity.getReservationId()));
        }

        private void setStatusView(String status) {
            if (status == null) {
                tvStatus.setVisibility(View.GONE);
                return;
            }
            tvStatus.setVisibility(View.VISIBLE);
            String displayStatus;
            int colorRes;

            switch (status.toUpperCase()) {
                case "CONFIRMED":
                    displayStatus = "Confirmado";
                    colorRes = android.R.color.holo_green_dark;
                    break;
                case "COMPLETED":
                    displayStatus = "Completado";
                    colorRes = android.R.color.holo_blue_dark;
                    break;
                case "CANCELLED":
                    displayStatus = "Cancelado";
                    colorRes = android.R.color.holo_red_dark;
                    break;
                case "PENDING":
                    displayStatus = "Pendiente";
                    colorRes = android.R.color.holo_orange_dark;
                    break;
                default:
                    displayStatus = status;
                    colorRes = android.R.color.darker_gray;
            }
            tvStatus.setText(displayStatus.toUpperCase());
            tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
        }

        private String formatDuration(Integer minutes) {
            if (minutes == null) return "N/A";
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (hours > 0) {
                return hours + " h " + remainingMinutes + " min";
            } else {
                return remainingMinutes + " min";
            }
        }
    }
}
