package com.example.da1androidnative.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.OfflineReservationStorage;
import com.example.da1androidnative.data.model.ActivityDetalleResponse;
import com.example.da1androidnative.data.model.ItineraryResponse;
import com.example.da1androidnative.data.model.ReservaCancelledResponse;
import com.example.da1androidnative.data.model.ReservaDetalleResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.data.network.NetworkUtils;
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
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ReservaDetalleFragment extends Fragment implements OnMapReadyCallback {
    
    @Inject ApiService apiService;
    @Inject OfflineReservationStorage offlineStorage;
    
    private long reservationId;
    private TextView reservationActivityNameText, reservationStatusText, reservationDestinationText;
    private TextView reservationIdText, reservationDateText, reservationTimeText, reservationParticipantsText;
    private TextView reservationMeetingPointText, reservationVoucherCodeText, reservationTotalPriceText;
    private TextView reservationCancellationPolicyText;
    private Button cancelReservationButton, btnHowToGet;
    
    private GoogleMap mMap;
    private ReservaDetalleResponse currentReserva;
    private ActivityDetalleResponse activityInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reserva_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        
        Bundle args = getArguments();
        if (args != null) {
            this.reservationId = args.getLong("reservationId", -1L);
        }

        // Buscamos el mapa con el ID correcto definido en el XML
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.reservaMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadDetalleReserva();
    }

    private void initViews(View view) {
        reservationActivityNameText = view.findViewById(R.id.reservationActivityNameText);
        reservationStatusText = view.findViewById(R.id.reservationStatusText);
        reservationDestinationText = view.findViewById(R.id.reservationDestinationText);
        reservationIdText = view.findViewById(R.id.reservationIdText);
        reservationDateText = view.findViewById(R.id.reservationDateText);
        reservationTimeText = view.findViewById(R.id.reservationTimeText);
        reservationParticipantsText = view.findViewById(R.id.reservationParticipantsText);
        reservationMeetingPointText = view.findViewById(R.id.reservationMeetingPointText);
        reservationVoucherCodeText = view.findViewById(R.id.reservationVoucherCodeText);
        reservationTotalPriceText = view.findViewById(R.id.reservationTotalPriceText);
        reservationCancellationPolicyText = view.findViewById(R.id.reservationCancellationPolicyText);
        cancelReservationButton = view.findViewById(R.id.cancelReservationButton);
        btnHowToGet = view.findViewById(R.id.btnHowToGet);
    }

    private void loadDetalleReserva() {
        if (this.reservationId == -1L) return;

        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            // Modo Offline: Cargar detalle desde SharedPreferences
            ReservaDetalleResponse savedDetail = offlineStorage.getSavedReservationDetail(reservationId);
            if (savedDetail != null) {
                currentReserva = savedDetail;
                bindDetalle(currentReserva);
                updateMap();
                setupHowToGetButton();
            } else {
                Toast.makeText(getContext(), "Detalle no disponible sin conexión", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Modo Online
            apiService.getDetalleReserva(reservationId).enqueue(new Callback<ReservaDetalleResponse>() {
                @Override
                public void onResponse(@NonNull Call<ReservaDetalleResponse> call, @NonNull Response<ReservaDetalleResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentReserva = response.body();
                        bindDetalle(currentReserva);
                        // Guardar para offline
                        offlineStorage.saveReservationDetail(currentReserva);
                        
                        fetchActivityDetails(currentReserva.getActivityId());
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ReservaDetalleResponse> call, @NonNull Throwable t) {
                    // Fallback a offline si hay error de red
                    ReservaDetalleResponse savedDetail = offlineStorage.getSavedReservationDetail(reservationId);
                    if (savedDetail != null) {
                        currentReserva = savedDetail;
                        bindDetalle(currentReserva);
                        updateMap();
                    }
                    Toast.makeText(getContext(), "Error al cargar reserva, mostrando datos locales", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchActivityDetails(long activityId) {
        apiService.getDetalleActivity(activityId).enqueue(new Callback<ActivityDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityDetalleResponse> call, @NonNull Response<ActivityDetalleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activityInfo = response.body();
                    updateMap();
                    setupHowToGetButton();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ActivityDetalleResponse> call, @NonNull Throwable t) {}
        });
    }

    private void bindDetalle(ReservaDetalleResponse detalle) {
        reservationActivityNameText.setText(detalle.getActivityName());
        reservationStatusText.setText(detalle.getStatus());
        reservationDestinationText.setText(detalle.getDestination());
        reservationIdText.setText(getString(R.string.reservation_item_id_label, detalle.getReservationId()));
        reservationDateText.setText("Fecha: " + (detalle.getDate() != null ? detalle.getDate().toString() : "N/A"));
        reservationTimeText.setText("Hora: " + detalle.getTime());
        reservationParticipantsText.setText("Participantes: " + detalle.getParticipantsCount());
        reservationMeetingPointText.setText("Punto de encuentro: " + detalle.getMeetingPoint());
        reservationVoucherCodeText.setText("Voucher: " + detalle.getVoucherCode());
        reservationTotalPriceText.setText("Total: $" + detalle.getTotalPrice());
        reservationCancellationPolicyText.setText(detalle.getCancellationPolicy());

        if (Objects.equals(detalle.getStatus(), "CANCELLED") || Objects.equals(detalle.getStatus(), "COMPLETED")) {
            cancelReservationButton.setEnabled(false);
            cancelReservationButton.setAlpha(0.5f);
        } else {
            // Deshabilitar cancelación si no hay internet
            cancelReservationButton.setEnabled(NetworkUtils.isNetworkAvailable(getContext()));
            cancelReservationButton.setAlpha(NetworkUtils.isNetworkAvailable(getContext()) ? 1.0f : 0.5f);
        }

        cancelReservationButton.setOnClickListener(v -> {
            if (NetworkUtils.isNetworkAvailable(getContext())) {
                cancelReserva();
            } else {
                Toast.makeText(getContext(), "Se requiere conexión para cancelar", Toast.LENGTH_SHORT).show();
            }
        });
        setupHowToGetButton();
    }

    private void setupHowToGetButton() {
        btnHowToGet.setOnClickListener(v -> {
            Double lat = null, lng = null;
            if (activityInfo != null && activityInfo.getMeetingPointLatitude() != null) {
                lat = activityInfo.getMeetingPointLatitude();
                lng = activityInfo.getMeetingPointLongitude();
            } else if (currentReserva != null && currentReserva.getMeetingPointLatitude() != null) {
                lat = currentReserva.getMeetingPointLatitude();
                lng = currentReserva.getMeetingPointLongitude();
            }

            if (lat != null && lng != null) {
                openNavigationApp(lat, lng);
            } else {
                Toast.makeText(getContext(), "Ubicación no disponible aún", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openNavigationApp(double lat, double lng) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng + "(Punto de Encuentro)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        startActivity(Intent.createChooser(mapIntent, "Selecciona tu aplicación de mapas"));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        updateMap();
    }

    private void updateMap() {
        if (mMap == null) return;

        Double lat = (activityInfo != null) ? activityInfo.getMeetingPointLatitude() : (currentReserva != null ? currentReserva.getMeetingPointLatitude() : null);
        Double lng = (activityInfo != null) ? activityInfo.getMeetingPointLongitude() : (currentReserva != null ? currentReserva.getMeetingPointLongitude() : null);
        List<ItineraryResponse> itineraries = (activityInfo != null) ? activityInfo.getItineraries() : (currentReserva != null ? currentReserva.getItineraries() : null);

        if (lat == null || lng == null) return;

        mMap.clear();
        LatLng meetingPoint = new LatLng(lat, lng);
        
        mMap.addMarker(new MarkerOptions()
                .position(meetingPoint)
                .title("Punto de Encuentro")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(meetingPoint);

        if (itineraries != null && !itineraries.isEmpty()) {
            PolylineOptions lineOptions = new PolylineOptions()
                    .width(12).color(Color.parseColor("#4A90E2"))
                    .startCap(new RoundCap()).endCap(new RoundCap()).geodesic(true);

            for (ItineraryResponse point : itineraries) {
                LatLng pos = new LatLng(point.getLatitude(), point.getLongitude());
                mMap.addMarker(new MarkerOptions().position(pos).title(point.getName()));
                lineOptions.add(pos);
                builder.include(pos);
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(meetingPoint, 15));
        }
    }

    private void cancelReserva() {
        apiService.cancelReserva(reservationId).enqueue(new Callback<ReservaCancelledResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReservaCancelledResponse> call, @NonNull Response<ReservaCancelledResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Reserva Cancelada", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(ReservaDetalleFragment.this).navigateUp();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ReservaCancelledResponse> call, @NonNull Throwable t) {}
        });
    }
}
