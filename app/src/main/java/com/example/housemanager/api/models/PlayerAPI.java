package com.example.housemanager.api.models;

import com.google.gson.annotations.SerializedName;

public class PlayerAPI {
    @SerializedName("id")          private int id;
    @SerializedName("name")        private String name;
    @SerializedName("nationality") private String nationality;
    // En football-data.org puede venir como "position" o "position" dentro de role,
    // para simplificar dejamos "position":
    @SerializedName("position")    private String position;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getNationality() { return nationality; }
    public String getPosition() { return position; }
}
