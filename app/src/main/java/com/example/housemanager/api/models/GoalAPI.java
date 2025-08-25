package com.example.housemanager.api.models;

/**
 * Evento de gol con información de tipo y asistente según v4.
 */
public class GoalAPI {
    private String type; // REGULAR | PENALTY | OWN
    private SimplePlayer scorer; // jugador que marca
    private SimplePlayer assist; // puede ser null

    public GoalAPI() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public SimplePlayer getScorer() { return scorer; }
    public void setScorer(SimplePlayer scorer) { this.scorer = scorer; }

    public SimplePlayer getAssist() { return assist; }
    public void setAssist(SimplePlayer assist) { this.assist = assist; }
}
