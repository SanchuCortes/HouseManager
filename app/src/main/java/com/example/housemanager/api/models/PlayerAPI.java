package com.example.housemanager.api.models;

import com.google.gson.annotations.SerializedName;

// Esta clase es para recibir datos de la API, nada más
public class PlayerAPI {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("position")
    private String position;

    @SerializedName("nationality")
    private String nationality;

    // este campo no viene de la API, lo calculamos nosotros
    private int points;

    // constructor vacío para Gson
    public PlayerAPI() { }

    // constructor simple para cuando creamos uno manualmente
    public PlayerAPI(int id, String name, String position, String nationality, int points) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.nationality = nationality;
        this.points = points;
    }

    // getters simples
    public int getId() { return id; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public String getNationality() { return nationality; }
    public int getPoints() { return points; }

    // setters simples
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPosition(String position) { this.position = position; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public void setPoints(int points) { this.points = points; }
}