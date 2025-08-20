package com.example.housemanager.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TeamAPI {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("shortName")
    private String shortName;

    @SerializedName("tla")
    private String tla;

    @SerializedName("crest")
    private String crest;

    @SerializedName("address")
    private String address;

    @SerializedName("website")
    private String website;

    @SerializedName("founded")
    private Integer founded;

    @SerializedName("clubColors")
    private String clubColors;

    @SerializedName("venue")
    private String venue;

    @SerializedName("squad")
    private List<PlayerAPI> squad;

    public TeamAPI() {}

    public TeamAPI(int id, String name, String crest) {
        this.id = id;
        this.name = name;
        this.crest = crest;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getTla() {
        return tla;
    }

    public String getCrest() {
        return crest;
    }

    public String getAddress() {
        return address;
    }

    public String getWebsite() {
        return website;
    }

    public Integer getFounded() {
        return founded;
    }

    public String getClubColors() {
        return clubColors;
    }

    public String getVenue() {
        return venue;
    }

    public List<PlayerAPI> getSquad() {
        return squad;
    }

    // Setters
    public TeamAPI setId(int id) {
        this.id = id;
        return this;
    }

    public TeamAPI setName(String name) {
        this.name = name;
        return this;
    }

    public TeamAPI setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public TeamAPI setTla(String tla) {
        this.tla = tla;
        return this;
    }

    public TeamAPI setCrest(String crest) {
        this.crest = crest;
        return this;
    }

    public TeamAPI setAddress(String address) {
        this.address = address;
        return this;
    }

    public TeamAPI setWebsite(String website) {
        this.website = website;
        return this;
    }

    public TeamAPI setFounded(Integer founded) {
        this.founded = founded;
        return this;
    }

    public TeamAPI setClubColors(String clubColors) {
        this.clubColors = clubColors;
        return this;
    }

    public TeamAPI setVenue(String venue) {
        this.venue = venue;
        return this;
    }

    public TeamAPI setSquad(List<PlayerAPI> squad) {
        this.squad = squad;
        return this;
    }

    @Override
    public String toString() {
        return "TeamAPI{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", crest='" + crest + '\'' +
                '}';
    }
}