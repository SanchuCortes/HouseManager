package com.example.housemanager.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TeamsResponse {
    @SerializedName("teams")
    private List<TeamAPI> teams;

    public List<TeamAPI> getTeams() { return teams; }
}
