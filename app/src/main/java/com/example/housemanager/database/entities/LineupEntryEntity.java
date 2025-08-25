package com.example.housemanager.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entrada de alineación por jugador (titular o suplente) para un partido.
 * Usamos tabla distinta a "lineups" para no interferir con el dashboard existente.
 */
@Entity(
        tableName = "lineup_entries",
        indices = {
                @Index(value = {"matchId"}),
                @Index(value = {"playerId"})
        }
)
public class LineupEntryEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "matchId")
    private long matchId;

    @ColumnInfo(name = "playerId")
    private int playerId;

    @ColumnInfo(name = "teamId")
    private int teamId;


    @ColumnInfo(name = "role")
    private String role;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMatchId() { return matchId; }
    public void setMatchId(long matchId) { this.matchId = matchId; }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
