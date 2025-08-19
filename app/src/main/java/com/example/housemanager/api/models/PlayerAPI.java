package com.example.housemanager.api.models;

import androidx.room.Ignore;
import com.google.gson.annotations.SerializedName;

public class PlayerAPI {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("position")
    private String position;

    @SerializedName("nationality")
    private String nationality;

    private int points;

    public PlayerAPI() { }

    @Ignore
    public PlayerAPI(int id, String name, String position, String nationality, int points) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.nationality = nationality;
        this.points = points;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
