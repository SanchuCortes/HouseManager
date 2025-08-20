package com.example.housemanager.api;

import static com.example.housemanager.BuildConfig.FOOTBALL_API_KEY;

import android.util.Log;

import com.example.housemanager.api.FootballApiService;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente API para conectar con football-data.org
 * Maneja la autenticación y configuración de Retrofit
 */
public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://api.football-data.org/v4/";

    private static Retrofit retrofit;

    /**
     * Construye el cliente HTTP con interceptor de autenticación
     */
    private static OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();

                        // Añadir header de autenticación si tenemos API key
                        if (FOOTBALL_API_KEY != null && !FOOTBALL_API_KEY.isEmpty()) {
                            builder.addHeader("X-Auth-Token", FOOTBALL_API_KEY);
                            Log.d(TAG, "Header de autenticación añadido a la petición");
                        } else {
                            Log.w(TAG, "API Key no configurada - las peticiones podrían fallar");
                        }

                        return chain.proceed(builder.build());
                    }
                })
                .build();
    }

    /**
     * Obtiene la instancia configurada de Retrofit
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(buildClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Log.d(TAG, "Cliente Retrofit inicializado con base URL: " + BASE_URL);
        }
        return retrofit;
    }

    /**
     * Obtiene el servicio de Football API configurado
     */
    public static FootballApiService getFootballService() {
        return getClient().create(FootballApiService.class);
    }
}