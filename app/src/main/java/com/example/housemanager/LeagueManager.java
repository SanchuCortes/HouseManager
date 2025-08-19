package com.example.housemanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LeagueManager {

    private static LeagueManager INSTANCE;
    public static LeagueManager getInstance() {
        if (INSTANCE == null) INSTANCE = new LeagueManager();
        return INSTANCE;
    }

    private final List<League> leagues = new ArrayList<>();

    public List<League> getLeagues() { return leagues; }

    // Necesario para LeaguesActivity
    public List<League> getUserLeagues() { return leagues; }

    public League createLeague(String name, String type, String marketHour, String teamType, boolean isPrivate) {
        League l = new League(name, type, isPrivate, 200, marketHour, teamType, 1, "Activa", "01/01/2025");
        leagues.add(l);
        return l;
    }

    public static class League implements Serializable {
        private final String name;
        private final String type;
        private final boolean isPrivate;
        private final int budget;
        private final String marketHour;
        private final String teamType;
        private final int participants;
        private final String status;
        private final String createdDate;

        public League(String name, String type, boolean isPrivate, int budget, String marketHour,
                      String teamType, int participants, String status, String createdDate) {
            this.name = name;
            this.type = type;
            this.isPrivate = isPrivate;
            this.budget = budget;
            this.marketHour = marketHour;
            this.teamType = teamType;
            this.participants = participants;
            this.status = status;
            this.createdDate = createdDate;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public boolean isPrivate() { return isPrivate; }
        public int getBudget() { return budget; }
        public String getMarketHour() { return marketHour; }
        public String getTeamType() { return teamType; }
        public int getParticipants() { return participants; }
        public String getStatus() { return status; }
        public String getCreatedDate() { return createdDate; }
    }
}
