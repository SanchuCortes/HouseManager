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

    @Query("UPDATE MarketListing SET isSold = 1 WHERE leagueId = :leagueId AND playerId = :playerId")
    void markSold(long leagueId, long playerId);

    @Query("SELECT COUNT(*) FROM MarketListing WHERE leagueId = :leagueId AND isSold = 0")
    int getActiveListingsCountSync(long leagueId);
}
