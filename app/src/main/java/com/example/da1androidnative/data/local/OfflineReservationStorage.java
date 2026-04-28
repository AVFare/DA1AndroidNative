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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class OfflineReservationStorage {
    private static final String PREF_NAME = "offline_reservations_pref";
    private static final String KEY_RESERVATIONS = "saved_reservations";
    private static final String KEY_DETAILS = "saved_details";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    @Inject
    public OfflineReservationStorage(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // Guarda o actualiza la lista completa de reservas (resumen)
    public void updateReservations(List<ReservaResponse> reservations) {
        String json = gson.toJson(reservations);
        sharedPreferences.edit().putString(KEY_RESERVATIONS, json).apply();
    }

    // Obtiene la lista guardada de reservas
    public List<ReservaResponse> getSavedReservations() {
        String json = sharedPreferences.getString(KEY_RESERVATIONS, null);
        if (json == null) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<ReservaResponse>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Guarda el detalle completo de una reserva específica
    public void saveReservationDetail(ReservaDetalleResponse detail) {
        Map<Long, ReservaDetalleResponse> details = getSavedDetailsMap();
        details.put(detail.getReservationId(), detail);
        saveDetailsMap(details);
    }

    // Obtiene el detalle guardado de una reserva
    public ReservaDetalleResponse getSavedReservationDetail(long reservationId) {
        return getSavedDetailsMap().get(reservationId);
    }

    private Map<Long, ReservaDetalleResponse> getSavedDetailsMap() {
        String json = sharedPreferences.getString(KEY_DETAILS, null);
        if (json == null) return new HashMap<>();
        try {
            Type type = new TypeToken<Map<Long, ReservaDetalleResponse>>() {}.getType();
            return gson.fromJson(json, type);
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
