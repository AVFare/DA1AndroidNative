package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
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
    private View errorStateNews;
    private TextView tvErrorTitle, tvErrorSubtitle;
    private Button btnRetryNews;
    private RecyclerView recyclerView;

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
        errorStateNews = view.findViewById(R.id.errorStateNews);
        tvErrorTitle = view.findViewById(R.id.tvErrorTitle);
        tvErrorSubtitle = view.findViewById(R.id.tvErrorSubtitle);
        btnRetryNews = view.findViewById(R.id.btnRetryNews);
        recyclerView = view.findViewById(R.id.newsRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NewsAdapter(requireContext(), this::openNewsDetail);
        recyclerView.setAdapter(adapter);

        btnRetryNews.setOnClickListener(v -> loadNews());

        loadNews();
    }

    private void loadNews() {
        showLoading(true);
        errorStateNews.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        apiService.getNews().enqueue(new Callback<List<NewsResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<NewsResponse>> call,
                                   @NonNull Response<List<NewsResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<NewsResponse> news = response.body();
                    updateUI(news, null);
                } else {
                    updateUI(null, "No se pudieron cargar las noticias.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NewsResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                updateUI(null, "Error de red: revisa tu conexión.");
            }
        });
    }

    private void updateUI(List<NewsResponse> news, String errorMessage) {
        if (errorMessage != null) {
            recyclerView.setVisibility(View.GONE);
            errorStateNews.setVisibility(View.VISIBLE);
            tvErrorTitle.setText("¡Ups! Algo salió mal");
            tvErrorSubtitle.setText(errorMessage);
        } else if (news == null || news.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            errorStateNews.setVisibility(View.VISIBLE);
            tvErrorTitle.setText("Sin noticias");
            tvErrorSubtitle.setText("Todavía no hay noticias disponibles.");
        } else {
            errorStateNews.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setNews(news);
        }
    }

    private void openNewsDetail(NewsResponse news) {
        Bundle args = new Bundle();
        args.putLong("newsId", news.getNewsId());
        args.putString("title", news.getTitle());
        args.putString("imageUrl", news.getImageUrl());
        args.putString("summary", news.getSummary());
        args.putString("date", news.getPublishedAt());

        Navigation.findNavController(requireView())
                .navigate(R.id.action_newsFragment_to_newsDetailFragment, args);
    }

    private void showLoading(boolean loading) {
        if (progressNews != null) {
            progressNews.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }
}
