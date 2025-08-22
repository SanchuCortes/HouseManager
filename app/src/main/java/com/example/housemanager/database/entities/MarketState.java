package com.example.housemanager.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Estado del mercado por liga: expiración y última generación.
 */
@Entity(tableName = "MarketState")
public class MarketState {

    @PrimaryKey
    @ColumnInfo(name = "leagueId")
    private long leagueId;

    @ColumnInfo(name = "marketExpiresAtMillis")
    private long marketExpiresAtMillis;

    @ColumnInfo(name = "lastGeneratedAtMillis")
    private long lastGeneratedAtMillis;

    public long getLeagueId() { return leagueId; }
    public void setLeagueId(long leagueId) { this.leagueId = leagueId; }

    public long getMarketExpiresAtMillis() { return marketExpiresAtMillis; }
    public void setMarketExpiresAtMillis(long marketExpiresAtMillis) { this.marketExpiresAtMillis = marketExpiresAtMillis; }

    public long getLastGeneratedAtMillis() { return lastGeneratedAtMillis; }
    public void setLastGeneratedAtMillis(long lastGeneratedAtMillis) { this.lastGeneratedAtMillis = lastGeneratedAtMillis; }
}
