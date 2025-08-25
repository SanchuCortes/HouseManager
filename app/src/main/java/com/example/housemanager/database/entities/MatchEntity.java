package com.example.housemanager.database.entities;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para almacenar partidos (para uso offline).
 */
@Entity(
        tableName = "matches",
        indices = {
                @Index(value = {"utcDateMillis"}),
                @Index(value = {"homeTeamId", "awayTeamId"})
        }
)
public class MatchEntity {

    @PrimaryKey
    @ColumnInfo(name = "matchId")
    private long matchId;

    @ColumnInfo(name = "homeTeamId")
    private long homeTeamId;

    @ColumnInfo(name = "awayTeamId")
    private long awayTeamId;

    @ColumnInfo(name = "homeTeamName")
    private String homeTeamName;

    @ColumnInfo(name = "awayTeamName")
    private String awayTeamName;

    // Fecha/hora en milisegundos desde epoch
    @ColumnInfo(name = "utcDateMillis")
    private long utcDateMillis;

    @ColumnInfo(name = "status")
    private String status;

    // Nulos si no jugado
    @Nullable
    @ColumnInfo(name = "homeScore")
    private Integer homeScore;

    @Nullable
    @ColumnInfo(name = "awayScore")
    private Integer awayScore;

    // Getters y setters
    public long getMatchId() { return matchId; }
    public void setMatchId(long matchId) { this.matchId = matchId; }

    public long getHomeTeamId() { return homeTeamId; }
    public void setHomeTeamId(long homeTeamId) { this.homeTeamId = homeTeamId; }

    public long getAwayTeamId() { return awayTeamId; }
    public void setAwayTeamId(long awayTeamId) { this.awayTeamId = awayTeamId; }

    public String getHomeTeamName() { return homeTeamName; }
    public void setHomeTeamName(String homeTeamName) { this.homeTeamName = homeTeamName; }

    public String getAwayTeamName() { return awayTeamName; }
    public void setAwayTeamName(String awayTeamName) { this.awayTeamName = awayTeamName; }

    public long getUtcDateMillis() { return utcDateMillis; }
    public void setUtcDateMillis(long utcDateMillis) { this.utcDateMillis = utcDateMillis; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Nullable
    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(@Nullable Integer homeScore) { this.homeScore = homeScore; }

    @Nullable
    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(@Nullable Integer awayScore) { this.awayScore = awayScore; }
}
