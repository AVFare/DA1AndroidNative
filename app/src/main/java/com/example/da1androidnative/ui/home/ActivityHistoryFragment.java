package com.example.da1androidnative.ui.home;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.ui.home.adapter.ActivityHistoryAdapter;

import java.util.Calendar;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivityHistoryFragment extends Fragment implements ActivityHistoryAdapter.OnActivityClickListener {

    private ActivityHistoryViewModel viewModel;
    private ActivityHistoryAdapter adapter;
    private EditText etDestination, etStartDate, etEndDate;
    private View progressBar, tvEmptyState, tvError;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ActivityHistoryViewModel.class);
        
        setupViews(view);
        setupRecyclerView(view);
        observeViewModel();

        // Carga inicial
        viewModel.loadHistory(null, null, null);
    }

    private void setupViews(View view) {
        etDestination = view.findViewById(R.id.etDestinationFilter);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvError = view.findViewById(R.id.tvError);

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        view.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> {
            String destination = etDestination.getText().toString().trim();
            String start = etStartDate.getText().toString().trim();
            String end = etEndDate.getText().toString().trim();
            Long destinationId = parseDestinationId(destination);
            if (!destination.isEmpty() && destinationId == null) {
                return;
            }
            
            viewModel.loadHistory(
                start.isEmpty() ? null : start,
                end.isEmpty() ? null : end,
                destinationId
            );
        });
    }

    private Long parseDestinationId(String destination) {
        if (destination.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(destination);
        } catch (NumberFormatException exception) {
            Toast.makeText(getContext(), "Ingresá un ID de destino válido", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            editText.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rvHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActivityHistoryAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.activities.observe(getViewLifecycleOwner(), list -> {
            adapter.setActivities(list);
            tvEmptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                tvError.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            } else {
                tvError.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityClick(long reservationId) {
        Bundle args = new Bundle();
        args.putLong("reservationId", reservationId);
        NavHostFragment.findNavController(this).navigate(R.id.action_activityHistory_to_activityHistoryDetailFragment, args);
    }
}
