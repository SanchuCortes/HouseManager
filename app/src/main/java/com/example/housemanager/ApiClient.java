package com.example.housemanager;

import static com.example.housemanager.BuildConfig.FOOTBALL_API_KEY;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Configuro Retrofit con el header de auth.
public class ApiClient {

    // Base oficial de football-data.org v4
    private static final String BASE_URL = "https://api.football-data.org/v4/";

    private static Retrofit retrofit;

    // Interceptor para añadir el token en cada petición
    private static OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();
                        if (FOOTBALL_API_KEY != null && !FOOTBALL_API_KEY.isEmpty()) {
                            builder.addHeader("X-Auth-Token", FOOTBALL_API_KEY);
                        }
                        return chain.proceed(builder.build());
                    }
                })
                .build();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(buildClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
