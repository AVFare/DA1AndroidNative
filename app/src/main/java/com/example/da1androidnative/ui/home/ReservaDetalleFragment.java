package com.example.da1androidnative.ui.home;

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

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ReservaDetalleResponse;
import com.example.da1androidnative.data.network.ApiService;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ReservaDetalleFragment extends Fragment {
    @Inject
    ApiService apiService;
    private long reservationId;
    private TextView reservationActivityNameText;
    private TextView reservationStatusText;
    private TextView reservationDestinationText;
    private TextView reservationIdText;
    private TextView reservationDateText;
    private TextView reservationTimeText;
    private TextView reservationParticipantsText;
    private TextView reservationMeetingPointText;
    private TextView reservationVoucherCodeText;
    private TextView reservationTotalPriceText;
    private TextView reservationCancellationPolicyText;
    private Button cancelReservationButton;
    private String reservationStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reserva_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        Bundle args = getArguments();
        if (args != null) {
            this.reservationId = args.getLong("reservationId", -1L);
        }

        loadDetalleReserva();
        setupButtons();
    }

    public String getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    private void loadDetalleReserva() {

        if (this.reservationId == -1L) {
            Toast.makeText(getContext(), "Reserva inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getDetalleReserva(reservationId).enqueue(new Callback<ReservaDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReservaDetalleResponse> call, @NonNull Response<ReservaDetalleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReservaDetalleResponse detalle = response.body();
                    android.util.Log.d("API_HOME", "Detalle de Reserva Recibido");
                    bindDetalle(detalle);
                    setReservationStatus(detalle.getStatus());
                } else {
                    android.util.Log.e("API_HOME", "Error en respuesta: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar detalle de reserva", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReservaDetalleResponse> call, @NonNull Throwable t) {
                android.util.Log.e("API_HOME", "Fallo total: " + t.getMessage());
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupButtons() {

        if (Objects.equals(this.reservationStatus, "Cancelada")) {
            cancelReservationButton.setEnabled(false);
        }

        cancelReservationButton.setOnClickListener(v -> cancelReserva());
    }

    private void cancelReserva () {




    }

    private void bindDetalle(ReservaDetalleResponse detalle) {
        reservationActivityNameText.setText(detalle.getActivityName());
        reservationStatusText.setText(detalle.getStatus());
        reservationDestinationText.setText(detalle.getDestination());
        reservationIdText.setText(getString(R.string.reservation_item_id_label, detalle.getReservationId()));
        reservationDateText.setText(getString(R.string.reservation_item_date_label) + ": " + detalle.getDate());
        reservationTimeText.setText(getString(R.string.reservation_item_time_label) + ": " + detalle.getTime());
        reservationParticipantsText.setText(getString(R.string.reservation_item_participants_label) + ": " + detalle.getParticipantsCount());
        reservationMeetingPointText.setText("Punto de encuentro: " + detalle.getMeetingPoint());
        reservationVoucherCodeText.setText(getString(R.string.reservation_item_voucher_label) + ": " + detalle.getVoucherCode());
        reservationTotalPriceText.setText("Total: $" + detalle.getTotalPrice());
        reservationCancellationPolicyText.setText(detalle.getCancellationPolicy());
        setReservationStatus(detalle.getStatus());
        setupButtons();
    }
}
