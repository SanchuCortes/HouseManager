package com.example.housemanager.market;

// IMPORTANTE: Esta clase NO debe tener NINGUNA anotación de Room
// No @Entity, no @PrimaryKey, no @ColumnInfo, NADA de Room

public class Player {
    private int playerId;
    private String name;
    private String position;
    private int teamId;
    private String teamName;
    private String nationality;
    private Integer shirtNumber;
    private String dateOfBirth;
    private double currentPrice;
    private double basePrice;
    private int totalPoints;
    private int matchesPlayed;
    private int goals;
    private int assists;
    private int yellowCards;
    private int redCards;
    private int cleanSheets;
    private boolean isAvailable;
    private boolean isInjured;
    private double formRating;
    private long lastUpdated;

    // Constructor vacío
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
        this.basePrice = calculateBasePrice(position);
        this.currentPrice = this.basePrice;
    }

    private double calculateBasePrice(String position) {
        if (position == null) return 10.0;

        String pos = position.toUpperCase();
        if (pos.contains("PORTERO") || pos.contains("GK")) {
            return 8.0;
        } else if (pos.contains("DEFENSA") || pos.contains("DEF")) {
            return 10.0;
        } else if (pos.contains("MEDIO") || pos.contains("MID")) {
            return 12.0;
        } else if (pos.contains("DELANTERO") || pos.contains("FWD")) {
            return 15.0;
        }
        return 10.0;
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

    // Métodos útiles
    public String getFormattedPrice() {
        return String.format("%.1fM €", currentPrice);
    }

    public String getFormattedStats() {
        return goals + "G/" + assists + "A";
    }

    public String getPositionDisplayName() {
        if (position == null) return "Desconocida";

        if (position.equals("Portero") || position.equals("GK")) {
            return "Portero";
        } else if (position.equals("Defensa") || position.equals("DEF")) {
            return "Defensa";
        } else if (position.equals("Medio") || position.equals("MID")) {
            return "Centrocampista";
        } else if (position.equals("Delantero") || position.equals("FWD")) {
            return "Delantero";
        }
        return position;
    }
}