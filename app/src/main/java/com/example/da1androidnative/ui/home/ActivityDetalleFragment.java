package com.example.da1androidnative.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.da1androidnative.ui.util.ToastHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.FavoritesManager;
import com.example.da1androidnative.data.model.ActivityDetalleResponse;
import com.example.da1androidnative.data.model.ItineraryResponse;
import com.example.da1androidnative.data.model.PaginatedSchedulesResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.home.adapter.ActivityGalleryAdapter;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Collections;
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
    @Inject FavoritesManager favoritesManager;

    private ActivityDetalleResponse currentDetalle;
    private ScheduleActivityAdapter scheduleActivityAdapter;
    private ActivityGalleryAdapter galleryAdapter;
    private Toolbar toolbar;
    private ImageButton btnFavoriteDetail;
    private long activityId;
    private ViewPager2 galleryViewPager;
    private LinearLayout galleryDotsContainer;
    private ViewPager2.OnPageChangeCallback galleryPageChangeCallback;
    private TextView activityNameText;
    private TextView priceText;
    private TextView durationText;
    private TextView fullDescriptionText;
    private TextView inclusionsText;
    private TextView cancellationPolicyText;
    private TextView meetingPointText;
    private TextView guideNameText;
    private TextView languageText;
    private Button btnReservar;
    private MaterialButton btnHowToGet;
    private LinearProgressIndicator progressIndicator;
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
        setupGalleryPager();
        setupRecyclerView(view);
        
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.activityMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadDetalleActividad();
        loadHorarios();

        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
    }

    @Override
    public void onDestroyView() {
        if (galleryViewPager != null && galleryPageChangeCallback != null) {
            galleryViewPager.unregisterOnPageChangeCallback(galleryPageChangeCallback);
        }
        super.onDestroyView();
    }

    private void initViews(View view) {
        galleryViewPager = view.findViewById(R.id.galleryViewPager);
        galleryDotsContainer = view.findViewById(R.id.galleryDotsContainer);

        activityNameText = view.findViewById(R.id.activityNameText);
        priceText = view.findViewById(R.id.priceText);
        durationText = view.findViewById(R.id.durationText);
        fullDescriptionText = view.findViewById(R.id.fullDescriptionText);
        inclusionsText = view.findViewById(R.id.inclusionsText);
        cancellationPolicyText = view.findViewById(R.id.cancellationPolicyText);
        meetingPointText = view.findViewById(R.id.meetingPointText);
        guideNameText = view.findViewById(R.id.guideNameText);
        languageText = view.findViewById(R.id.languageText);
        toolbar = view.findViewById(R.id.toolbar);
        btnFavoriteDetail = view.findViewById(R.id.btnFavoriteDetail);
        btnReservar = view.findViewById(R.id.btnReservar);
        btnHowToGet = view.findViewById(R.id.btnHowToGetActivity);

        progressIndicator = view.findViewById(R.id.progressIndicator);

        btnFavoriteDetail.setOnClickListener(v -> toggleFavoriteDetail());
    }

    private void setupGalleryPager() {
        galleryAdapter = new ActivityGalleryAdapter();
        galleryViewPager.setAdapter(galleryAdapter);

        galleryPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateGalleryDots(position);
            }
        };

        galleryViewPager.registerOnPageChangeCallback(galleryPageChangeCallback);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.scheduleRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleActivityAdapter = new ScheduleActivityAdapter(getContext(), this);
        recyclerView.setAdapter(scheduleActivityAdapter);
    }

    private void loadDetalleActividad() {
        if (this.activityId == -1L) return;

        if (progressIndicator != null) progressIndicator.setVisibility(View.VISIBLE);

        apiService.getDetalleActivity(activityId).enqueue(new Callback<ActivityDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityDetalleResponse> call, @NonNull Response<ActivityDetalleResponse> response) {
                if (progressIndicator != null) progressIndicator.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentDetalle = response.body();
                    bindDetalle(currentDetalle);
                    updateMap();
                } else {
                    ToastHelper.show(getContext(), "Error al cargar detalle");
                }
            }
            @Override
            public void onFailure(@NonNull Call<ActivityDetalleResponse> call, @NonNull Throwable t) {
                if (progressIndicator != null) progressIndicator.setVisibility(View.GONE);
                ToastHelper.show(getContext(), "Error de conexion: " + t.getMessage());
            }
        });
    }

    private void bindDetalle(ActivityDetalleResponse detalle) {
        activityNameText.setText(detalle.getName());
        priceText.setText(String.format(Locale.getDefault(), "Precio: %s %.2f", detalle.getCurrency(), detalle.getBasePrice()));
        durationText.setText("Duración: " + formatDuration(detalle.getDurationMinutes()));
        fullDescriptionText.setText(detalle.getFullDescription());
        inclusionsText.setText(valueOrFallback(detalle.getInclusions(), "No hay inclusiones detalladas para esta actividad."));
        cancellationPolicyText.setText(valueOrFallback(detalle.getCancellationPolicy(), "No hay una política de cancelación disponible para esta actividad."));
        meetingPointText.setText("Punto de encuentro: " + detalle.getMeetingPoint());
        guideNameText.setText("Guía: " + detalle.getGuideName());
        languageText.setText("Idioma: " + detalle.getLanguage());
        updateFavoriteButton();

        List<String> gallery = detalle.getGallery();

        if (gallery != null && !gallery.isEmpty()) {
            galleryAdapter.setImages(gallery);
            setupGalleryDots(gallery.size());
            galleryViewPager.setVisibility(View.VISIBLE);
            galleryViewPager.setCurrentItem(0, false);
        } else if (detalle.getFirstImageUrl() != null && !detalle.getFirstImageUrl().isEmpty()) {
            galleryAdapter.setImages(Collections.singletonList(detalle.getFirstImageUrl()));
            setupGalleryDots(1);
            galleryViewPager.setVisibility(View.VISIBLE);
            galleryViewPager.setCurrentItem(0, false);
        } else {
            galleryAdapter.setImages(Collections.emptyList());
            galleryDotsContainer.removeAllViews();
            galleryDotsContainer.setVisibility(View.GONE);
            galleryViewPager.setVisibility(View.GONE);
        }

        btnHowToGet.setOnClickListener(v -> {
            if (detalle.getMeetingPointLatitude() != null && detalle.getMeetingPointLatitude() != 0.0) {
                openNavigationApp(detalle.getMeetingPointLatitude(), detalle.getMeetingPointLongitude());
            } else {
                ToastHelper.show(getContext(), "Ubicacion no disponible");
            }
        });
    }

    private String formatDuration(int minutes) {
        if (minutes <= 0) {
            return "A confirmar";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours > 0 && remainingMinutes > 0) {
            return hours + " h " + remainingMinutes + " min";
        }

        if (hours > 0) {
            return hours + " h";
        }

        return remainingMinutes + " min";
    }

    private String valueOrFallback(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private void setupGalleryDots(int count) {
        galleryDotsContainer.removeAllViews();

        if (count <= 0) {
            galleryDotsContainer.setVisibility(View.GONE);
            return;
        }

        galleryDotsContainer.setVisibility(View.VISIBLE);

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(requireContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(8),
                    dpToPx(8)
            );

            if (i > 0) {
                params.leftMargin = dpToPx(6);
            }

            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0
                    ? R.drawable.dot_indicator_selected
                    : R.drawable.dot_indicator_unselected);

            galleryDotsContainer.addView(dot);
        }
    }

    private void updateGalleryDots(int selectedPosition) {
        if (galleryDotsContainer == null) return;

        int count = galleryDotsContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View dot = galleryDotsContainer.getChildAt(i);
            dot.setBackgroundResource(i == selectedPosition
                    ? R.drawable.dot_indicator_selected
                    : R.drawable.dot_indicator_unselected);
        }
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void openNavigationApp(double lat, double lng) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng + "(Punto de Encuentro)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        startActivity(Intent.createChooser(mapIntent, "Selecciona tu aplicación de mapas"));
    }

    private void toggleFavoriteDetail() {
        if (activityId == -1L) return;
        boolean favorite = currentDetalle != null
                ? favoritesManager.toggleFavorite(currentDetalle)
                : toggleFavoriteById();
        updateFavoriteButton();
        ToastHelper.show(getContext(), favorite ? "Agregado a favoritos" : "Quitado de favoritos");
    }

    private boolean toggleFavoriteById() {
        favoritesManager.toggleFavorite(activityId);
        return favoritesManager.isFavorite(activityId);
    }

    private void updateFavoriteButton() {
        if (btnFavoriteDetail == null || activityId == -1L) return;
        boolean favorite = favoritesManager.isFavorite(activityId);
        btnFavoriteDetail.setImageResource(favorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
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
                ToastHelper.show(getContext(), "Error al cargar horarios");
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
