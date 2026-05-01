package com.example.da1androidnative.ui.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.FavoritesManager;
import com.example.da1androidnative.data.model.ActivityResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private final Context context;
    private List<ActivityResponse> activities = new ArrayList<>();
    private final OnActivityClickListener listener;
    private final FavoritesManager favoritesManager;

    public interface OnActivityClickListener {
        void onActivityClick(Long activityId);
        void onFavoriteClick(ActivityResponse activity);
    }

    public ActivityAdapter(Context context, FavoritesManager favoritesManager, OnActivityClickListener listener) {
        this.context = context;
        this.favoritesManager = favoritesManager;
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
        holder.activityDestination.setText("Destino: " + activity.getDestination());
        holder.activityCategory.setText("Categoría: " + activity.getCategory());
        holder.activityDuration.setText("Duración: " + activity.getDurationMinutes() + " min");
        holder.activityPrice.setText("Precio: $" + activity.getBasePrice());
        holder.activitySpots.setText("Cupos: " + activity.getAvailableSpots());

        Glide.with(holder.itemView)
                .load(activity.getFirstImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.activityImage);

        // Favoritos
        boolean isFav = favoritesManager.isFavorite(activity.getId());
        holder.btnFavorite.setImageResource(
                isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
        );

        // CLICK CARD
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActivityClick(activity.getId());
            }
        });

        // CLICK FAVORITE
        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(activity);
                notifyItemChanged(position);
            }
        });

        // FEATURED (correcto, con reset explícito)
        if (activity.isFeatured()) {
            holder.badgeFeatured.setVisibility(View.VISIBLE);
            holder.badgeFeatured.setText("Destacada");
        } else {
            holder.badgeFeatured.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView activityImage;
        ImageButton btnFavorite;
        TextView activityTitle;
        TextView activityDestination;
        TextView activityCategory;
        TextView activityDuration;
        TextView activityPrice;
        TextView activitySpots;
        TextView badgeFeatured;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.activityCard);
            activityImage = itemView.findViewById(R.id.activityImage);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            activityTitle = itemView.findViewById(R.id.activityTitle);
            activityDestination = itemView.findViewById(R.id.activityDestination);
            activityCategory = itemView.findViewById(R.id.activityCategory);
            activityDuration = itemView.findViewById(R.id.activityDuration);
            activityPrice = itemView.findViewById(R.id.activityPrice);
            activitySpots = itemView.findViewById(R.id.activitySpots);

            badgeFeatured = itemView.findViewById(R.id.badgeFeatured);
        }
    }
}