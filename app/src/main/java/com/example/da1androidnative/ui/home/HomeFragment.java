package com.example.da1androidnative.ui.home;

import android.app.DatePickerDialog;
import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.FavoritesManager;
import com.example.da1androidnative.data.local.TokenManager;
import com.example.da1androidnative.data.model.ActivityFilterOptionsResponse;
import com.example.da1androidnative.data.model.ActivityResponse;
import com.example.da1androidnative.data.model.DestinationOptionResponse;
import com.example.da1androidnative.data.model.PaginatedActivitiesResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.data.network.NetworkUtils;
import com.example.da1androidnative.ui.home.adapter.ActivityAdapter;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements ActivityAdapter.OnActivityClickListener {

    @Inject ApiService apiService;
    @Inject TokenManager tokenManager;
    @Inject FavoritesManager favoritesManager;

    private ActivityAdapter adapter;
    private TextView tvOfflineBanner;
    private TextView tvPageNumber;
    private Button btnPreviousPage;
    private Button btnNextPage;
    private Button btnApplyFilters;
    private Button btnClearFilters;
    private AutoCompleteTextView destinationFilter;
    private AutoCompleteTextView categoryFilter;
    private EditText dateFilter;
    private EditText minPriceFilter;
    private EditText maxPriceFilter;
    private SwitchMaterial biometricSwitch;
    private ConnectivityManager.NetworkCallback networkCallback;
    private int currentPage = 0;
    private boolean isLastPage = false;
    private static final int PAGE_SIZE = 6;
    private static final String ALL_FILTERS_LABEL = "Todos";
    private final Map<String, Long> destinationIdByName = new HashMap<>();
    private final Map<String, String> categoryValueByLabel = new HashMap<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableBiometricIfPossible();
                } else {
                    Toast.makeText(getContext(), R.string.biometric_permission_denied, Toast.LENGTH_SHORT).show();
                    biometricSwitch.setChecked(false);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvOfflineBanner = view.findViewById(R.id.tvOfflineBanner);
        biometricSwitch = view.findViewById(R.id.biometricSwitch);

        setupRecyclerView(view);
        setupPagination(view);
        setupFilters(view);
        setupBiometricSwitch();
        registerNetworkCallback();
        loadFilterOptions();
        loadActivities();
        setupNavigationButtons(view);
    }

    private void setupNavigationButtons(View view) {
        view.findViewById(R.id.btnReservas).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_reservas));
        view.findViewById(R.id.btnFavoritos).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_favorites));
        view.findViewById(R.id.btnHistorial).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_activityHistory));
        view.findViewById(R.id.btnCalificaciones).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_reviewsFragment));
        view.findViewById(R.id.btnMisDatos).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_profileFragment));
        view.findViewById(R.id.btnNoticias).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_news));
    }

    private void setupBiometricSwitch() {
        if (biometricSwitch == null) return;
        biometricSwitch.setChecked(tokenManager.isBiometricEnabled());
        biometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (tokenManager.getSavedPassword() == null) {
                    Toast.makeText(getContext(), "Inicia sesión con contraseña una vez para habilitar biometría", Toast.LENGTH_LONG).show();
                    biometricSwitch.setChecked(false);
                    return;
                }
                String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ? Manifest.permission.USE_BIOMETRIC : Manifest.permission.USE_FINGERPRINT;
                requestPermissionLauncher.launch(permission);
            } else {
                tokenManager.setBiometricEnabled(false);
            }
        });
    }

    private void enableBiometricIfPossible() {
        BiometricManager manager = BiometricManager.from(requireContext());
        if (manager.canAuthenticate(Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
            tokenManager.setBiometricEnabled(true);
            Toast.makeText(getContext(), R.string.biometric_enabled_msg, Toast.LENGTH_SHORT).show();
        } else {
            biometricSwitch.setChecked(false);
            Toast.makeText(getContext(), R.string.biometric_error_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.activitiesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActivityAdapter(getContext(), favoritesManager, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupPagination(View view) {
        btnPreviousPage = view.findViewById(R.id.btnPreviousPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        tvPageNumber = view.findViewById(R.id.tvPageNumber);

        btnPreviousPage.setOnClickListener(v -> previousPage());
        btnNextPage.setOnClickListener(v -> nextPage());
        updatePaginationControls();
    }

    private void setupFilters(View view) {
        destinationFilter = view.findViewById(R.id.homeDestinationFilter);
        categoryFilter = view.findViewById(R.id.homeCategoryFilter);
        dateFilter = view.findViewById(R.id.homeDateFilter);
        minPriceFilter = view.findViewById(R.id.homeMinPriceFilter);
        maxPriceFilter = view.findViewById(R.id.homeMaxPriceFilter);
        btnApplyFilters = view.findViewById(R.id.homeApplyFiltersButton);
        btnClearFilters = view.findViewById(R.id.homeClearFiltersButton);

        setupDestinationOptions(Collections.emptyList());
        setupCategoryOptions(Arrays.asList("AVENTURA", "CULTURA", "GASTRONOMIA", "NATURALEZA", "RELAX"));

        dateFilter.setOnClickListener(v -> showDatePicker());
        btnApplyFilters.setOnClickListener(v -> {
            if (!validatePriceRange()) return;
            currentPage = 0;
            loadActivities();
        });
        btnClearFilters.setOnClickListener(v -> {
            destinationFilter.setText(ALL_FILTERS_LABEL, false);
            categoryFilter.setText(ALL_FILTERS_LABEL, false);
            dateFilter.setText("");
            minPriceFilter.setText("");
            maxPriceFilter.setText("");
            currentPage = 0;
            loadActivities();
        });
    }

    private void loadFilterOptions() {
        if (!NetworkUtils.isNetworkAvailable(getContext())) return;

        apiService.getActivityFilterOptions().enqueue(new Callback<ActivityFilterOptionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityFilterOptionsResponse> call,
                                   @NonNull Response<ActivityFilterOptionsResponse> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                setupDestinationOptions(response.body().getDestinations());
                setupCategoryOptions(response.body().getCategories());
            }

            @Override
            public void onFailure(@NonNull Call<ActivityFilterOptionsResponse> call,
                                  @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Fallo al cargar filtros", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupDestinationOptions(List<DestinationOptionResponse> destinations) {
        destinationIdByName.clear();
        List<String> labels = new ArrayList<>();
        labels.add(ALL_FILTERS_LABEL);

        if (destinations != null) {
            for (DestinationOptionResponse destination : destinations) {
                if (destination.getName() == null) continue;
                labels.add(destination.getName());
                destinationIdByName.put(destination.getName(), destination.getDestinationId());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, labels);
        destinationFilter.setAdapter(adapter);
        if (destinationFilter.getText().toString().isEmpty()) {
            destinationFilter.setText(ALL_FILTERS_LABEL, false);
        }
    }

    private void setupCategoryOptions(List<String> categories) {
        categoryValueByLabel.clear();
        List<String> labels = new ArrayList<>();
        labels.add(ALL_FILTERS_LABEL);

        if (categories != null) {
            for (String category : categories) {
                if (category == null) continue;
                String label = formatCategory(category);
                labels.add(label);
                categoryValueByLabel.put(label, category);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, labels);
        categoryFilter.setAdapter(adapter);
        if (categoryFilter.getText().toString().isEmpty()) {
            categoryFilter.setText(ALL_FILTERS_LABEL, false);
        }
    }

    private String formatCategory(String category) {
        String lower = category.toLowerCase(Locale.ROOT).replace('_', ' ');
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (v, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            dateFilter.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean validatePriceRange() {
        Double minPrice = parsePrice(minPriceFilter.getText().toString());
        Double maxPrice = parsePrice(maxPriceFilter.getText().toString());
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            Toast.makeText(getContext(), "El precio mínimo no puede superar al máximo", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Double parsePrice(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long getSelectedDestinationId() {
        if (destinationFilter == null) return null;
        String selected = destinationFilter.getText().toString();
        if (selected.isEmpty() || ALL_FILTERS_LABEL.equals(selected)) return null;
        return destinationIdByName.get(selected);
    }

    private String getSelectedCategory() {
        if (categoryFilter == null) return null;
        String selected = categoryFilter.getText().toString();
        if (selected.isEmpty() || ALL_FILTERS_LABEL.equals(selected)) return null;
        return categoryValueByLabel.get(selected);
    }

    private String getTextFilter(EditText editText) {
        if (editText == null) return null;
        String value = editText.getText().toString().trim();
        return value.isEmpty() ? null : value;
    }

    private void registerNetworkCallback() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> { updateOfflineBanner(); loadActivities(); });
                }
                @Override
                public void onLost(@NonNull Network network) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> updateOfflineBanner());
                }
            };
            cm.registerDefaultNetworkCallback(networkCallback);
        }
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null) {
            ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) cm.unregisterNetworkCallback(networkCallback);
        }
    }

    private void updateOfflineBanner() {
        if (tvOfflineBanner != null) {
            tvOfflineBanner.setVisibility(NetworkUtils.isNetworkAvailable(getContext()) ? View.GONE : View.VISIBLE);
        }
    }

    private void loadActivities() {
        if (!NetworkUtils.isNetworkAvailable(getContext())) return;

        long userId = tokenManager.getUserId();

        apiService.getAllActivities(
                userId != -1 ? userId : null,
                getSelectedDestinationId(),
                getSelectedCategory(),
                getTextFilter(dateFilter),
                getTextFilter(minPriceFilter),
                getTextFilter(maxPriceFilter),
                currentPage,
                PAGE_SIZE
        ).enqueue(new Callback<PaginatedActivitiesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedActivitiesResponse> call,
                                   @NonNull Response<PaginatedActivitiesResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    PaginatedActivitiesResponse body = response.body();
                    List<ActivityResponse> content = body.getContent();
                    adapter.setActivities(content != null ? content : new ArrayList<>());
                    isLastPage = isLastPage(body, content);
                    updatePaginationControls();

                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedActivitiesResponse> call,
                                  @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Fallo al cargar actividades", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void nextPage() {
        if (!isLastPage) {
            currentPage++;
            updatePaginationControls();
            loadActivities();
        }
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePaginationControls();
            loadActivities();
        }
    }

    private void updatePaginationControls() {
        if (tvPageNumber != null) {
            tvPageNumber.setText(String.format(Locale.getDefault(), "P\u00e1gina %d", currentPage + 1));
        }
        if (btnPreviousPage != null) {
            btnPreviousPage.setEnabled(currentPage > 0);
        }
        if (btnNextPage != null) {
            btnNextPage.setEnabled(!isLastPage);
        }
    }

    private boolean isLastPage(PaginatedActivitiesResponse body, List<ActivityResponse> content) {
        if (body.getLast() != null) {
            return body.getLast();
        }

        Integer totalPages = body.getTotalPages();
        if (totalPages != null) {
            return currentPage >= totalPages - 1;
        }

        return content == null || content.size() < PAGE_SIZE;
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); unregisterNetworkCallback(); }

    @Override
    public void onActivityClick(Long activityId) {
        if (activityId == null) return;
        Bundle args = new Bundle();
        args.putLong("activityId", activityId);
        NavHostFragment.findNavController(this).navigate(R.id.action_home_to_activityDetalleFragment, args);
    }

    @Override
    public void onFavoriteClick(ActivityResponse activity) {
        favoritesManager.toggleFavorite(activity.getId());
        Toast.makeText(getContext(), favoritesManager.isFavorite(activity.getId()) ? "Agregado a favoritos" : "Quitado de favoritos", Toast.LENGTH_SHORT).show();
    }
}
