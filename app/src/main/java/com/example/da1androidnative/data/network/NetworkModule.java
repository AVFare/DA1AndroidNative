package com.example.da1androidnative.data.network;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public static ApiService provideApiService() {
        return new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/api/v1/") // 10.0.2.2 es el localhost para el emulador
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }
}