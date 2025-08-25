package com.example.housemanager.api;

import com.example.housemanager.api.models.CompetitionAPI;
import com.example.housemanager.api.models.CompetitionDetails;
import com.example.housemanager.api.models.MatchDetailResponse;
import com.example.housemanager.api.models.MatchesResponse;
import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.api.models.TeamsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Endpoints para consultar equipos, plantillas y partidos sin rodeos. */
public interface FootballApiService {

    /** Info de la competición PD (incluye current season y currentMatchday). */
    @GET("competitions/PD")
    Call<CompetitionAPI> getCompetition();

    /** Alternativa simple para leer detalles de la competición (estructura básica). */
    @GET("competitions/PD")
    Call<CompetitionDetails> getCompetitionDetails();

    /** Lista de equipos de LaLiga. */
    @GET("competitions/PD/teams")
    Call<TeamsResponse> getLaLigaTeams();

    /** Detalle de equipo con plantilla. */
    @GET("teams/{id}")
    Call<TeamAPI> getTeamDetails(@Path("id") int teamId);

    /** Partidos entre dos fechas (formato ISO yyyy-MM-dd). */
    @GET("competitions/PD/matches")
    Call<MatchesResponse> getMatches(
            @Query("dateFrom") String dateFromIso,
            @Query("dateTo") String dateToIso
    );

    /** Partidos filtrados por jornada. */
    @GET("competitions/PD/matches")
    Call<MatchesResponse> getMatchesByMatchday(@Query("matchday") Integer matchday);

    /** Detalle de un partido */
    @GET("matches/{matchId}")
    Call<MatchDetailResponse> getMatchDetail(@Path("matchId") long matchId);
}
