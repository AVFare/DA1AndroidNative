package com.example.da1androidnative.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import com.example.da1androidnative.data.model.ItineraryResponse;
import com.example.da1androidnative.data.model.PaginatedSchedulesResponse;
import com.example.da1androidnative.data.model.ScheduleResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.home.adapter.ScheduleActivityAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ActivityDetalleFragment extends Fragment implements ScheduleActivityAdapter.OnScheduleClickListener, OnMapReadyCallback {

    @Inject ApiService apiService;

    private ActivityDetalleResponse currentDetalle;
    private ScheduleActivityAdapter scheduleActivityAdapter;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private long activityId;
    private ImageView activityImageView;
    private TextView activityNameText, priceText, fullDescriptionText, meetingPointText, guideNameText, languageText;
    private Button btnReservar, btnHowToGet;
    
    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            this.activityId = args.getLong("activityId", -1L);
        }

        initViews(view);
        setupRecyclerView(view);
        
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.activityMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadDetalleActividad();
        loadHorarios();

        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
    }

    private void initViews(View view) {
        activityImageView = view.findViewById(R.id.activityImageView);
        activityNameText = view.findViewById(R.id.activityNameText);
        priceText = view.findViewById(R.id.priceText);
        fullDescriptionText = view.findViewById(R.id.fullDescriptionText);
        meetingPointText = view.findViewById(R.id.meetingPointText);
        guideNameText = view.findViewById(R.id.guideNameText);
        languageText = view.findViewById(R.id.languageText);
        toolbar = view.findViewById(R.id.toolbar);
        btnReservar = view.findViewById(R.id.btnReservar);
        btnHowToGet = view.findViewById(R.id.btnHowToGetActivity);
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.scheduleRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleActivityAdapter = new ScheduleActivityAdapter(getContext(), this);
        recyclerView.setAdapter(scheduleActivityAdapter);
    }

    private void loadDetalleActividad() {
        if (this.activityId == -1L) return;

        apiService.getDetalleActivity(activityId).enqueue(new Callback<ActivityDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityDetalleResponse> call, @NonNull Response<ActivityDetalleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentDetalle = response.body();
                    bindDetalle(currentDetalle);
                    updateMap();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ActivityDetalleResponse> call, @NonNull Throwable t) {}
        });
    }

    private void bindDetalle(ActivityDetalleResponse detalle) {
        activityNameText.setText(detalle.getName());
        priceText.setText(String.format("Precio: %s %.2f", detalle.getCurrency(), detalle.getBasePrice()));
        fullDescriptionText.setText(detalle.getFullDescription());
        meetingPointText.setText("Punto de encuentro: " + detalle.getMeetingPoint());
        guideNameText.setText("Guía: " + detalle.getGuideName());
        languageText.setText("Idioma: " + detalle.getLanguage());

        Glide.with(this).load(detalle.getFirstImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(activityImageView);

        btnHowToGet.setOnClickListener(v -> {
            if (detalle.getMeetingPointLatitude() != null) {
                openNavigationApp(detalle.getMeetingPointLatitude(), detalle.getMeetingPointLongitude());
            }
        });
    }

    private void openNavigationApp(double lat, double lng) {
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(Punto de Encuentro)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        
        Intent chooser = Intent.createChooser(mapIntent, "Selecciona tu aplicación de mapas");
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null || chooser.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(getContext(), "No tienes aplicaciones de mapas instaladas", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadHorarios() {
        apiService.getHorariosActivity(activityId).enqueue(new Callback<PaginatedSchedulesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedSchedulesResponse> call, @NonNull Response<PaginatedSchedulesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    scheduleActivityAdapter.setSchedules(response.body().getContent());
                    btnReservar.setEnabled(true);
                }
            }
            @Override
            public void onFailure(@NonNull Call<PaginatedSchedulesResponse> call, @NonNull Throwable t) {}
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        updateMap();
    }

    private void updateMap() {
        if (mMap == null || currentDetalle == null || currentDetalle.getMeetingPointLatitude() == null) return;

        mMap.clear();
        LatLng meetingPoint = new LatLng(currentDetalle.getMeetingPointLatitude(), currentDetalle.getMeetingPointLongitude());
        
        mMap.addMarker(new MarkerOptions()
                .position(meetingPoint)
                .title("Punto de Encuentro")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(meetingPoint);

        List<ItineraryResponse> itineraries = currentDetalle.getItineraries();
        if (itineraries != null && !itineraries.isEmpty()) {
            PolylineOptions lineOptions = new PolylineOptions().width(8).color(Color.BLUE).geodesic(true);
            for (ItineraryResponse point : itineraries) {
                LatLng pos = new LatLng(point.getLatitude(), point.getLongitude());
                mMap.addMarker(new MarkerOptions().position(pos).title(point.getName()));
                lineOptions.add(pos);
                builder.include(pos);
            }
            mMap.addPolyline(lineOptions);
        }

        LatLngBounds bounds = builder.build();
        mMap.setOnMapLoadedCallback(() -> {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        });
    }

    @Override
    public void onScheduleClick(long scheduleId, String date, String time, int availableSpots) {
        Bundle args = new Bundle();
        args.putLong("activityId", activityId);
        args.putLong("scheduleId", scheduleId);
        args.putString("date", date);
        args.putString("time", time);
        args.putInt("availableSpots", availableSpots);
        args.putDouble("precioBase", currentDetalle.getBasePrice());
        NavHostFragment.findNavController(this).navigate(R.id.action_activityDetalle_to_createReservationFragment, args);
    }
}