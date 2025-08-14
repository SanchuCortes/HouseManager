package com.example.housemanager.api;

import com.example.housemanager.api.models.TeamsResponse;
import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.api.models.CompetitionAPI;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Servicio Retrofit para la API de Football-Data.org
 */
public interface FootballApiService {

    // ID de La Liga española en football-data.org
    int LA_LIGA_ID = 2014;

    /**
     * Obtiene todos los equipos de La Liga Española
     */
    @GET("competitions/" + LA_LIGA_ID + "/teams")
    Call<TeamsResponse> getLaLigaTeams();

    /**
     * Obtiene los detalles de un equipo específico incluyendo su plantilla
     */
    @GET("teams/{teamId}")
    Call<TeamAPI> getTeamDetails(@Path("teamId") int teamId);

    /**
     * Método simple para probar la conectividad de la API
     */
    @GET("competitions/" + LA_LIGA_ID)
    Call<CompetitionAPI> testApiConnection();
}