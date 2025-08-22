package com.example.housemanager.api.models;

/**
 * Entrada de alineación (titulares o suplentes) con el jugador asociado.
 */
public class LineupEntryAPI {
    private SimplePlayer player;

    public LineupEntryAPI() {}

    public SimplePlayer getPlayer() { return player; }
    public void setPlayer(SimplePlayer player) { this.player = player; }
}
