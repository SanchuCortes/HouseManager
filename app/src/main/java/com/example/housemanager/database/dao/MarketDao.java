package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.MarketListing;

import java.util.List;

@Dao
public interface MarketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MarketListing> listings);

    @Query("DELETE FROM MarketListing WHERE leagueId = :leagueId")
    void clearLeagueMarket(long leagueId);

    @Query("SELECT * FROM MarketListing WHERE leagueId = :leagueId AND isSold = 0 ORDER BY listedAtMillis ASC")
    LiveData<List<MarketListing>> getLiveListings(long leagueId);

    @Query("UPDATE MarketListing SET isSold = 1 WHERE leagueId = :leagueId AND playerId = :playerId AND isSold = 0")
    void markSold(long leagueId, long playerId);

    @Query("SELECT COUNT(*) FROM MarketListing WHERE leagueId = :leagueId AND isSold = 0")
    int getActiveListingsCountSync(long leagueId);

    // Proyección para el listado del mercado con puntos acumulados y nombre de equipo
    @Query("SELECT p.playerId AS playerId, p.name AS name, p.position AS position, p.teamId AS teamId, p.currentPrice AS price, " +
           "       COALESCE(t.name, p.teamName, '') AS teamDisplay, " +
           "       COALESCE(SUM(h.points), p.totalPoints, 0) AS displayPoints " +
           "FROM MarketListing ml " +
           "JOIN players p ON p.playerId = ml.playerId " +
           "LEFT JOIN teams t ON t.teamId = p.teamId " +
           "LEFT JOIN PlayerMatchPoints h ON h.playerId = p.playerId " +
           "WHERE ml.leagueId = :leagueId AND ml.isSold = 0 " +
           "GROUP BY p.playerId " +
           "ORDER BY p.currentPrice DESC")
    LiveData<java.util.List<com.example.housemanager.database.pojo.MarketPlayerRow>> getMarketRows(long leagueId);
}
