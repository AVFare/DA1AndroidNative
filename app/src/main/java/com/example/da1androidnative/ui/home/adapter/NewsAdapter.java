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
import com.example.da1androidnative.data.model.NewsResponse;
import com.example.da1androidnative.ui.util.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    public interface OnNewsClickListener {
        void onNewsClick(NewsResponse news);
    }

    private final Context context;
    private final OnNewsClickListener listener;
    private List<NewsResponse> newsList = new ArrayList<>();

    public NewsAdapter(Context context, OnNewsClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setNews(List<NewsResponse> newsList) {
        this.newsList = newsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsResponse news = newsList.get(position);

        holder.newsTitle.setText(news.getTitle());
        holder.newsSummary.setText(news.getSummary());
        holder.newsDate.setText(DateUtils.formatDate(news.getPublishedAt()));

        Glide.with(holder.itemView)
                .load(news.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.newsImage);

        holder.btnReadMore.setOnClickListener(v -> listener.onNewsClick(news));
        
        holder.newsCard.setOnClickListener(null);
        holder.newsCard.setClickable(false);
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView newsCard;
        ImageView newsImage;
        TextView newsTitle, newsSummary, newsDate;
        MaterialButton btnReadMore;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsCard = itemView.findViewById(R.id.newsCard);
            newsImage = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsSummary = itemView.findViewById(R.id.newsSummary);
            newsDate = itemView.findViewById(R.id.newsDate);
            btnReadMore = itemView.findViewById(R.id.btnReadMore);
        }
    }
}
