package com.example.housemanager.api.models;

/**
 * Evento de partido: goles, tarjetas, etc.
 */
public class MatchEventAPI {
    private String type; // e.g., "GOAL", "YELLOW_CARD", "RED_CARD"
    private SimplePlayer player;

    public MatchEventAPI() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public SimplePlayer getPlayer() { return player; }
    public void setPlayer(SimplePlayer player) { this.player = player; }
}
