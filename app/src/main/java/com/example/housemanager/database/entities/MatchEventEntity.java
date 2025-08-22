package com.example.housemanager.database.entities;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Evento de partido relevante para el cálculo de puntos.
 */
@Entity(
        tableName = "match_events",
        indices = {
                @Index(value = {"matchId"}),
                @Index(value = {"playerId"})
        }
)
public class MatchEventEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "matchId")
    private long matchId;

    @ColumnInfo(name = "playerId")
    private int playerId;

    @ColumnInfo(name = "teamId")
    private int teamId;

    /** Tipo del evento: GOAL, YELLOW, RED */
    @ColumnInfo(name = "type")
    private String type;

    @Nullable
    @ColumnInfo(name = "minute")
    private Integer minute;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMatchId() { return matchId; }
    public void setMatchId(long matchId) { this.matchId = matchId; }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Nullable public Integer getMinute() { return minute; }
    public void setMinute(@Nullable Integer minute) { this.minute = minute; }
}
