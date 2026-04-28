package com.example.da1androidnative.ui.home;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.OfflineReservationStorage;
import com.example.da1androidnative.data.model.PaginatedReservasResponse;
import com.example.da1androidnative.data.model.ReservaCancelledResponse;
import com.example.da1androidnative.data.model.ReservaResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.data.network.NetworkUtils;
import com.example.da1androidnative.ui.home.adapter.ReservasAdapter;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ReservasFragment extends Fragment implements ReservasAdapter.OnReservaClickListener {

    @Inject ApiService apiService;
    @Inject OfflineReservationStorage offlineStorage;
    private ReservasAdapter reservasAdapter;
    private RecyclerView recyclerView;
    private TextView tvOfflineBanner;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvOfflineBanner = view.findViewById(R.id.tvOfflineBanner);
        setupRecyclerView(view);
        setupButtons(view);
        registerNetworkCallback();
        loadReservas();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReservas();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterNetworkCallback();
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.reservasRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reservasAdapter = new ReservasAdapter(getContext(), this);
        recyclerView.setAdapter(reservasAdapter);
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnCalificaciones).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_reservas_to_reviewsFragment));
        view.findViewById(R.id.btnFavoritos).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_reservas_to_favoritesFragment));

        view.findViewById(R.id.btnMisDatos).setOnClickListener(v ->
                Toast.makeText(getContext(), R.string.nav_perfil, Toast.LENGTH_SHORT).show());
    }

    private void registerNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Sincronización automática al recuperar conexión
                            syncPendingCancellations();
                            loadReservas();
                        });
                    }
                }

                @Override
                public void onLost(@NonNull Network network) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showOfflineMode(true);
                        });
                    }
                }
            };
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            }
        }
    }

    private void syncPendingCancellations() {
        List<Long> pending = offlineStorage.getPendingCancellations();
        if (pending.isEmpty()) return;

        for (Long reservationId : pending) {
            apiService.cancelReserva(reservationId).enqueue(new Callback<ReservaCancelledResponse>() {
                @Override
                public void onResponse(@NonNull Call<ReservaCancelledResponse> call, @NonNull Response<ReservaCancelledResponse> response) {
                    if (response.isSuccessful()) {
                        offlineStorage.removePendingCancellation(reservationId);
                        offlineStorage.clearReservation(reservationId);
                        loadReservas(); // Recargar lista para ver cambios
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ReservaCancelledResponse> call, @NonNull Throwable t) {
                    // Reintentar en la próxima conexión
                }
            });
        }
    }

    private void loadReservas() {
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            // Modo Offline: Cargar desde SharedPreferences
            showOfflineMode(true);
            List<ReservaResponse> savedReservas = offlineStorage.getSavedReservations();
            reservasAdapter.setReservas(savedReservas);
        } else {
            // Modo Online: Cargar desde el servidor
            showOfflineMode(false);
            apiService.getAllReservas().enqueue(new Callback<PaginatedReservasResponse>() {
                @Override
                public void onResponse(@NonNull Call<PaginatedReservasResponse> call, @NonNull Response<PaginatedReservasResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ReservaResponse> listaReservas = response.body().getContent();
                        reservasAdapter.setReservas(listaReservas);
                        // Sincronizar cache local con los datos actuales del servidor
                        offlineStorage.updateReservations(listaReservas);
                    } else {
                        Toast.makeText(getContext(), "Error al cargar reservas: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PaginatedReservasResponse> call, @NonNull Throwable t) {
                    // Fallback a offline si hay fallo de red inesperado
                    List<ReservaResponse> savedReservas = offlineStorage.getSavedReservations();
                    reservasAdapter.setReservas(savedReservas);
                    showOfflineMode(true);
                }
            });
        }
    }

    private void showOfflineMode(boolean isOffline) {
        if (tvOfflineBanner != null) {
            tvOfflineBanner.setVisibility(isOffline ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onReservaClick(long reservationId) {
        if (reservationId == -1L) return;

        Bundle args = new Bundle();
        args.putLong("reservationId", reservationId);
        NavHostFragment.findNavController(this).navigate(R.id.action_reservas_to_reservaDetalleFragment, args);
    }
}
