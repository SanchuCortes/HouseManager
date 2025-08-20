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

    // Constructor vacío OBLIGATORIO para Room
    public PlayerEntity() {
    }

    // Constructor con parámetros básicos para crear jugadores fácilmente
    public PlayerEntity(int playerId, String name, int teamId, String teamName, String position) {
        this.playerId = playerId;
        this.name = name != null ? name : "";
        this.teamId = teamId;
        this.teamName = teamName != null ? teamName : "";
        this.position = position != null ? position : "";
        this.nationality = "España"; // Por defecto
        this.currentPrice = 10; // Precio base
        this.totalPoints = 0; // Sin puntos inicialmente
        this.available = true; // Disponible por defecto
    }

    // Getters - Room los necesita para mapear los datos
    public int getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public int getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getNationality() {
        return nationality;
    }

    public String getPosition() {
        return position;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public boolean isAvailable() {
        return available;
    }

    // Setters - Room los necesita para crear las entities
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setName(@NonNull String name) {
        this.name = name != null ? name : "";
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public void setTeamName(@NonNull String teamName) {
        this.teamName = teamName != null ? teamName : "";
    }

    public void setNationality(@NonNull String nationality) {
        this.nationality = nationality != null ? nationality : "España";
    }

    public void setPosition(@NonNull String position) {
        this.position = position != null ? position : "";
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = Math.max(1, currentPrice); // Mínimo 1M
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = Math.max(0, totalPoints); // Mínimo 0 puntos
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // Métodos auxiliares útiles
    public String getFormattedPrice() {
        return currentPrice + "M €";
    }

    public String getPositionShort() {
        switch (position) {
            case "Portero": return "POR";
            case "Defensa": return "DEF";
            case "Medio": return "MED";
            case "Delantero": return "DEL";
            default: return "???";
        }
    }

    // Para debug
    @Override
    public String toString() {
        return "PlayerEntity{" +
                "id=" + playerId +
                ", name='" + name + '\'' +
                ", team='" + teamName + '\'' +
                ", position='" + position + '\'' +
                ", price=" + currentPrice +
                ", points=" + totalPoints +
                ", available=" + available +
                '}';
    }

    // Para comparaciones
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerEntity that = (PlayerEntity) o;
        return playerId == that.playerId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(playerId);
    }
}