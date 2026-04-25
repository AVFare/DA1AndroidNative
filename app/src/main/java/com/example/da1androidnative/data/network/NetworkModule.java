package com.example.da1androidnative.data.network;

import android.os.Build;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        String baseUrl;
        // Detecta si es un emulador para usar 10.0.2.2 o dispositivo fisico para usar la IP de la PC
        if (Build.FINGERPRINT.contains("generic") 
            || Build.FINGERPRINT.contains("unknown") 
            || Build.MODEL.contains("google_sdk") 
            || Build.MODEL.contains("Emulator") 
            || Build.MODEL.contains("Android SDK built for x86") 
            || Build.MANUFACTURER.contains("Genymotion") 
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) 
            || "google_sdk".equals(Build.PRODUCT)) {
            
            baseUrl = "http://10.0.2.2:8080/api/v1/";
        } else {
            baseUrl = "http://192.168.0.209:8080/api/v1/";
        }

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public static ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}