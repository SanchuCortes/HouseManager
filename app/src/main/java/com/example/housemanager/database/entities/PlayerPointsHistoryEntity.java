package com.example.housemanager.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Historial de puntos por jugador en cada partido.
 */
@Entity(
        tableName = "player_points_history",
        indices = {
                @Index(value = {"matchId"}),
                @Index(value = {"playerId"})
        }
)
public class PlayerPointsHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "matchId")
    private long matchId;

    @ColumnInfo(name = "playerId")
    private int playerId;

    @ColumnInfo(name = "points")
    private int points;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMatchId() { return matchId; }
    public void setMatchId(long matchId) { this.matchId = matchId; }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
