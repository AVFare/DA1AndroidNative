package com.example.da1androidnative.data.network;

import com.example.da1androidnative.data.local.TokenManager;
import java.io.IOException;
import javax.inject.Inject;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    @Inject
    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder requestBuilder = request.newBuilder();

        // NO agregamos el token si la ruta es de autenticacion (login, register, otp)
        // Esto evita errores 403 por tokens viejos persistidos en el dispositivo
        if (!request.url().encodedPath().contains("/auth/")) {
            String token = tokenManager.getToken();
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }
        }

        return chain.proceed(requestBuilder.build());
    }
}