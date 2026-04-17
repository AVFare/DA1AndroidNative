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
import com.example.da1androidnative.data.model.ActivityResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.home.adapter.ActivityAdapter;

import java.util.List;
import com.example.da1androidnative.data.model.PaginatedActivitiesResponse;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements ActivityAdapter.OnActivityClickListener {

    @Inject ApiService apiService;
    private ActivityAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView(view);
        setupButtons(view);
        loadActivities();
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.activitiesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActivityAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnReservas).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Mis Reservas", Toast.LENGTH_SHORT).show());
        
        view.findViewById(R.id.btnCalificaciones).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Mis Calificaciones", Toast.LENGTH_SHORT).show());
            
        view.findViewById(R.id.btnMisDatos).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Mis Datos", Toast.LENGTH_SHORT).show());
    }

    private void loadActivities() {
        apiService.getAllActivities().enqueue(new Callback<PaginatedActivitiesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedActivitiesResponse> call, @NonNull Response<PaginatedActivitiesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ActivityResponse> lista = response.body().getContent();
                    android.util.Log.d("API_HOME", "Actividades extraídas del paginado: " + lista.size());
                    adapter.setActivities(lista);
                } else {
                    android.util.Log.e("API_HOME", "Error en respuesta: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar actividades", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedActivitiesResponse> call, @NonNull Throwable t) {
                android.util.Log.e("API_HOME", "Fallo total: " + t.getMessage());
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityClick(Long activityId) {
        Toast.makeText(getContext(), "ID Actividad: " + activityId, Toast.LENGTH_SHORT).show();
        
        // TODO: NavHostFragment.findNavController(this).navigate(R.id.action_home_to_detail, bundle);
    }
}