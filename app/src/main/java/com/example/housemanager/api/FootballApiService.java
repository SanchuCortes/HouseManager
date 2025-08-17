package com.example.housemanager.api;

import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.api.models.TeamsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FootballApiService {
    // LaLiga (PD = Primera División en football-data.org)
    @GET("competitions/PD/teams")
    Call<TeamsResponse> getLaLigaTeams();

    @GET("teams/{id}")
    Call<TeamAPI> getTeamDetails(@Path("id") int teamId);
}
