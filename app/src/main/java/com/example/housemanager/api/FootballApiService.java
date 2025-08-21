package com.example.housemanager.api;

import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.api.models.TeamsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/** Endpoints necesarios para equipos y plantillas. */
public interface FootballApiService {

    /** Lista de equipos de LaLiga. */
    @GET("competitions/PD/teams")
    Call<TeamsResponse> getLaLigaTeams();

    /** Detalle de equipo con plantilla. */
    @GET("teams/{id}")
    Call<TeamAPI> getTeamDetails(@Path("id") int teamId);
}
