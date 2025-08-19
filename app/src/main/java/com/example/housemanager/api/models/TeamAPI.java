package com.example.housemanager.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TeamAPI {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    // URL del escudo (png/svg) según football-data v4
    @SerializedName("crest")
    private String crest;

    // Algunas respuestas traen la plantilla como "squad"
    @SerializedName("squad")
    private List<PlayerAPI> squad;

    public TeamAPI() {}

    public TeamAPI(int id, String name, String crest) {
        this.id = id;
        this.name = name;
        this.crest = crest;
    }

    public int getId() {
        return id;
    }

    public TeamAPI setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TeamAPI setName(String name) {
        this.name = name;
        return this;
    }

    public String getCrest() {
        return crest;
    }

    public TeamAPI setCrest(String crest) {
        this.crest = crest;
        return this;
    }

    public List<PlayerAPI> getSquad() {
        return squad;
    }

    public TeamAPI setSquad(List<PlayerAPI> squad) {
        this.squad = squad;
        return this;
    }
}
