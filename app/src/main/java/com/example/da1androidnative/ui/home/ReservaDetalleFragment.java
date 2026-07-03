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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.OfflineReservationStorage;
import com.example.da1androidnative.data.model.ActivityDetalleResponse;
import com.example.da1androidnative.data.model.ActivityHistoryDetailResponse;
import com.example.da1androidnative.data.model.ItineraryResponse;
import com.example.da1androidnative.data.model.ReservaCancelledResponse;
import com.example.da1androidnative.data.model.ReservaDetalleResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.data.network.NetworkUtils;
import com.example.da1androidnative.ui.util.ToastHelper;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;
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
    private TextView reservationRatingStatusText, reservationActivityRatingText, reservationGuideRatingText, reservationCommentText;
    private Button cancelReservationButton, btnHowToGet, viewVoucherButton;
    private LinearProgressIndicator progressIndicator;

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
        setupToolbar(view);

        Bundle args = getArguments();
        if (args != null) {
            this.reservationId = args.getLong("reservationId", -1L);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.reservaMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadDetalleReserva();
    }

    private void initViews(View view) {
        progressIndicator = view.findViewById(R.id.progressIndicator);
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
        reservationRatingStatusText = view.findViewById(R.id.reservationRatingStatusText);
        reservationActivityRatingText = view.findViewById(R.id.reservationActivityRatingText);
        reservationGuideRatingText = view.findViewById(R.id.reservationGuideRatingText);
        reservationCommentText = view.findViewById(R.id.reservationCommentText);
        cancelReservationButton = view.findViewById(R.id.cancelReservationButton);
        btnHowToGet = view.findViewById(R.id.btnHowToGet);
        viewVoucherButton = view.findViewById(R.id.viewVoucherButton);
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.reservaDetalleToolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        }
    }

    private void loadDetalleReserva() {
        if (this.reservationId == -1L) return;

        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            bindSavedDetalle();
            return;
        }

        setLoading(true);
        apiService.getDetalleReserva(reservationId).enqueue(new Callback<ReservaDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReservaDetalleResponse> call, @NonNull Response<ReservaDetalleResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentReserva = response.body();
                    offlineStorage.saveReservation(currentReserva);
                    offlineStorage.saveReservationDetail(currentReserva);
                    bindDetalle(currentReserva);
                    updateMap();
                    fetchActivityDetails(currentReserva.getActivityId());
                    fetchHistoryDetail(currentReserva.getReservationId());
                } else {
                    bindSavedDetalleOrShowError("Error al cargar detalle");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReservaDetalleResponse> call, @NonNull Throwable t) {
                setLoading(false);
                bindSavedDetalleOrShowError("Error de red: " + t.getMessage());
            }
        });
    }

    private void bindSavedDetalle() {
        bindSavedDetalleOrShowError("No hay detalle guardado para esta reserva");
    }

    private void bindSavedDetalleOrShowError(String errorMessage) {
        ReservaDetalleResponse savedReserva = offlineStorage.getSavedReservationDetailOrSummary(reservationId);
        if (savedReserva == null) {
            ToastHelper.show(getContext(), errorMessage);
            return;
        }

        currentReserva = savedReserva;
        bindDetalle(currentReserva);
        updateMap();
        bindEmptyRating();
    }

    private void fetchHistoryDetail(long reservationId) {
        apiService.getActivityHistoryDetail(reservationId).enqueue(new Callback<ActivityHistoryDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityHistoryDetailResponse> call, @NonNull Response<ActivityHistoryDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindHistoryRating(response.body());
                } else {
                    bindEmptyRating();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivityHistoryDetailResponse> call, @NonNull Throwable t) {
                bindEmptyRating();
            }
        });
    }

    private void fetchActivityDetails(long activityId) {
        apiService.getDetalleActivity(activityId).enqueue(new Callback<ActivityDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityDetalleResponse> call, @NonNull Response<ActivityDetalleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activityInfo = response.body();
                    updateMap();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivityDetalleResponse> call, @NonNull Throwable t) {}
        });
    }

    private void bindDetalle(ReservaDetalleResponse detalle) {
        reservationActivityNameText.setText(valueOrFallback(detalle.getActivityName()));
        reservationStatusText.setText(valueOrFallback(detalle.getStatus()));
        reservationDestinationText.setText(String.format("Destino: %s", valueOrFallback(detalle.getDestination())));
        reservationIdText.setText(getString(R.string.reservation_item_id_label, detalle.getReservationId()));
        reservationDateText.setText(String.format("Fecha: %s", (detalle.getDate() != null ? detalle.getDate().toString() : "N/A")));
        reservationTimeText.setText(String.format("Hora: %s", valueOrFallback(detalle.getTime())));
        reservationParticipantsText.setText(String.format("Participantes: %d", detalle.getParticipantsCount()));
        reservationMeetingPointText.setText(String.format("Punto de encuentro: %s", valueOrFallback(detalle.getMeetingPoint())));
        reservationVoucherCodeText.setText(String.format("Voucher: %s", valueOrFallback(detalle.getVoucherCode())));
        reservationTotalPriceText.setText(detalle.getTotalPrice() > 0
                ? String.format(Locale.getDefault(), "Total: $%.2f", detalle.getTotalPrice())
                : "Total: No disponible sin conexion");
        reservationCancellationPolicyText.setText(valueOrFallback(detalle.getCancellationPolicy()));
        bindEmptyRating();

        if (Objects.equals(detalle.getStatus(), "CANCELLED") || Objects.equals(detalle.getStatus(), "COMPLETED")) {
            cancelReservationButton.setEnabled(false);
            cancelReservationButton.setAlpha(0.5f);
        } else {
            cancelReservationButton.setEnabled(true);
            cancelReservationButton.setAlpha(1.0f);
        }

        cancelReservationButton.setOnClickListener(v -> showCancelConfirmationDialog(detalle));

        btnHowToGet.setOnClickListener(v -> {
            Double lat = getSafeLat();
            Double lng = getSafeLng();
            if (lat != null) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng + "(Punto de Encuentro)");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(Intent.createChooser(mapIntent, "Selecciona tu aplicación de mapas"));
            } else {
                ToastHelper.show(getContext(), "Ubicación no disponible");
            }
        });

        viewVoucherButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong("reservationId", reservationId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_reservaDetalleFragment_to_voucherFragment, args);
        });
    }

    private String valueOrFallback(String value) {
        return value == null || value.trim().isEmpty() ? "No disponible sin conexion" : value;
    }

    private void bindHistoryRating(ActivityHistoryDetailResponse detail) {
        if (detail == null || !detail.isHasRating()) {
            bindEmptyRating();
            return;
        }

        reservationRatingStatusText.setText("Calificacion enviada.");
        reservationActivityRatingText.setVisibility(View.VISIBLE);
        reservationGuideRatingText.setVisibility(View.VISIBLE);
        reservationCommentText.setVisibility(View.VISIBLE);
        reservationActivityRatingText.setText("Actividad: " + formatRating(detail.getActivityStars()));
        reservationGuideRatingText.setText("Guia: " + formatRating(detail.getGuideStars()));

        String comment = detail.getComment();
        reservationCommentText.setText(comment == null || comment.trim().isEmpty()
                ? "Comentario: Sin comentario."
                : "Comentario: " + comment);
    }

    private void bindEmptyRating() {
        reservationRatingStatusText.setText("Sin calificacion registrada.");
        reservationActivityRatingText.setVisibility(View.GONE);
        reservationGuideRatingText.setVisibility(View.GONE);
        reservationCommentText.setVisibility(View.GONE);
    }

    private String formatRating(Integer rating) {
        return rating != null ? String.format(Locale.getDefault(), "%d/5", rating) : "N/A";
    }

    private Double getSafeLat() {
        if (activityInfo != null && activityInfo.getMeetingPointLatitude() != null && activityInfo.getMeetingPointLatitude() != 0.0) return activityInfo.getMeetingPointLatitude();
        if (currentReserva != null && currentReserva.getMeetingPointLatitude() != null && currentReserva.getMeetingPointLatitude() != 0.0) return currentReserva.getMeetingPointLatitude();
        return null;
    }

    private Double getSafeLng() {
        if (activityInfo != null && activityInfo.getMeetingPointLongitude() != null && activityInfo.getMeetingPointLongitude() != 0.0) return activityInfo.getMeetingPointLongitude();
        if (currentReserva != null && currentReserva.getMeetingPointLongitude() != null && currentReserva.getMeetingPointLongitude() != 0.0) return currentReserva.getMeetingPointLongitude();
        return null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        updateMap();
    }

    private void updateMap() {
        if (mMap == null) return;
        Double lat = getSafeLat();
        Double lng = getSafeLng();
        if (lat == null) return;

        mMap.clear();
        LatLng meetingPoint = new LatLng(lat, lng);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(meetingPoint);

        List<ItineraryResponse> itineraries = (activityInfo != null && activityInfo.getItineraries() != null)
                ? activityInfo.getItineraries()
                : (currentReserva != null ? currentReserva.getItineraries() : null);

        if (itineraries != null && !itineraries.isEmpty()) {
            PolylineOptions lineOptions = new PolylineOptions()
                    .width(12)
                    .color(Color.parseColor("#4A90E2"))
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .geodesic(true);

            for (ItineraryResponse point : itineraries) {
                LatLng pos = new LatLng(point.getLatitude(), point.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(point.getName())
                        .zIndex(1.0f));
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

        mMap.addMarker(new MarkerOptions()
                .position(meetingPoint)
                .title("Punto de Encuentro")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .zIndex(2.0f));
    }

    private void showCancelConfirmationDialog(ReservaDetalleResponse detalle) {
        String cancellationPolicy = detalle.getCancellationPolicy();
        if (cancellationPolicy == null || cancellationPolicy.trim().isEmpty()) {
            cancellationPolicy = "No hay una politica de cancelacion disponible para esta reserva.";
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cancelar reserva")
                .setMessage("Politica de cancelacion aplicable:\n\n" + cancellationPolicy)
                .setNegativeButton("Volver", null)
                .setPositiveButton("Cancelar reserva", (dialog, which) -> cancelReserva())
                .show();
    }

    private void cancelReserva() {
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            offlineStorage.addPendingCancellation(reservationId);
            offlineStorage.updateReservationStatus(reservationId, "CANCELLED");
            ToastHelper.show(getContext(), "Cancelacion guardada. Se sincronizara cuando vuelva la conexion.");
            NavHostFragment.findNavController(ReservaDetalleFragment.this).navigateUp();
            return;
        }

        setLoading(true);
        apiService.cancelReserva(reservationId).enqueue(new Callback<ReservaCancelledResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReservaCancelledResponse> call, @NonNull Response<ReservaCancelledResponse> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    ToastHelper.show(getContext(), "Reserva Cancelada");
                    NavHostFragment.findNavController(ReservaDetalleFragment.this).navigateUp();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReservaCancelledResponse> call, @NonNull Throwable t) {
                setLoading(false);
                ToastHelper.show(getContext(), "Error al cancelar reserva");
            }
        });
    }
}
