package com.example.housemanager.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

/** Puntos de un jugador en un partido concreto (por jornada). */
@Entity(
        tableName = "PlayerMatchPoints",
        primaryKeys = {"matchId", "playerId"},
        indices = {
                @Index(value = {"matchday"}),
                @Index(value = {"playerId"})
        }
)
public class PlayerMatchPoints {
    @ColumnInfo(name = "matchId")
    private long matchId;

    @ColumnInfo(name = "playerId")
    private int playerId;

    @ColumnInfo(name = "matchday")
    private int matchday;

    @ColumnInfo(name = "points")
    private int points;

    public long getMatchId() { return matchId; }
    public void setMatchId(long matchId) { this.matchId = matchId; }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public int getMatchday() { return matchday; }
    public void setMatchday(int matchday) { this.matchday = matchday; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
