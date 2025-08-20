package com.example.housemanager.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "teams")
public class TeamEntity {

    @PrimaryKey
    @ColumnInfo(name = "teamId")
    private int teamId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name = "";

    @NonNull
    @ColumnInfo(name = "crestUrl")
    private String crestUrl = "";

    // Constructor vacío OBLIGATORIO para Room
    public TeamEntity() {
    }

    // Constructor con parámetros para crear equipos fácilmente
    public TeamEntity(int teamId, String name, String crestUrl) {
        this.teamId = teamId;
        this.name = name != null ? name : "";
        this.crestUrl = crestUrl != null ? crestUrl : "";
    }

    // Constructor solo con ID y nombre
    public TeamEntity(int teamId, String name) {
        this.teamId = teamId;
        this.name = name != null ? name : "";
        this.crestUrl = "";
    }

    // Getters que usa Room para mapear los datos
    public int getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public String getCrestUrl() {
        return crestUrl;
    }

    // Setters que usa Room para crear las entities
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public void setName(@NonNull String name) {
        this.name = name != null ? name : "";
    }

    public void setCrestUrl(@NonNull String crestUrl) {
        this.crestUrl = crestUrl != null ? crestUrl : "";
    }

    // Métodos auxiliares útiles
    public String getShortName() {
        if (name.length() <= 10) {
            return name;
        }
        // Crear nombre corto tomando las primeras letras
        String[] words = name.split(" ");
        if (words.length > 1) {
            StringBuilder shortName = new StringBuilder();
            for (String word : words) {
                if (word.length() > 0) {
                    shortName.append(word.charAt(0));
                }
            }
            return shortName.toString().toUpperCase();
        }
        return name.substring(0, Math.min(3, name.length())).toUpperCase();
    }

    public boolean hasCrest() {
        return crestUrl != null && !crestUrl.isEmpty();
    }

    // Para debug
    @Override
    public String toString() {
        return "TeamEntity{" +
                "id=" + teamId +
                ", name='" + name + '\'' +
                ", crestUrl='" + crestUrl + '\'' +
                '}';
    }

    // Para comparaciones
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamEntity that = (TeamEntity) o;
        return teamId == that.teamId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(teamId);
    }
}