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
    // v4 breakdown (opcionales): goles y tarjetas
    private List<GoalAPI> goals = new ArrayList<>();
    private List<BookingAPI> bookings = new ArrayList<>();
    // Fallback genérico si la API del plan devuelve un array plano de eventos
    private List<MatchEventAPI> events = new ArrayList<>();

    public MatchDetailResponse() {}

    public MatchAPI getMatch() { return match; }
    public void setMatch(MatchAPI match) { this.match = match; }

    public Lineup getHomeTeamLineup() { return homeTeamLineup; }
    public void setHomeTeamLineup(Lineup homeTeamLineup) { this.homeTeamLineup = homeTeamLineup; }

    public Lineup getAwayTeamLineup() { return awayTeamLineup; }
    public void setAwayTeamLineup(Lineup awayTeamLineup) { this.awayTeamLineup = awayTeamLineup; }

    public List<GoalAPI> getGoals() { return goals; }
    public void setGoals(List<GoalAPI> goals) { this.goals = goals != null ? goals : new ArrayList<>(); }

    public List<BookingAPI> getBookings() { return bookings; }
    public void setBookings(List<BookingAPI> bookings) { this.bookings = bookings != null ? bookings : new ArrayList<>(); }

    public List<MatchEventAPI> getEvents() { return events; }
    public void setEvents(List<MatchEventAPI> events) { this.events = events != null ? events : new ArrayList<>(); }
}
