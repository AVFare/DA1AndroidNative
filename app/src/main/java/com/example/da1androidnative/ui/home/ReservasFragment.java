package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.PaginatedReservasResponse;
import com.example.da1androidnative.data.model.ReservaResponse;
import com.example.da1androidnative.data.network.ApiService;
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
    private ReservasAdapter reservasAdapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView(view);
        setupButtons(view);
        loadReservas();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReservas();
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.reservasRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reservasAdapter = new ReservasAdapter(getContext(), this);
        recyclerView.setAdapter(reservasAdapter);
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnFavoritos).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_reservas_to_favoritesFragment));
        
        view.findViewById(R.id.btnMisDatos).setOnClickListener(v ->
                Toast.makeText(getContext(), R.string.nav_perfil, Toast.LENGTH_SHORT).show());
    }

    private void loadReservas() {
        apiService.getAllReservas().enqueue(new Callback<PaginatedReservasResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedReservasResponse> call, @NonNull Response<PaginatedReservasResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReservaResponse> listaReservas = response.body().getContent();
                    reservasAdapter.setReservas(listaReservas);
                } else {
                    Toast.makeText(getContext(), "Error al cargar reservas: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedReservasResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onReservaClick(long reservationId) {
        if (reservationId == -1L) return;
        
        Bundle args = new Bundle();
        args.putLong("reservationId", reservationId);
        NavHostFragment.findNavController(this).navigate(R.id.action_reservas_to_reservaDetalleFragment, args);
    }
}
