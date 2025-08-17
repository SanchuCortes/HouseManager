package com.example.housemanager.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TeamAPI {
    @SerializedName("id")    private int id;
    @SerializedName("name")  private String name;
    @SerializedName("crest") private String crest; // en v4 se llama "crest"
    @SerializedName("squad") private List<PlayerAPI> squad; // cuando llamas /teams/{id}

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCrest() { return crest; }
    public List<PlayerAPI> getSquad() { return squad; }
}
