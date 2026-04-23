package com.example.da1androidnative.ui.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ReservaResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivityAdapter {

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        TextView reservationActivityName;
        TextView reservationStatus;
        TextView reservationDestination;
        TextView reservationDate;
        TextView reservationTime;
        TextView reservationParticipants;
        TextView reservationVoucherCode;
        TextView reservationId;
        MaterialCardView cardView;

        public ReservasViewHolder(@NonNull View itemView) {
            super(itemView);
            reservationActivityName = itemView.findViewById(R.id.reservationActivityName);
            reservationStatus = itemView.findViewById(R.id.reservationStatus);
            reservationDestination = itemView.findViewById(R.id.reservationDestination);
            reservationDate = itemView.findViewById(R.id.reservationDate);
            reservationTime = itemView.findViewById(R.id.reservationTime);
            reservationParticipants = itemView.findViewById(R.id.reservationParticipants);
            reservationVoucherCode = itemView.findViewById(R.id.reservationVoucherCode);
            reservationId = itemView.findViewById(R.id.reservationId);
            cardView = itemView.findViewById(R.id.reservationCard);
        }

    }

    private List<ReservaResponse> reservas = new ArrayList<>();

    private final Context context;

    private ReservasAdapter.OnReservaClickListener listener;

    public interface OnReservaClickListener {
        void onReservaClick(long reservationId);

    }

    public ReservasAdapter(Context context, ReservasAdapter.OnReservaClickListener listener) {
        this.context = context;
        this.listener = listener;
    }
    public void setReservas(List<ReservaResponse> reservas) {
        this.reservas = reservas;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    @NonNull
    @Override
    public ReservasAdapter.ReservasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reserva, parent, false);
        return new ReservasAdapter.ReservasViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ReservasAdapter.ReservasViewHolder holder, int position) {

        ReservaResponse reserva = reservas.get(position);

        holder.reservationActivityName.setText(reserva.getActivityName());
        holder.reservationStatus.setText(reserva.getStatus());
        holder.reservationDestination.setText(reserva.getDestination());
        holder.reservationDate.setText(reserva.getDate().toString());
        holder.reservationTime.setText(reserva.getTime());
        holder.reservationParticipants.setText(String.format("Cantidad de Participantes: %d", reserva.getParticipantsCount()));
        holder.reservationVoucherCode.setText(reserva.getVoucherCode());
        holder.reservationId.setText(String.format("ID Reserva: %d", reserva.getReservationId()));

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReservaClick(reserva.getReservationId());
            }
        });
    }
}
