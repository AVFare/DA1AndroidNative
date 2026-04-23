package com.example.da1androidnative.ui.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ActivityResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private final Context context;
    private List<ActivityResponse> activities = new ArrayList<>();
    private OnActivityClickListener listener; 

    public interface OnActivityClickListener {
        void onActivityClick(Long activityId);
    }

    public ActivityAdapter(Context context, OnActivityClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setActivities(List<ActivityResponse> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityResponse activity = activities.get(position);

        holder.activityTitle.setText(activity.getName());
        holder.activityDestination.setText(String.format("Destino: %s", activity.getName()));
        holder.activityCategory.setText(String.format("Categoria: %s", activity.getCategory()));
        holder.activityDuration.setText(String.format("Duracion en Minutos: %d", activity.getDurationMinutes()));
        holder.activityPrice.setText(String.format("Precio Base: %f", activity.getBasePrice()));
        holder.activitySpots.setText(String.format("Lugares Disponibles: %d", activity.getAvailableSpots()));

        Glide.with(context)
                .load(activity.getFirstImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.activityImage);

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActivityClick(activity.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView activityImage;
        TextView activityTitle;
        TextView activityDestination;
        TextView activityCategory;
        TextView activityDuration;
        TextView activityPrice;
        TextView activitySpots;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.activityCard);
            activityImage = itemView.findViewById(R.id.activityImage);
            activityTitle = itemView.findViewById(R.id.activityTitle);
            activityDestination = itemView.findViewById(R.id.activityDestination);
            activityCategory = itemView.findViewById(R.id.activityCategory);
            activityDuration = itemView.findViewById(R.id.activityDuration);
            activityPrice = itemView.findViewById(R.id.activityPrice);
            activitySpots = itemView.findViewById(R.id.activitySpots);
        }
    }
}