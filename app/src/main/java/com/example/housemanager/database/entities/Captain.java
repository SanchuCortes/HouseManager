package com.example.housemanager.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Capitán por liga y usuario (uno por usuario en cada liga). */
@Entity(tableName = "Captains")
public class Captain {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "leagueId")
    private long leagueId;

    @ColumnInfo(name = "ownerUserId")
    private long ownerUserId;

    @ColumnInfo(name = "captainPlayerId")
    private int captainPlayerId;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getLeagueId() { return leagueId; }
    public void setLeagueId(long leagueId) { this.leagueId = leagueId; }
    public long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(long ownerUserId) { this.ownerUserId = ownerUserId; }
    public int getCaptainPlayerId() { return captainPlayerId; }
    public void setCaptainPlayerId(int captainPlayerId) { this.captainPlayerId = captainPlayerId; }
}
