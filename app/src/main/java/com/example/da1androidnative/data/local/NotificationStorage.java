package com.example.da1androidnative.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.da1androidnative.data.model.Notification;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class NotificationStorage {
    private static final String PREF_NAME = "notifications_pref";
    private static final String KEY_NOTIFS = "saved_notifications";
    private final SharedPreferences prefs;
    private final Gson gson;

    @Inject
    public NotificationStorage(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public synchronized void saveNotification(Notification novedad) {
        if (novedad == null || novedad.getId() == null) return;
        List<Notification> list = getNotifications();
        
        for (Notification n : list) {
            if (novedad.getId().equals(n.getId())) return;
        }
        
        list.add(0, novedad); // la mas reciente es la ultima
        saveList(list);
    }

    public synchronized List<Notification> getNotifications() {
        String json = prefs.getString(KEY_NOTIFS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Notification>>() {}.getType();
        try {
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public synchronized void removeNotification(Long id) {
        List<Notification> list = getNotifications();
        list.removeIf(n -> n.getId().equals(id));
        saveList(list);
    }

    private void saveList(List<Notification> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_NOTIFS, json).apply();
    }
}
