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
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.data.network.NetworkModule;
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
import com.google.android.gms.maps.model.RoundCap;

import java.util.List;
import java.util.Locale;

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
        RecyclerView recyclerView = view.findViewById(R.id.scheduleRecyclerView);
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
                } else {
                    Toast.makeText(getContext(), "Error al cargar detalle", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ActivityDetalleResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindDetalle(ActivityDetalleResponse detalle) {
        activityNameText.setText(detalle.getName());
        priceText.setText(String.format(Locale.getDefault(), "Precio: %s %.2f", detalle.getCurrency(), detalle.getBasePrice()));
        fullDescriptionText.setText(detalle.getFullDescription());
        meetingPointText.setText("Punto de encuentro: " + detalle.getMeetingPoint());
        guideNameText.setText("Guía: " + detalle.getGuideName());
        languageText.setText("Idioma: " + detalle.getLanguage());

        String fullImageUrl = NetworkModule.getFullImageUrl(detalle.getFirstImageUrl());

        Glide.with(this).load(fullImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(activityImageView);

        btnHowToGet.setOnClickListener(v -> {
            if (detalle.getMeetingPointLatitude() != null && detalle.getMeetingPointLatitude() != 0.0) {
                openNavigationApp(detalle.getMeetingPointLatitude(), detalle.getMeetingPointLongitude());
            } else {
                Toast.makeText(getContext(), "Ubicacion no disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openNavigationApp(double lat, double lng) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng + "(Punto de Encuentro)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        startActivity(Intent.createChooser(mapIntent, "Selecciona tu aplicación de mapas"));
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
            public void onFailure(@NonNull Call<PaginatedSchedulesResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error al cargar horarios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        updateMap();
    }

    private void updateMap() {
        if (mMap == null || currentDetalle == null) return;

        Double mLat = currentDetalle.getMeetingPointLatitude();
        Double mLng = currentDetalle.getMeetingPointLongitude();
        LatLng tempMeetingPoint = (mLat != null && mLat != 0.0) ? new LatLng(mLat, mLng) : null;

        List<ItineraryResponse> itineraries = currentDetalle.getItineraries();
        
        if (tempMeetingPoint == null && itineraries != null && !itineraries.isEmpty()) {
            tempMeetingPoint = new LatLng(itineraries.get(0).getLatitude(), itineraries.get(0).getLongitude());
        }

        if (tempMeetingPoint == null) return;

        final LatLng meetingPoint = tempMeetingPoint;

        mMap.clear();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(meetingPoint);

        if (itineraries != null && !itineraries.isEmpty()) {
            PolylineOptions lineOptions = new PolylineOptions()
                    .width(12).color(Color.parseColor("#4A90E2")).startCap(new RoundCap()).endCap(new RoundCap()).geodesic(true);
            
            for (ItineraryResponse point : itineraries) {
                LatLng pos = new LatLng(point.getLatitude(), point.getLongitude());
                builder.include(pos);
                lineOptions.add(pos);

                boolean isMP = isSameLocation(pos, meetingPoint);
                mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(point.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(isMP ? BitmapDescriptorFactory.HUE_BLUE : BitmapDescriptorFactory.HUE_RED))
                        .zIndex(isMP ? 2.0f : 1.0f));
            }
            mMap.addPolyline(lineOptions);
            
            mMap.setOnMapLoadedCallback(() -> {
                try {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
                } catch (Exception e) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(meetingPoint, 15));
                }
            });
        } else {
            mMap.addMarker(new MarkerOptions()
                    .position(meetingPoint)
                    .title("Punto de Encuentro")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .zIndex(2.0f));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(meetingPoint, 15));
        }
    }

    private boolean isSameLocation(LatLng loc1, LatLng loc2) {
        if (loc1 == null || loc2 == null) return false;
        return Math.abs(loc1.latitude - loc2.latitude) < 0.0001 && 
               Math.abs(loc1.longitude - loc2.longitude) < 0.0001;
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