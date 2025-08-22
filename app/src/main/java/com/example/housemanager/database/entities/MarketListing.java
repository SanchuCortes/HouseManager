package com.example.housemanager.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

/**
 * Listado de mercado por liga para un jugador.
 * PK compuesta: (leagueId, playerId)
 */
@Entity(
        tableName = "MarketListing",
        primaryKeys = {"leagueId", "playerId"},
        indices = {
            @Index(value = {"leagueId"}),
            @Index(value = {"expiresAtMillis"})
        }
)
public class MarketListing {

    @ColumnInfo(name = "leagueId")
    private long leagueId;

    @ColumnInfo(name = "playerId")
    private long playerId;

    @ColumnInfo(name = "listedAtMillis")
    private long listedAtMillis;

    @ColumnInfo(name = "expiresAtMillis")
    private long expiresAtMillis;

    @ColumnInfo(name = "isSold")
    private boolean isSold;

    public long getLeagueId() { return leagueId; }
    public void setLeagueId(long leagueId) { this.leagueId = leagueId; }

    public long getPlayerId() { return playerId; }
    public void setPlayerId(long playerId) { this.playerId = playerId; }

    public long getListedAtMillis() { return listedAtMillis; }
    public void setListedAtMillis(long listedAtMillis) { this.listedAtMillis = listedAtMillis; }

    public long getExpiresAtMillis() { return expiresAtMillis; }
    public void setExpiresAtMillis(long expiresAtMillis) { this.expiresAtMillis = expiresAtMillis; }

    public boolean isSold() { return isSold; }
    public void setSold(boolean sold) { isSold = sold; }
}
