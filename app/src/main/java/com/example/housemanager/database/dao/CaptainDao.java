package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.Captain;

@Dao
public interface CaptainDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long upsert(Captain captain);

    @Query("SELECT * FROM Captains WHERE leagueId = :leagueId AND ownerUserId = :ownerUserId LIMIT 1")
    LiveData<Captain> getCaptainLive(long leagueId, long ownerUserId);

    @Query("SELECT * FROM Captains WHERE leagueId = :leagueId AND ownerUserId = :ownerUserId LIMIT 1")
    Captain getCaptainSync(long leagueId, long ownerUserId);

    @Query("DELETE FROM Captains WHERE leagueId = :leagueId")
    void deleteCaptainByLeague(long leagueId);
}
