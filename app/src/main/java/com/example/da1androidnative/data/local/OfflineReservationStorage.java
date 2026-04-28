package com.example.da1androidnative.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.da1androidnative.data.model.ReservaResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class OfflineReservationStorage {
    private static final String PREF_NAME = "offline_reservations_pref";
    private static final String KEY_RESERVATIONS = "saved_reservations";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    @Inject
    public OfflineReservationStorage(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveReservation(ReservaResponse reservation) {
        List<ReservaResponse> currentReservations = getSavedReservations();
        // Evitar duplicados por ID
        currentReservations.removeIf(r -> r.getReservationId() == reservation.getReservationId());
        currentReservations.add(reservation);
        saveList(currentReservations);
    }

    public List<ReservaResponse> getSavedReservations() {
        String json = sharedPreferences.getString(KEY_RESERVATIONS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<List<ReservaResponse>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void updateReservations(List<ReservaResponse> reservations) {
        saveList(reservations);
    }

    public void clearReservation(long id) {
        List<ReservaResponse> currentReservations = getSavedReservations();
        currentReservations.removeIf(r -> r.getReservationId() == id);
        saveList(currentReservations);
    }

    private void saveList(List<ReservaResponse> list) {
        String json = gson.toJson(list);
        sharedPreferences.edit().putString(KEY_RESERVATIONS, json).apply();
    }
}
