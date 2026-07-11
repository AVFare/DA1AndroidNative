package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.NewsDetailResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.util.DateUtils;
import com.example.da1androidnative.ui.util.ToastHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class NewsDetailFragment extends Fragment {

    @Inject
    ApiService apiService;

    private long newsId;
    private ImageView ivDetailImage;
    private Chip tvDetailDate;
    private TextView tvDetailTitle;
    private TextView tvDetailSummary;
    private TextView tvDetailContent;
    private Toolbar toolbar;
    private LinearProgressIndicator progressIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        Bundle args = getArguments();
        if (args != null) {
            newsId = args.getLong("newsId", -1L);
            tvDetailTitle.setText(args.getString("title", ""));
            tvDetailSummary.setText(args.getString("summary", ""));
            tvDetailDate.setText(DateUtils.formatDate(args.getString("date", "")));

            String imageUrl = args.getString("imageUrl", "");
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(this).load(imageUrl).into(ivDetailImage);
            }
        }

        loadNewsDetail();
    }

    private void initViews(View view) {
        progressIndicator = view.findViewById(R.id.progressIndicator);
        ivDetailImage = view.findViewById(R.id.ivDetailImage);
        tvDetailDate = view.findViewById(R.id.tvDetailDate);
        tvDetailTitle = view.findViewById(R.id.tvDetailTitle);
        tvDetailSummary = view.findViewById(R.id.tvDetailSummary);
        tvDetailContent = view.findViewById(R.id.tvDetailContent);
        toolbar = view.findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        }
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadNewsDetail() {
        if (newsId == -1L) return;
        setLoading(true);
        apiService.getNewsDetail(newsId).enqueue(new Callback<NewsDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsDetailResponse> call, @NonNull Response<NewsDetailResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    bindFullDetail(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsDetailResponse> call, @NonNull Throwable t) {
                setLoading(false);
                if (isAdded()) {
                    ToastHelper.show(getContext(), "Error al conectar con el servidor");
                }
            }
        });
    }

    private void bindFullDetail(NewsDetailResponse news) {
        tvDetailTitle.setText(news.getTitle());
        tvDetailDate.setText(DateUtils.formatDate(news.getPublishedAt()));
        tvDetailSummary.setText(news.getShortDescription());

        String fullContent = news.getFullDescription();
        if (!TextUtils.isEmpty(fullContent)) {
            tvDetailContent.setText(fullContent);
        } else {
            tvDetailContent.setText(news.getShortDescription());
        }

        if (!TextUtils.isEmpty(news.getImageUrl())) {
            Glide.with(this).load(news.getImageUrl()).into(ivDetailImage);
        }
    }

}
