package com.example.housemanager.api.models;

/**
 * Detalle de competición PD para obtener la jornada actual.
 * Estructura mínima compatible con football-data.org v4.
 */
public class CompetitionDetails {
    private CurrentSeason currentSeason;

    public CompetitionDetails() {}

    public CurrentSeason getCurrentSeason() { return currentSeason; }
    public void setCurrentSeason(CurrentSeason currentSeason) { this.currentSeason = currentSeason; }

    public static class CurrentSeason {
        private Integer currentMatchday;

        public CurrentSeason() {}

        public Integer getCurrentMatchday() { return currentMatchday; }
        public void setCurrentMatchday(Integer currentMatchday) { this.currentMatchday = currentMatchday; }
    }
}
