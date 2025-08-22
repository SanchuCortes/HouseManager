package com.example.housemanager.api.models;

/**
 * Temporada asociada a la competición/match.
 */
public class SeasonAPI {
    private long id;
    private String startDate; // ISO 8601 (yyyy-MM-dd)
    private String endDate;   // ISO 8601 (yyyy-MM-dd)
    private Integer currentMatchday;

    public SeasonAPI() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Integer getCurrentMatchday() { return currentMatchday; }
    public void setCurrentMatchday(Integer currentMatchday) { this.currentMatchday = currentMatchday; }
}
