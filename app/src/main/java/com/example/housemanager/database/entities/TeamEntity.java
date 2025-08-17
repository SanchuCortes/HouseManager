package com.example.housemanager.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "teams")
public class TeamEntity {
    @PrimaryKey public int teamId;
    @NonNull public String name = "";
    public String crestUrl; // opcional
}
