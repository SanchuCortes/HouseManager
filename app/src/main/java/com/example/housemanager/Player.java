package com.example.housemanager.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "players",
        foreignKeys = @ForeignKey(
                entity = Team.class,
                parentColumns = "team_id",
                childColumns = "team_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("team_id"), @Index("position")}
)
public class Player {
    @PrimaryKey
    @ColumnInfo(name = "player_id")
    private int playerId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "position")
    private String position; // GK, DEF, MID, FWD

    @ColumnInfo(name = "team_id")
    private int teamId;

    @ColumnInfo(name = "team_name")
    private String teamName;

    @ColumnInfo(name = "nationality")
    private String nationality;

    @ColumnInfo(name = "shirt_number")
    private Integer shirtNumber;

    @ColumnInfo(name = "date_of_birth")
    private String dateOfBirth;

    @ColumnInfo(name = "current_price")
    private double currentPrice; // Precio actual en millones

    @ColumnInfo(name = "base_price")
    private double basePrice; // Precio inicial

    @ColumnInfo(name = "total_points")
    private int totalPoints; // Puntos acumulados en la temporada

    @ColumnInfo(name = "matches_played")
    private int matchesPlayed;

    @ColumnInfo(name = "goals")
    private int goals;

    @ColumnInfo(name = "assists")
    private int assists;

    @ColumnInfo(name = "yellow_cards")
    private int yellowCards;

    @ColumnInfo(name = "red_cards")
    private int redCards;

    @ColumnInfo(name = "clean_sheets")
    private int cleanSheets; // Para porteros y defensas

    @ColumnInfo(name = "is_available")
    private boolean isAvailable; // Si está disponible para transferencias

    @ColumnInfo(name = "is_injured")
    private boolean isInjured;

    @ColumnInfo(name = "form_rating")
    private double formRating; // Rating de forma actual (1-10)

    @ColumnInfo(name = "last_updated")
    private long lastUpdated;

    // Constructor vacío requerido por Room
    public Player() {}

    // Constructor básico
    public Player(int playerId, String name, String position, int teamId, String teamName) {
        this.playerId = playerId;
        this.name = name;
        this.position = position;
        this.teamId = teamId;
        this.teamName = teamName;
        this.isAvailable = true;
        this.isInjured = false;
        this.totalPoints = 0;
        this.matchesPlayed = 0;
        this.goals = 0;
        this.assists = 0;
        this.yellowCards = 0;
        this.redCards = 0;
        this.cleanSheets = 0;
        this.formRating = 5.0;
        this.lastUpdated = System.currentTimeMillis();

        // Asignar precio base según posición
        this.basePrice = calculateBasePrice(position);
        this.currentPrice = this.basePrice;
    }

    // Constructor completo
    public Player(int playerId, String name, String position, int teamId, String teamName,
                  String nationality, Integer shirtNumber, String dateOfBirth) {
        this(playerId, name, position, teamId, teamName);
        this.nationality = nationality;
        this.shirtNumber = shirtNumber;
        this.dateOfBirth = dateOfBirth;
    }

    // Calcular precio base según posición
    private double calculateBasePrice(String position) {
        if (position == null) return 10.0;

        switch (position.toUpperCase()) {
            case "GK":
                return 8.0; // 8M euros base para porteros
            case "DEF":
                return 10.0; // 10M euros base para defensas
            case "MID":
                return 12.0; // 12M euros base para medios
            case "FWD":
                return 15.0; // 15M euros base para delanteros
            default:
                return 10.0;
        }
    }

    // Getters
    public int getPlayerId() { return playerId; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public int getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public String getNationality() { return nationality; }
    public Integer getShirtNumber() { return shirtNumber; }
    public String getDateOfBirth() { return dateOfBirth; }
    public double getCurrentPrice() { return currentPrice; }
    public double getBasePrice() { return basePrice; }
    public int getTotalPoints() { return totalPoints; }
    public int getMatchesPlayed() { return matchesPlayed; }
    public int getGoals() { return goals; }
    public int getAssists() { return assists; }
    public int getYellowCards() { return yellowCards; }
    public int getRedCards() { return redCards; }
    public int getCleanSheets() { return cleanSheets; }
    public boolean isAvailable() { return isAvailable; }
    public boolean isInjured() { return isInjured; }
    public double getFormRating() { return formRating; }
    public long getLastUpdated() { return lastUpdated; }

    // Setters
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public void setName(String name) { this.name = name; }
    public void setPosition(String position) { this.position = position; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public void setShirtNumber(Integer shirtNumber) { this.shirtNumber = shirtNumber; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }
    public void setGoals(int goals) { this.goals = goals; }
    public void setAssists(int assists) { this.assists = assists; }
    public void setYellowCards(int yellowCards) { this.yellowCards = yellowCards; }
    public void setRedCards(int redCards) { this.redCards = redCards; }
    public void setCleanSheets(int cleanSheets) { this.cleanSheets = cleanSheets; }
    public void setAvailable(boolean available) { this.isAvailable = available; }
    public void setInjured(boolean injured) { this.isInjured = injured; }
    public void setFormRating(double formRating) { this.formRating = formRating; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    // Métodos helper
    public String getFormattedPrice() {
        return String.format("%.1fM €", currentPrice);
    }

    public String getFormattedStats() {
        return goals + "G/" + assists + "A";
    }

    public String getPositionDisplayName() {
        switch (position) {
            case "GK": return "Portero";
            case "DEF": return "Defensa";
            case "MID": return "Centrocampista";
            case "FWD": return "Delantero";
            default: return position;
        }
    }

    public double getPointsPerMatch() {
        if (matchesPlayed == 0) return 0.0;
        return (double) totalPoints / matchesPlayed;
    }

    public String getFormRatingDisplay() {
        if (formRating >= 8.0) return "Excelente";
        if (formRating >= 6.5) return "Buena";
        if (formRating >= 5.0) return "Regular";
        if (formRating >= 3.5) return "Mala";
        return "Pésima";
    }

    public boolean isGoodValue() {
        double expectedPrice = basePrice + (totalPoints * 0.1);
        return currentPrice < expectedPrice;
    }

    public void updatePrice() {
        // Algoritmo simple para actualizar precio según rendimiento
        double performanceMultiplier = 1.0;

        if (matchesPlayed > 0) {
            double avgPoints = (double) totalPoints / matchesPlayed;
            performanceMultiplier = 1.0 + (avgPoints - 5.0) * 0.1; // 5 pts es promedio
        }

        this.currentPrice = Math.max(basePrice * 0.5, basePrice * performanceMultiplier);
        this.currentPrice = Math.min(this.currentPrice, basePrice * 3.0); // Máximo 3x precio base
    }

    public void addMatchStats(int goals, int assists, int yellowCards, int redCards,
                              boolean cleanSheet, int points) {
        this.matchesPlayed++;
        this.goals += goals;
        this.assists += assists;
        this.yellowCards += yellowCards;
        this.redCards += redCards;
        if (cleanSheet && (position.equals("GK") || position.equals("DEF"))) {
            this.cleanSheets++;
        }
        this.totalPoints += points;

        // Actualizar form rating (últimos 5 partidos aproximadamente)
        this.formRating = (formRating * 0.8) + (points * 0.2);

        updatePrice();
        this.lastUpdated = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Player{" +
                "playerId=" + playerId +
                ", name='" + name + '\'' +
                ", position='" + position + '\'' +
                ", teamName='" + teamName + '\'' +
                ", totalPoints=" + totalPoints +
                ", currentPrice=" + currentPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return playerId == player.playerId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(playerId);
    }
}