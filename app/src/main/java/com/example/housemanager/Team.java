package com.example.housemanager.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "teams")
public class Team {
    @PrimaryKey
    @ColumnInfo(name = "team_id")
    private int teamId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "short_name")
    private String shortName;

    @ColumnInfo(name = "tla")
    private String tla; // Código 3 letras

    @ColumnInfo(name = "crest_url")
    private String crestUrl;

    @ColumnInfo(name = "position")
    private int position; // Posición en la tabla

    @ColumnInfo(name = "points")
    private int points; // Puntos en la liga real

    @ColumnInfo(name = "played_games")
    private int playedGames;

    @ColumnInfo(name = "won")
    private int won;

    @ColumnInfo(name = "draw")
    private int draw;

    @ColumnInfo(name = "lost")
    private int lost;

    @ColumnInfo(name = "goals_for")
    private int goalsFor;

    @ColumnInfo(name = "goals_against")
    private int goalsAgainst;

    @ColumnInfo(name = "goal_difference")
    private int goalDifference;

    @ColumnInfo(name = "last_updated")
    private long lastUpdated;

    // Constructor vacío requerido por Room
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

    // Constructor completo
    public Team(int teamId, String name, String shortName, String tla, String crestUrl,
                int position, int points, int playedGames, int won, int draw, int lost,
                int goalsFor, int goalsAgainst, int goalDifference) {
        this.teamId = teamId;
        this.name = name;
        this.shortName = shortName;
        this.tla = tla;
        this.crestUrl = crestUrl;
        this.position = position;
        this.points = points;
        this.playedGames = playedGames;
        this.won = won;
        this.draw = draw;
        this.lost = lost;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.goalDifference = goalDifference;
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

    // Métodos helper
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

    @Override
    public String toString() {
        return "Team{" +
                "teamId=" + teamId +
                ", name='" + name + '\'' +
                ", tla='" + tla + '\'' +
                ", position=" + position +
                ", points=" + points +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return teamId == team.teamId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(teamId);
    }
}