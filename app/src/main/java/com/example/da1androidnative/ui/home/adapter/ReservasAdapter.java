package com.example.da1androidnative.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ReservaResponse;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReservasAdapter extends RecyclerView.Adapter<ReservasAdapter.ReservasViewHolder>{

    public static class ReservasViewHolder extends RecyclerView.ViewHolder {

        TextView reservationActivityName;
        TextView reservationStatus;
        TextView reservationDestination;
        TextView reservationDate;
        TextView reservationTime;
        TextView reservationParticipants;
        TextView reservationVoucherCode;
        TextView reservationId;

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

        }

    }

    private final List<ReservaResponse> reservas = new ArrayList<>();

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    @Override
    public ReservasViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ReservasViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ReservasViewHolder holder, int position) {
        holder.reservationActivityName.setText(reservas.get(position).getActivityName());
        holder.reservationStatus.setText(reservas.get(position).getStatus());
        holder.reservationDestination.setText(reservas.get(position).getDestination());
        holder.reservationDate.setText(reservas.get(position).getDate().toString());
        holder.reservationTime.setText(reservas.get(position).getTime());
        holder.reservationParticipants.setText(reservas.get(position).getParticipantsCount());
        holder.reservationVoucherCode.setText(reservas.get(position).getVoucherCode());
        holder.reservationId.setText(reservas.get(position).getReservationId());
    }








}
