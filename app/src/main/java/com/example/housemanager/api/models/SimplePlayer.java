package com.example.housemanager.api.models;

/**
 * Referencia simple a un jugador (id y nombre) usada en lineups/eventos.
 */
public class SimplePlayer {
    private int id;
    private String name;

    public SimplePlayer() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
