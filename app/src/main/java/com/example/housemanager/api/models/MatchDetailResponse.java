package com.example.housemanager.api.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Detalle de partido que puede incluir alineaciones y eventos.
 * Estructura mínima para acumular puntos más adelante.
 */
public class MatchDetailResponse {
    private MatchAPI match; // debe incluir id, utcDate, status, equipos, score

    // Si el plan lo permite, football-data expone alineaciones y eventos.
    private Lineup homeTeamLineup;
    private Lineup awayTeamLineup;
    private List<MatchEventAPI> events = new ArrayList<>();

    public MatchDetailResponse() {}

    public MatchAPI getMatch() { return match; }
    public void setMatch(MatchAPI match) { this.match = match; }

    public Lineup getHomeTeamLineup() { return homeTeamLineup; }
    public void setHomeTeamLineup(Lineup homeTeamLineup) { this.homeTeamLineup = homeTeamLineup; }

    public Lineup getAwayTeamLineup() { return awayTeamLineup; }
    public void setAwayTeamLineup(Lineup awayTeamLineup) { this.awayTeamLineup = awayTeamLineup; }

    public List<MatchEventAPI> getEvents() { return events; }
    public void setEvents(List<MatchEventAPI> events) { this.events = events != null ? events : new ArrayList<>(); }
}
