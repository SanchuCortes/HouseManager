package com.example.housemanager.api;

import com.example.housemanager.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Cliente para configurar Retrofit con la API de Football-Data.org
 */
public class ApiClient {

    private static final String BASE_URL = "https://api.football-data.org/v4/";
    private static Retrofit retrofit = null;
    private static FootballApiService apiService = null;

    /**
     * Obtiene la instancia de Retrofit configurada
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Interceptor para logs (solo en debug)
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            if (BuildConfig.DEBUG) {
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            } else {
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
            }

            // Interceptor para añadir API Key automáticamente
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();

                    // Añadir header de autenticación
                    Request.Builder requestBuilder = originalRequest.newBuilder()
                            .addHeader("X-Auth-Token", BuildConfig.FOOTBALL_API_KEY)
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json");

                    Request newRequest = requestBuilder.build();
                    return chain.proceed(newRequest);
                }
            };

            // Configurar cliente HTTP
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Crear instancia de Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Obtiene el servicio de la API de Football
     */
    public static FootballApiService getFootballApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(FootballApiService.class);
        }
        return apiService;
    }

    /**
     * Método de conveniencia para realizar primera llamada de prueba
     */
    public static void testApiConnection(ApiCallback callback) {
        FootballApiService service = getFootballApiService();

        retrofit2.Call<com.example.housemanager.api.models.TeamsResponse> call = service.getLaLigaTeams();
        call.enqueue(new retrofit2.Callback<com.example.housemanager.api.models.TeamsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.housemanager.api.models.TeamsResponse> call,
                                   retrofit2.Response<com.example.housemanager.api.models.TeamsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess("API conectada correctamente. Equipos encontrados: " +
                            response.body().getTeams().size());
                } else {
                    callback.onError("Error en la respuesta: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.housemanager.api.models.TeamsResponse> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Interface para callbacks de prueba de API
     */
    public interface ApiCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}