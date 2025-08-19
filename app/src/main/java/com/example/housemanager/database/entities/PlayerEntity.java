package com.example.housemanager.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "players")
public class PlayerEntity {

    @PrimaryKey
    @ColumnInfo(name = "playerId")
    private int playerId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name = "";

    @ColumnInfo(name = "teamId")
    private int teamId;

    @NonNull
    @ColumnInfo(name = "teamName")
    private String teamName = "";

    @NonNull
    @ColumnInfo(name = "nationality")
    private String nationality = "";

    @NonNull
    @ColumnInfo(name = "position")
    private String position = "";

    @ColumnInfo(name = "currentPrice")
    private int currentPrice;

    @ColumnInfo(name = "totalPoints")
    private int totalPoints;

    @ColumnInfo(name = "available")
    private boolean available;

    public PlayerEntity() {
    }

    // --- GETTERS compatibles con el repositorio ---
    public int getPlayerId() { return playerId; }
    public String getName() { return name; }
    public int getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public String getNationality() { return nationality; }
    public String getPosition() { return position; }
    public int getCurrentPrice() { return currentPrice; }
    public int getTotalPoints() { return totalPoints; }
    public boolean isAvailable() { return available; }

    // --- SETTERS (por si los necesitas al guardar/actualizar) ---
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public void setName(@NonNull String name) { this.name = name; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public void setTeamName(@NonNull String teamName) { this.teamName = teamName; }
    public void setNationality(@NonNull String nationality) { this.nationality = nationality; }
    public void setPosition(@NonNull String position) { this.position = position; }
    public void setCurrentPrice(int currentPrice) { this.currentPrice = currentPrice; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public void setAvailable(boolean available) { this.available = available; }
}
