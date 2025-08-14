package com.example.housemanager;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Fuente única de datos mock para no depender de Room/API en el MVP.
public class LeagueManager {

    // Singleton rápido para acceder desde Activities.
    private static LeagueManager INSTANCE;
    public static LeagueManager getInstance() {
        if (INSTANCE == null) INSTANCE = new LeagueManager();
        return INSTANCE;
    }

    // Modelo simple que cubre lo que muestran las pantallas.
    public static class League implements Serializable {
        private final String name;
        private final String type;        // "Privada" / "Comunitaria"
        private final boolean isPrivate;
        private final int budget;         // 100..200 (M€)
        private final String marketHour;  // "14:00"
        private final String teamType;    // "Equipo Vacío", "Aleatorio 150M", etc.
        private final int participants;   // nº usuarios
        private final String status;      // "Activa"…
        private final String createdDate; // "dd/MM/yyyy"

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

    private final List<League> leagues = new ArrayList<>();

    private LeagueManager() {
        // Arranco con 2 ligas de demo para no ver vacío.
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        leagues.add(new League("Liga Amigos", "Privada", true, 150, "14:00", "Equipo Vacío", 8, "Activa", today));
        leagues.add(new League("Liga Pro", "Comunitaria", false, 180, "12:00", "Aleatorio 150M", 16, "Activa", today));
    }

    // Devuelvo copia para evitar modificar la lista original sin querer.
    public List<League> getUserLeagues() { return new ArrayList<>(leagues); }

    // Por si quieres añadir desde la config en el futuro.
    public void addLeague(League league) { leagues.add(league); }
}
