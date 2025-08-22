package com.example.housemanager.api.models;

/**
 * Resultado del partido con marcador a tiempo completo.
 */
public class ScoreAPI {
    private FullTime fullTime;

    public ScoreAPI() {}

    public FullTime getFullTime() { return fullTime; }
    public void setFullTime(FullTime fullTime) { this.fullTime = fullTime; }

    public static class FullTime {
        private Integer home; // puede ser null si no jugado
        private Integer away; // puede ser null si no jugado

        public FullTime() {}

        public Integer getHome() { return home; }
        public void setHome(Integer home) { this.home = home; }

        public Integer getAway() { return away; }
        public void setAway(Integer away) { this.away = away; }
    }
}
