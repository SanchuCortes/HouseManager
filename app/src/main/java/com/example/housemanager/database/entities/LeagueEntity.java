package com.example.housemanager.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "leagues")
public class LeagueEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name = "";

    // "Privada" / "Comunitaria" u otro texto descriptivo
    @NonNull
    @ColumnInfo(name = "type")
    private String type = "Privada";

    // Presupuesto inicial en millones (p.ej. 150)
    @ColumnInfo(name = "budget")
    private int budget;

    @NonNull
    @ColumnInfo(name = "marketHour")
    private String marketHour = "14:00";

    @NonNull
    @ColumnInfo(name = "teamType")
    private String teamType = "Equipo Vacío";

    @ColumnInfo(name = "participants")
    private int participants = 1;

    @NonNull
    @ColumnInfo(name = "status")
    private String status = "Activa";

    @NonNull
    @ColumnInfo(name = "createdDate")
    private String createdDate = "";

    // Opcional: creador de la liga (email/usuario)
    @ColumnInfo(name = "creator")
    private String creator;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull public String getType() { return type; }
    public void setType(@NonNull String type) { this.type = type; }

    public int getBudget() { return budget; }
    public void setBudget(int budget) { this.budget = budget; }

    @NonNull public String getMarketHour() { return marketHour; }
    public void setMarketHour(@NonNull String marketHour) { this.marketHour = marketHour; }

    @NonNull public String getTeamType() { return teamType; }
    public void setTeamType(@NonNull String teamType) { this.teamType = teamType; }

    public int getParticipants() { return participants; }
    public void setParticipants(int participants) { this.participants = participants; }

    @NonNull public String getStatus() { return status; }
    public void setStatus(@NonNull String status) { this.status = status; }

    @NonNull public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(@NonNull String createdDate) { this.createdDate = createdDate; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
}
