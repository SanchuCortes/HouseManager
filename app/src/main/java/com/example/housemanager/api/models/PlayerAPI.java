package com.example.housemanager.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo que representa un jugador según la estructura de la API externa.
 * También se utiliza como modelo de transferencia en algunas partes de la aplicación
 * donde se necesita una representación simplificada del jugador.
 */
public class PlayerAPI {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("position")
    private String position;

    @SerializedName("nationality")
    private String nationality;

    // Campo para puntos que se calcula internamente
    private int points;

    // Constructor vacío necesario para deserialización
    public PlayerAPI() {
    }

    // Constructor para uso interno con todos los campos
    public PlayerAPI(int id, String name, String position, String nationality, int points) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.nationality = nationality;
        this.points = points;
    }

    // Constructor básico sin puntos
    public PlayerAPI(int id, String name, String position, String nationality) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.nationality = nationality;
        this.points = 0;
    }

    // Métodos getter
    public int getId() {
        return id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getPosition() {
        return position != null ? position : "";
    }

    public String getNationality() {
        return nationality != null ? nationality : "";
    }

    public int getPoints() {
        return points;
    }

    // Métodos setter
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "PlayerAPI{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", position='" + position + '\'' +
                ", nationality='" + nationality + '\'' +
                ", points=" + points +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerAPI playerAPI = (PlayerAPI) o;
        return id == playerAPI.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}