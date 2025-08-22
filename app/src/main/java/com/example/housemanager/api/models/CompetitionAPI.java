package com.example.housemanager.api.models;

/**
 * DTO de competición (v4). Debe incluir currentSeason con currentMatchday.
 */
public class CompetitionAPI {
    private int id;
    private String name;
    private String code; // PD, PL, etc.
    private SeasonAPI currentSeason;

    public CompetitionAPI() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public SeasonAPI getCurrentSeason() { return currentSeason; }
    public void setCurrentSeason(SeasonAPI currentSeason) { this.currentSeason = currentSeason; }
}
