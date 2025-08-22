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

    public League createLeague(android.content.Context context, String name, String typeOrBudget, String marketHour, String teamType, boolean isPrivate) {
        // Interpretar el segundo parámetro como presupuesto (así se usaba en LeagueConfigActivity)
        int budget = parseBudgetMillions(typeOrBudget);
        String typeText = isPrivate ? "Privada" : "Comunitaria";
        League l = new League(name, typeText, isPrivate, budget, marketHour, teamType, 1, "Activa", currentDate());
        leagues.add(l);

        // Guardar en Room en un hilo de BD
        com.example.housemanager.database.HouseManagerDatabase db = com.example.housemanager.database.HouseManagerDatabase.getInstance(context.getApplicationContext());
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            com.example.housemanager.database.entities.LeagueEntity e = new com.example.housemanager.database.entities.LeagueEntity();
            e.setName(name);
            e.setType(typeText);
            e.setBudget(budget);
            e.setMarketHour(marketHour);
            e.setTeamType(teamType);
            e.setParticipants(1);
            e.setStatus("Activa");
            e.setCreatedDate(currentDate());
            db.leagueDao().insertLeague(e);
        });
        return l;
    }

    private int parseBudgetMillions(String text) {
        if (text == null) return 150;
        String digits = text.replaceAll("[^0-9]", "");
        try { return Integer.parseInt(digits); } catch (Exception ignored) { return 150; }
    }

    private String currentDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
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
