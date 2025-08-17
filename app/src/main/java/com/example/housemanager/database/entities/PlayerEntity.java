package com.example.housemanager.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "players")
public class PlayerEntity {
    @PrimaryKey public int playerId;
    @NonNull public String name = "";
    public int teamId;
    @NonNull public String teamName = "";
    @NonNull public String nationality = "";
    @NonNull public String position = "MID"; // GK/DEF/MID/FWD
    public double currentPrice = 5.0;
    public int totalPoints = 0;
    public boolean available = true;
}
