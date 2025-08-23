package com.example.housemanager.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

/** Relación de jugadores comprados por un usuario en un equipo concreto de una liga. */
@Entity(tableName = "user_team_players", primaryKeys = {"teamId", "leagueId", "playerId"})
public class UserTeamPlayerEntity {

    @ColumnInfo(name = "teamId")
    private int teamId;

    @ColumnInfo(name = "leagueId")
    private int leagueId;

    @ColumnInfo(name = "playerId")
    private long playerId;

    @ColumnInfo(name = "purchasePrice")
    private int purchasePrice;

    @ColumnInfo(name = "purchaseDate")
    private long purchaseDate;

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public int getLeagueId() { return leagueId; }
    public void setLeagueId(int leagueId) { this.leagueId = leagueId; }

    public long getPlayerId() { return playerId; }
    public void setPlayerId(long playerId) { this.playerId = playerId; }

    public int getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(int purchasePrice) { this.purchasePrice = purchasePrice; }

    public long getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(long purchaseDate) { this.purchaseDate = purchaseDate; }
}
