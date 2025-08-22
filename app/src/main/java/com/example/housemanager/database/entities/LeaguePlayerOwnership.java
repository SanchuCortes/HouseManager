package com.example.housemanager.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

/**
 * Propiedad de un jugador dentro de una liga concreta.
 * PK compuesta: (leagueId, playerId)
 */
@Entity(
        tableName = "LeaguePlayerOwnership",
        primaryKeys = {"leagueId", "playerId"},
        indices = {
                @Index(value = {"leagueId"}),
                @Index(value = {"playerId"})
        }
)
public class LeaguePlayerOwnership {

    @ColumnInfo(name = "leagueId")
    private long leagueId;

    @ColumnInfo(name = "playerId")
    private long playerId;

    @ColumnInfo(name = "ownerUserId")
    private long ownerUserId;

    @ColumnInfo(name = "acquiredPrice")
    private int acquiredPrice;

    @ColumnInfo(name = "acquiredAtMillis")
    private long acquiredAtMillis;

    public long getLeagueId() { return leagueId; }
    public void setLeagueId(long leagueId) { this.leagueId = leagueId; }

    public long getPlayerId() { return playerId; }
    public void setPlayerId(long playerId) { this.playerId = playerId; }

    public long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(long ownerUserId) { this.ownerUserId = ownerUserId; }

    public int getAcquiredPrice() { return acquiredPrice; }
    public void setAcquiredPrice(int acquiredPrice) { this.acquiredPrice = acquiredPrice; }

    public long getAcquiredAtMillis() { return acquiredAtMillis; }
    public void setAcquiredAtMillis(long acquiredAtMillis) { this.acquiredAtMillis = acquiredAtMillis; }
}
