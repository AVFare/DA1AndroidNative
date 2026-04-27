package com.example.da1androidnative.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ActivityHistoryResponse;

import java.util.ArrayList;
import java.util.List;

public class ActivityHistoryAdapter extends RecyclerView.Adapter<ActivityHistoryAdapter.ViewHolder> {

    private List<ActivityHistoryResponse> activities = new ArrayList<>();
    private final OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(long activityId);
    }

    public ActivityHistoryAdapter(OnActivityClickListener listener) {
        this.listener = listener;
    }

    public void setActivities(List<ActivityHistoryResponse> activities) {
        this.activities = activities;
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvActivityName);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvGuide = itemView.findViewById(R.id.tvGuide);
        }

        public void bind(ActivityHistoryResponse activity, OnActivityClickListener listener) {
            tvName.setText(activity.getActivityName());
            tvDestination.setText(activity.getDestination());
            tvDate.setText("📅 " + activity.getDate());
            tvGuide.setText("👤 Guía: " + activity.getGuideName());
            tvDuration.setText("🕒 " + formatDuration(activity.getDurationMinutes()));

            //TODO: cambiar a ActivityID
            itemView.setOnClickListener(v -> listener.onActivityClick(activity.getReservationId()));
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
