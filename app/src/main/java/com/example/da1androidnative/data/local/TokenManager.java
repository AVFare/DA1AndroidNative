package com.example.da1androidnative.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.BackoffPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.da1androidnative.data.network.NotificationPollingWorker;

import java.util.concurrent.TimeUnit;

import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TokenManager {
    private static final String PREF_NAME = "XploreNowPrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";
    private final Context context;
    private final SharedPreferences sharedPreferences;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.context = context;
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void saveUserId(long userId) {
        sharedPreferences.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1L);
    }

    public void clearToken() {
        sharedPreferences.edit().remove(KEY_TOKEN).remove(KEY_USER_ID).apply();
    }

    public void setBiometricEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    public boolean isBiometricEnabled() {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void saveCredentials(String email, String password) {
        sharedPreferences.edit()
                .putString(KEY_SAVED_EMAIL, email)
                .putString(KEY_SAVED_PASSWORD, password)
                .apply();
        enableNotifications();
    }

    private void enableNotifications() {
        OneTimeWorkRequest inicial = new OneTimeWorkRequest.Builder(NotificationPollingWorker.class).setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build();

        WorkManager.getInstance(this.context).enqueueUniqueWork("notificationPolling", ExistingWorkPolicy.REPLACE, inicial);
    }

    private void disableNotifications(){
        WorkManager.getInstance(this.context).cancelUniqueWork("notificationPolling");
    }

    public String getSavedEmail() {
        return sharedPreferences.getString(KEY_SAVED_EMAIL, null);
    }

    public String getSavedPassword() {
        return sharedPreferences.getString(KEY_SAVED_PASSWORD, null);
    }

    public void clearCredentials() {
        sharedPreferences.edit()
                .remove(KEY_SAVED_EMAIL)
                .remove(KEY_SAVED_PASSWORD)
                .apply();
        disableNotifications();
    }
}