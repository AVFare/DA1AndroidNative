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

    private String getUserKey(long userId) {
        return KEY_NOTIFS + userId;
    }

    public synchronized boolean saveNotification(long userId, Notification notification) {
        if (notification == null || notification.getId() == null) return false;
        if (userId == -1) return false;

        List<Notification> list = getNotifications(userId);
        
        for (Notification n : list) {
            if (notification.getId().equals(n.getId())) return false;
        }
        
        list.add(0, notification); // la mas reciente al principio
        saveList(userId,list);
        return true;
    }

    public synchronized List<Notification> getNotifications(long userId) {
        if (userId == -1) return new ArrayList<>();
        
        String json = prefs.getString(getUserKey(userId), null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Notification>>() {}.getType();
        try {
            List<Notification> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public synchronized void removeNotification(long userId,Long id) {
        if (id == null || userId == -1) return;
        List<Notification> list = getNotifications(userId);
        list.removeIf(n -> id.equals(n.getId()));
        saveList(userId,list);
    }

    public synchronized void clearAll(long userId) {
        prefs.edit().remove(getUserKey(userId)).apply();
    }

    private void saveList(long userId, List<Notification> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(getUserKey(userId), json).apply();
    }
}
