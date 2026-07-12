package com.example.da1androidnative.ui.home;

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
import com.example.da1androidnative.data.model.ReservaDetalleResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.data.network.NetworkUtils;
import com.example.da1androidnative.ui.util.DateUtils;
import com.example.da1androidnative.ui.util.ToastHelper;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

@AndroidEntryPoint
public class VoucherFragment extends Fragment {

    @Inject ApiService apiService;
    @Inject OfflineReservationStorage offlineStorage;

    private long reservationId;

    private TextView reservationActivityNameText;
    private TextView reservationDateText;
    private TextView reservationTimeText;
    private TextView reservationParticipantsText;
    private TextView reservationMeetingPointText;
    private TextView reservationVoucherCodeText;
    private TextView reservationGuideText;
    private TextView tvAttendanceStatus;

    private Button btnScanQr;
    private Button btnBackToDetail;
    private LinearProgressIndicator progressIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voucher, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar(view);

        Bundle args = getArguments();
        if (args != null) {
            reservationId = args.getLong("reservationId", -1L);
        }

        btnBackToDetail.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        btnScanQr.setOnClickListener(v -> {
            Bundle scanArgs = new Bundle();
            scanArgs.putLong("reservationId", reservationId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_voucherFragment_to_qrScannerFragment, scanArgs);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDetalleReserva();
    }

    private void initViews(View view) {
        progressIndicator = view.findViewById(R.id.progressIndicator);

        reservationActivityNameText = view.findViewById(R.id.reservationActivityNameText);
        reservationDateText = view.findViewById(R.id.reservationDateText);
        reservationTimeText = view.findViewById(R.id.reservationTimeText);
        reservationParticipantsText = view.findViewById(R.id.reservationParticipantsText);
        reservationMeetingPointText = view.findViewById(R.id.reservationMeetingPointText);
        reservationVoucherCodeText = view.findViewById(R.id.reservationVoucherCodeText);

        reservationGuideText = view.findViewById(R.id.reservationGuideText);
        tvAttendanceStatus = view.findViewById(R.id.tvAttendanceStatus);

        btnScanQr = view.findViewById(R.id.btnScanQr);
        btnBackToDetail = view.findViewById(R.id.btnBackToDetail);
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.voucherToolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigateUp()
            );
        }
    }

    private void setLoading(boolean loading) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void loadDetalleReserva() {
        if (reservationId == -1L) return;

        ReservaDetalleResponse cached = offlineStorage.getSavedReservationDetailOrSummary(reservationId);
        if (cached != null) {
            bindDetalle(cached);
        }

        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            if (cached == null) bindSavedDetalle();
            return;
        }

        setLoading(true);

        apiService.getDetalleReserva(reservationId).enqueue(new Callback<ReservaDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReservaDetalleResponse> call,
                                   @NonNull Response<ReservaDetalleResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ReservaDetalleResponse detalle = response.body();
                    offlineStorage.saveReservation(detalle);
                    offlineStorage.saveReservationDetail(detalle);
                    bindDetalle(detalle);
                } else {
                    if (cached == null) bindSavedDetalleOrShowError("Error al cargar voucher");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReservaDetalleResponse> call,
                                  @NonNull Throwable t) {
                setLoading(false);
                bindSavedDetalleOrShowError("Error de red: " + t.getMessage());
            }
        });
    }

    private void bindSavedDetalle() {
        bindSavedDetalleOrShowError("No hay voucher guardado para esta reserva");
    }

    private void bindSavedDetalleOrShowError(String errorMessage) {
        ReservaDetalleResponse savedReserva =
                offlineStorage.getSavedReservationDetailOrSummary(reservationId);

        if (savedReserva == null) {
            ToastHelper.show(getContext(), errorMessage);
            return;
        }

        bindDetalle(savedReserva);
    }

    private void bindDetalle(ReservaDetalleResponse detalle) {
        reservationActivityNameText.setText(valueOrFallback(detalle.getActivityName()));

        reservationDateText.setText(DateUtils.formatDate(detalle.getDate()));

        reservationTimeText.setText(valueOrFallback(detalle.getTime()));

        reservationParticipantsText.setText(
                String.valueOf(detalle.getParticipantsCount())
        );

        reservationMeetingPointText.setText(
                valueOrFallback(detalle.getMeetingPoint())
        );

        reservationVoucherCodeText.setText(
                valueOrFallback(detalle.getVoucherCode())
        );

        String guideName = detalle.getGuideName();

        reservationGuideText.setText(
                guideName != null && !guideName.isBlank()
                        ? guideName
                        : "A confirmar"
        );

        String status = detalle.getStatus();
        if ("COMPLETED".equalsIgnoreCase(status)) {
            btnScanQr.setVisibility(View.GONE);
            tvAttendanceStatus.setVisibility(View.VISIBLE);
        } else {
            tvAttendanceStatus.setVisibility(View.GONE);
            btnScanQr.setVisibility(View.VISIBLE);

            if ("CANCELLED".equalsIgnoreCase(status)) {
                btnScanQr.setText("Check-in no disponible");
                btnScanQr.setEnabled(false);
                btnScanQr.setAlpha(0.5f);
            } else {
                btnScanQr.setText("Escanear Código QR");
                btnScanQr.setEnabled(true);
                btnScanQr.setAlpha(1.0f);
            }
        }
    }
    private String valueOrFallback(String value) {
        return value == null || value.trim().isEmpty()
                ? "No disponible sin conexión"
                : value;
    }
}