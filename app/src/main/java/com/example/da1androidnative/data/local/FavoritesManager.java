package com.example.da1androidnative.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.da1androidnative.data.model.ActivityResponse;
import com.example.da1androidnative.data.model.SavedActivityCheckItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FavoritesManager {
    private static final String PREF_NAME = "FavoritesPrefs";
    private static final String KEY_FAVORITES = "favorite_ids";
    private static final String KEY_SNAPSHOTS = "favorite_snapshots";
    private static final String KEY_UPDATE_MESSAGES = "favorite_update_messages";
    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    @Inject
    public FavoritesManager(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void toggleFavorite(Long activityId) {
        if (activityId == null) return;
        if (isFavorite(activityId)) {
            removeFavorite(activityId);
        } else {
            addFavoriteId(activityId);
        }
    }

    public boolean toggleFavorite(ActivityResponse activity) {
        if (activity == null) return false;
        long activityId = activity.getId();
        if (isFavorite(activityId)) {
            removeFavorite(activityId);
            return false;
        }
        addFavoriteId(activityId);
        saveSnapshot(activity);
        return true;
    }

    public void saveSnapshot(ActivityResponse activity) {
        if (activity == null) return;
        Map<String, FavoriteSnapshot> snapshots = getSnapshots();
        String id = String.valueOf(activity.getId());
        if (!snapshots.containsKey(id)) {
            snapshots.put(id, new FavoriteSnapshot(activity.getBasePrice(), activity.getAvailableSpots()));
            saveSnapshots(snapshots);
        }
    }

    public void applyBatchState(List<SavedActivityCheckItem> currentStates) {
        if (currentStates == null || currentStates.isEmpty()) return;

        Map<String, FavoriteSnapshot> snapshots = getSnapshots();
        Map<String, String> updateMessages = getUpdateMessages();

        for (SavedActivityCheckItem current : currentStates) {
            String id = String.valueOf(current.getActivityId());
            FavoriteSnapshot previous = snapshots.get(id);
            FavoriteSnapshot updated = new FavoriteSnapshot(current.getPrice(), current.getAvailableSpots());

            if (previous == null) {
                snapshots.put(id, updated);
                continue;
            }

            boolean priceChanged = previous.price != null
                    && current.getPrice() != null
                    && Double.compare(previous.price, current.getPrice()) != 0;
            boolean spotsIncreased = previous.availableSpots != null
                    && current.getAvailableSpots() != null
                    && current.getAvailableSpots() > previous.availableSpots;

            if (priceChanged || spotsIncreased) {
                updateMessages.put(id, buildUpdateMessage(priceChanged, spotsIncreased));
                snapshots.put(id, updated);
            }
        }

        saveSnapshots(snapshots);
        saveUpdateMessages(updateMessages);
    }

    public boolean isFavorite(Long activityId) {
        return getFavoriteIds().contains(String.valueOf(activityId));
    }

    public String getUpdateMessage(Long activityId) {
        if (activityId == null) return null;
        return getUpdateMessages().get(String.valueOf(activityId));
    }

    public Set<String> getFavoriteIds() {
        return new HashSet<>(sharedPreferences.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }

    public String getFavoriteIdsCsv() {
        return String.join(",", getFavoriteIds());
    }

    private void addFavoriteId(Long activityId) {
        Set<String> favorites = getFavoriteIds();
        favorites.add(String.valueOf(activityId));
        sharedPreferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    private void removeFavorite(Long activityId) {
        String id = String.valueOf(activityId);
        Set<String> favorites = getFavoriteIds();
        Map<String, FavoriteSnapshot> snapshots = getSnapshots();
        Map<String, String> updateMessages = getUpdateMessages();

        favorites.remove(id);
        snapshots.remove(id);
        updateMessages.remove(id);

        sharedPreferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
        saveSnapshots(snapshots);
        saveUpdateMessages(updateMessages);
    }

    private Map<String, FavoriteSnapshot> getSnapshots() {
        String json = sharedPreferences.getString(KEY_SNAPSHOTS, "{}");
        Type type = new TypeToken<Map<String, FavoriteSnapshot>>() {}.getType();
        Map<String, FavoriteSnapshot> snapshots = gson.fromJson(json, type);
        return snapshots != null ? snapshots : new HashMap<>();
    }

    private void saveSnapshots(Map<String, FavoriteSnapshot> snapshots) {
        sharedPreferences.edit().putString(KEY_SNAPSHOTS, gson.toJson(snapshots)).apply();
    }

    private Map<String, String> getUpdateMessages() {
        String json = sharedPreferences.getString(KEY_UPDATE_MESSAGES, "{}");
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> updateMessages = gson.fromJson(json, type);
        return updateMessages != null ? updateMessages : new HashMap<>();
    }

    private void saveUpdateMessages(Map<String, String> updateMessages) {
        sharedPreferences.edit().putString(KEY_UPDATE_MESSAGES, gson.toJson(updateMessages)).apply();
    }

    private String buildUpdateMessage(boolean priceChanged, boolean spotsIncreased) {
        if (priceChanged && spotsIncreased) return "¡Precio y cupos nuevos!";
        if (priceChanged) return "¡Precio nuevo!";
        return "¡Cupos nuevos!";
    }

    private static class FavoriteSnapshot {
        Double price;
        Integer availableSpots;

        FavoriteSnapshot(Double price, Integer availableSpots) {
            this.price = price;
            this.availableSpots = availableSpots;
        }
    }
}
