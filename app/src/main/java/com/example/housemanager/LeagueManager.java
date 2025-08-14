package com.example.housemanager;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Gestor singleton para manejar las ligas del usuario
 * Simula una base de datos temporal hasta implementar Room
 */
public class LeagueManager {

    private static LeagueManager instance;
    private List<League> leaguesList;
    private SimpleDateFormat dateFormat;

    private LeagueManager() {
        leaguesList = new ArrayList<>();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Añadimos ligas de ejemplo para que no esté vacío
        addExampleLeagues();
    }

    public static LeagueManager getInstance() {
        if (instance == null) {
            instance = new LeagueManager();
        }
        return instance;
    }

    // Añade una nueva liga creada por el usuario
    public void addLeague(League league) {
        if (league != null) {
            // Asignar fecha actual
            league.setCreatedDate(dateFormat.format(new Date()));
            leaguesList.add(league);
        }
    }

    // Crea y guarda una liga desde los datos del formulario
    public League createLeague(String name, String budget, String marketHour,
                               String teamType, boolean isPrivate) {
        League newLeague = new League(
                name,
                isPrivate ? "Liga Privada" : "Liga Comunitaria",
                isPrivate,
                budget,
                "1/12 jugadores" // Empieza con 1 jugador (el creador)
        );

        // Configuración adicional
        newLeague.setMarketHour(marketHour);
        newLeague.setTeamType(teamType);
        newLeague.setStatus("Activa");

        addLeague(newLeague);
        return newLeague;
    }

    // Obtiene todas las ligas del usuario
    public List<League> getAllLeagues() {
        return new ArrayList<>(leaguesList);
    }

    // Busca una liga por nombre
    public League getLeagueByName(String name) {
        for (League league : leaguesList) {
            if (league.getName().equals(name)) {
                return league;
            }
        }
        return null;
    }

    // Obtiene el número total de ligas
    public int getLeaguesCount() {
        return leaguesList.size();
    }

    // Limpia todas las ligas (para testing)
    public void clearAllLeagues() {
        leaguesList.clear();
        addExampleLeagues();
    }

    // Añade ligas de ejemplo para demostración
    private void addExampleLeagues() {
        League example1 = new League(
                "Liga de Amigos",
                "Liga Privada",
                true,
                "150M €",
                "8/12 jugadores"
        );
        example1.setStatus("Activa");
        example1.setCreatedDate("15/01/2025");
        example1.setMarketHour("14:00");
        example1.setTeamType("Aleatorio 150M");
        leaguesList.add(example1);

        League example2 = new League(
                "Champions League",
                "Liga Comunitaria",
                false,
                "200M €",
                "24/50 jugadores"
        );
        example2.setStatus("Activa");
        example2.setCreatedDate("10/01/2025");
        example2.setMarketHour("21:00");
        example2.setTeamType("Aleatorio 200M");
        leaguesList.add(example2);
    }

    /**
     * Clase League mejorada para representar una liga
     */
    public static class League {
        private String name;
        private String type;
        private boolean isPrivate;
        private String budget;
        private String participants;
        private String status;
        private String createdDate;
        private String marketHour;
        private String teamType;

        public League(String name, String type, boolean isPrivate, String budget, String participants) {
            this.name = name;
            this.type = type;
            this.isPrivate = isPrivate;
            this.budget = budget;
            this.participants = participants;
            this.status = "Creando...";
            this.createdDate = "Hoy";
            this.marketHour = "14:00";
            this.teamType = "Equipo Vacío";
        }

        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public boolean isPrivate() { return isPrivate; }
        public String getBudget() { return budget; }
        public String getParticipants() { return participants; }
        public String getStatus() { return status; }
        public String getCreatedDate() { return createdDate; }
        public String getMarketHour() { return marketHour; }
        public String getTeamType() { return teamType; }

        // Setters
        public void setName(String name) { this.name = name; }
        public void setType(String type) { this.type = type; }
        public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }
        public void setBudget(String budget) { this.budget = budget; }
        public void setParticipants(String participants) { this.participants = participants; }
        public void setStatus(String status) { this.status = status; }
        public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
        public void setMarketHour(String marketHour) { this.marketHour = marketHour; }
        public void setTeamType(String teamType) { this.teamType = teamType; }

        @Override
        public String toString() {
            return "League{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", participants='" + participants + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }
}