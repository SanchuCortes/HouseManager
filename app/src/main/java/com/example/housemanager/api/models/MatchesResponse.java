package com.example.housemanager.api.models;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO raíz para la respuesta de /competitions/PD/matches
 */
public class MatchesResponse {
    private List<MatchAPI> matches = new ArrayList<>();

    public MatchesResponse() {}

    public List<MatchAPI> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchAPI> matches) {
        this.matches = matches != null ? matches : new ArrayList<>();
    }
}
