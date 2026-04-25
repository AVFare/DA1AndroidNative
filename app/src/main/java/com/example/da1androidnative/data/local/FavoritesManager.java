package com.example.da1androidnative.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FavoritesManager {
    private static final String PREF_NAME = "FavoritesPrefs";
    private static final String KEY_FAVORITES = "favorite_ids";
    private final SharedPreferences sharedPreferences;

    @Inject
    public FavoritesManager(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void toggleFavorite(Long activityId) {
        Set<String> favorites = getFavoriteIds();
        String idStr = String.valueOf(activityId);
        if (favorites.contains(idStr)) {
            favorites.remove(idStr);
        } else {
            favorites.add(idStr);
        }
        sharedPreferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public boolean isFavorite(Long activityId) {
        return getFavoriteIds().contains(String.valueOf(activityId));
    }

    public Set<String> getFavoriteIds() {
        return new HashSet<>(sharedPreferences.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }
}