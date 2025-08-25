package com.example.housemanager.api.models;

/**
 * Tarjeta mostrada a un jugador según
 */
public class BookingAPI {
    private String card; // YELLOW | YELLOW_RED | RED
    private SimplePlayer player;

    public BookingAPI() {}

    public String getCard() { return card; }
    public void setCard(String card) { this.card = card; }

    public SimplePlayer getPlayer() { return player; }
    public void setPlayer(SimplePlayer player) { this.player = player; }
}
