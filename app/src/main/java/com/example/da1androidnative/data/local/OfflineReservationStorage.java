package com.example.da1androidnative.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.da1androidnative.data.model.ReservaDetalleResponse;
import com.example.da1androidnative.data.model.ReservaResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class OfflineReservationStorage {
    private static final String PREF_NAME = "offline_reservations_pref";
    private static final String KEY_RESERVATIONS = "saved_reservations";
    private static final String KEY_DETAILS = "saved_details";
    private static final String KEY_PENDING_CANCELLATIONS = "pending_cancellations";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    @Inject
    public OfflineReservationStorage(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // Guarda una reserva individual en la lista resumen
    public void saveReservation(ReservaResponse reservation) {
        if (reservation == null) return;
        List<ReservaResponse> currentReservations = getSavedReservations();
        
        // Protección: Si ya está completada localmente, mantener ese estado
        ReservaDetalleResponse detail = getSavedReservationDetail(reservation.getReservationId());
        if (detail != null && "COMPLETED".equalsIgnoreCase(detail.getStatus())
                && !"COMPLETED".equalsIgnoreCase(reservation.getStatus())) {
            reservation.setStatus("COMPLETED");
        }

        currentReservations.removeIf(r -> r.getReservationId() == reservation.getReservationId());
        currentReservations.add(reservation);
        updateReservations(currentReservations);
    }

    // Guarda o actualiza la lista completa de reservas (resumen)
    public void updateReservations(List<ReservaResponse> reservations) {
        if (reservations == null) reservations = new ArrayList<>();
        
        Map<Long, ReservaDetalleResponse> details = getSavedDetailsMap();
        for (ReservaResponse r : reservations) {
            ReservaDetalleResponse existing = details.get(r.getReservationId());
            if (existing != null && "COMPLETED".equalsIgnoreCase(existing.getStatus())
                    && !"COMPLETED".equalsIgnoreCase(r.getStatus())) {
                r.setStatus("COMPLETED");
            }
        }

        String json = gson.toJson(reservations);
        sharedPreferences.edit().putString(KEY_RESERVATIONS, json).apply();
    }

    // Obtiene la lista guardada de reservas
    public List<ReservaResponse> getSavedReservations() {
        String json = sharedPreferences.getString(KEY_RESERVATIONS, null);
        if (json == null) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<ReservaResponse>>() {}.getType();
            List<ReservaResponse> reservations = gson.fromJson(json, type);
            return reservations != null ? reservations : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Elimina una reserva específica del cache
    public void clearReservation(long id) {
        List<ReservaResponse> currentReservations = getSavedReservations();
        currentReservations.removeIf(r -> r.getReservationId() == id);
        updateReservations(currentReservations);
        
        Map<Long, ReservaDetalleResponse> details = getSavedDetailsMap();
        details.remove(id);
        saveDetailsMap(details);
    }

    // Guarda el detalle completo de una reserva específica
    public void saveReservationDetail(ReservaDetalleResponse detail) {
        if (detail == null) return;
        Map<Long, ReservaDetalleResponse> details = getSavedDetailsMap();
        
        // Protección: No permitir que el servidor revierta el estado COMPLETED local
        ReservaDetalleResponse existing = details.get(detail.getReservationId());
        if (existing != null && "COMPLETED".equalsIgnoreCase(existing.getStatus())
                && !"COMPLETED".equalsIgnoreCase(detail.getStatus())) {
            detail.setStatus("COMPLETED");
        }

        details.put(detail.getReservationId(), detail);
        saveDetailsMap(details);
    }

    // Obtiene el detalle guardado de una reserva
    public ReservaDetalleResponse getSavedReservationDetail(long reservationId) {
        return getSavedDetailsMap().get(reservationId);
    }

    public ReservaDetalleResponse getSavedReservationDetailOrSummary(long reservationId) {
        ReservaDetalleResponse detail = getSavedReservationDetail(reservationId);
        if (detail != null) return detail;

        for (ReservaResponse reservation : getSavedReservations()) {
            if (reservation.getReservationId() == reservationId) {
                return ReservaDetalleResponse.fromSummary(reservation);
            }
        }
        return null;
    }

    public void updateReservationStatus(long reservationId, String status) {
        List<ReservaResponse> reservations = getSavedReservations();
        for (ReservaResponse reservation : reservations) {
            if (reservation.getReservationId() == reservationId) {
                reservation.setStatus(status);
                break;
            }
        }
        updateReservations(reservations);

        ReservaDetalleResponse detail = getSavedReservationDetail(reservationId);
        if (detail != null) {
            detail.setStatus(status);
            saveReservationDetail(detail);
        }
    }

    public void addPendingCancellation(long reservationId) {
        Set<String> pending = sharedPreferences.getStringSet(KEY_PENDING_CANCELLATIONS, new HashSet<>());
        Set<String> newPending = new HashSet<>(pending);
        newPending.add(String.valueOf(reservationId));
        sharedPreferences.edit().putStringSet(KEY_PENDING_CANCELLATIONS, newPending).apply();
    }

    public List<Long> getPendingCancellations() {
        Set<String> pending = sharedPreferences.getStringSet(KEY_PENDING_CANCELLATIONS, new HashSet<>());
        List<Long> result = new ArrayList<>();
        for (String id : pending) {
            result.add(Long.parseLong(id));
        }
        return result;
    }

    public void removePendingCancellation(long reservationId) {
        Set<String> pending = sharedPreferences.getStringSet(KEY_PENDING_CANCELLATIONS, new HashSet<>());
        Set<String> newPending = new HashSet<>(pending);
        newPending.remove(String.valueOf(reservationId));
        sharedPreferences.edit().putStringSet(KEY_PENDING_CANCELLATIONS, newPending).apply();
    }

    private Map<Long, ReservaDetalleResponse> getSavedDetailsMap() {
        String json = sharedPreferences.getString(KEY_DETAILS, null);
        if (json == null) return new HashMap<>();
        try {
            Type type = new TypeToken<Map<Long, ReservaDetalleResponse>>() {}.getType();
            Map<Long, ReservaDetalleResponse> details = gson.fromJson(json, type);
            return details != null ? details : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void saveDetailsMap(Map<Long, ReservaDetalleResponse> map) {
        String json = gson.toJson(map);
        sharedPreferences.edit().putString(KEY_DETAILS, json).apply();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }
}
