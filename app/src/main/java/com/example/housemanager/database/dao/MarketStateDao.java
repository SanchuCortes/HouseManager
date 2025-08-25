package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.MarketState;

@Dao
public interface MarketStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(MarketState state);

    @Query("SELECT * FROM MarketState WHERE leagueId = :leagueId LIMIT 1")
    LiveData<MarketState> getLive(long leagueId);

    @Query("SELECT * FROM MarketState WHERE leagueId = :leagueId LIMIT 1")
    MarketState getSync(long leagueId);

    @Query("DELETE FROM MarketState WHERE leagueId = :leagueId")
    void deleteByLeague(long leagueId);
}
