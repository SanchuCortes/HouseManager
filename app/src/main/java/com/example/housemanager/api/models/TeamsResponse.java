package com.example.housemanager.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TeamsResponse {

    @SerializedName("teams")
    private List<TeamAPI> teams;

    public TeamsResponse() {}

    public List<TeamAPI> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamAPI> teams) {
        this.teams = teams;
    }
}
