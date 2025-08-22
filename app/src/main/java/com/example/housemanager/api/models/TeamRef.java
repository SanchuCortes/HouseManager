package com.example.housemanager.api.models;

/**
 * Referencia simple a un equipo (id y nombre) usada en endpoints de partidos.
 */
public class TeamRef {
    private long id;
    private String name;

    public TeamRef() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
