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

    public TeamEntity() {
    }

    // GETTERS que usa el repositorio / viewmodel
    public int getTeamId() { return teamId; }
    public String getName() { return name; }
    public String getCrestUrl() { return crestUrl; }

    // SETTERS por si haces inserts/updates manuales
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public void setName(@NonNull String name) { this.name = name; }
    public void setCrestUrl(@NonNull String crestUrl) { this.crestUrl = crestUrl; }
}
