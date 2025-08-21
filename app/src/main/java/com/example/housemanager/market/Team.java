package com.example.housemanager.market;

// IMPORTANTE: Esta clase NO debe tener NINGUNA anotación de Room
// Es solo un POJO simple para la UI

public class Team {
    private int teamId;
    private String name;
    private String shortName;
    private String tla;
    private String crestUrl;
    private int position;
    private int points;
    private int playedGames;
    private int won;
    private int draw;
    private int lost;
    private int goalsFor;
    private int goalsAgainst;
    private int goalDifference;
    private long lastUpdated;

    // Constructor vacío
    public Team() {}

    // Constructor básico
    public Team(int teamId, String name, String shortName, String tla, String crestUrl) {
        this.teamId = teamId;
        this.name = name;
        this.shortName = shortName;
        this.tla = tla;
        this.crestUrl = crestUrl;
        this.position = 0;
        this.points = 0;
        this.playedGames = 0;
        this.won = 0;
        this.draw = 0;
        this.lost = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.goalDifference = 0;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters
    public int getTeamId() { return teamId; }
    public String getName() { return name; }
    public String getShortName() { return shortName; }
    public String getTla() { return tla; }
    public String getCrestUrl() { return crestUrl; }
    public int getPosition() { return position; }
    public int getPoints() { return points; }
    public int getPlayedGames() { return playedGames; }
    public int getWon() { return won; }
    public int getDraw() { return draw; }
    public int getLost() { return lost; }
    public int getGoalsFor() { return goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public int getGoalDifference() { return goalDifference; }
    public long getLastUpdated() { return lastUpdated; }

    // Setters
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public void setName(String name) { this.name = name; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public void setTla(String tla) { this.tla = tla; }
    public void setCrestUrl(String crestUrl) { this.crestUrl = crestUrl; }
    public void setPosition(int position) { this.position = position; }
    public void setPoints(int points) { this.points = points; }
    public void setPlayedGames(int playedGames) { this.playedGames = playedGames; }
    public void setWon(int won) { this.won = won; }
    public void setDraw(int draw) { this.draw = draw; }
    public void setLost(int lost) { this.lost = lost; }
    public void setGoalsFor(int goalsFor) { this.goalsFor = goalsFor; }
    public void setGoalsAgainst(int goalsAgainst) { this.goalsAgainst = goalsAgainst; }
    public void setGoalDifference(int goalDifference) { this.goalDifference = goalDifference; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    // Métodos útiles
    public String getFormattedRecord() {
        return won + "W-" + draw + "D-" + lost + "L";
    }

    public String getFormattedGoals() {
        return goalsFor + ":" + goalsAgainst;
    }

    public double getWinPercentage() {
        if (playedGames == 0) return 0.0;
        return (double) won / playedGames * 100;
    }
}