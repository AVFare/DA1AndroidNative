package com.example.da1androidnative.ui.home;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.da1androidnative.ui.util.ToastHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ActivityHistoryResponse;
import com.example.da1androidnative.ui.home.adapter.ActivityHistoryAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivityHistoryFragment extends Fragment implements ActivityHistoryAdapter.OnActivityClickListener {

    private ActivityHistoryViewModel viewModel;
    private ActivityHistoryAdapter adapter;
    private EditText etStartDate, etEndDate;
    private AutoCompleteTextView spinnerStatus, spinnerDestination;
    private View progressBar, tvEmptyState, tvError;
    private Button btnPreviousPage, btnNextPage;
    private TextView tvPageNumber;

    private final String[] statusOptionsDisplay = {"Todos", "Cancelado", "Completo"};
    private final Map<String, String> statusMap = new HashMap<>();
    private final Map<String, Long> destinationIdByName = new HashMap<>();
    private List<ActivityHistoryResponse> allActivities = new ArrayList<>();

    public ActivityHistoryFragment() {
        statusMap.put("Cancelado", "CANCELLED");
        statusMap.put("Completo", "COMPLETED");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ActivityHistoryViewModel.class);

        setupToolbar(view);
        setupViews(view);
        setupRecyclerView(view);
        observeViewModel();

        // Carga inicial: Historial filtrado por completados y cancelados
        viewModel.loadHistory(null, null, null, "COMPLETED,CANCELLED");
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.historyToolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigateUp());
        }
    }

    private void setupViews(View view) {
        spinnerDestination = view.findViewById(R.id.spinnerDestination);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvError = view.findViewById(R.id.tvError);

        btnPreviousPage = view.findViewById(R.id.btnPreviousPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        tvPageNumber = view.findViewById(R.id.tvPageNumber);

        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptionsDisplay);
        spinnerStatus.setAdapter(adapterStatus);
        spinnerStatus.setText(statusOptionsDisplay[0], false);

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        view.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> applyFilters());

        view.findViewById(R.id.btnClearFilters).setOnClickListener(v -> {
            spinnerDestination.setText("Todos", false);
            etStartDate.setText("");
            etEndDate.setText("");
            spinnerStatus.setText(statusOptionsDisplay[0], false);
            viewModel.loadHistory(null, null, null, "COMPLETED,CANCELLED");
        });

        btnPreviousPage.setOnClickListener(v -> viewModel.previousPage());
        btnNextPage.setOnClickListener(v -> viewModel.nextPage());
    }

    private void applyFilters() {
        String start = etStartDate.getText().toString().trim();
        String end = etEndDate.getText().toString().trim();
        String selectedStatus = spinnerStatus.getText().toString();
        String statusValue = statusMap.get(selectedStatus);
        String selectedDestination = spinnerDestination.getText().toString();
        Long destinationId = destinationIdByName.get(selectedDestination);

        if ("Todos".equals(selectedStatus)) {
            statusValue = "COMPLETED,CANCELLED";
        }

        viewModel.loadHistory(
                start.isEmpty() ? null : start,
                end.isEmpty() ? null : end,
                "Todos".equals(selectedDestination) ? null : destinationId,
                statusValue
        );
    }

    private void updateDestinationSpinner(List<ActivityHistoryResponse> activities) {
        if (activities == null) return;

        Set<String> destinations = new HashSet<>();
        destinationIdByName.clear();
        for (ActivityHistoryResponse activity : activities) {
            if (activity.getDestination() != null) {
                destinations.add(activity.getDestination());
                if (activity.getDestinationId() != null) {
                    destinationIdByName.put(activity.getDestination(), activity.getDestinationId());
                }
            }
        }

        List<String> destinationList = new ArrayList<>(destinations);
        Collections.sort(destinationList);
        destinationList.add(0, "Todos");

        ArrayAdapter<String> destAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, destinationList);
        spinnerDestination.setAdapter(destAdapter);

        if (spinnerDestination.getText().toString().isEmpty()) {
            spinnerDestination.setText("Todos", false);
        }
    }

    private void filterByDestinationLocally(String destination) {
        if (destination.equals("Todos") || destination.isEmpty()) {
            adapter.setActivities(allActivities);
        } else {
            List<ActivityHistoryResponse> filtered = allActivities.stream()
                    .filter(a -> destination.equals(a.getDestination()))
                    .collect(Collectors.toList());
            adapter.setActivities(filtered);
        }
        tvEmptyState.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (v, year, month, dayOfMonth) -> {
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
            allActivities = list != null ? list : new ArrayList<>();
            updateDestinationSpinner(allActivities);
            filterByDestinationLocally(spinnerDestination.getText().toString());
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            tvError.setVisibility(error != null ? View.VISIBLE : View.GONE);
            if (error != null) {
                ToastHelper.show(getContext(), error);
            }
        });

        viewModel.currentPage.observe(getViewLifecycleOwner(), page -> {
            tvPageNumber.setText(String.format(Locale.getDefault(), "Página %d", page + 1));
            btnPreviousPage.setEnabled(page > 0);
        });

        viewModel.isLastPage.observe(getViewLifecycleOwner(), isLast -> {
            btnNextPage.setEnabled(!isLast);
        });

        spinnerDestination.setOnItemClickListener((parent, v, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            filterByDestinationLocally(selected);
        });
    }

    @Override
    public void onActivityClick(long reservationId) {
        Bundle args = new Bundle();
        args.putLong("reservationId", reservationId);
        NavHostFragment.findNavController(this).navigate(R.id.action_activityHistory_to_reservaDetalleFragment, args);
    }
}
