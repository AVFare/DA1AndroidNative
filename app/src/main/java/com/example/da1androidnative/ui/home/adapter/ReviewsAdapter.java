package com.example.da1androidnative.ui.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ReviewResponse;

import java.util.ArrayList;
import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private final Context context;
    private List<ReviewResponse> reviews = new ArrayList<>();

    public ReviewsAdapter(Context context) {
        this.context = context;
    }

    public void setReviews(List<ReviewResponse> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //TODO: Resolver los PlaceHolders
        ReviewResponse review = reviews.get(position);
        //holder.activityNameText.setText(review.getActivityName());
        holder.activityNameText.setText("Aca iria el nombre de la actividad");
        //holder.destinationText.setText("Destino: " + review.getDestination());
        holder.destinationText.setText("Aca iria el destino");
        //holder.guideText.setText("Guia: " + review.getGuideName());
        holder.guideText.setText("Aca iria el nombre de la guia");

        holder.dateText.setText("Fecha: " + review.getCreatedAt());
        holder.activityRatingText.setText(String.format("Actividad: %d/5", review.getActivityStars()));
        holder.guideRatingText.setText(String.format("Guia: %d/5", review.getGuideStars()));

        String comment = review.getComment();
        if (comment == null || comment.trim().isEmpty()) {
            holder.commentText.setText("Sin comentario");
        } else {
            holder.commentText.setText(comment);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityNameText;
        TextView destinationText;
        TextView guideText;
        TextView dateText;
        TextView activityRatingText;
        TextView guideRatingText;
        TextView commentText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            activityNameText = itemView.findViewById(R.id.reviewActivityNameText);
            destinationText = itemView.findViewById(R.id.reviewDestinationText);
            guideText = itemView.findViewById(R.id.reviewGuideText);
            dateText = itemView.findViewById(R.id.reviewDateText);
            activityRatingText = itemView.findViewById(R.id.reviewActivityRatingText);
            guideRatingText = itemView.findViewById(R.id.reviewGuideRatingText);
            commentText = itemView.findViewById(R.id.reviewCommentText);
        }
    }
}
