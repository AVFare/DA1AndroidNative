package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.NewsResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.home.adapter.NewsAdapter;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class NewsFragment extends Fragment {

    @Inject
    ApiService apiService;

    private NewsAdapter adapter;
    private ProgressBar progressNews;
    private TextView tvEmptyNews;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.newsToolbar);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        progressNews = view.findViewById(R.id.progressNews);
        tvEmptyNews = view.findViewById(R.id.tvEmptyNews);

        RecyclerView recyclerView = view.findViewById(R.id.newsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new NewsAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        loadNews();
    }

    private void loadNews() {
        showLoading(true);

        apiService.getNews().enqueue(new Callback<List<NewsResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<NewsResponse>> call,
                                   @NonNull Response<List<NewsResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<NewsResponse> news = response.body();
                    adapter.setNews(news);
                    tvEmptyNews.setVisibility(news.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvEmptyNews.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NewsResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                tvEmptyNews.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoading(boolean loading) {
        if (progressNews != null) {
            progressNews.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (tvEmptyNews != null && loading) {
            tvEmptyNews.setVisibility(View.GONE);
        }
    }
}
