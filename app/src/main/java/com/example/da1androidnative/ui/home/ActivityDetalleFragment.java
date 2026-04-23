package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ActivityDetalleResponse;
import com.example.da1androidnative.data.model.PaginatedReservasResponse;
import com.example.da1androidnative.data.model.PaginatedSchedulesResponse;
import com.example.da1androidnative.data.model.ReservaDetalleResponse;
import com.example.da1androidnative.data.model.ReservaResponse;
import com.example.da1androidnative.data.model.ScheduleResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.home.adapter.ReservasAdapter;
import com.example.da1androidnative.ui.home.adapter.ScheduleActivityAdapter;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ActivityDetalleFragment extends Fragment implements ScheduleActivityAdapter.OnScheduleClickListener {

    @Inject
    ApiService apiService;

    private ActivityDetalleResponse currentDetalle;

    private ScheduleActivityAdapter scheduleActivityAdapter;
    private RecyclerView recyclerView;

    private Toolbar toolbar;

    private long activityId;
    private ImageView activityImageView;
    private TextView activityNameText;
    private TextView categoryBadgeText;
    private TextView destinationText;
    private TextView durationText;
    private TextView priceText;
    private TextView fullDescriptionText;
    private TextView meetingPointText;
    private TextView guideNameText;
    private TextView languageText;
    private TextView inclusionsText;
    private TextView cancellationPolicyText;
    private Button btnReservar;


    public ActivityDetalleResponse getCurrentDetalle() {
        return currentDetalle;
    }

    public void setCurrentDetalle(ActivityDetalleResponse currentDetalle) {
        this.currentDetalle = currentDetalle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_detalle, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            this.activityId = args.getLong("activityId", -1L);
        }

        activityImageView = view.findViewById(R.id.activityImageView);
        activityNameText = view.findViewById(R.id.activityNameText);;
        categoryBadgeText = view.findViewById(R.id.categoryBadgeText);;
        destinationText = view.findViewById(R.id.destinationText);;
        durationText = view.findViewById(R.id.durationText);;
        priceText = view.findViewById(R.id.priceText);
        fullDescriptionText = view.findViewById(R.id.fullDescriptionText);;
        meetingPointText = view.findViewById(R.id.meetingPointText);;
        guideNameText = view.findViewById(R.id.guideNameText);;
        languageText = view.findViewById(R.id.languageText);;
        inclusionsText = view.findViewById(R.id.inclusionsText);;
        cancellationPolicyText = view.findViewById(R.id.cancellationPolicyText);;
        toolbar = view.findViewById(R.id.toolbar);
        btnReservar = view.findViewById(R.id.btnReservar);;
        btnReservar.setEnabled(false);

        setupRecyclerView(view);
        loadDetalleActividad();
        loadHorarios();

        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.scheduleRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleActivityAdapter = new ScheduleActivityAdapter(getContext(), this);
        recyclerView.setAdapter(scheduleActivityAdapter);
    }

    private void loadDetalleActividad() {
        if (this.activityId == -1L) {
            Toast.makeText(getContext(), "Actividad inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getDetalleActivity(activityId).enqueue(new Callback<ActivityDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityDetalleResponse> call, @NonNull Response<ActivityDetalleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ActivityDetalleResponse detalle = response.body();
                    android.util.Log.d("API_HOME", "Detalle de Actividad Recibido");
                    bindDetalle(detalle);
                    setCurrentDetalle(detalle);
                } else {
                    android.util.Log.e("API_HOME", "Error en respuesta: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar detalle de actividad", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivityDetalleResponse> call, @NonNull Throwable t) {
                android.util.Log.e("API_HOME", "Fallo total: " + t.getMessage());
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadHorarios() {
        if (this.activityId == -1L) {
            Toast.makeText(getContext(), "Actividad inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getHorariosActivity(activityId).enqueue(new Callback<PaginatedSchedulesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedSchedulesResponse> call, @NonNull Response<PaginatedSchedulesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ScheduleResponse> horarios = response.body().getContent();
                    android.util.Log.d("API_HOME", "Horarios de Actividad Recibido");
                    scheduleActivityAdapter.setSchedules(horarios);
                    btnReservar.setEnabled(true);
                } else {
                    android.util.Log.e("API_HOME", "Error en respuesta: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar horarios de actividad", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedSchedulesResponse> call, @NonNull Throwable t) {
                android.util.Log.e("API_HOME", "Fallo total: " + t.getMessage());
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindDetalle(ActivityDetalleResponse detalle) {
        activityNameText.setText(detalle.getName());
        categoryBadgeText.setText(detalle.getCategory());
        destinationText.setText(detalle.getDestination());
        durationText.setText(String.format("Duracion en Minutos: %d", detalle.getDurationMinutes()));
        priceText.setText(String.format("Precio Base: %f", detalle.getBasePrice()));
        fullDescriptionText.setText(detalle.getFullDescription());
        meetingPointText.setText(detalle.getMeetingPoint());
        guideNameText.setText(detalle.getGuideName());
        languageText.setText(detalle.getLanguage());
        inclusionsText.setText(detalle.getInclusions());
        cancellationPolicyText.setText(detalle.getCancellationPolicy());

        String imageUrl = null;
        if (detalle.getImages() != null && !detalle.getImages().isEmpty()) {
            imageUrl = detalle.getFirstImageUrl();
        }

        Glide.with(this).load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(activityImageView);
    }


    @Override
    public void onScheduleClick(long scheduleId, String date, String time, int availableSpots) {
        Bundle args = new Bundle();
        args.putLong("activityId", activityId);
        args.putLong("scheduleId", scheduleId);
        args.putString("date", date);
        args.putString("time", time);
        args.putInt("availableSpots", availableSpots);
        args.putDouble("precioBase", getCurrentDetalle().getBasePrice());
        NavHostFragment.findNavController(this).navigate(R.id.action_activityDetalle_to_createReservationFragment, args);
    }
}

